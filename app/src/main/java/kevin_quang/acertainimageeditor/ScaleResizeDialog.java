package kevin_quang.acertainimageeditor;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

public class ScaleResizeDialog extends DialogFragment {

    private EditDisplaySurfaceView editDisplaySurfaceView;

    /**
     * Create a new instance of MyDialogFragment, providing "num"
     * as an argument.
     */
    public static ScaleResizeDialog newInstance(
            EditDisplaySurfaceView editDisplaySurfaceView
    ) {
        ScaleResizeDialog f = new ScaleResizeDialog();
        f.editDisplaySurfaceView = editDisplaySurfaceView;

        // Supply num input as an argument.
        Bundle args = new Bundle();
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Pick a style based on the num.
        int style = DialogFragment.STYLE_NORMAL, theme = 0;
        /*
        switch ((mNum-1)%6) {
            case 1: style = DialogFragment.STYLE_NO_TITLE; break;
            case 2: style = DialogFragment.STYLE_NO_FRAME; break;
            case 3: style = DialogFragment.STYLE_NO_INPUT; break;
            case 4: style = DialogFragment.STYLE_NORMAL; break;
            case 5: style = DialogFragment.STYLE_NORMAL; break;
            case 6: style = DialogFragment.STYLE_NO_TITLE; break;
            case 7: style = DialogFragment.STYLE_NO_FRAME; break;
            case 8: style = DialogFragment.STYLE_NORMAL; break;
        }
        switch ((mNum-1)%6) {
            case 4: theme = android.R.style.Theme_Holo; break;
            case 5: theme = android.R.style.Theme_Holo_Light_Dialog; break;
            case 6: theme = android.R.style.Theme_Holo_Light; break;
            case 7: theme = android.R.style.Theme_Holo_Light_Panel; break;
            case 8: theme = android.R.style.Theme_Holo_Light; break;
        }
        */
        setStyle(style, theme);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.scale_resize_dialog, container, false);
        final int srcWidth = editDisplaySurfaceView.getBitmapWidth();
        final int srcHeight = editDisplaySurfaceView.getBitmapHeight();

        final EditText widthField = v.findViewById(R.id.width);
        final EditText heightField = v.findViewById(R.id.height);
        widthField.setHint(String.valueOf(srcWidth));
        heightField.setHint(String.valueOf(srcHeight));

        final EditText meshWidthField = v.findViewById(R.id.mesh_width);
        final EditText meshHeightField = v.findViewById(R.id.mesh_height);

        final EditText iterationsField = v.findViewById(R.id.iterations);

        Button submit = v.findViewById(R.id.resize);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // TODO SHOW A TOAST ON ERROR

                String widthText = widthField.getText().toString();
                String heightText = heightField.getText().toString();
                if(widthText.length() == 0 || heightText.length() == 0) return;
                int width = Integer.parseInt(widthText);
                int height = Integer.parseInt(heightText);

                if(width <= 0 || height <= 0) return;

                String meshWidthText = meshWidthField.getText().toString();
                String meshHeightText = meshHeightField.getText().toString();
                if(meshWidthText.length() == 0 || meshHeightText.length() == 0) return;
                int meshWidth = Integer.parseInt(meshWidthText);
                int meshHeight = Integer.parseInt(meshHeightText);

                if(meshWidth <= 0 || meshWidth <= 0
                        || meshWidth > srcWidth || meshHeight > srcHeight) return;

                String iterationsText = iterationsField.getText().toString();
                if(iterationsText.length() == 0) return;
                int iterations = Integer.parseInt(iterationsText);
                if(iterations <= 0) return;

                ScaleResizeTool.ResizeArgs resizeArgs
                        = new ScaleResizeTool.ResizeArgs(width, height, meshWidth, meshHeight, iterations);
                editDisplaySurfaceView.passArgs(new Tool.Args(
                        ScaleResizeTool.RESIZE, resizeArgs));

                getFragmentManager().popBackStack();
            }
        });
        return v;
    }
}