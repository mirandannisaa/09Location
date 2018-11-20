package com.example.miranda.modul09location;

import android.Manifest;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.AnimatorRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

public class MainActivity extends AppCompatActivity implements GetTaskAddress.onTaskFinish{

    //button
    private Button mLocationButton;
    private Button mPickLocationButton;
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
    private static final int REQUEST_PICK_PLACE = 0;
    //places class
    private PlaceDetectionClient mPlaceDetectionClient;
    private String mLastPlaceName;

    //variabel untuk savedinstance
    private static String name, address;
    private static  int gambar = -1;

    //savedinstance agar tampilan landscape dan portrait tetap
    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString("name",name);
        savedInstanceState.putString("address",address);
        savedInstanceState.putInt("gambar",gambar);
    }

    //restore data dari instancestate pada object yg diinginkan
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if(savedInstanceState.getString("name")=="") {
            mLocationTextView.setText("Tekan Button dibawah ini untuk mendapatkan lokasi anda");
        } else{

            mLocationTextView.setText(getString(R.string.address_text,savedInstanceState.getString("name"),savedInstanceState.getString("address"), System.currentTimeMillis()));
            mAndroidImageView.setImageResource(savedInstanceState.getInt("gambar"));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //inisiasi object placedetectionclient untuk mendapat informasi lokasi device
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        //deklarasi variabel button, textview, imageview dengan id yang diambil dari activity main
        mLocationButton = (Button)findViewById(R.id.button_location);
        mLocationTextView = (TextView)findViewById(R.id.textview_location);
        mAndroidImageView = (ImageView)findViewById(R.id.imageview_an);
        mPickLocationButton = (Button) findViewById(R.id.button_pilihlocation);
        //mengatur animasi pada imageview
        mRotateAnim = (AnimatorSet)AnimatorInflater.loadAnimator(this, R.animator.rotate);
        mRotateAnim.setTarget(mAndroidImageView);

        mPickLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //eksekusi placepicker
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                try {
                    startActivityForResult(builder.build(MainActivity.this), REQUEST_PICK_PLACE);
                } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });


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

            Log.d("getpermission", "getLocation: permission granted");
            mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null){
                        mLastLocation = location;
                        mLocationTextView.setText(getString(R.string.location_text, mLastLocation.getLatitude(), mLastLocation.getLongitude(), mLastLocation.getTime()));
                        //reserve geocode AsyncTask
                        //new GetTaskAddress(MainActivity.this, MainActivity.this).execute(location);

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
            //argumen settext nama tempat, alamat, waktu
            mLocationTextView.setText(getString(R.string.address_text, "Searching Places", "Searching Address", System.currentTimeMillis()));
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
    //tambahkan throws
    public void onTaskCompleted(final String result) throws SecurityException{
        //cek mtrackinglocation
        if(mTrackingLocation) {
            Task<PlaceLikelihoodBufferResponse> placeResult = mPlaceDetectionClient.getCurrentPlace(null);
            //menambahkan throw securityexception karena akan muncul apabila permission lokasi tidak didapatkan
            placeResult.addOnCompleteListener(new OnCompleteListener<PlaceLikelihoodBufferResponse>() {
                @Override
                public void onComplete(@NonNull Task<PlaceLikelihoodBufferResponse> task) {
                    if (task.isSuccessful()){
                        //jika successful hasil akan tampil pada getresult dan dimasukkan dalam placelikehoodbufferresponse
                        PlaceLikelihoodBufferResponse likelyPlaces = task.getResult();
                        float maxLikehood = 0;
                        Place currentPlace = null;

                        //cek apakah tempat yg dihasilakn paling mendekati (likelihood)
                        for (PlaceLikelihood placeLikelihood : likelyPlaces){
                            if (maxLikehood < placeLikelihood.getLikelihood()){
                                //jika ya maka update object maxlikelihood dengan currentplace
                                maxLikehood = placeLikelihood.getLikelihood();
                                currentPlace = placeLikelihood.getPlace();
                            }

                            //Tampil di UI
                            if (currentPlace!= null){
                                mLocationTextView.setText(getString(R.string.address_text, currentPlace.getName(), result, System.currentTimeMillis()));
                                setTipeLokasi(currentPlace);
                                //input data pada variabel di saveinstance
                                name = placeLikelihood.getPlace().getName().toString();
                                address = placeLikelihood.getPlace().getAddress().toString();
                                gambar = setTipeLokasi(currentPlace);
                                mAndroidImageView.setImageResource(gambar);
                            }
                        }
                        //hapus buffer
                        likelyPlaces.release();
                    //jika nama tempat tidak ditemukan
                    }else{
                        mLocationTextView.setText(getString(R.string.address_text, "Location Name Not Found", result, System.currentTimeMillis()));
                    }
                }
            });
            //update UI dengan tampilan hasil alamat
            //jika aktif menampilkan alamat dan waktu
            //mLocationTextView.setText(getString(R.string.address_text, result, System.currentTimeMillis()));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        //passing data tempat yang dipilih melalui intent
        if (resultCode == RESULT_OK){
            //mendapatkan object place dari placepicker
            Place place = PlacePicker.getPlace(this, data);
            mLocationTextView.setText(getString(R.string.address_text, place.getName(), place.getAddress(), System.currentTimeMillis()));
            name = place.getName().toString();
            address = place.getAddress().toString();
            gambar = setTipeLokasi(place);
            mAndroidImageView.setImageResource(gambar);
        } else {
            mLocationTextView.setText("Pick The Location First!");
        }

    }

    //location type
    private static int setTipeLokasi(Place currentPlace){
        int drawableID = -1;
        for (Integer placeType : currentPlace.getPlaceTypes()){
            switch (placeType){
                case Place.TYPE_UNIVERSITY:
                    drawableID = R.drawable.school;
                    break;
                case Place.TYPE_SHOPPING_MALL:
                    drawableID = R.drawable.mall;
                    break;
                case Place.TYPE_CAFE:
                    drawableID = R.drawable.cafe;
                    break;
                case Place.TYPE_MOVIE_THEATER:
                    drawableID = R.drawable.cinema;
                    break;
                //tempat lain
                case Place.TYPE_AIRPORT:
                    drawableID = R.drawable.airport;
                    break;
                case Place.TYPE_STADIUM:
                    drawableID = R.drawable.football;
                    break;
                case Place.TYPE_BUS_STATION:
                    drawableID = R.drawable.busstation;
                    break;
                case Place.TYPE_ATM:
                    drawableID = R.drawable.atm;
                    break;
            }
        }
        if (drawableID < 0){
            drawableID = R.drawable.warning;
        }
        return drawableID;
    }

}
