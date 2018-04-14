package kevin_quang.acertainimageeditor;

import android.content.Context;
import android.graphics.Bitmap;

//import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

/**
 * Created by Kevin on 3/28/2018.
 */

public class ShoeTool extends Tool {

    private static final String MODEL = "file:///android_asset/graphs/shoes.pb";

    //private TensorFlowInferenceInterface tf;

    @Override
    void init(Context context) {
        //tf = new TensorFlowInferenceInterface(context.getAssets(), MODEL);
    }

    @Override
    void destroy() {

    }

    @Override
    void load(Bitmap bitmap) {

    }

    @Override
    void setArgs(Args args) {
        //tf.feed("", "", );
        //tf.run(new String[] {  });
    }

    @Override
    void onDraw(float aspectRatio) {

    }

    @Override
    void getLeftMenu() {

    }

    @Override
    void getRightMenu() {

    }

    @Override
    void save(String path) {

    }
}