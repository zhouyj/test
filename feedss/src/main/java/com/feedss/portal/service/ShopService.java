package com.feedss.portal.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.feedss.base.ErrorCode;
import com.feedss.user.entity.Account;
import com.feedss.user.entity.UserProduct;
import com.feedss.user.entity.UsrProduct;
import com.feedss.user.repository.AccountRepository;
import com.feedss.user.repository.UserProductRepository;
import com.feedss.user.repository.UsrProductRepository;
import com.feedss.user.service.AccountService;

@Service
public class ShopService {
	
	@Autowired
	private UsrProductRepository productRepository;
	@Autowired
	private UserProductRepository userProductRepository;
	@Autowired
	private AccountService accountService;
	@Autowired
	private AccountRepository accountRepository;
	
	@Transactional
	public ErrorCode buy(String userId, String productId, int num){
		UsrProduct product = productRepository.findOne(productId);
		if (product == null) {
			return ErrorCode.INTERNAL_ERROR;// 商品获取失败
		}
		
		int totalAmount = product.getPrice() * num;
		
		Account account = accountService.getAccountInfo(userId);
		if(null == account){
			return ErrorCode.NOT_ENOGH_BANLANCE;
		}
		int balance = account.getBalance();
		if(balance < totalAmount){
			return ErrorCode.NOT_ENOGH_BANLANCE;
		}
		int lastBalance = balance - totalAmount;
		// 更新余额
		account.setBalance(lastBalance);
		accountRepository.save(account);
		
		//记录兑换
		UserProduct userProduct = new UserProduct(userId, productId, num);
		userProductRepository.save(userProduct);
		
		return ErrorCode.SUCCESS;
	}
}
