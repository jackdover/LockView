package com.dover.lockview.lock;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.Log;
import android.view.View;

/**
 * Created by d on 2017/7/25.
 * 单个view
 */
public class LockView extends View {
    //需求: 根据手势的状态, 绘制各个状态的 view (内外圆)

    private static final String TAG = "GestureLockView";

    // GestureLockView的三种状态
    enum Status {
        STATUS_NORMAL,  //默认状态: 无触摸
        STATUS_ON,      //选中状态: 手指按下+移动
        STATUS_ERROR;   //错误状态: 手指抬起
    }

    // GestureLockView的当前状态
    private Status mCurrentStatus = Status.STATUS_NORMAL; //初始默认状态

    // 宽度, 高度
    private int mWidth, mHeight;
    // 圆心坐标
    private int mCenterX, mCenterY;

    // 外圆半径
    private float mRadiusOuter;
    // 内外圆半径比例
    private float mRadiusRate = 0.25f;
    // 内圆半径 =  = mRadiusOuter * mRadiusRate
    private float mRadiusInner;

    // 画笔
    private Paint mPaint;
    // 画笔的宽度
    private int mStrokeWidth = 4;


    // 箭头（小三角底边的长度 =外圆半径 mRadiusOuter * mArrowRate ）
    private float mArrowRate = 0.25f;
    private int mArrowDegree = -1;
    // 箭头路径
    private Path mArrowPath;


    /**
     * 三个状态的颜色，可由用户自定义，初始化时 可由 LockViewConfig 设置后传入
     */
    private int mNormalColor = 0xFF0F8EE8;
    private int mFingerOnColor = 0xFF2177C7;
    private int mErrorColor = 0xFFFF0000;

    public LockView(Context context, LockViewConfig config) {
        super(context);
        //一些颜色
        if (config != null) {
            if (config.getNormalColor() != -1)
                this.mNormalColor = config.getNormalColor();
            if (config.getFingerOnColor() != -1)
                this.mFingerOnColor = config.getFingerOnColor();
            if (config.getErrorColor() != -1)
                this.mErrorColor = config.getErrorColor();

            if (config.getRadiusRate() != -1)
                this.mRadiusRate = config.getRadiusRate();
            if (config.getArrowRate() != -1)
                this.mArrowRate = config.getArrowRate();
            if (config.getStrokeWidth() != -1)
                this.mStrokeWidth = config.getStrokeWidth();
        }

        //描边宽度 --- //最小值 2
        this.mStrokeWidth = this.mStrokeWidth < 2 ? 2 : this.mStrokeWidth;

        //画笔
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);//抗锯齿
        //路径
        mArrowPath = new Path();//箭头路径
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // 获取控件大小
        mWidth = MeasureSpec.getSize(widthMeasureSpec);
        mHeight = MeasureSpec.getSize(heightMeasureSpec);

        // 取长和宽中的小值
        mWidth = mWidth < mHeight ? mWidth : mHeight;
        mRadiusOuter = mCenterX = mCenterY = mWidth / 2;
        mRadiusOuter -= mStrokeWidth / 2; //drawCircle时, 描边向两侧扩展, 所以半径需要减去描边的一半
        mRadiusInner = mRadiusOuter * mRadiusRate;


        //绘制三角形，初始时是个默认箭头朝上的一个等腰三角形，用户绘制结束后，根据由两个GestureLockView决定需要旋转多少度
        float mArrowLength = mRadiusOuter * mArrowRate;
        float mArrowHeight = mArrowLength * 0.8f;
        //空白距离的一半
        float mArrowDistance = (mWidth / 2 - mRadiusInner - mArrowHeight) / 2;

        mArrowPath.moveTo(mWidth / 2, mArrowDistance);
        mArrowPath.lineTo(mWidth / 2 - mArrowHeight, mArrowDistance + mArrowLength);
        mArrowPath.lineTo(mWidth / 2 + mArrowHeight, mArrowDistance + mArrowLength);
        mArrowPath.close();
        mArrowPath.setFillType(Path.FillType.WINDING);

        Log.d(TAG, "mROuter=" + mRadiusOuter + ",mRInner=" + mRadiusInner + ",mArrowLength=" + mArrowLength);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        switch (mCurrentStatus) {
            case STATUS_NORMAL: //默认状态(无内圆, 外圆描边较细)

                mPaint.setColor(mNormalColor);
                // 绘制外圆
                mPaint.setStyle(Paint.Style.STROKE);
                mPaint.setStrokeWidth((int) (mStrokeWidth / 2 + 0.5f));
                canvas.drawCircle(mCenterX, mCenterY, mRadiusOuter, mPaint);

                break;
            case STATUS_ON:

                mPaint.setColor(mFingerOnColor);
                // 绘制外圆
                mPaint.setStyle(Paint.Style.STROKE);
                mPaint.setStrokeWidth(mStrokeWidth);
                canvas.drawCircle(mCenterX, mCenterY, mRadiusOuter, mPaint);
                // 绘制内圆
                mPaint.setStyle(Paint.Style.FILL);
                canvas.drawCircle(mCenterX, mCenterY, mRadiusInner, mPaint);

                // 绘制箭头
                drawArrow(canvas);

                break;
            case STATUS_ERROR:

                mPaint.setColor(mErrorColor);
                // 绘制外圆
                mPaint.setStyle(Paint.Style.STROKE);
                mPaint.setStrokeWidth(mStrokeWidth);
                canvas.drawCircle(mCenterX, mCenterY, mRadiusOuter, mPaint);
                // 绘制内圆
                mPaint.setStyle(Paint.Style.FILL);
                canvas.drawCircle(mCenterX, mCenterY, mRadiusInner, mPaint);

                // 绘制箭头
                drawArrow(canvas);

                break;
        }
    }

    /**
     * 绘制箭头
     *
     * @param canvas
     */
    private void drawArrow(Canvas canvas) {
        if (mArrowDegree != -1) {
            mPaint.setStyle(Paint.Style.FILL);

            canvas.save();
            canvas.rotate(mArrowDegree, mCenterX, mCenterY);
            canvas.drawPath(mArrowPath, mPaint);

            canvas.restore();
        }

    }

    /**
     * 设置当前模式并重绘界面
     *
     * @param mode
     */
    public void setStatus(Status mode) {
        this.mCurrentStatus = mode;
        invalidate();
    }

    public Status getStatus() {
        return mCurrentStatus;
    }

    public void setArrowDegree(int degree) {
        this.mArrowDegree = degree;
        invalidate();
    }


}
