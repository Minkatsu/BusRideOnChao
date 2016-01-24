package com.example.katsumi.myapplication;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import java.io.IOException;

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


    MainActivity mainActivity;

    InputSelection(MainActivity mainActivity){
        this.mainActivity = mainActivity;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
        getActivity().setTitle("Select The Bus Stop");

        //  オートコンプリートの設定
        setAutoCompleteTextView();

        //  リセットボタンの設定
        getActivity().findViewById(R.id.reset).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getOnBusStopName = "";
                getOffBusStopName = "";

                setGetOnAutoCompleteTextView();
                setGetOffAutoCompleteTextView();

                getOnAutoCompleteTextView.setText("");
                getOffAutoCompleteTextView.setText("");

                mainActivity.getOnBusStopText.setText("");
                mainActivity.getOffBusStopText.setText("");
            }
        });

        // 入れ替えボタンの設定
        getActivity().findViewById(R.id.swap).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setBusStop();
                if (getOnBusStopName.length() != 0 || getOffBusStopName.length() != 0) {

                    getOnBusStopName = mainActivity.getOnBusStopText.getText().toString();
                    getOffBusStopName = mainActivity.getOffBusStopText.getText().toString();

                    mainActivity.getOnBusStopText.setText(getOffBusStopName);
                    mainActivity.getOffBusStopText.setText(getOnBusStopName);

                    getOnAutoCompleteTextView.setText(getOffBusStopName);
                    getOffAutoCompleteTextView.setText(getOnBusStopName);
                }
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

        return view;
    }

    //  連結しているバス停の取得
    public void setConnectionData() throws IOException {
        connectionBusStopList = new String[mainActivity.mBusStopInformationList.data.length];
        try {
            new GetBusLine(this,
                    "https://raw.githubusercontent.com/Minkatsu/BusRideOnCiaoData/master/BusStopLine.txt").execute();
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
        getOnBusStopList = new String[mainActivity.mBusStopInformationList.data.length];
        for (int i = 0; i < getOnBusStopList.length; i++) {
            getOnBusStopList[i] = mainActivity.mBusStopInformationList.data[i].BusStopName;
        }

        //  ArrayAdapterの設定
        ArrayAdapter<String> getOnArrayAdapter = new ArrayAdapter<>(getActivity(), R.layout.auto_geton_bus, getOnBusStopList);
        getOnAutoCompleteTextView.setAdapter(getOnArrayAdapter);

        //  フォーカスが当てられた時の処理
        getOnAutoCompleteTextView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean flag) {
                setBusStop();
                if (flag) {
                    mainActivity.getOnBusStopText.setText(getOnBusStopName);
                    mainActivity.getOffBusStopText.setText(getOffBusStopName);
                    //  オートコンプリートの取得
                    getAutoCompleteArrayAdapter(view);
                } else {
                    // ソフトキーボードを非表示にする
                    mainActivity.getOnBusStopText.setText(getOnBusStopName);
                    mainActivity.getOffBusStopText.setText(getOffBusStopName);
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
            }
        });

        getOnAutoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mainActivity.getOnBusStopText.setText((String) (parent.getItemAtPosition(position)));
            }
        });

        getOnAutoCompleteTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mainActivity.getOnBusStopText.setText(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

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
        getOffBusStopList = new String[mainActivity.mBusStopInformationList.data.length];
        for (int i = 0; i < getOffBusStopList.length; i++) {
            getOffBusStopList[i] = mainActivity.mBusStopInformationList.data[i].BusStopName;
        }

        //  ArrayAdapterの設定
        ArrayAdapter<String> getOffArrayAdapter
                = new ArrayAdapter<>(getActivity(), R.layout.auto_getoff_bus, getOffBusStopList);
        getOffAutoCompleteTextView.setAdapter(getOffArrayAdapter);

        //  フォーカスが当てられた時の処理
        getOffAutoCompleteTextView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean flag) {
                setBusStop();
                if (flag) {
                    //  オートコンプリートの取得
                    mainActivity.getOnBusStopText.setText(getOnBusStopName);
                    mainActivity.getOffBusStopText.setText(getOffBusStopName);
                    getAutoCompleteArrayAdapter(view);
                } else {
                    // ソフトキーボードを非表示にする
                    mainActivity.getOnBusStopText.setText(getOnBusStopName);
                    mainActivity.getOffBusStopText.setText(getOffBusStopName);

                    InputMethodManager imm
                            = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
            }
        });

        getOffAutoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mainActivity.getOffBusStopText.setText((String) (parent.getItemAtPosition(position)));
            }
        });


        getOffAutoCompleteTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mainActivity.getOffBusStopText.setText(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    public void getAutoCompleteArrayAdapter(View view) {
        int ID;
        Integer connectionBusStopID[];
        try {
            switch (view.getId()) {
                case R.id.SelectGetOff:
                    //  乗車のバス停の名前が存在する場合
                    if (mainActivity.mBusStopInformationList.BusStopNameToID(getOnBusStopName) != null) {
                        ID = mainActivity.mBusStopInformationList.BusStopNameToID(getOnBusStopName);
                        connectionBusStopID = new Integer[connectionBusStopList[ID].split(" ").length];

                        for (int i = 0; i < connectionBusStopID.length; i++) {
                            connectionBusStopID[i] = Integer.parseInt(connectionBusStopList[ID].split(" ")[i]);
                        }

                        getOffBusStopList = new String[connectionBusStopID.length - 1];
                        for (int i = 1; i < connectionBusStopID.length; i++) {
                            getOffBusStopList[i - 1] = mainActivity.mBusStopInformationList.data[connectionBusStopID[i]].BusStopName;
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
                    if (mainActivity.mBusStopInformationList.BusStopNameToID(getOffBusStopName) != null) {
                        ID = mainActivity.mBusStopInformationList.BusStopNameToID(getOffBusStopName);

                        connectionBusStopID = new Integer[connectionBusStopList[ID].split(" ").length];
                        for (int i = 0; i < connectionBusStopID.length; i++) {
                            connectionBusStopID[i] = Integer.parseInt(connectionBusStopList[ID].split(" ")[i]);
                        }

                        getOnBusStopList = new String[connectionBusStopID.length - 1];
                        for (int i = 1; i < connectionBusStopID.length; i++) {
                            getOnBusStopList[i - 1] = mainActivity.mBusStopInformationList.data[connectionBusStopID[i]].BusStopName;
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
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void setBusStop(){
        getOnBusStopName = getOnAutoCompleteTextView.getText().toString();
        getOffBusStopName = getOffAutoCompleteTextView.getText().toString();
    }
}
