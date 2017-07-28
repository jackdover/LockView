package com.dover.lockview.lock;

/**
 * Created by d on 2017/7/27.
 * 自定义样式参数类
 */
public class LockViewConfig {   //以下所有参数皆可重新定义

    private int normalColor = -1;   // LockView无手指触摸的状态下的颜色
    private int fingerOnColor = -1; // LockView手指触摸的状态下内圆和外圆的颜色
    private int errorColor = -1;    // LockView手指抬起后, 错误状态下内圆和外圆的颜色


    private float radiusRate = -1;  // 内外圆半径比例 (内圆半径 =  = mRadiusOuter * mRadiusRate)
    private float arrowRate = -1;   // 箭头大小比例（小三角底边的长度 =外圆半径 mRadiusOuter * mArrowRate ）
    private int strokeWidth = -1;   // 按下状态时外圆描边的画笔的宽度


    public int getNormalColor() {
        return normalColor;
    }

    public void setNormalColor(int normalColor) {
        this.normalColor = normalColor;
    }

    public int getFingerOnColor() {
        return fingerOnColor;
    }

    public void setFingerOnColor(int fingerOnColor) {
        this.fingerOnColor = fingerOnColor;
    }

    public int getErrorColor() {
        return errorColor;
    }

    public void setErrorColor(int errorColor) {
        this.errorColor = errorColor;
    }

    public float getRadiusRate() {
        return radiusRate;
    }

    public void setRadiusRate(float radiusRate) {
        this.radiusRate = radiusRate;
    }

    public float getArrowRate() {
        return arrowRate;
    }

    public void setArrowRate(float arrowRate) {
        this.arrowRate = arrowRate;
    }

    public int getStrokeWidth() {
        return strokeWidth;
    }

    public void setStrokeWidth(int strokeWidth) {
        this.strokeWidth = strokeWidth;
    }

}
