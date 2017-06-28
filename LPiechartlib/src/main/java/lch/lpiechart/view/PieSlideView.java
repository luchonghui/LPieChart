package lch.lpiechart.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import java.util.ArrayList;
import java.util.List;

import lch.lpiechart.R;
import lch.lpiechart.bean.ChartData;
import lch.lpiechart.utils.ColorUtils;

/**
 * 滑动view
 *
 * @author luchonghui
 * @date 2017/5/20 9:28
 */

public class PieSlideView extends View implements ValueAnimator.AnimatorUpdateListener {

    /**
     * 文本的颜色
     */
    private int mTextColor;
    /**
     * 分割线颜色颜色
     */
    private int mDividerColor;
    /**
     * 文本字体大小
     */
    private int mTextSize;
    /**
     * 百分比字体大小
     */
    private int mPTextSize;
    /**
     * 水平分割线高度
     */
    private int mHDividerHeight;
    /**
     * 水平分割线宽度
     */
    private int mHDividerWidth;
    /**
     * 垂直分割线高度
     */
    private int mVDividerHeight;
    /**
     * 三角形直角边长度
     */
    private int mTriangleHeight;

    private int mGridHeight;
    private int mGridWidth;
    private int mGridColor;
    /**
     * 字体高度
     */
    private int mTextHeight;

    private int mTextStartY;         //文本绘制的Y轴坐标
    private int mPStartY;         //百分比的Y轴坐标
    private int mWidth, mHeigth;
    /**
     * 文本画笔
     */
    private Paint mTextPaint;
    private Paint mHDividerPaint;
    private Paint mPPaint;
    private Paint mOtherPaint;
    private Paint mVDividerPaint;
    private Paint mGridPaint;

    private ValueAnimator mValueAnimator;

    /**
     * 添加的数据
     */
    private ChartData[] slideDatas;

    private OnItemChangedListener mOnItemChangedListener;

    /**
     * 初始偏移量
     */
    private float xOffset = 0.0f;
    /**
     * 按下位置
     */
    private float xDown = 0.0f;

    private float xPreOffset = 0.0f;
    private float mStart, mEnd;
    private float[] xPoints;
    private int selectedIndex = 0;      //当前选中序号
    private List<Rect> mBounds;     //保存文本的量的结果
    private List<Rect> mPercentBounds; //保存百分占比宽度
    /**
     * 控件宽度
     */
    private float xViewWith = 0.0f;
    private boolean isSliding = false;  //手指是否在拖动
    private float slidX, slidY;         //手指当前位置（相对于本控件左上角的坐标）

    public PieSlideView(Context context) {
        this(context, null);
    }

    public PieSlideView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PieSlideView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //初始化属性
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.PieSlideView);
        mTextColor = ta.getColor(R.styleable.PieSlideView_textColor, Color.BLACK);
        mDividerColor = ta.getColor(R.styleable.PieSlideView_dividerColor, Color.GRAY);
        mGridColor = ta.getColor(R.styleable.PieSlideView_gridColor, Color.WHITE);
        mTextSize = ta.getDimensionPixelSize(R.styleable.PieSlideView_textSize, getResources().getDimensionPixelSize(R.dimen.text_size_x_large));
        mPTextSize = ta.getDimensionPixelSize(R.styleable.PieSlideView_textPCSize, getResources().getDimensionPixelSize(R.dimen.text_size_x_large));

        mHDividerHeight = ta.getDimensionPixelSize(R.styleable.PieSlideView_hDividerHeight, 100);
        mHDividerWidth = ta.getDimensionPixelSize(R.styleable.PieSlideView_hDividerWidth, 20);
        mVDividerHeight = ta.getDimensionPixelSize(R.styleable.PieSlideView_vDividerHeight, 10);
        mGridHeight = ta.getDimensionPixelSize(R.styleable.PieSlideView_gridHight, 100);
        mGridWidth = ta.getDimensionPixelSize(R.styleable.PieSlideView_gridWidth, 200);
        mTriangleHeight = ta.getDimensionPixelSize(R.styleable.PieSlideView_triangleHeight, 30);

        mHDividerPaint = new Paint();
        mHDividerPaint.setColor(mDividerColor);
        mHDividerPaint.setStyle(Paint.Style.FILL);//设置填充
        mHDividerPaint.setStrokeWidth(mHDividerHeight);//笔宽像素
        mHDividerPaint.setAntiAlias(true);//锯齿不显示

        mVDividerPaint = new Paint();
        mVDividerPaint.setColor(mDividerColor);
        mVDividerPaint.setStyle(Paint.Style.FILL);//设置填充
        mVDividerPaint.setStrokeWidth(mGridHeight + mVDividerHeight * 2);//笔宽像素
        mVDividerPaint.setAntiAlias(true);//锯齿不显示

        mOtherPaint = new Paint();


        mTextPaint = new Paint();
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setColor(mTextColor);
        mPPaint = new Paint();
        mPPaint.setTextSize(mPTextSize);

        mGridPaint = new Paint();
        mGridPaint.setColor(mGridColor);
        mGridPaint.setStyle(Paint.Style.FILL);//设置填充
        mGridPaint.setStrokeWidth(mGridHeight);//笔宽像素
        mGridPaint.setAntiAlias(true);//锯齿不显示


        ta.recycle();


    }

    @Override
    protected void onDraw(Canvas canvas) {

        if (slideDatas != null && slideDatas.length > 0) {
            //画灰色基准线
            canvas.drawLine(xOffset - mGridWidth / 2 - mHDividerWidth, (mGridHeight + mVDividerHeight * 2) / 2, xOffset + (slideDatas.length) * (mGridWidth + mHDividerWidth) - mGridWidth / 2, (mGridHeight + mVDividerHeight * 2) / 2, mVDividerPaint);
            float centerY = mVDividerHeight;
            for (int i = 0; i < slideDatas.length; i++) {
                float centerX = xOffset + (i * mGridWidth) + (i * mHDividerWidth);
                //画基准线上灰色小圆圈
                xPoints[i] = centerX;//记录坐标点
                //画背景
                canvas.drawRect(centerX - mGridWidth / 2, mVDividerHeight, centerX + mGridWidth / 2, mVDividerHeight + mGridHeight, mGridPaint);

                /*******绘制文字*****/
                float startX;
                startX = centerX - (mBounds.get(i).width() / 2);
                canvas.drawText(slideDatas[i].name, startX, mVDividerHeight * 5, mTextPaint);
                /*******画百分比*****/
                mPPaint.setColor(ColorUtils.getColor(slideDatas.length, i));
                startX = centerX - (mPercentBounds.get(i).width() / 2);
                canvas.drawText(slideDatas[i].percentage, startX, mBounds.get(i).height() + mVDividerHeight * 7, mPPaint);
                /*******画三角*****/
                Path path = new Path();
                path.moveTo(centerX + mGridWidth / 2 - mTriangleHeight, centerY);
                path.lineTo(centerX + mGridWidth / 2, centerY);
                path.lineTo(centerX + mGridWidth / 2, centerY + mTriangleHeight);
                path.close();
                canvas.drawPath(path, mPPaint);
            }
        }

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);   //获取宽的模式
        int heightMode = MeasureSpec.getMode(heightMeasureSpec); //获取高的模式
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);   //获取宽的尺寸
        int heightSize = MeasureSpec.getSize(heightMeasureSpec); //获取高的尺寸

        int width;
        int height;
        if (widthMode == MeasureSpec.EXACTLY) {
            //如果match_parent或者具体的值，直接赋值
            width = widthSize;
        } else {
            //如果是wrap_content，我们要得到控件需要多大的尺寸
            width = widthSize;
        }
        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else {
            if (mBounds != null && mBounds.size() > 0) {
                height = mVDividerHeight * 2 + mGridHeight;
            } else {
                height = 200;
            }
        }
        mWidth = width;
        mHeigth = height;
        //保存测量宽度和测量高度
        setMeasuredDimension(width, height);
        initConstant();
    }

    private void animateToValue(float start, float end) {

        if (mValueAnimator == null) {
            mValueAnimator = createAnimator(end);
        }
        mValueAnimator.setFloatValues(start, end);
        if (mValueAnimator.isRunning()) {
            mValueAnimator.cancel();
        }
        mValueAnimator.start();
    }

    private long DURATION = 600;//动画时长 毫秒

    private ValueAnimator createAnimator(float value) {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, value);
        valueAnimator.setDuration(DURATION);
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.addUpdateListener(this);
        return valueAnimator;
    }

    /**
     * 获取回弹最近的坐标点
     *
     * @return
     */
    private int getNearByItemIndex() {
        int index = 0;
        float xCenter = xViewWith / 2;
        float miniDistance = Math.abs(xPoints[0] - xCenter);
        for (int i = 1; i < xPoints.length; i++) {

            if (Math.abs(xPoints[i] - xCenter) <= miniDistance) {
                miniDistance = Math.abs(xPoints[i] - xCenter);
                index = i;
            }
        }

        return index;
    }

    /**
     * 设置中间点坐标的item
     *
     * @param index
     */
    private void setCenterItem(int index) {
        if (index < xPoints.length) {
            float xPoint = xPoints[index]; //该点x坐标
            float offset = xViewWith / 2 - xPoint;
            mStart = xOffset;
            mEnd = xOffset + offset;
            animateToValue(mStart, mEnd);
            if (mOnItemChangedListener != null)
                mOnItemChangedListener.onItemChanged(index);
        }
    }

    /**
     * 设置当前选中的位置
     *
     * @param selectedIndex start with 0
     */
    public void setSelectedIndex(int selectedIndex) {
        if (selectedIndex < 0) {
            Log.e("chlu", "selected index is error, " + selectedIndex + " is less 0, please be start with 0");
            return;
        }
        if (selectedIndex >= slideDatas.length) {
            Log.e("chlu", "selected index is error, the max index is " + (slideDatas.length - 1) + " ,but your's is " + selectedIndex);
            return;
        }
        this.selectedIndex = selectedIndex;
        setCenterItem(selectedIndex);
        invalidate();
    }

    /**
     * 检查点击哪个点
     *
     * @param x
     * @return
     */
    private int checkPressItem(float x) {

        int index = -1;
        for (int i = 0; i < xPoints.length; i++) {

            if (Math.abs(xPoints[i] - x) <= mGridWidth / 2) {
                return i;
            }
        }
        return index;

    }

    /**
     * 设置下面的字
     *
     * @param slideDatas
     */
    public void setSlideTabs(ChartData[] slideDatas) {
        if (slideDatas != null) {
            this.slideDatas = slideDatas;
            xPoints = new float[slideDatas.length];
            measureText();
            selectedIndex = 0;
            initConstant();
            if (mBounds != null && mBounds.size() > 0) {
                mTextHeight = mBounds.get(0).height();
            }
        } else {
            this.slideDatas = slideDatas;
            mBounds = null;
        }
        invalidate();
    }

    /**
     * measure the text bounds by paint
     */
    private void measureText() {
        mBounds = new ArrayList<>();
        mPercentBounds = new ArrayList<>();
        for (ChartData data : slideDatas) {
            Rect bound = new Rect();
            mTextPaint.getTextBounds(data.name, 0, data.name.length(), bound);
            mBounds.add(bound);
            Rect prrcentBround = new Rect();
            mTextPaint.getTextBounds(data.percentage, 0, data.percentage.length(), prrcentBround);
            mPercentBounds.add(prrcentBround);
        }
    }

    public void setOnItemChangedListener(OnItemChangedListener listener) {
        this.mOnItemChangedListener = listener;
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        xOffset = Float.valueOf(mValueAnimator.getAnimatedValue().toString());
        if (xOffset == mEnd) {
            //重置初始角度
            xPreOffset = xOffset;
        }
        invalidate();
    }

    public interface OnItemChangedListener {
        void onItemChanged(int index);
    }

    private void initConstant() {
        xViewWith = getWidth();
        if (xViewWith <= 0) {
            xViewWith = mWidth;
        }
        xOffset = xViewWith / 2;
        xPreOffset = xOffset;
        // FontMetrics对象
        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        mTextStartY = mHeigth - (int) fontMetrics.bottom;    //baseLine的位置
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        slidX = event.getX();   //以本控件左上角为坐标原点
        slidY = event.getY();
        if (slideDatas == null)
            return true;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //  Log.e(TAG, "手指按下:  getX:" + slidX + "  getY:" + slidY);
                xDown = slidX;
                break;
            case MotionEvent.ACTION_MOVE:
                //  Log.i(TAG, "手指滑动:  getX:" + slidX + "  getY:" + slidY);
                xOffset = xPreOffset + (slidX - xDown);
                if (Math.abs(slidX - xDown) > 3)
                    isSliding = true;
                break;
            case MotionEvent.ACTION_UP:
                //    Log.e(TAG, "手指抬起:  getX:" + slidX + "  getY:" + slidY);
                xOffset = xPreOffset + (slidX - xDown);
                xPreOffset = xOffset;
                if (Math.abs(slidX - xDown) <= 3) {
                    //查看点击的哪个条目
                    int index = checkPressItem(xDown);
                    if (index != -1) {
                        //将该点直接移到中间
                        setCenterItem(index);
                        selectedIndex = index;
                        Log.i("chlu", "点击位置=" + (index + 1));
                    } else {
                        //点击事件无效时获取最近点
                        index = getNearByItemIndex();
                        setCenterItem(index);
                        selectedIndex = index;
                        Log.i("chlu", "点击无效回弹位置=" + (index + 1));
                    }
                } else {
                    //获取回弹坐标点
                    int index = getNearByItemIndex();
                    setCenterItem(index);
                    selectedIndex = index;
                    Log.i("chlu", "拖动回弹位置=" + (index + 1));
                }
                isSliding = false;
                break;
        }
        invalidate();
        return true;
    }

}
