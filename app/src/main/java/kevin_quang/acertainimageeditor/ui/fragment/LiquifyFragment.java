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

import kevin_quang.acertainimageeditor.tool.ImageDrawTool;
import kevin_quang.acertainimageeditor.ui.toggle.Toggler;
import kevin_quang.acertainimageeditor.ui.view.EditDisplaySurfaceView;
import kevin_quang.acertainimageeditor.R;
import kevin_quang.acertainimageeditor.tool.LiquifyTool;
import kevin_quang.acertainimageeditor.tool.Tool;

public class LiquifyFragment extends Fragment {

    private EditDisplaySurfaceView editDisplaySurfaceView;
    private ImageButton shrink, grow, smudge;
    private String shrinkTag = "liquify_shrink";
    private String growTag = "liquify_grow";
    private String smudgeTag = "liquify_smudge";

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
            editDisplaySurfaceView.setTool(new ImageDrawTool());
            getFragmentManager().popBackStack();
        });

        ImageButton shrink = view.findViewById(R.id.shrink);
        ImageButton grow = view.findViewById(R.id.grow);
        ImageButton smudge = view.findViewById(R.id.smudge);
        Toggler.add(shrinkTag, shrink);
        Toggler.add(growTag, grow);
        Toggler.add(smudgeTag, smudge);
        shrink.setOnClickListener(v -> {
            editDisplaySurfaceView.setTool(new LiquifyTool());
            if(!Toggler.toggle(shrinkTag)) {
                LiquifyTool.LiquifyArgs args = new LiquifyTool.LiquifyArgs(LiquifyTool.Mode.SHRINK);
                editDisplaySurfaceView.passArgs(new Tool.Args(LiquifyTool.LIQUIFY, args));
            }
        });


        grow.setOnClickListener(v -> {
            editDisplaySurfaceView.setTool(new LiquifyTool());
            if(!Toggler.toggle(growTag)) {
                LiquifyTool.LiquifyArgs args = new LiquifyTool.LiquifyArgs(LiquifyTool.Mode.ENLARGE);
                editDisplaySurfaceView.passArgs(new Tool.Args(LiquifyTool.LIQUIFY, args));
            }
        });

        smudge.setOnClickListener(v -> {
            editDisplaySurfaceView.setTool(new LiquifyTool());
            if(!Toggler.toggle(smudgeTag)) {
                LiquifyTool.LiquifyArgs args = new LiquifyTool.LiquifyArgs(LiquifyTool.Mode.SMUDGE);
                editDisplaySurfaceView.passArgs(new Tool.Args(LiquifyTool.LIQUIFY, args));
            }
        });

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toggler.remove(shrinkTag);
        Toggler.remove(growTag);
        Toggler.remove(smudgeTag);
    }
}
