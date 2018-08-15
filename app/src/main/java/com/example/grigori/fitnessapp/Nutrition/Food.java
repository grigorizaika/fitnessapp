package com.example.grigori.fitnessapp.Nutrition;

/**
 * Created by grigori on 9/28/17.
 */

public class Food {
    String name;
    float barcode;
    String keyword;
    String brand;
    Nutrition nutrition;

    public Food(String name) {
        this.name = name;
    }

    public Nutrition getNutrition() {
        return nutrition;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setBarcode(float barcode) {
        this.barcode = barcode;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getName() {
        return name;
    }

    public float getBarcode() {
        return barcode;
    }

    public String getKeyword() {
        return keyword;
    }

    public String getBrand() {
        return brand;
    }
}
