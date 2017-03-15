package com.feedss.contact.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.feedss.contact.entity.Group;

public interface GroupRepository extends CrudRepository<Group, String>{
	@Query("select g.uuid from Group g where g.type= :type")
	public List<String> findByType(@Param("type")String type);
}
