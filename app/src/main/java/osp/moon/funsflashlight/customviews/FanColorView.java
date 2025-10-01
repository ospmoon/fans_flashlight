package osp.moon.funsflashlight.customviews;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;
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

import java.io.IOException;
import java.io.InputStream;
import android.graphics.Path;
import java.util.List;

import osp.moon.funsflashlight.customobjects.AnimatedColor;
import osp.moon.funsflashlight.customobjects.FanColor;
import osp.moon.funsflashlight.customobjects.FanImage;
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
    private Bitmap currentBitmapToDraw = null; // Для хранения загруженной картинки
    private Paint bitmapPaint;                 // Paint для отрисовки Bitmap (можно настроить фильтрацию и т.д.)
    private RectF drawingRect = new RectF();   // Прямоугольник для рисования Bitmap (для масштабирования)
    private Paint colorFillPaint;
    private Path clipPath; // <-- Путь для обрезки канваса
    private float cornerRadius; // <-- Радиус закругления углов
    private final OnFanColorClickListener _callback;
    public interface OnFanColorClickListener {
        void onFanColorClicked(FanColor fanColor);
    }

    public FanColorView(Context context, FanColor fanColor, OnFanColorClickListener callback) {
        super(context);
        Log.d(TAG, "FanColorView CONSTRUCTOR: " + (fanColor != null ? fanColor.getClass().getSimpleName() : "null"));
        this._callback = callback;
        init(context);
        setFanColor(fanColor);
    }

    private void init(final Context context) {
        this.mContext = context;
        this.mHandler = new Handler(Looper.getMainLooper());

        int widthInDp = 80;
        int heightInDp = 80;

        float density = getResources().getDisplayMetrics().density;
        this.desiredWidthInPx = (int) (widthInDp * density);
        this.desiredHeightInPx = (int) (heightInDp * density);

        this.cornerRadius = 8 * density; // Пример: 8dp радиус закругления
        this.clipPath = new Path();

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

        bitmapPaint = new Paint();
        bitmapPaint.setAntiAlias(true);
        bitmapPaint.setFilterBitmap(true); // Включаем фильтрацию для более гладкого масштабирования
        bitmapPaint.setDither(true);       // Включаем дизеринг для лучшего качества цвета

        colorFillPaint = new Paint();
        colorFillPaint.setStyle(Paint.Style.FILL);
        colorFillPaint.setAntiAlias(true);

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
        this.currentBitmapToDraw = null; // Сбрасываем предыдущий Bitmap

        if (mFanColor == null) {
            invalidate(); // Перерисовать с прозрачным фоном или состоянием по умолчанию
            return;
        }
        setTitle(mFanColor.getTitle());
        //setBorderColor(Color.LTGRAY);
        setBorderColor(Color.TRANSPARENT);


        if (mFanColor instanceof SolidColor) {
            //invalidate();
        } else if (mFanColor instanceof FanImage) {
            FanImage fanImage = (FanImage) mFanColor;
            loadBitmapFromAssets(fanImage.getFileName());
        } else if (mFanColor instanceof AnimatedColor) {
            AnimatedColor animatedColor = (AnimatedColor) mFanColor;
            List<Integer> ids = animatedColor.getIds();
            this.mAnimationDelay = animatedColor.getDelay() > 0 ? animatedColor.getDelay() : 1000;
            Log.d(TAG, "ADD AnimatedColor: "  + mFanColor.toString());
            if (ids != null) {
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

// FanColorView.java

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        // НЕ вызываем super.onDraw(canvas); так как мы полностью контролируем отрисовку.

        int width = getWidth();
        int height = getHeight();
        if (width == 0 || height == 0) return;

        float padding = 0f;
        drawingRect.set(padding, padding, width-padding, height-padding);
        // Создаем путь для обрезки в форме закругленного прямоугольника
        clipPath.reset(); // Сбрасываем путь перед использованием
        clipPath.addRoundRect(drawingRect, cornerRadius, cornerRadius, Path.Direction.CW);
        // Применяем обрезку. Все, что будет нарисовано дальше, будет видимо только внутри этого пути.
        canvas.clipPath(clipPath);


        padding = 10f;
        RectF contentRect = new RectF(padding, padding, width-padding, height-padding);

        if (mFanColor instanceof SolidColor) {
            int colorToDraw = ((SolidColor) mFanColor).getColor();
            colorFillPaint.setColor(colorToDraw);
            canvas.drawRoundRect(contentRect, cornerRadius, cornerRadius, colorFillPaint);

        } else if (mFanColor instanceof AnimatedColor && mCircularIntegers != null) {
            Integer animatedCurrentColor = mCircularIntegers.getCurrent();
            int colorToDraw = Color.TRANSPARENT;

            if (animatedCurrentColor != null) {
                colorToDraw = animatedCurrentColor;
            } else {
                // Запасной вариант, чтобы не было "дырки" до первого тика анимации
                colorToDraw = mCircularIntegers.getNext();
            }

            colorFillPaint.setColor(colorToDraw);
            canvas.drawRoundRect(contentRect, cornerRadius, cornerRadius, colorFillPaint);

        } else if (mFanColor instanceof FanImage && currentBitmapToDraw != null) {
            // Рисуем Bitmap на всю область, он будет обрезан по краям
            canvas.drawBitmap(currentBitmapToDraw, null, contentRect, bitmapPaint);

        } else {
            // Запасной вариант, если нет контента
            if (mFanColor == null && isAnimationRunning) {
                stopColorAnimation();
            }
            colorFillPaint.setColor(Color.TRANSPARENT);
            canvas.drawRect(contentRect, colorFillPaint);
        }


        // --- 3. Рисование контура (обводки) ---
        // Контур рисуется поверх контента. Чтобы он был виден внутри краев,
        // мы рисуем его с отступом, равным половине его толщины.
        float halfBorder = borderWidth / 2f;
        RectF borderRect = new RectF(halfBorder, halfBorder, width - halfBorder, height - halfBorder);
        // Рисуем закругленный прямоугольник, а не просто прямоугольник
        canvas.drawRoundRect(borderRect, cornerRadius, cornerRadius, borderPaint);


        // --- 4. Рисование текста ---
        // Логика рисования текста остается прежней, он будет нарисован поверх всего.
        if (titlePaint != null && titleText != null && !titleText.isEmpty()) {
            titlePaint.getTextBounds(titleText, 0, titleText.length(), textBounds);
            float textX = width / 2f;
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
        //Log.d(TAG, "setBackgroundColor called with " + String.format("#%06X", (0xFFFFFF & color)) + ", но мы рисуем цвет в onDraw.");
        // Просто вызываем invalidate, чтобы onDraw был вызван с новым цветом (если он берется из mFanColor)
        // Но лучше, чтобы цвет для отрисовки всегда брался из mFanColor.
    }

    private void loadBitmapFromAssets(String assetFileName) {
        if (assetFileName == null || assetFileName.isEmpty()) {
            Log.w(TAG, "Путь к изображению в assets пуст.");
            this.currentBitmapToDraw = null;
            invalidate();
            return;
        }

        AssetManager assetManager = mContext.getAssets();
        InputStream inputStream = null;
        try {
            inputStream = assetManager.open(assetFileName);
            // Опции для BitmapFactory, если нужно контролировать размер или конфигурацию
            // BitmapFactory.Options options = new BitmapFactory.Options();
            // options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            this.currentBitmapToDraw = BitmapFactory.decodeStream(inputStream);
            Log.d(TAG, "Bitmap " + assetFileName + " успешно загружен из assets.");
        } catch (IOException e) {
            Log.e(TAG, "Ошибка загрузки Bitmap " + assetFileName + " из assets: " + e.getMessage());
            this.currentBitmapToDraw = null; // Устанавливаем null в случае ошибки
            // Здесь можно загрузить изображение-заглушку по умолчанию, если нужно
            // this.currentBitmapToDraw = BitmapFactory.decodeResource(getResources(), R.drawable.placeholder_image);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    // Игнорируем или логируем
                }
            }
        }
        invalidate(); // Запрашиваем перерисовку после попытки загрузки Bitmap
    }


}

