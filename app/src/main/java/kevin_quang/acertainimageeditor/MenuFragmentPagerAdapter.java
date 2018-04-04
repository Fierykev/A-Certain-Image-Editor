package kevin_quang.acertainimageeditor;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class MenuFragmentPagerAdapter extends FragmentPagerAdapter {
    final int PAGE_COUNT = 3;
    private EditDisplaySurfaceView editDisplaySurfaceView;
    private Context context;

    public MenuFragmentPagerAdapter(
            FragmentManager fm,
            EditDisplaySurfaceView editDisplaySurfaceView,
            Context context) {
        super(fm);
        this.editDisplaySurfaceView = editDisplaySurfaceView;
        this.context = context;
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public Fragment getItem(int position) {
        switch(position) {
            case 0:
                return LoadFragment.newInstance(editDisplaySurfaceView);
            case 1:
                return FilterFragment.newInstance(editDisplaySurfaceView);
            case 2:
                return BrushFragment.newInstance(editDisplaySurfaceView);
            default:
                return null;
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return null;
    }
}
