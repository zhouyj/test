package com.feedss.portal.common;

import java.io.Serializable;

/**
 * 
 * @author shenbingtao
 * @createTime 2015年4月25日 下午11:32:32
 */
public class Pager implements Serializable {

    private static final long serialVersionUID = 1L;
    private int page = 1;
    private int pageSize = 10;
    private int total = 0;
    private int totalPage = 0;

    public Pager() {}

    public Pager(int page, int pageSize) {
        this(page, pageSize, 0);
    }

    public Pager(int page, int pageSize, int total) {
        if (page < 1) {
            page = 1;
        }
        if (pageSize < 1) {
            throw new IllegalArgumentException("Illegal pageSize: " + pageSize + ". pageSize can not less than 1.");
        }
        if (total < 0) {
            throw new IllegalArgumentException("Illegal total: " + total + ". total can not less than 0.");
        }
        this.page = page;
        this.pageSize = pageSize;
        this.total = total;
        this.totalPage = (total - 1) / pageSize + 1;
    }

    public void setPage(int page) {
        if (page < 1) {
            throw new IllegalArgumentException("Illegal page: " + page + ". page can not less than 1.");
        }
        this.page = page;
    }

    public int getPage() {
        return page;
    }

    public void setPageSize(int pageSize) {
        if (pageSize < 1) {
            throw new IllegalArgumentException("Illegal pageSize: " + pageSize + ". pageSize can not less than 1.");
        }
        this.pageSize = pageSize;
        totalPage = (total - 1) / pageSize + 1;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setTotal(int total) {
        if (total < 0) {
            throw new IllegalArgumentException("Illegal total: " + total + ". total can not less than 0.");
        }
        this.total = total;
        totalPage = (total - 1) / pageSize + 1;
    }

    public int getTotal() {
        return total;
    }

    public int getTotalPage() {
        return totalPage;
    }

    public boolean hasPreviousPage() {
        return page > 1 && page <= totalPage;
    }

    public boolean hasNextPage() {
        return page < totalPage;
    }

    public int getPreviousPage() {
        if (page > totalPage) {
            return totalPage;
        } else {
            if (hasPreviousPage()) {
                return page - 1;
            } else {
                return 1;
            }
        }
    }

    public int getNextPage() {
        if (page > totalPage) {
            return totalPage;
        } else {
            if (hasNextPage()) {
                return page + 1;
            } else {
                return totalPage;
            }
        }
    }

    public int getPageFirstIndex() {
        return pageSize * (page - 1);
    }

    public int getPageLastIndex() {
        if (page < totalPage) {
            return pageSize * (page - 1) + pageSize - 1;
        } else if (page == totalPage) {
            return total == 0 ? 0 : total - 1;
        } else {
            return -1;
        }
    }

    public void previousPage() {
        int previous = this.page - 1;
        if (previous < 1) {
            previous = 1;
        }
        setPage(previous);
    }

    public void nextPage() {
        setPage(this.page + 1);
    }

    public void firstPage() {
        setPage(1);
    }

    public void lastPage() {
        setPage(totalPage);
    }

    public void limit() {
        if (page > totalPage) {
            page = totalPage;
        }
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("{");
        buf.append("page: ").append(page);
        buf.append(", pageSize: ").append(pageSize);
        buf.append(", total: ").append(total);
        buf.append(", totalPage: ").append(totalPage);
        buf.append("}");
        return buf.toString();
    }


}
