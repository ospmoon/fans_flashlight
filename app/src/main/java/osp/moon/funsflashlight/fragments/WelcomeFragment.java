package osp.moon.funsflashlight.fragments;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.List;
import java.util.Random;

import osp.moon.funsflashlight.R;
import osp.moon.funsflashlight.customobjects.AnimatedColor;
import osp.moon.funsflashlight.customobjects.FanColor;
import osp.moon.funsflashlight.customobjects.SolidColor;
import osp.moon.funsflashlight.helpers.CircularIntegers;

public class WelcomeFragment extends Fragment {

    private final String TAG = WelcomeFragment.class.getName();
    private View mColorView;
    private Handler mHandler;
    private Runnable mColorChangeRunnable;
    private Random random = new Random();
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

        if (getArguments() != null && getArguments().containsKey("fanColor")) {
            mCurrentFanColor = (FanColor) getArguments().getSerializable("fanColor");
            if (mCurrentFanColor != null) {
                Log.i(TAG, "Received fanColor: " + mCurrentFanColor.getClass().getSimpleName() + ", " + mCurrentFanColor);
            }
        }

        if (mCurrentFanColor instanceof SolidColor) {
            mColorView.setBackgroundColor(((SolidColor) mCurrentFanColor).getColor());
        } else if (mCurrentFanColor instanceof AnimatedColor) {
            AnimatedColor animatedColor = (AnimatedColor) mCurrentFanColor;
            this.mAnimationDelay = animatedColor.getDelay() > 0 ? animatedColor.getDelay() : 1000;
            List<Integer> ids = animatedColor.getIds();
            if (ids != null && !ids.isEmpty()) {
                mCircularIntegers = new CircularIntegers(requireContext(), ids);
                Log.d(TAG, "Set AnimatedColor with " + ids.size() + " colors and delay " + this.mAnimationDelay + "ms.");
                conditionallyStartAnimation();
            }
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

    private boolean isAnimatedColor() {
        return mCurrentFanColor instanceof AnimatedColor;
    }
}
