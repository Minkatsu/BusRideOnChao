package com.example.katsumi.myapplication;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
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

    BusStopInformationList busStopInformationList = new BusStopInformationList();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.display_location_window, container, false);

        TITLE = getActivity().getTitle().toString();
        getActivity().setTitle("位置情報:" + TITLE.split(":")[1]);

        setUp();

        return view;
    }

    //  準備
    private void setUp() {
        getOnBusStopName = TITLE.split(":")[1].split("→")[0];
        getOffBusStopName = TITLE.split(":")[1].split("→")[1];

        getLinkID();

        getHTMLSource();
    }

    //  LinkIDの取得
    private void getLinkID() {
        getOnBusStopLinkID = busStopInformationList.BusStopNameToID(getOnBusStopName);
        getOnBusStopLinkID = busStopInformationList.IDToLinkID(getOnBusStopLinkID);

        getOffBusStopLinkID = busStopInformationList.BusStopNameToID(getOffBusStopName);
        getOffBusStopLinkID = busStopInformationList.IDToLinkID(getOffBusStopLinkID);

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
            String preSource = new String(), postSource = new String();
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

    //  ListViewの情報の格納・取得
    public class CustomData {
        private Bitmap imageData_;
        private String textData_;
        private String space_;
        private int color_;

        public void setImageData(Bitmap image) {
            imageData_ = image;
        }

        public Bitmap getImageData() {
            return imageData_;
        }

        public void setTextData(String text) {
            textData_ = text;
        }

        public String getTextData() {
            return textData_;
        }

        public void setSpace(String space) {
            space_ = space;
        }

        public String getSpace() {
            return space_;
        }

        public void setColor(int color) {
            color_ = color;
        }

        public int getColor() {
            return color_;
        }
    }

    //  ListViewのWidgetの設定
    public class CustomAdapter extends ArrayAdapter<CustomData> {
        private LayoutInflater layoutInflater_;

        public CustomAdapter(Context context, int textViewResourceId, List<CustomData> objects) {
            super(context, textViewResourceId, objects);
            layoutInflater_ = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // 特定の行(position)のデータを得る
            CustomData item = (CustomData) getItem(position);

            // convertViewは使い回しされている可能性があるのでnullの時だけ新しく作る
            if (null == convertView) {
                convertView = layoutInflater_.inflate(R.layout.custom_list_layout, null);
            }

            // CustomDataのデータをViewの各Widgetにセットする
            TextView space;
            space = (TextView) convertView.findViewById(R.id.textView5);
            space.setText(item.getSpace());

            ImageView imageView;
            imageView = (ImageView) convertView.findViewById(R.id.image);
            imageView.setImageBitmap(item.getImageData());

            TextView textView;
            textView = (TextView) convertView.findViewById(R.id.textView6);
            textView.setText(item.getTextData());
            textView.setTextColor(item.getColor());

            return convertView;
        }
    }

    //  ListViewの出力
    void createListView(String text) {
        //  アイコンの設定
        Bitmap busStopImage, kenkoBusImage, kenhokuBusImage, otherBusImage;
        busStopImage = BitmapFactory.decodeResource(getResources(), R.drawable.bus_stop_location_mode);
        kenkoBusImage = BitmapFactory.decodeResource(getResources(), R.drawable.kenko_bus);
        kenhokuBusImage = BitmapFactory.decodeResource(getResources(), R.drawable.kenhoku_bus);
        otherBusImage = BitmapFactory.decodeResource(getResources(), R.drawable.other_bus);

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