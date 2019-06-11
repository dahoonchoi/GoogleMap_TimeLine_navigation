package com.example.googlemap;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class ListDataActivity extends AppCompatActivity {

    private ListView listView;
    DatabaseHelper mDatabaseHelper;
    ArrayList<Path> pathList;
    Path path;

    @Override
    protected void onCreate(@Nullable Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setContentView(R.layout.list_layout);

        mDatabaseHelper = new DatabaseHelper(this);

        populateListView();
    }

    private void populateListView() {

        pathList = new ArrayList<>();
        Intent intent = getIntent();
        String data1 = intent.getStringExtra("ListPath_timelinnum");
        Cursor data = mDatabaseHelper.getListContents2(data1);

        int numRows = data.getCount();


        if(numRows == 0 ){
            Toast.makeText(ListDataActivity.this, "데이터 베이스가 없다.",Toast.LENGTH_SHORT).show();
        }
        else{

            while (data.moveToNext()){
                path = new Path(data.getString(1),data.getString(2), data.getString(3), data.getString(4));
                pathList.add(path);
                Log.d("Path", data.getString(1));
            }
            ThreeColumn_ListAdapter adapter = new ThreeColumn_ListAdapter(this, R.layout.list_adapter_view,pathList);
            listView = (ListView) findViewById(R.id.listView);
            listView.setAdapter(adapter);
        }

//        Cursor data = mDatabaseHelper.getData();
//        ArrayList<String> listData = new ArrayList<>();
//        while (data.moveToNext()){
//            listData.add(data.getString(1),data.getString(2));
//        }
//        ListAdapter adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listData);
//        mListView.setAdapter(adapter);
    }

    private void toastMessage(String message){
        Toast.makeText(this,message, Toast.LENGTH_SHORT).show();
    }

}
