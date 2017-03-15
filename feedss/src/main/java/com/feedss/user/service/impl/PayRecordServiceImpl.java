package com.feedss.user.service.impl;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.feedss.user.entity.PayRecord;
import com.feedss.user.model.PayRecordVo;
import com.feedss.user.repository.PayRecordRepository;
import com.feedss.user.service.PayRecordService;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.HashMap;
import java.util.List;

/**
 * Created by qinqiang on 2016/8/7.
 */
@Service
public class PayRecordServiceImpl implements PayRecordService{

	@Autowired
	private PayRecordRepository payRecordRepository;

	@Autowired
	private EntityManager entityManager;

	@Override
	public PayRecord saveOrUpdate(PayRecord payRecord) {
		return payRecordRepository.save(payRecord);
	}

	@Override
	public PayRecord findByOrderNo(String orderNo) {
		return payRecordRepository.findByOrderNo(orderNo);
	}

	@Override
	public HashMap<String, Object> getPayRecordList(int pageNo, int pageSize, String orderNo,
	           String userId, String mobile, String sortDirection) {
		int start = (pageNo-1)*pageSize;
		String sql = "from PayRecord pr,Profile p,User u where pr.userId = p.userId and u.uuid=pr.userId and pr.payStatus = "+PayRecord.PAY_SUCCES;
		if(StringUtils.isNotBlank(orderNo)){
			sql = sql + " and pr.orderNo = '"+orderNo+"'";
        }
        if(StringUtils.isNotBlank(userId)){
	        sql = sql + " and u.uuid = '"+userId+"'";
        }
        if(StringUtils.isNotBlank(mobile)){
	        sql = sql + " and u.mobile = '"+mobile+"'";
        }
        if(StringUtils.isBlank(sortDirection) || "desc".equals(sortDirection.toLowerCase())){
	        sql = sql + " order by pr.created desc";
        }else{
	        sql = sql + " order by pr.created asc";
        }
        String select = "select new com.feedss.user.model.PayRecordVo(pr.orderNo,pr.outOrderNo,pr.userId,pr.payMethodId as payMethod,pr.currencyAmount,pr.moneyAmount,pr.created,p.nickname,u.mobile)  ";
		List<PayRecordVo> datalist = entityManager.createQuery(select+sql,PayRecordVo.class)
				                       .setFirstResult(start)
				                      .setMaxResults(pageSize)
				                     .getResultList();
		TypedQuery<Number> query =entityManager.createQuery("select count(1) as c "+sql,Number.class);
		int totalCount = query.getSingleResult().intValue();
//		System.out.println("totalCount :" + totalCount);
//		for(PayRecordVo payRecordVo :datalist){
//			System.out.println(payRecordVo.getOrderNo());
//		}
		HashMap<String,Object> data = new HashMap<String,Object>();
		data.put("totalCount",totalCount);
		data.put("list",datalist);
		return data;
	}
}
