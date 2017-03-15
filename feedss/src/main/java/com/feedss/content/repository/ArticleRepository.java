package com.feedss.content.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.feedss.content.entity.Article;

/**
 * 文章
 * 
 * @author : by AegisLee on 16/11/24.
 */
public interface ArticleRepository extends JpaRepository<Article, String> {

	@Query("select a from Article a where a.status != 0")
	public List<Article> selectArticleList();// 所有非删除的文章数据

	@Query("select a from Article a where a.status != 0")
	public Page<Article> selectArticlePages(Pageable pageable);// 所有非删除的文章数据

	@Query("select a from Article a where a.category = :category and a.status = :status")
	public Page<Article> selectArticlePages(@Param("category") String category, @Param("status") Integer status, Pageable pageable);// 所有非删除的文章数据

	@Query("select a from Article a where a.category = :category and a.status != 0")
	public Page<Article> selectArticlePagesByCategory(@Param("category") String category, Pageable pageable);// 所有非删除的文章数据

	@Query("select a from Article a where a.status = :status")
	public Page<Article> selectArticlePageByStatus(@Param("status") Integer status, Pageable pageable);// 所有非删除的文章数据

	@Query("select a from Article a where a.uuid = :articleId")
	public Article selectByUuid(@Param("articleId") String articleId); // 根据uuid查询详情

	// 更新文章状态: 草稿, 发布, 回收站, 已删除
	@Modifying
	@Query("update Article a set a.status=?2 where a.uuid=?1")
	public int updateStatus(String articleId, Integer status);

	@Query("select count(a) from Article a where a.status != 0")
	public Long selectArticleCount();

	@Query("select count(a) from Article a where a.category = ?1 and a.status=?2")
	public Long selectArticleCount(String category, Integer status);

	@Query("select count(a) from Article a where a.category = ?1 and a.status != 0")
	public Long selectArticleCountByCategory(String category);

	@Query("select count(a) from Article a where a.status = ?1")
	public Long selectArticleCountByStatus(Integer status);

	@Modifying
	@Query("update Article a set a.viewCount=viewCount+:count where a.uuid=:id")
	public void updateViewCount(@Param("id") String id, @Param("count") Integer count);

	// 更新文章分类
	@Modifying
	@Query("update Article a set a.status=?1 where a.category=?2")
	public int delCategory(Integer status, String categoryOld);
}
