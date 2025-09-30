package osp.moon.funsflashlight.customviews;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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
    private int animationDelay = 1000;
    private Context mContext;
    private OnFanColorClickListener _callback;
    public interface OnFanColorClickListener {
        void onFanColorClicked(FanColor fanColor);
    }
    public void setOnFanColorClickListener(OnFanColorClickListener callback) {
        this._callback = callback;
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
        stopColorChange();
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        Log.d(TAG, "onVisibilityChanged called with visibility: " + visibility + ", " + (mFanColor != null ? mFanColor.getClass().getSimpleName() : "null"));
        //if (changedView == this) {
            isViewVisible = (visibility == View.VISIBLE);
            Log.d(TAG, "onVisibilityChanged: " + (isViewVisible ? "VISIBLE" : "NOT VISIBLE") + ", isWindowAttached: " + isWindowAttached);
            if (isViewVisible) {
                conditionallyStartAnimation();
            } else {
                stopColorChange();
            }
        //}
    }

    // onWindowVisibilityChanged может быть полезен, но для простоты начнем без него,
    // так как onVisibilityChanged и onAttachedToWindow/onDetachedFromWindow
    // часто покрывают большинство случаев. Если нужна более гранулированная проверка
    // видимости окна (например, при перекрытии диалогом), его можно добавить.
    /*
    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        // boolean wasWindowVisible = isWindowVisible;
        // isWindowVisible = (visibility == View.VISIBLE);
        // Log.d(TAG, "onWindowVisibilityChanged: " + (isWindowVisible ? "VISIBLE" : "NOT VISIBLE"));
        // if (isWindowVisible) {
        //     conditionallyStartAnimation();
        // } else {
        //     stopColorChange();
        // }
    }
    */


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
        stopColorChange();

        if (mFanColor == null) {
            setBackgroundColor(Color.TRANSPARENT);
            Log.d(TAG, "FanColor is null, setting background to TRANSPARENT.");
            return;
        }

        if (mFanColor instanceof SolidColor) {
            SolidColor solidColor = (SolidColor) mFanColor;
            setBackgroundColor(solidColor.getColor());
            Log.d(TAG, "Set SolidColor: " + String.format("#%06X", (0xFFFFFF & solidColor.getColor())));
        } else if (mFanColor instanceof AnimatedColor) {
            AnimatedColor animatedColor = (AnimatedColor) mFanColor;
            List<Integer> ids = animatedColor.getIds();
            this.animationDelay = animatedColor.getDelay() > 0 ? animatedColor.getDelay() : 1000;
            Log.d(TAG, "ADD AnimatedColor: "  + mFanColor.toString());
            if (ids != null && !ids.isEmpty()) {
                mCircularIntegers = new CircularIntegers(mContext, ids);
                Log.d(TAG, "Set AnimatedColor with " + ids.size() + " colors and delay " + this.animationDelay + "ms.");
                conditionallyStartAnimation(); // Анимация начнется, только если View видима
            } else {
                setBackgroundColor(Color.TRANSPARENT);
                Log.w(TAG, "Список ID для AnimatedColor пуст или null. Setting background to TRANSPARENT.");
            }
        }
        invalidate();
    }

    private void conditionallyStartAnimation() {
        Log.d(TAG, "conditionallyStartAnimation called: " + mFanColor.toString());
        if (isViewVisible && isWindowAttached && mFanColor instanceof AnimatedColor && mCircularIntegers != null && !isAnimationRunning) {
            Log.d(TAG, "Условия для старта анимации выполнены. Запуск...");
            startColorAnimateInternal();
        } else {
            StringBuilder reason = new StringBuilder("Условия для старта анимации НЕ выполнены: ");
            if (!isViewVisible) reason.append("View не видима. ");
            if (!isWindowAttached) reason.append("View не присоединена к окну. ");
            if (!(mFanColor instanceof AnimatedColor)) reason.append("mFanColor не AnimatedColor. ");
            if (mCircularIntegers == null) reason.append("mCircularIntegers is null. ");
            if (isAnimationRunning) reason.append("Анимация уже запущена. ");
            Log.d(TAG, reason.toString());
        }
    }

    private void startColorAnimateInternal() {
        Log.d(TAG, "startColorAnimateInternal called, color: " + mFanColor.toString());
        if (mCircularIntegers == null) { // Дополнительная проверка
            Log.w(TAG, "CircularIntegers не инициализирован в startColorAnimateInternal, анимация не начнется.");
            return;
        }
        if (isAnimationRunning) {
            Log.d(TAG, "Анимация уже запущена (проверка в startColorAnimateInternal), повторный запуск не требуется.");
            return;
        }

        mColorChangeRunnable = new Runnable() {
            @Override
            public void run() {
                // Дополнительная проверка видимости внутри Runnable
                if (!isViewVisible || !isWindowAttached || !isAnimationRunning) {
                    // Если isAnimationRunning уже false, значит stopColorChange был вызван извне
                    if (isAnimationRunning) { // Только если мы сами себя останавливаем здесь
                        Log.d(TAG, "Runnable: View стала невидимой/отсоединена или анимация остановлена. Остановка цикла.");
                        stopColorChange(); // Останавливаем, если View больше не видна/присоединена
                    }
                    return;
                }

                Integer nextColor = mCircularIntegers.getNext();
                setBackgroundColor(nextColor);
                invalidate();
                //Log.d(TAG, "Анимированный цвет установлен: " + String.format("#%06X", (0xFFFFFF & nextColor)) + ", следующий через " + animationDelay + "ms");
                mHandler.postDelayed(this, animationDelay);
            }
        };

        // Устанавливаем первый цвет немедленно
        Integer firstColor = mCircularIntegers.getCurrent();
        if (firstColor == null) {
            firstColor = mCircularIntegers.getNext();
        }
        setBackgroundColor(firstColor);
        Log.d(TAG, "Первый анимированный цвет установлен: " + String.format("#%06X", (0xFFFFFF & firstColor)));

        mHandler.postDelayed(mColorChangeRunnable, animationDelay);
        isAnimationRunning = true;
        Log.d(TAG, "Анимация ЗАПУЩЕНА с задержкой: " + animationDelay + "ms");
    }

    private void stopColorChange() {
        Log.d(TAG, "stopColorChange called");
        if (mHandler != null && mColorChangeRunnable != null) {
            mHandler.removeCallbacks(mColorChangeRunnable);
        }
        if (isAnimationRunning) {
            Log.d(TAG, "Анимация ОСТАНОВЛЕНА");
        }
        isAnimationRunning = false;
    }

    /**
     * Возвращает текущий объект FanColor, установленный для этого View.
     * @return FanColor или null, если не установлен.
     */
    public FanColor getFanColor() {
        return mFanColor;
    }
}

