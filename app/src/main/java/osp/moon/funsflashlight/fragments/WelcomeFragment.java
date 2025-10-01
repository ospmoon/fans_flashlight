package osp.moon.funsflashlight.fragments;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

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
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Random;

import osp.moon.funsflashlight.R;
import osp.moon.funsflashlight.customobjects.AnimatedColor;
import osp.moon.funsflashlight.customobjects.FanColor;
import osp.moon.funsflashlight.customobjects.FanImage;
import osp.moon.funsflashlight.customobjects.SolidColor;
import osp.moon.funsflashlight.database.AppDatabase;
import osp.moon.funsflashlight.helpers.CircularIntegers;
import osp.moon.funsflashlight.helpers.PrefHelper;

public class WelcomeFragment extends Fragment {

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
        mColorView = root.findViewById(R.id.color_view);
        mColorView.setVisibility(GONE);

        mImageView = root.findViewById(R.id.imageView);
        mImageView.setVisibility(GONE);

        if (getArguments() != null && getArguments().containsKey("fanColor")) {
            mCurrentFanColor = (FanColor) getArguments().getSerializable("fanColor");
            if (mCurrentFanColor != null) {
                Log.i(TAG, "Received fanColor: " + mCurrentFanColor.getClass().getSimpleName() + ", " + mCurrentFanColor);
            }
        } else {
            int lastCollectionId = PrefHelper.readLastSavedCollectionId(requireContext());
            int lastColorId = PrefHelper.readLastSavedColorId(requireContext());
            if (lastCollectionId != -1 && lastColorId != -1 ) {
                mCurrentFanColor = AppDatabase.getFanColor(requireContext(), lastCollectionId, lastColorId);
            } else {
                mCurrentFanColor = AppDatabase.getAnimatedColor(requireContext(), 3);
            }

        }

        if (mCurrentFanColor instanceof SolidColor) {
            mColorView.setBackgroundColor(((SolidColor) mCurrentFanColor).getColor());
            mColorView.setVisibility(VISIBLE);
        } else if (mCurrentFanColor instanceof AnimatedColor) {
            mColorView.setVisibility(VISIBLE);
            AnimatedColor animatedColor = (AnimatedColor) mCurrentFanColor;
            this.mAnimationDelay = animatedColor.getDelay() > 0 ? animatedColor.getDelay() : 1000;
            List<Integer> ids = animatedColor.getIds();
            if (ids != null) {
                mCircularIntegers = new CircularIntegers(requireContext(), ids);
                Log.d(TAG, "Set AnimatedColor with " + ids.size() + " colors and delay " + this.mAnimationDelay + "ms.");
                conditionallyStartAnimation();
            }
        } else if (mCurrentFanColor instanceof FanImage) {
            mImageView.setVisibility(VISIBLE);
            FanImage fanImage = (FanImage) mCurrentFanColor;
            loadBitmapFromAssets(fanImage.getFileName());
        }
        return root;
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

}
