package kevin_quang.acertainimageeditor.ui.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import kevin_quang.acertainimageeditor.ui.view.EditDisplaySurfaceView;
import kevin_quang.acertainimageeditor.R;
import kevin_quang.acertainimageeditor.tool.LiquifyTool;
import kevin_quang.acertainimageeditor.tool.Tool;

public class LiquifyFragment extends Fragment {

    private EditDisplaySurfaceView editDisplaySurfaceView;

    public static LiquifyFragment newInstance(
            EditDisplaySurfaceView editDisplaySurfaceView
    ) {
        LiquifyFragment f = new LiquifyFragment();
        f.editDisplaySurfaceView = editDisplaySurfaceView;

        // Supply num input as an argument.
        Bundle args = new Bundle();
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.liquify, container, false);

        ImageButton back = view.findViewById(R.id.back);
        back.setOnClickListener(v -> {
            getFragmentManager().popBackStack();
        });

        ImageButton shrink = view.findViewById(R.id.shrink);
        ImageButton grow = view.findViewById(R.id.grow);
        ImageButton smudge = view.findViewById(R.id.smudge);
        shrink.setOnClickListener(v -> {
            editDisplaySurfaceView.setTool(new LiquifyTool());
            LiquifyTool.LiquifyArgs args = new LiquifyTool.LiquifyArgs(LiquifyTool.Mode.SHRINK);
            editDisplaySurfaceView.passArgs(new Tool.Args(LiquifyTool.LIQUIFY, args));
            shrink.setColorFilter(getActivity().getColor(R.color.highlight));
            grow.setColorFilter(Color.WHITE);
            smudge.setColorFilter(Color.WHITE);
        });


        grow.setOnClickListener(v -> {
            editDisplaySurfaceView.setTool(new LiquifyTool());
            LiquifyTool.LiquifyArgs args = new LiquifyTool.LiquifyArgs(LiquifyTool.Mode.ENLARGE);
            editDisplaySurfaceView.passArgs(new Tool.Args(LiquifyTool.LIQUIFY, args));
            shrink.setColorFilter(Color.WHITE);
            grow.setColorFilter(getActivity().getColor(R.color.highlight));
            smudge.setColorFilter(Color.WHITE);
        });

        smudge.setOnClickListener(v -> {
            editDisplaySurfaceView.setTool(new LiquifyTool());
            LiquifyTool.LiquifyArgs args = new LiquifyTool.LiquifyArgs(LiquifyTool.Mode.SMUDGE);
            editDisplaySurfaceView.passArgs(new Tool.Args(LiquifyTool.LIQUIFY, args));
            shrink.setColorFilter(Color.WHITE);
            grow.setColorFilter(Color.WHITE);
            smudge.setColorFilter(getActivity().getColor(R.color.highlight));
        });

        return view;
    }
}
