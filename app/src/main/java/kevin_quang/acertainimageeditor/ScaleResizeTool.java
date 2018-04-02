package kevin_quang.acertainimageeditor;


import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES30;
import android.util.Pair;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.saliency.StaticSaliencySpectralResidual;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import static kevin_quang.acertainimageeditor.GLHelper.loadProgram;
import static kevin_quang.acertainimageeditor.GLHelper.loadTexture;
import static org.opencv.core.CvType.CV_32FC1;
import static org.opencv.core.CvType.CV_8U;
import static org.opencv.core.CvType.CV_8UC3;
import static org.opencv.imgproc.Imgproc.COLOR_BGR2GRAY;


/**
 * Created by Kevin on 3/28/2018.
 */

public class ScaleResizeTool extends Tool {

    private Mat convertImage, origImage, significance;
    private Pair<Integer, Integer> vertDim;
    private VertexArray verts;
    private FloatBuffer vertexBuffer;
    private FloatBuffer uvBuffer;
    private IntBuffer indexBuffer;
    private int vertBufferID[] = new int[1];
    private int indexBufferID[] = new int[1];
    private int program;
    private int postionAttr, texCoordAttr, textureUnif;
    private int textureID;
    private int indices[];
    private int vertID[] = new int[1];

    private Pair<Integer, Integer> meshDim
            = new Pair<>(10, 10);
    private Pair<Integer, Integer> desiredDim
            = new Pair<>(500, 300);

    class Vertex
    {
        float x, y, z;
        float u, v;
    }

    class VertexArray
    {
        float[] floatArray;
        int index = 0;

        VertexArray(int numEl)
        {
            floatArray = new float[numEl * 5];
        }

        void add(Vertex v)
        {
            floatArray[index] = v.x;
            index++;

            floatArray[index] = v.y;
            index++;

            floatArray[index] = v.z;
            index++;

            //floatArray[index] = v.u;
            //index++;

            //floatArray[index] = v.v;
            //index++;
        }

        int size()
        {
            return floatArray.length * 4;
        }

        float[] getFloatArray()
        {
            return floatArray;
        }
    }


    // TMP
    ScaleResizeTool()
    {

    }

    void init(Context context)
    {
        program = loadProgram(
                "shaders/ScaleResizeTool/main.vs",
                "shaders/ScaleResizeTool/main.fs",
                context.getAssets());
        postionAttr = GLES30.glGetAttribLocation(program, "position");
        texCoordAttr = GLES30.glGetAttribLocation(program, "texCoord");
        textureUnif = GLES30.glGetUniformLocation(program, "texture");
    }

    void destroy()
    {
        GLES30.glDeleteProgram(program);
        GLES30.glDeleteBuffers(1, vertBufferID, 0);
        GLES30.glDeleteBuffers(1, indexBufferID, 0);
    }

    void load(Bitmap bitmap)
    {
        textureID = loadTexture(bitmap);

        origImage = new Mat();
        Utils.bitmapToMat(bitmap, origImage);

        convertImage = origImage.clone();
        convertImage.convertTo(convertImage, CV_8U);

        //StaticSaliencySpectralResidual d = StaticSaliencySpectralResidual.create();

        //d.computeSaliency(image, image);

        // TMP
        compute();
    }

    @Override
    void onDraw() {
        GLES30.glEnable(GLES30.GL_TEXTURE_2D);

        GLES30.glUseProgram(program);

        GLES30.glBindBuffer (GLES30.GL_ARRAY_BUFFER, vertBufferID[0]);

        GLES30.glEnableVertexAttribArray(postionAttr);
        GLES30.glVertexAttribPointer(postionAttr, 3, GLES30.GL_FLOAT, false, 4 * 5, 0);

        GLES30.glEnableVertexAttribArray(texCoordAttr);
        GLES30.glVertexAttribPointer(texCoordAttr, 2, GLES30.GL_FLOAT, false, 4 * 5, 4 * 3);

        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureID);
        GLES30.glUniform1ui(textureUnif, textureID);

        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, indexBufferID[0]);
        GLES30.glDrawElements(GLES30.GL_TRIANGLES, indices.length, GLES30.GL_UNSIGNED_INT, 0);

        GLES30.glDisableVertexAttribArray(postionAttr);
        GLES30.glDisableVertexAttribArray(texCoordAttr);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);
        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    @Override
    void getLeftMenu() {

    }

    @Override
    void getRightMenu() {

    }

    private void createMesh(
            Pair<Integer, Integer> meshDim,
            Pair<Integer, Integer> actualDim,
            Pair<Integer, Integer> targetDim
    ) {
        // get quad number
        Pair<Integer, Integer> quadDim =
                new Pair<>(
                        actualDim.first / meshDim.first,
                        actualDim.second / meshDim.second);

        // get overflow
        Pair<Integer, Integer> overflowDim =
                new Pair<>(
                        actualDim.first - quadDim.first * meshDim.first,
                        actualDim.second - quadDim.second * meshDim.second
                );

        // number of vertices
        vertDim =
                new Pair<>(
                        quadDim.first + 1,
                        quadDim.second + 1
                );

        // verts
        Vertex vertex = new Vertex();
        verts = new VertexArray(vertDim.first * vertDim.second);

        for (int y = 0; y < vertDim.second; y++) {
            for (int x = 0; x < vertDim.first; x++) {
                int index = x + y * vertDim.first;

                vertex.x =
                        Math.min(
                                meshDim.first * x
                                        + Math.round(
                                        (float) (overflowDim.first * x)
                                                / (float) (quadDim.first)
                                ),
                                actualDim.first
                        );
                vertex.y =
                        Math.min(
                                meshDim.second * y
                                        + Math.round(
                                        (float) (overflowDim.second * y)
                                                / (float) (quadDim.second)
                                ),
                                actualDim.second
                        );
                vertex.z = .1f;

                vertex.u =
                    (float)(vertex.x)
                            / (float)(actualDim.first);
                vertex.v =
                    (float)(vertex.y)
                            / (float)(actualDim.second);

                verts.add(vertex);
            }
        }

        // indices
        indices =
                new int[vertDim.first * vertDim.second * 6];

        for (int y = 0; y < quadDim.second; y++) {
            for (int x = 0; x < quadDim.first; x++) {
                int index = (x + y * vertDim.first) * 6;

                indices[index] = x + y * vertDim.first;
                indices[index + 1] = (x + 1) + y * vertDim.first;
                indices[index + 2] = x + (y + 1) * vertDim.first;

                indices[index + 3] = (x + 1) + y * vertDim.first;
                indices[index + 4] = x + (y + 1) * vertDim.first;
                indices[index + 5] = (x + 1) + (y + 1) * vertDim.first;

                /*
                // top left
                indices[index] =
                        x + (y + 1) * vertDim.first;

                // top right
                indices[index + 1] =
                        x + 1 + (y + 1) * vertDim.first;

                // bottom right
                indices[index + 2] =
                        x + 1 + y * vertDim.first;

                // bottom left
                indices[index + 3] =
                        x + y * vertDim.first;*/


            }
        }

        GLES30.glGenBuffers (1, vertBufferID, 0);

        GLES30.glBindBuffer (GLES30.GL_ARRAY_BUFFER, vertBufferID[0]);
        GLES30.glBufferData (GLES30.GL_ARRAY_BUFFER, verts.size(),
                null, GLES30.GL_STATIC_DRAW );

        vertexBuffer =
                ((ByteBuffer) GLES30.glMapBufferRange (
                    GLES30.GL_ARRAY_BUFFER, 0, verts.size(),
                    GLES30.GL_MAP_WRITE_BIT | GLES30.GL_MAP_INVALIDATE_BUFFER_BIT)
                ).order(ByteOrder.nativeOrder()).asFloatBuffer();
        vertexBuffer.put(verts.getFloatArray()).position(0);

        GLES30.glUnmapBuffer(GLES30.GL_ARRAY_BUFFER);

        GLES30.glGenBuffers (1, indexBufferID, 0);

        GLES30.glBindBuffer (GLES30.GL_ELEMENT_ARRAY_BUFFER, indexBufferID[0]);
        GLES30.glBufferData (GLES30.GL_ELEMENT_ARRAY_BUFFER, indices.length * 4,
                null, GLES30.GL_STATIC_DRAW);

        indexBuffer =
                ((ByteBuffer) GLES30.glMapBufferRange (
                        GLES30.GL_ELEMENT_ARRAY_BUFFER, 0, indices.length * 4,
                        GLES30.GL_MAP_WRITE_BIT | GLES30.GL_MAP_INVALIDATE_BUFFER_BIT )
                ).order(ByteOrder.nativeOrder()).asIntBuffer();
        indexBuffer.put(indices).position(0);

        GLES30.glUnmapBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER);
    }

    private void createSignificanceMap(Mat image) {
        // norm
        Mat norm;
        {
            Pair<Mat, Mat> d =
                    new Pair<>(new Mat(), new Mat());
            Imgproc.Sobel(image, d.first, CV_8U, 1, 0);
            Imgproc.Sobel(image, d.second, CV_8U, 0, 1);

            norm =
                    new Mat(
                            image.rows(),
                            image.cols(),
                            CV_8UC3,
                            new Scalar(0, 0, 0));

            int size = (int) (d.first.total() * d.first.channels());
            byte dData[][] = new byte[2][];
            dData[0] = new byte[size];
            d.first.get(0, 0, dData[0]);
            dData[1] = new byte[size];
            d.second.get(0, 0, dData[1]);

            int normSize = (int) (norm.total() * norm.channels());
            byte normData[] = new byte[normSize];
            norm.get(0, 0, normData);

            for (int i = 0; i < size; i += d.first.channels()) {
                normData[i] = (byte) Math.sqrt(
                        (double) dData[0][i] * (double) dData[0][i]
                                + (double) dData[1][i] * (double) dData[1][i]
                );
            }

            norm.put(0, 0, normData);

            Imgproc.cvtColor(norm, norm, COLOR_BGR2GRAY);
            norm.convertTo(norm, CV_32FC1, 1.f / 255.f);
        }

        // saliency
        Mat saliency;
        {
            List<Mat> channels = new ArrayList<>();
            List<Mat> saliencyChannels = new ArrayList<>();

            for (int i = 0; i < image.channels(); i++) {
                channels.add(new Mat());
                saliencyChannels.add(new Mat());
            }

            Core.split(image, channels);

            StaticSaliencySpectralResidual saliencyAlgo
                    = StaticSaliencySpectralResidual.create();

            for (int channel = 0; channel < image.channels(); channel++)
            {
                saliencyAlgo.computeSaliency(
                        channels.get(channel),
                        saliencyChannels.get(channel)
                );
            }

            saliency = new Mat(
                    image.rows(),
                    image.cols(),
                    CV_8UC3,
                    new Scalar(0, 0, 0)
            );
            Core.merge(saliencyChannels, saliency);

            // combine into single channel
            Imgproc.cvtColor(saliency, saliency, COLOR_BGR2GRAY);
            saliency.convertTo(saliency, CV_32FC1, 1.f);
        }

        // significance
        {
            significance =
                    new Mat(
                            image.rows(),
                            image.cols(),
                            CV_32FC1,
                            new Scalar(0, 0, 0)
                    );
            int size = (int) (significance.total() * significance.channels());
            byte sigData[] = new byte[size];

            //for (int i = 0; i < size; i++)
                //sigData[i] =
                        ;
        }
    }

    private void updateIteration(
            Mat quadSig,
            Pair<Integer, Integer> meshDim,
            Pair<Integer, Integer> desiredDim
    ) {
        Pair<Integer, Integer> quadDim =
                new Pair<>(
                        meshDim.first - 1,
                        meshDim.second - 1
                );


        for (int h = 0; h < meshDim.second; h++)
        {
            for (int w = 0; w < meshDim.first; w++)
            {
                float numerator = 0, denominator = 0;

                // search quad
                for (int i = 0; i < 4; i++)
                {

                }
            }
        }
    }

    void compute() {
        // TMP
        createMesh(
                meshDim,
                new Pair<>(convertImage.rows(), convertImage.cols()),
                desiredDim
        );
    }
}
