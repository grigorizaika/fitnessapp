package com.example.grigori.fitnessapp;

import android.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.ListView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PickFoodActivity extends AppCompatActivity {

    @BindView(R.id.food_list_lv)
    ListView mFoodListListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_food);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById (R.id.pick_food_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);




        // Display all foods from database in alphabetical order
    }
}
