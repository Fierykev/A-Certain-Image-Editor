package kevin_quang.acertainimageeditor;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import java.io.FileOutputStream;

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
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public Fragment getItem(int position) {
        switch(position) {
            case 0:
                if(fragments[0] == null) {
                    fragments[0] = LoadFragment.newInstance(editDisplaySurfaceView);
                }
                return fragments[0];
            case 1:
                if(fragments[1] == null) {
                    fragments[1] = FilterRootFragment.newInstance(editDisplaySurfaceView);//FilterFragment.newInstance(editDisplaySurfaceView);
                }
                return fragments[1];
            case 2:
                if(fragments[2] == null) {
                    fragments[2] = BrushFragment.newInstance(editDisplaySurfaceView);
                }
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
