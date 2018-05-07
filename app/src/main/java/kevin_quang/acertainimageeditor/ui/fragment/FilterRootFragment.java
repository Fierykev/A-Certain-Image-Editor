package kevin_quang.acertainimageeditor.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import kevin_quang.acertainimageeditor.ui.view.EditDisplaySurfaceView;
import kevin_quang.acertainimageeditor.R;

public class FilterRootFragment extends Fragment {

    private EditDisplaySurfaceView editDisplaySurfaceView;

    public static FilterRootFragment newInstance(
            EditDisplaySurfaceView editDisplaySurfaceView
    ) {

        Bundle args = new Bundle();

        FilterRootFragment fragment = new FilterRootFragment();
        fragment.editDisplaySurfaceView = editDisplaySurfaceView;
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        /* Inflate the layout for this fragment */
        View view = inflater.inflate(R.layout.filter_root, container, false);

        FragmentTransaction transaction = getFragmentManager().beginTransaction();

        /*
         * When this container fragment is created, we fill it with our first
         * "real" fragment
         */
        transaction.replace(R.id.filter_root, FilterFragment.newInstance(editDisplaySurfaceView));

        transaction.commit();

        return view;
    }
}
