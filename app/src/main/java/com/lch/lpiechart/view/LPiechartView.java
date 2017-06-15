package com.lch.lpiechart.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.LinearLayout;

import com.lch.lpiechart.R;
import com.lch.lpiechart.bean.ChartData;
import com.lch.lpiechart.utils.ColorUtils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * 饼状图
 *
 * @author luchonghui
 * @date 2017/1/13 17:09
 */
public class LPiechartView extends View implements ValueAnimator.AnimatorUpdateListener {
    private float mWidth, mHeight;
    private Context mContext;
    private int mCircleLR; // 大圆半径
    private RectF mRect, mRectSelected;
    private Paint paintArc;//圆弧画笔
    //圆心位置
    private int centerX, centerY = 0;
    //整个饼图的半径
    private float radius = 0;
    //应该被扣除位置
    private int mSubtractHeight = 0;
    //中间原型半径
    private float mHollowRadius = 0;
    //饼图边距
    private float margin = 0;
    private ChartData[] mChartDatas = {};
    /**
     * 中心文字
     */
    private String mCenterTxt = "";
    private Paint mPaintBg = null;
    private Paint mInnerPaint = null;
    /**
     * 总值
     */
    private double dataSum;
    /**
     * 圆弧颜色
     * 【请自行添加 set方法】
     */
    private String[] arcColors = new String[]{"#1BA586", "#31D860",
            "#72D41B", "#CAD01A", "#F4B939", "#FB8248", "#F75959", "#EB4AA9", "#CF53F3"
            , "#955AF6", "#6760F9", "#608FF9", "#65B2E7", "#32D8DA"};
    //圆周率
    private static final float PI = 3.1415f;

    private static final int PART_ONE = 1;

    private static final int PART_TWO = 2;

    private static final int PART_THREE = 3;

    private static final int PART_FOUR = 4;
    /**
     * 点击的第几段
     */
    private int mTouchIndex;
    /**
     * 旋转角度
     */
    private float mRotateAngle;
    /**
     * 旋转方向 0顺时针 1 逆时针
     */
    private float mRotateWay = 0;
    private ValueAnimator valueAnimator;
    private long DURATION = 600;//动画时长 毫秒
    /**
     * 画布旋转开始角度
     */
    private float mRotate = 0;
    /**
     * 画布旋转结束角度
     */
    private float mEndRotate = 0;
    /**
     * 是否在旋转
     */
    private boolean isRotateing = false;

    /**
     * 是否展示环百分比
     */
    private boolean isShowProportion = false;
    /**
     * 是否展示环间分割
     */
    private boolean isShowDivid = false;
    /**
     * 保存百分占比宽度
     */
    private List<Rect> mPercentBounds;
    /**
     * 绘制百分比画笔
     */
    private Paint mPPaint;
    private int mPColorTextDef;      // 默认百分比文本的颜色
    private int mPSizeDef;          // 大小
    private int mCColorTextDef;      // 中心文本默认百分比文本的颜色
    private int mCSizeDef;          // 中心文本大小
    private OnItemChangedListener mOnItemChangedListener;
    private boolean isCanCallBack = false;
    /**
     * 绘制中心文字
     */
    private Paint mCPaint;

    /**
     * 可显示的半径区域
     */
    private float mShowLR;

    public LPiechartView(Context context) {
        this(context, null);
    }

    public LPiechartView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LPiechartView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        TypedArray ta = context.obtainStyledAttributes(attrs,
                R.styleable.LPiechartView);
        mCircleLR = ta.getDimensionPixelSize(
                R.styleable.LPiechartView_circleLR, 220);
        mHollowRadius = ta.getDimensionPixelSize(
                R.styleable.LPiechartView_circleMR, 0);
        mSubtractHeight = ta.getDimensionPixelSize(
                R.styleable.LPiechartView_subtract_height, 0);
        mPColorTextDef = ta.getColor(R.styleable.LPiechartView_textPColor, Color.WHITE);
        mPSizeDef = ta.getDimensionPixelSize(R.styleable.LPiechartView_textPSize, getResources().getDimensionPixelSize(R.dimen.text_size_x_large));
        mCColorTextDef = ta.getColor(R.styleable.LPiechartView_textPieCenterColor, Color.BLACK);
        mCSizeDef = ta.getDimensionPixelSize(R.styleable.LPiechartView_textPieCenterSize, getResources().getDimensionPixelSize(R.dimen.text_size_x_large));
        ta.recycle();

        paintArc = new Paint();
        paintArc.setStyle(Paint.Style.FILL);// 设置填充
        paintArc.setAntiAlias(true);// 锯齿不显示
        paintArc.setColor(Color.RED);

        mPaintBg = new Paint();
        mPaintBg.setStyle(Paint.Style.FILL);// 设置填充
        mPaintBg.setAntiAlias(true);// 锯齿不显示
        mPaintBg.setColor(Color.WHITE);

        mInnerPaint = new Paint();
        mInnerPaint.setStyle(Paint.Style.FILL);// 设置填充
        mInnerPaint.setAntiAlias(true);// 锯齿不显示
        mInnerPaint.setColor(Color.WHITE);
        //画百分比
        mPPaint = new Paint();
        mPPaint.setStyle(Paint.Style.FILL);// 设置填充
        mPPaint.setAntiAlias(true);// 锯齿不显示
        mPPaint.setColor(mPColorTextDef);
        mPPaint.setTextSize(mPSizeDef);

        //中心文字
        mCPaint = new Paint();
        mCPaint.setStyle(Paint.Style.FILL);// 设置填充
        mCPaint.setAntiAlias(true);// 锯齿不显示
        mCPaint.setColor(mCColorTextDef);
        mCPaint.setTextSize(mCSizeDef);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        if (mPercentBounds == null || mChartDatas == null) {
            return;
        }

        if (mSubtractHeight != 0 && mShowLR != mCircleLR) {
            //当传入有扣除高度时，执行
            mShowLR = ((LinearLayout) getParent()).getHeight() - mSubtractHeight;
            mShowLR = mShowLR / 2;
            if (mShowLR < mCircleLR) {
                mHollowRadius = (float) (mHollowRadius * (mShowLR / mCircleLR));
                mPSizeDef = (int) (mPSizeDef * (mShowLR / mCircleLR));
                mCSizeDef = (int) (mCSizeDef * (mShowLR / mCircleLR));

                mCircleLR = (int) mShowLR;
            }
        }

        float centerX = mCircleLR;
        float centerY = mCircleLR;
        canvas.rotate(mRotate, centerX, centerY);
        canvas.save();//画布绑定一个精灵
        //画外圆
        canvas.drawCircle(centerX, centerY, mCircleLR, mPaintBg);
        //画弧度
        drawArcView(centerX, centerY, canvas);
        //画内圆
        canvas.drawCircle(centerX, centerY, mHollowRadius, mInnerPaint);
        if (mTouchIndex < mPercentBounds.size()) {
            mCPaint.setColor(ColorUtils.getColor(mChartDatas.length, mTouchIndex));
            canvas.drawText(mCenterTxt, centerX - (mPercentBounds.get(mTouchIndex).width() / 2), centerY + mPercentBounds.get(mTouchIndex).height() / 2, mCPaint);
        }
        canvas.restore();//释放这个精灵
        super.onDraw(canvas);
    }

    /**
     * 获取百分比坐标点
     *
     * @return
     */
    private Point getRoundPoint(float midx, float midy, float arg, float radius) {
        Point point = new Point();
        double x, y;
        if (arg == 0) {
            x = midx + radius;
            y = midy;
        } else if (arg == 180) {
            x = midx - radius;
            y = midy;
        } else if (arg == 90) {
            x = midx;
            y = midy + radius;
        } else if (arg == 270) {
            x = midx;
            y = midy - radius;
        } else if (arg < 90) {
            arg = (float) (Math.PI * arg / 180.0);
            x = midx + (float) (Math.cos(arg)) * radius;
            y = midy + (float) (Math.sin(arg)) * radius;
        } else if (arg > 90 && arg < 180) {
            arg = (float) (Math.PI * (180 - arg) / 180.0);
            x = midx - (float) (Math.cos(arg)) * radius;
            y = midy + (float) (Math.sin(arg)) * radius;
        } else if (arg > 180 && arg < 270) {
            arg = (float) (Math.PI * (arg - 180) / 180.0);
            x = midx - (float) (Math.cos(arg)) * radius;
            y = midy - (float) (Math.sin(arg)) * radius;
        } else {
            arg = (float) (Math.PI * (360 - arg) / 180.0);
            x = midx + (float) (Math.cos(arg)) * radius;
            y = midy - (float) (Math.sin(arg)) * radius;
        }
        point.x = (int) x;
        point.y = (int) y;
        return point;
    }

    private void drawArcView(float centerX, float centerY, Canvas canvas) {
        if (mChartDatas == null || mChartDatas.length <= 0) {
            return;
        }
        float endArg = 0.0f;
        if (isRotateing) {
            for (int i = 0; i < mChartDatas.length; i++) {
                //画角度
                paintArc.setColor(ColorUtils.getColor(mChartDatas.length, i));
                //paintArc.setColor(Color.parseColor(arcColors[i % arcColors.length]));
                if (isShowDivid)
                    endArg = mChartDatas[i].arg - 1.0f > 1.0f ? mChartDatas[i].arg - 1.0f : 1.0f;
                else {
                    endArg = mChartDatas[i].arg;
                }
                canvas.drawArc(mRect, mChartDatas[i].offsetArg, endArg, true, paintArc);
                if (isShowProportion) {
                    Point point = getRoundPoint(centerX, centerY, mChartDatas[i].midArg, (mCircleLR + mHollowRadius) / 2);
                    canvas.drawText(mChartDatas[i].percentage, point.x - (mPercentBounds.get(i).width() / 2), point.y + mPercentBounds.get(i).height() / 2, mPPaint);
                }

            }
        } else {
            for (int i = 0; i < mChartDatas.length; i++) {
                //画角度
                paintArc.setColor(ColorUtils.getColor(mChartDatas.length, i));
                //paintArc.setColor(Color.parseColor(arcColors[i % arcColors.length]));
                if (isShowDivid)
                    endArg = mChartDatas[i].arg - 1.0f > 1.0f ? mChartDatas[i].arg - 1.0f : 1.0f;
                else {
                    endArg = mChartDatas[i].arg;
                }
                if (i == mTouchIndex) {
                    canvas.drawArc(mRectSelected, mChartDatas[i].offsetArg, endArg, true, paintArc);
                } else {
                    canvas.drawArc(mRect, mChartDatas[i].offsetArg, endArg, true, paintArc);
                }
                if (isShowProportion) {
                    Point point = getRoundPoint(centerX, centerY, mChartDatas[i].midArg, (mCircleLR + mHollowRadius) / 2);
                    canvas.drawText(mChartDatas[i].percentage, point.x - (mPercentBounds.get(i).width() / 2), point.y + mPercentBounds.get(i).height() / 2, mPPaint);
                }

            }
            if (mOnItemChangedListener != null && isCanCallBack)
                mOnItemChangedListener.onItemChanged(mTouchIndex, mChartDatas[mTouchIndex]);
        }
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec); // 获取宽的模式
        int heightMode = MeasureSpec.getMode(heightMeasureSpec); // 获取高的模式
        int widthSize = MeasureSpec.getSize(widthMeasureSpec); // 获取宽的尺寸
        int heightSize = MeasureSpec.getSize(heightMeasureSpec); // 获取高的尺寸
        int width;
        int height;
        if (widthMode == MeasureSpec.EXACTLY) {
            // 如果match_parent或者具体的值，直接赋值
            width = widthSize;
            mCircleLR = width / 2;
        } else {
            // 如果是wrap_content，我们要得到控件需要多大的尺寸
            width = mCircleLR * 2;
        }
        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else {

            height = mCircleLR * 2;
        }
        mWidth = width;
        mHeight = height;
        initR();
        // 保存测量宽度和测量高度
        setMeasuredDimension(width, height);
    }

    private void initR() {
        this.paintArc.setStrokeWidth(mCircleLR / 5);
        centerX = mCircleLR;
        centerY = mCircleLR;
        mRect = new RectF();
        mRect.left = 10.5f;
        mRect.right = mWidth - 10.5f;
        mRect.bottom = mWidth - 10.5f;
        mRect.top = 10.5f;
        mRectSelected = new RectF();
        mRectSelected.left = 0;
        mRectSelected.right = mWidth;
        mRectSelected.bottom = mWidth;
        mRectSelected.top = 0;
    }

    public void setDatas(ChartData[] dataFlo) {
        this.mChartDatas = dataFlo.clone();
        if (dataFlo == null) {
            invalidate();
            return;
        }
        if (dataFlo != null && dataFlo.length < 1) {
            invalidate();
            return;
        }
        mEndRotate = 0;
        mRotate = 0;
        this.dataSum = 0f;
        for (ChartData dataitem : dataFlo) {
            this.dataSum += dataitem.data;
        }
        mPercentBounds = new ArrayList<>();
        if (dataSum == 0) {
            mChartDatas[0].percentage = "0%";
            mCenterTxt = mChartDatas[0].percentage;
            Rect prrcentBround = new Rect();
            mPPaint.getTextBounds(mChartDatas[0].percentage, 0, mChartDatas[0].percentage.length(), prrcentBround);
            mPercentBounds.add(prrcentBround);
            invalidate();
            requestLayout();
            return;
        }

        /**
         * 初始化角度
         */
        for (int i = 0; i < mChartDatas.length; i++) {
            mChartDatas[i].arg = (float) (360f * (mChartDatas[i].data / dataSum));
            mChartDatas[i].index = i;
            float argOffset = 0.0f;
            float endOffset = 0.00f;
            float midArg = 0.0f;
            if (i == 0) {
                argOffset = (180 - mChartDatas[i].arg) / 2;
                argOffset = argOffset < 0 ? 360 + argOffset : argOffset;
                mChartDatas[i].offsetArg = argOffset;
                endOffset = argOffset + mChartDatas[i].arg;
                mChartDatas[i].endArg = endOffset >= 360 ? endOffset % 360 : endOffset;
            } else {
                argOffset = mChartDatas[i - 1].offsetArg + mChartDatas[i - 1].arg;
                if (argOffset >= 360) {
                    argOffset = argOffset % 360;
                }
                endOffset = argOffset + mChartDatas[i].arg;
                mChartDatas[i].offsetArg = argOffset;
                mChartDatas[i].endArg = endOffset >= 360 ? endOffset % 360 : endOffset;

            }
            midArg = mChartDatas[i].offsetArg + mChartDatas[i].arg / 2;
            mChartDatas[i].midArg = midArg >= 360 ? midArg % 360 : midArg;
            //直接显示比例 不需要计算
            mChartDatas[i].percentage = getFormat(mChartDatas[i].data * 100 / dataSum) + "%";
            Rect prrcentBround = new Rect();
            mPPaint.getTextBounds(mChartDatas[i].percentage, 0, mChartDatas[i].percentage.length(), prrcentBround);
            mPercentBounds.add(prrcentBround);
        }
        mCenterTxt = mChartDatas[0].percentage;
        invalidate();
        requestLayout();
    }

    private float curtX, curtY;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                curtX = event.getX();
                curtY = event.getY();
                //点击的位置到圆心距离的平方
                double distance = Math.pow(curtX - centerX, 2) + Math.pow(curtY - centerX, 2);
                //判断点击的坐标是否在环内
                if (distance >= mHollowRadius * mHollowRadius) {
                    int which = touchOnWhichPart(event);
                    //点击偏移角度
                    double alfa = 0.0f;
                    switch (which) {
                        case PART_ONE:
                            alfa = Math.atan2(curtX - mCircleLR, mCircleLR - curtY) * 180 / PI + 270;
                            break;
                        case PART_TWO:
                            alfa = Math.atan2(curtY - mCircleLR, curtX - mCircleLR) * 180 / PI;
                            break;
                        case PART_THREE:
                            alfa = Math.atan2(mCircleLR - curtX, curtY - mCircleLR) * 180 / PI + 90;
                            break;
                        case PART_FOUR:
                            alfa = Math.atan2(mCircleLR - curtY, mCircleLR - curtX) * 180 / PI + 180;
                            break;
                    }
                    mTouchIndex = getCheckedItem(alfa);
                }
                break;
            case MotionEvent.ACTION_UP:
                isCanCallBack = true;
                //计算旋转角度
                mRotateAngle = calculationArg(mTouchIndex);
                if (mTouchIndex < mChartDatas.length) {
                    mCenterTxt = mChartDatas[mTouchIndex].percentage;
                }
                if (mRotateWay == 0) {
                    rotation(mRotateAngle);
                } else {
                    rotation(-mRotateAngle);
                }
                break;
        }
        return true;
    }


    public void setOnItemSelectedIndex(int index) {
        mTouchIndex = index;
        if (mTouchIndex < mChartDatas.length) {
            mCenterTxt = mChartDatas[mTouchIndex].percentage;
        }
        isCanCallBack = false;
        //计算旋转角度
        mRotateAngle = calculationArg(mTouchIndex);
        if (mRotateWay == 0) {
            rotation(mRotateAngle);
        } else {
            rotation(-mRotateAngle);
        }
    }

    /**
     * 计算旋转角度
     *
     * @return
     */
    private float calculationArg(int index) {
        float offsetDegree = 0.0f;
        float centerArg = 0;
        if (mChartDatas != null && index < mChartDatas.length) {
            float start = mChartDatas[index].offsetArg;
            float end = mChartDatas[index].endArg;

            if (start > end) {
                float center = start + mChartDatas[index].arg / 2;
                centerArg = center >= 360 ? center % 360 : center;
            } else {
                centerArg = end - mChartDatas[index].arg / 2;
            }
        }

        if ((centerArg >= 0 && centerArg <= 90)) {
            offsetDegree = 90 - centerArg;
//            System.out.println("chlu---------------顺时针");
//            System.out.println("chlu---------------旋转角度=" + offsetDegree);
            mRotateWay = 0;
        } else if ((centerArg >= 270 && centerArg <= 360)) {
            offsetDegree = 360 - centerArg + 90;
//            System.out.println("chlu---------------顺时针");
//            System.out.println("chlu---------------旋转角度=" + offsetDegree);
            mRotateWay = 0;
        } else if (centerArg > 90 && centerArg <= 180) {
            offsetDegree = centerArg - 90;
//            System.out.println("chlu---------------逆时针");
//            System.out.println("chlu---------------旋转角度=" + offsetDegree);
            mRotateWay = 1;
        } else {
            offsetDegree = centerArg - 90;
//            System.out.println("chlu---------------逆时针");
//            System.out.println("chlu---------------旋转角度=" + offsetDegree);
            mRotateWay = 1;
        }


        return offsetDegree;
    }

    /**
     * 4  |  1
     * -----|-----
     * 3 |  2
     * 圆被分成四等份，判断点击在园的哪一部分
     */

    private int touchOnWhichPart(MotionEvent event) {
        curtX = event.getX();
        curtY = event.getY();
        if (curtX >= mWidth / 2) {
            if (curtY >= mWidth / 2) {
                return PART_TWO;
            } else {
                return PART_ONE;
            }
        } else {
            if (curtY >= mWidth / 2) {
                return PART_THREE;
            } else {
                return PART_FOUR;
            }
        }

    }

    //获取点击的哪一块
    private int getCheckedItem(double arg) {

        int index = 0;
        if (mChartDatas != null && mChartDatas.length > 0) {
            for (int i = 0; i < mChartDatas.length; i++) {
                float startArg = mChartDatas[i].offsetArg;
                float endArg = mChartDatas[i].endArg;
                if (startArg <= endArg) {
                    if (arg >= startArg && arg <= endArg) {
                        index = i;
                        //    System.out.println("chlu-----------------startArg=" + startArg + "     endArg =" + endArg);
                        break;
                    }
                } else {
                    if ((arg >= startArg && arg <= 360) || (arg >= 0 && arg <= endArg)) {
                        index = i;
                        //     System.out.println("chlu-----------------startArg=" + startArg + "     endArg =" + endArg);
                        break;
                    }
                }
            }
        }

        return index;
    }

    /**
     * 重置起始角度
     *
     * @param arg
     */
    private void resetOffsetArg(float arg) {
        float argOffset = 0.0f;
        float endOffset = 0.00f;
        float midArg = 0.0f;
        if (mChartDatas != null && mChartDatas.length > 0) {
            for (int i = 0; i < mChartDatas.length; i++) {

                if (mRotateWay == 0)//顺时针
                {
                    argOffset = mChartDatas[i].offsetArg + arg;
                    mChartDatas[i].offsetArg = argOffset >= 360 ? argOffset % 360 : argOffset;
                    endOffset = argOffset + mChartDatas[i].arg;
                    mChartDatas[i].endArg = endOffset >= 360 ? endOffset % 360 : endOffset;
                    midArg = mChartDatas[i].offsetArg + mChartDatas[i].arg / 2;
                    mChartDatas[i].midArg = midArg >= 360 ? midArg % 360 : midArg;
                } else { //逆时针
                    argOffset = mChartDatas[i].offsetArg - arg;
                    mChartDatas[i].offsetArg = argOffset < 0 ? argOffset + 360 : argOffset;
                    endOffset = argOffset + mChartDatas[i].arg;
                    mChartDatas[i].endArg = endOffset >= 360 ? endOffset % 360 : endOffset < 0 ? endOffset + 360 : endOffset;
                    midArg = mChartDatas[i].offsetArg + mChartDatas[i].arg / 2;
                    mChartDatas[i].midArg = midArg >= 360 ? midArg % 360 : midArg;
                }
//                System.out.println("chlu-----------------mChartDatas[i].offsetArg=" + mChartDatas[i].offsetArg);
//                System.out.println("chlu-----------------mChartDatas[i].endArg=" + mChartDatas[i].endArg);
            }


        }
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        mRotate = Float.valueOf(valueAnimator.getAnimatedValue().toString());
        if (mRotate == mEndRotate) {
            //重置初始角度
            resetOffsetArg(mRotateAngle);
            mRotate = 0;
            isRotateing = false;
        }
        invalidate();
    }

    private void animateToValue(float value) {
        isRotateing = true;
        if (valueAnimator == null) {
            valueAnimator = createAnimator(value);
        }
        mEndRotate = mRotate + value;
        valueAnimator.setFloatValues(mRotate, mEndRotate);
        if (valueAnimator.isRunning()) {
            valueAnimator.cancel();
        }
        valueAnimator.start();
    }

    private ValueAnimator createAnimator(float value) {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, value);
        valueAnimator.setDuration(DURATION);
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.addUpdateListener(this);
        return valueAnimator;
    }


    public void rotation(float degree) {
        animateToValue(degree);
    }

    public void setShowProportion(boolean showProportion) {
        isShowProportion = showProportion;
    }

    public void setShowDivid(boolean showDivid) {
        isShowDivid = showDivid;
    }

    public void setOnItemChangedListener(OnItemChangedListener listener) {
        this.mOnItemChangedListener = listener;
    }

    public interface OnItemChangedListener {
        void onItemChanged(int index, ChartData value);
    }

    /**
     * 格式化数字
     *
     * @param value
     * @return
     */
    private String getFormat(double value) {
        return new DecimalFormat("###################.##").format(value);
    }

    private String getFormat(Double value) {
        if (value == null) {
            return "";
        } else {
            return getFormat(value.doubleValue());
        }
    }

}
