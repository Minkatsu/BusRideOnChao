package com.example.katsumi.myapplication;

import android.graphics.Point;
import android.graphics.PointF;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public class ArcTranslateAnimation extends Animation {

    private int mCenterXType;
    private int mCenterYType;
    private float mCenterXValue;
    private float mCenterYValue;

    private float mRadius;
    private Point mStartPoint;

    private float mDeltaRad;
    private float mStartRad;
    private float mEndRad;

    /**
     * 円弧上に沿うように移動させるアニメーション
     * @param startDegrees  開始角度
     * @param endDegrees    終了角度
     * @param centerXType   中心点のX座標のタイプ
     * @param centerXValue  中心点のX座標を表す値
     * @param centerYType   中心点のY座標のタイプ
     * @param centerYValue  中心点のY座標を表す値
     */
    public ArcTranslateAnimation(int startDegrees, int endDegrees,
                                 int centerXType, float centerXValue, int centerYType, float centerYValue) {
        this.mStartRad = (float)(startDegrees * Math.PI / 180.0f);
        this.mEndRad   = (float)(endDegrees   * Math.PI / 180.0f);
        this.mCenterXType  = centerXType;
        this.mCenterXValue = centerXValue;
        this.mCenterYType  = centerYType;
        this.mCenterYValue = centerYValue;
    }

    /**
     * 現在の座標を取得する
     * @param interpolatedTime
     * @return
     */
    private PointF getArcPoint(float interpolatedTime) {
        float rad = mStartRad + (mEndRad - mStartRad) * interpolatedTime + mDeltaRad;
        float dx = (float)(Math.cos(rad) * mRadius);
        float dy = (float)(Math.sin(rad) * mRadius);
        return new PointF(dx - mStartPoint.x, dy - mStartPoint.y);
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        PointF p = getArcPoint(interpolatedTime);
        t.getMatrix().postTranslate(p.x, p.y);
    }

    @Override
    public void initialize(int width, int height, int parentWidth,
                           int parentHeight) {
        super.initialize(width, height, parentWidth, parentHeight);

        float fromX   = resolveSize(Animation.ABSOLUTE, 0, width, parentWidth);
        float fromY   = resolveSize(Animation.ABSOLUTE, 0, height, parentHeight);
        float centerX = resolveSize(mCenterXType, mCenterXValue, width, parentWidth);
        float centerY = resolveSize(mCenterYType, mCenterYValue, height, parentHeight);

        mDeltaRad = (float)Math.atan2(fromY - centerY, fromX - centerX);
        mRadius = (float)Math.sqrt(Math.pow(fromX - centerX, 2) + Math.pow(fromY - centerY, 2));
        mStartPoint = new Point((int)(fromX - centerX), (int)(fromY - centerY));
    }
}