package com.theif519.sakoverlay.Fragments.Floating;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.theif519.sakoverlay.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

/**
 * Created by theif519 on 11/4/2015.
 */
public class GoogleMapsFragment extends FloatingFragment {

    protected static final String IDENTIFIER = "Google Maps";

    private GoogleMap mMap;

    private Geocoder mGeocoder;

    private TextView mAddress;

    private PublishSubject<Location> mOnNextAddress = PublishSubject.create();

    public static GoogleMapsFragment newInstance() {
        GoogleMapsFragment fragment = new GoogleMapsFragment();
        fragment.LAYOUT_ID = R.layout.google_maps;
        fragment.LAYOUT_TAG = IDENTIFIER;
        fragment.ICON_ID = R.drawable.maps;
        return fragment;
    }

    @Override
    public void setup() {
        super.setup();
        mAddress = (TextView) getContentView().findViewById(R.id.google_maps_address);
        ((MapFragment) getChildFragmentManager().findFragmentById(R.id.map)).getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
                mMap.setMyLocationEnabled(true);
                mMap.getMyLocation();
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                mMap.setTrafficEnabled(true);
                mMap.setBuildingsEnabled(true);
                mGeocoder = new Geocoder(getActivity(), Locale.getDefault());
                mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
                    @Override
                    public void onMyLocationChange(Location location) {
                        mOnNextAddress.onNext(location);
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 18.0f));
                    }
                });
            }
        });
        mOnNextAddress
                .observeOn(AndroidSchedulers.mainThread()) // When finished processing, update on UI thread.
                .subscribeOn(Schedulers.io()) // Specify the scheduler to run processing on an IO blocking thread.
                .map(new Func1<Location, String>() { // Turn each location update into the String address. Geocoder blocks on getFromLocation, hence the IO scheduler.
                    @Override
                    public String call(Location location) {
                        String address = null;
                        try {
                            List<Address> addresses = mGeocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                            if (addresses != null && !addresses.isEmpty()) {
                                Address a = addresses.get(0);
                                ArrayList<String> partialAddresses = new ArrayList<String>(a.getMaxAddressLineIndex());
                                for (int i = 0; i < a.getMaxAddressLineIndex(); i++) {
                                    partialAddresses.add(a.getAddressLine(i));
                                }
                                address = TextUtils.join(System.getProperty("line.separator"), partialAddresses);
                            }
                        } catch (IOException | IllegalArgumentException e) {
                            Log.w(getClass().getName(), e.getMessage());
                        }
                        return address;
                    }
                })
                .filter(new Func1<String, Boolean>() { // Anything that returns null gets filtered out here.
                    @Override
                    public Boolean call(String s) {
                        return s != null;
                    }
                })
                .subscribe(new Action1<String>() { // Now the UI does literally next to no work.
                    @Override
                    public void call(String s) {
                        mAddress.setText(s);
                    }
                });
    }

}
