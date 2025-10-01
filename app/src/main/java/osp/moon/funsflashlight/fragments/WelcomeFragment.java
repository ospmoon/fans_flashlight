package osp.moon.funsflashlight.fragments;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import static osp.moon.funsflashlight.AppConstants.ANIMATED_COLOR;
import static osp.moon.funsflashlight.AppConstants.FAN_IMAGE;
import static osp.moon.funsflashlight.AppConstants.SOLID_COLOR;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Random;

import osp.moon.funsflashlight.R;
import osp.moon.funsflashlight.customobjects.AnimatedColor;
import osp.moon.funsflashlight.customobjects.FanCollection;
import osp.moon.funsflashlight.customobjects.FanColor;
import osp.moon.funsflashlight.customobjects.FanImage;
import osp.moon.funsflashlight.customobjects.SolidColor;
import osp.moon.funsflashlight.customviews.FanColorView;
import osp.moon.funsflashlight.database.AppDatabase;
import osp.moon.funsflashlight.helpers.CircularIntegers;
import osp.moon.funsflashlight.helpers.PrefHelper;

public class WelcomeFragment extends Fragment implements FanColorView.OnFanColorClickListener {

    private final String TAG = WelcomeFragment.class.getName();
    private View mColorView;
    private ImageView mImageView;
    private Handler mHandler;
    private Runnable mColorChangeRunnable;
    private FanColor mCurrentFanColor;
    private boolean isAttached = false;
    private boolean isFragmentVisible = false;
    private CircularIntegers mCircularIntegers;
    private int mAnimationDelay = 1000;
    private boolean isAnimationRunning = false;
    private LinearLayout mScrollContainerBottom;
    private LinearLayout mScrollContainerLeft;
    private LinearLayout mScrollContainerRight;
    private RelativeLayout mBottomView;
    private RelativeLayout mLeftView;
    private RelativeLayout mRightView;
    private SeekBar mDelaySeekBar;
    private boolean isPanelsVisible = false;
    private boolean arePanelsReadyForAnimation = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate()");
        mHandler = new Handler(Looper.getMainLooper());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView()");
        View root = inflater.inflate(R.layout.fragment_welcome, container, false);

        init(root);
        fillPanels(AppDatabase.getCollectionList(requireContext()));

        int lastCollectionId = PrefHelper.readLastSavedCollectionId(requireContext());
        int lastColorId = PrefHelper.readLastSavedColorId(requireContext());
        if (lastCollectionId != -1 && lastColorId != -1 ) {
            setCurrentFanColor(AppDatabase.getFanColor(requireContext(), lastCollectionId, lastColorId));
        } else {
            setCurrentFanColor(AppDatabase.getAnimatedColor(requireContext(), 3));
        }
        return root;
    }

    private void setCurrentFanColor(FanColor fanColor) {
        Log.d(TAG, "setCurrentFanColor()): " + fanColor.getClass().getSimpleName() + ", " + fanColor.toString());
        mCurrentFanColor = fanColor;
        if (mCurrentFanColor instanceof SolidColor) {
            mDelaySeekBar.setVisibility(INVISIBLE);
            isAnimationRunning = false;
            mColorView.setBackgroundColor(((SolidColor) mCurrentFanColor).getColor());
            mColorView.setVisibility(VISIBLE);
            mColorView.invalidate();
            mImageView.setVisibility(GONE);
        } else if (mCurrentFanColor instanceof AnimatedColor) {
            AnimatedColor animatedColor = (AnimatedColor) mCurrentFanColor;
            mDelaySeekBar.setVisibility(VISIBLE);
            mColorView.setVisibility(VISIBLE);
            mAnimationDelay = animatedColor.getDelay() > 0 ? animatedColor.getDelay() : 1000;
            mDelaySeekBar.setProgress(mAnimationDelay);
            List<Integer> ids = animatedColor.getIds();
            if (ids != null) {
                mCircularIntegers = new CircularIntegers(requireContext(), ids);
                Log.d(TAG, "Set AnimatedColor with " + ids.size() + " colors and delay " + this.mAnimationDelay + "ms.");
                conditionallyStartAnimation();
            }
            mImageView.setVisibility(GONE);
        } else if (mCurrentFanColor instanceof FanImage) {
            mDelaySeekBar.setVisibility(INVISIBLE);
            isAnimationRunning = false;
            mImageView.setVisibility(VISIBLE);
            FanImage fanImage = (FanImage) mCurrentFanColor;
            loadBitmapFromAssets(fanImage.getFileName());
            mColorView.setVisibility(GONE);
        }

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Log.i(TAG, "onAttach()");
        isAttached = true;
        conditionallyStartAnimation();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.i(TAG, "onDetach()");
        isAttached = false;
        stopColorAnimation();
    }

    private void conditionallyStartAnimation() {
        Log.d(TAG, "conditionallyStartAnimation called");
        if (mCurrentFanColor != null
                && isFragmentVisible
                && isAttached
                && mCurrentFanColor instanceof AnimatedColor
                && mCircularIntegers != null
                && !isAnimationRunning) {
            startColorAnimate();
        }
    }

    private void startColorAnimate() {
        Log.d(TAG, "startColorAnimateInternal called, color: " + mCurrentFanColor.toString());
        if (mCircularIntegers == null || isAnimationRunning || mColorView == null) {
            return;
        }
        mColorChangeRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isFragmentVisible
                        || !isAttached
                        || !isAnimationRunning
                        || mColorView == null) {
                    if (isAnimationRunning) {
                        stopColorAnimation();
                    }
                    return;
                }

                Integer nextColor = mCircularIntegers.getNext();
                mColorView.setBackgroundColor(nextColor);
                mColorView.invalidate();
                mHandler.postDelayed(this, mAnimationDelay);
            }
        };

        Integer firstColor = mCircularIntegers.getCurrent();
        if (firstColor == null) {
            firstColor = mCircularIntegers.getNext();
        }
        mColorView.setBackgroundColor(firstColor);

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
    public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume()");
        this.isFragmentVisible = true;
        conditionallyStartAnimation();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG, "onPause()");
        this.isFragmentVisible = false;
        stopColorAnimation();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy()");
        stopColorAnimation();
    }

    private void loadBitmapFromAssets(String assetFileName) {
        if (mImageView == null || assetFileName == null || assetFileName.isEmpty()) {
            return;
        }

        AssetManager assetManager = requireContext().getAssets();
        InputStream inputStream = null;
        try {
            inputStream = assetManager.open(assetFileName);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            mImageView.setImageBitmap(bitmap);
            Log.d(TAG, "Bitmap " + assetFileName + " успешно загружен из assets.");
        } catch (IOException e) {
            Log.e(TAG, "Ошибка загрузки Bitmap " + assetFileName + " из assets: ", e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    // Игнорируем или логируем
                }
            }
        }
    }

    private void init(View root) {
        Log.i(TAG, "init()");
        RelativeLayout relativeLayout = root.findViewById(R.id.root_view);
        relativeLayout.setOnClickListener(view -> changePanelsVisibility());

        mDelaySeekBar = root.findViewById(R.id.seekBar);
        mDelaySeekBar.setMax(2000);
        mDelaySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mAnimationDelay = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        mDelaySeekBar.setVisibility(INVISIBLE);

        mBottomView = root.findViewById(R.id.bottomView);
        mLeftView = root.findViewById(R.id.leftView);
        mRightView = root.findViewById(R.id.rightView);

        mRightView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Убираем слушатель, чтобы он не срабатывал повторно
                mRightView.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                // Теперь getWidth() и getHeight() вернут правильные значения.
                // Устанавливаем начальное положение панелей за экраном.
                mRightView.setTranslationX(mRightView.getWidth());
                mLeftView.setTranslationX(-mLeftView.getWidth());
                mBottomView.setTranslationY(mBottomView.getHeight());

                arePanelsReadyForAnimation = true;
                Log.d(TAG, "Панели готовы к анимации. Размеры (RightView): " + mRightView.getWidth());
            }
        });

        mBottomView.setVisibility(INVISIBLE);
        mLeftView.setVisibility(INVISIBLE);
        mRightView.setVisibility(INVISIBLE);

        mScrollContainerBottom = root.findViewById(R.id.scrollContainerBottom);
        mScrollContainerLeft = root.findViewById(R.id.scrollContainerLeft);
        mScrollContainerRight = root.findViewById(R.id.scrollContainerRight);

        mColorView = root.findViewById(R.id.color_view);
        mColorView.setVisibility(GONE);

        mImageView = root.findViewById(R.id.imageView);
        mImageView.setVisibility(GONE);
    }

    private void changePanelsVisibility() {
        Log.d(TAG, "changePanelsVisibility called. isPanelsVisible was: " + isPanelsVisible);
        long animationDuration = 300;
        if (!arePanelsReadyForAnimation) {
            Log.w(TAG, "Панели еще не готовы к анимации, клик проигнорирован.");
            return;
        }

        if (isPanelsVisible) {
            mLeftView.animate().translationX(-mLeftView.getWidth()).setDuration(animationDuration).withEndAction(() -> mLeftView.setVisibility(INVISIBLE));
            mRightView.animate().translationX(mRightView.getWidth()).setDuration(animationDuration).withEndAction(() -> mRightView.setVisibility(INVISIBLE));
            mBottomView.animate().translationY(mBottomView.getHeight()).setDuration(animationDuration).withEndAction(() -> mBottomView.setVisibility(INVISIBLE));
            isPanelsVisible = false;
        } else {
            Log.d(TAG, "Animating panels IN");
            mLeftView.setVisibility(View.VISIBLE);
            mRightView.setVisibility(View.VISIBLE);
            mBottomView.setVisibility(View.VISIBLE);
            mLeftView.animate().translationX(0).setDuration(animationDuration);
            mRightView.animate().translationX(0).setDuration(animationDuration);
            mBottomView.animate().translationY(0).setDuration(animationDuration);
            isPanelsVisible = true;
        }
    }

    private void fillPanels(List<FanCollection> collectionList) {
        Log.d(TAG, "fillPanels called with: " + collectionList);
        mScrollContainerBottom.removeAllViews();
        mScrollContainerLeft.removeAllViews();
        mScrollContainerRight.removeAllViews();

        for (FanCollection collection : collectionList) {
            if (collection.getId() == SOLID_COLOR) {
                Log.d(TAG, "collection.getId() == 1");
                for (FanColor color : collection.getColorList()) {
                    FanColorView fanColorView = new FanColorView(requireContext(), color, this);
                    mScrollContainerRight.addView(fanColorView);
                }
            } else if (collection.getId() == ANIMATED_COLOR) {
                Log.d(TAG, "collection.getId() == 2");
                for (FanColor color : collection.getColorList()) {
                    FanColorView fanColorView = new FanColorView(requireContext(), color, this);
                    mScrollContainerBottom.addView(fanColorView);
                }
            } else if (collection.getId() == FAN_IMAGE) {
                Log.d(TAG, "collection.getId() == 3");
                for (FanColor color : collection.getColorList()) {
                    FanColorView fanColorView = new FanColorView(requireContext(), color, this);
                    mScrollContainerLeft.addView(fanColorView);
                }
            }
        }
    }

    @Override
    public void onFanColorClicked(FanColor fanColor) {
        if (fanColor == null) return;
        Log.d(TAG, "onFanColorClicked called with: " + fanColor.getClass().getSimpleName());
        Log.d(TAG, "FanColor: " + fanColor);
        setCurrentFanColor(fanColor);
        changePanelsVisibility();
        PrefHelper.storeColorId(requireContext(), fanColor.getId());
        PrefHelper.storeCollectionId(requireContext(), fanColor.getCollectionId());
    }

}
