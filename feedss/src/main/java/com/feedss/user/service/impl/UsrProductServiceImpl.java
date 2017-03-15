package com.feedss.user.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.feedss.base.ErrorCode;
import com.feedss.base.Pages;
import com.feedss.user.entity.Account;
import com.feedss.user.entity.AccountTransaction;
import com.feedss.user.entity.AccountTransaction.AccoutTransactionSourceType;
import com.feedss.user.entity.ProductRecord;
import com.feedss.user.entity.ProductTransaction;
import com.feedss.user.entity.UserProduct;
import com.feedss.user.entity.UsrProduct;
import com.feedss.user.model.ProductVo;
import com.feedss.user.repository.AccountRepository;
import com.feedss.user.repository.AccountTransactionRepository;
import com.feedss.user.repository.ProductRecordRepostitory;
import com.feedss.user.repository.ProductTransactionRepostitory;
import com.feedss.user.repository.UserProductRepository;
import com.feedss.user.repository.UsrProductRepository;
import com.feedss.user.service.AccountService;
import com.feedss.user.service.UsrProductService;

import lombok.extern.slf4j.Slf4j;

/**
 * Created by qin.qiang on 2016/8/3 0003.
 */
@Slf4j
@Service
public class UsrProductServiceImpl implements UsrProductService {

	@Autowired
	private UsrProductRepository productRepository;
	@Autowired
	private ProductRecordRepostitory productRecordRepostitory;
	@Autowired
	private ProductTransactionRepostitory productTransactionRepostitory;

	@Autowired
	private UserProductRepository userProductRepository;

	@Autowired
	private AccountService accountService;

	@Autowired
	private AccountRepository accountRepository;
	@Autowired
	private AccountTransactionRepository accountTransactionRepository;

	@Override
	public List<ProductVo> getProductsByType(String type) {
		List<UsrProduct> productList = productRepository.getProductsByType(type);
		List<ProductVo> productVos = new ArrayList<ProductVo>();
		for (UsrProduct product : productList) {
			productVos.add(ProductVo.product2Vo(product));
		}
		return productVos;
	}

	@Override
	@Transactional
	public ErrorCode giveGift(String fromUserId, String toUserId, String productId, int num) {
		UsrProduct product = productRepository.findOne(productId);
		if (product == null) {
			return ErrorCode.INTERNAL_ERROR;// 商品获取失败
		}
		int totalAmount = product.getPrice() * num;
		// 保存交易记录
		ProductRecord productRecord = new ProductRecord(fromUserId, toUserId, productId, num, totalAmount);
		productRecord.setType(ProductRecord.RecordType.GIVE.name()); // 交易类型为赠送
		productRecord.setStatus(ProductRecord.GIVE_SUCCESS); // 0标识为未处理,1标识为已处理
		productRecordRepostitory.save(productRecord);

		/** 更新送礼物者信息 **/
		/*
		 * UserProduct fromUserProduct = userProductRepository.getUserProduct(fromUserId,productId); //获取赠送者当前礼物数量 if(fromUserProduct!=null && fromUserProduct.getProductNum()>=num){ //赠送者有此礼物,可以赠送 int
		 * productNum = fromUserProduct.getProductNum(); int lastNum = productNum-num; //保存变更记录 ProductTransaction productTransaction= new
		 * ProductTransaction(fromUserId,productId,"",productRecord.getUuid(),-num,productNum,lastNum); productTransactionRepostitory.save(productTransaction); //保存变更记录
		 * fromUserProduct.setProductNum(lastNum); userProductRepository.save(fromUserProduct);// 更新用户礼物数 }else
		 */
		{ // 购买此礼物.
			Account fromUserAccount = accountService.getAccountInfo(fromUserId);
			// 检查余额是否充足
			if (fromUserAccount == null || fromUserAccount.getBalance() < totalAmount) {
				return ErrorCode.NOT_ENOGH_BANLANCE;
			}
			int balance = fromUserAccount.getBalance();
			int lastBalance = balance - totalAmount;
			String name = AccoutTransactionSourceType.buyGift.getMsg() + product.getName();
			AccountTransaction accountTransaction = new AccountTransaction(
					fromUserId,
					fromUserAccount.getUuid(),
					AccoutTransactionSourceType.buyGift.name(),
					productRecord.getUuid(),
					-totalAmount,
					balance,
					lastBalance,
					AccountTransaction.UnExpiredTime,
					name);
			// 插入余额变更记录
			accountTransactionRepository.save(accountTransaction);
			// 更新余额
			fromUserAccount.setBalance(lastBalance);
			accountRepository.save(fromUserAccount);
			// 记录用户从系统里,购买了礼物
			ProductRecord productRecord1 = new ProductRecord("system", fromUserId, productId, num, totalAmount);
			productRecord1.setType(ProductRecord.RecordType.PURCHASE.name()); // 交易类型为购买
			productRecordRepostitory.save(productRecord1);

		}

		return ErrorCode.SUCCESS;
	}

	@Override
	public UsrProduct getProductById(String productId) {
		return productRepository.findOne(productId);
	}

	/**
	 * 根据送礼物记录,收礼物者异步接收礼物
	 */
	@Override
	@Transactional
	public void receiveGift() {
		List<ProductRecord> productRecords = productRecordRepostitory.findByTypeAndStatus(ProductRecord.RecordType.GIVE.name(), ProductRecord.GIVE_SUCCESS);
		if (productRecords != null && productRecords.size() > 0) {
			for (int i = 0; i < productRecords.size(); i++) {
				ProductRecord productRecord = productRecords.get(i);
				String toUserId = productRecord.getToUserId();
				String productId = productRecord.getProductId();
				int num = productRecord.getProductNum();
				log.info("excute receiveGift toUserId: " + toUserId + ",productId: " + productId + ",ProductNum :" + num);
				UserProduct receiveUserProduct = userProductRepository.getUserProduct(toUserId, productId);// 获取接收者该礼物数量
				if (receiveUserProduct == null) { // 没有该礼物
					ProductTransaction productTransaction = new ProductTransaction(toUserId, productId, "", productRecord.getUuid(), num, 0, num);
					productTransactionRepostitory.save(productTransaction); // 保存变更记录
					UserProduct userProduct = new UserProduct(toUserId, productId, num);
					userProductRepository.save(userProduct); // 新增一条用户礼物数据
				} else {
					int currProductNum = receiveUserProduct.getProductNum();
					int newProductNum = currProductNum + num;
					ProductTransaction productTransaction = new ProductTransaction(toUserId, productId, "", productRecord.getUuid(), num, currProductNum, newProductNum);
					// 保存礼物变更记录
					productTransactionRepostitory.save(productTransaction);

					// 更新礼物数量
					receiveUserProduct.setProductNum(newProductNum);
					userProductRepository.save(receiveUserProduct);
				}
				productRecord.setStatus(ProductRecord.RECEIVE_SUCCESS);
				productRecordRepostitory.save(productRecord);
				log.info("excute receiveGift toUserId: " + toUserId + " SUCCESS.");
			}
			log.info("excute receiveGift SUCCESS.");
		}
	}

	/**
	 * 分页查询商品<br>
	 * 
	 * @param category 分类，为空查询全部商品
	 * @param pageNo
	 * @param pageSize
	 */
	@Override
	public Pages<UsrProduct> selectProductList(String category, Integer pageNo, Integer pageSize, Integer status) {
		List<UsrProduct> list = new ArrayList<UsrProduct>();
		Sort sort = new Sort(Direction.DESC, "updated");// 排序
		PageRequest pageRequest = new PageRequest(pageNo, pageSize, sort);// 分页查询
		
		Page<UsrProduct> page;
		if(StringUtils.isBlank(category)){
			page = (null == status) ? productRepository.findAll(pageRequest) : productRepository.findAll(status, pageRequest);
		}else{
			page = (null == status) ? productRepository.findProductsByCategory(category, pageRequest)
					: productRepository.findProductsByCategory(category, status, pageRequest);
		}
		list = page.getContent();
		return new Pages<UsrProduct>((int) page.getTotalElements(), list);
	}
	
	/**
	 * 分页查询商品<br>
	 * 
	 * @param category 分类，为空查询全部商品
	 * @param pageNo
	 * @param pageSize
	 */
	@Override
	public Pages<UsrProduct> selectProductListByCategoryList(List<String> categoryList, Integer pageNo, Integer pageSize, Integer status) {
		List<UsrProduct> list = new ArrayList<UsrProduct>();
		Sort sort = new Sort(Direction.DESC, "updated");// 排序
		PageRequest pageRequest = new PageRequest(pageNo, pageSize, sort);// 分页查询
		
		Page<UsrProduct> page = productRepository.findProductsByCategory(categoryList, status, pageRequest);
		list = page.getContent();
		return new Pages<UsrProduct>((int) page.getTotalElements(), list);
	}

	@Transactional
	public boolean deleteUsrProducts(String[] ids) {
		for (String id : ids) {
			productRepository.updateStatus(id, 0);
		}
		return true;
	}
}
