package com.feedss.portal.service;

import java.util.List;

import com.feedss.portal.common.Pager;
import com.feedss.portal.controller.domain.ReportParam;
import com.feedss.portal.entity.Feedback;
import com.feedss.portal.entity.Report;

public interface ReportService {

    public Report add(Report one);

    public List<Report> list(Pager pager, ReportParam other);
    
    public Feedback add(Feedback one);

}
