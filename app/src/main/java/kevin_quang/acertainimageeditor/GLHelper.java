package kevin_quang.acertainimageeditor;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.GLUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by Kevin on 4/1/2018.
 */

public class GLHelper {

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
            Log.d("Shader loading failed", "Error:\n" + GLES20.glGetShaderInfoLog(shaderID));
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
}
