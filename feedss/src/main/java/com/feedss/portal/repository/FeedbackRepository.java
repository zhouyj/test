package com.feedss.portal.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.feedss.portal.entity.Feedback;


@Repository
public interface FeedbackRepository extends CrudRepository<Feedback, String> {

}
