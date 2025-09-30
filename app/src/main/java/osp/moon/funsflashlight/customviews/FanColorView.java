package osp.moon.funsflashlight.customviews;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;

import java.util.List;

import osp.moon.funsflashlight.customobjects.AnimatedColor;
import osp.moon.funsflashlight.customobjects.FanColor;
import osp.moon.funsflashlight.customobjects.SolidColor;
import osp.moon.funsflashlight.helpers.CircularIntegers;

public class FanColorView extends View {

    private static final String TAG = FanColorView.class.getName();
    private int desiredWidthInPx;
    private int desiredHeightInPx;
    private FanColor mFanColor;
    private Handler mHandler;
    private Runnable mColorChangeRunnable;
    private CircularIntegers mCircularIntegers;
    private boolean isViewVisible = false;
    private boolean isWindowAttached = false;
    private boolean isAnimationRunning = false;
    private int mAnimationDelay = 1000;
    private Context mContext;
    private OnFanColorClickListener _callback;
    public interface OnFanColorClickListener {
        void onFanColorClicked(FanColor fanColor);
    }

    public FanColorView(Context context, FanColor fanColor) {
        super(context);
        Log.d(TAG, "FanColorView CONSTRUCTOR: " + (fanColor != null ? fanColor.getClass().getSimpleName() : "null"));
        init(context);
        setFanColor(fanColor);
    }

    private void init(final Context context) {
        this.mContext = context;
        this._callback = (OnFanColorClickListener) context;
        this.mHandler = new Handler(Looper.getMainLooper());

        int widthInDp = 100;
        int heightInDp = 100;

        float density = getResources().getDisplayMetrics().density;
        this.desiredWidthInPx = (int) (widthInDp * density);
        this.desiredHeightInPx = (int) (heightInDp * density);
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (_callback != null && mFanColor != null) {
                    Log.d(TAG, "FanColorView clicked. FanColor: " + (mFanColor != null ? mFanColor.getClass().getSimpleName() : "null"));
                    _callback.onFanColorClicked(mFanColor);
                }
            }
        });
        setClickable(true);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        isWindowAttached = true;
        Log.d(TAG, "onAttachedToWindow. isViewVisible: " + isViewVisible);
        conditionallyStartAnimation();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        isWindowAttached = false;
        Log.d(TAG, "onDetachedFromWindow");
        stopColorAnimation();
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        Log.d(TAG, "onVisibilityChanged called with visibility: " + visibility + ", " + (mFanColor != null ? mFanColor.getClass().getSimpleName() : "null"));
        isViewVisible = (visibility == View.VISIBLE);
        Log.d(TAG, "onVisibilityChanged: " + (isViewVisible ? "VISIBLE" : "NOT VISIBLE") + ", isWindowAttached: " + isWindowAttached);
        if (isViewVisible) {
            conditionallyStartAnimation();
        } else {
            stopColorAnimation();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;

        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            width = Math.min(desiredWidthInPx, widthSize);
        } else { // MeasureSpec.UNSPECIFIED
            width = desiredWidthInPx;
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            height = Math.min(desiredHeightInPx, heightSize);
        } else { // MeasureSpec.UNSPECIFIED
            height = desiredHeightInPx;
        }
        setMeasuredDimension(width, height);
    }

    public void setFanColor(FanColor fanColor) {
        Log.d(TAG, "setFanColor called with: " + (fanColor != null ? fanColor.getClass().getSimpleName() : "null"));
        this.mFanColor = fanColor;
        stopColorAnimation();

        if (mFanColor == null) {
            return;
        }

        if (mFanColor instanceof SolidColor) {
            SolidColor solidColor = (SolidColor) mFanColor;
            setBackgroundColor(solidColor.getColor());
            Log.d(TAG, "Set SolidColor: " + String.format("#%06X", (0xFFFFFF & solidColor.getColor())));
        } else if (mFanColor instanceof AnimatedColor) {
            AnimatedColor animatedColor = (AnimatedColor) mFanColor;
            List<Integer> ids = animatedColor.getIds();
            this.mAnimationDelay = animatedColor.getDelay() > 0 ? animatedColor.getDelay() : 1000;
            Log.d(TAG, "ADD AnimatedColor: "  + mFanColor.toString());
            if (ids != null && !ids.isEmpty()) {
                mCircularIntegers = new CircularIntegers(mContext, ids);
                Log.d(TAG, "Set AnimatedColor with " + ids.size() + " colors and delay " + this.mAnimationDelay + "ms.");
                conditionallyStartAnimation();
            }
        }
        invalidate();
    }

    private void conditionallyStartAnimation() {
        Log.d(TAG, "conditionallyStartAnimation called: " + mFanColor.toString());
        if (isViewVisible
                && isWindowAttached
                && mFanColor instanceof AnimatedColor
                && mCircularIntegers != null
                && !isAnimationRunning) {
            startColorAnimate();
        }
    }

    private void startColorAnimate() {
        Log.d(TAG, "startColorAnimateInternal called, color: " + mFanColor.toString());
        if (mCircularIntegers == null || isAnimationRunning) {
            return;
        }
        mColorChangeRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isViewVisible || !isWindowAttached || !isAnimationRunning) {
                    if (isAnimationRunning) {
                        stopColorAnimation();
                    }
                    return;
                }

                Integer nextColor = mCircularIntegers.getNext();
                setBackgroundColor(nextColor);
                invalidate();
                mHandler.postDelayed(this, mAnimationDelay);
            }
        };

        Integer firstColor = mCircularIntegers.getCurrent();
        if (firstColor == null) {
            firstColor = mCircularIntegers.getNext();
        }
        setBackgroundColor(firstColor);

        mHandler.postDelayed(mColorChangeRunnable, mAnimationDelay);
        isAnimationRunning = true;
    }

    private void stopColorAnimation() {
        Log.d(TAG, "stopColorChange called");
        if (mHandler != null && mColorChangeRunnable != null) {
            mHandler.removeCallbacks(mColorChangeRunnable);
        }
        isAnimationRunning = false;
    }

}

