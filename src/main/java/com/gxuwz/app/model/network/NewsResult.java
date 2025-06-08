package com.gxuwz.app.model.network;

import java.util.List;

public class NewsResult {
    private String stat;
    private List<NewsItem> data;
    private String page;
    private String pageSize;

    public String getStat() {
        return stat;
    }

    public void setStat(String stat) {
        this.stat = stat;
    }

    public List<NewsItem> getData() {
        return data;
    }

    public void setData(List<NewsItem> data) {
        this.data = data;
    }

    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public String getPageSize() {
        return pageSize;
    }

    public void setPageSize(String pageSize) {
        this.pageSize = pageSize;
    }
}