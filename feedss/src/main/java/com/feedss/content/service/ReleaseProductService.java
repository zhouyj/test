package com.feedss.content.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Component;

import com.feedss.base.Pages;
import com.feedss.content.entity.ReleaseProduct;
import com.feedss.content.repository.ReleaseProductRepository;

/**
 * 发布商品<br>
 * @author wangjingqing
 * @date 2016-07-20
 */
@Component
public class ReleaseProductService{
	
	Log logger = LogFactory.getLog(getClass());
	
	@Autowired
	private ReleaseProductRepository releaseProductRepository;
	
	/**
	 * 查询商品在当前视频发布的信息<br>
	 * @param streamId
	 * @param productIds
	 * @return
	 */
	public List<ReleaseProduct> selectReleaseProduct(String streamId,List<String> productIds){
		return releaseProductRepository.selectList(streamId, productIds);
	}
	/**
	 * 批量存储ReleaseProduct<br>
	 * @param products
	 * @return
	 */
	public List<ReleaseProduct> saveReleaseProduct(List<ReleaseProduct> products){
		return releaseProductRepository.save(products);
	}
	
	public Pages<ReleaseProduct> selectReleaseProductList(String streamId,Integer pageNo,Integer pageSize){
		Integer count = releaseProductRepository.selectCountByStreamId(streamId);
		List<ReleaseProduct> list = new ArrayList<ReleaseProduct>();
		//step:1 判断是否存在预告
		if(count > 0){
			Sort sort = new Sort(Direction.DESC,"updated");//排序
			PageRequest pageRequest = new PageRequest(pageNo, pageSize,sort);//分页查询
			Page<ReleaseProduct> page = releaseProductRepository.selectReleaseProductList(streamId, pageRequest);
			list = page.getContent();
		}
		return new Pages<ReleaseProduct>(count, list);
	}
	/**
	 * 发布商品个数<br>
	 * @param streamId
	 * @return
	 */
	public Integer selectCount(String streamId){
		return releaseProductRepository.selectCountByStreamId(streamId);
	}
}
