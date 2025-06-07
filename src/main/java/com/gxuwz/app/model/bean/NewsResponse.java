package com.gxuwz.app.model.bean;

import com.gxuwz.app.model.vo.BaseResponse;
import java.util.List;

public class NewsResponse extends BaseResponse<NewsResponse.Result> {
    private String reason;  // 聚合数据API的reason字段
    private int error_code; // 聚合数据API的error_code字段
    private Result result;  // 聚合数据API的result字段

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public int getError_code() {
        return error_code;
    }

    public void setError_code(int error_code) {
        this.error_code = error_code;
    }

    @Override
    public Result getData() {
        return result;  // 重写getData方法，返回result字段
    }

    @Override
    public void setData(Result data) {
        this.result = data;  // 重写setData方法，设置result字段
    }

    @Override
    public boolean isSuccess() {
        return error_code == 0;  // 聚合数据API成功时error_code为0
    }

    @Override
    public String getErrorMessage() {
        if (error_code != 0) {
            return reason != null ? reason : "未知错误";
        }
        return null;
    }

    public static class Result {
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
}