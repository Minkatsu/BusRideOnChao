package com.example.katsumi.myapplication;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class InputSelection extends Fragment {

    View view;

    //  バス停入力用
    AutoCompleteTextView getOnAutoCompleteTextView, getOffAutoCompleteTextView;

    //  バス停の表示リスト
    String getOnBusStopList[], getOffBusStopList[];

    //  連結しているバス停のリスト
    String connectionBusStopList[];

    //  乗車・降車のバス停名
    String getOnBusStopName, getOffBusStopName;

    //  バスの情報一覧
    BusStopInformationList busStopInformationList;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //  バス停名の登録
        busStopInformationList = new BusStopInformationList();

        try {
            //連結しているバス停の取得
            setConnectionData();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.input_selection_window, container, false);

        // タイトルの設定
        getActivity().setTitle("バス停選択:");

        //  オートコンプリートの設定
        setAutoCompleteTextView();

        //  リセットボタンの設定
        view.findViewById(R.id.reset).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                setGetOnAutoCompleteTextView();
                setGetOffAutoCompleteTextView();
                getOnAutoCompleteTextView.setText(getOnBusStopName);
                getOffAutoCompleteTextView.setText(getOffBusStopName);
            }
        });

        // 入れ替えボタンの設定
        view.findViewById(R.id.change).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getOnBusStopName = getOnAutoCompleteTextView.getText().toString();
                getOffBusStopName = getOffAutoCompleteTextView.getText().toString();
                if (getOnBusStopName.length() != 0 || getOffBusStopName.length() != 0) {
                    String swap = "";
                    swap = getOnBusStopName;
                    getOnBusStopName = getOffBusStopName;
                    getOffBusStopName = swap;
                    getOnAutoCompleteTextView.setText(getOnBusStopName);
                    getOffAutoCompleteTextView.setText(getOffBusStopName);
                }
            }
        });
        return view;
    }

    //  連結しているバス停の取得
    public void setConnectionData() throws IOException {
        connectionBusStopList = new String[busStopInformationList.data.length + 1];

        String connectionData;

        try {
            //  テキストファイルから連結しているバス停のリストを取得
            InputStream inputStream = getActivity().getResources().getAssets().open("connection_bus_stop.txt");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            for (int i = 0; (connectionData = bufferedReader.readLine()) != null; i++) {
                connectionBusStopList[i] = connectionData;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //  オートコンプリートの設定
    public void setAutoCompleteTextView() {
        setGetOnAutoCompleteTextView();
        setGetOffAutoCompleteTextView();
    }

    //  出発オートコンプリートの設定
    public void setGetOnAutoCompleteTextView() {
        //  オートコンプリートの設定
        getOnAutoCompleteTextView = ((AutoCompleteTextView) (view.findViewById(R.id.SelectGetOn)));
        getOnAutoCompleteTextView.setThreshold(1);
        getOnAutoCompleteTextView.setCompletionHint("Select Get On Bus Stop");

        //  バス停の一覧を取得
        getOnBusStopList = new String[busStopInformationList.data.length];
        for (int i = 0; i < getOnBusStopList.length; i++) {
            getOnBusStopList[i] = busStopInformationList.data[i].BusStopName;
        }

        //  ArrayAdapterの設定
        ArrayAdapter<String> getOnArrayAdapter = new ArrayAdapter<>(getActivity(), R.layout.auto_geton_bus, getOnBusStopList);
        getOnAutoCompleteTextView.setAdapter(getOnArrayAdapter);

        //  フォーカスが当てられた時の処理
        getOnAutoCompleteTextView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean flag) {
                getOnBusStopName = getOnAutoCompleteTextView.getText().toString();
                getOffBusStopName = getOffAutoCompleteTextView.getText().toString();
                if (flag) {
                    //  オートコンプリートの取得
                    getAutoCompleteArrayAdapter(view);
                } else {
                    // ソフトキーボードを非表示にする
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
            }
        });
    }

    //  到着オートコンプリートの設定
    public void setGetOffAutoCompleteTextView() {
        //  オートコンプリートの設定
        getOffAutoCompleteTextView = ((AutoCompleteTextView) view.findViewById(R.id.SelectGetOff));
        getOffAutoCompleteTextView.setThreshold(1);
        getOffAutoCompleteTextView.setCompletionHint("Select Get Off Bus Stop");

        //  バス停の一覧を取得
        getOffBusStopList = new String[busStopInformationList.data.length];
        for (int i = 0; i < getOffBusStopList.length; i++) {
            getOffBusStopList[i] = busStopInformationList.data[i].BusStopName;
        }

        //  ArrayAdapterの設定
        ArrayAdapter<String> getOffArrayAdapter
                = new ArrayAdapter<>(getActivity(), R.layout.auto_getoff_bus, getOffBusStopList);
        getOffAutoCompleteTextView.setAdapter(getOffArrayAdapter);

        //  フォーカスが当てられた時の処理
        getOffAutoCompleteTextView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean flag) {
                getOnBusStopName = getOnAutoCompleteTextView.getText().toString();
                getOffBusStopName = getOffAutoCompleteTextView.getText().toString();
                if (flag) {
                    //  オートコンプリートの取得
                    getAutoCompleteArrayAdapter(view);
                } else {
                    // ソフトキーボードを非表示にする
                    InputMethodManager imm
                            = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
            }
        });
    }

    public void getAutoCompleteArrayAdapter(View view) {
        int ID;
        Integer connectionBusStopID[];

        switch (view.getId()) {
            case R.id.SelectGetOff:
                //  乗車のバス停の名前が存在する場合
                if (busStopInformationList.BusStopNameToID(getOnBusStopName) != null) {
                    ID = busStopInformationList.BusStopNameToID(getOnBusStopName);
                    connectionBusStopID = new Integer[connectionBusStopList[ID].split(" ").length];

                    for (int i = 0; i < connectionBusStopID.length; i++) {
                        connectionBusStopID[i] = Integer.parseInt(connectionBusStopList[ID].split(" ")[i]);
                    }

                    getOffBusStopList = new String[connectionBusStopID.length - 1];
                    for (int i = 1; i < connectionBusStopID.length; i++) {
                        getOffBusStopList[i - 1] = busStopInformationList.data[connectionBusStopID[i]].BusStopName;
                    }

                    ArrayAdapter<String> getOffArrayAdapter
                            = new ArrayAdapter<>(getActivity(), R.layout.auto_getoff_bus, getOffBusStopList);
                    getOffAutoCompleteTextView.setAdapter(getOffArrayAdapter);

                }
                //  乗車のバス停が入力されていない場合
                else if (getOnBusStopName.length() == 0) {
                    setGetOffAutoCompleteTextView();
                }
                break;

            case R.id.SelectGetOn:
                //  降車のバス停の名前が存在する場合
                if (busStopInformationList.BusStopNameToID(getOffBusStopName) != null) {
                    ID = busStopInformationList.BusStopNameToID(getOffBusStopName);

                    connectionBusStopID = new Integer[connectionBusStopList[ID].split(" ").length];
                    for (int i = 0; i < connectionBusStopID.length; i++) {
                        connectionBusStopID[i] = Integer.parseInt(connectionBusStopList[ID].split(" ")[i]);
                    }

                    getOnBusStopList = new String[connectionBusStopID.length - 1];
                    for (int i = 1; i < connectionBusStopID.length; i++) {
                        getOnBusStopList[i - 1] = busStopInformationList.data[connectionBusStopID[i]].BusStopName;
                    }

                    ArrayAdapter<String> getOnArrayAdapter
                            = new ArrayAdapter<>(getActivity(), R.layout.auto_geton_bus, getOnBusStopList);
                    getOnAutoCompleteTextView.setAdapter(getOnArrayAdapter);

                }
                //  乗車のバス停が入力されていない場合
                else if (getOffBusStopName.length() == 0) {
                    setGetOnAutoCompleteTextView();
                }
                break;
        }
    }
}
