package com.example.katsumi.myapplication;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 *
 * Created by g031k065 on 2015/01/29.
 *
 */
public class DisplayTimetable extends Fragment {

    View view;
    String TITLE;

    MainActivity mainActivity;

    String getOnBusStopName, getOffBusStopName;
    int getOnBusStopLinkID, getOffBusStopLinkID;

    DisplayTimetable(MainActivity mainActivity){
        this.mainActivity = mainActivity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.display_timetable_window, container, false);

        TITLE = getActivity().getTitle().toString();

        getActivity().setTitle("Timetable");

        setUp();

        //  リセットボタンの設定
        getActivity().findViewById(R.id.reset).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setUp();
            }
        });

        // 入れ替えボタンの設定
        getActivity().findViewById(R.id.swap).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.getOnBusStopText.setText(getOffBusStopName);
                mainActivity.getOffBusStopText.setText(getOnBusStopName);

                ImageView dots = (ImageView) getActivity().findViewById(R.id.allow);
                AnimationSet set = new AnimationSet(true);

                RotateAnimation rotate = new RotateAnimation(0, 180, dots.getWidth()/2, dots.getHeight()/2);
                set.addAnimation(rotate);
                set.setFillAfter(true);

                set.setDuration(300); // 3000msかけてアニメーションする
                dots.startAnimation(set); // アニメーション適用

                setUp();
            }
        });

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

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
    }

    //  HTMLソースの取得
    private void getHTMLSource() {
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());

        HttpURLConnection http = null;
        InputStream inputStream = null;
        try {
            // URLにHTTP接続
            URL url = new URL("http://gps.iwatebus.or.jp/bls/pc/" +
                    "jikoku_jk.jsp?jjg=1&jtr=" + getOnBusStopLinkID + "&kjg=1&ktr=" + getOffBusStopLinkID + "&ty=1");
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
            }
        }
    }

    //  HTMLソースのアレンジ
    String ArrangeSource(String text) {
        if (text.contains("hour")) {
            text = text.trim()
                    .replace("<td width=\"30\" class=\"dya-hour\"> <div align=\"center\"><strong>", "")
                    .replace("</strong></div></td>", "");
            return (text + " :");
        } else if (text.contains("min")) {
            if (text.contains("even")) {
                text = text.trim()
                        .replace("<td class=\"dya-min-even\">&nbsp;", "")
                        .replace("&nbsp;</td>", "");
            } else if (text.contains("odd")) {
                text = text.trim()
                        .replace("<td class=\"dya-min-odd\">&nbsp;", "")
                        .replace("&nbsp;</td>", "");
            } else {
                text = "";
            }
            return text + "\n\n";
        } else {
            return "";
        }
    }

    //  ListViewの出力
    public void createListView(String text) {

        int PARENT_DATA_NUM = text.split("\n\n").length;
        int CHILD_DATA_NUM;

        final String KEY1 = "PARENT";
        final String KEY2 = "CHILD";

        // 設定する文字列のリスト
        List<Map<String, String>> parentList = new ArrayList<Map<String, String>>();
        List<List<Map<String, String>>> childList = new ArrayList<List<Map<String, String>>>();

        boolean flg[] = new boolean[PARENT_DATA_NUM];

        // リストに文字列を設定していく
        for (int i = 0; i < PARENT_DATA_NUM; i++) {
            // 親要素の追加
            Map<String, String> parentData = new HashMap<String, String>();

            parentList.add(parentData);
            parentData.put(KEY1, text.split("\n\n")[i].split(" ")[0] + " 時");

            List<Map<String, String>> childData = new ArrayList<Map<String, String>>();

            CHILD_DATA_NUM = text.split("\n\n")[i].split(" ").length;
            for (int j = 2; j < CHILD_DATA_NUM; j++) {
                // 子要素の追加
                Map<String, String> childMap = new HashMap<String, String>();

                childData.add(childMap);

                childMap.put(KEY2, text.split("\n\n")[i].split(" ")[0]
                        + text.split("\n\n")[i].split(" ")[1] + text.split("\n\n")[i].split(" ")[j]);
            }

            if (CHILD_DATA_NUM <= 2) {
                flg[i] = true;
            } else {
                flg[i] = false;
            }

            childList.add(childData);
        }

        int j = 0;
        for (int i = 0; i < PARENT_DATA_NUM; i++) {
            if (flg[i]) {
                Log.d("remove" + i, "true");
                parentList.remove(i - j);
                childList.remove(i - j);
                j++;
            }
        }

        if (parentList.size() == 0 && childList.size() == 0) {
            Map<String, String> parentData = new HashMap<String, String>();
            parentList.add(parentData);
            parentData.put(KEY1, "※該当するデータがありません");
            List<Map<String, String>> childData = new ArrayList<Map<String, String>>();
            //Map<String, String> childMap = new HashMap<String, String>();
            childList.add(childData);
        }

        // ExpandableListAdapter の作成
        ExpandableListAdapter adapter = new SimpleExpandableListAdapter(
                getActivity(),
                parentList, R.layout.simple_expandable_list_item,
                new String[]{KEY1}, new int[]{R.id.text1},
                childList, R.layout.simple_expandable_list_item,
                new String[]{KEY2}, new int[]{R.id.text1}) {

            @Override
            public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
                final View itemRenderer = super.getChildView(groupPosition, childPosition, isLastChild, convertView, parent);
                final TextView tv = (TextView) itemRenderer.findViewById(R.id.text1);

                tv.setTextColor(Color.rgb(183, 28, 28)); // 子リストのタイトルは赤に設定
                return itemRenderer;
            }

            @Override
            public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
                final View itemRenderer = super.getGroupView(groupPosition, isExpanded, convertView, parent);
                final TextView tv = (TextView) itemRenderer.findViewById(R.id.text1);

                if (tv.getText().toString().contains("該当するデータがありません")) {
                    tv.setTextColor(Color.rgb(183, 28, 28));
                } else {
                    tv.setTextColor(Color.rgb(26, 35, 126)); // 親リストのタイトルは青に設定
                }
                return itemRenderer;
            }
        };

        ExpandableListView listView = (ExpandableListView) view.findViewById(R.id.expandableListView);
        listView.setAdapter(adapter);
    }
}