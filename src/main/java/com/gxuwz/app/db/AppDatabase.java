package com.gxuwz.app.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.gxuwz.app.dao.NewsHistoryDao;
import com.gxuwz.app.dao.UserDao;
import com.gxuwz.app.model.pojo.NewsHistory;
import com.gxuwz.app.model.pojo.User;

@Database(entities = {User.class,  NewsHistory.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase INSTANCE;

    public abstract UserDao userDao();
    public abstract NewsHistoryDao newsHistoryDao();
    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "news.db")
                            .allowMainThreadQueries()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}