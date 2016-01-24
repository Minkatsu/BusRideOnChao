package com.example.katsumi.myapplication;


import java.util.Hashtable;

class BusStopInformation {
    int ID;
    int LinkID;
    String BusStopName;
    String BusStopName_ruby;
    double latitude;
    double longitude;

    BusStopInformation(int ID, int LinkID, String BusStopName, String BusStopName_ruby, double latitude, double longitude){
        this.ID = ID;
        this.LinkID = LinkID;
        this.BusStopName = BusStopName;
        this.BusStopName_ruby = BusStopName_ruby;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}

public class BusStopInformationList {

    BusStopInformation[] data;

    BusStopInformationList(MainActivity mainActivity){
        new GetBusData(this, mainActivity,
                "https://raw.githubusercontent.com/Minkatsu/BusRideOnCiaoData/master/BusStopData.txt").execute();
    }

    public Integer BusStopNameToID(String BusStopName){

        Hashtable<String, Integer> BusStopNameToID = new Hashtable<>();

        for (BusStopInformation aData : data) {
            BusStopNameToID.put(aData.BusStopName, aData.ID);
        }

        return BusStopNameToID.get(BusStopName);
    }

    public Integer IDToLinkID(int ID){
        Hashtable<Integer, Integer> IDToLinkID = new Hashtable<>();

        for (BusStopInformation aData : data) {
            IDToLinkID.put(aData.ID, aData.LinkID);
        }

        return IDToLinkID.get(ID);
    }
}
