package com.example.katsumi.myapplication;

import android.graphics.Bitmap;

//  ListViewの情報の格納・取得
public class CustomData {
    private Bitmap _imageData;
    private String _textData;
    private String _space;
    private int _color;

    public void setImageData(Bitmap image) {
        _imageData = image;
    }

    public Bitmap getImageData() {
        return _imageData;
    }

    public void setTextData(String text) {
        _textData = text;
    }

    public String getTextData() {
        return _textData;
    }

    public void setSpace(String space) {
        _space = space;
    }

    public String getSpace() {
        return _space;
    }

    public void setColor(int color) {
        _color = color;
    }

    public int getColor() {
        return _color;
    }
}
