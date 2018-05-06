package kevin_quang.acertainimageeditor;

import android.content.Context;
import android.graphics.Bitmap;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.photo.Photo;
import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.util.ArrayList;
import java.util.List;

import static org.opencv.core.CvType.CV_32F;
import static org.opencv.core.CvType.CV_32FC1;
import static org.opencv.core.CvType.CV_32FC3;
import static org.opencv.core.CvType.CV_8UC3;
import static org.opencv.imgproc.Imgproc.COLOR_BGR2GRAY;
import static org.opencv.imgproc.Imgproc.COLOR_RGBA2RGB;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY;

//import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

/**
 * Created by Kevin on 3/28/2018.
 */

public class ShoeTool extends Tool {

    private static final String MODEL = "file:///android_asset/graphs/shoes.pb";
    private static final int CROP_SIZE = 256;

    private TensorFlowInferenceInterface tf;

    private Mat convertImage, origImage, outImage;
    private int textureID;

    public static final int RUN = 0;

    @Override
    void init(Context context) {
        super.init(context);
        tf = new TensorFlowInferenceInterface(context.getAssets(), MODEL);
    }

    @Override
    void destroy() {
        super.destroy();

        tf = null;
    }

    @Override
    void load(Bitmap bitmap) {
        super.load(bitmap);

        origImage = new Mat();
        Utils.bitmapToMat(bitmap, origImage);

        // TODO: add warning

        convertImage = origImage.clone();
        convertImage.convertTo(convertImage, CV_32F, 1.0 / 255.0);
        Imgproc.cvtColor(convertImage, convertImage, COLOR_RGBA2RGB);

        Imgproc.resize(convertImage, convertImage, new Size(CROP_SIZE, CROP_SIZE));
    }

    void convertToInput()
    {

    }

    @Override
    void setArgs(Args args) {
        switch (args.type)
        {
            case RUN:

                compute();

                break;
        }
    }

    @Override
    void onDraw(float aspectRatio, int width, int height) {
        super.onDraw(aspectRatio, width, height);
    }

    @Override
    void getLeftMenu() {

    }

    @Override
    void getRightMenu() {

    }

    Mat clamp(Mat img)
    {
        List<Mat> channels = new ArrayList<>();
        for (int i = 0; i < img.channels(); i++) {
            channels.add(new Mat());
        }
        Core.split(img, channels);

        for (int i = 0; i < channels.size(); i++)
        {
            Imgproc.threshold(channels.get(i), channels.get(i), 0, 1, THRESH_BINARY);
        }

        Core.merge(channels, img);

        return img;
    }

    Mat sharpen(Mat img)
    {
        /*
        Mat mask = new Mat();
        Imgproc.cvtColor(origImage, mask, COLOR_RGBA2BGRA);
        Imgproc.Canny(mask, mask, 100, 200);

        Core.multiply(mask, new Scalar(-1), mask);
        Core.add(mask, new Scalar(1), mask);

        Imgproc.threshold(mask, mask, .9, 1.00, THRESH_BINARY_INV);
        Imgproc.GaussianBlur(mask, mask, new Size(21, 21), 11);*/

        int kSize = 2;
        Mat kernel = new Mat(
                kSize,
                kSize,
                CV_32FC1,
                new Scalar(1.f / (float)(kSize * kSize))
        );
        Imgproc.filter2D(img, img, -1, kernel);

        img.convertTo(img, CV_32FC3, 1.0 / 255.0);
        int sigma = 5;

        Mat mask = new Mat();
        Imgproc.cvtColor(img,  mask, COLOR_BGR2GRAY);
        Imgproc.GaussianBlur(mask, mask, new Size(0, 0), sigma, sigma);

        List<Mat> channels = new ArrayList<>();
        for (int i = 0; i < img.channels(); i++) {
            channels.add(new Mat());
        }

        float amount = 1.f;
        Core.split(img, channels);
        Core.multiply(mask, new Scalar(-amount), mask);

        for (int i = 0; i < channels.size(); i++)
        {
            Core.multiply(channels.get(i), new Scalar(1.f + amount), channels.get(i));
            Core.add(channels.get(i), mask, channels.get(i));
        }

        Core.merge(channels, img);

        img.convertTo(img, CV_8UC3, 255.0);

        return img;
    }

    Mat denoise(Mat img)
    {
        Mat denoise = new Mat();

        Photo.fastNlMeansDenoisingColored(
                img,
                denoise,
                10,
                10,
                7,
                21);

        return denoise;
/*
        img.convertTo(img, CV_32F, 1.0 / 255.0);
        denoise.convertTo(denoise, CV_32F, 1.0 / 255.0);

        float amount = -.5f;

        Core.multiply(img, new Scalar(1.f + amount), img);
        Core.multiply(denoise, new Scalar(-amount), denoise);
        Core.add(img, denoise, img);

        //img = clamp(img);
        img.convertTo(img, CV_8UC3, 255.0);

        return img;*/
    }

    void postProcess()
    {

    }

    void compute()
    {
        // convert image data
        float data[] = new float[
                convertImage.rows() * convertImage.cols() * convertImage.channels()];
        convertImage.get(0, 0, data);

        tf.feed("input", data, 1, convertImage.rows(), convertImage.cols(), 3);
        tf.run(new String[] { "output" });

        tf.fetch("output", data);

        outImage = new Mat(CROP_SIZE, CROP_SIZE, CV_32FC3);
        outImage.put(0, 0, data);

        outImage.convertTo(outImage, CV_8UC3, 255.0);
        //outImage = denoise(outImage);

        Imgproc.resize(outImage, outImage, new Size(origImage.cols(), origImage.rows()));

        Bitmap outBmp = Bitmap.createBitmap(outImage.cols(), outImage.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(outImage, outBmp);

        GLHelper.standardizeBitamp(outBmp);
        this.load(outBmp);
    }
}