package com.example.katsumi.myapplication;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;

public class MapSelection extends Fragment {

    View view;

    //  Map
    MapFragment mMapFragment;
    GoogleMap googleMap;

    //  乗車・降車のバス停
    String getOnBusStopName = "";
    String getOffBusStopName = "";

    //  連結しているバス停のリスト
    String connectionBusStopList[];

    private Handler mHandler = new Handler();

    ProgressBar progressBar;

    MainActivity mainActivity;

    MapSelection(MainActivity mainActivity){
        this.mainActivity = mainActivity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getOnBusStopName =  mainActivity.getOnBusStopText.getText().toString();
        getOffBusStopName = mainActivity.getOffBusStopText.getText().toString();

        //  連結しているバス停の取得
        setConnectionData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.map_selection_window, container, false);

        getActivity().setTitle("Select The Bus Stop");

        progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);

        //  リセットボタンの設定
        getActivity().findViewById(R.id.reset).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getOnBusStopName = "";
                getOffBusStopName = "";
                setTitle();

                new SetBusStopMarker(MapSelection.this, googleMap).execute();
            }
        });

        // 入れ替えボタンの設定
        getActivity().findViewById(R.id.swap).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getOnBusStopName = mainActivity.getOnBusStopText.getText().toString();
                getOffBusStopName = mainActivity.getOffBusStopText.getText().toString();
                mainActivity.getOnBusStopText.setText(getOffBusStopName);
                mainActivity.getOffBusStopText.setText(getOnBusStopName);
            }
        });

        getActivity().findViewById(R.id.go_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mainActivity.exceptionCheck() == mainActivity.SUCCESS) {
                    mainActivity.changeToDisplayTimetableMode();
                }
            }
        });

        //  マップの取得
        mMapFragment = MapFragment.newInstance();
        getFragmentManager().beginTransaction().replace(R.id.MapView, MapFragment.newInstance()).commit();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        mHandler.post(new SetUpMap(this));
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
        connectionBusStopList = new String[mainActivity.mBusStopInformationList.data.length];
        try {
            new GetBusLine(this,
                    "https://raw.githubusercontent.com/Minkatsu/BusRideOnCiaoData/master/BusStopLine.txt").execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setUpMapOptions() {
        googleMap.setMyLocationEnabled(true);

        //  盛岡駅にズームして表示
        LatLng Morioka_sta = new LatLng(39.701683, 141.136369);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Morioka_sta, 15));

        //  バス停マーカーの設定
        new SetBusStopMarker(this, googleMap).execute();
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
                        new GetConnectionBusStopList(MapSelection.this, getOffBusStopName, googleMap).execute();
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
                        new GetConnectionBusStopList(MapSelection.this, getOnBusStopName, googleMap).execute();
                    }
                })
                .create()
                .show();
    }

    //  タイトルの設定
    public void setTitle() {
        mainActivity.getOnBusStopText.setText(getOnBusStopName);
        mainActivity.getOffBusStopText.setText(getOffBusStopName);
    }

    public BitmapDescriptor setIcon() {
        try {
            Bitmap OriginalBusStopIcon = BitmapFactory.decodeResource(getResources(), R.mipmap.bus_stop_point);
            OriginalBusStopIcon = Bitmap.createScaledBitmap(OriginalBusStopIcon, 40, 40, false);
            BitmapDescriptor BusStopIcon = BitmapDescriptorFactory.fromBitmap(OriginalBusStopIcon);
            return BusStopIcon;
        } catch(Exception e ){
            return null;
        }
    }

}