package com.example.katsumi.myapplication;

import android.os.AsyncTask;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

/**
 * Created by K.S. on 16/01/21.
 */
public class GetBusLine extends AsyncTask<String, Void, String> {
    private MapSelection mapSelection;
    private InputSelection inputSelection;
    private HttpClient httpClient;
    private HttpGet httpGet;
    private String ReceiveStr;

    public GetBusLine(MapSelection mapSelection, String url) {
        this.mapSelection = mapSelection;
        httpClient = new DefaultHttpClient();
        httpGet = new HttpGet(url);
    }

    public GetBusLine(InputSelection inputSelection, String url) {
        this.inputSelection = inputSelection;
        httpClient = new DefaultHttpClient();
        httpGet = new HttpGet(url);
    }

    @Override
    protected String doInBackground(String[] url) {
        return downloadText();
    }

    @Override
    protected void onPostExecute(String text) {
        String connectionData[] = text.split("\n");

        if(mapSelection!=null) {
            mapSelection.connectionBusStopList = connectionData;
        }
        if(inputSelection != null){
            inputSelection.connectionBusStopList = connectionData;
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
}
