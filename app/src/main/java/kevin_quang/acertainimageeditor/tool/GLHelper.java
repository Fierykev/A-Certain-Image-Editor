package kevin_quang.acertainimageeditor.tool;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES30;
import android.opengl.GLUtils;
import android.util.Log;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.opencv.core.CvType.CV_8UC3;

/**
 * Created by Kevin on 4/1/2018.
 */

public class GLHelper {

    public static class Vertex
    {
        float x, y, z;
        float u, v;

        public Vertex() {}

        public Vertex(float x, float y, float z, float u, float v)
        {
            this.x = x;
            this.y = y;
            this.z = z;
            this.u = u;
            this.v = v;
        }
    }

    public static class Point<T extends Number>
    {
        public T x, y;

        public Point(T x, T y)
        {
            this.x = x;
            this.y = y;
        }

        public Point()
        {

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

        double distance(Point p)
        {
            return Math.sqrt(
                    subNumbers(x, p.x).doubleValue() * subNumbers(x, p.x).doubleValue()
                            + subNumbers(y, p.y).doubleValue() * subNumbers(y, p.y).doubleValue());
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

    public static class VertexArray
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

        void add(Vertex vArr[])
        {
            for (Vertex v : vArr) {
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

        void put(int in, Vertex v)
        {
            floatArray[in * 5] = v.x;
            floatArray[in * 5 + 1] = v.y;
            floatArray[in * 5 + 2] = v.z;
            floatArray[in * 5 + 3] = v.u;
            floatArray[in * 5 + 4] = v.v;
        }
    }

    static class Plane
    {
        Vertex verts[] = {
                new Vertex(-1, 1, 0, 0, 0),
                new Vertex(1, 1, 0, 1, 0),
                new Vertex(1, -1, 0, 1, 1),
                new Vertex(-1, -1, 0, 0, 1)
        };

        int indices[] = {
                0, 1, 2,
                0, 2, 3
        };
    }

    static class DrawData
    {
        FloatBuffer vertexBuffer;
        IntBuffer indexBuffer;

        int[] vertBufferID;
        int[] indexBufferID;

        int numIndices;

        void destroy()
        {
            if (vertBufferID[0] != 0)
                GLES30.glDeleteBuffers(1, vertBufferID, 0);

            if (indexBufferID[0] != 0)
                GLES30.glDeleteBuffers(1, indexBufferID, 0);
        }
    }

    public static String loadFile(String file, AssetManager ag)
    {
        InputStream is;
        try {
            is = ag.open(file);
        } catch (IOException e) {
            Log.d("Load File", "Could not load file " + file);
            return new String();
        }

        BufferedReader in =
                new BufferedReader(new InputStreamReader(is));

        StringBuilder sb = new StringBuilder();
        String line;
        try {
            while ((line = in.readLine()) != null)
                sb.append(line);
        } catch (IOException e) {
            Log.d("Load File", "Could not read file " + file);
            return new String();
        }

        try {
            in.close();
        } catch (IOException e) {
            // do nothing
        }

        return sb.toString();
    }

    public static int loadShader(String shader, int type) {
        int[] compile = new int[1];
        int shaderID = GLES30.glCreateShader(type);
        GLES30.glShaderSource(shaderID, shader);
        GLES30.glCompileShader(shaderID);
        GLES30.glGetShaderiv(shaderID, GLES30.GL_COMPILE_STATUS, compile, 0);

        if (compile[0] == 0)
        {
            Log.d("Shader loading failed", "Error:\n" + GLES30.glGetShaderInfoLog(shaderID));
            return 0;
        }

        return shaderID;
    }

    public static int loadProgram(String vertexShaderFile,
                                  String fragmentShaderFile,
                                  AssetManager ag) {
        return loadProgram(loadFile(vertexShaderFile, ag), loadFile(fragmentShaderFile, ag));
    }

    public static int loadProgram(String vertexShaderFile, String fragmentShaderFile) {
        int vertexShaderProgram = loadShader(vertexShaderFile, GLES30.GL_VERTEX_SHADER);

        if (vertexShaderProgram == 0)
        {
            Log.d("Loading Program", "VS Failure");
            return 0;
        }

        int fragmentShaderProgram = loadShader(fragmentShaderFile, GLES30.GL_FRAGMENT_SHADER);
        if (fragmentShaderProgram == 0)
        {
            Log.d("Loading Program", "FS Failure");
            return 0;
        }

        int programID;
        programID = GLES30.glCreateProgram();
        GLES30.glAttachShader(programID, vertexShaderProgram);
        GLES30.glAttachShader(programID, fragmentShaderProgram);

        GLES30.glLinkProgram(programID);

        int link[] = new int[1];
        GLES30.glGetProgramiv(programID, GLES30.GL_LINK_STATUS,  link, 0);
        if (link[0] <= 0)
        {
            Log.d("Loading Program", "Link Failed");
            return 0;
        }

        GLES30.glDeleteShader(vertexShaderProgram);
        GLES30.glDeleteShader(fragmentShaderProgram);

        return programID;
    }

    public static int loadTexture(String file, Context context)
    {
        InputStream is = null;

        try
        {
            is = context.getAssets().open(file);
        }
        catch (IOException e)
        {
            return 0;
        }

        if (is == null)
            return 0;

        Bitmap bitmap = BitmapFactory.decodeStream(is);

        return loadTexture(bitmap);
    }

    public static int loadTexture(Bitmap bitmap)
    {
        int texture[] = new int[1];

        GLES30.glGenTextures(1, texture, 0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, texture[0]);

        GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bitmap, 0);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);

        return texture[0];
    }

    public static DrawData createBuffers(VertexArray verts, int[] indices)
    {
        DrawData data = new DrawData();

        data.vertBufferID = new int[1];
        GLES30.glGenBuffers (1, data.vertBufferID, 0);

        GLES30.glBindBuffer (GLES30.GL_ARRAY_BUFFER, data.vertBufferID[0]);
        GLES30.glBufferData (GLES30.GL_ARRAY_BUFFER, verts.size(),
                null, GLES30.GL_STATIC_DRAW );

        data.vertexBuffer =
                ((ByteBuffer) GLES30.glMapBufferRange (
                        GLES30.GL_ARRAY_BUFFER, 0, verts.size(),
                        GLES30.GL_MAP_WRITE_BIT | GLES30.GL_MAP_INVALIDATE_BUFFER_BIT)
                ).order(ByteOrder.nativeOrder()).asFloatBuffer();
        data.vertexBuffer.put(verts.getFloatArray()).position(0);

        GLES30.glUnmapBuffer(GLES30.GL_ARRAY_BUFFER);

        data.indexBufferID = new int[1];
        GLES30.glGenBuffers (1, data.indexBufferID, 0);

        GLES30.glBindBuffer (GLES30.GL_ELEMENT_ARRAY_BUFFER, data.indexBufferID[0]);
        GLES30.glBufferData (GLES30.GL_ELEMENT_ARRAY_BUFFER, indices.length * 4,
                null, GLES30.GL_STATIC_DRAW);

        data.indexBuffer =
                ((ByteBuffer) GLES30.glMapBufferRange (
                        GLES30.GL_ELEMENT_ARRAY_BUFFER, 0, indices.length * 4,
                        GLES30.GL_MAP_WRITE_BIT | GLES30.GL_MAP_INVALIDATE_BUFFER_BIT )
                ).order(ByteOrder.nativeOrder()).asIntBuffer();
        data.indexBuffer.put(indices).position(0);

        GLES30.glUnmapBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER);

        data.numIndices = indices.length;

        return data;
    }

    public static void saveMat(Mat mat, String path) {
        String[] permissions = {
                "android.permission.READ_EXTERNAL_STORAGE",
                "android.permission.WRITE_EXTERNAL_STORAGE"
        };

        File file = new File(path);
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Mat sMat = new Mat();
        mat.convertTo(sMat, CV_8UC3, 1.0);
        boolean b = Imgcodecs.imwrite(file.getAbsolutePath(), sMat);
    }

    public static Bitmap standardizeBitamp(Bitmap image)
    {
        return image.copy(Bitmap.Config.ARGB_8888, true);
    }
}
