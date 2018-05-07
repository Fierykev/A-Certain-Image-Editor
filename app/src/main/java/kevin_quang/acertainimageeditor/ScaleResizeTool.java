package kevin_quang.acertainimageeditor;


import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES30;
import android.opengl.Matrix;
import android.util.Log;
import android.util.Pair;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Range;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.saliency.StaticSaliencySpectralResidual;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.opencv.core.CvType.CV_32F;
import static org.opencv.core.CvType.CV_32FC1;
import static org.opencv.core.CvType.CV_32FC2;
import static org.opencv.core.CvType.CV_8UC3;
import static org.opencv.imgproc.Imgproc.COLOR_BGR2GRAY;
import static org.opencv.imgproc.Imgproc.COLOR_RGBA2BGR;
import static org.opencv.imgproc.Imgproc.COLOR_RGBA2BGRA;


/**
 * Created by Kevin on 3/28/2018.
 */

public class ScaleResizeTool extends Tool {

    private int MAX_ITR = 33;
    private static final float MIN_MOVEMENT = .05f;
    private static final float LAMBDA = .01f;
    private static final int COMPUTE_FRAME = 1;

    private Mat convertImage, origImage, significance, quadMat;
    private Pair<Integer, Integer> vertDim;
    private GLHelper.VertexArray origVerts, verts;
    private FloatBuffer vertexBuffer;
    private FloatBuffer uvBuffer;
    private IntBuffer indexBuffer;
    private int vertBufferID[] = new int[1];
    private int indexBufferID[] = new int[1];

    private int indices[];
    private int vertID[] = new int[1];
    private int needsCompute = COMPUTE_FRAME + 1;

    // Note: width and height are swapped
    private Pair<Integer, Integer> meshDim
            = new Pair<>(50, 50);
    private Pair<Integer, Integer> desiredDim
            = new Pair<>(300, 100);

    public static final int RESIZE = 0;

    public static class ResizeArgs
    {
        int width, height, meshWidth, meshHeight, iterations;

        ResizeArgs(int width, int height, int meshWidth, int meshHeight, int iterations)
        {
            this.width = width;
            this.height = height;
            this.meshWidth = meshWidth;
            this.meshHeight = meshHeight;
            this.iterations = iterations;
        }
    }

    // TMP
    ScaleResizeTool()
    {

    }

    void init(Context context)
    {
        super.init(context);
        needsCompute = COMPUTE_FRAME + 1;
    }

    void destroy()
    {
        super.destroy();

        needsCompute = COMPUTE_FRAME + 1;

        GLES30.glDeleteBuffers(1, vertBufferID, 0);
        GLES30.glDeleteBuffers(1, indexBufferID, 0);
    }

    void load(Bitmap bitmap, boolean storeHistory)
    {
        super.load(bitmap, storeHistory);

        needsCompute = COMPUTE_FRAME + 1;

        origImage = new Mat();
        Utils.bitmapToMat(bitmap, origImage);
        Imgproc.cvtColor(origImage, origImage, COLOR_RGBA2BGRA);

        convertImage = origImage.clone();
        convertImage.convertTo(convertImage, CV_32F);
        Imgproc.cvtColor(convertImage, convertImage, COLOR_RGBA2BGR);
/*
        Imgproc.resize(
                convertImage,
                convertImage,
                new Size(
                        (float)convertImage.rows() / 4.f,
                        (float)convertImage.cols() / 4.f
                        )
        );
*/
        // setup display
        Pair<Integer, Integer> actualDim =
                new Pair<>(convertImage.rows(), convertImage.cols());
        desiredDim = actualDim;
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
                MAX_ITR = ((ResizeArgs)args.arg).iterations;

                // show bad resize for first frame
                createMesh(
                        meshDim,
                        new Pair<>(convertImage.rows(), convertImage.cols()),
                        desiredDim);
                createGLMesh();

                // init computation
                needsCompute = 0;

                break;
        }
    }

    @Override
    void onDraw(float aspectRatio, int width, int height) {
        if (needsCompute == COMPUTE_FRAME)
        {
            compute();
            super.onDraw(aspectRatio, width, height);
        } else if (needsCompute <= COMPUTE_FRAME) {
            needsCompute++;

            createMesh(
                    meshDim,
                    new Pair<>(convertImage.rows(), convertImage.cols()),
                    desiredDim
            );
            createGLMesh();

            Bitmap storage = super.image;
            super.image = renderToTex();

            super.onDraw(aspectRatio, width, height);

            super.image = storage;
        } else
        {
            super.onDraw(aspectRatio, width, height);
        }
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
        GLHelper.Vertex vertex = new GLHelper.Vertex();
        origVerts = new GLHelper.VertexArray(vertDim.first * vertDim.second);
        verts = new GLHelper.VertexArray(vertDim.first * vertDim.second);

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
        /*
        int maxSize = 1000;

        if (image.cols() < image.rows() &&
                maxSize < (float)image.rows())
        {
            Imgproc.resize(
                    image,
                    image,
                    new Size(
                            maxSize,
                            (int)((float)image.cols() * (float)maxSize / (float)image.rows()))
            );
        } else if (maxSize < (float)image.cols())
        {
            Imgproc.resize(
                    image,
                    image,
                    new Size(
                            (int)((float)image.rows() * (float)maxSize / (float)image.cols()),
                            maxSize)
            );
        }
*/
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

                Mat subMatrix = new Mat(
                        significance,
                        new Range(
                                (int) origVerts.get(w + h * meshDim.first).x,
                                (int)Math.ceil(origVerts.get((w + 1) + h * meshDim.first).x)
                        ),
                        new Range(
                                (int)origVerts.get(w + h * meshDim.first).y,
                                (int)Math.ceil(origVerts.get(w + (h + 1) * meshDim.first).y)
                        )
                );

                sum += Core.sumElems(subMatrix).val[0];

            /*
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
                */

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
        GLHelper.VertexArray lastVerts = verts.clone();

        Pair<Integer, Integer> quadDim =
                new Pair<>(
                        meshDim.first - 1,
                        meshDim.second - 1
                );

        // sf coefficient
        Mat sfCoeff =
                new Mat(quadDim.first, quadDim.second, CV_32FC1, new Scalar(0, 0, 0));
        {
            GLHelper.Point<Integer> edges[][] = new GLHelper.Point[][]{
                    {new GLHelper.Point<>(0, 0), new GLHelper.Point<>(1, 0)},
                    {new GLHelper.Point<>(1, 0), new GLHelper.Point<>(1, 1)},
                    {new GLHelper.Point<>(1, 1), new GLHelper.Point<>(0, 1)},
                    {new GLHelper.Point<>(0, 1), new GLHelper.Point<>(0, 0)}
            };

            float val[] = new float[1];

            for (int h = 0; h < quadDim.second; h++) {
                for (int w = 0; w < quadDim.first; w++) {
                    float numerator = 0, denominator = 0;

                    // search quad
                    for (int i = 0; i < 4; i++) {
                        GLHelper.Point<Integer> pX = (new GLHelper.Point<Integer>(w, h)).add(edges[i][0]);
                        GLHelper.Point<Integer> pY = (new GLHelper.Point<Integer>(w, h)).add(edges[i][1]);

                        int indexX = pX.x + pX.y * meshDim.first;
                        int indexY = pY.x + pY.y * meshDim.first;

                        GLHelper.Point<Float> origDelta = new GLHelper.Point<Float>(
                                origVerts.get(indexX).x - origVerts.get(indexY).x,
                                origVerts.get(indexX).y - origVerts.get(indexY).y
                        );
                        GLHelper.Point<Float> currentDelta = new GLHelper.Point<Float>(
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
                        GLHelper.Point<Float> origDelta = new GLHelper.Point<Float>(
                                origVerts.get(index).x - origVerts.get(nextIndexW).x,
                                origVerts.get(index).y - origVerts.get(nextIndexW).y
                        );
                        GLHelper.Point<Float> currentDelta = new GLHelper.Point<Float>(
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
                        GLHelper.Point<Float> origDelta = new GLHelper.Point<Float>(
                                origVerts.get(index).x - origVerts.get(nextIndexH).x,
                                origVerts.get(index).y - origVerts.get(nextIndexH).y
                        );
                        GLHelper.Point<Float> currentDelta = new GLHelper.Point<Float>(
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

            GLHelper.Point<Integer> dir[] = new GLHelper.Point[]{
                    new GLHelper.Point<Integer>(-1, -1),
                    new GLHelper.Point<Integer>(-1, 0),
                    new GLHelper.Point<Integer>(0, -1),
                    new GLHelper.Point<Integer>(0, 0)
            };

            GLHelper.Point<Integer> quadDir[] = new GLHelper.Point[]{
                    new GLHelper.Point<Integer>(-1, -1),
                    new GLHelper.Point<Integer>(-1, 1),
                    new GLHelper.Point<Integer>(1, -1),
                    new GLHelper.Point<Integer>(1, 1)
            };

            GLHelper.Point<Integer> edgeDir[] = new GLHelper.Point[]{
                    new GLHelper.Point<Integer>(0, -1),
                    new GLHelper.Point<Integer>(-1, 0),
                    new GLHelper.Point<Integer>(0, 1),
                    new GLHelper.Point<Integer>(1, 0)
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
                        GLHelper.Point<Integer> position =
                                new GLHelper.Point<Integer>(w, h);

                        int location = w + h * meshDim.first;

                        for (int j = 0; j < 4; j++)
                        {
                            GLHelper.Point<Integer> point =
                                    position.add(dir[j]);

                            GLHelper.Point<Integer> extreme =
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
                            GLHelper.Point<Integer> pt = position.clone();

                            if (edgeDir[j].x != 1)
                                pt.x += edgeDir[j].x;

                            if (edgeDir[j].y != 1)
                                pt.y += edgeDir[j].y;

                            GLHelper.Point<Integer> extreme =
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
                    GLHelper.Point<Integer> boundW[][] = new GLHelper.Point[][]{
                            {
                                    new GLHelper.Point<Integer>(0, 1),
                                    new GLHelper.Point<Integer>(quadDim.first, meshDim.first)
                            },
                            {
                                    new GLHelper.Point<Integer>(0, meshDim.first),
                                    new GLHelper.Point<Integer>(0, meshDim.first)
                            }
                    };

                    GLHelper.Point<Integer> boundH[][] = new GLHelper.Point[][]{
                            {
                                    new GLHelper.Point<Integer>(0, meshDim.second),
                                    new GLHelper.Point<Integer>(0, meshDim.second)
                            },
                            {
                                    new GLHelper.Point<Integer>(0, 1),
                                    new GLHelper.Point<Integer>(quadDim.second, meshDim.second)
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

    private void solver(
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

    private void clampVerts(
            Pair<Integer, Integer> desiredDim)
    {
        for (int i = 0; i < verts.numEls(); i++)
        {
            verts.put(i, 0, Math.min(Math.max(0.f, verts.get(i).x), desiredDim.first));
            verts.put(i, 1, Math.min(Math.max(0.f, verts.get(i).y), desiredDim.second));
        }
    }

    private void renderMesh()
    {
        GLES30.glEnable(GLES30.GL_TEXTURE_2D);

        GLES30.glUseProgram(program);

        // construct matrix
        Matrix.setIdentityM(world, 0);

        // now plane fills screen
        Matrix.scaleM(
                world,
                0,
                1.f,
                -1.f,
                1.f
        );
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
                1.f / (float)desiredDim.first * 2.f,
                -1.f / (float)desiredDim.second * 2.f,
                1.f);
       Matrix.translateM(
                world,
                0,
                -(float)desiredDim.first / 2.f,
                -(float)desiredDim.second / 2.f,
                0.f);

        GLES30.glBindBuffer (GLES30.GL_ARRAY_BUFFER, vertBufferID[0]);

        GLES30.glEnableVertexAttribArray(positionAttr);
        GLES30.glVertexAttribPointer(positionAttr, 3, GLES30.GL_FLOAT, false, 4 * 5, 0);

        GLES30.glEnableVertexAttribArray(texCoordAttr);
        GLES30.glVertexAttribPointer(texCoordAttr, 2, GLES30.GL_FLOAT, false, 4 * 5, 4 * 3);

        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureID);
        GLES30.glUniform1ui(textureUnif, textureID);

        GLES30.glUniformMatrix4fv(worldUnif, 1, false, world, 0);

        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, indexBufferID[0]);
        GLES30.glDrawElements(GLES30.GL_TRIANGLES, indices.length, GLES30.GL_UNSIGNED_INT, 0);

        GLES30.glDisableVertexAttribArray(positionAttr);
        GLES30.glDisableVertexAttribArray(texCoordAttr);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);
        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    private Bitmap renderToTex()
    {
        int[] frameBuffer = new int[1];

        GLES30.glGenFramebuffers(1, frameBuffer, 0);
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, frameBuffer[0]);

        int[] renderTex = new int[1];
        GLES30.glGenTextures(1, renderTex, 0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, renderTex[0]);

        GLES30.glTexImage2D(
                GLES30.GL_TEXTURE_2D,
                0,
                GLES30.GL_RGBA,
                desiredDim.second,
                desiredDim.first,
                0,
                GLES30.GL_RGBA,
                GLES30.GL_UNSIGNED_BYTE,
                null);

        GLES30.glFramebufferTexture2D(
                GLES30.GL_FRAMEBUFFER,
                GLES30.GL_COLOR_ATTACHMENT0,
                GLES30.GL_TEXTURE_2D,
                renderTex[0],
                0
        );

        GLES30.glDrawBuffers(1, new int[]{ GLES30.GL_COLOR_ATTACHMENT0 }, 0);

        int status = GLES30.glCheckFramebufferStatus(GLES30.GL_FRAMEBUFFER);

        if (status != GLES30.GL_FRAMEBUFFER_COMPLETE)
        {
            // TODO: some error
            Log.d("FB error", "Status: " + status);
        }

        // setup viewport
        GLES30.glClearColor(0, 0, 1, 1);
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT);
        GLES30.glViewport(0, 0, desiredDim.second, desiredDim.first);

        renderMesh();

        GLES30.glPixelStorei(GLES30.GL_PACK_ALIGNMENT, 1);
        GLES30.glPixelStorei(GLES30.GL_PACK_ROW_LENGTH, desiredDim.second);
        ByteBuffer buffer =
                ByteBuffer.allocateDirect(
                        desiredDim.first * desiredDim.second * 4);
        GLES30.glReadPixels(
                0, 0,
                desiredDim.second,
                desiredDim.first,
                GLES30.GL_RGBA,
                GLES30.GL_UNSIGNED_BYTE,
                buffer
        );

        Bitmap bitmap =
                Bitmap.createBitmap(
                        desiredDim.second,
                        desiredDim.first,
                        Bitmap.Config.ARGB_8888
                );
        bitmap.copyPixelsFromBuffer(buffer);

        GLES30.glDeleteTextures(1, renderTex, 0);
        GLES30.glDeleteFramebuffers(1, frameBuffer, 0);

        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);

        return bitmap;
    }

    private void compute() {
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

        this.load(renderToTex(), true);
    }
}
