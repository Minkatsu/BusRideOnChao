package com.example.katsumi.myapplication;

import android.os.AsyncTask;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

/**
 * Created by K.S. on 16/01/24.
 */
public class GetBusData extends AsyncTask<String, Void, String> {
    private HttpClient httpClient;
    private HttpGet httpGet;
    private String ReceiveStr;
    private MainActivity mainActivity;
    private BusStopInformationList busStopInformationList;

    public GetBusData(BusStopInformationList busStopInformationList, MainActivity mainActivity, String url) {
        this.busStopInformationList = busStopInformationList;
        this.mainActivity = mainActivity;
        httpClient = new DefaultHttpClient();
        httpGet = new HttpGet(url);
    }

    @Override
    protected String doInBackground(String[] url) {
        return downloadText();
    }

    @Override
    protected void onPostExecute(String text) {
        try {
            setUpMainActivity(text);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String downloadText() {
        try {
            HttpResponse response = httpClient.execute(httpGet);
            ReceiveStr = EntityUtils.toString(response.getEntity(), "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ReceiveStr;
    }

    public void setUpMainActivity(String text){
        String BusStopData[] = text.split("\n");

        busStopInformationList.data = new BusStopInformation[BusStopData.length];

        for (int i = 0; i < BusStopData.length; i++) {
            String[] item = BusStopData[i].split(" ");

            busStopInformationList.data[i] = new BusStopInformation(
                    Integer.parseInt(item[0]),
                    Integer.parseInt(item[1]),
                    item[2],
                    item[3],
                    Double.parseDouble(item[4]),
                    Double.parseDouble(item[5]));
        }

        mainActivity.busStopArrayList = new String[busStopInformationList.data.length];
        for (int i = 0; i < mainActivity.busStopArrayList.length; i++) {
            mainActivity.busStopArrayList[i] = busStopInformationList.data[i].BusStopName;
        }

        mainActivity.mInputSelection = new InputSelection(mainActivity);
        mainActivity.mMapSelection = new MapSelection(mainActivity);
        mainActivity.mDisplayTimetable = new DisplayTimetable(mainActivity);
        mainActivity.mDisplayLocation = new DisplayLocation(mainActivity);

        //  クリックリスナーの登録
        mainActivity.setClickListener();

        //  入力選択画面の表示
        mainActivity.changeToInputMode();
    }
}
