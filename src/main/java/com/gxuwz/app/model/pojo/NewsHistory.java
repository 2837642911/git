package com.gxuwz.app.model.pojo;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "news_history")

public class NewsHistory {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private int userId;
    private String uniquekey;
    private String title;

    private String category;
    private String thumbnail_pic_s;
    private String url;
    private String author_name;
    private String date;
    private long viewTime;
    private boolean isFavorite;

    public NewsHistory(int userId, String uniquekey, String title, String category,String thumbnail_pic_s,
                      String url, String author_name, String date) {
        this.userId = userId;
        this.uniquekey = uniquekey;
        this.title = title;
        this.thumbnail_pic_s = thumbnail_pic_s;
        this.url = url;
        this.author_name = author_name;
        this.date = date;
        this.viewTime = System.currentTimeMillis();
        this.category=category;
        this.isFavorite = false;
    }

    public String getCategory(){
        return category;
    }
    public void setCategory(String category){
        this.category=category;
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUniquekey() {
        return uniquekey;
    }

    public void setUniquekey(String uniquekey) {
        this.uniquekey = uniquekey;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getThumbnail_pic_s() {
        return thumbnail_pic_s;
    }

    public void setThumbnail_pic_s(String thumbnail_pic_s) {
        this.thumbnail_pic_s = thumbnail_pic_s;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAuthor_name() {
        return author_name;
    }

    public void setAuthor_name(String author_name) {
        this.author_name = author_name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public long getViewTime() {
        return viewTime;
    }

    public void setViewTime(long viewTime) {
        this.viewTime = viewTime;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }
} 