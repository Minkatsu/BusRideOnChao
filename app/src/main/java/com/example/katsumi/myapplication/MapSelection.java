package com.example.katsumi.myapplication;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
public class MapSelection extends Fragment {

    View view;

    //  Map
    MapFragment mMapFragment;
    GoogleMap googleMap;

    //  乗車・降車のバス停
    String getOnBusStopName = "";
    String getOffBusStopName = "";

    //  バス停の情報一覧
    BusStopInformationList busStopInformationList;

    //  連結しているバス停のリスト
    String connectionBusStopList[];

    private Handler mHandler = new Handler();

    ProgressBar progressBar;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getOnBusStopName = "";
        getOffBusStopName = "";

        busStopInformationList = new BusStopInformationList();

        //  連結しているバス停の取得
        setConnectionData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.map_selection_window, container, false);

        getActivity().setTitle("バス停選択:");

        progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);

        //  マップの取得
        mMapFragment = MapFragment.newInstance();
        getFragmentManager().beginTransaction().replace(R.id.MapView, MapFragment.newInstance()).commit();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        mHandler.post(new setUpMap());
    }

    public synchronized void sleep(long mSec) {
        try {
            wait(mSec);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //  連結しているバス停のデータの取得
    public void setConnectionData() {
        connectionBusStopList = new String[busStopInformationList.data.length + 1];

        String connectionData;

        try {
            //  テキストファイルから連結しているバス停のリストを取得
            InputStream inputStream = getActivity().getResources().getAssets().open("BusStopLine.txt");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            for (int i = 0; (connectionData = bufferedReader.readLine()) != null; i++) {
                connectionBusStopList[i] = connectionData;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    class setUpMap implements Runnable {
        @Override
        public void run() {
            try {
                mMapFragment = ((MapFragment) getFragmentManager().findFragmentById(R.id.MapView));
                googleMap = mMapFragment.getMap();

                mMapFragment.getMapAsync(new OnMapReadyCallback() {

                    @Override
                    public void onMapReady(GoogleMap googleMap) {
                        setUpMapOptions();
                    }
                });
            } catch (Exception e) {
                // 10ms待ってもう一回実行
                sleep(10);
                new Handler().post(this);

                e.printStackTrace();
            }
        }
    }

    public void setUpMapOptions() {
        googleMap.setMyLocationEnabled(true);

        //  盛岡駅にズームして表示
        LatLng Morioka_sta = new LatLng(39.701683, 141.136369);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Morioka_sta, 15));

        //  リセットボタンの設定
        view.findViewById(R.id.reset).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!getActivity().getTitle().equals("バス停選択:")) {
                    getActivity().setTitle("バス停選択:");
                }
                //setBusStopMarker();
                new SetBusStopMarker(googleMap).execute();
            }
        });

        // 入れ替えボタンの設定
        view.findViewById(R.id.change).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getOnBusStopName.length() != 0 || getOffBusStopName.length() != 0) {
                    String swap = getOnBusStopName;
                    getOnBusStopName = getOffBusStopName;
                    getOffBusStopName = swap;
                    getActivity().setTitle("バス停選択:" + getOnBusStopName + "→" + getOffBusStopName);
                }
            }
        });

        //  バス停マーカーの設定

        //setBusStopMarker();
        new SetBusStopMarker(googleMap).execute();
    }

    //  アラートダイアログの設定
    public void setMarkerClickEvent(final String title) {
        new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setMessage("出発、到着のバス停をを選択")
                .setPositiveButton("到着のバス停に設定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getActivity(), title + "を出発のバス停に設定しました", Toast.LENGTH_LONG).show();
                        getOffBusStopName = title;
                        setTitle();
                        new getConnectionBusStopList(getOffBusStopName, googleMap).execute();
                    }
                })
                .setNeutralButton("キャンセル", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setNegativeButton("出発のバス停に設定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getActivity(), title + "を到着のバス停に設定しました", Toast.LENGTH_LONG).show();
                        getOnBusStopName = title;
                        setTitle();
                        new getConnectionBusStopList(getOnBusStopName, googleMap).execute();
                    }
                })
                .create()
                .show();
    }

    //  タイトルの設定
    public void setTitle() {
        getActivity().setTitle("バス停選択:" + getOnBusStopName + "→" + getOffBusStopName);
    }

    public BitmapDescriptor setIcon() {
        try {
            Bitmap OriginalBusStopIcon = BitmapFactory.decodeResource(getResources(), R.mipmap.bus_stop);
            OriginalBusStopIcon = Bitmap.createScaledBitmap(OriginalBusStopIcon, 20, 44, false);
            BitmapDescriptor BusStopIcon = BitmapDescriptorFactory.fromBitmap(OriginalBusStopIcon);
            return BusStopIcon;
        } catch(Exception e ){
            return null;
        }
    }

    public class SetBusStopMarker extends AsyncTask<MarkerOptions, MarkerOptions, MarkerOptions[]> {

        private GoogleMap googleMap;

        public SetBusStopMarker(GoogleMap googleMap) {
            super();
            progressBar.setVisibility(View.VISIBLE);
            this.googleMap = googleMap;
            googleMap.clear();
        }

        @Override
        protected MarkerOptions[] doInBackground(MarkerOptions marker[]) {

            getOnBusStopName = "";
            getOffBusStopName = "";

            marker = new MarkerOptions[busStopInformationList.data.length];

            //  マーカーの設定
            for (int i = 0; i < marker.length; i++) {
                MarkerOptions markerOptions = new MarkerOptions();

                //  マーカーのタイトル、位置情報の設定
                markerOptions.title(busStopInformationList.data[i].BusStopName);
                LatLng position
                        = new LatLng(busStopInformationList.data[i].latitude, busStopInformationList.data[i].longitude);
                markerOptions.position(position);
                markerOptions.icon(setIcon());

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
                    setMarkerClickEvent(marker.getTitle());

                    return false;
                }
            });
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    public class getConnectionBusStopList extends AsyncTask<MarkerOptions, MarkerOptions, MarkerOptions[]> {

        private GoogleMap googleMap;
        private String busStopName;

        public getConnectionBusStopList(String busStopName, GoogleMap googleMap){
            super();
            progressBar.setVisibility(View.VISIBLE);

            this.busStopName = busStopName;
            this.googleMap = googleMap;

            googleMap.clear();
        }

        @Override
        protected MarkerOptions[] doInBackground(MarkerOptions marker[]) {

            int ID = busStopInformationList.BusStopNameToID(busStopName);

            Integer connectionBusStopID[] = new Integer[connectionBusStopList[ID].split(" ").length];
            for (int i = 0; i < connectionBusStopID.length; i++) {
                connectionBusStopID[i] = Integer.parseInt(connectionBusStopList[ID].split(" ")[i]);
            }

            marker = new MarkerOptions[connectionBusStopID.length];

            for (int i = 1; i < marker.length; i++) {
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.title(busStopInformationList.data[connectionBusStopID[i]].BusStopName);
                LatLng position = new LatLng(busStopInformationList.data
                        [connectionBusStopID[i]].latitude, busStopInformationList.data[connectionBusStopID[i]].longitude);
                markerOptions.position(position);

                //  アイコンの設定
                markerOptions.icon(setIcon());

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
                    setMarkerClickEvent(marker.getTitle());

                    return false;
                }
            });
            progressBar.setVisibility(View.INVISIBLE);
        }
    }
}