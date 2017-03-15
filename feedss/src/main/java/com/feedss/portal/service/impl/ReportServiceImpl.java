package com.feedss.portal.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.feedss.portal.common.Pager;
import com.feedss.portal.controller.domain.ReportParam;
import com.feedss.portal.entity.Feedback;
import com.feedss.portal.entity.Report;
import com.feedss.portal.repository.FeedbackRepository;
import com.feedss.portal.repository.ReportRepository;
import com.feedss.portal.service.ReportService;

import lombok.extern.slf4j.Slf4j;

/**
 * Created by shenbingtao on 2016/7/23.
 */
@Slf4j
@Service
public class ReportServiceImpl implements ReportService {

    @Resource
    private ReportRepository reportDao;
    
    @Resource
    private FeedbackRepository feedbackDao;

    @Override
    public Report add(Report one) {
        return reportDao.save(one);
    }

    @Override
    public List<Report> list(Pager pager, ReportParam other) {
        int pageNo = pager.getPage();
        int pageSize = pager.getPageSize();
        int total = 0;
        List<Report> list = null;
        if(StringUtils.isNotEmpty(other.getUserId())){
            list = reportDao.getListByPageUserId((pageNo - 1) * pageSize, pageSize, other.getUserId());
            total = reportDao.getTotalCountUserId(other.getUserId());
        }else if(StringUtils.isNotEmpty(other.getUserId())){
            list = reportDao.getListByPageToUserId((pageNo - 1) * pageSize, pageSize, other.getToUserId());
            total = reportDao.getTotalCountToUserId(other.getToUserId());
        }else if(StringUtils.isNotEmpty(other.getKeyword())){
            list = reportDao.getListByPageKeyword((pageNo - 1) * pageSize, pageSize, "%"+other.getKeyword()+"%");
            total = reportDao.getTotalCountKeyword(other.getKeyword());
        }else{
            list = reportDao.getListByPage((pageNo - 1) * pageSize, pageSize);
            total = reportDao.getTotalCount();
        }
        pager.setTotal(total);
        if(list == null){
            return new ArrayList<Report>();
        }
        return list;
    }

	@Override
	public Feedback add(Feedback one) {
		return feedbackDao.save(one);
	}
}
