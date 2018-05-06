package kevin_quang.acertainimageeditor;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES30;
import android.opengl.Matrix;
import android.view.View;

import java.io.FileOutputStream;
import java.io.IOException;

import static kevin_quang.acertainimageeditor.GLHelper.loadProgram;
import static kevin_quang.acertainimageeditor.GLHelper.loadTexture;

/**
 * Created by Kevin on 3/24/2018.
 */

abstract class Tool {

    protected Bitmap image;

    protected static int program;
    protected static int postionAttr, texCoordAttr, textureUnif, worldUnif;

    protected GLHelper.DrawData data;
    protected float world[] = new float[16];
    protected int textureID;

    public static class Args
    {
        int type;
        Object arg;

        Args()
        {

        }

        Args(int type, Object arg)
        {
            this.type = type;
            this.arg = arg;
        }
    }

    private View.OnTouchListener touchListener;

    void init(Context context)
    {
        if (program == 0) {
            program = loadProgram(
                    "shaders/ScaleResizeTool/main.vs",
                    "shaders/ScaleResizeTool/main.fs",
                    context.getAssets());

            postionAttr = GLES30.glGetAttribLocation(program, "position");
            texCoordAttr = GLES30.glGetAttribLocation(program, "texCoord");
            textureUnif = GLES30.glGetUniformLocation(program, "texture");
            worldUnif = GLES30.glGetUniformLocation(program, "world");

            GLHelper.VertexArray verts = new GLHelper.VertexArray(4);
            verts.add(new GLHelper.Plane().verts);

            data = GLHelper.createBuffers(verts, new GLHelper.Plane().indices);
        }
    }

    void destroy()
    {
        GLES30.glDeleteProgram(program);

        if (textureID != 0)
            GLES30.glDeleteTextures(1, new int[] { textureID }, 0);
    }

    void load(Bitmap bitmap)
    {
        image = bitmap;

        if (textureID != 0)
            GLES30.glDeleteTextures(1, new int[] { textureID }, 0);

        textureID = loadTexture(bitmap);
    }

    void setArgs(Args args) { }

    void onDraw(float aspectRatio)
    {
        GLES30.glUseProgram(program);

        // construct matrix
        Matrix.setIdentityM(world, 0);

        float currentAspectRatio =
                (float)image.getWidth()  / (float)image.getHeight();
        float hS = Math.min(aspectRatio / currentAspectRatio, 1.f);
        float wS = Math.min(currentAspectRatio / aspectRatio, 1.f);

        Matrix.scaleM(
                world,
                0,
                wS,
                hS,
                1.f);

        GLES30.glBindBuffer (GLES30.GL_ARRAY_BUFFER, data.vertBufferID[0]);

        GLES30.glEnableVertexAttribArray(postionAttr);
        GLES30.glVertexAttribPointer(postionAttr, 3, GLES30.GL_FLOAT, false, 4 * 5, 0);

        GLES30.glEnableVertexAttribArray(texCoordAttr);
        GLES30.glVertexAttribPointer(texCoordAttr, 2, GLES30.GL_FLOAT, false, 4 * 5, 4 * 3);

        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureID);
        GLES30.glUniform1ui(textureUnif, textureID);

        GLES30.glUniformMatrix4fv(worldUnif, 1, false, world, 0);

        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, data.indexBufferID[0]);
        GLES30.glDrawElements(GLES30.GL_TRIANGLES, data.numIndices, GLES30.GL_UNSIGNED_INT, 0);

        GLES30.glDisableVertexAttribArray(postionAttr);
        GLES30.glDisableVertexAttribArray(texCoordAttr);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);
        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    abstract void getLeftMenu();

    abstract void getRightMenu();

    void save(String path)
    {
        FileOutputStream ostream = null;
        try
        {
            ostream = new FileOutputStream(path);
            image.compress(Bitmap.CompressFormat.PNG, 100, ostream);
        } catch (Exception e)
        {
            e.printStackTrace();
        } finally
        {
            try
            {
                if (ostream != null)
                {
                    ostream.close();
                }
            } catch (IOException e)
            {
                // TODO: do something about io issue
                e.printStackTrace();
            }
        }
    }

    View.OnTouchListener getTouchListener()
    {
        return touchListener;
    }

    void dismissMenu()
    {

    }
}
