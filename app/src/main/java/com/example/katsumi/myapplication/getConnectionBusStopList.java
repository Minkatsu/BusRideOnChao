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
public class getConnectionBusStopList extends AsyncTask<MarkerOptions, MarkerOptions, MarkerOptions[]> {

    private MapSelection mapSelection;
    private GoogleMap googleMap;
    private String busStopName;

    public getConnectionBusStopList(MapSelection mapSelection, String busStopName, GoogleMap googleMap) {
        super();
        this.mapSelection = mapSelection;
        mapSelection.progressBar.setVisibility(View.VISIBLE);

        this.busStopName = busStopName;
        this.googleMap = googleMap;

        googleMap.clear();
    }

    @Override
    protected MarkerOptions[] doInBackground(MarkerOptions marker[]) {

        int ID = mapSelection.mainActivity.mBusStopInformationList.BusStopNameToID(busStopName);

        Integer connectionBusStopID[] = new Integer[mapSelection.connectionBusStopList[ID].split(" ").length];
        for (int i = 0; i < connectionBusStopID.length; i++) {
            connectionBusStopID[i] = Integer.parseInt(mapSelection.connectionBusStopList[ID].split(" ")[i]);
        }

        marker = new MarkerOptions[connectionBusStopID.length];

        for (int i = 1; i < marker.length; i++) {
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.title(mapSelection.mainActivity.mBusStopInformationList.data[connectionBusStopID[i]].BusStopName);
            LatLng position = new LatLng(mapSelection.mainActivity.mBusStopInformationList.data
                    [connectionBusStopID[i]].latitude, mapSelection.mainActivity.mBusStopInformationList.data[connectionBusStopID[i]].longitude);
            markerOptions.position(position);

            //  アイコンの設定
            markerOptions.icon(mapSelection.setIcon());

            marker[i] = markerOptions;
        }

        return marker;
    }

    @Override
    protected void onPostExecute(MarkerOptions markerOptions[]) {
        for (int i = 1; i < markerOptions.length; i++) {
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
