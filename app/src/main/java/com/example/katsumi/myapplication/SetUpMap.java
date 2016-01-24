package com.example.katsumi.myapplication;

import android.os.Handler;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;

class SetUpMap implements Runnable {
    private MapSelection mapSelection;

    public SetUpMap(MapSelection mapSelection) {
        this.mapSelection = mapSelection;
    }

    @Override
    public void run() {
        try {
            mapSelection.mMapFragment = ((MapFragment) mapSelection.getFragmentManager().findFragmentById(R.id.MapView));
            mapSelection.googleMap = mapSelection.mMapFragment.getMap();

            mapSelection.mMapFragment.getMapAsync(new OnMapReadyCallback() {

                @Override
                public void onMapReady(GoogleMap googleMap) {
                    mapSelection.setUpMapOptions();
                }
            });
        } catch (Exception e) {
            // 10ms待ってもう一回実行
            mapSelection.sleep(10);
            new Handler().post(this);

            e.printStackTrace();
        }
    }
}
