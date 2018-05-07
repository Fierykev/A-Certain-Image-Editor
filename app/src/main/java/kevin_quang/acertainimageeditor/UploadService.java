package kevin_quang.acertainimageeditor;

import android.content.Context;

import java.lang.ref.WeakReference;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedFile;


public class UploadService {

    public UploadService() {}

    public void Execute(Upload upload, Callback<ImageResponse> callback) {
        final Callback<ImageResponse> cb = callback;

        RestAdapter restAdapter = buildRestAdapter();

        restAdapter.create(ImgurService.class).postImage(
                "Client-ID 9fc9698ea1ed8e0",
                upload.title,
                upload.description,
                upload.albumId,
                null,
                new TypedFile("image/*", upload.image),
                new Callback<ImageResponse>() {
                    @Override
                    public void success(ImageResponse imageResponse, Response response) {
                        if (cb != null) cb.success(imageResponse, response);
                        if (response == null) {
                            return;
                        }
                        /*
                        Notify image was uploaded successfully
                        */
                        if (imageResponse.success) {
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        if (cb != null) cb.failure(error);
                    }
                });
    }

    private RestAdapter buildRestAdapter() {
        RestAdapter imgurAdapter = new RestAdapter.Builder()
                .setEndpoint(ImgurService.server)
                .build();
        return imgurAdapter;
    }
}