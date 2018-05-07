package kevin_quang.acertainimageeditor.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import kevin_quang.acertainimageeditor.tool.BrushTool;
import kevin_quang.acertainimageeditor.tool.ImageDrawTool;
import kevin_quang.acertainimageeditor.tool.TextTool;
import kevin_quang.acertainimageeditor.ui.toggle.Toggler;
import kevin_quang.acertainimageeditor.ui.view.EditDisplaySurfaceView;
import kevin_quang.acertainimageeditor.R;
import kevin_quang.acertainimageeditor.ui.dialog.ColorPickerDialog;

public class BrushFragment extends Fragment {

    private EditDisplaySurfaceView editDisplaySurfaceView;
    private ImageButton brush, text;
    private String brushTag = "brush_brush";
    private String textTag = "brush_text";

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

        brush = view.findViewById(R.id.brush);
        Toggler.add(brushTag, brush);
        text = view.findViewById(R.id.text);
        Toggler.add(textTag, text);
        brush.setOnClickListener(v -> {
            if(!Toggler.toggle(brushTag)) {
                editDisplaySurfaceView.setTool(new BrushTool());
            } else {
                editDisplaySurfaceView.setTool(new ImageDrawTool());
            }
        });

        text.setOnClickListener(v -> {
            if(!Toggler.toggle(textTag)) {
                editDisplaySurfaceView.setTool(new TextTool(editDisplaySurfaceView, getActivity()));
            } else {
                editDisplaySurfaceView.setTool(new ImageDrawTool());
            }
        });

        ImageButton select_color = view.findViewById(R.id.select_color);
        select_color.setOnClickListener(v -> {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            Fragment prev = getFragmentManager().findFragmentByTag("dialog");
            if (prev != null) {
                ft.remove(prev);
            }
            ft.addToBackStack(null);

            // Create and show the dialog.
            DialogFragment newFragment = ColorPickerDialog.newInstance(editDisplaySurfaceView);
            newFragment.show(ft, "dialog");
        });

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toggler.remove(brushTag);
        Toggler.remove(textTag);
    }
}
