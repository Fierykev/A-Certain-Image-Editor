package kevin_quang.acertainimageeditor.ui;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;


import kevin_quang.acertainimageeditor.R;
import kevin_quang.acertainimageeditor.ui.toggle.Toggler;
import kevin_quang.acertainimageeditor.ui.view.EditDisplaySurfaceView;
import kevin_quang.acertainimageeditor.ui.tab.MenuFragmentPagerAdapter;

public class MainActivity extends AppCompatActivity {

    BaseLoaderCallback openCVCallback;

    public static MainActivity singleton;

    public interface KeyListener {
        void onKeyUp(int keyCode, KeyEvent event);
    }

    private EditDisplaySurfaceView editDisplaySurfaceView;
    private boolean canTouch = true;
    private KeyListener keyListener;
    private ProgressBar progressBar;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        singleton = this;
        createDisplay(savedInstanceState);
        progressBar = findViewById(R.id.progressBar);

        final ActivityManager activityManager =
                (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        final ConfigurationInfo configurationInfo =
                activityManager.getDeviceConfigurationInfo();

        // TODO: CHECK ANDROID VERSION HERE
        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }

        progressBar.setVisibility(View.GONE);

        // wait for OpenCV to load
        openCVCallback = new BaseLoaderCallback(this) {
            @Override
            public void onManagerConnected(int status) {
                switch (status) {
                    case LoaderCallbackInterface.SUCCESS:
                    {
                        Log.i("OpenCV", "OpenCV loaded successfully");

                        progressBar.setVisibility(View.GONE);
                    } break;
                    default:
                    {
                        super.onManagerConnected(status);
                    } break;
                }
            }
        };

        if (!OpenCVLoader.initDebug()) {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, openCVCallback);
        } else {
            Log.d("OpenCV", "OpenCV library found inside package.");
            openCVCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void progress() {
        runOnUiThread(() -> {
            progressBar.setVisibility(View.VISIBLE);
            canTouch = true;
        });
    }

    public void finished() {
        runOnUiThread(() -> {
            progressBar.setVisibility(View.GONE);
            canTouch = false;
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(canTouch) {
            super.onTouchEvent(event);
        }
        return false;
    }

    private void createDisplay(Bundle savedInstanceState)
    {
        setContentView(R.layout.activity_main);

        editDisplaySurfaceView = findViewById(R.id.mainEditor);
        // Get the ViewPager and set it's PagerAdapter so that it can display items
        ViewPager viewPager = findViewById(R.id.viewpager);
        viewPager.setAdapter(new MenuFragmentPagerAdapter(
                getSupportFragmentManager(),
                editDisplaySurfaceView,
                MainActivity.this));

        // Give the TabLayout the ViewPager
        TabLayout tabLayout = findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);

        // Icons
        int[] imageResId = {
                R.drawable.ic_insert_photo_white_48dp,
                R.drawable.ic_photo_filter_white_48dp,
                R.drawable.ic_brush_white_24dp
        };

        for (int i = 0; i < imageResId.length; i++) {
            tabLayout.getTabAt(i).setIcon(imageResId[i]);
        }
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Toggler.toggle(null);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        /*
        if(savedInstanceState != null) {
            editDisplaySurfaceView.restore(savedInstanceState);
        }*/
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        //editDisplaySurfaceView.save(outState);
    }

    public void setKeyListener(KeyListener keyListener) {
        this.keyListener = keyListener;
    }

    public void clearKeyListener() {
        this.keyListener = null;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if(keyListener != null) {
            keyListener.onKeyUp(keyCode, event);
        }
        return super.onKeyUp(keyCode, event);
    }
}
