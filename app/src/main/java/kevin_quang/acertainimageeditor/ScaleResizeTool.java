package kevin_quang.acertainimageeditor;


import android.graphics.Bitmap;

import org.opencv.android.Utils;
import org.opencv.core.Mat;


/**
 * Created by Kevin on 3/28/2018.
 */

public class ScaleResizeTool extends Tool {

    private Mat image;

    // TMP
    ScaleResizeTool()
    {

    }

    void load(Bitmap bitmap)
    {
        image = new Mat();
        Utils.bitmapToMat(bitmap, image);

        //StaticSaliencySpectralResidual d = StaticSaliencySpectralResidual.create();

        //d.computeSaliency(image, image);
    }

    @Override
    void onDraw() {

    }

    @Override
    void getLeftMenu() {

    }

    @Override
    void getRightMenu() {

    }

    void compute() {

    }
}
