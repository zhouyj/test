package com.feedss.content.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.feedss.content.entity.Product;

public interface ProductRepository extends JpaRepository<Product, String>{

	@Query("select p from Product p where p.productId in ?1 and p.creator=?2")
	public List<Product> selectProductInProductId(List<String> list,String userId);//根据商品ID查询商品信息
	
	@Modifying
	@Transactional
	@Query("update Product p set p.status=2 where p.creator=?1")
	public void updateStatusNotInProductId(String userId);//根据商品ID更新商品状态
	
	@Query("select count(p) from Product p where p.status=1 and p.creator=?1")
	public Integer selectCountByUserId(String userId);
	
	@Query("select count(p) from Product p where p.status=1")
	public Integer selectCount();
	
	@Query("select p from Product p where p.status=1 and p.creator=?1")
	public Page<Product> selectProductList(String userId,Pageable pageable);
	
	@Query("select p from Product p where p.status=1")
	public Page<Product> selectProductList(Pageable pageable);
	
}
