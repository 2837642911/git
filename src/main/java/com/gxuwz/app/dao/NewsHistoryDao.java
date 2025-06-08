package com.gxuwz.app.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.gxuwz.app.model.pojo.NewsHistory;

import java.util.List;

@Dao
public interface NewsHistoryDao {
    @Insert
    void insert(NewsHistory newsHistory);

    @Update
    void update(NewsHistory newsHistory);

    @Delete
    void delete(NewsHistory newsHistory);

    @Query("SELECT * FROM news_history WHERE userId = :userId ORDER BY viewTime DESC")
    List<NewsHistory> getNewsHistoryByUserId(int userId);

    @Query("SELECT * FROM news_history WHERE userId = :userId AND isFavorite = 1 ORDER BY viewTime DESC")
    List<NewsHistory> getFavoriteNewsByUserId(int userId);

    @Query("SELECT * FROM news_history WHERE uniquekey = :uniquekey AND userId = :userId LIMIT 1")
    NewsHistory getNewsHistory(String uniquekey, int userId);

    @Query("UPDATE news_history SET isFavorite = :isFavorite WHERE uniquekey = :uniquekey AND userId = :userId")
    void updateFavorite(String uniquekey, int userId, boolean isFavorite);

    @Query("SELECT * FROM news_history WHERE userId = :userId AND category = :category ORDER BY viewTime DESC")
    List<NewsHistory> getNewsHistoryByCategory(int userId, String category);

    @Query("SELECT DISTINCT category FROM news_history WHERE userId = :userId")
    List<String> getAllCategories(int userId);

    @Query("DELETE FROM news_history WHERE userId = :userId")
    void clearHistory(int userId);
}