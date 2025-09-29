package osp.moon.funsflashlight.fragments;

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

import java.util.Random;

import osp.moon.funsflashlight.R;

public class WelcomeFragment extends Fragment {

    private final String TAG = WelcomeFragment.class.getName();
    private View colorView;
    private Handler handler;
    private Runnable colorChangeRunnable;
    private Random random = new Random();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate()");
        handler = new Handler(Looper.getMainLooper());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView()");
        View root = inflater.inflate(R.layout.fragment_welcome, container, false);
        colorView = root.findViewById(R.id.color_view);
        return root;
    }

    private void startColorChange() {
        colorChangeRunnable = new Runnable() {
            @Override
            public void run() {
                // Генерируем случайный цвет
                int red = random.nextInt(256);
                int green = random.nextInt(256);
                int blue = random.nextInt(256);
                int randomColor = Color.rgb(red, green, blue);

                if (colorView != null) {
                    colorView.setBackgroundColor(randomColor);
                }
                long delayMillis = 100 + random.nextInt(10);
                handler.postDelayed(this, delayMillis);
            }
        };
        handler.post(colorChangeRunnable);
    }

    private void stopColorChange() {
        if (handler != null && colorChangeRunnable != null) {
            handler.removeCallbacks(colorChangeRunnable);
        }
    }

    private void openServerFragment() {
        Log.w(TAG, "openServerFragment()");
        //Navigation.findNavController(requireActivity(), R.id.nav_host_fragment).navigate(R.id.serverFragment);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume()");
        startColorChange();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG, "onPause()");
        stopColorChange();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy()");
        stopColorChange();
    }
}
