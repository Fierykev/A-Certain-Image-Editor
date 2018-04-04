package kevin_quang.acertainimageeditor;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;

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
                // TODO: Set this and sharing is golden
                Bitmap bitmap = null;

                if(bitmap != null) {
                    Uri uri = save(bitmap, "Image Title", "Image Description");

                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("image/jpeg");
                    intent.putExtra(Intent.EXTRA_STREAM, uri);
                    startActivity(Intent.createChooser(intent, "Share Image"));
                }
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
        return view;
    }

    private Uri save(Bitmap bitmap, String title, String desc) {
        String path = MediaStore.Images.Media.insertImage(getActivity().getContentResolver(), bitmap, title, desc);
        return Uri.parse(path);
    }

    private static final int CAMERA_REQUEST = 1888;
    private static final int GALLERY_REQUEST = 1889;

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Bitmap photo = null;
        if ((requestCode == CAMERA_REQUEST || requestCode == GALLERY_REQUEST) && resultCode == Activity.RESULT_OK) {
            try {
                if(requestCode == GALLERY_REQUEST) {
                    fileUri = data.getData();
                }
                photo = BitmapFactory.decodeStream(getActivity().getContentResolver().openInputStream(fileUri), null, null);
                Log.d("Photo", String.valueOf(photo.getWidth()) + "," + String.valueOf(photo.getHeight()));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(getActivity(), "Failed to load image", Toast.LENGTH_LONG).show();
            }
        }
        if(photo != null) {
            editDisplaySurfaceView.setBitmap(photo);
        }
    }

    ////////////////////////////////////////////////////////////////////////////

    public void pictureButtonPressed(View view) {
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        String filename = Environment.getExternalStorageDirectory().getPath() + "/output.jpg";
        fileUri = FileProvider.getUriForFile(getActivity().getApplicationContext(), getActivity().getApplicationContext().getPackageName() + ".provider", new File(filename));
        intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, fileUri);
        startActivityForResult(intent, CAMERA_REQUEST);
    }
}
