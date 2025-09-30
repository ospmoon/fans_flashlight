package osp.moon.funsflashlight.customviews;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.graphics.Canvas; // Импорт для Canvas
import android.graphics.Color;  // Импорт для Color
import android.graphics.Paint;  // Импорт для Paint
import android.graphics.Rect;   // Импорт для Rect (для текста)
import android.text.TextPaint; // Более подходящий Paint для текста

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
    private Paint borderPaint;
    private TextPaint titlePaint;
    private float borderWidth = 1f; // Ширина контура в пикселях (можно сделать в dp)
    private float paddingBetweenBorderAndColor = 4f; // Отступ в пикселях (можно сделать в dp)
    private String titleText = ""; // Текст для отображения
    private Rect textBounds = new Rect(); // Для вычисления размеров текста

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
        if (context instanceof OnFanColorClickListener) {
            this._callback = (OnFanColorClickListener) context;
        }
        this.mHandler = new Handler(Looper.getMainLooper());

        int widthInDp = 80;
        int heightInDp = 80;

        float density = getResources().getDisplayMetrics().density;
        this.desiredWidthInPx = (int) (widthInDp * density);
        this.desiredHeightInPx = (int) (heightInDp * density);

        borderWidth = 1 * density; // Пример: 4dp ширина контура
        paddingBetweenBorderAndColor = 4 * density; // Пример: 8dp отступ

        borderPaint = new Paint();
        borderPaint.setStyle(Paint.Style.STROKE); // Только контур
        borderPaint.setColor(Color.BLACK);        // Цвет контура (можно настраивать)
        borderPaint.setStrokeWidth(borderWidth);
        borderPaint.setAntiAlias(true);

        titlePaint = new TextPaint(); // Используем TextPaint для текста
        titlePaint.setColor(Color.BLACK);         // Цвет текста (можно настраивать)
        titlePaint.setTextSize(16 * density);     // Размер текста (пример: 16sp)
        titlePaint.setTextAlign(Paint.Align.CENTER); // Выравнивание текста по центру
        titlePaint.setAntiAlias(true);

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
        setTitle(mFanColor.getTitle());
        setBorderColor(Color.TRANSPARENT);


        if (mFanColor instanceof SolidColor) {
            invalidate();
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

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        // Получаем размеры View
        int width = getWidth();
        int height = getHeight();

        if (width == 0 || height == 0) {
            return; // Нечего рисовать, если размеров нет
        }

        // 1. Рисуем основную цветную область с отступом от контура
        // Прямоугольник для цветной области
        float colorAreaLeft = borderWidth / 2f + paddingBetweenBorderAndColor;
        float colorAreaTop = borderWidth / 2f + paddingBetweenBorderAndColor;
        float colorAreaRight = width - (borderWidth / 2f + paddingBetweenBorderAndColor);
        float colorAreaBottom = height - (borderWidth / 2f + paddingBetweenBorderAndColor);

        int currentColorToDraw = Color.TRANSPARENT; // Цвет по умолчанию

        if (mFanColor instanceof SolidColor) {
            currentColorToDraw = ((SolidColor) mFanColor).getColor();
        } else if (mFanColor instanceof AnimatedColor && mCircularIntegers != null) {
            // Для анимированного цвета, берем текущий цвет из CircularIntegers
            // Это будет тот цвет, который установил Runnable анимации
            // Однако, если анимация еще не стартанула или остановлена,
            // getCurrent() может вернуть последний цвет или null.
            Integer animatedCurrentColor = mCircularIntegers.getCurrent();
            if (animatedCurrentColor != null) {
                currentColorToDraw = animatedCurrentColor;
            } else if (!((AnimatedColor) mFanColor).getIds().isEmpty()){
                // Если текущий null, но есть ID, возьмем первый из списка ID как начальный
                // (Это для случая до первого тика анимации)
                try {
                    currentColorToDraw = ((AnimatedColor) mFanColor).getIds().get(0);
                } catch (IndexOutOfBoundsException e) {
                    Log.e(TAG, "Ошибка получения первого цвета из AnimatedColor IDs", e);
                }
            }
        } else if (mFanColor == null && isAnimationRunning) {
            // Если анимация была, но mFanColor стал null, останавливаем
            stopColorAnimation();
        }


        // Используем временный Paint для заливки цветом, если нет своего
        Paint colorFillPaint = new Paint();
        colorFillPaint.setStyle(Paint.Style.FILL);
        colorFillPaint.setColor(currentColorToDraw);
        colorFillPaint.setAntiAlias(true);

        if (colorAreaLeft < colorAreaRight && colorAreaTop < colorAreaBottom) {
            canvas.drawRect(colorAreaLeft, colorAreaTop, colorAreaRight, colorAreaBottom, colorFillPaint);
        }


        // 2. Рисуем контур вокруг всей View
        // Прямоугольник для контура должен учитывать половину ширины кисти,
        // чтобы вся линия была видна внутри View.
        float borderRectLeft = borderWidth / 2f;
        float borderRectTop = borderWidth / 2f;
        float borderRectRight = width - borderWidth / 2f;
        float borderRectBottom = height - borderWidth / 2f;

        if (borderRectLeft < borderRectRight && borderRectTop < borderRectBottom) {
            canvas.drawRect(borderRectLeft, borderRectTop, borderRectRight, borderRectBottom, borderPaint);
        }

        // 3. Рисуем текст (титл) в центре
        if (titleText != null && !titleText.isEmpty()) {
            // Вычисляем позицию для текста, чтобы он был по центру
            // titlePaint.setTextAlign(Paint.Align.CENTER) уже установлено

            // Получаем границы текста для точного центрирования по вертикали
            titlePaint.getTextBounds(titleText, 0, titleText.length(), textBounds);
            float textX = width / 2f;
            // Центрируем по вертикали: (высота_view / 2) - ( (textBounds.bottom + textBounds.top) / 2 )
            // textBounds.bottom это смещение нижней части текста от базовой линии
            // textBounds.top это смещение верхней части текста от базовой линии (обычно отрицательное)
            float textY = height / 2f - textBounds.exactCenterY();


            canvas.drawText(titleText, textX, textY, titlePaint);
        }
    }



    public void setTitle(String title) {
        this.titleText = title == null ? "" : title;
        invalidate();
    }

    public void setBorderColor(int color) {
        if (borderPaint != null) {
            borderPaint.setColor(color);
            invalidate();
        }
    }

    public void setTitleColor(int color) {
        if (titlePaint != null) {
            titlePaint.setColor(color);
            invalidate();
        }
    }

    @Override
    public void setBackgroundColor(int color) {
        // Мы больше не будем использовать стандартный setBackgroundColor,
        // так как будем рисовать цветную область сами внутри onDraw.
        // Вместо этого, сохраним цвет и перерисуем.
        // Это изменение нужно, чтобы фон не перекрывал наш кастомный рисунок.
        // Если вы все же хотите использовать setBackgroundColor для SolidColor,
        // то в onDraw для AnimatedColor нужно будет рисовать поверх фона.
        // Для простоты, будем всегда рисовать цветную область в onDraw.

        // Удаляем или комментируем: super.setBackgroundColor(color);
        // Вместо этого, если mFanColor - SolidColor, можно сохранить его цвет
        // для использования в onDraw. Но ваш setFanColor уже это делает.
        // Важно: если setBackgroundColor вызывается извне, это может
        // сбить нашу кастомную отрисовку.
        Log.d(TAG, "setBackgroundColor called with " + String.format("#%06X", (0xFFFFFF & color)) + ", но мы рисуем цвет в onDraw.");
        // Просто вызываем invalidate, чтобы onDraw был вызван с новым цветом (если он берется из mFanColor)
        // Но лучше, чтобы цвет для отрисовки всегда брался из mFanColor.
    }

}

