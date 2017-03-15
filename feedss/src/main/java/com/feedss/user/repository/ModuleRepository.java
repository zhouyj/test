package com.feedss.user.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.feedss.user.entity.Module;

/**
 * 模块
 * @author 张杰
 *
 */
public interface ModuleRepository extends BaseRepository<Module>{

	
	@Query("select m from Module m where type=:type and status=0 order by id")
	List<Module> getByType(@Param("type")String type);
	
}
