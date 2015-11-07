package com.theif519.sakoverlay;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;

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
            }
        });
    }

}
