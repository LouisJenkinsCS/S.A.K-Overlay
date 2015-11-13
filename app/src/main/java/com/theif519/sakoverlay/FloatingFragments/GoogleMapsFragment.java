package com.theif519.sakoverlay.FloatingFragments;

import android.location.Location;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.theif519.sakoverlay.R;

/**
 * Created by theif519 on 11/4/2015.
 */
public class GoogleMapsFragment extends FloatingFragment {

    public static final String IDENTIFIER = "Google Maps";

    private GoogleMap map;

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
        ((MapFragment) getChildFragmentManager().findFragmentById(R.id.map)).getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                map = googleMap;
                map.setMyLocationEnabled(true);
                map.getMyLocation();
                map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                map.setTrafficEnabled(true);
                map.setBuildingsEnabled(true);
                map.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
                    @Override
                    public void onMyLocationChange(Location location) {
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 18.0f));
                    }
                });
            }
        });
    }

}
