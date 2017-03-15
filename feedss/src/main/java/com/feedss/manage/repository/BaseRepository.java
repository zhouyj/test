package com.feedss.manage.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;


@NoRepositoryBean
public interface BaseRepository<T extends com.feedss.base.BaseEntity> extends JpaRepository<T, String> {

}
