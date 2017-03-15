package com.feedss.user.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.feedss.base.Constants;
import com.feedss.base.ErrorCode;
import com.feedss.base.JsonResponse;
import com.feedss.base.util.WeChatPayUtil;
import com.feedss.base.util.conf.ConfigureUtil;
import com.feedss.content.entity.Product;
import com.feedss.content.service.ProductService;
import com.feedss.user.entity.AccountTransaction.AccoutTransactionSourceType;
import com.feedss.user.entity.PayRecord;
import com.feedss.user.service.AccountService;
import com.feedss.user.service.PayRecordService;
import com.feedss.user.service.WeChatService;

/**
 * Created by qinqiang on 2016/8/7.
 */
@RestController
@RequestMapping("pay")
public class PayController {

	@Autowired
	private WeChatService weChatService;

	@Autowired
	private PayRecordService payRecordService;

	@Autowired
	private AccountService accountService;

	@Autowired
	private ProductService productService;

	@Autowired
	private ConfigureUtil configureUtil;

	private static final Logger logger = LoggerFactory.getLogger(PayController.class);

	/**
	 * 5.1微信下单接口
	 * 
	 * @param userId
	 * @param body
	 * @param request
	 * @return
	 */
	@RequestMapping("wechat/unifiedorder")
	public ResponseEntity<Object> weChatUnifiedOrder(HttpServletRequest request, @RequestBody String body,
			@RequestHeader String userId) {
		JSONObject jsonBody = JSON.parseObject(body);
		PayRecord payRecord = new PayRecord();
		String productId = jsonBody.getString("productId"); // 商品id
		int productCount = jsonBody.getIntValue("productCount"); // 商品数量
		String orderType = jsonBody.getString("orderType"); // 订单类型
		String orderName = jsonBody.getString("orderName"); // 下单人姓名
		String orderMobile = jsonBody.getString("orderMobile"); // 下单人联系方式
		int moneyAmount = jsonBody.getIntValue("moneyAmount");// 总金额 分 为单位
		int currencyAmount = jsonBody.getIntValue("currencyAmount");//虚拟币 总量

		AccoutTransactionSourceType type = null;
		try{
			if(StringUtils.isNotEmpty(orderType)){
				type = AccoutTransactionSourceType.valueOf(orderType);
			}
		}catch (Exception e) {
		}
		
		if(type==null){
			type = AccoutTransactionSourceType.charge;
		}
		if(type == AccoutTransactionSourceType.charge){ // 购买虚拟币
			if(currencyAmount <= 0){
				return JsonResponse.fail(ErrorCode.INVALIDPARAMETERS, "虚拟币数量不能少于0");
			}else{
				// 获取购买虚拟币数量
				int currencyRatio = 1;
				int moneyRatio = 1;
				String ratio = configureUtil.getConfig(Constants.RECHARGE_RATIO);
				if (StringUtils.isNotBlank(ratio) && ratio.indexOf(":") > 0) {
					moneyRatio = Integer.parseInt(ratio.split(":")[0]);
					currencyRatio = Integer.parseInt(ratio.split(":")[1]);
				}
				// 根据转换率 获取所需支付的人民币(分)
				moneyAmount = currencyAmount * moneyRatio * 100 / currencyRatio;
				payRecord.setPercent(ratio);
			}
			
		}else if(type == AccoutTransactionSourceType.buyProduct){ // 购买商品
			if (StringUtils.isNotEmpty(productId)) {
				Product p = productService.get(productId);
				if (p == null) {
					return JsonResponse.fail(ErrorCode.PRODUCT_NOT_EXIST);
				} else {
					if (productCount == 0)
						productCount = 1;
					BigDecimal price = p.getPrice();
					BigDecimal fenToYuan = new BigDecimal("100");
					BigDecimal price_fen = price.multiply(fenToYuan);
					if(jsonBody.containsKey("moneyAmount")){
						if (productCount * price_fen.intValue() != moneyAmount) {
							return JsonResponse.fail(ErrorCode.PRODUCT_PRICE_ERROR);
						}
					}else{
						moneyAmount = productCount * price_fen.intValue();
					}
				}
			}else{
				return JsonResponse.fail(ErrorCode.INVALID_PARAMETERS, "缺少商品标示");
			}
		}else {
			return JsonResponse.fail(ErrorCode.INVALID_PARAMETERS, "不支持该交易类型: " + orderType);
		}

		String orderNum = WeChatPayUtil.randomOrderNum();
		String ip = request.getRemoteAddr();
		// 执行微信下单
		TreeMap<String, String> data = weChatService.unifiedOrder(orderNum, moneyAmount, ip);
		if (data == null) {
			return JsonResponse.fail(ErrorCode.WEIXIN_ORDER_FAILURE);
		}
		data.put("orderNo", orderNum);
		// 生成交易记录
		payRecord.setUserId(userId);
		payRecord.setPayStatus(PayRecord.UN_PAY);
		payRecord.setOrderNo(orderNum);
		payRecord.setPayMethodId(PayRecord.PayMethod.WeChat.name());
		payRecord.setCurrencyAmount(currencyAmount);
		payRecord.setMoneyAmount(moneyAmount);
		payRecord.setType(orderType);
		Map<String, String> extMap = new HashMap<>();
		extMap.put("productId", productId);
		extMap.put("productCount", String.valueOf(productCount));
		extMap.put("orderName", orderName);
		extMap.put("orderMobile", orderMobile);

		payRecord.setExtAttr(JSONObject.toJSONString(extMap));

		payRecordService.saveOrUpdate(payRecord);

		return JsonResponse.success(data);
	}

	/**
	 * 微信异步通知
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("wechat/paynotify")
	public String weChatPayNotify(HttpServletRequest request, @RequestBody String body) {
		String xmldata = body;
		System.out.println("xmldata : " + xmldata);
		String successResult = "<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>";
		logger.info("**************微信支付-异步通知开始*******************");
		try {
			/** 获取异步通知内容 **/
			/*
			 * BufferedReader br = new BufferedReader(new
			 * InputStreamReader(request.getInputStream(),"UTF-8"));
			 * StringBuffer xmldata=new StringBuffer(); String buffer = null;
			 * while ((buffer = br.readLine()) != null) {
			 * xmldata.append(buffer); }
			 */
			/** 将异步通知请求内容转为map **/
			Map<String, String> data = WeChatPayUtil.xml2Map(xmldata.toString());
			logger.info("notify data: " + JSON.toJSONString(data));
			String signOri = data.get("sign");
			data.remove("sign");
			/** 验证签名 **/
			String sign = WeChatPayUtil.md5Sign(data, configureUtil.getConfig(Constants.WECHAT_KEY));
			logger.info("generate new sign: " + sign);
			if (!signOri.equals(sign)) {
				return "";
			}
			String result_code = data.get("result_code");
			String return_code = data.get("return_code");
			String out_trade_no = data.get("out_trade_no"); // 商户订单号
			String transaction_id = data.get("transaction_id"); // 微信订单号

			PayRecord payRecord = payRecordService.findByOrderNo(out_trade_no);
			/** 校验支付记录是否已经处理过 **/
			if (PayRecord.PAY_SUCCES == payRecord.getPayStatus()) {
				logger.info("订单状态已更新过,直接返回.");
				return successResult;
			}
			/** 支付后先将支付返回信息保存到记录中,方便排错 **/
			logger.info("保存返回信息,方便以后查错.");
			payRecord.setReturnMsg(xmldata.toString());
			payRecordService.saveOrUpdate(payRecord);

			if ("SUCCESS".equals(result_code) && "SUCCESS".equals(return_code)) {
				logger.info("支付成功.更新状态.");
				int status = weChatService.updatePaySuccess(out_trade_no, transaction_id);
				if (status == 0) {
					return successResult;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
		return "";
	}

	/***
	 * 5.2 微信订单查询接口
	 * 
	 * @return
	 */
	@RequestMapping("wechat/orderquery")
	public ResponseEntity<Object> orderQuery(HttpServletRequest request, @RequestBody String body) {
		HashMap<String, Object> data = new HashMap<String, Object>();
		JSONObject jsonBody = JSON.parseObject(body);
		String orderNo = jsonBody.getString("orderNo");
		logger.info("****************微信订单查询开始***********************");
		PayRecord payRecord = payRecordService.findByOrderNo(orderNo);
		logger.info("****************查询订单记录***********************");
		logger.info("payRecord: " + JSON.toJSONString(payRecord));
		/** 如果交易记录为支付成功,直接返回 **/
		if (PayRecord.PAY_SUCCES == payRecord.getPayStatus()) {
			logger.info("**************状态为支付成功,直接返回*******************");
			data.put("payStatus", "SUCCESS");
			data.put("balance", accountService.getAccountInfo(payRecord.getUserId()).getBalance());
			return JsonResponse.success(data);
		}
		// 根据本地订单号查询微信支付状态
		Map<String, String> queryMap = weChatService.queryOrder(orderNo);
		logger.info("查询微信订单状态,返回data: " + JSON.toJSONString(queryMap));
		if (queryMap == null) {
			return JsonResponse.fail(ErrorCode.INVALID_PARAMETERS); // 查询失败
		}
		String result_code = queryMap.get("result_code");
		String return_code = queryMap.get("return_code");
		String out_trade_no = queryMap.get("out_trade_no"); // 商户订单号
		String transaction_id = queryMap.get("transaction_id"); // 微信订单号
		String trade_state = queryMap.get("trade_state"); // 订单状态
		if ("SUCCESS".equals(result_code) && "SUCCESS".equals(return_code)) {
			if ("SUCCESS".equals(trade_state)) {
				logger.info("支付成功,更新订单状态.");
				int status = weChatService.updatePaySuccess(out_trade_no, transaction_id);
				if(status!=0){
					logger.error("更新订单状态失败, orderNo = " + out_trade_no + ", outOrderNo = " + transaction_id);
				}
			}
			data.put("payStatus", trade_state);
			data.put("balance", accountService.getAccountInfo(payRecord.getUserId()).getBalance());
			return JsonResponse.success(data);
		}
		return JsonResponse.fail(ErrorCode.FIND_ORDER_FAILURE);// 查询订单失败
	}

}
