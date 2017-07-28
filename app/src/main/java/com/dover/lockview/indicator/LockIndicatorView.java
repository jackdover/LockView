package com.dover.lockview.indicator;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.dover.lockview.R;

import java.util.Arrays;

/**
 * Created by d on 2017/7/25.
 * 正上方的提示区域，用一个自定义view(LockIndicator)来绘制9个提示小图标；
 */
public class LockIndicatorView extends View {
    //需求: 第一次绘制过程中不显示, 第一次绘制完成后根据保存的绘制 path 来绘制提示小图标

    private static final String TAG = "LockIndicatorView";

    private String lockPassStr; // 手势密码


    private int numRow;         // 行
    private int numCol;         // 列

    private int tipWidth;       // 行宽
    private int tipHeight;      // 列高
    private int spaceRow;       // 行间距
    private int spaceCol;       // 列间距

    private Drawable tipNoraml = null;
    private Drawable tipPressed = null;


    public LockIndicatorView(Context context) {
        super(context);
        init(context);  //初始化
    }

    public LockIndicatorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);   //初始化
    }

    public LockIndicatorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);   //初始化
    }

    //无论哪一种创建方式, 都会执行此初始化方法
    private void init(Context context) {

        numRow = 3;         // 行
        numCol = 3;         // 列

        //预初始化---基本不用(需要根据图片大小重新初始化)
        tipWidth = 40;      // 行宽
        tipHeight = 40;     // 列高
        spaceRow = 5;       // 行间距
        spaceCol = 5;       // 列间距


        tipNoraml = getResources().getDrawable(R.drawable.tip_normal);
        tipPressed = getResources().getDrawable(R.drawable.tip_pressed);

        if (tipNoraml != null && tipPressed != null) {
            this.tipWidth = tipPressed.getIntrinsicWidth();  //返回dp为单位的宽
            this.tipHeight = tipPressed.getIntrinsicHeight();   //返回值单位为 dp
            this.spaceRow = (tipWidth * 2 / 5);    //间距
            this.spaceCol = (tipHeight * 2 / 5);   //间距
            tipPressed.setBounds(0, 0, tipWidth, tipHeight);
            tipNoraml.setBounds(0, 0, tipWidth, tipHeight);
        }

        Log.d(TAG, "W/H=" + tipWidth + "/" + tipHeight + ",space=" + spaceRow + "/" + spaceCol);
    }


    @Override
    protected void onDraw(Canvas canvas) {

        if ((tipPressed == null) || (tipNoraml == null)) {
            return;
        }
        // 绘制3*3的图标
        for (int i = 0; i < numRow; i++) {
            for (int j = 0; j < numCol; j++) {

                int dx = j * tipWidth + j * this.spaceRow;      //横向 dx
                int dy = i * tipHeight + i * this.spaceCol;     //纵向 dy

//canvas.save();和canvas.restore();是两个相互匹配出现的，作用是用来保存画布的状态和取出保存的状态的
                canvas.save();//保存画布的状态

                canvas.translate(dx, dy);

                String curNum = String.valueOf(numCol * i + (j + 1));//数字1-9
                if (!TextUtils.isEmpty(lockPassStr)) {
                    if (lockPassStr.indexOf(curNum) == -1) {
                        // 未选中
                        tipNoraml.draw(canvas);
                    } else {    // lockPassStr.contains(curNum)
                        // 被选中
                        tipPressed.draw(canvas);
                    }
                } else {
                    // 重置状态
                    tipNoraml.draw(canvas);
                }

                canvas.restore();//取出画布保存的状态
            }
        }
    }

    @Override
    protected void onMeasure(int paramInt1, int paramInt2) {
        if (tipPressed != null)
            setMeasuredDimension(
                    numCol * tipHeight + this.spaceCol * (-1 + numCol),
                    numRow * tipWidth + this.spaceRow * (-1 + numRow));
    }

    /**
     * 根据手势密码, 请求重新绘制
     *
     * @param answer 手势密码数字序列   1-9
     */
    public void setAnswer(int[] answer) {
        lockPassStr = Arrays.toString(answer);
        Log.d(TAG, "setAnswer: lockPassStr=" + lockPassStr);
        invalidate();
    }


}
