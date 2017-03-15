package com.feedss.content.service;

import java.util.Arrays;
import java.util.List;

import javax.transaction.Transactional;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.feedss.content.entity.Banner;
import com.feedss.content.entity.Banner.BannerStatus;
import com.feedss.content.entity.Category;
import com.feedss.content.entity.Category.CategoryType;
import com.feedss.content.repository.BannerRepository;
import com.feedss.portal.util.CollectionUtil;

/**
 * baner服务<br>
 * 
 * @author wangjingqing
 * @date 2016-07-25
 */
@Component
public class BannerService {

	@Autowired
	private BannerRepository bannerRepository;
	@Autowired
	private CategoryService categoryService;

	public List<Banner> selectBannerByLocation(String location) {
		return bannerRepository.findByLocationAndStatus(location, Banner.BannerStatus.PUBLISHED.ordinal(), new Sort(Sort.Direction.ASC, "sort"));
	}

	/**
	 * 查询banner<br>
	 */
	public List<Banner> selectBanner() {
		return bannerRepository.selectBanner(new Sort(Sort.Direction.ASC, "sort"));
	}
	
	/**
	 * 查询banner<br>
	 * @param category 分类
	 * @param regions 区域列表
	 * @return Banner List
	 */
	public List<Banner> selectBanner(String category, String[] regions) {
		return bannerRepository.selectBannerByRegions(category, Arrays.asList(regions), new Sort(Sort.Direction.ASC, "sort"));
	}
	
	/**
	 * 查询banner<br>
	 * @param category 分类
	 * @param regions 区域列表
	 * @return Banner List
	 */
	public List<Banner> selectBannerLike(String category, String region) {
		return bannerRepository.selectBannerLikeRegion(category, region, new Sort(Sort.Direction.ASC, "sort"));
	}
	
	/**
	 * 查询banner<br>
	 * @param category 分类
	 * @return Banner List
	 */
	public List<Banner> selectBanner(String category) {
		return bannerRepository.selectBannerByCategory(category);
	}
	
	/**
	 * 查询banner<br>
	 * @param location banner显示位置
	 * @return 
	 */
	public JSONArray selectBannerGropyBy(String location) {
		JSONArray result = new JSONArray();
		List<Category> categories = categoryService.selectCategoryByType(CategoryType.AD);//广告分类
		for (Category category : categories) {
			List<Banner> banners = bannerRepository.findByLocationAndStatusAndCategory(
					location, BannerStatus.PUBLISHED.ordinal(), category.getUuid(), new Sort(Sort.Direction.ASC, "sort"));
			if(null != banners && banners.size() > 0){
				//对banner分组
				List<List<Banner>> split = CollectionUtil.split(banners, 6);

				JSONObject obj = new JSONObject();
				obj.put("banners", split);
				obj.put("category", category);
				result.add(obj);
			}
		}
		
		return result;
	}

	/**
	 * 保存或跟新<br>
	 * 
	 * @param banner
	 * @return
	 */
	public Banner save(Banner banner) {
		return bannerRepository.save(banner);
	}

	/**
	 * 查询最大排序<br>
	 * 
	 * @return
	 */
	public Integer selectMaxSort() {
		return bannerRepository.selecMaxSort();
	}

	public Banner selectByUUID(String bannerId) {
		return bannerRepository.findByUuid(bannerId);
	}

	@Transactional
	public Integer updateSortSubtract(Integer sort) {
		return bannerRepository.updateSortSubtract(sort);
	}

	@Transactional
	public boolean delete(String bannerId) {
		// 获取删除的banner
		Banner banner = selectByUUID(bannerId);
		if (banner == null) {
			return false;
		}
		// 删除banner
		banner.setStatus(BannerStatus.DELETE.ordinal());
		save(banner);
		// 跟新之后的sort
		updateSortSubtract(banner.getSort());
		return true;
	}

	public boolean updateSort(String bannerId, String sort) {
		Banner banner = new Banner();
		if (StringUtils.isNotBlank(bannerId)) {
			banner = selectByUUID(bannerId);
			if (banner == null) {
				return false;
			}
			banner.setSort(Integer.valueOf(sort));
			return null == save(banner) ? false : true;
		}
		return false;
	}
}
