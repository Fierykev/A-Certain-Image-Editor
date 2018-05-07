package kevin_quang.acertainimageeditor.ui.tab;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import kevin_quang.acertainimageeditor.R;
import kevin_quang.acertainimageeditor.ui.MainActivity;
import kevin_quang.acertainimageeditor.ui.view.EditDisplaySurfaceView;
import kevin_quang.acertainimageeditor.ui.fragment.BrushFragment;
import kevin_quang.acertainimageeditor.ui.fragment.FilterRootFragment;
import kevin_quang.acertainimageeditor.ui.fragment.LoadFragment;

public class MenuFragmentPagerAdapter extends FragmentPagerAdapter {
    final int PAGE_COUNT = 3;
    private EditDisplaySurfaceView editDisplaySurfaceView;
    private Context context;
    private Fragment[] fragments = new Fragment[PAGE_COUNT];
    private FragmentManager fragmentManager;

    public MenuFragmentPagerAdapter(
            FragmentManager fm,
            EditDisplaySurfaceView editDisplaySurfaceView,
            Context context) {
        super(fm);
        this.fragmentManager = fm;
        this.editDisplaySurfaceView = editDisplaySurfaceView;
        this.context = context;
        fragments[0] = LoadFragment.newInstance(editDisplaySurfaceView);
        fragments[1] = FilterRootFragment.newInstance(editDisplaySurfaceView);//FilterFragment.newInstance(editDisplaySurfaceView);
        fragments[2] = BrushFragment.newInstance(editDisplaySurfaceView);
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public Fragment getItem(int position) {
        MainActivity.singleton.findViewById(R.id.root).requestFocus();
        switch(position) {
            case 0:
                return fragments[0];
            case 1:
                return fragments[1];
            case 2:
                return fragments[2];
            default:
                return null;
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return null;
    }
}
