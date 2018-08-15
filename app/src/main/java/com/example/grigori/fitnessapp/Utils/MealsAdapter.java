package com.example.grigori.fitnessapp.Utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.grigori.fitnessapp.R;

import org.w3c.dom.Text;

import java.util.ArrayList;


public class MealsAdapter extends BaseAdapter {

    String[] mMealNames;
    String[] mMealContentList;
    Context mContext;

    private static LayoutInflater inflater = null;

    public MealsAdapter (Context context, String[] mealNames, String[] mealContentList) {
        mContext = context;
        mMealNames = mealNames;
        mMealContentList = mealContentList;

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mMealNames.length;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public class Holder {
        TextView mealNameTextView;
        TextView mealContentTextView;
        ImageView addMealContentButton;
    }

    @Override
    public View getView(final int position, View view, ViewGroup viewGroup) {
        Holder holder = new Holder();

        View mealListItem;

        mealListItem = inflater.inflate(R.layout.meals_list_item, null);

        holder.mealNameTextView = (TextView) mealListItem.findViewById(R.id.meal_name_tv);
        holder.mealContentTextView = (TextView) mealListItem.findViewById(R.id.meal_content_tv);
        holder.addMealContentButton = (ImageButton) mealListItem.findViewById(R.id.add_meal_content_button);

        holder.mealNameTextView.setText(mMealNames[position]);
        holder.mealContentTextView.setText(mMealContentList[position]);
        holder.addMealContentButton.setImageResource(R.drawable.plus_ic);

        mealListItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(mContext, "You've clicked" + mMealNames[position], Toast.LENGTH_SHORT).show();

            }
        });

        return mealListItem;
    }
}
