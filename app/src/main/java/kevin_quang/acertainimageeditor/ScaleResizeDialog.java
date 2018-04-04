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
        final EditText widthField = v.findViewById(R.id.width);
        final EditText heightField = v.findViewById(R.id.height);
        Button submit = v.findViewById(R.id.resize);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int width = Integer.parseInt(widthField.getText().toString());
                int height = Integer.parseInt(heightField.getText().toString());
                if(width > 0 && height > 0) {
                    // TODO: Resize here!

                    ScaleResizeTool.ResizeArgs resizeArgs
                            = new ScaleResizeTool.ResizeArgs(width, height);
                    editDisplaySurfaceView.passArgs(new Tool.Args(
                            ScaleResizeTool.RESIZE, resizeArgs));

                    getFragmentManager().popBackStack();
                } else {
                    // TODO: Error Handling
                }
            }
        });
        return v;
    }
}