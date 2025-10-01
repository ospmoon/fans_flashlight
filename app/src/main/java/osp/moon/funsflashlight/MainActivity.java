package osp.moon.funsflashlight;

import android.os.Bundle;
import android.util.Log;

import androidx.core.view.WindowCompat;

public class MainActivity extends BaseActivity {

    private final String TAG = MainActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i(TAG, "onCreate()");
    }
}
