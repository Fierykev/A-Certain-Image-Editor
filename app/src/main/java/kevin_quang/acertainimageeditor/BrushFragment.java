package kevin_quang.acertainimageeditor;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

public class BrushFragment extends Fragment {
    private EditDisplaySurfaceView editDisplaySurfaceView;

    public static BrushFragment newInstance(
            EditDisplaySurfaceView editDisplaySurfaceView
    ) {
        Bundle args = new Bundle();
        BrushFragment fragment = new BrushFragment();
        fragment.editDisplaySurfaceView = editDisplaySurfaceView;
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.brush, container, false);

        ImageButton select_color = view.findViewById(R.id.select_color);
        select_color.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                Fragment prev = getFragmentManager().findFragmentByTag("dialog");
                if (prev != null) {
                    ft.remove(prev);
                }
                ft.addToBackStack(null);

                // Create and show the dialog.
                DialogFragment newFragment = ColorPickerDialog.newInstance();
                newFragment.show(ft, "dialog");
            }
        });

        return view;
    }
}
