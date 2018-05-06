package kevin_quang.acertainimageeditor;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.support.v4.provider.DocumentFile;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.OpacityBar;
import com.larswerkman.holocolorpicker.SaturationBar;
import com.larswerkman.holocolorpicker.ValueBar;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;

public class SaveDialog extends DialogFragment {

    private EditText path, name;
    private DocumentFile tree;
    private EditDisplaySurfaceView editDisplaySurfaceView;

    public static SaveDialog newInstance(EditDisplaySurfaceView editDisplaySurfaceView) {
        SaveDialog f = new SaveDialog();
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

    private final static int SAVE_REQUEST = 1890;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.save_dialog, container, false);

        path = v.findViewById(R.id.file_path);
        path.setText(Environment.getExternalStorageDirectory() + File.separator + getString(R.string.app_name) + File.separator + "images");
        path.setFocusable(false);
        path.setClickable(true);
        path.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent saveIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                startActivityForResult(saveIntent, SAVE_REQUEST);
            }
        });

        name = v.findViewById(R.id.file_name);

        Button save = v.findViewById(R.id.save);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(name.getText().length() > 0 && tree != null) {
                    String fileName = name.getText().toString();
                    DocumentFile file = tree.createFile("image/jpeg", fileName);
                    try {
                        editDisplaySurfaceView.save(getPath(file.getUri()));//getContext().getContentResolver().openOutputStream(file.getUri()));
                    } catch(Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Could not open file", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    getFragmentManager().popBackStack();
                }
            }
        });

        Button cancel = v.findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });

        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == SAVE_REQUEST && resultCode == Activity.RESULT_OK) {
            Uri treeUri = data.getData();
            tree = DocumentFile.fromTreeUri(getContext(), treeUri);
            if(!tree.canWrite()) {
                Toast.makeText(getContext(), "Cannot write to this directory", Toast.LENGTH_SHORT).show();
                tree = null;
            } else {
                path.setText(getPath(tree.getUri()));
            }
        }
    }

    private String getPath(Uri path) {
        final String docId = DocumentsContract.getDocumentId(path);
        final String[] split = docId.split(":");
        final String type = split[0];

        if ("primary".equalsIgnoreCase(type)) {
            return Environment.getExternalStorageDirectory() + "/" + split[1];
        }
        return "";
    }
}