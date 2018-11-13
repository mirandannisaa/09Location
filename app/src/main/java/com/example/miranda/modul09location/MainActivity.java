package com.example.miranda.modul09location;

import android.Manifest;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.AnimatorRes;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class MainActivity extends AppCompatActivity implements GetTaskAddress.onTaskFinish{

    //button
    private Button mLocationButton;
    //textview
    private TextView mLocationTextView;
    //imageview
    private ImageView mAndroidImageView;
    //animasi
    private AnimatorSet mRotateAnim;
    //location
    private Location mLastLocation;
    //deklarasi variabel untuk fusedlocationproviderclient
    private FusedLocationProviderClient mFusedLocationClient;

    private boolean mTrackingLocation;
    //object location callback
    private LocationCallback mLocationCallback;
    //constant untuk mengidentifikasi permission pada onRequestPemission
    private static final int REQUEST_LOCATION_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        //deklarasi variabel button, textview, imageview dengan id yang diambil dari activity main
        mLocationButton = (Button)findViewById(R.id.button_location);
        mLocationTextView = (TextView)findViewById(R.id.textview_location);
        mAndroidImageView = (ImageView)findViewById(R.id.imageview_an);

        //mengatur animasi pada imageview
        mRotateAnim = (AnimatorSet)AnimatorInflater.loadAnimator(this, R.animator.rotate);
        mRotateAnim.setTarget(mAndroidImageView);

        //onclick button yang sudah dideklarasi
        mLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //jika tracking false yang dijalankan fungsi trackinglocation()
                if (!mTrackingLocation){
                    trackingLocation();
                } else {
                    //jika tracking true yang dijalankan fungsi stoptrackinglocation()
                    stopTrackingLocation();
                }
            }
        });

        mLocationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult){
                if (mTrackingLocation){
                    new GetTaskAddress(MainActivity.this, MainActivity.this).execute(locationResult.getLastLocation());
                }
            }
        };
    }

    private void getLocation(){

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);

        }else{

            //Log.d("getpermission", "getLocation: permission granted");
            mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null){
                        //mLastLocation = location;
                        //mLocationTextView.setText(getString(R.string.location_text, mLastLocation.getLatitude(), mLastLocation.getLongitude(), mLastLocation.getTime()));
                        //reserve geocode AsyncTask
                        new GetTaskAddress(MainActivity.this, MainActivity.this).execute(location);

                    } else{
                        //mLocationTextView.setText("Location not Available");
                        mLocationTextView.setText(getString(R.string.address_text, "Searching Address", System.currentTimeMillis()));
                    }
                }
            });
        }
    }

    //start trackinglocation
    private void trackingLocation(){

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION} , REQUEST_LOCATION_PERMISSION );

        } else {
            mFusedLocationClient.requestLocationUpdates(getLocationRequest(), mLocationCallback,null );
            mLocationTextView.setText(getString(R.string.address_text, "Searching Address", System.currentTimeMillis()));
            mTrackingLocation = true;
            mLocationButton.setText("Stop Tracking");
            mRotateAnim.start();
        }
    }

    //stop trackinglocation
    private void stopTrackingLocation(){
        if (mTrackingLocation){
            mTrackingLocation = false;
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
            mLocationButton.setText("Start Tracking Location");
            mLocationTextView.setText("Tracking stop");
            mRotateAnim.end();
        }
    }

    private LocationRequest getLocationRequest(){
        LocationRequest locationRequest = new LocationRequest();
        //lama waktu update lokasi yg diinginkan /millisecond
        //1000ms = 1s
        locationRequest.setInterval(10000);
        //lama waktu update lokasi dari aplikasi yang lain
        locationRequest.setFastestInterval(5000);
        //akurasi tinggi menggunakan GPS
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }

    @Override
    public void onRequestPermissionsResult(int RequesCode, @NonNull String[] permissions, @NonNull int[] grantResult){
        //permission
        switch (RequesCode){
            case REQUEST_LOCATION_PERMISSION :
                if(grantResult.length > 0 && grantResult[0] == PackageManager.PERMISSION_GRANTED){
                    getLocation();
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    public void onTaskCompleted(String result){
        //cek mtrackinglocation
        if(mTrackingLocation) {
            //update UI dengan tampilan hasil alamat
            //jika aktif menampilkan alamat dan waktu
            mLocationTextView.setText(getString(R.string.address_text, result, System.currentTimeMillis()));
        }
    }
}
