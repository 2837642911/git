package com.gxuwz.app.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.gxuwz.app.model.bean.User;

@Dao
public interface UserDao {
    @Insert
    long insertUser(User user);

    @Query("SELECT * FROM user WHERE phone = :phone LIMIT 1")
    User getUserByPhone(String phone);
}