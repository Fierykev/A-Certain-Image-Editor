package kevin_quang.acertainimageeditor;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.FileProvider;
import android.support.v4.provider.DocumentFile;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class LoadFragment extends Fragment {
    private Uri fileUri;
    private EditDisplaySurfaceView editDisplaySurfaceView;

    public static LoadFragment newInstance(
            EditDisplaySurfaceView editDisplaySurfaceView
    ) {
        Bundle args = new Bundle();
        LoadFragment fragment = new LoadFragment();
        fragment.editDisplaySurfaceView = editDisplaySurfaceView;
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.load, container, false);

        ImageButton camera = view.findViewById(R.id.camera);
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pictureButtonPressed(v);
            }
        });

        ImageButton share = view.findViewById(R.id.share);
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String dirname = Environment.getExternalStorageDirectory().getPath() + File.separator + getString(R.string.app_name);
                String filename = dirname + File.separator + "share.jpg";
                try {
                    File file = new File(filename);
                    file.mkdirs();
                    if(file.exists()) {
                        file.delete();
                    }
                    editDisplaySurfaceView.save(filename);
                    Log.d("Picture", "Saved to share");
                    fileUri = FileProvider.getUriForFile(getActivity().getApplicationContext(), getActivity().getApplicationContext().getPackageName() + ".provider", new File(filename));
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), "Could not save file to share", Toast.LENGTH_SHORT);
                    return;
                }
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_STREAM, fileUri);
                startActivity(Intent.createChooser(intent, "Share Image"));
            }
        });

        ImageButton gallery = view.findViewById(R.id.photo_gallery);
        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, GALLERY_REQUEST);
            }
        });

        final ImageButton save = view.findViewById(R.id.save);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                Fragment prev = getFragmentManager().findFragmentByTag("dialog");
                if (prev != null) {
                    ft.remove(prev);
                }
                ft.addToBackStack(null);

                // Create and show the dialog.
                DialogFragment newFragment = SaveDialog.newInstance(editDisplaySurfaceView);
                newFragment.show(ft, "dialog");
            }
        });

        return view;
    }

    private static final int CAMERA_REQUEST = 1888;
    private static final int GALLERY_REQUEST = 1889;

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Bitmap photo = null;
        if ((requestCode == CAMERA_REQUEST || requestCode == GALLERY_REQUEST) && resultCode == Activity.RESULT_OK) {
            try {
                if (requestCode == GALLERY_REQUEST) {
                    fileUri = data.getData();
                }
                photo = BitmapFactory.decodeStream(getActivity().getContentResolver().openInputStream(fileUri), null, null);
                if (requestCode == CAMERA_REQUEST) {
                    Matrix mat = new Matrix();
                    mat.postRotate(-90);
                    photo = Bitmap.createBitmap(Bitmap.createScaledBitmap(photo, photo.getWidth(), photo.getHeight(), true), 0, 0, photo.getWidth(), photo.getHeight(), mat, true);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(getActivity(), "Failed to load image", Toast.LENGTH_LONG).show();
            }
            if (photo != null) {
                Log.d("Picture", "Setting picture");
                editDisplaySurfaceView.setBitmap(photo);
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////

    public void pictureButtonPressed(View view) {
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        String filename = Environment.getExternalStorageDirectory().getPath() + "/output.png";
        fileUri = FileProvider.getUriForFile(getActivity().getApplicationContext(), getActivity().getApplicationContext().getPackageName() + ".provider", new File(filename));
        intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, fileUri);
        startActivityForResult(intent, CAMERA_REQUEST);
    }
}
