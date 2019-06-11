package com.example.googlemap;


import android.Manifest;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;

import android.content.pm.PackageManager;
import android.database.Cursor;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;


import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;

import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class navpolyline extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mMagnetometer;
    private float[] mLastAccelerometer = new float[3];
    private float[] mLastMagnetometer = new float[3];
    private boolean mLastAccelerometerSet = false;
    private boolean mLastMagnetometerSet = false;
    private float[] mR = new float[9];
    private float[] mOrientation = new float[3];
    private float mCurrentDegree = 0f;

    //polyline
    private CoordinatorLayout cool;
    private LinearLayout lin;
    //

    private GoogleApiClient mGoogleApiClient = null;
    private GoogleMap mGoogleMap = null;
    private Marker currentMarker = null;

    private static final String TAG = "googlemap_example";
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 2002;
    private static final int UPDATE_INTERVAL_MS = 1000;  // 1초
    private static final int FASTEST_UPDATE_INTERVAL_MS = 500; // 0.5초

    private AppCompatActivity mActivity;
    boolean askPermissionOnceAgain = false;
    boolean mRequestingLocationUpdates = false;
    Location mCurrentLocatiion;



    boolean equals = false;

    boolean mMoveMapByUser = true;
    boolean mMoveMapByAPI = true;
    LatLng currentPosition;

    private LatLng endLatLng = new LatLng(0, 0);
    private LatLng startLatLng = new LatLng(0, 0);
    private LatLng equalsLng = new LatLng(0,0);
    private LatLng startLatLng1 = new LatLng(0, 0);

    ArrayList polylines;

    String ewsn;
    DatabaseHelper mDatabaseHelper;
    private ImageView mPointer;
    //equals 비교
    int intequals = 0;

    int postition = 1;

    LocationRequest locationRequest = new LocationRequest()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(UPDATE_INTERVAL_MS)
            .setFastestInterval(FASTEST_UPDATE_INTERVAL_MS);


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDatabaseHelper = new DatabaseHelper(this);

        cool = (CoordinatorLayout)findViewById(R.id.coor);
        lin = (LinearLayout)findViewById(R.id.linearLayout);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.navpoly_layout);

        mPointer = (ImageView) findViewById(R.id.pointer);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);


        Log.d(TAG, "onCreate");

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map2);

        mapFragment.getMapAsync(this);

        Intent intent = getIntent();
        String data1 = intent.getStringExtra("ListPath_timelinnum");
        Cursor data = mDatabaseHelper.getListContents3(data1);

        yongjin();


    }

    @Override
    public void onResume() {

        super.onResume();

        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_GAME);

        if (mGoogleApiClient.isConnected()) {

            Log.d(TAG, "onResume : call startLocationUpdates");
            if (!mRequestingLocationUpdates) startLocationUpdates();
        }


        //앱 정보에서 퍼미션을 허가했는지를 다시 검사해봐야 한다.
        if (askPermissionOnceAgain) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                askPermissionOnceAgain = false;

                checkPermissions();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this, mAccelerometer);
        mSensorManager.unregisterListener(this, mMagnetometer);
    }

    private void startLocationUpdates() {

        if (!checkLocationServicesStatus()) {

            Log.d(TAG, "startLocationUpdates : call showDialogForLocationServiceSetting");
            showDialogForLocationServiceSetting();
        } else {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                Log.d(TAG, "startLocationUpdates : 퍼미션 안가지고 있음");
                return;
            }


            Log.d(TAG, "startLocationUpdates : call FusedLocationApi.requestLocationUpdates");
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, this);
            mRequestingLocationUpdates = true;

            mGoogleMap.setMyLocationEnabled(true);

        }

    }


    private void stopLocationUpdates() {

        Log.d(TAG, "stopLocationUpdates : LocationServices.FusedLocationApi.removeLocationUpdates");
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        mRequestingLocationUpdates = false;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady :");

        Intent intent = getIntent();
        String data1 = intent.getStringExtra("ListPath_timelinnum");
        Cursor data = mDatabaseHelper.getListContents3(data1);
        mGoogleMap = googleMap;


        //런타임 퍼미션 요청 대화상자나 GPS 활성 요청 대화상자 보이기전에
        //지도의 초기위치를 서울로 이동
        setDefaultLocation();


        //mGoogleMap.getUiSettings().setZoomControlsEnabled(false);
        mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
        mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(17));
        mGoogleMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {

            @Override
            public boolean onMyLocationButtonClick() {

                Log.d(TAG, "onMyLocationButtonClick : 위치에 따른 카메라 이동 활성화");
                mMoveMapByAPI = true;
                return true;
            }
        });
        mGoogleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng latLng) {

                Log.d(TAG, "onMapClick :");
            }
        });

        mGoogleMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {

            @Override
            public void onCameraMoveStarted(int i) {

                if (mMoveMapByUser == true && mRequestingLocationUpdates) {

                    Log.d(TAG, "onCameraMove : 위치에 따른 카메라 이동 비활성화");
                    mMoveMapByAPI = false;
                }

                mMoveMapByUser = true;

            }
        });


        mGoogleMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {

            @Override
            public void onCameraMove() {


            }
        });


        if (data.getCount() == 0) {
            Toast.makeText(navpolyline.this, "DB없다", Toast.LENGTH_SHORT).show();
        } else{
            data.moveToFirst();
            Double lat2 = Double.parseDouble(data.getString(2));
            Double lng2 = Double.parseDouble(data.getString(3));

            LatLng startLatLng1 = new LatLng(lat2, lng2);

            while (data.moveToNext()){

                Double lat1 = Double.parseDouble(data.getString(2));
                Double lng1 = Double.parseDouble(data.getString(3));
                endLatLng = new LatLng(lat1, lng1);
                PolylineOptions options = new PolylineOptions().add(startLatLng1).add(endLatLng).width(15)
                        .color(Color.BLUE).geodesic(true);
                mGoogleMap.addPolyline(options);
                startLatLng1 = new LatLng(lat1, lng1);
            }

        }


//        while (data.moveToNext()) {
//
//            Double lat1 = Double.parseDouble(data.getString(2));
//            Double lng1 = Double.parseDouble(data.getString(3));
//            endLatLng = new LatLng(lat1, lng1);
//            PolylineOptions options = new PolylineOptions().add(startLatLng).add(endLatLng).width(15)
//                    .color(Color.BLUE).geodesic(true);
//            Log.d("ads", data.getString(2));
//            Log.d("asd", data.getString(3));
//            mGoogleMap.addPolyline(options);
//            startLatLng = new LatLng(lat1, lng1);
//
//        }


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
//        Intent intent = getIntent();
//        String data1 = intent.getStringExtra("ListPath_timelinnum");
//        Cursor data = mDatabaseHelper.getListContents3(data1);
//
//        data.moveToFirst();
//
////        Double lat2 = Double.parseDouble(data.getString(2));
////        Double lng2 = Double.parseDouble(data.getString(3));
//
//
////        new LatLng( mCurrentLocatiion.getLatitude(), mCurrentLocatiion.getLongitude());
//
//        startLatLng = new LatLng(mCurrentLocatiion.getLatitude(), mCurrentLocatiion.getLongitude());
//        while (data.moveToNext()){
//
//            Double lat1 = Double.parseDouble(data.getString(2));
//            Double lng1 = Double.parseDouble(data.getString(3));
//            endLatLng = new LatLng(lat1, lng1);
//            PolylineOptions options = new PolylineOptions().add(startLatLng).add(endLatLng).width(15)
//                    .color(Color.BLUE).geodesic(true);
//            Log.d("ads", data.getString(2));
//            Log.d("asd", data.getString(3));
//            mGoogleMap.addPolyline(options);
//            startLatLng = new LatLng(lat1, lng1);
//        }
    }

    @Override
    public void onLocationChanged(Location location) {


        currentPosition
                = new LatLng(location.getLatitude(), location.getLongitude());


        String markerTitle = getCurrentAddress(currentPosition);
        String markerSnippet = "위도:" + String.valueOf(location.getLatitude())
                + " 경도:" + String.valueOf(location.getLongitude());


        //현재 위치에 마커 생성하고 이동
        setCurrentLocation(location, markerTitle, markerSnippet);
//        mylat = String.valueOf(location.getLatitude());
//        mylng = String.valueOf(location.getLongitude());

        mCurrentLocatiion = location;



                equalsmylocation();

//
//        if (equalsmylocation() == true) {
//            equals = true;
//        } else if (equalsmylocation() == false) {
//            equals = false;
//            Log.d("equalsmylocation1", "false");
//        }

        Log.d("polystate", String.valueOf(postition));
//////
        if (currentPosition != null) {


                Intent intent = getIntent();
                String data1 = intent.getStringExtra("ListPath_timelinnum");
                Cursor data = mDatabaseHelper.getListContents3(data1);

//                startLatLng = new LatLng(mCurrentLocatiion.getLatitude(), mCurrentLocatiion.getLongitude());
//                while (data.moveToNext()) {
//                    polylines = new ArrayList<>();
//                    Double lat1 = Double.parseDouble(data.getString(2));
//                    Double lng1 = Double.parseDouble(data.getString(3));
//                    endLatLng = new LatLng(lat1, lng1);
//                    PolylineOptions options = new PolylineOptions().add(startLatLng).add(endLatLng).width(15)
//                            .color(Color.BLUE).geodesic(true);
//                    Log.d("ads", data.getString(2));
//                    Log.d("asd", data.getString(3));
////                mGoogleMap.addPolyline(options);
//
//                    polylines.add(mGoogleMap.addPolyline(options));
//                    startLatLng = new LatLng(lat1, lng1);
//                }



//            Intent intent = getIntent();
//            String data1 = intent.getStringExtra("ListPath_timelinnum");
//            Cursor data = mDatabaseHelper.getListContents3(data1);
//
//            startLatLng = new LatLng(mCurrentLocatiion.getLatitude(), mCurrentLocatiion.getLongitude());
//            while (data.moveToNext()) {
//                polylines = new ArrayList<>();
//                Double lat1 = Double.parseDouble(data.getString(2));
//                Double lng1 = Double.parseDouble(data.getString(3));
//                endLatLng = new LatLng(lat1, lng1);
//                PolylineOptions options = new PolylineOptions().add(startLatLng).add(endLatLng).width(15)
//                        .color(Color.BLUE).geodesic(true);
//                Log.d("ads", data.getString(2));
//                Log.d("asd", data.getString(3));
////                mGoogleMap.addPolyline(options);
//
//                polylines.add(mGoogleMap.addPolyline(options));
//                startLatLng = new LatLng(lat1, lng1);
//            }


        }
        ////
//
    }

    @Override
    protected void onStart() {

        if (mGoogleApiClient != null && mGoogleApiClient.isConnected() == false) {

            Log.d(TAG, "onStart: mGoogleApiClient connect");
            mGoogleApiClient.connect();
        }

        super.onStart();
    }

    @Override
    protected void onStop() {

        if (mRequestingLocationUpdates) {

            Log.d(TAG, "onStop : call stopLocationUpdates");
            stopLocationUpdates();
        }

        if (mGoogleApiClient.isConnected()) {

            Log.d(TAG, "onStop : mGoogleApiClient disconnect");
            mGoogleApiClient.disconnect();
        }

        super.onStop();
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (mRequestingLocationUpdates == false) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);

                if (hasFineLocationPermission == PackageManager.PERMISSION_DENIED) {

                    ActivityCompat.requestPermissions(mActivity,
                            new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

                } else {

                    Log.d(TAG, "onConnected : 퍼미션 가지고 있음");
                    Log.d(TAG, "onConnected : call startLocationUpdates");
                    startLocationUpdates();
                    mGoogleMap.setMyLocationEnabled(true);
                }

            } else {

                Log.d(TAG, "onConnected : call startLocationUpdates");
                startLocationUpdates();
                mGoogleMap.setMyLocationEnabled(true);
            }
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed");
        setDefaultLocation();
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.d(TAG, "onConnectionSuspended");
        if (cause == CAUSE_NETWORK_LOST)
            Log.e(TAG, "onConnectionSuspended(): Google Play services " +
                    "connection lost.  Cause: network lost.");
        else if (cause == CAUSE_SERVICE_DISCONNECTED)
            Log.e(TAG, "onConnectionSuspended():  Google Play services " +
                    "connection lost.  Cause: service disconnected");
    }

    public String getCurrentAddress(LatLng latlng) {

        //지오코더... GPS를 주소로 변환
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        List<Address> addresses;

        try {

            addresses = geocoder.getFromLocation(
                    latlng.latitude,
                    latlng.longitude,
                    1);
        } catch (IOException ioException) {
            //네트워크 문제
            Toast.makeText(this, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";

        }


        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show();
            return "주소 미발견";

        } else {
            Address address = addresses.get(0);
            return address.getAddressLine(0).toString();
        }

    }

    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    public void setCurrentLocation(Location location, String markerTitle, String markerSnippet) {

        mMoveMapByUser = false;


        if (currentMarker != null) currentMarker.remove();


        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
//        mylat = String.valueOf(location.getLatitude());
//        mylng = String.valueOf(location.getLongitude());

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(currentLatLng);
        markerOptions.title(markerTitle);
        markerOptions.snippet(markerSnippet);
        markerOptions.draggable(true);


        currentMarker = mGoogleMap.addMarker(markerOptions);


        if (mMoveMapByAPI) {

            Log.d(TAG, "setCurrentLocation :  mGoogleMap moveCamera "
                    + location.getLatitude() + " " + location.getLongitude());
            // CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(currentLatLng, 15);
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(currentLatLng);
            mGoogleMap.moveCamera(cameraUpdate);
        }
    }

    public void setDefaultLocation() {

        mMoveMapByUser = false;


        //디폴트 위치, Seoul
        LatLng DEFAULT_LOCATION = new LatLng(37.56, 126.97);
        String markerTitle = "위치정보 가져올 수 없음";
        String markerSnippet = "위치 퍼미션과 GPS 활성 요부 확인하세요";


        if (currentMarker != null) currentMarker.remove();

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(DEFAULT_LOCATION);
        markerOptions.title(markerTitle);
        markerOptions.snippet(markerSnippet);
        markerOptions.draggable(true);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        currentMarker = mGoogleMap.addMarker(markerOptions);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 17);
        mGoogleMap.moveCamera(cameraUpdate);

    }

    @TargetApi(Build.VERSION_CODES.M)
    private void checkPermissions() {
        boolean fineLocationRationale = ActivityCompat
                .shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);

        if (hasFineLocationPermission == PackageManager
                .PERMISSION_DENIED && fineLocationRationale)
            showDialogForPermission("앱을 실행하려면 퍼미션을 허가하셔야합니다.");

        else if (hasFineLocationPermission
                == PackageManager.PERMISSION_DENIED && !fineLocationRationale) {
            showDialogForPermissionSetting("퍼미션 거부 + Don't ask again(다시 묻지 않음) " +
                    "체크 박스를 설정한 경우로 설정에서 퍼미션 허가해야합니다.");
        } else if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED) {


            Log.d(TAG, "checkPermissions : 퍼미션 가지고 있음");

            if (mGoogleApiClient.isConnected() == false) {

                Log.d(TAG, "checkPermissions : 퍼미션 가지고 있음");
                mGoogleApiClient.connect();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int permsRequestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (permsRequestCode
                == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION && grantResults.length > 0) {

            boolean permissionAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;

            if (permissionAccepted) {


                if (mGoogleApiClient.isConnected() == false) {

                    Log.d(TAG, "onRequestPermissionsResult : mGoogleApiClient connect");
                    mGoogleApiClient.connect();
                }


            } else {

                checkPermissions();
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void showDialogForPermission(String msg) {

        AlertDialog.Builder builder = new AlertDialog.Builder(navpolyline.this);
        builder.setTitle("알림");
        builder.setMessage(msg);
        builder.setCancelable(false);
        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                ActivityCompat.requestPermissions(mActivity,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }
        });

        builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        });
        builder.create().show();
    }

    private void showDialogForPermissionSetting(String msg) {

        AlertDialog.Builder builder = new AlertDialog.Builder(navpolyline.this);
        builder.setTitle("알림");
        builder.setMessage(msg);
        builder.setCancelable(true);
        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                askPermissionOnceAgain = true;

                Intent myAppSettings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:" + mActivity.getPackageName()));
                myAppSettings.addCategory(Intent.CATEGORY_DEFAULT);
                myAppSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mActivity.startActivity(myAppSettings);
            }
        });
        builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        });
        builder.create().show();
    }

    private void showDialogForLocationServiceSetting() {

        AlertDialog.Builder builder = new AlertDialog.Builder(navpolyline.this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
                + "위치 설정을 수정하실래요?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case GPS_ENABLE_REQUEST_CODE:

                //사용자가 GPS 활성 시켰는지 검사
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {

                        Log.d(TAG, "onActivityResult : 퍼미션 가지고 있음");


                        if (mGoogleApiClient.isConnected() == false) {

                            Log.d(TAG, "onActivityResult : mGoogleApiClient connect ");
                            mGoogleApiClient.connect();
                        }
                        return;
                    }
                }

                break;
        }
    }


    public void yongjin() {
//        ImageView imageView = (ImageView) findViewById(R.id.imageView);


        Intent intent = getIntent();
        String data1 = intent.getStringExtra("ListPath_timelinnum");
        Cursor data = mDatabaseHelper.getListContents3(data1);

        int numRows = data.getCount();

        Log.d("yongjinequals", String.valueOf(equals));
        Log.d("intequls", String.valueOf(intequals));


//
//        ewsn = data.getString(1);
//        String navlat = data.getString(2);
//        String navlng = data.getString(3);
//
//        double lat = Double.parseDouble(navlat);
//        double lng = Double.parseDouble(navlng);
//        double melat = Double.parseDouble(mylat);
//        double melng = Double.parseDouble(mylng);


//        double cutnavlat = Double.parseDouble(String.format("%.4f", lat));
//        double cutnavlng = Double.parseDouble(String.format("%.5f", lng));
//        double cutmynavlat = Double.parseDouble(String.format("%.4f",melat));
//        double cutmynavlng = Double.parseDouble(String.format("%.5f",melng));
//
//        String scutnavlat = Double.toString(cutnavlat);
//        String scutnavlng = Double.toString(cutnavlng);
//        String scutmynavlat = Double.toString(cutmynavlat);
//        String scutmynavlng = Double.toString(cutmynavlng);
////
//        Log.d("lat", scutnavlat); //35.1456  4개
//        Log.d("lng", scutnavlng); //129.00753 5개
//
//        Log.d("mylat",scutmynavlat); //
//        Log.d("mylng",scutmynavlng); //


//        if (ewsn.equals("E")) {
//            imageView.setImageResource(R.drawable.e);
//        } else if (ewsn.equals("N")) {
//            imageView.setImageResource(R.drawable.n);
//        } else if (ewsn.equals("NE")) {
//            imageView.setImageResource(R.drawable.ne);
//        } else if (ewsn.equals("NW")) {
//            imageView.setImageResource(R.drawable.nw);
//        } else if (ewsn.equals("S")) {
//            imageView.setImageResource(R.drawable.s);
//        } else if (ewsn.equals("SE")) {
//            imageView.setImageResource(R.drawable.se);
//        } else if (ewsn.equals("SW")) {
//            imageView.setImageResource(R.drawable.sw);
//        } else if (ewsn.equals("W")) {
//            imageView.setImageResource(R.drawable.w);
//        }
    }

//    public void drawpath() {
//
//        if (currentPosition != null) {
//            Intent intent = getIntent();
//            String data1 = intent.getStringExtra("ListPath_timelinnum");
//            Cursor data = mDatabaseHelper.getListContents3(data1);
//
//
//
//            data.moveToFirst();
//        Double lat2 = Double.parseDouble(data.getString(2));
//        Double lng2 = Double.parseDouble(data.getString(3));
//
////        new LatLng( mCurrentLocatiion.getLatitude(), mCurrentLocatiion.getLongitude());
//
//                startLatLng = new LatLng(lat2,lng2);
//                while (data.moveToNext()) {
//                    polylines = new ArrayList<>();
//                    Double lat1 = Double.parseDouble(data.getString(2));
//                    Double lng1 = Double.parseDouble(data.getString(3));
//                    endLatLng = new LatLng(lat1, lng1);
//                    PolylineOptions options = new PolylineOptions().add(startLatLng).add(endLatLng).width(15)
//                            .color(Color.BLUE).geodesic(true);
//                    Log.d("ads", data.getString(2));
//                    Log.d("asd", data.getString(3));
////                mGoogleMap.addPolyline(options);
//
//                    polylines.add(mGoogleMap.addPolyline(options));
//                    startLatLng = new LatLng(lat1, lng1);
//                    Log.d("폴리라인","실행");
//                }
//            }
//
//
//
//
//    }

    public void equalsmylocation() {

        lin = (LinearLayout)findViewById(R.id.linearLayout);
        Intent intent = getIntent();
        String data1 = intent.getStringExtra("ListPath_timelinnum");
        Cursor data = mDatabaseHelper.getListContents3(data1);
        Handler handler = new Handler();


        if (postition >= data.getCount()-1) {
            //
            Toast.makeText(navpolyline.this, "도착했습니다.", Toast.LENGTH_SHORT).show();
            show();
        } else {
            if (data.getCount() == 0) {
                Toast.makeText(navpolyline.this, "DB없다", Toast.LENGTH_SHORT).show();

            } else {

                data.moveToPosition(postition);
//            while (data.moveToNext()) {
//            Log.d("datagetPositionfirst", String.valueOf(data.getPosition(postition)));
                Log.d("getcount", String.valueOf(data.getCount()));
                String ID = data.getString(0);
                Double lat2 = Double.parseDouble(data.getString(2));
                Double lng2 = Double.parseDouble(data.getString(3));
                Double mylat = mCurrentLocatiion.getLatitude();
                Double mylng = mCurrentLocatiion.getLongitude();

                double cutnavlat = Double.parseDouble(String.format("%.4f", lat2));
                double cutnavlng = Double.parseDouble(String.format("%.4f", lng2));
                double cutmynavlat = Double.parseDouble(String.format("%.4f", mylat));
                double cutmynavlng = Double.parseDouble(String.format("%.4f", mylng));



                String scutnavlat = Double.toString(cutnavlat);
                String scutnavlng = Double.toString(cutnavlng);
                String scutmynavlat = Double.toString(cutmynavlat);
                String scutmynavlng = Double.toString(cutmynavlng);

                Log.d("lat", scutnavlat); //35.1456  4개
                Log.d("lng1", scutnavlng); //129.00753 5개
                Log.d("mylat", scutmynavlat); //
                Log.d("mylng", scutmynavlng); //



//
                //위치가 동일 할 때
                if (cutnavlat == cutmynavlat ) {
                    manageBlinkEffect();

//                    while (data.getPosition() < data.getCount() - 1) {

                    data.moveToPosition(postition);
                    data.moveToNext();
                    equals = true;


                    Log.d("datagetPositiontrue", String.valueOf(data.getPosition()));
                    Log.d("getcount", String.valueOf(data.getCount()));
                    postition = data.getPosition();
//                }
                    Toast.makeText(getApplicationContext(), "현재값같음 : " + scutnavlat + "  " + scutmynavlat + "  " + scutnavlng + "  " + scutmynavlng + " " + postition + " getcount :" + data.getCount() + " ID " + ID, Toast.LENGTH_SHORT).show();

                }

                //위치가 다들 때
                 if (cutnavlat != cutmynavlat ) {
                     manageBlinkEffectred();
                     data.moveToPosition(postition);
                     equals = false;
                     Log.d("datagetPositionfalse", String.valueOf(data.getPosition()));
                     Log.d("getcount", String.valueOf(data.getCount()));
                     postition = data.getPosition() ;
                     Toast.makeText(getApplicationContext(), "현재값다름 : " + scutnavlat + "  " + scutmynavlat + "  " + scutnavlng + "  " + scutmynavlng + "   " + postition + "  getcount :" + data.getCount() + " ID " + ID, Toast.LENGTH_SHORT).show();
                 }

//                //위치가 동일 할 때
//                if (cutnavlat == cutmynavlat && cutnavlng == cutmynavlng) {
//
//                    while (data.getPosition() < data.getCount() - 1) {
//
//                        data.moveToNext();
//                        equals = true;
//
//                        Log.d("datagetPositiontrue", String.valueOf(data.getPosition()));
//                        Log.d("getcount", String.valueOf(data.getCount()));
//                        postition = data.getPosition();
//
//                    }
//                    Toast.makeText(getApplicationContext(), "현재값같음 : " + scutnavlat + "  " + scutmynavlat + "  " + scutnavlng + "  " + scutmynavlng + " " + postition + " getcount :" + data.getCount() + " ID " + ID, Toast.LENGTH_SHORT).show();
//
//                }
//
//                //위치가 다들 때
//                else if (cutnavlat != cutmynavlat && cutnavlng != cutmynavlng) {
//                    data.moveToPosition(postition);
//                    equals = false;
//                    Log.d("datagetPositionfalse", String.valueOf(data.getPosition()));
//                    Log.d("getcount", String.valueOf(data.getCount()));
//                    postition = data.getPosition() ;
//                    Toast.makeText(getApplicationContext(), "현재값다름 : " + scutnavlat + "  " + scutmynavlat + "  " + scutnavlng + "  " + scutmynavlng + "   " + postition + "  getcount :" + data.getCount() + " ID " + ID, Toast.LENGTH_SHORT).show();
//                }
//            }
            }
        }


    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        Log.d("생명주기", "onSensorChanged");
        Intent intent = getIntent();
        String data1 = intent.getStringExtra("ListPath_timelinnum");
        Cursor data = mDatabaseHelper.getListContents3(data1);

        Log.d("Sensorequals", String.valueOf(equals));
        Log.d("센서포지션", String.valueOf(postition));
        Log.d("센서getCount", String.valueOf(data.getCount()));


        data.moveToPosition(postition);
        ewsn = data.getString(1);


//        else{
//            if (data.getCount() == 0) {
//                Toast.makeText(navpolyline.this, "DB없다", Toast.LENGTH_SHORT).show();
//                Toast.makeText(navpolyline.this, "경로없음"+postition, Toast.LENGTH_SHORT).show();
//            }
//            else{
//                data.moveToFirst();
//                if (equals == true) {
//            while (data.getPosition() < data.getCount()) {
//                /// 여기서 오류
//                data.moveToNext();
//                ewsn = data.getString(1);
//                Log.d("true값", ewsn);
//                Log.d("count값", data.getString(1));
//                postition = data.getPosition();
//            }
//
//        } else if (equals == false) {
//            ewsn = data.getString(1);
//                Log.d("false값", ewsn);
//                Log.d("count값", String.valueOf(data.getCount()));
//            postition = data.getPosition();
//        }
//            }
//
//        }
//        data.moveToFirst();
//
//        if (equals == true) {
//            while (data.getPosition() < data.getCount()) {
//                data.moveToNext();
//                ewsn = data.getString(1);
//                Log.d("true값", ewsn);
//                Log.d("count값", data.getString(1));
//                postition = data.getPosition();
//            }
//
//        } else if (equals == false) {
//            ewsn = data.getString(1);
//                Log.d("false값", ewsn);
//                Log.d("count값", String.valueOf(data.getCount()));
//            postition = data.getPosition();
//        }


//        if(data.moveToFirst()){
//
//            if(equals == true){
//                data.moveToNext();
//                ewsn = data.getString(1);
//                Log.d("true값", ewsn);
//                Log.d("count값", data.getString(1));
//            }
//
//            else if(equals == false){
//
//                ewsn = data.getString(1);
//                Log.d("false값", ewsn);
//                Log.d("count값", String.valueOf(data.getCount()));
//            }
//        }


        if (event.sensor == mAccelerometer) {
            System.arraycopy(event.values, 0, mLastAccelerometer, 0, event.values.length);
            mLastAccelerometerSet = true;
        } else if (event.sensor == mMagnetometer) {
            System.arraycopy(event.values, 0, mLastMagnetometer, 0, event.values.length);
            mLastMagnetometerSet = true;
        }
        if (mLastAccelerometerSet && mLastMagnetometerSet) {

            SensorManager.getRotationMatrix(mR, null, mLastAccelerometer, mLastMagnetometer);
            double asd = 100.011962825;


            String s_asd = ewsn;
            Log.d("s_asd", s_asd);
            float f_asd = Float.parseFloat(s_asd);

            float azimuthinDegress = (int) ((360 - (((Math.toDegrees(SensorManager.getOrientation(mR, mOrientation)[0]) + 360) % 360))) + f_asd);
            float mylocation = (int) ((Math.toDegrees(SensorManager.getOrientation(mR, mOrientation)[0]) + 360) % 360);
            float noazimuthinDegress = (int) (azimuthinDegress + 180.0);

            if (azimuthinDegress >= 360) {
                azimuthinDegress = azimuthinDegress - 360;
            }


            if (noazimuthinDegress >= 180.0) {
                noazimuthinDegress = noazimuthinDegress - 360;
            }

            Log.d("내위치", "" + mylocation);
            Log.d("f_asd", "" + azimuthinDegress);
            Log.d("noazimuthinDegress", "" + noazimuthinDegress);


            ImageView imageViewse = (ImageView)findViewById(R.id.se);
            ImageView imageViews = (ImageView)findViewById(R.id.s);
            ImageView imageViewsw = (ImageView)findViewById(R.id.sw);
            ImageView imageVieww = (ImageView)findViewById(R.id.w);
            ImageView imageViewnw = (ImageView)findViewById(R.id.nw);
            ImageView imageViewn = (ImageView)findViewById(R.id.n);
            ImageView imageViewne = (ImageView)findViewById(R.id.ne);
            ImageView imageViewe = (ImageView)findViewById(R.id.e);


            if (noazimuthinDegress >= -22.5 && noazimuthinDegress < 22.5) {
                //s
                imageViews.setVisibility(View.VISIBLE);
                imageViewse.setVisibility(View.INVISIBLE);
                imageViewsw.setVisibility(View.INVISIBLE);
                imageVieww.setVisibility(View.INVISIBLE);
                imageViewnw.setVisibility(View.INVISIBLE);
                imageViewn.setVisibility(View.INVISIBLE);
                imageViewne.setVisibility(View.INVISIBLE);
                imageViewe.setVisibility(View.INVISIBLE);
            }
            if (noazimuthinDegress >= 22.5 && noazimuthinDegress < 67.5) {
                //sw
                imageViews.setVisibility(View.INVISIBLE);
                imageViewse.setVisibility(View.INVISIBLE);
                imageViewsw.setVisibility(View.VISIBLE);
                imageVieww.setVisibility(View.INVISIBLE);
                imageViewnw.setVisibility(View.INVISIBLE);
                imageViewn.setVisibility(View.INVISIBLE);
                imageViewne.setVisibility(View.INVISIBLE);
                imageViewe.setVisibility(View.INVISIBLE);

            }
            if (noazimuthinDegress >= 67.5 && noazimuthinDegress < 112.5) {
                //w
                imageViews.setVisibility(View.INVISIBLE);
                imageViewse.setVisibility(View.INVISIBLE);
                imageViewsw.setVisibility(View.INVISIBLE);
                imageVieww.setVisibility(View.VISIBLE);
                imageViewnw.setVisibility(View.INVISIBLE);
                imageViewn.setVisibility(View.INVISIBLE);
                imageViewne.setVisibility(View.INVISIBLE);
                imageViewe.setVisibility(View.INVISIBLE);

            }
            if (noazimuthinDegress >= 112.5 && noazimuthinDegress < 157.5) {
                //nw
                imageViews.setVisibility(View.INVISIBLE);
                imageViewse.setVisibility(View.INVISIBLE);
                imageViewsw.setVisibility(View.INVISIBLE);
                imageVieww.setVisibility(View.INVISIBLE);
                imageViewnw.setVisibility(View.VISIBLE);
                imageViewn.setVisibility(View.INVISIBLE);
                imageViewne.setVisibility(View.INVISIBLE);
                imageViewe.setVisibility(View.INVISIBLE);
            }
            if (noazimuthinDegress >= 157.5 || noazimuthinDegress < -157.5) {
                //n
                imageViews.setVisibility(View.INVISIBLE);
                imageViewse.setVisibility(View.INVISIBLE);
                imageViewsw.setVisibility(View.INVISIBLE);
                imageVieww.setVisibility(View.INVISIBLE);
                imageViewnw.setVisibility(View.INVISIBLE);
                imageViewn.setVisibility(View.VISIBLE);
                imageViewne.setVisibility(View.INVISIBLE);
                imageViewe.setVisibility(View.INVISIBLE);
            }
            if (noazimuthinDegress >= -157.5 && noazimuthinDegress < -112.5) {
                //ne
                imageViews.setVisibility(View.INVISIBLE);
                imageViewse.setVisibility(View.INVISIBLE);
                imageViewsw.setVisibility(View.INVISIBLE);
                imageVieww.setVisibility(View.INVISIBLE);
                imageViewnw.setVisibility(View.INVISIBLE);
                imageViewn.setVisibility(View.INVISIBLE);
                imageViewne.setVisibility(View.VISIBLE);
                imageViewe.setVisibility(View.INVISIBLE);
            }
            if (noazimuthinDegress >= -112.5 && noazimuthinDegress < -67.5) {
                //e
                imageViews.setVisibility(View.INVISIBLE);
                imageViewse.setVisibility(View.INVISIBLE);
                imageViewsw.setVisibility(View.INVISIBLE);
                imageVieww.setVisibility(View.INVISIBLE);
                imageViewnw.setVisibility(View.INVISIBLE);
                imageViewn.setVisibility(View.INVISIBLE);
                imageViewne.setVisibility(View.INVISIBLE);
                imageViewe.setVisibility(View.VISIBLE);
            }

            if (noazimuthinDegress >= -67.5 && noazimuthinDegress < -22.5) {
               //se
                imageViews.setVisibility(View.INVISIBLE);
                imageViewse.setVisibility(View.VISIBLE);
                imageViewsw.setVisibility(View.INVISIBLE);
                imageVieww.setVisibility(View.INVISIBLE);
                imageViewnw.setVisibility(View.INVISIBLE);
                imageViewn.setVisibility(View.INVISIBLE);
                imageViewne.setVisibility(View.INVISIBLE);
                imageViewe.setVisibility(View.INVISIBLE);
            }


            RotateAnimation ra = new RotateAnimation(
                    mCurrentDegree,
                    -noazimuthinDegress,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f
            );
            ra.setDuration(250);
            ra.setFillAfter(true);
            mPointer.startAnimation(ra);

            mCurrentDegree = noazimuthinDegress;

        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    void show(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("도착");
        builder.setMessage("목적지에 도착했습니다. 목록으로 돌아가겠습니다.");
        builder.setPositiveButton("예",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                       onBackPressed();
                    }
                });

        builder.show();
    }


    @SuppressLint("WrongConstant")
    private void manageBlinkEffect() {
        ObjectAnimator anim = ObjectAnimator.ofInt(lin, "backgroundColor", Color.WHITE, Color.rgb(135,206,250),
                Color.WHITE);
        anim.setDuration(1500);
        anim.setEvaluator(new ArgbEvaluator());
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(Animation.INFINITE);
        anim.start();
    }

    @SuppressLint("WrongConstant")
    private void manageBlinkEffectred() {
        ObjectAnimator anim = ObjectAnimator.ofInt(lin, "backgroundColor", Color.WHITE, Color.rgb(250,128,114),
                Color.WHITE);
        anim.setDuration(1500);
        anim.setEvaluator(new ArgbEvaluator());
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(Animation.INFINITE);
        anim.start();
    }
}
