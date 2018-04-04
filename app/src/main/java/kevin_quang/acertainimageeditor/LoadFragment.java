package kevin_quang.acertainimageeditor;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

public class LoadFragment extends Fragment {

    public static LoadFragment newInstance() {
        Bundle args = new Bundle();
        LoadFragment fragment = new LoadFragment();
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
                    String path = MediaStore.Images.Media.insertImage(getActivity().getContentResolver(), bitmap, "Image Description", null);
                    Uri uri = Uri.parse(path);

                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("image/jpeg");
                    intent.putExtra(Intent.EXTRA_STREAM, uri);
                    startActivity(Intent.createChooser(intent, "Share Image"));
                }
            }
        });
        return view;
    }

    private static final int CAMERA_REQUEST = 1888;

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            // TODO: Set Bitmap Here!
        }
    }

    ////////////////////////////////////////////////////////////////////////////

    public void pictureButtonPressed(View view) {
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST);
    }
}
