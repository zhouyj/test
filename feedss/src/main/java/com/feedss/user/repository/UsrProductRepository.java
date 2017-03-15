package com.feedss.user.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.feedss.user.entity.UsrProduct;

/**
 * Created by qin.qiang on 2016/8/3 0003.
 */
public interface UsrProductRepository extends BaseRepository<UsrProduct> {

	@Query("select count(1) from UsrProduct p")
	public Integer selectCount();

	@Query("select p from UsrProduct p where p.type=:type order by p.uuid")
	public List<UsrProduct> getProductsByType(@Param("type") String type);

	@Query("select p from UsrProduct p where p.category=:category")
	public Page<UsrProduct> findProductsByCategory(@Param("category") String category, Pageable pageable);
	
	@Query("select p from UsrProduct p where p.status=:status and p.category in :categoryIds")
	public Page<UsrProduct> findProductsByCategory(@Param("categoryIds") List<String> categoryIds, @Param("status") int status, Pageable pageable);
	
	@Query("select p from UsrProduct p where p.status=:status and p.category=:category")
	public Page<UsrProduct> findProductsByCategory(@Param("category") String category, @Param("status") int status, Pageable pageable);
	
	@Query("select p from UsrProduct p where status=:status")
	public Page<UsrProduct> findAll(@Param("status") int status, Pageable pageable);
	
	// 更新商品状态: 草稿, 发布, 回收站, 已删除
	@Modifying
	@Query("update UsrProduct p set p.status=?2 where p.uuid=?1")
	public int updateStatus(String articleId, Integer status);
}
