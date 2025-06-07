package com.gxuwz.app.model.bean;

import com.gxuwz.app.model.vo.BaseResponse;

public class NewsDetailResponse extends BaseResponse<NewsDetailResponse.Result> {
    public static class Result {
        private String uniquekey;
        private String content;
        private Detail detail;

        public String getUniquekey() {
            return uniquekey;
        }

        public String getContent() {
            return content;
        }

        public Detail getDetail() {
            return detail;
        }
    }

    public static class Detail {
        private String title;
        private String date;
        private String category;
        private String author_name;
        private String url;
        private String thumbnail_pic_s;

        public String getTitle() {
            return title;
        }

        public String getDate() {
            return date;
        }

        public String getCategory() {
            return category;
        }

        public String getAuthor_name() {
            return author_name;
        }

        public String getUrl() {
            return url;
        }

        public String getThumbnail_pic_s() {
            return thumbnail_pic_s;
        }
    }
}