package osp.moon.funsflashlight;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;

import static osp.moon.funsflashlight.AppConstants.ANIMATED_COLOR;
import static osp.moon.funsflashlight.AppConstants.FAN_IMAGE;
import static osp.moon.funsflashlight.AppConstants.SOLID_COLOR;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentContainerView;
import androidx.navigation.Navigation;

import java.util.List;

import osp.moon.funsflashlight.customobjects.FanCollection;
import osp.moon.funsflashlight.customobjects.FanColor;
import osp.moon.funsflashlight.customviews.FanColorView;
import osp.moon.funsflashlight.database.AppDatabase;
import osp.moon.funsflashlight.helpers.PrefHelper;

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
    private boolean arePanelsReadyForAnimation = false; // Новый флаг готовности

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_main);
        Log.i(TAG, "onCreate()");

        init();

        fillPanels(AppDatabase.getCollectionList(this));
    }

    private void init() {
        Log.i(TAG, "init()");
        mNavHostFragment = findViewById(R.id.nav_host_fragment);
        mNavHostFragment.setOnClickListener(view -> changePanelsVisibility());

        mBottomView = findViewById(R.id.bottomView);
        mLeftView = findViewById(R.id.leftView);
        mRightView = findViewById(R.id.rightView);

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

        mScrollContainerBottom = findViewById(R.id.scrollContainerBottom);
        mScrollContainerLeft = findViewById(R.id.scrollContainerLeft);
        mScrollContainerRight = findViewById(R.id.scrollContainerRight);
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
                    FanColorView fanColorView = new FanColorView(this, color);
                    mScrollContainerRight.addView(fanColorView);
                }
            } else if (collection.getId() == ANIMATED_COLOR) {
                Log.d(TAG, "collection.getId() == 2");
                for (FanColor color : collection.getColorList()) {
                    FanColorView fanColorView = new FanColorView(this, color);
                    mScrollContainerBottom.addView(fanColorView);
                }
            } else if (collection.getId() == FAN_IMAGE) {
                Log.d(TAG, "collection.getId() == 3");
                for (FanColor color : collection.getColorList()) {
                    FanColorView fanColorView = new FanColorView(this, color);
                    mScrollContainerLeft.addView(fanColorView);
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
        if (fanColor == null) return;
        Log.d(TAG, "onFanColorClicked called with: " + fanColor.getClass().getSimpleName());
        Log.d(TAG, "FanColor: " + fanColor);
        sendColorToFragment(fanColor);
        changePanelsVisibility();
        PrefHelper.storeColorId(getApplicationContext(), fanColor.getId());
        PrefHelper.storeCollectionId(getApplicationContext(), fanColor.getCollectionId());
    }
}
