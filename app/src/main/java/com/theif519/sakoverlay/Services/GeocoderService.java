package com.theif519.sakoverlay.Services;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;

import com.theif519.sakoverlay.Misc.Globals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by lpj11535 on 12/2/2015.
 */
public class GeocoderService extends IntentService {

    public GeocoderService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        ResultReceiver result
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        Location location = intent.getParcelableExtra(Globals.Keys.GEOCODER_DECODE_LOCATION);
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
        } catch(IOException | IllegalArgumentException e){
            Log.w(getClass().getName(), e.getMessage());
        }
        if(addresses != null || addresses.size() > 0){
            Address address = addresses.get(0);
            ArrayList<String> partialAddresses = new ArrayList<>();
            for(int i = 0; i < address.getMaxAddressLineIndex(); i++){
                partialAddresses.add(address.getAddressLine(i));
            }
            TextUtils.join(System.getProperty("line.seperator"), partialAddresses);

        }
    }
}
