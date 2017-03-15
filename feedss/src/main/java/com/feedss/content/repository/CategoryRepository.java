package com.feedss.content.repository;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.feedss.content.entity.Category;

/**
 * 分类<br>
 * @author wangjingqing
 * @date 2016-07-30
 */
public interface CategoryRepository extends JpaRepository<Category, String>{

	/**
	 * 根据类型查询<be>
	 * 取所有的叶子节点，app
	 * @param types
	 * @param sort
	 * @return
	 */
	@Query("select c from Category c where c.type in ?1 and c.status=0 and c.visiable=?2 and parentId is not null")
	public List<Category> selectChildCategoryList(List<String> types,boolean visiable, Sort sort);//排序;
	
	/**
	 * 根据类型查询<be>
	 * @param types
	 * @param sort
	 * @return
	 */
	@Query("select c from Category c where c.type in ?1 and c.status=0 and c.visiable=?2")
	public List<Category> selectCategoryList(List<String> types,boolean visiable, Sort sort);//排序;
	
	/**
	 * 查询主页模块化显示的分类列表
	 * @param parentId
	 * @param visiable
	 * @param sort
	 * @return
	 */
	@Query("select c from Category c where showInHomePageModel=1 and c.status=0 and c.visiable=1")
	public List<Category> selectCategoryShowInMainPage(Sort sort);
	
	@Query("select c from Category c where showInRightModel=1 and c.status=0 and c.visiable=1")
	public List<Category> selectCategoryShowInRightPage(Sort sort);
	
	//查询sort最大值
	@Query("select max(sort) from Category c where c.visiable=true and c.status=0 and parentId=?1")
	public Integer selectMaxSort(String parentId);
	
	@Modifying
	@Query("update Category c set c.sort = c.sort-1 where  c.sort >?1 and c.status=0 and c.visiable = true")
	public Integer updateSortSubtract(Integer sort);
	
	@Modifying
//	@Query("update Category c set c.streamCount = c.streamCount + ?1,c.backCount = c.backCount+?2 where c.uuid = ?3")
	@Query("update Category c set c.streamCount = ?1,c.backCount = ?2 where c.uuid = ?3")
	public Integer updateStreamCount(int streamCount,int backCount,String categoryId);
	
	
//	public Integer updateStreamCount(int streamCount, int backCount, String categoryId);
	
	public Category findByTagsAndVisiable(String tags,boolean visiable);
}
