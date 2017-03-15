package com.feedss.content.repository;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.feedss.content.entity.Banner;

/**
 * banner<br>
 * 
 * @author wangjingqing
 * @date 2016-07-30
 */
public interface BannerRepository extends JpaRepository<Banner, String> {

	@Query("select b from Banner b where b.status=1")
	public List<Banner> selectBanner(Sort sort);// 排序;
	
	/**
	 * 根据地域查询对应分类广告
	 * @param category 分类
	 * @param regions 地域列表
	 * @param sort 排序
	 * @return Banner列表
	 */
	@Query("select b from Banner b where b.status=1 and b.category=?1 and b.region in ?2")
	public List<Banner> selectBannerByRegions(String category, List<String> regions, Sort sort);
	
	/**
	 * 根据地域查询对应分类广告
	 * @param category 分类
	 * @param regions 地域列表
	 * @param sort 排序
	 * @return Banner列表
	 */
	@Query("select b from Banner b where b.status=1 and b.category=?1 and b.region like %?2%")
	public List<Banner> selectBannerLikeRegion(String category, String region, Sort sort);
	
	/**
	 * 根据分类查询
	 * @param category 分类
	 * @return Banner列表
	 */
	@Query("select b from Banner b where b.status=1 and b.category=?1")
	public List<Banner> selectBannerByCategory(String category);

	@Query("select MAX(b.sort) from Banner b where b.status=1")
	public Integer selecMaxSort();

	public Banner findByUuid(String uuid);

	@Modifying
	@Query("update Banner b set b.sort = b.sort-1 where  b.sort >?1 and b.status = 1")
	public Integer updateSortSubtract(Integer sort);

	public List<Banner> findByLocationAndStatus(String location, int status, Sort sort);
	
	public List<Banner> findByLocationAndStatusAndCategory(String location, int status, String category, Sort sort);
}
