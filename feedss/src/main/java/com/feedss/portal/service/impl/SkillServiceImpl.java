package com.feedss.portal.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.feedss.portal.common.Pager;
import com.feedss.portal.entity.Skill;
import com.feedss.portal.repository.SkillRepository;
import com.feedss.portal.service.SkillService;

import lombok.extern.slf4j.Slf4j;

/**
 * Created by shenbingtao on 2016/7/23.
 */
@Slf4j
@Service
public class SkillServiceImpl implements SkillService {

    @Resource
    private SkillRepository shopContentDao;

    @Override
    public Skill addContent(String userId, String contentType, String content, String imgUrl) {
        Skill one = new Skill();
        one.setCreator(userId);
        one.setDescription(content);
        one.setType(contentType+"");
        one.setImgUrl(imgUrl);
        Date now = new Date();
        one.setCreated(now);
        one.setUpdated(now);
        return shopContentDao.save(one);
    }

    @Override
    public List<Skill> getContentList(String userId, String contentType, Pager pager) {
        int total = shopContentDao.getContentListTotalCount(userId, contentType);
        if (total > 0) {
            List<Skill> list = shopContentDao.getContentListByPage(userId, contentType, (pager.getPage() - 1) * pager.getPageSize(), pager.getPageSize());
            pager.setTotal(total);
            return list;
        }
        return new ArrayList<Skill>();
    }

    @Override
    public boolean delContent(String contentId) {
        shopContentDao.delete(contentId);
        return true;
    }

    @Override
    public int getTotalCount(String userId) {
        return shopContentDao.getContentListTotalCount(userId);
    }

    @Override
    public Skill getContent(String contentId) {
        return shopContentDao.findOne(contentId);
    }

}
