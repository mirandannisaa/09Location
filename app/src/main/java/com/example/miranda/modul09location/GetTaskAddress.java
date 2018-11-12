package com.example.miranda.modul09location;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.location.Location;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GetTaskAddress extends AsyncTask<Location, Void, String> {

    private Context mContext;
    private onTaskFinish mListener;

    GetTaskAddress(Context applicationContext, onTaskFinish listener){
        mContext = applicationContext;
        mListener = listener;
    }

    @Override
    protected void onPostExecute(String address) {
        mListener.onTaskCompleted(address);
        super.onPostExecute(address);
    }

    @Override
    protected String doInBackground(Location... locations) {
        Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
        Location location = locations[0];
        List<Address> address = null;
        String resultMessage = "";

        try {
            //maxresult = max alamat
            address = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
        } catch (IOException ioException){
            resultMessage = "Service Not Available";
            Log.e("Error Location", resultMessage, ioException);
        } catch (IllegalArgumentException illegalArgumentException){
            resultMessage = "Invalid Coordinates";
            Log.e("Error Location", resultMessage + ". " + "Latitude = " + location.getLatitude() + ", Longtitude = " + location.getLongitude(), illegalArgumentException);
        }

        //jika alamat tidak ditemukan maka muncul error
        if (address == null || address.size() == 0){
            if(resultMessage.isEmpty()){
                resultMessage = "Address Not Found";
                Log.e("Error Location", resultMessage);
            }
        } else {
            //jika alamat ditemukan, proses dan masukkan ke resultMessage
            Address myAddress = address.get(0);
            ArrayList<String> addressLine = new ArrayList<>();

            //get baris alamat using fungsi getAddressLine lalu gabungkan
            for (int i=0; i<=myAddress.getMaxAddressLineIndex(); i++){
                addressLine.add(myAddress.getAddressLine(i));
            }
            //Gabung line address dipisah baris baru
            resultMessage = TextUtils.join("\n", addressLine);
        }

        return resultMessage;
    }

    interface onTaskFinish{
        void onTaskCompleted(String result);
    }
}
