package com.feedss.app;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ButtonRepository extends JpaRepository<Button, String> {
	List<Button> findByParentIdOrderBySort(String parentId);
}
