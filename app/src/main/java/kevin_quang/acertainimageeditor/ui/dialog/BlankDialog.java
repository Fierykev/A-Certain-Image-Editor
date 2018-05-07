package kevin_quang.acertainimageeditor.ui.dialog;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import kevin_quang.acertainimageeditor.ui.view.EditDisplaySurfaceView;
import kevin_quang.acertainimageeditor.R;

public class BlankDialog extends DialogFragment {

    private EditDisplaySurfaceView editDisplaySurfaceView;

    public static BlankDialog newInstance(EditDisplaySurfaceView editDisplaySurfaceView) {
        BlankDialog f = new BlankDialog();
        f.editDisplaySurfaceView = editDisplaySurfaceView;
        Bundle args = new Bundle();
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int style = DialogFragment.STYLE_NORMAL, theme = 0;
        setStyle(style, theme);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.blank_dialog, container, false);

        final EditText widthField = view.findViewById(R.id.width);
        final EditText heightField = view.findViewById(R.id.height);

        Button select = view.findViewById(R.id.create);
        select.setOnClickListener(v -> {
            String widthString = widthField.getText().toString();
            String heightString = heightField.getText().toString();
            if(widthString.length() == 0 || heightString.length() == 0) {
                Toast.makeText(getContext(), "Width/Height field(s) empty", Toast.LENGTH_SHORT).show();
                return;
            }

            int width = Integer.parseInt(widthString);
            int height = Integer.parseInt(heightString);

            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444);
            bitmap.eraseColor(Color.WHITE);
            editDisplaySurfaceView.setBitmap(bitmap);

            getFragmentManager().popBackStack();
        });
        Button cancel = view.findViewById(R.id.cancel);
        cancel.setOnClickListener(v -> getFragmentManager().popBackStack());

        return view;
    }
}