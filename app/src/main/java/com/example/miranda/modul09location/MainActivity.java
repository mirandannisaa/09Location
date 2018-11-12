package com.example.miranda.modul09location;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class MainActivity extends AppCompatActivity implements GetTaskAddress.onTaskFinish{

    Button mLocationButton;
    TextView mLocationTextView;
    private Location mLastLocation;
    private FusedLocationProviderClient mFusedLocationClient;

    //constant
    private static final int REQUEST_LOCATION_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mLocationButton = (Button)findViewById(R.id.button_location);
        mLocationTextView = (TextView)findViewById(R.id.textview_location);

        mLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLocation();
            }
        });
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
//                        mLastLocation = location;
//                        mLocationTextView.setText(getString(R.string.location_text, mLastLocation.getLatitude(), mLastLocation.getLongitude(), mLastLocation.getTime()));
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

    @Override
    public void onRequestPermissionsResult(int RequesCode, @NonNull String[] permissions, @NonNull int[] grantResult){
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
        //update UI dengan tampilan hasil alamat
        mLocationTextView.setText(getString(R.string.address_text, result, System.currentTimeMillis()));
    }
}
