package com.feedss.user.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.feedss.user.entity.UserProduct;
import com.feedss.user.entity.UserRelation.RelationType;

import java.util.List;

/**
 * Created by qin.qiang on 2016/8/4 0004.
 */
public interface UserProductRepository extends BaseRepository<UserProduct> {
	@Query("select p from UserProduct p where p.userId=:userId order by p.uuid")
	public List<UserProduct> getProductsByUserId(@Param("userId") String userId);
	
	@Query("select p from UserProduct p where status=:status")
	public Page<UserProduct> getProductsByStatus(@Param("status") int status, Pageable pageable);

	@Query("select p from UserProduct p where p.userId=:userId and p.productId=:productId ")
	public UserProduct getUserProduct(@Param("userId") String userId, @Param("productId") String productId);

	/**
	 * 根据 关系主体 查询总数
	 * 
	 * @param userId
	 * @param type {@link RelationType}
	 * @return
	 */
	@Query("select sum(p.productNum) from UserProduct p where p.userId=:userId ")
	Long getSumByUserId(@Param("userId") String userId);
}
