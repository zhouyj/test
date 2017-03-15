package com.feedss.portal.controller.domain;

import lombok.Data;

/**
 * Created by shenbingtao on 2016/8/7.
 */
@Data
public class SkillParam {

	private String userId;

	private String contentId;

	private String type;

	private String description;

	private String imgUrl;

	private Integer pageNo;

	private Integer pagSize;

}
