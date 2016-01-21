package com.example.katsumi.myapplication;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Arrays;

public class MainActivity extends ActionBarActivity implements View.OnClickListener {

    //  重ねるレイアウト
    private final InputSelection ISWindow = new InputSelection();
    private final MapSelection MSWindow = new MapSelection();
    private final DisplayTimetable DTWindow = new DisplayTimetable();
    private final DisplayLocation DSSWindow = new DisplayLocation();

    //  ボタン
    Button INPUT_BUTTON, MAP_BUTTON, TIMETABLE_BUTTON, LOCATION_BUTTON;

    //  エラー番号
    public final int SUCCESS = 0;
    public final int NoExistGetOnBusStop = 110;
    public final int NoExistGetOffBusStop = 101;
    public final int NoInputGetOnBusStop = 120;
    public final int NoInputGetOffBusStop = 102;

    //  モード番号
    private int MODE;
    private final int INPUT_SELECTION_MODE = 0;
    private final int MAP_SELECTION_MODE = 1;
    private final int TIMETABLE = 2;
    private final int LOCATION = 3;

    //  乗車・降車のバス停（）
    String getOnBusStopName, getOffBusStopName;
    String[] busStopArray;
    BusStopInformationList busStopInformationList = new BusStopInformationList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //  クリックリスナーの登録
        setClickListener();

        //  入力選択画面の表示
        changeToInputMode();

        //
        busStopArray = new String[busStopInformationList.data.length];
        for (int i = 0; i < busStopArray.length; i++) {
            busStopArray[i] = busStopInformationList.data[i].BusStopName;
        }
    }

    @Override
    public void onClick(View v) {
        menuClickEvent(v);
    }

    //  メニューボタンのクリックイベント
    public void menuClickEvent(View v) {
        switch (MODE) {
            case INPUT_SELECTION_MODE:
                switch (v.getId()) {
                    case R.id.InputSelection:
                        break;

                    case R.id.MapSelection:
                        changeToMapMode();
                        break;

                    case R.id.Timetable:
                        if (exceptionCheck() == SUCCESS) {
                            setTitleD();
                            changeToDisplayTimetableMode();
                        }
                        break;

                    case R.id.Location:
                        if (exceptionCheck() == SUCCESS) {
                            setTitleD();
                            changeToDisplayLocationMode();
                        }
                        break;
                }
                break;

            case MAP_SELECTION_MODE:
                switch (v.getId()) {
                    case R.id.InputSelection:
                        changeToInputMode();
                        break;

                    case R.id.MapSelection:
                        break;

                    case R.id.Timetable:
                        if (exceptionCheck() == SUCCESS) {
                            changeToDisplayTimetableMode();
                        }
                        break;
                    case R.id.Location:
                        if (exceptionCheck() == SUCCESS) {
                            changeToDisplayLocationMode();
                        }
                        break;
                }
                break;

            case TIMETABLE:
                switch (v.getId()) {
                    case R.id.InputSelection:
                        changeToInputMode();
                        break;

                    case R.id.MapSelection:
                        changeToMapMode();
                        break;

                    case R.id.Timetable:
                        break;

                    case R.id.Location:
                        changeToDisplayLocationMode();
                        break;
                }
                break;

            case LOCATION:
                switch (v.getId()) {
                    case R.id.InputSelection:
                        changeToInputMode();
                        break;

                    case R.id.MapSelection:
                        changeToMapMode();
                        break;

                    case R.id.Timetable:
                        changeToDisplayTimetableMode();
                        break;

                    case R.id.Location:
                        changeToDisplayLocationMode();
                        break;
                }
                break;
        }
    }

    //  ボタンのクリックリスナー登録
    public void setClickListener() {
        INPUT_BUTTON = (Button) findViewById(R.id.InputSelection);
        INPUT_BUTTON.setOnClickListener(this);

        MAP_BUTTON = (Button) findViewById(R.id.MapSelection);
        MAP_BUTTON.setOnClickListener(this);

        TIMETABLE_BUTTON = (Button) findViewById(R.id.Timetable);
        TIMETABLE_BUTTON.setOnClickListener(this);

        LOCATION_BUTTON = (Button) findViewById(R.id.Location);
        LOCATION_BUTTON.setOnClickListener(this);
    }

    //  入力選択モードへの移行
    public void changeToInputMode() {
        //  ボタンのアイコンの色を変更
        changeIconColor();

        MODE = INPUT_SELECTION_MODE;

        //  ボタンのアイコンの設定
        Drawable image = getResources().getDrawable(R.mipmap.edit_square_red);
        INPUT_BUTTON.setCompoundDrawablesWithIntrinsicBounds(null, null, null, image);

        getFragmentManager().beginTransaction().replace(R.id.MainWindow, ISWindow).commit();
    }

    //  マップ選択モードへの移行
    public void changeToMapMode() {
        //  ボタンのアイコンの色を変更
        changeIconColor();

        MODE = MAP_SELECTION_MODE;

        //  ボタンのアイコンの設定
        Drawable image = getResources().getDrawable(R.mipmap.map_with_placeholder_red);
        MAP_BUTTON.setCompoundDrawablesWithIntrinsicBounds(null, null, null, image);

        getFragmentManager().beginTransaction().replace(R.id.MainWindow, MSWindow).commit();
    }

    //  時刻表表示モードへの移行
    public void changeToDisplayTimetableMode() {
        //  ボタンのアイコンの色を変更
        changeIconColor();

        MODE = TIMETABLE;

        //  ボタンのアイコンの設定
        Drawable image = getResources().getDrawable(R.mipmap.calendar_with_a_clock_time_tools_red);
        TIMETABLE_BUTTON.setCompoundDrawablesWithIntrinsicBounds(null, null, null, image);

        getFragmentManager().beginTransaction().replace(R.id.MainWindow, DTWindow).commit();
    }

    //  運行状況表示モードへの移行
    public void changeToDisplayLocationMode() {
        //  ボタンのアイコンの色を変更
        changeIconColor();

        MODE = LOCATION;

        //  ボタンのアイコンの設定
        Drawable image = getResources().getDrawable(R.mipmap.bus_side_view_red);
        LOCATION_BUTTON.setCompoundDrawablesWithIntrinsicBounds(null, null, null, image);

        getFragmentManager().beginTransaction().replace(R.id.MainWindow, DSSWindow).commit();
    }

    //  ボタンのアイコンの色の変更
    void changeIconColor() {
        Drawable image;
        switch (MODE) {
            case INPUT_SELECTION_MODE:
                try {
                    image = getResources().getDrawable(R.mipmap.edit_square_blue);
                    INPUT_BUTTON.setCompoundDrawablesWithIntrinsicBounds(null, null, null, image);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    break;
                }
            case MAP_SELECTION_MODE:
                try {
                    image = getResources().getDrawable(R.mipmap.map_with_placeholder_blue);
                    MAP_BUTTON.setCompoundDrawablesWithIntrinsicBounds(null, null, null, image);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    break;
                }
            case TIMETABLE:
                try {
                    image = getResources().getDrawable(R.mipmap.calendar_with_a_clock_time_tools_blue);
                    TIMETABLE_BUTTON.setCompoundDrawablesWithIntrinsicBounds(null, null, null, image);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    break;
                }
            case LOCATION:
                try {
                    image = getResources().getDrawable(R.mipmap.bus_side_view_blue);
                    LOCATION_BUTTON.setCompoundDrawablesWithIntrinsicBounds(null, null, null, image);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    break;
                }
        }
    }


    //  エラー判定
    public int exceptionCheck() {

        int checker = SUCCESS;

        switch (MODE) {
            case INPUT_SELECTION_MODE:
                EditText getOnBusStopEdit = (EditText) findViewById(R.id.SelectGetOn);
                EditText getOffBusStopEdit = (EditText) findViewById(R.id.SelectGetOff);

                getOnBusStopName = getOnBusStopEdit.getText().toString();
                getOffBusStopName = getOffBusStopEdit.getText().toString();

                if (getOnBusStopName.length() == 0) {
                    checker += NoInputGetOnBusStop;
                } else if (!Arrays.asList(busStopArray).contains(getOnBusStopName)) {
                    checker += NoExistGetOnBusStop;
                }

                if (getOffBusStopName.length() == 0) {
                    checker += NoInputGetOffBusStop;
                } else if (!Arrays.asList(busStopArray).contains(getOffBusStopName)) {
                    checker += NoExistGetOffBusStop;
                }
                break;

            case MAP_SELECTION_MODE:
                String[] s = getTitle().toString().replace("→", ":").split(":");

                switch (s.length) {
                    case 1:
                        checker = (NoInputGetOnBusStop + NoInputGetOffBusStop);
                        break;
                    case 2:
                        checker = NoInputGetOffBusStop;
                        break;
                    case 3:
                        if (s[1].length() == 0) {
                            checker = NoInputGetOnBusStop;
                        } else {
                            checker = SUCCESS;
                        }
                        break;
                }
                break;
        }

        switch (checker) {
            case 222:
                Toast.makeText(this, "バス停が入力されていません．", Toast.LENGTH_SHORT).show();
                break;
            case 221:
                Toast.makeText(this, "乗車のバス停が入力されていません．\n降車のバス停が存在しません．", Toast.LENGTH_SHORT).show();
                break;
            case 120:
                Toast.makeText(this, "乗車のバス停が入力されていません．", Toast.LENGTH_SHORT).show();
                break;
            case 212:
                Toast.makeText(this, "乗車のバス停が存在しません．\n降車のバス停が入力されていません．", Toast.LENGTH_SHORT).show();
                break;
            case 211:
                Toast.makeText(this, "乗車のバス停が存在しません．\n降車のバス停が存在しません．", Toast.LENGTH_SHORT).show();
                break;
            case 110:
                Toast.makeText(this, "乗車のバス停が存在しません．", Toast.LENGTH_SHORT).show();
                break;
            case 102:
                Toast.makeText(this, "降車のバス停が入力されていません．", Toast.LENGTH_SHORT).show();
                break;
            case 101:
                Toast.makeText(this, "降車のバス停が存在しません．", Toast.LENGTH_SHORT).show();
                break;
            case 0:
                break;
        }

        return checker;
    }

    //  ゴリ押すためのタイトル設定
    public void setTitleD() {
        setTitle("バス停選択:" + getOnBusStopName + "→" + getOffBusStopName);
    }
}
