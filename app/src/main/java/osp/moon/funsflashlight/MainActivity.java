package osp.moon.funsflashlight;

import static android.view.View.GONE;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentContainerView;
import androidx.navigation.Navigation;

import java.util.List;

import osp.moon.funsflashlight.customobjects.FanCollection;
import osp.moon.funsflashlight.customobjects.FanColor;
import osp.moon.funsflashlight.customviews.FanColorView;
import osp.moon.funsflashlight.database.AppDatabase;

public class MainActivity extends BaseActivity implements FanColorView.OnFanColorClickListener {

    private final String TAG = MainActivity.class.getName();
    private FragmentContainerView mNavHostFragment;
    private LinearLayout mScrollContainerBottom;
    private LinearLayout mScrollContainerLeft;
    private LinearLayout mScrollContainerRight;
    private RelativeLayout mBottomView;
    private RelativeLayout mLeftView;
    private RelativeLayout mRightView;
    private boolean isPanelsVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i(TAG, "onCreate()");

        init();

        fillPanels(AppDatabase.getCollectionList(this));
    }

    private void init() {
        Log.i(TAG, "init()");
        mNavHostFragment = findViewById(R.id.nav_host_fragment);
        mNavHostFragment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changePanelsVisibility();
            }
        });
        mBottomView = findViewById(R.id.bottomView);
        mBottomView.setVisibility(GONE);
        mLeftView = findViewById(R.id.leftView);
        mLeftView.setVisibility(GONE);
        mRightView = findViewById(R.id.rightView);
        mRightView.setVisibility(GONE);
        mScrollContainerBottom = findViewById(R.id.scrollContainerBottom);
        mScrollContainerLeft = findViewById(R.id.scrollContainerLeft);
        mScrollContainerRight = findViewById(R.id.scrollContainerRight);

    }

    private void changePanelsVisibility() {
        Log.d(TAG, "changePanelsVisibility called. isPanelsVisible now: " + isPanelsVisible);
        if (isPanelsVisible) {
            mBottomView.setVisibility(GONE);
            mLeftView.setVisibility(GONE);
            mRightView.setVisibility(GONE);
            isPanelsVisible = false;
        } else {
            mBottomView.setVisibility(View.VISIBLE);
            mLeftView.setVisibility(View.VISIBLE);
            mRightView.setVisibility(View.VISIBLE);
            isPanelsVisible = true;
        }
    }

    private void fillPanels(List<FanCollection> collectionList) {
        Log.d(TAG, "fillPanels called with: " + collectionList);
        mScrollContainerBottom.removeAllViews();
        mScrollContainerLeft.removeAllViews();
        mScrollContainerRight.removeAllViews();

        for (FanCollection collection : collectionList) {
            if (collection.getId() == 1) {
                Log.d(TAG, "collection.getId() == 1");
                for (FanColor color : collection.getColorList()) {
                    FanColorView fanColorView = new FanColorView(this, color);
                    //mScrollContainerLeft.addView(fanColorView);
                    mScrollContainerBottom.addView(fanColorView);
                }
            } else if (collection.getId() == 2) {
                Log.d(TAG, "collection.getId() == 2");
                for (FanColor color : collection.getColorList()) {
                    FanColorView fanColorView = new FanColorView(this, color);
                    //mScrollContainerBottom.addView(fanColorView);
                    mScrollContainerRight.addView(fanColorView);
                }
            } else if (collection.getId() == 3) {
                Log.d(TAG, "collection.getId() == 3");
                for (FanColor color : collection.getColorList()) {
                    FanColorView fanColorView = new FanColorView(this, color);
                    //mScrollContainerRight.addView(fanColorView);
                }
            }
        }
    }

    private void sendColorToFragment(FanColor fanColor) {
        Log.w(TAG, "sendColorToFragment()");
        Bundle bundle = new Bundle();
        bundle.putSerializable("fanColor", fanColor);
        Navigation.findNavController(this, R.id.nav_host_fragment).navigate(R.id.welcomeFragment, bundle);
    }

    @Override
    public void onFanColorClicked(FanColor fanColor) {
        Log.d(TAG, "onFanColorClicked called with: " + (fanColor != null ? fanColor.getClass().getSimpleName() : "null"));
        Log.d(TAG, "FanColor: " + (fanColor != null ? fanColor.toString() : "null"));
        sendColorToFragment(fanColor);
    }
}
