
package com.gxuwz.app.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.gxuwz.app.model.pojo.User;

import java.util.List;

@Dao
public interface UserDao {
    // 插入单个用户
    @Insert
    long insertUser(User user);


    // 根据手机号查询用户
    @Query("SELECT * FROM user WHERE phone = :phone LIMIT 1")
    User getUserByPhone(String phone);

    // 根据用户ID查询用户
    @Query("SELECT * FROM user WHERE userId = :userId LIMIT 1")
    User getUserById(int userId);


    // 根据用户名模糊查询用户
    @Query("SELECT * FROM user WHERE userName LIKE :keyword")
    List<User> searchUsersByKeyword(String keyword);

    // 更新用户信息
    @Update
    int updateUser(User user);

    // 根据用户ID更新用户名
    @Query("UPDATE user SET userName = :userName WHERE userId = :userId")
    int updateUserName(int userId, String userName);

    // 根据用户ID更新密码
    @Query("UPDATE user SET password = :newPassword WHERE userId = :userId")
    int updatePassword(int userId, String newPassword);

    // 根据用户ID删除用户
    @Query("DELETE FROM user WHERE userId = :userId")
    int deleteUserById(int userId);

    // 删除指定用户
    @Delete
    int deleteUser(User user);


}