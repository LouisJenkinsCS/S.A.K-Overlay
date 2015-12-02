package com.theif519.sakoverlay.Fragments.Floating;

import android.location.Geocoder;
import android.location.Location;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.theif519.sakoverlay.R;

import java.util.Locale;

/**
 * Created by theif519 on 11/4/2015.
 */
public class GoogleMapsFragment extends FloatingFragment {

    protected static final String IDENTIFIER = "Google Maps";

    private GoogleMap mMap;

    private Geocoder mGeocoder;

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
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 18.0f));
                    }
                });
            }
        });
    }

}
