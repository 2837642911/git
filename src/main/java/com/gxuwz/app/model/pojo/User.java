package com.gxuwz.app.model.pojo;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "user")
public class User {
    @PrimaryKey(autoGenerate = true)
    public int userId;

    public String phone;


    public String userName;
    public String password;

    public User(String phone, String password) {
        this.phone = phone;
        this.password = password;
    }


    public User(String userName, String password, String phone){
        this.phone = phone;
        this.password = password;
        this.userName=userName;
    }

    public User() {
    }


    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId){
        this.userId=userId;
    }


    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName= userName;
    }
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}