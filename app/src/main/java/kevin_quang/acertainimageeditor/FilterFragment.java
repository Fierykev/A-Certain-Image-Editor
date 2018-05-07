package kevin_quang.acertainimageeditor;

import android.graphics.Color;
import android.media.Image;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

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
        undo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editDisplaySurfaceView.undo();
            }
        });

        final ImageButton redo = view.findViewById(R.id.redo);
        redo.setColorFilter(editDisplaySurfaceView.canRedo() ? Color.WHITE : Color.DKGRAY);
        redo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editDisplaySurfaceView.redo();
            }
        });

        Tool.historyUpdate = new Tool.IHistoryUpdate() {
            @Override
            public void updateUI() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        undo.setColorFilter(editDisplaySurfaceView.canUndo() ? Color.WHITE : Color.DKGRAY);
                        redo.setColorFilter(editDisplaySurfaceView.canRedo() ? Color.WHITE : Color.DKGRAY);
                    }
                });
            }
        };

        final Fragment that = this;

        ImageButton resize = view.findViewById(R.id.resize);
        resize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().beginTransaction()
                        .replace(R.id.filter_root, ScaleResizeFragment.newInstance(editDisplaySurfaceView))
                        .addToBackStack(null)
                        .commit();
            }
        });

        ImageButton shoe = view.findViewById(R.id.shoe);
        shoe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShoeTool shoeTool = new ShoeTool();
                editDisplaySurfaceView.setTool(shoeTool);

                Tool.Args args = new Tool.Args();
                args.type = ShoeTool.RUN;

                editDisplaySurfaceView.passArgs(args);
            }
        });

        ImageButton rotateLeft = view.findViewById(R.id.rotate_left);
        rotateLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editDisplaySurfaceView.rotate(-90);
            }
        });

        ImageButton rotateRight = view.findViewById(R.id.rotate_right);
        rotateRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editDisplaySurfaceView.rotate(90);
            }
        });

        return view;
    }

}
