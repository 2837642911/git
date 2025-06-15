package com.gxuwz.app.model.network;

import com.gxuwz.app.model.IApiResponse;

import java.util.ArrayList;
import java.util.List;

//新闻详情返回的数据
public class NewsDetailResponse implements IApiResponse {
    private String reason;
    private int error_code;
    private Result result;

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

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    @Override
    public boolean isSuccess() {
        return error_code == 0;
    }

    @Override
    public String getErrorMessage() {
        if (error_code != 0) {
            return reason != null ? reason : "未知错误";
        }
        return null;
    }

    public static class Result {
        private String uniquekey;
        private String content;
        private Detail detail;



        public String getUniquekey() {
            return uniquekey;
        }

        public void setUniquekey(String uniquekey) {
            this.uniquekey = uniquekey;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }


        public Detail getDetail() {
            return detail;
        }


        public void setDetail(Detail detail) {
            this.detail = detail;
        }
        public List<String> getContentImages() {
            List<String> images = new ArrayList<>();
            if (content == null || content.isEmpty()) {
                return images;
            }

            String imgTagStart = "<img";
            String srcAttribute = "src=\"";
            String srcAttributeAlt = "src='";

            int currentIndex = 0;

            // 循环查找所有<img>标签
            while (currentIndex < content.length()) {
                int imgStartIndex = content.indexOf(imgTagStart, currentIndex);
                if (imgStartIndex == -1) {
                    break; // 没有更多<img>标签
                }

                // 查找src属性（双引号版本）
                int srcStartIndex = content.indexOf(srcAttribute, imgStartIndex);
                int srcAltStartIndex = content.indexOf(srcAttributeAlt, imgStartIndex);

                // 确定使用哪个src属性
                int srcActualStartIndex = -1;
                String quoteChar = "";

                if (srcStartIndex != -1 && (srcAltStartIndex == -1 || srcStartIndex < srcAltStartIndex)) {
                    srcActualStartIndex = srcStartIndex + srcAttribute.length();
                    quoteChar = "\"";
                } else if (srcAltStartIndex != -1) {
                    srcActualStartIndex = srcAltStartIndex + srcAttributeAlt.length();
                    quoteChar = "'";
                }

                if (srcActualStartIndex != -1) {
                    // 查找对应的结束引号
                    int srcEndIndex = content.indexOf(quoteChar, srcActualStartIndex);
                    if (srcEndIndex != -1) {
                        String srcUrl = content.substring(srcActualStartIndex, srcEndIndex);
                        if (!srcUrl.isEmpty()) {
                            // 处理相对URL
                            if (srcUrl.startsWith("//")) {
                                srcUrl = "https:" + srcUrl;
                            } else if (srcUrl.startsWith("/")) {
                                srcUrl = "https://dfzximg01.dftoutiao.com" + srcUrl; // 根据实际域名调整
                            }
                            images.add(srcUrl);
                        }
                    }
                }

                // 移动到当前<img>标签之后，继续查找
                currentIndex = imgStartIndex + imgTagStart.length();
            }

            return images;
        }

    }

    public static class Detail {
        private String title;
        private String date;
        private String category;
        private String author_name;
        private String url;
        private String thumbnail_pic_s;
        private List<String> allImages;


        public List<String> getAllImages() {
            return allImages;
        }

        public void setAllImages(List<String> allImages) {
            this.allImages = allImages;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public String getAuthor_name() {
            return author_name;
        }

        public void setAuthor_name(String author_name) {
            this.author_name = author_name;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getThumbnail_pic_s() {
            return thumbnail_pic_s;
        }

        public void setThumbnail_pic_s(String thumbnail_pic_s) {
            this.thumbnail_pic_s = thumbnail_pic_s;
        }

    }



}