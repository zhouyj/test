package com.feedss.user.service.impl;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang.StringUtils;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.feedss.base.Constants;
import com.feedss.base.util.WeChatPayUtil;
import com.feedss.base.util.conf.ConfigureUtil;
import com.feedss.user.entity.Account;
import com.feedss.user.entity.AccountTransaction;
import com.feedss.user.entity.AccountTransaction.AccoutTransactionSourceType;
import com.feedss.user.entity.PayRecord;
import com.feedss.user.repository.AccountRepository;
import com.feedss.user.repository.AccountTransactionRepository;
import com.feedss.user.repository.PayRecordRepository;
import com.feedss.user.service.AccountService;
import com.feedss.user.service.WeChatService;

/**
 * Created by qinqiang on 2016/8/7.
 */
@Service
public class WeChatServiceImpl implements WeChatService {
	private static final Logger debugLogger = LoggerFactory.getLogger(WeChatServiceImpl.class);

	private final ReentrantLock lock = new ReentrantLock();

	@Autowired
	private PayRecordRepository payRecordRepository;

	@Autowired
	private AccountTransactionRepository accountTransactionRepository;
	@Autowired
	private AccountRepository accountRepository;
	@Autowired
	private AccountService accountService;
	@Autowired
	private ConfigureUtil configureUtil;

	@Override
	public TreeMap<String, String> unifiedOrder(String orderno, int amont, String remoteIp) {
		String prepayid = submitWeChatOrder(orderno, amont, remoteIp);
		if (StringUtils.isBlank(prepayid)) {
			return null;
		}
		TreeMap<String, String> reqData = new TreeMap<String, String>();
		String randomStr = WeChatPayUtil.create_nonce_str();
		reqData.put("appid", configureUtil.getConfig(Constants.WECHAT_APPID));
		reqData.put("partnerid", configureUtil.getConfig(Constants.WECHAT_MERCHANTID));
		reqData.put("prepayid", prepayid);
		reqData.put("timestamp", System.currentTimeMillis() / 1000 + "");
		reqData.put("noncestr", randomStr);
		reqData.put("package", "Sign=WXPay");

		String sign = null;
		try {
			sign = WeChatPayUtil.md5Sign(reqData, configureUtil.getConfig(Constants.WECHAT_KEY));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		reqData.put("sign", sign);
		return reqData;
	}

	@Override
	@Transactional
	public int updatePaySuccess(String orderNo, String outOrderNo) {
		lock.lock();
		try {
			// 更新支付交易记录
			PayRecord payRecord = payRecordRepository.findByOrderNo(orderNo);
			if (payRecord.getPayStatus() == PayRecord.PAY_SUCCES) {
				return -1;
			}
			payRecord.setOutOrderNo(outOrderNo);
			payRecord.setPayStatus(PayRecord.PAY_SUCCES);
			payRecord.setUpdated(new Date());
			debugLogger.info("更新订单状态payRecord: " + JSON.toJSONString(payRecord));
			payRecordRepository.save(payRecord);
			
			Account account = accountRepository.findByUserId(payRecord.getUserId());
			int balance = accountService.getAccountInfo(payRecord.getUserId()).getBalance(); // 余额
			int increase = 0;
			int newBalance = balance;
			if (payRecord.getCurrencyAmount() > 0) {
			    increase = payRecord.getCurrencyAmount(); // 购买虚拟币数
				newBalance = balance + increase; // 最新值
				// 更新余额
				account.setBalance(newBalance);
				debugLogger.info("更新余额account: " + JSON.toJSONString(account));
				accountRepository.save(account);
			}
			String orderType = payRecord.getType();
			AccoutTransactionSourceType sourceType = AccoutTransactionSourceType.charge;
			if(StringUtils.isNotEmpty(orderType)){
				try{
					sourceType = AccoutTransactionSourceType.valueOf(orderType);
				}catch (Exception e) {
					debugLogger.error("交易类型未知", e);
					sourceType = AccoutTransactionSourceType.charge;
				}
			}
			if(sourceType == AccoutTransactionSourceType.charge){
				float moneyAmout_fen = (float)(payRecord.getMoneyAmount());
				float moneyAmount_yuan = (float)(moneyAmout_fen / 100);
				String name = sourceType.getMsg() + moneyAmount_yuan + "元";
				// 生成余额变化记录
				AccountTransaction transaction = new AccountTransaction(payRecord.getUserId(), account.getUuid(),
						sourceType.name(), payRecord.getUuid(), increase, balance, newBalance,
						AccountTransaction.UnExpiredTime, name);

				debugLogger.info("生成余额变更记录transaction: " + JSON.toJSONString(transaction));

				accountTransactionRepository.save(transaction);
			}
			
			return 0;
		} finally {
			lock.unlock();
		}

	}

	public String submitWeChatOrder(String orderno, int amont, String remoteIp) {
		debugLogger.info("******微信支付提交订单开始********");
		String returnCode = null;
		try {
			TreeMap<String, String> apiparamsMap = new TreeMap<String, String>();
			String body = configureUtil.getConfig(Constants.WECHAT_APPNAME);
			// 公众账号ID
			apiparamsMap.put("appid", configureUtil.getConfig(Constants.WECHAT_APPID));
			// 商户号
			apiparamsMap.put("mch_id", configureUtil.getConfig(Constants.WECHAT_MERCHANTID));
			// 商品描述
			apiparamsMap.put("body", body);
			// 随机字符串
			apiparamsMap.put("nonce_str", WeChatPayUtil.create_nonce_str());
			// 商户订单号
			apiparamsMap.put("out_trade_no", orderno);
			// 总金额
			apiparamsMap.put("total_fee", amont + "");
			// 终端 IP
			apiparamsMap.put("spbill_create_ip", remoteIp);
			// 通知地址
			apiparamsMap.put("notify_url", configureUtil.getConfig(Constants.WECHAT_NOTIFY_URL));
			String trade_type = "APP";
			// openid jsapi所需参数
			/*
			 * if(!StringUtils.isBlank(openid)){ apiparamsMap.put("openid",
			 * openid); trade_type = "JSAPI"; }
			 */
			// 交易类型
			apiparamsMap.put("trade_type", trade_type);
			// 签名
			String sign = WeChatPayUtil.md5Sign(apiparamsMap, configureUtil.getConfig(Constants.WECHAT_KEY));
			StringBuffer params = new StringBuffer("<xml>");
			params.append(WeChatPayUtil.map2Xml(apiparamsMap));
			apiparamsMap.clear();
			apiparamsMap.put("sign", sign);
			params.append(WeChatPayUtil.map2Xml(apiparamsMap));
			params.append("</xml>");
			debugLogger.info("params : " + params);

			CloseableHttpClient httpclient = HttpClients.createDefault();
			HttpPost httpPost = new HttpPost(configureUtil.getConfig(Constants.WECHAT_UNIFIED_ORDER));
			StringEntity stringEntity = new StringEntity(params.toString(),
					ContentType.create("text/xml", Consts.UTF_8));
			httpPost.setEntity(stringEntity);
			System.out.println(httpPost.getRequestLine());
			try {
				// 执行POST请求
				HttpResponse httpResponse = httpclient.execute(httpPost);
				// 获取响应消息实体
				HttpEntity entity = httpResponse.getEntity();
				// 响应状态
				System.out.println("status:" + httpResponse.getStatusLine());
				// 判断响应实体是否为空
				if (entity != null) {
					String content = new String(EntityUtils.toString(entity).getBytes("ISO-8859-1"), "UTF-8");
					debugLogger.info("response content:" + content);
					Map<String, String> resultMap = WeChatPayUtil.xml2Map(content);
					debugLogger.info("resultMap : " + resultMap);
					String return_code = resultMap.get("return_code");
					String return_msg = resultMap.get("return_msg");
					String result_code = resultMap.get("result_code");
					trade_type = resultMap.get("trade_type");
					String prepay_id = resultMap.get("prepay_id");
					if ("SUCCESS".equals(return_code) && "OK".equals(return_msg) && "SUCCESS".equals(result_code)) {
						debugLogger.info("******微信支付提交订单:" + orderno + "结果:成功********");
						returnCode = prepay_id;
					}
				}
			} catch (IOException e) {
				debugLogger.info(e.getMessage());
				throw e;
			} finally {
				try {
					// 关闭流并释放资源
					httpclient.close();
				} catch (IOException e) {
					throw e;
				}
			}
		} catch (Exception e) {
			debugLogger.info(e.getMessage());
			e.printStackTrace();
		}
		return returnCode;
	}

	/***
	 * 根据订单号.查询订单状态
	 * 
	 * @param orderNo
	 * @return
	 */
	public Map<String, String> queryOrder(String orderNo) {
		debugLogger.info("******微信支付查询订单开始********");
		try {
			TreeMap<String, String> apiparamsMap = new TreeMap<String, String>();
			// 公众账号ID
			apiparamsMap.put("appid", configureUtil.getConfig(Constants.WECHAT_APPID));
			// 商户号
			apiparamsMap.put("mch_id", configureUtil.getConfig(Constants.WECHAT_MERCHANTID));
			// 随机字符串
			apiparamsMap.put("nonce_str", WeChatPayUtil.create_nonce_str());
			// 商户订单号
			apiparamsMap.put("out_trade_no", orderNo);
			// 签名
			String sign = WeChatPayUtil.md5Sign(apiparamsMap, configureUtil.getConfig(Constants.WECHAT_KEY));
			StringBuffer params = new StringBuffer("<xml>");
			params.append(WeChatPayUtil.map2Xml(apiparamsMap));
			apiparamsMap.clear();
			apiparamsMap.put("sign", sign);
			params.append(WeChatPayUtil.map2Xml(apiparamsMap));
			params.append("</xml>");
			debugLogger.info("params : " + params);

			CloseableHttpClient httpclient = HttpClients.createDefault();
			HttpPost httpPost = new HttpPost(configureUtil.getConfig(Constants.WECHAT_ORDER_QUERY));
			StringEntity stringEntity = new StringEntity(params.toString(),
					ContentType.create("text/xml", Consts.UTF_8));
			httpPost.setEntity(stringEntity);
			System.out.println(httpPost.getRequestLine());
			try {
				// 执行POST请求
				HttpResponse httpResponse = httpclient.execute(httpPost);
				// 获取响应消息实体
				HttpEntity entity = httpResponse.getEntity();
				// 响应状态
				System.out.println("status:" + httpResponse.getStatusLine());
				// 判断响应实体是否为空
				if (entity != null) {
					String content = new String(EntityUtils.toString(entity).getBytes("ISO-8859-1"), "UTF-8");
					debugLogger.info("response content:" + content);
					Map<String, String> resultMap = WeChatPayUtil.xml2Map(content);
					debugLogger.info("resultMap : " + resultMap);
					return resultMap;
				}
			} catch (IOException e) {
				debugLogger.info(e.getMessage());
				throw e;
			} finally {
				try {
					// 关闭流并释放资源
					httpclient.close();
				} catch (IOException e) {
					throw e;
				}
			}
		} catch (Exception e) {
			debugLogger.info(e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

}
