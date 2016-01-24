package com.example.katsumi.myapplication;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by g031k065 on 2015/01/29.
 */
public class DisplayLocation extends Fragment {

    View view;
    String TITLE;

    String getOnBusStopName, getOffBusStopName;
    int getOnBusStopLinkID, getOffBusStopLinkID;

    MainActivity mainActivity;

    DisplayLocation(MainActivity mainActivity){
        this.mainActivity = mainActivity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.display_location_window, container, false);

        TITLE = getActivity().getTitle().toString();
        getActivity().setTitle("Bus Location");

        setUp();

        return view;
    }

    //  準備
    private void setUp() {
        getOnBusStopName =  mainActivity.getOnBusStopText.getText().toString();
        getOffBusStopName = mainActivity.getOffBusStopText.getText().toString();

        getLinkID();

        getHTMLSource();
    }

    //  LinkIDの取得
    private void getLinkID() {
        getOnBusStopLinkID = mainActivity.mBusStopInformationList.BusStopNameToID(getOnBusStopName);
        getOnBusStopLinkID = mainActivity.mBusStopInformationList.IDToLinkID(getOnBusStopLinkID);

        getOffBusStopLinkID = mainActivity.mBusStopInformationList.BusStopNameToID(getOffBusStopName);
        getOffBusStopLinkID = mainActivity.mBusStopInformationList.IDToLinkID(getOffBusStopLinkID);

        Toast.makeText(getActivity(), getOnBusStopName + getOnBusStopLinkID + getOffBusStopName + getOffBusStopLinkID, Toast.LENGTH_SHORT).show();
    }


    //  HTMLソースを取得する
    private void getHTMLSource() {
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());

        HttpURLConnection http = null;
        InputStream inputStream = null;
        try {
            // URLにHTTP接続
            URL url = new URL("http://gps.iwatebus.or.jp/bls/pc/" +
                    "busiti_jk.jsp?jjg=1&jtr=" + getOnBusStopLinkID +
                    "&kjg=3&ktr=" + getOffBusStopLinkID + "&don=2");
            http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod("GET");
            http.connect();

            // データを取得
            inputStream = http.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "SJIS");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            // データの編集

            String preSource, postSource = new String();
            while ((preSource = bufferedReader.readLine()) != null) {
                postSource += ArrangeSource(preSource);
            }

            if (!postSource.contains("つ前")) {
                if (postSource.contains("該当するデータがありません")) {
                    postSource = "該当するデータがありません";
                } else {
                    postSource = "始点のためバス位置の表示はありません";
                }
            }

            createListView(postSource);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (http != null)
                    http.disconnect();
                if (inputStream != null)
                    inputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //  HTMLソースをアレンジする
    String ArrangeSource(String text) {
        if (text.contains("つ前") || text.contains(getOnBusStopName + " &nbsp;</td>")) {
            text = text.trim().replace("&nbsp;</td>", "");
            return ("busstop:" + text + "\n");
        } else if (text.contains("BUS_NOMAL.gif") && !text.contains("example")) {
            text = text
                    .replace("<img src=\"BUS_NOMAL.gif\" align=\"absmiddle\" title=\"", "")
                    .replace("<img src=\"BUS_NOMAL.gif\" align=\"absmiddle\" alt=\"", "")
                    .replace("\">", " ").replace("&nbsp;", " ")
                    .trim();
            return ("bus:" + text + "\n");
        } else if (text.contains("該当するデータがありません"))
            return "該当するデータがありません";
        else
            return "";
    }

    //  ListViewの出力
    void createListView(String text) {
        //  アイコンの設定
        Bitmap busStopImage, kenkoBusImage, kenhokuBusImage, otherBusImage;
        busStopImage = BitmapFactory.decodeResource(getResources(), R.mipmap.bus_stop_location_mode);
        kenkoBusImage = BitmapFactory.decodeResource(getResources(), R.mipmap.kenko_bus);
        kenhokuBusImage = BitmapFactory.decodeResource(getResources(), R.mipmap.kenhoku_bus);
        otherBusImage = BitmapFactory.decodeResource(getResources(), R.mipmap.other_bus);

        //  データの作成
        List<CustomData> objects = new ArrayList<CustomData>();
        String[] items = text.split("\n");

        for (int i = 0; i < items.length; i++) {
            CustomData item = new CustomData();
            if (items[i].contains("busstop:")) {
                item.setSpace("");
                item.setImageData(busStopImage);
                item.setTextData(items[i].replace("busstop:", ""));
                item.setColor(Color.rgb(26, 35, 126));
            } else if (items[i].contains("bus:")) {
                item.setSpace("\t\t\t");
                item.setColor(Color.BLACK);

                items[i] = items[i].replace("１", "1").replace("２", "2").replace("３", "3").replace("４", "4")
                        .replace("５", "5").replace("６", "6").replace("７", "7").replace("８", "8")
                        .replace("９", "9").replace("０", "0").replace("（", "(").replace("）", ")");

                if (items[i].contains("[県北]")) {
                    item.setImageData(kenhokuBusImage);
                    item.setTextData("\t" + items[i].replace("bus:", "").replace("[県北]", ""));
                } else if (items[i].contains("[県交]")) {
                    item.setImageData(kenkoBusImage);
                    item.setTextData("\t" + items[i].replace("bus:", "").replace("[県交]", ""));
                } else {
                    item.setImageData(otherBusImage);
                    item.setTextData("\t" + items[i].replace("bus:", ""));
                }
            } else if (items[i].contains("始点のためバス位置の表示はありません")) {
                item.setSpace("");
                item.setTextData("※" + items[i]);
                item.setColor(Color.rgb(183, 28, 28));
            } else if (items[i].contains("該当するデータがありません")) {
                item.setSpace("");
                item.setTextData("※該当するデータがありません");
                item.setColor(Color.rgb(183, 28, 28));
            }

            objects.add(item);
        }

        CustomAdapter customAdapter = new CustomAdapter(getActivity(), 0, objects);

        ListView listView = (ListView) view.findViewById(R.id.listView);
        listView.setAdapter(customAdapter);
    }

}