package kevin_quang.acertainimageeditor;


import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES30;
import android.opengl.Matrix;
import android.os.Environment;
import android.util.Log;
import android.util.Pair;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.saliency.StaticSaliencySpectralResidual;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import static kevin_quang.acertainimageeditor.GLHelper.loadProgram;
import static kevin_quang.acertainimageeditor.GLHelper.loadTexture;
import static org.opencv.core.CvType.CV_32F;
import static org.opencv.core.CvType.CV_32FC1;
import static org.opencv.core.CvType.CV_32FC2;
import static org.opencv.core.CvType.CV_8UC3;
import static org.opencv.imgproc.Imgproc.COLOR_BGR2GRAY;
import static org.opencv.imgproc.Imgproc.COLOR_BGRA2BGR;


/**
 * Created by Kevin on 3/28/2018.
 */

public class ScaleResizeTool extends Tool {

    private static final int MAX_ITR = 33;
    private static final float MIN_MOVEMENT = .05f;
    private static final float LAMBDA = .01f;

    private Mat convertImage, origImage, significance, quadMat;
    private Pair<Integer, Integer> vertDim;
    private VertexArray origVerts, verts;
    private FloatBuffer vertexBuffer;
    private FloatBuffer uvBuffer;
    private IntBuffer indexBuffer;
    private int vertBufferID[] = new int[1];
    private int indexBufferID[] = new int[1];
    private int program;
    private int postionAttr, texCoordAttr, textureUnif, worldUnif;
    private int textureID;
    private int indices[];
    private int vertID[] = new int[1];
    private float world[] = new float[16];

    // Note: width and height are swapped
    private Pair<Integer, Integer> meshDim
            = new Pair<>(50, 25);
    private Pair<Integer, Integer> desiredDim
            = new Pair<>(300, 100);

    public static final int RESIZE = 0;

    public static class ResizeArgs
    {
        int width, height, meshWidth, meshHeight;

        ResizeArgs(int width, int height, int meshWidth, int meshHeight)
        {
            this.width = width;
            this.height = height;
            this.meshWidth = meshWidth;
            this.meshHeight = meshHeight;
        }
    }

    class Vertex
    {
        float x, y, z;
        float u, v;
    }

    class VertexArray
    {
        float[] floatArray;
        int index = 0;

        protected VertexArray clone()
        {
            VertexArray v = new VertexArray();

            v.floatArray = floatArray.clone();
            v.index = index;

            return v;
        }

        private VertexArray()
        {

        }

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

            floatArray[index] = v.u;
            index++;

            floatArray[index] = v.v;
            index++;
        }

        int size()
        {
            return floatArray.length * 4;
        }

        int numEls()
        {
            return floatArray.length / 5;
        }

        float[] getFloatArray()
        {
            return floatArray;
        }

        Vertex get(int in)
        {
            Vertex v = new Vertex();
            v.x = floatArray[in * 5];
            v.y = floatArray[in * 5 + 1];
            v.z = floatArray[in * 5 + 2];
            v.u = floatArray[in * 5 + 3];
            v.v = floatArray[in * 5 + 4];

            return v;
        }

        void put(int in, int ind, float v)
        {
            switch (ind) {
                case 0:
                    floatArray[in * 5] = v;
                    break;
                case 1:
                    floatArray[in * 5 + 1] = v;
                    break;
                case 2:
                    floatArray[in * 5 + 2] = v;
                    break;
                case 3:
                    floatArray[in * 5 + 3] = v;
                    break;
                case 4:
                    floatArray[in * 5 + 4] = v;
                    break;
            }
        }
    }

    class Point<T extends Number>
    {
        T x, y;

        Point(T x, T y)
        {
            this.x = x;
            this.y = y;
        }

        protected Point<T> clone()
        {
            return new Point<T>(x, y);
        }

        Point add(Point p)
        {
            return new Point(addNumbers(x, p.x), addNumbers(y, p.y));
        }

        Point sub(Point p)
        {
            return new Point(subNumbers(x, p.x), subNumbers(y, p.y));
        }

        float norm()
        {
            return (float) Math.sqrt(x.floatValue() * x.floatValue() + y.floatValue() * y.floatValue());
        }

        Number addNumbers(Number a, Number b) {
            if(a instanceof Double || b instanceof Double)
            {
                return new Double(a.doubleValue() + b.doubleValue());
            }
            else if(a instanceof Float || b instanceof Float) {
                return new Float(a.floatValue() + b.floatValue());
            }
            else if(a instanceof Long || b instanceof Long) {
                return new Long(a.longValue() + b.longValue());
            }
            else
            {
                return new Integer(a.intValue() + b.intValue());
            }
        }

        Number subNumbers(Number a, Number b) {
            if(a instanceof Double || b instanceof Double)
            {
                return new Double(a.doubleValue() - b.doubleValue());
            }
            else if(a instanceof Float || b instanceof Float) {
                return new Float(a.floatValue() - b.floatValue());
            }
            else if(a instanceof Long || b instanceof Long) {
                return new Long(a.longValue() - b.longValue());
            }
            else
            {
                return new Integer(a.intValue() - b.intValue());
            }
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
        worldUnif = GLES30.glGetUniformLocation(program, "world");
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
        convertImage.convertTo(convertImage, CV_32F);
        Imgproc.cvtColor(convertImage, convertImage, COLOR_BGRA2BGR);

        // setup display
        Pair<Integer, Integer> actualDim =
                new Pair<>(convertImage.rows(), convertImage.cols());
        desiredDim = actualDim;

        createMesh(meshDim, actualDim, desiredDim);
        createGLMesh();
    }

    @Override
    void setArgs(Args args)
    {
        switch (args.type)
        {
            case RESIZE:

                desiredDim =
                        new Pair<>(
                                ((ResizeArgs) args.arg).height,
                                ((ResizeArgs) args.arg).width);
                meshDim =
                        new Pair<>(
                                ((ResizeArgs)args.arg).meshHeight,
                                ((ResizeArgs)args.arg).meshWidth);
                compute();

                break;
        }
    }

    @Override
    void onDraw(float aspectRatio) {
        GLES30.glEnable(GLES30.GL_TEXTURE_2D);

        GLES30.glUseProgram(program);

        // construct matrix
        Matrix.setIdentityM(world, 0);

        // now plane fills screen
        // fix for aspect ratio
        float currentAspectRatio =
                (float)desiredDim.second  / (float)desiredDim.first;
        float hS = Math.min(aspectRatio / currentAspectRatio, 1.f);
        float wS = Math.min(currentAspectRatio / aspectRatio, 1.f);

        Matrix.rotateM(
                world,
                0,
                90.f,
                0.f,
                0.f,
                1.f
        );
        Matrix.scaleM(
                world,
                0,
                1.f / (float)desiredDim.first * 2.f * hS,
                -1.f / (float)desiredDim.second * 2.f * wS,
                1.f);
        Matrix.translateM(
                world,
                0,
                -(float)desiredDim.first / 2.f,
                -(float)desiredDim.second / 2.f,
                0.f);

        GLES30.glBindBuffer (GLES30.GL_ARRAY_BUFFER, vertBufferID[0]);

        GLES30.glEnableVertexAttribArray(postionAttr);
        GLES30.glVertexAttribPointer(postionAttr, 3, GLES30.GL_FLOAT, false, 4 * 5, 0);

        GLES30.glEnableVertexAttribArray(texCoordAttr);
        GLES30.glVertexAttribPointer(texCoordAttr, 2, GLES30.GL_FLOAT, false, 4 * 5, 4 * 3);

        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureID);
        GLES30.glUniform1ui(textureUnif, textureID);

        GLES30.glUniformMatrix4fv(worldUnif, 1, false, world, 0);

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
        origVerts = new VertexArray(vertDim.first * vertDim.second);
        verts = new VertexArray(vertDim.first * vertDim.second);

        for (int y = 0; y < vertDim.second; y++) {
            for (int x = 0; x < vertDim.first; x++) {
                vertex.x =
                        (float)Math.min(
                                meshDim.first * x
                                        + Math.round(
                                        (float) (overflowDim.first * x)
                                                / (float) (quadDim.first)
                                ),
                                actualDim.first
                        );
                vertex.y =
                        (float)Math.min(
                                meshDim.second * y
                                        + Math.round(
                                        (float) (overflowDim.second * y)
                                                / (float) (quadDim.second)
                                ),
                                actualDim.second
                        );
                vertex.z = .1f;

                vertex.u =
                    (float)(vertex.y)
                            / (float)(actualDim.second);
                vertex.v =
                    1.f - (float)(vertex.x)
                            / (float)(actualDim.first);

                origVerts.add(vertex);

                vertex.x *= (float)targetDim.first / (float)actualDim.first;
                vertex.y *= (float)targetDim.second / (float)actualDim.second;

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
            }
        }
    }

    private void createGLMesh()
    {
        if (vertBufferID[0] != 0)
            GLES30.glDeleteBuffers(1, vertBufferID, 0);

        if (indexBufferID[0] != 0)
            GLES30.glDeleteBuffers(1, indexBufferID, 0);

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
            Imgproc.Sobel(image, d.first, image.depth(), 1, 0);
            Imgproc.Sobel(image, d.second, image.depth(), 0, 1);

            norm = new Mat(d.first.size(), d.first.type());
            Core.magnitude(d.first, d.second, norm);

            Imgproc.cvtColor(norm, norm, COLOR_BGR2GRAY);
            Core.divide(norm, new Scalar(255), norm);
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
            significance = norm.mul(saliency);
        }

        saveMat(significance);
    }

    private void createQuadMat(Pair<Integer, Integer> meshDim)
    {
        quadMat = new Mat(
                vertDim.first - 1,
                vertDim.second - 1,
                CV_32FC1,
                new Scalar(0, 0, 0));

        float sum;

        for (int h = 0; h < meshDim.second - 1; h++)
        {
            for (int w = 0; w < meshDim.first - 1; w++)
            {
                sum = 0;

                for (int y = (int) origVerts.get(w + h * meshDim.first).y;
                     y < origVerts.get(w + (h + 1) * meshDim.first).y;
                     y++)
                {
                    for (int x = (int) origVerts.get(w + h * meshDim.first).x;
                         x < origVerts.get((w + 1) + h * meshDim.first).x;
                         x++)
                    {
                        float v[] = new float[1];
                        significance.get(x, y, v);
                        sum += v[0];
                    }
                }

                quadMat.put(w, h, sum);
            }
        }

        Scalar c = new Scalar(1.0 / Core.sumElems(quadMat).val[0]);
        Core.multiply(quadMat, c, quadMat);
    }

    int Z = 0;
    private float updateIteration(
            Mat quadSig,
            Pair<Integer, Integer> meshDim,
            Pair<Integer, Integer> desiredDim
    ) {
        // clone verts
        VertexArray lastVerts = verts.clone();

        Pair<Integer, Integer> quadDim =
                new Pair<>(
                        meshDim.first - 1,
                        meshDim.second - 1
                );

        // sf coefficient
        Mat sfCoeff =
                new Mat(quadDim.first, quadDim.second, CV_32FC1, new Scalar(0, 0, 0));
        {
            Point<Integer> edges[][] = new Point[][]{
                    {new Point<>(0, 0), new Point<>(1, 0)},
                    {new Point<>(1, 0), new Point<>(1, 1)},
                    {new Point<>(1, 1), new Point<>(0, 1)},
                    {new Point<>(0, 1), new Point<>(0, 0)}
            };

            float val[] = new float[1];

            for (int h = 0; h < quadDim.second; h++) {
                for (int w = 0; w < quadDim.first; w++) {
                    float numerator = 0, denominator = 0;

                    // search quad
                    for (int i = 0; i < 4; i++) {
                        Point<Integer> pX = (new Point<Integer>(w, h)).add(edges[i][0]);
                        Point<Integer> pY = (new Point<Integer>(w, h)).add(edges[i][1]);

                        int indexX = pX.x + pX.y * meshDim.first;
                        int indexY = pY.x + pY.y * meshDim.first;

                        Point<Float> origDelta = new Point<Float>(
                                origVerts.get(indexX).x - origVerts.get(indexY).x,
                                origVerts.get(indexX).y - origVerts.get(indexY).y
                        );
                        Point<Float> currentDelta = new Point<Float>(
                                verts.get(indexX).x - verts.get(indexY).x,
                                verts.get(indexX).y - verts.get(indexY).y
                        );

                        numerator += origDelta.x * currentDelta.x + origDelta.y * currentDelta.y;

                        denominator += origDelta.norm() * origDelta.norm();
                    }

                    sfCoeff.put(w, h, numerator / denominator);
                }
            }
        }

        // bending coefficient
        Mat bendCoeff =
                new Mat(meshDim.first, meshDim.second, CV_32FC2, new Scalar(0, 0, 0));
        {
            float val[] = new float[2];

            for (int h = 0; h < meshDim.second; h++) {
                for (int w = 0; w < meshDim.first; w++) {
                    int index = w + h * meshDim.first;
                    int nextIndexW = index + 1;
                    int nextIndexH = index + meshDim.first;

                    if (w < quadDim.first)
                    {
                        Point<Float> origDelta = new Point<Float>(
                                origVerts.get(index).x - origVerts.get(nextIndexW).x,
                                origVerts.get(index).y - origVerts.get(nextIndexW).y
                        );
                        Point<Float> currentDelta = new Point<Float>(
                                verts.get(index).x - verts.get(nextIndexW).x,
                                verts.get(index).y - verts.get(nextIndexW).y
                        );

                        val[0] = currentDelta.norm() / origDelta.norm();
                    }
                    else
                    {
                        val[0] = 0.f;
                    }

                    if (h < quadDim.second)
                    {
                        Point<Float> origDelta = new Point<Float>(
                                origVerts.get(index).x - origVerts.get(nextIndexH).x,
                                origVerts.get(index).y - origVerts.get(nextIndexH).y
                        );
                        Point<Float> currentDelta = new Point<Float>(
                                verts.get(index).x - verts.get(nextIndexH).x,
                                verts.get(index).y - verts.get(nextIndexH).y
                        );

                        val[1] = currentDelta.norm() / origDelta.norm();
                    }
                    else
                    {
                        val[1] = 0.f;
                    }

                    bendCoeff.put(w, h, val);
                }
            }
        }

        // quad coeffs
        Mat quadCoeffs =
                new Mat(quadDim.first, quadDim.second, CV_32FC1, new Scalar(0, 0, 0));
        {
            float val[] = new float[2];
            float val1[] = new float[1];

            Point<Integer> dir[] = new Point[]{
                    new Point<Integer>(-1, -1),
                    new Point<Integer>(-1, 0),
                    new Point<Integer>(0, -1),
                    new Point<Integer>(0, 0)
            };

            Point<Integer> quadDir[] = new Point[]{
                    new Point<Integer>(-1, -1),
                    new Point<Integer>(-1, 1),
                    new Point<Integer>(1, -1),
                    new Point<Integer>(1, 1)
            };

            Point<Integer> edgeDir[] = new Point[]{
                    new Point<Integer>(0, -1),
                    new Point<Integer>(-1, 0),
                    new Point<Integer>(0, 1),
                    new Point<Integer>(1, 0)
            };

            for (int i = 0; i < 2; i++)
            {
                Mat mat[] = new Mat[2];
                mat[0] =
                        new Mat(
                                meshDim.first * meshDim.second,
                                meshDim.first * meshDim.second,
                                CV_32FC1,
                                new Scalar(0, 0, 0));
                mat[1] =
                        new Mat(
                                meshDim.first * meshDim.second,
                                1,
                                CV_32FC1,
                                new Scalar(0, 0, 0));

                for (int h = 0; h < meshDim.second; h++) {
                    for (int w = 0; w < meshDim.first; w++) {
                        Point<Integer> position =
                                new Point<Integer>(w, h);

                        int location = w + h * meshDim.first;

                        for (int j = 0; j < 4; j++)
                        {
                            Point<Integer> point =
                                    position.add(dir[j]);

                            Point<Integer> extreme =
                                    position.add(quadDir[j]);

                            if (extreme.x < 0 ||
                                    meshDim.first <= extreme.x ||
                                    extreme.y < 0 ||
                                    meshDim.second <= extreme.y)
                                continue;

                            float valWf[] = new float[1];
                            quadSig.get(point.x, point.y, valWf);
                            float wf = valWf[0];

                            float valSf[] = new float[1];
                            sfCoeff.get(point.x, point.y, valSf);
                            float sf = valSf[0];

                            mat[0].get(location, location, val1);
                            mat[0].put(location, location, val1[0] + 2.f * wf);

                            mat[0].get(
                                    location,
                                    location + quadDir[j].x,
                                    val1);
                            mat[0].put(
                                    location,
                                    location + quadDir[j].x,
                                    val1[0] - wf);

                            mat[0].get(
                                    location,
                                    location + quadDir[j].y * meshDim.first,
                                    val1);
                            mat[0].put(
                                    location,
                                    location + quadDir[j].y * meshDim.first,
                                    val1[0] - wf);

                            if (i == 0)
                            {
                                mat[1].get(
                                        location,
                                        0,
                                        val1
                                        );
                                mat[1].put(
                                        location,
                                        0,
                                        val1[0] + wf * sf
                                               * (2.f * origVerts.get(location).x -
                                                origVerts.get(location + quadDir[j].x).x
                                                - origVerts.get(location + quadDir[j].y * meshDim.first).x));
                            }
                            else
                            {
                                mat[1].get(
                                        location,
                                        0,
                                        val1
                                );
                                mat[1].put(
                                        location,
                                        0,
                                        val1[0] + wf * sf
                                                * (2.f * origVerts.get(location).y -
                                                origVerts.get(location + quadDir[j].x).y
                                                - origVerts.get(location + quadDir[j].y * meshDim.first).y));
                            }
                        }

                        // bending
                        for (int j = 0; j < 4; j++) {
                            // copy point
                            Point<Integer> pt = position.clone();

                            if (edgeDir[j].x != 1)
                                pt.x += edgeDir[j].x;

                            if (edgeDir[j].y != 1)
                                pt.y += edgeDir[j].y;

                            Point<Integer> extreme =
                                    position.add(edgeDir[j]);

                            if (extreme.x < 0 ||
                                    meshDim.first <= extreme.x ||
                                    extreme.y < 0 ||
                                    meshDim.second <= extreme.y)
                                continue;

                            float scale;
                            bendCoeff.get(pt.x, pt.y, val);

                            if (j % 2 == 0)
                                scale = val[0];
                            else
                                scale = val[1];

                            mat[0].get(location, location, val);
                            mat[0].put(location, location, val[0] + LAMBDA);

                            int fetchX = location + edgeDir[j].x;
                            int fetchY = location + edgeDir[j].y * meshDim.first;
                            mat[0].get(fetchX, fetchY, val);
                            mat[0].put(
                                    fetchX,
                                    fetchY,
                                    val[0] - LAMBDA);

                            float value;

                            if (i == 0)
                                value =
                                        scale * (
                                                origVerts.get(location).x -
                                                        origVerts.get(
                                                                location + edgeDir[j].x
                                                                        + edgeDir[j].y * meshDim.first).x
                                        ) * LAMBDA;
                            else
                                value =
                                        scale * (
                                                origVerts.get(location).y -
                                                        origVerts.get(
                                                                location + edgeDir[j].x
                                                                        + edgeDir[j].y * meshDim.first).y
                                        ) * LAMBDA;

                            mat[1].get(
                                    0,
                                    0,
                                    val1);
                            mat[1].put(
                                    0,
                                    0,
                                    val1[0] + value);
                        }
                    }
                }

                // constraints
                {
                    Point<Integer> boundW[][] = new Point[][]{
                            {
                                    new Point<Integer>(0, 1),
                                    new Point<Integer>(quadDim.first, meshDim.first)
                            },
                            {
                                    new Point<Integer>(0, meshDim.first),
                                    new Point<Integer>(0, meshDim.first)
                            }
                    };

                    Point<Integer> boundH[][] = new Point[][]{
                            {
                                    new Point<Integer>(0, meshDim.second),
                                    new Point<Integer>(0, meshDim.second)
                            },
                            {
                                    new Point<Integer>(0, 1),
                                    new Point<Integer>(quadDim.second, meshDim.second)
                            }
                    };

                    for (int j = 0; j < 2; j++) {
                        for (int h = boundH[i][j].x; h < boundH[i][j].y; h++) {
                            for (int w = boundW[i][j].x; w < boundW[i][j].y; w++) {
                                int location = w + h * meshDim.first;

                                for (int clear = 0;
                                     clear < meshDim.first * meshDim.second;
                                     clear++)
                                    mat[0].put(location, clear, 0);

                                mat[0].put(location, location, 1);

                                if (j % 2 == 0)
                                    mat[1].put(location, 0, 0);
                                else
                                    mat[1].put(location, 0, i == 0 ? desiredDim.first : desiredDim.second);
                            }
                        }
                    }
                }

                // solve system
                Mat answ = new Mat();
                Core.solve(mat[0], mat[1], answ);

                // get last answ
                Mat lastAnsw =
                        new Mat(
                                meshDim.first * meshDim.second,
                                1,
                                CV_32FC1,
                                new Scalar(0, 0, 0));

                for (int h = 0; h < meshDim.second; h++) {
                    for (int w = 0; w < meshDim.first; w++) {
                        if (i == 0) {
                            lastAnsw.put(w + h * meshDim.first,
                                    0,
                                    verts.get(w + h * meshDim.first).x
                            );
                        }
                        else
                        {
                            lastAnsw.put(w + h * meshDim.first,
                                    0,
                                    verts.get(w + h * meshDim.first).y
                            );
                        }
                    }
                }

                Scalar c = new Scalar(.7f);
                Core.multiply(answ, c, answ);

                c = new Scalar(.3f);
                Core.multiply(lastAnsw, c, lastAnsw);

                Core.add(answ, lastAnsw, answ);

                // apply
                for (int h = 0; h < meshDim.second; h++) {
                    for (int w = 0; w < meshDim.first; w++) {
                        answ.get(w + h * meshDim.first, 0, val1);
                        verts.put(w + h * meshDim.first, i, val1[0]);
                    }
                }
            }
        }

        // get movement squared
        float maxMoveSq = 0;

        for (int i = 0; i < lastVerts.numEls(); i++)
        {
            maxMoveSq =
                Math.max(
                        (lastVerts.get(i).x - verts.get(i).x) * (lastVerts.get(i).x - verts.get(i).x)
                        + (lastVerts.get(i).y - verts.get(i).y) * (lastVerts.get(i).y - verts.get(i).y),
                        maxMoveSq
                );
        }

        return (float)Math.sqrt(maxMoveSq);
    }

    void solver(
            Mat quadSig,
            Pair<Integer, Integer> meshDim,
            Pair<Integer, Integer> desiredDim
    )
    {
        float movement = Float.MAX_VALUE;

        for (int it = 0; MIN_MOVEMENT < movement && it < MAX_ITR; it++)
        {
            Log.d("Scale Resize Tool", "Iteration Number " + it);

            movement = updateIteration(quadSig, meshDim, desiredDim);

            Log.d("Scale Resize Tool", "Movement " + movement);
        }
    }

    void saveMat(Mat mat) {
        String[] permissions = {
                "android.permission.READ_EXTERNAL_STORAGE",
                "android.permission.WRITE_EXTERNAL_STORAGE"
        };

        File file = new File(
                Environment.getExternalStorageDirectory()
                        + File.separator + "fame.png");
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Mat sMat = new Mat();
        mat.convertTo(sMat, CV_8UC3, 255.0);
        boolean b = Imgcodecs.imwrite(file.getAbsolutePath(), sMat);
    }

    void clampVerts(
            Pair<Integer, Integer> desiredDim)
    {
        for (int i = 0; i < verts.numEls(); i++)
        {
            verts.put(i, 0, Math.min(Math.max(0.f, verts.get(i).x), desiredDim.first));
            verts.put(i, 1, Math.min(Math.max(0.f, verts.get(i).y), desiredDim.second));
        }
    }

    void compute() {
        createMesh(
                meshDim,
                new Pair<>(convertImage.rows(), convertImage.cols()),
                desiredDim
        );

        createSignificanceMap(convertImage);

        createQuadMat(vertDim);

        solver(quadMat, vertDim, desiredDim);

        // TODO: force triangles not to intersect with each other
        clampVerts(desiredDim);

        createGLMesh();
    }
}
