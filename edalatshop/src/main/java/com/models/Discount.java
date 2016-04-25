package com.models;

import java.io.Serializable;

/**
 * Created by Elyas on 2/6/2016.
 */
public class Discount implements Serializable{

    /**
     *
     */
    private static final long serialVersionUID = 6761346513269116884L;

    int discount_id;
    int store_id;
    String discount_content;
    String discount_title;
    String photo_url;
    int created_at;
    int updated_at;
    int is_deleted;
    String discount_val;
    int exp_date;
    int viewCount;

    public void setDiscount_content(String discount_content) {
        this.discount_content = discount_content;
    }

    public String getDiscount_content() {
        return discount_content;
    }

    public void setDiscount_title(String discount_title) {
        this.discount_title = discount_title;
    }

    public String getDiscount_title() {
        return discount_title;
    }

    public void setPhoto_url(String photo_url) {
        this.photo_url = photo_url;
    }

    public String getPhoto_url() {
        return photo_url;
    }

    public void setDiscount_id(int discount_id) {
        this.discount_id = discount_id;
    }

    public int getDiscount_id() {
        return discount_id;
    }

    public void setStore_id(int store_id) {
        this.store_id = store_id;
    }

    public int getStore_id() {
        return store_id;
    }


    public void setCreated_at(int created_at) {
        this.created_at = created_at;
    }

    public int getCreated_at() {
        return created_at;
    }


    public void setUpdated_at(int updated_at) {
        this.updated_at = updated_at;
    }

    public int getUpdated_at() {
        return updated_at;
    }


    public void setIs_deleted(int is_deleted) {
        this.is_deleted = is_deleted;
    }

    public int getIs_deleted() {
        return is_deleted;
    }

    public void setDiscount_val(String discount_val) {
        this.discount_val = discount_val;
    }

    public String getDiscount_val() {
        return discount_val;
    }

    public void setExp_date(int exp_date) {
        this.exp_date = exp_date;
    }

    public int getExp_date() {
        return exp_date;
    }

    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }

    public int getViewCount() {
        return viewCount;
    }
}
