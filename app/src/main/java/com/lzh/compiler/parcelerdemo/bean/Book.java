package com.lzh.compiler.parcelerdemo.bean;


import com.lzh.compiler.parceler.annotation.Arg;
import com.lzh.compiler.parceler.annotation.NonNull;

public class Book {
    @NonNull
    @Arg
    String username;
    @Arg
    private float price;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }
}
