package com.dover.lockview.lock;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by d on 2017/7/25.
 * 多个view的排列
 */
public class LockViewGroup extends RelativeLayout {
    //需求: 绘制时:实时显示箭头, 绘制完成后隐藏;
    // 如果绘制完成后错误, 就用红色显示轨迹
    /**
     * 修改界面需要的自定义参数:
     * mCount 行列个数
     * mMarginRate  间距与大小的比例关系
     * mStrokeWidthRate  画笔宽度与大小的比例关系
     * 连接线的颜色
     * mFingerOnColor-选中状态, mErrorColor-错误状态
     */

    private static final String TAG = "LockViewGroup";

    private Paint mPathPaint;   //画笔
    private Path mPath;     //路径

    private int mCount = 3;   // 每个边上的 LockView 的个数

    // 保存所有的 LockView
    private LockView[] mLockViews;  //总个数 = mCount * mCount
    // 保存用户选中的 LockView 的id
    private List<Integer> mChoose = new ArrayList<Integer>();

    // 宽度, 高度  (LockViewGroup 的大小)
    private int mWidth, mHeight;

    // 宽度, 高度  (子view--LockView 的大小)
    private int mLockViewWidth;
    // 间距 (子view--LockView 的间距)
    private int mLockViewMargin;
    // 间距与宽度的比例 (mLockViewMargin = mLockViewWidth * mLockMarginRate)
    private float mMarginRate = 0.6f;

    // 画笔宽度 = mLockViewWidth * mStrokeWidthRate
    private float mStrokeWidth;
    // 画笔宽度与LockView大小的比例
    private float mStrokeWidthRate = 0.02f;


    // 连接线的颜色
    private int mFingerOnColor = 0xFF2177C7;// 选中状态
    private int mErrorColor = 0xFFFF0000;// 错误状态


    // 指引线的开始位置 x
    private int mLastPathX;
    // 指引线的开始位置 y
    private int mLastPathY;
    // 指引线的结束位置
    private Point mTmpTarget = new Point();


    //第一次绘制时 存储答案
    private int[] mFirstAnswer;

    // 最大尝试次数
    private int mTryTimes = 4;

    // 回调接口
    private OnLockListener mLockListener;

    // 自定义参数设置
    private LockViewConfig mConfig;

    // 构造
    public LockViewGroup(Context context) {
        super(context);
        init(context);   //初始化
    }

    public LockViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);   //初始化
    }

    public LockViewGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // init(context, attrs, defStyleAttr); //初始化自定义属性 (可以自己扩展)
        init(context);   //初始化
    }

    /**
     * 1. 初始化
     *
     * @param context
     */
    private void init(Context context) {
        // 1.1 初始化画笔
        mPathPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPathPaint.setStyle(Paint.Style.STROKE);
        // 设置 画笔宽度 --- 后面根据 LockView 大小和比例计算设置
        // mPaint.setStrokeWidth(mStrokeWidth);
        // 设置画笔颜色 --- 后面根据 LockView 状态设置不同颜色
        // mPaint.setColor(Color.parseColor("#aaffffff"));
        mPathPaint.setStrokeCap(Paint.Cap.ROUND);
        mPathPaint.setStrokeJoin(Paint.Join.ROUND);

        // 1.2 连线路径
        mPath = new Path();

    }

    // 2. 测量 ViewGroup大小, 并根据其计算 子view大小和间距
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // 2.1 测量viewgroup 大小
        mWidth = MeasureSpec.getSize(widthMeasureSpec);
        mHeight = MeasureSpec.getSize(heightMeasureSpec);
        // 2.2 获取最小值
        mWidth = mHeight = mWidth < mHeight ? mWidth : mHeight;
        // setMeasuredDimension(mWidth, mHeight);

        // 3. 初始化 mLockViews
        if (mLockViews == null) {
            initmLockViews();
        }

        Log.e(TAG, "onMeasure---mWidth = " + mWidth);
    }

    // 3. 初始化 mLockViews 及其子view
    private void initmLockViews() {
        // 3.0 初始化 mLockViews 及个数
        mLockViews = new LockView[mCount * mCount];


        //////******------ 根据宽高,计算 子view大小和间距 ------******//////

        // 3.1 计算每个 LockView 的宽度
        mLockViewWidth = (int) (mWidth / (mCount + mMarginRate * (mCount + 1)));
        // 3.2 计算每个 LockView 的间距
        mLockViewMargin = (int) (mLockViewWidth * mMarginRate);

        // 3.3 设置画笔的宽度（不喜欢的话，随便设置比例）
        mStrokeWidth = mLockViewWidth * mStrokeWidthRate;
        mStrokeWidth = mStrokeWidth < 2 ? 2 : mStrokeWidth;//最小值2
        mPathPaint.setStrokeWidth(mStrokeWidth);


        // 3.4 初始化每一个 LockView, 并设置其相应的界面位置
        for (int i = 0; i < mLockViews.length; i++) {
            // 初始化每个 LockView --- //参数2 设置自定义样式的config
            mLockViews[i] = new LockView(getContext(), mConfig);
            // 设置ID
            mLockViews[i].setId(i + 1); // 1到 mCount * mCount

            // 设置参数，主要是定位 LockView 间的位置
            LayoutParams params = new LayoutParams(mLockViewWidth, mLockViewWidth);

            // 不是每行的第一个，则设置位置为前一个的右边
            if (i % mCount != 0) {
                params.addRule(RelativeLayout.RIGHT_OF, mLockViews[i - 1].getId());
            }
            // 从第二行开始，设置为上一行同一位置View的下面
            if (i > mCount - 1) {
                params.addRule(RelativeLayout.BELOW, mLockViews[i - mCount].getId());
            }
            //设置右下左上的边距
            int rightMargin = mLockViewMargin;
            int bottomMargin = mLockViewMargin;
            int leftMagin = 0;
            int topMargin = 0;
            /**
             * 每个View都有右外边距和底外边距 第一行的有上外边距 第一列的有左外边距
             */
            if (i >= 0 && i < mCount)// 第一行
                topMargin = mLockViewMargin;

            if (i % mCount == 0)// 第一列
                leftMagin = mLockViewMargin;

            params.setMargins(leftMagin, topMargin, rightMargin, bottomMargin);

//            mLockViews[i].setMode(LockView.Status.STATUS_NORMAL); //减少重绘
            addView(mLockViews[i], params);
        }

        Log.e(TAG, "mLockViewWidth=" + mLockViewWidth
                + ",mLockViewMargin=" + mLockViewMargin
                + ",mStrokeWidth=" + mStrokeWidth);
    }


    @Override
    public void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        //绘制 LockView 间的连线
        if (mPath != null) {
            canvas.drawPath(mPath, mPathPaint);
        }
        // 绘制指引线
        if (mChoose.size() > 0) {
            if (mLastPathX != 0 && mLastPathY != 0)
                canvas.drawLine(mLastPathX, mLastPathY, mTmpTarget.x,
                        mTmpTarget.y, mPathPaint);
        }

    }

    //重写这个方法，并且在方法里面请求所有的父控件都不要拦截他的事件
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        getParent().requestDisallowInterceptTouchEvent(true);
        return super.dispatchTouchEvent(ev);
    }


    boolean isDrawing;   //down-true, up-false

    /**
     * 4. 处理手势触摸事件
     *
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int eventX = (int) event.getX();    //手指当前坐标 X
        int eventY = (int) event.getY();    //手指当前坐标 Y

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isDrawing = true;    //可以开始绘制
                // 重置
                reset();
                break;
            case MotionEvent.ACTION_MOVE:
                isDrawing = true;    //正在绘制
                // 选中状态连接线画笔颜色
                mPathPaint.setColor(mFingerOnColor);

                // 根据当前坐标 获取范围内的 LockView 对象
                LockView curChild = getLockViewByPoint(eventX, eventY);
                if (curChild != null) {
                    // 选中当前的 LockView
                    doLockViewSelect(curChild);
                }

                // 指引线的终点
                mTmpTarget.x = eventX;
                mTmpTarget.y = eventY;

                break;
            case MotionEvent.ACTION_UP:
                isDrawing = false;   //已经结束绘制

                // 将终点设置位置为起点，即取消最后一条指引线
                mTmpTarget.x = mLastPathX;
                mTmpTarget.y = mLastPathY;

                // 校验和保存结果
                doSaveCheckResult();

                Log.e(TAG, "mTryTimes=" + mTryTimes
                        + ", mChoose=" + mChoose
                        + ", mFirstAnswer=" + Arrays.toString(mFirstAnswer));
                break;
        }
        invalidate();
        return true;
    }

    /**
     * 校验和保存结果
     */
    private void doSaveCheckResult() {
        //1. 先校验4点以上的合法性
        //1.1 不合法: 错误界面 + 4点提示回调
        //1.2 合法:
        //1.2.1 如果是第一次---保存答案 + 还原默认界面 + 执行保存回调
        //1.2.2 如果不是, 去校验
        //1.2.2.1 正确 --- 图标还原默认状态 + 执行正确回调
        //1.2.2.2 错误 --- 图标错误状态 + 提示剩余次数回调


        // 检查有效个数 (至少4个点)
        if (mChoose.size() < 4) {   //1.1 不合法
            // 绘制错误界面
            drawError();
            //少于4点的提示回调
            if (mLockListener != null) {
                mLockListener.onLess4Points();//在回调中一定时间后消失
            }
        } else {    //1.2 合法,去校验

            //1.2.1 如果是第一次 --- 保存答案 + 执行保存回调 + 还原默认界面
            if (mFirstAnswer == null) {
                // 保存答案
                mFirstAnswer = new int[mChoose.size()];
                for (int i = 0; i < mChoose.size(); i++) {
                    mFirstAnswer[i] = mChoose.get(i);
                }
                Log.e(TAG, "mChoose=" + mChoose
                        + ", mFirstAnswer=" + Arrays.toString(mFirstAnswer));
                // 执行保存回调
                if (mLockListener != null) {
                    mLockListener.onSaveFirstAnswer(mFirstAnswer);
                }
                // 界面还原
                reset();
            } else {    //1.2.2 如果不是, 去校验
                if (checkAnswer()) { //.1 正确 --- 还原默认 + 执行成功回调
                    // 界面还原
                    reset();
                    // 成功回调
                    if (mLockListener != null) {
                        mLockListener.onSucessed();
                    }
                } else {    //.2 错误  --- 图标错误状态 + 剩余次数 + 次数提示回调
                    // 绘制错误界面
                    drawError();
                    // 次数减 1
                    this.mTryTimes--;
                    // 执行次数提示回调
                    if (mLockListener != null) {
                        mLockListener.onFailed(this.mTryTimes);//在回调中一定时间后消失
                    }
                }
            }
        }
    }


    /**
     * 当前 LockView 选中状态 --- 选中状态界面
     *
     * @param curChild
     */
    private void doLockViewSelect(LockView curChild) {
        int cId = curChild.getId();     // 1到 mCount * mCount
        if (!mChoose.contains(cId)) {   // 避免重复
            mChoose.add(cId);           // 添加选中的 id

            // 改变当前 LockView 为 选中状态
            curChild.setStatus(LockView.Status.STATUS_ON);

            if (mLockListener != null)  //接口回调
                mLockListener.onLockSelected(cId);

            // 设置指引线的起点
            mLastPathX = curChild.getLeft() / 2 + curChild.getRight() / 2;
            mLastPathY = curChild.getTop() / 2 + curChild.getBottom() / 2;

            if (mChoose.size() == 1) {
                // 当前添加为第一个
                mPath.moveTo(mLastPathX, mLastPathY);
            } else if (mChoose.size() > 1) {
                // 非第一个，将两者使用线连上
                mPath.lineTo(mLastPathX, mLastPathY);

                // 计算和设置箭头旋转角度
                updateArrowDegree(mChoose.get(mChoose.size() - 2), cId);
            }
        }
    }


    /**
     * 错误状态界面
     */
    private void drawError() {

        // 错误状态连接线画笔颜色
        mPathPaint.setColor(mErrorColor);

        // 改变子元素的状态为 Error
        setLockViewError();

        // 计算每个元素中箭头需要旋转的角度
        for (int i = 0; i + 1 < mChoose.size(); i++) {
            int startChildId = mChoose.get(i);
            int nextChildId = mChoose.get(i + 1);
            // 计算和设置箭头旋转角度
            updateArrowDegree(startChildId, nextChildId);
        }
    }


    /**
     * 计算箭头旋转角度, 并设置给前一个 LockView
     *
     * @param startChildId
     * @param nextChildId
     */
    private void updateArrowDegree(int startChildId, int nextChildId) {
        LockView startChild = (LockView) findViewById(startChildId);
        LockView nextChild = (LockView) findViewById(nextChildId);

        int dx = nextChild.getLeft() - startChild.getLeft();
        int dy = nextChild.getTop() - startChild.getTop();
        // 计算角度
        int angle = (int) Math.toDegrees(Math.atan2(dy, dx)) + 90;
        // 设置箭头旋转角度
        startChild.setArrowDegree(angle);
    }


    /**
     * 设置错误路径下的 LockView 为 错误状态
     */
    private void setLockViewError() {
        for (LockView lockView : mLockViews) {
            if (mChoose.contains(lockView.getId())) {
                lockView.setStatus(LockView.Status.STATUS_ERROR);
            }
        }
    }

    /**
     * 做一些必要的重置
     */
    private void reset() {
        mChoose.clear();
        mPath.reset();
        for (LockView lockView : mLockViews) {
            if (lockView.getStatus() != LockView.Status.STATUS_NORMAL)  //减少重绘
                lockView.setStatus(LockView.Status.STATUS_NORMAL);
            lockView.setArrowDegree(-1);
        }
    }

    /**
     * 通过x,y获得落入的 LockView
     * 判断: 圆心到该点的距离 是否小于 圆的半径 (true-在圆内, false-不在)
     *
     * @param x
     * @param y
     * @return
     */
    private LockView getLockViewByPoint(int x, int y) {
        for (LockView lockView : mLockViews) {
            //获取圆的半径
            float radius = (lockView.getRight() - lockView.getLeft()) / 2;
            //获取圆心坐标
            float circleX = radius + lockView.getLeft();
            float circleY = radius + lockView.getTop();
            //计算两点间距离
            float distance = (float) Math.sqrt((circleX - x) * (circleX - x) + (circleY - y) * (circleY - y));

            if (distance <= radius) {   // 该点在view的圆内
                return lockView;
            }
        }
        return null;
    }


    /**
     * 检查用户绘制的手势是否正确
     *
     * @return
     */
    private boolean checkAnswer() {
        if (mFirstAnswer.length != mChoose.size())
            return false;

        for (int i = 0; i < mFirstAnswer.length; i++) {
            if (mFirstAnswer[i] != mChoose.get(i))
                return false;
        }

        return true;
    }


    ///////*********************************************************//////
    ///////******************** 对外部公开调用的方法 *****************//////
    ///////*********************************************************//////


    /**
     * 开启震动
     *
     * @param mHapticFeedbackEnabled
     */
    public void setHapticFeedbackEnabled(boolean mHapticFeedbackEnabled) {
        if (mHapticFeedbackEnabled) {
            performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY,
                    HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING |
                            HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
        }
    }

    /**
     * 外部--- 指定时间去清除绘制的状态 (一般在回调中执行)
     *
     * @param delayTime 延迟执行时间
     */
    public void clear2ResetDelay(long delayTime) {
        // 目前不在绘制状态, 指定时间后消失---重置
        if (delayTime > 0 && !isDrawing) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    // 重置
                    reset();
                    // 重置后需要调起重绘
                    invalidate();
                }
            }, delayTime);
        }
    }

    /**
     * 外部---设置初始答案
     *
     * @param answer
     */
    public void setAnswer(int[] answer) {
        if (answer == null && answer.length >= 4) {
            this.mFirstAnswer = answer;
        } else {
            if (mLockListener != null)
                mLockListener.onSetAnswerLessError();
//            Toast.makeText(getContext(), "验证密码不能少于4位", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 外部---设置最大实验次数
     *
     * @param tryTimes
     */
    public void setMaxTryTimes(int tryTimes) {
        this.mTryTimes = tryTimes;
    }

    /**
     * 外部---设置自定义参数
     *
     * @param config
     */
    public void setConfig(LockViewConfig config) {
        this.mConfig = config;
    }

    /**
     * 外部---设置回调接口
     *
     * @param listener
     */
    public void setOnLockListener(OnLockListener listener) {
        this.mLockListener = listener;
    }


    public interface OnLockListener {

        // 单独选中某一个 Id的回调   id 从 1到 mCount * mCount
        void onLockSelected(int id);

        // 至少4个点的提示
        void onLess4Points();

        // 第一次保存答案的回调 (回传给服务器, 设置给指示器 等等)
        void onSaveFirstAnswer(int[] answer);

        // 校验成功
        void onSucessed();

        // 剩余次数提示回调
        void onFailed(int mTryTimes);

        // 验证密码少于4位
        void onSetAnswerLessError();
    }

}