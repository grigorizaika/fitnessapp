package com.example.grigori.fitnessapp.Profile;


public class Post {
    String username;
    String userID;
    String uniqueID;
    String statusText;
    String dateString;
    long date;
    String photoUri;

    public Post(String username, String userID, String uniqueID, String statusText, String dateString, long date) {
        this.username = username;
        this.uniqueID = uniqueID;
        this.statusText = statusText;
        this.dateString = dateString;
        this.date = date;
        this.userID = userID;
    }

    public Post() {
    }

    public String getPhotoUri() {
        return photoUri;
    }

    public String getUsername() {
        return username;
    }

    public String getUniqueID() {
        return uniqueID;
    }

    public String getStatusText() {
        return statusText;
    }

    public String getDateString() {
        return dateString;
    }

    public String getUserID() {
        return userID;
    }

    public long getDate() {
        return date;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setUniqueID(String uniqueID) {
        this.uniqueID = uniqueID;
    }

    public void setStatusText(String statusText) {
        this.statusText = statusText;
    }

    public void setDateString(String dateString) {
        this.dateString = dateString;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public void setPhotoUri(String photoUri) {
        this.photoUri = photoUri;
    }


    public void setUserID(String userID) {
        this.userID = userID;
    }
}
