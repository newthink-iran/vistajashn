package com.models;

import java.io.Serializable;

/**
 * Created by Elyas on 2/6/2016.
 */
public class Setting implements Serializable{

    /**
     *
     */
    private static final long serialVersionUID = 6761346513269116884L;

    int setting_id;
    String about1;
    String about2;
    String about3;
    String about_img1;
    String about_img2;
    String about_img3;


    public void setAbout1(String about1) {
        this.about1 = about1;
    }

    public String getAbout1() {
        return about1;
    }

    public void setAbout2(String about2) {
        this.about2 = about2;
    }

    public String getAbout2() {
        return about2;
    }

    public void setAbout3(String about3) {
        this.about3 = about3;
    }

    public String getAbout3() {
        return about3;
    }

    public void setAbout_img1(String about_img1) {
        this.about_img1 = about_img1;
    }

    public String getAbout_img1() {
        return about_img1;
    }

    public void setAbout_img2(String about_img2) {
        this.about_img2 = about_img2;
    }

    public String getAbout_img2() {
        return about_img2;
    }

    public void setAbout_img3(String about_img3) {
        this.about_img3 = about_img3;
    }

    public String getAbout_img3() {
        return about_img3;
    }

    public void setSetting_id(int setting_id) {
        this.setting_id = setting_id;
    }

    public int getSetting_id() {
        return setting_id;
    }


}
