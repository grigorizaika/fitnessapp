package com.example.grigori.fitnessapp.Profile;

import com.example.grigori.fitnessapp.Nutrition.NutritionDiary;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by grigori on 9/28/17.
 */

public class UserProfile {
    String name;
    String email;
    String birthDate;
    String gender;
    String about;
    String profilePicUrl;
    String uid;

    NutritionDiary nutritionDiary;



    public UserProfile() {
    }


    public void setProfilePicUrl(String profilePicUrl) {
        this.profilePicUrl = profilePicUrl;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getGender() {
        return gender;
    }

    public String getAbout() {
        return about;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public String getProfilePicUrl() {
        return profilePicUrl;
    }

    public String getUid() {
        return uid;
    }
}
