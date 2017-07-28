package com.dover.lockview;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.dover.lockview.indicator.LockIndicatorView;
import com.dover.lockview.lock.LockViewConfig;
import com.dover.lockview.lock.LockViewGroup;

public class MainActivity extends AppCompatActivity {

    private LockIndicatorView mLockIndicator;
    private LockViewGroup mLockViewGroup;
    private TextView mTvTips;
    private LockViewConfig mConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLockIndicator = (LockIndicatorView) findViewById(R.id.indicator);

        mTvTips = (TextView) findViewById(R.id.tv_tips);

        mLockViewGroup = (LockViewGroup) findViewById(R.id.lockgroup);

        initData();
    }

    private void initData() {

        /**
         * 可以设置自定义样式
         */
        /*
        mConfig = new LockViewConfig();
        //设置颜色
        mConfig.setNormalColor(0xFF0F8EE8);
        mConfig.setFingerOnColor(0xFF2177C7);
        mConfig.setErrorColor(0xFFFF0000);
        // LockView
        mConfig.setRadiusRate(0.3f);
        mConfig.setArrowRate(0.25f);
        mConfig.setStrokeWidth(6);
        // 设置自定义样式
        mLockViewGroup.setConfig(mConfig);
        */

        /**
         * 如果是 验证密码, 需要设置原始密码
         */
//        mLockViewGroup.setAnswer(new int[]{1, 2, 3});

        // 设置尝试次数
//        mLockViewGroup.setMaxTryTimes(5);

        mLockViewGroup.setOnLockListener(new LockViewGroup.OnLockListener() {
            @Override
            public void onLockSelected(int id) {
//                mTvTips.setText("当前连接的点是:" + id);
            }

            @Override
            public void onLess4Points() {
                mLockViewGroup.clear2ResetDelay(1200L); //清除错误

                mTvTips.setTextColor(Color.RED);
                mTvTips.setText("至少连接4个点 , 请重新输入");
            }

            @Override
            public void onSaveFirstAnswer(int[] answer) {
                mTvTips.setTextColor(Color.GRAY);
                mTvTips.setText("再次绘制 , 确认解锁图案");
                // 设置给指示器view
                mLockIndicator.setAnswer(answer);
            }

            @Override
            public void onSucessed() {
                mTvTips.setTextColor(Color.BLACK);
                mTvTips.setText("验证成功");
                // doNext();  //执行成功后的操作
            }

            @Override
            public void onFailed(int mTryTimes) {
                mLockViewGroup.clear2ResetDelay(1400L); //清除错误
                mLockViewGroup.setHapticFeedbackEnabled(true); //手机振动
                mTvTips.setTextColor(Color.RED);
                mTvTips.setText("与上一次绘制不一致 , 请重新绘制");

                Toast.makeText(MainActivity.this, "剩余尝试机会: " + mTryTimes + " 次", Toast.LENGTH_SHORT).show();
                if (mTryTimes < 0) {
                    // here do something about limit input, for example:
                    Toast.makeText(MainActivity.this, "设置失败", Toast.LENGTH_SHORT).show();
                    finish();
                }

                // 左右移动动画
                Animation shakeAnimation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.shake);
                mTvTips.startAnimation(shakeAnimation);

            }

            // 验证密码时, 设置的原始密码少于4位的错误提示
            @Override
            public void onSetAnswerLessError() {
                mTvTips.setTextColor(Color.RED);
                mTvTips.setText("验证密码不能少于4位");
            }
        });

    }


}
