package kevin_quang.acertainimageeditor.ui.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import kevin_quang.acertainimageeditor.tool.LiquifyTool;
import kevin_quang.acertainimageeditor.ui.view.EditDisplaySurfaceView;
import kevin_quang.acertainimageeditor.R;
import kevin_quang.acertainimageeditor.tool.ShoeTool;
import kevin_quang.acertainimageeditor.tool.Tool;

public class FilterFragment extends Fragment {

    private EditDisplaySurfaceView editDisplaySurfaceView;

    public static FilterFragment newInstance(
            EditDisplaySurfaceView editDisplaySurfaceView
    ) {
        Bundle args = new Bundle();
        FilterFragment fragment = new FilterFragment();
        fragment.editDisplaySurfaceView = editDisplaySurfaceView;
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.filter, container, false);

        final ImageButton undo = view.findViewById(R.id.undo);
        undo.setColorFilter(editDisplaySurfaceView.canUndo() ? Color.WHITE : Color.DKGRAY);
        undo.setOnClickListener(v -> editDisplaySurfaceView.undo());

        final ImageButton redo = view.findViewById(R.id.redo);
        redo.setColorFilter(editDisplaySurfaceView.canRedo() ? Color.WHITE : Color.DKGRAY);
        redo.setOnClickListener(v -> editDisplaySurfaceView.redo());

        Tool.historyUpdate = () -> getActivity().runOnUiThread(() -> {
            undo.setColorFilter(editDisplaySurfaceView.canUndo() ? Color.WHITE : Color.DKGRAY);
            redo.setColorFilter(editDisplaySurfaceView.canRedo() ? Color.WHITE : Color.DKGRAY);
        });

        final Fragment that = this;

        ImageButton resize = view.findViewById(R.id.resize);
        resize.setOnClickListener(v -> getFragmentManager().beginTransaction()
                .replace(R.id.filter_root, ScaleResizeFragment.newInstance(editDisplaySurfaceView))
                .addToBackStack(null)
                .commit());

        ImageButton shoe = view.findViewById(R.id.shoe);
        shoe.setOnClickListener(v -> {
            ShoeTool shoeTool = new ShoeTool();
            editDisplaySurfaceView.setTool(shoeTool);

            Tool.Args args = new Tool.Args();
            args.type = ShoeTool.RUN;

            editDisplaySurfaceView.passArgs(args);
        });

        ImageButton liquify = view.findViewById(R.id.liquify);
        liquify.setOnClickListener(v -> {
            LiquifyTool liquifyTool = new LiquifyTool();
            editDisplaySurfaceView.setTool(liquifyTool);
        });

        ImageButton rotateLeft = view.findViewById(R.id.rotate_left);
        rotateLeft.setOnClickListener(v -> editDisplaySurfaceView.rotate(-90));

        ImageButton rotateRight = view.findViewById(R.id.rotate_right);
        rotateRight.setOnClickListener(v -> editDisplaySurfaceView.rotate(90));

        return view;
    }

}
