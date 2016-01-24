package com.example.katsumi.myapplication;

import android.os.AsyncTask;
import android.view.View;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by K.S. on 16/01/21.
 */
public class SetBusStopMarker extends AsyncTask<MarkerOptions, MarkerOptions, MarkerOptions[]> {

    private MapSelection mapSelection;
    private GoogleMap googleMap;

    public SetBusStopMarker(MapSelection mapSelection, GoogleMap googleMap) {
        super();

        this.mapSelection = mapSelection;
        this.mapSelection.progressBar.setVisibility(View.VISIBLE);

        this.googleMap = googleMap;
        googleMap.clear();
    }

    @Override
    protected MarkerOptions[] doInBackground(MarkerOptions marker[]) {

        mapSelection.getOnBusStopName = "";
        mapSelection.getOffBusStopName = "";

        marker = new MarkerOptions[mapSelection.mainActivity.mBusStopInformationList.data.length];

        //  マーカーの設定
        for (int i = 0; i < marker.length; i++) {
            MarkerOptions markerOptions = new MarkerOptions();

            //  マーカーのタイトル、位置情報の設定
            markerOptions.title(mapSelection.mainActivity.mBusStopInformationList.data[i].BusStopName);
            LatLng position
                    = new LatLng(mapSelection.mainActivity.mBusStopInformationList.data[i].latitude,
                    mapSelection.mainActivity.mBusStopInformationList.data[i].longitude);
            markerOptions.position(position);
            markerOptions.icon(mapSelection.setIcon());

            marker[i] = markerOptions;
        }

        return marker;
    }

    @Override
    protected void onPostExecute(MarkerOptions markerOptions[]) {
        for (int i = 0; i < markerOptions.length; i++) {
            googleMap.addMarker(markerOptions[i]);
        }

        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                mapSelection.setMarkerClickEvent(marker.getTitle());

                return false;
            }
        });
        mapSelection.progressBar.setVisibility(View.INVISIBLE);
    }
}
