package com.theif519.sakoverlay.Fragments.Widgets;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.theif519.sakoverlay.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

/**
 * Created by theif519 on 11/4/2015.
 * <p/>
 * A simple Google Maps floating fragment, made more as a proof of concept in the meantime. In the future
 * it will provide the following functionalities...
 * <p/>
 * 1) Display nearby shops/restaurants/gas stations, etc. I.E, if the user has a preference for Weiss, it will
 * always alert him when one is nearby. Google Maps API would handle and abstract almost everything of course.
 * <p/>
 * 2) Always display where the user currently is, which right now is implemented, albeit poorly and minimal.
 * Right now it displays the address/current location in a text view embedded on top of the BaseWidget.
 * <p/>
 * Right now those are the goals of it. I'm not sure if I care much for navigation, as this app isn't meant
 * for that, it's meant for gaming. Hence, if you are on the go, have your head down in your phone/tablet
 * and you're within a mile or so distance of your favorite store, you may see an alert/notification letting
 * you know.
 */
public class GoogleMapsWidget extends BaseWidget {

    public GoogleMapsWidget() {
        mLayoutId = R.layout.google_maps;
        mIconId = R.drawable.maps;
        LAYOUT_TAG = IDENTIFIER;
    }

    protected static final String IDENTIFIER = "Google Maps";

    private GoogleMap mMap;

    private Geocoder mGeocoder;

    /*
        Utilizing a PublishSubject, we can publish a new task which allows us to delegate it to a background
        thread, which is handled whenever we emit a new item/task. Handy.
    */
    private PublishSubject<Location> mOnNextAddress = PublishSubject.create();

    @Override
    public void setup() {
        super.setup();
        TextView mAddress = (TextView) getContentView().findViewById(R.id.google_maps_address);
        ((MapFragment) getChildFragmentManager().findFragmentById(R.id.map)).getMapAsync(googleMap -> {
            mMap = googleMap;
            mMap.setMyLocationEnabled(true);
            mMap.getMyLocation();
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            mMap.setTrafficEnabled(true);
            mMap.setBuildingsEnabled(true);
            mGeocoder = new Geocoder(getActivity(), Locale.getDefault());
            mMap.setOnMyLocationChangeListener(location -> {
                mOnNextAddress.onNext(location);
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 18.0f));
            });
        });
        mOnNextAddress
                .observeOn(Schedulers.io()) // Do processing of next address change on a background IO-Bound thread, as geocoder can block.
                .throttleLast(5, TimeUnit.SECONDS) // As changes can occur rather frequently, we only want to handle it once per 5 seconds.
                .map(location -> {
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
                )
                .filter(s -> s != null)
                .observeOn(AndroidSchedulers.mainThread()) // Swap back to the main thread for the easy bit.
                .subscribe(mAddress::setText);
    }

}
