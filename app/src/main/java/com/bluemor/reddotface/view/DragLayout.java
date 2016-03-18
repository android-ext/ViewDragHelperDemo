package com.bluemor.reddotface.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bluemor.reddotface.R;
import com.nineoldandroids.view.ViewHelper;

public class DragLayout extends FrameLayout {

    private static final String TAG = "DragLayout";
    private boolean isShowShadow = true;

    /** 兼容低版本的手势监测器  onTouchEvent()中使用过*/
    private GestureDetectorCompat gestureDetector;
    private ViewDragHelper dragHelper;
    private DragListener dragListener;
    /** 菜单left的最大值 */
    private int range;
    /** 菜单的宽度 */
    private int width;
    private int height;
    /** 菜单left */
    private int mainLeft;
    private Context context;
    private ImageView iv_shadow;
    private RelativeLayout vg_left;
    private MyRelativeLayout vg_main;
    private Status status = Status.Close;

    public DragLayout(Context context) {
        this(context, null);
    }

    public DragLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        this.context = context;
    }

    public DragLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        gestureDetector = new GestureDetectorCompat(context, new YScrollDetector());
        dragHelper = ViewDragHelper.create(this, dragHelperCallback);
    }

    class YScrollDetector extends SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float dx, float dy) {
            Log.i(TAG, "onScroll");
            return Math.abs(dy) <= Math.abs(dx);
        }
    }

    private ViewDragHelper.Callback dragHelperCallback = new ViewDragHelper.Callback() {

        /**
         * 约束被拖拽的子view在水平方向上的手势，默认实现是不允许水平方向的移动
         * @param child 被拖拽的view
         * @param left  沿着x轴试图移动的手势/运动
         * @param dx  left位置的建议改变
         * @return  left夹紧的位置
         *
         * */
        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            Log.i(TAG, "clampViewPositionHorizontal: left = " + left + " class = " + child.getClass().getSimpleName());
            if (mainLeft + dx < 0) {
                return 0;
            } else if (mainLeft + dx > range) {
                return range;
            } else {
                return left;
            }
        }

        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            Log.i(TAG, "tryCaptureView");
            return true;
        }

        @Override
        public int getViewHorizontalDragRange(View child) {
            Log.i(TAG, "getViewHorizontalDragRange");
            return width;
        }

        /**
         * @description:
         * @params: xvel X velocity of the pointer as it left the screen in pixels per second
         * @return:
         */
        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            Log.i(TAG, "onViewReleased");
            super.onViewReleased(releasedChild, xvel, yvel);
            if (xvel > 0) {
                open();
            } else if (xvel < 0) {
                close();
            } else if (releasedChild == vg_main && mainLeft > range * 0.3) {
                open();
            } else if (releasedChild == vg_left && mainLeft > range * 0.7) {
                open();
            } else {
                close();
            }
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            Log.i(TAG, "onViewPositionChanged : " + left + " class = " + changedView.getClass().getSimpleName());
            if (changedView == vg_main) {
                mainLeft = left;
            } else {
                mainLeft = mainLeft + left;
            }
            if (mainLeft < 0) {
                mainLeft = 0;
            } else if (mainLeft > range) {
                mainLeft = range;
            }

            if (isShowShadow) {
                iv_shadow.layout(mainLeft, 0, mainLeft + width, height);
            }
            if (changedView == vg_left) {
                vg_left.layout(0, 0, width, height);
                vg_main.layout(mainLeft, 0, mainLeft + width, height);
            }

            dispatchDragEvent(mainLeft);
        }
    };

    public interface DragListener {
        /** 实现listView位置的滑动 */
        public void onOpen();

        /** 关闭的时候主布局左侧图片按钮抖动 */
        public void onClose();

        /** 拖拽的时候设置主布局左侧图片按钮的透明度 */
        public void onDrag(float percent);
    }

    public void setDragListener(DragListener dragListener) {
        this.dragListener = dragListener;
    }

    @Override
    protected void onFinishInflate() {
        Log.i(TAG, "onFinishInflate");
        super.onFinishInflate();
        if (isShowShadow) {
            iv_shadow = new ImageView(context);
            iv_shadow.setImageResource(R.drawable.shadow);
            LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            addView(iv_shadow, 1, lp);
        }
        vg_left = (RelativeLayout) getChildAt(0);
        vg_main = (MyRelativeLayout) getChildAt(isShowShadow ? 2 : 1);
        /** 主布局对当前对象的引用 */
        vg_main.setDragLayout(this);
        vg_left.setClickable(true);
        vg_main.setClickable(true);
    }

    public ViewGroup getVg_main() {
        return vg_main;
    }

    public ViewGroup getVg_left() {
        return vg_left;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.i(TAG, "onMeasure");
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        Log.i(TAG, "onSizeChanged");
        super.onSizeChanged(w, h, oldw, oldh);
        width = vg_left.getMeasuredWidth();
        height = vg_left.getMeasuredHeight();
        range = (int) (width * 0.6f);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        Log.i(TAG, "onLayout ： " + mainLeft);
        vg_left.layout(0, 0, width, height);
        vg_main.layout(mainLeft, 0, mainLeft + width, height);
    }

    /**
     * public static final int ACTION_DOWN             = 0;
     * public static final int ACTION_UP               = 1;
     * public static final int ACTION_MOVE             = 2;
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        Log.i(TAG, "onInterceptTouchEvent:" + ev.getAction());
        boolean shouldInterceptTouchEvent = dragHelper.shouldInterceptTouchEvent(ev);
        boolean onTouchEvent = false;
        if (shouldInterceptTouchEvent) {
            onTouchEvent = gestureDetector.onTouchEvent(ev);
        } else {
            return false;
        }
        return onTouchEvent;
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        Log.i(TAG, "onTouchEvent");
        try {
            dragHelper.processTouchEvent(e);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    private void dispatchDragEvent(int mainLeft) {
        if (dragListener == null) {
            return;
        }
        float percent = mainLeft / (float) range;
        animateView(percent);
        // 设置主界面导航栏左侧图片透明度的回调
        dragListener.onDrag(percent);
        Status lastStatus = status;
        if (lastStatus != getStatus() && status == Status.Close) {
            // 设置主界面导航栏左侧图片抖动回调
            dragListener.onClose();
        } else if (lastStatus != getStatus() && status == Status.Open) {
            // 设置主界面导航栏左侧列表位置的移动回调
            dragListener.onOpen();
        }
    }

    /**
     * @description: 菜单和主界面view的动画效果设置
     * @author: Ext
     * @time: 2016/3/16 9:21
     */
    private void animateView(float percent) {
        float f1 = 1 - percent * 0.3f;
        // 主界面的缩放
        ViewHelper.setScaleX(vg_main, f1);
        ViewHelper.setScaleY(vg_main, f1);
        // 菜单的平移和缩放
        ViewHelper.setTranslationX(vg_left, vg_left.getWidth() / 2.3f * (percent - 1));
        ViewHelper.setScaleX(vg_left, 0.5f + 0.5f * percent);
        ViewHelper.setScaleY(vg_left, 0.5f + 0.5f * percent);
        // 菜单的透明度改变
        ViewHelper.setAlpha(vg_left, percent);
        if (isShowShadow) {
            ViewHelper.setScaleX(iv_shadow, f1 * 1.4f * (1 - percent * 0.12f));
            ViewHelper.setScaleY(iv_shadow, f1 * 1.85f * (1 - percent * 0.12f));
        }
        getBackground().setColorFilter(evaluate(percent, Color.BLACK, Color.TRANSPARENT), Mode.SRC_OVER);
    }

    private Integer evaluate(float fraction, Object startValue, Integer endValue) {
        int startInt = (Integer) startValue;
        int startA = (startInt >> 24) & 0xff;
        int startR = (startInt >> 16) & 0xff;
        int startG = (startInt >> 8) & 0xff;
        int startB = startInt & 0xff;
        int endInt = (Integer) endValue;
        int endA = (endInt >> 24) & 0xff;
        int endR = (endInt >> 16) & 0xff;
        int endG = (endInt >> 8) & 0xff;
        int endB = endInt & 0xff;
        return (int) ((startA + (int) (fraction * (endA - startA))) << 24)
                | (int) ((startR + (int) (fraction * (endR - startR))) << 16)
                | (int) ((startG + (int) (fraction * (endG - startG))) << 8)
                | (int) ((startB + (int) (fraction * (endB - startB))));
    }

    @Override
    public void computeScroll() {
        Log.i(TAG, "computeScroll");
        if (dragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    public enum Status {
        Drag, Open, Close
    }

    /**
     * @description: 获取菜单当前状态
     * @time: 2016/3/16 9:06
     */
    public Status getStatus() {
        if (mainLeft == 0) {
            status = Status.Close;
        } else if (mainLeft == range) {
            status = Status.Open;
        } else {
            status = Status.Drag;
        }
        return status;
    }

    public void open() {
        open(true);
    }

    public void open(boolean animate) {
        Log.i(TAG, "open: range = " + range);
        if (animate) {
            /**
             * @param child Child view to capture and animate
             * @param finalLeft Final left position of child
             * @param finalTop Final top position of child
             * @return true if animation should continue through {@link #continueSettling(boolean)} calls
             * 如果还没有滑动到指定的位置则返回true调用view.invalidate()，在view.draw()中会调用computeScroll() 继续invalidate()
             */
            if (dragHelper.smoothSlideViewTo(vg_main, range, 0)) {
                ViewCompat.postInvalidateOnAnimation(this);
            }
        } else {
            vg_main.layout(range, 0, range * 2, height);
            dispatchDragEvent(range);
        }
    }

    public void close() {
        close(true);
    }

    public void close(boolean animate) {
        if (animate) {
            if (dragHelper.smoothSlideViewTo(vg_main, 0, 0)) {
                ViewCompat.postInvalidateOnAnimation(this);
            }
        } else {
            vg_main.layout(0, 0, width, height);
            dispatchDragEvent(0);
        }
    }

}
/**
 启动后打印的日志信息
 onFinishInflate
 onMeasure
 onSizeChanged
 onLayout ： 0
 onMeasure
 onLayout ： 0
 computeScroll
 onMeasure
 onLayout ： 0
 computeScroll
 onMeasure
 onLayout ： 0
 computeScroll
 computeScroll
 onMeasure
 onLayout ： 0
 computeScroll
 onMeasure
 onLayout ： 0
 computeScroll
 */

/**
 拖动后显示菜单时的打印的日志信息
 onInterceptTouchEvent
 onInterceptTouchEvent
 getViewHorizontalDragRange
 onInterceptTouchEvent
 getViewHorizontalDragRange
 clampViewPositionHorizontal
 getViewHorizontalDragRange
 tryCaptureView
 onScroll
 onTouchEvent
 clampViewPositionHorizontal
 onViewPositionChanged : 383 class = MyRelativeLayout
 computeScroll
 onTouchEvent
 clampViewPositionHorizontal
 onViewPositionChanged : 336 class = MyRelativeLayout
 computeScroll
 onTouchEvent
 */
