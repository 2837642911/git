package com.gxuwz.app.model.bean;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "user")
public class User {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String phone;
    public String password;

    public User(String phone, String password) {
        this.phone = phone;
        this.password = password;
    }
}