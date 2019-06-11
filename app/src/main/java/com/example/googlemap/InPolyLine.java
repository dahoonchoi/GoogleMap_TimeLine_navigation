package com.example.googlemap;


import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;

import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;

import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

public class InPolyLine extends AppCompatActivity implements View.OnClickListener,OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {


    DatabaseHelper mDatabaseHelper;

    private GoogleApiClient mGoogleApiClient = null;
    private GoogleMap mGoogleMap = null;
    private Marker currentMarker = null;


    private LatLng endLatLng = new LatLng(0, 0);
    private LatLng startLatLng1 = new LatLng(0, 0);

    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 2002;
    private static final int UPDATE_INTERVAL_MS = 1000; //업데이트 시간 1초씩
    private static final int FASTEST_UPDATE_INTERVAL_MS = 500; //업데이트 시간 0.5초

    boolean mMoveMapByUser = true;
    boolean mMoveMapByAPI = true;

    //업데이트 요청
    LocationRequest locationRequest = new LocationRequest()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(UPDATE_INTERVAL_MS)//초단위 1초씩
            .setFastestInterval(FASTEST_UPDATE_INTERVAL_MS);



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDatabaseHelper = new DatabaseHelper(this);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.inpolyline_layout);
        Toolbar toolbar1 = (Toolbar) findViewById(R.id.Intoolbar);
        Intent intent = getIntent();
        String currenttime = intent.getStringExtra("courrenttime");
        toolbar1.setTitle("" + currenttime + "의 타임라인");


        FloatingActionButton fabnav = (FloatingActionButton) findViewById(R.id.fabnav);
        fabnav.setOnClickListener(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map1);

//        MapFragment mapFragment = (MapFragment) getFragmentManager()
//                .findFragmentById(R.id.map1);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onClick(View v) {

      show();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        Intent intent = getIntent();
        String data1 = intent.getStringExtra("ListPath_timelinnum");
        Cursor data = mDatabaseHelper.getListContents2(data1);
        int numRows = data.getCount();

        mGoogleMap = googleMap;

        mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
        mGoogleMap.getUiSettings().setCompassEnabled(false);

        if (numRows == 0) {
            Toast.makeText(InPolyLine.this, "DB없다", Toast.LENGTH_SHORT).show();
        } else {
            data.moveToFirst();
            Double lat = Double.parseDouble(data.getString(2));
            Double lng = Double.parseDouble(data.getString(3));

            LatLng startLatLng = new LatLng(lat, lng);

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(startLatLng)
                    .zoom(18)
                    .build();


            Log.d("카메라", "카메라 줌 실행");
            mGoogleMap.animateCamera((CameraUpdateFactory.newCameraPosition(cameraPosition)));


            while (data.moveToNext()) {

                Double lat1 = Double.parseDouble(data.getString(2));
                Double lng1 = Double.parseDouble(data.getString(3));
                endLatLng = new LatLng(lat1, lng1);
                PolylineOptions options = new PolylineOptions().add(startLatLng).add(endLatLng).width(15)
                        .color(Color.BLUE).geodesic(true);
                Log.d("ads", data.getString(2));
                Log.d("asd", data.getString(3));
                mGoogleMap.addPolyline(options);
                startLatLng = new LatLng(lat1, lng1);

            }


            mGoogleMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {


                @Override
                public boolean onMyLocationButtonClick() {
                    Log.d("", "위치에 따른 카메라 이동 활성화");
                    mMoveMapByAPI = true;
                    return true;
                }
            });

            mGoogleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

                @Override
                public void onMapClick(LatLng latLng) {

                    Log.d("", "onMapClick");
                }
            });

        }

        //출발 도착 마커 찍기
        MarkerOptions markerOptions = new MarkerOptions();
        MarkerOptions markerOptions1 = new MarkerOptions();

        data.moveToFirst();
        Double start2Lat = Double.parseDouble(data.getString(2));
        Double start2Log = Double.parseDouble(data.getString(3));
        LatLng startLatLng2 = new LatLng(start2Lat, start2Log);

        BitmapDrawable bitmapstart=(BitmapDrawable)getResources().getDrawable(R.drawable.start);
        Bitmap b=bitmapstart.getBitmap();
        Bitmap startMarker = Bitmap.createScaledBitmap(b, 150, 200, false);
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(startMarker));

        BitmapDrawable bitmapend=(BitmapDrawable)getResources().getDrawable(R.drawable.end);
        Bitmap a=bitmapend.getBitmap();
        Bitmap endMarker = Bitmap.createScaledBitmap(a, 150, 200, false);
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(endMarker));


        markerOptions
                .position(startLatLng2)
                .title("출발")
                .icon(BitmapDescriptorFactory.fromBitmap(startMarker));


        data.moveToLast();
        Double end1Lat = Double.parseDouble(data.getString(2));
        Double end1Lng = Double.parseDouble(data.getString(3));

        LatLng end1LatLng = new LatLng(end1Lat, end1Lng);
        markerOptions1
                .position(end1LatLng)
                .title("도착")
                .icon(BitmapDescriptorFactory.fromBitmap(endMarker));

        mGoogleMap.addMarker(markerOptions);
        mGoogleMap.addMarker(markerOptions1);

    }

    void show(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("길안내");
        builder.setMessage("길안내를 받으시겠습니까? ");
        builder.setPositiveButton("예",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent1 = getIntent();
                        String data1 = intent1.getStringExtra("ListPath_timelinnum");


                        Intent intent = new Intent(getApplicationContext(), navpolyline.class);
                        intent.putExtra("ListPath_timelinnum", data1);
                        startActivity(intent);
                    }
                });

        builder.setNegativeButton("아니요",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        builder.show();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

}
