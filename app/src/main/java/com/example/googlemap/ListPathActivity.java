package com.example.googlemap;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class ListPathActivity extends AppCompatActivity {
    private ListView listView;
    DatabaseHelper mDatabaseHelper;
    ArrayList<TimeLine> timelineList;
    TimeLine timeline;


    ArrayList<Path> pathList;
    Path path;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pathlist_layout);

        mDatabaseHelper = new DatabaseHelper(this);

        viewData();
        Cursor data = mDatabaseHelper.getListContents1();

        if (data.getCount() == 0){
            Toast.makeText(getApplicationContext(),"데이터가 없습니다",Toast.LENGTH_SHORT).show();
            onBackPressed();
        }
        else {
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


                    pathList = new ArrayList<>();
//                Toast.makeText(getApplicationContext(), timelineList.get(position).getTnum(), Toast.LENGTH_SHORT).show();

                    String timelinenum = timelineList.get(position).getTnum();
                    String courrenttime = timelineList.get(position).getTnum1();
                    Toast.makeText(getApplicationContext(), "" + courrenttime + "의 타임라인 입니다.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), InPolyLine.class);
                    intent.putExtra("ListPath_timelinnum", timelinenum);
                    intent.putExtra("courrenttime", courrenttime);
                    startActivity(intent);
                }
            });
            //데이터 값 사용
        }

    }

    private void viewData() {

        timelineList = new ArrayList<>();
        Cursor data = mDatabaseHelper.getListContents1();


        int numRows = data.getCount();

        if (numRows == 0) {
            Toast.makeText(ListPathActivity.this, "데이터 베이스가 없다.", Toast.LENGTH_SHORT).show();
        } else {

            while (data.moveToNext()) {
                timeline = new TimeLine((data.getString(2)),(data.getString(1))); //1번이 시간 , 2번이 getnum
                timelineList.add(timeline);


            }
            TimelineColumn_ListAdapter adapter = new TimelineColumn_ListAdapter(this, R.layout.path_adapter_view,timelineList);
            listView = (ListView) findViewById(R.id.listView1);
            listView.setAdapter(adapter);
        }
    }
}
//    public void asdf(){
//        final ArrayList<String> items = new ArrayList<>() ;
//        // ArrayAdapter 생성. 아이템 View를 선택(single choice)가능하도록 만듦.
//        Cursor data = mDatabaseHelper.getListContents1();
//
//        final ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, items) ;
//
//        int numRows = data.getCount();
//        if(numRows == 0 ){
//            Toast.makeText(ListPathActivity.this, "데이터 베이스가 없다.",Toast.LENGTH_SHORT).show();
//        }
//        else{
//            while (data.moveToNext()){
//
//                String voca = data.getString(1);
//                //여기서부터 시작
//
//
//            }
//            // listview 생성 및 adapter 지정.
//            listView1 = (ListView) findViewById(R.id.listView1) ;
//            listView1.setAdapter(adapter) ;
//
//        }
//
//    }





