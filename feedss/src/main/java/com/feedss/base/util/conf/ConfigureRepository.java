package com.feedss.base.util.conf;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.feedss.user.entity.Configure;

public interface ConfigureRepository extends CrudRepository<Configure, String> {

	// @Query("select cf from ConnectConfigure cf where cf.name= :name")
	public Configure findByName(@Param("name") String name);
	
	public Iterable<Configure> findByType(Integer type);
	
	public Iterable<Configure> findByCategory(Integer category);
}
