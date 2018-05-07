package kevin_quang.acertainimageeditor.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Calendar;

import kevin_quang.acertainimageeditor.ui.view.EditDisplaySurfaceView;
import kevin_quang.acertainimageeditor.R;
import kevin_quang.acertainimageeditor.ui.dialog.BlankDialog;
import kevin_quang.acertainimageeditor.ui.dialog.SaveDialog;

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

        ImageButton blank = view.findViewById(R.id.blank);
        blank.setOnClickListener(v -> {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            Fragment prev = getFragmentManager().findFragmentByTag("dialog");
            if (prev != null) {
                ft.remove(prev);
            }
            ft.addToBackStack(null);

            // Create and show the dialog.
            DialogFragment newFragment = BlankDialog.newInstance(editDisplaySurfaceView);
            newFragment.show(ft, "dialog");
        });

        ImageButton camera = view.findViewById(R.id.camera);
        camera.setOnClickListener(v -> pictureButtonPressed(v));

        ImageButton share = view.findViewById(R.id.share);
        share.setOnClickListener(v -> {
            String dirname = Environment.getExternalStorageDirectory().getPath() + File.separator + getString(R.string.app_name) + File.separator + "share";
            String filename = dirname + File.separator + Calendar.getInstance().getTime() + ".jpg";
            File file = new File(filename);
            file.mkdirs();
            if(file.exists()) {
                file.delete();
            }
            editDisplaySurfaceView.save(filename);
            fileUri = FileProvider.getUriForFile(getActivity().getApplicationContext(), getActivity().getApplicationContext().getPackageName() + ".provider", new File(filename));
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_STREAM, fileUri);
            startActivity(Intent.createChooser(intent, "Share Image"));
        });

        ImageButton gallery = view.findViewById(R.id.photo_gallery);
        gallery.setOnClickListener(v -> {
            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
            photoPickerIntent.setType("image/*");
            startActivityForResult(photoPickerIntent, GALLERY_REQUEST);
        });

        final ImageButton save = view.findViewById(R.id.save);
        save.setOnClickListener(v -> {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            Fragment prev = getFragmentManager().findFragmentByTag("dialog");
            if (prev != null) {
                ft.remove(prev);
            }
            ft.addToBackStack(null);

            // Create and show the dialog.
            DialogFragment newFragment = SaveDialog.newInstance(editDisplaySurfaceView);
            newFragment.show(ft, "dialog");
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
                    mat.postRotate(90);
                    photo = Bitmap.createBitmap(Bitmap.createScaledBitmap(photo, photo.getWidth(), photo.getHeight(), true), 0, 0, photo.getWidth(), photo.getHeight(), mat, true);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(getActivity(), "Failed to load image", Toast.LENGTH_LONG).show();
            }
            if (photo != null) {
                Log.d("Picture", "Setting picture");
                if(photo.getWidth() > 1920 || photo.getHeight() > 1080) {
                    if(photo.getWidth() / 1920.0f > photo.getHeight() / 1080.0f) {
                        photo = Bitmap.createScaledBitmap(photo, 1920, (int)(photo.getHeight() * 1920.0f / photo.getWidth()), false);
                    } else {
                        photo = Bitmap.createScaledBitmap(photo, (int)(photo.getWidth() * 1020.0f / photo.getHeight()), 1020, false);
                    }
                }
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
