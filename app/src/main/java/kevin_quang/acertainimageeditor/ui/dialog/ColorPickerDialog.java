package kevin_quang.acertainimageeditor.ui.dialog;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.OpacityBar;
import com.larswerkman.holocolorpicker.SaturationBar;
import com.larswerkman.holocolorpicker.ValueBar;

import kevin_quang.acertainimageeditor.ui.view.EditDisplaySurfaceView;
import kevin_quang.acertainimageeditor.R;

public class ColorPickerDialog extends DialogFragment {

    private EditDisplaySurfaceView editDisplaySurfaceView;

    public static ColorPickerDialog newInstance(EditDisplaySurfaceView editDisplaySurfaceView) {
        ColorPickerDialog f = new ColorPickerDialog();
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
        View view = inflater.inflate(R.layout.color_picker_dialog, container, false);

        final ColorPicker picker = view.findViewById(R.id.picker);
        OpacityBar opacityBar = view.findViewById(R.id.opacitybar);
        SaturationBar saturationBar = view.findViewById(R.id.saturationbar);
        ValueBar valueBar = view.findViewById(R.id.valuebar);

        picker.addOpacityBar(opacityBar);
        picker.addSaturationBar(saturationBar);
        picker.addValueBar(valueBar);

        picker.setOldCenterColor(editDisplaySurfaceView.getToolColor());
        picker.setColor(editDisplaySurfaceView.getToolColor());

        Button select = view.findViewById(R.id.select);
        select.setOnClickListener(v -> {
            editDisplaySurfaceView.setToolColor(picker.getColor());
            getFragmentManager().popBackStack();
        });
        Button cancel = view.findViewById(R.id.cancel);
        cancel.setOnClickListener(v -> getFragmentManager().popBackStack());

        return view;
    }
}