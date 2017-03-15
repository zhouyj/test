package com.feedss.content.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.feedss.content.entity.ReleaseProduct;

/**
 * ReleaseProduct<br>
 * @author wangjingqing
 * @date 2016-07-30
 */
public interface ReleaseProductRepository extends JpaRepository<ReleaseProduct, String>{

	@Query("select rp from ReleaseProduct rp where rp.streamId = ?1 and rp.productId in ?2")
	public List<ReleaseProduct> selectList(String streamId,List<String> productIds);
	
	@Query("select count(rp) from ReleaseProduct rp where rp.streamId = ?1")
	public Integer selectCountByStreamId(String streamId);
	
	@Query("select rp from ReleaseProduct rp where rp.streamId=?1")
	public Page<ReleaseProduct> selectReleaseProductList(String streamId,Pageable pageable);
	
}
