package kevin_quang.acertainimageeditor.ui.fragment;

import android.graphics.Color;
import android.graphics.ColorFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import kevin_quang.acertainimageeditor.ui.toggle.Toggler;
import kevin_quang.acertainimageeditor.ui.view.EditDisplaySurfaceView;
import kevin_quang.acertainimageeditor.R;
import kevin_quang.acertainimageeditor.tool.ScaleResizeTool;
import kevin_quang.acertainimageeditor.tool.Tool;

public class ScaleResizeFragment extends Fragment {

    private EditDisplaySurfaceView editDisplaySurfaceView;

    public static ScaleResizeFragment newInstance(
            EditDisplaySurfaceView editDisplaySurfaceView
    ) {
        ScaleResizeFragment f = new ScaleResizeFragment();
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
        View view = inflater.inflate(R.layout.resize, container, false);

        final int srcWidth = editDisplaySurfaceView.getBitmapWidth();
        final int srcHeight = editDisplaySurfaceView.getBitmapHeight();
        final int meshWidth = 50;
        final int meshHeight = 50;
        final int iterations = 35;

        ImageButton back = view.findViewById(R.id.back);
        back.setOnClickListener(v -> getFragmentManager().popBackStack());

        final EditText widthField = view.findViewById(R.id.width);
        widthField.setBackgroundColor(0x55FFFFFF);
        widthField.setHint(String.valueOf(srcWidth));
        final EditText heightField = view.findViewById(R.id.height);
        heightField.setBackgroundColor(0x55FFFFFF);
        heightField.setHint(String.valueOf(srcHeight));

        ImageButton done = view.findViewById(R.id.done);
        done.setOnClickListener(v -> {
            String widthText = widthField.getText().toString();
            String heightText = heightField.getText().toString();
            if(widthText.length() == 0 && heightText.length() == 0) {
                Toast.makeText(getContext(), "Specified size same as current size", Toast.LENGTH_SHORT).show();
                return;
            }
            int width = srcWidth;
            if(widthText.length() != 0) {
                width = Integer.parseInt(widthText);
            }
            int height = srcHeight;
            if(heightText.length() != 0) {
                height = Integer.parseInt(heightText);
            }
            if(width <= 0 || height <= 0) {
                Toast.makeText(getContext(), "Width/Height cannot be negative", Toast.LENGTH_SHORT).show();
                return;
            }

            if(width > 1920 || height > 1080) {
                Toast.makeText(getContext(), "Max Image Size 1920x1080", Toast.LENGTH_SHORT).show();
                return;
            }

            if(srcWidth == width && srcHeight == height) {
                Toast.makeText(getContext(), "Specified size same as current size", Toast.LENGTH_SHORT).show();
                return;
            }

            editDisplaySurfaceView.setTool(
                    new ScaleResizeTool()
            );

            ScaleResizeTool.ResizeArgs resizeArgs
                    = new ScaleResizeTool.ResizeArgs(width, height, meshWidth, meshHeight, iterations);
            editDisplaySurfaceView.passArgs(new Tool.Args(
                    ScaleResizeTool.RESIZE, resizeArgs));
        });

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
