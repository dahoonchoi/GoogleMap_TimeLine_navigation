package com.example.googlemap;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class TimelineColumn_ListAdapter extends ArrayAdapter<TimeLine> {


    private LayoutInflater mInflater;
    private ArrayList<TimeLine> timelines;
    private int mViewResourceId;

    public TimelineColumn_ListAdapter(Context context, int textViewResourceId, ArrayList<TimeLine> timelines){
        super(context, textViewResourceId,timelines);
        this.timelines = timelines;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mViewResourceId = textViewResourceId;
    }

    public View getView(int position, View convertView, ViewGroup parents){
        convertView = mInflater.inflate(mViewResourceId,null);

        TimeLine timeLine = timelines.get(position);
        String strColor = "#AEBD06";
        if(timeLine != null){
            TextView timeLineNum = (TextView) convertView.findViewById(R.id.timelist);
            TextView timeLineNum1 = (TextView) convertView.findViewById(R.id.timedate);

            if(timeLineNum != null){
                timeLineNum.setText((timeLine.getTnum()));

            }

            if(timeLineNum1 != null){
                timeLineNum1.setText((timeLine.getTnum1()));
            }

        }
        return convertView;
    }


}
