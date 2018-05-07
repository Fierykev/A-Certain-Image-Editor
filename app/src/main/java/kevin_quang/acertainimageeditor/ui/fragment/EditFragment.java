package kevin_quang.acertainimageeditor.ui.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import kevin_quang.acertainimageeditor.ui.view.EditDisplaySurfaceView;

/**
 * Created by Kevin on 3/24/2018.
 */

public class EditFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return new EditDisplaySurfaceView(getContext());
    }
}
