package com.feedss.base;

import java.io.Serializable;
import java.util.List;

/**
 * 分页<br>
 * @author wangjingqing
 * @date 2016-07-30
 *
 */
public class Pages<T> implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -429243047139400653L;

	private Integer totalCount;//总数量
	
	private Integer pageSize = 10;//默认每页显示条数
	
	private List<T> list;

	public Pages(Integer totalCcount,List<T> list){
		this.totalCount = totalCcount;
		this.list = list;
	}
	public Integer getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(Integer totalCount) {
		this.totalCount = totalCount;
	}

	public Integer getPageCount() {
		float page = (float)totalCount/pageSize;
		int size = (int)page;
		return page>size? size+1:size;
	}

	public Integer getPageSize() {
		return pageSize;
	}

	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}

	public List<T> getList() {
		return list;
	}

	public void setList(List<T> list) {
		this.list = list;
	}
}
