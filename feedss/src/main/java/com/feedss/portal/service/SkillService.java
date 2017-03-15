package com.feedss.portal.service;

import com.feedss.portal.common.Pager;
import com.feedss.portal.entity.Skill;

import java.util.List;

public interface SkillService {
    public Skill addContent(String userId, String contentType, String content, String imgUrl);

    public List<Skill> getContentList(String userId, String contentType, Pager pager);

    public boolean delContent(String contentId);

    public int getTotalCount(String userId);

    public Skill getContent(String contentId);

}
