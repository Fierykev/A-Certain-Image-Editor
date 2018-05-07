package kevin_quang.acertainimageeditor.tool;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.opengl.GLES30;
import android.opengl.Matrix;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

import static kevin_quang.acertainimageeditor.tool.GLHelper.loadProgram;
import static kevin_quang.acertainimageeditor.tool.GLHelper.loadTexture;

/**
 * Created by Kevin on 3/24/2018.
 */

public abstract class Tool {

    public static int screenWidth, screenHeight;

    public Bitmap image;

    protected static int program;
    protected static int positionAttr, texCoordAttr, textureUnif, worldUnif;
    protected static GLHelper.DrawData data;
    protected static int textureID;

    protected float world[] = new float[16];

    public static final int HIST_LEN = 5;
    protected static ArrayList<Bitmap> history = new ArrayList<>();
    protected static ArrayList<Bitmap> redoHist = new ArrayList<>();

    protected int color = Color.BLACK;
    private ReentrantLock renderLock = new ReentrantLock();

    public interface IHistoryUpdate {
        void updateUI();
    }
    public static IHistoryUpdate historyUpdate = null;

    public interface TouchLambda {
        public boolean onTouch(View view, MotionEvent motionEvent);
    }

    static class ToolTouch implements View.OnTouchListener
    {
        protected TouchLambda touchMethod = null;

        @Override
        public synchronized boolean onTouch(View view, MotionEvent motionEvent) {
            if (touchMethod == null)
                return false;

            return touchMethod.onTouch(view, motionEvent);
        }

        public synchronized void setTouchMethod(TouchLambda lambda)
        {
            touchMethod = lambda;
        }
    }

    public static ToolTouch onTouch = new ToolTouch();

    public static class Args
    {
        public int type;
        public Object arg;

        public Args()
        {

        }

        public Args(int type, Object arg)
        {
            this.type = type;
            this.arg = arg;
        }
    }

    public void init(Context context)
    {
        if (program == 0) {
            program = loadProgram(
                    "shaders/ScaleResizeTool/main.vs",
                    "shaders/ScaleResizeTool/main.fs",
                    context.getAssets());

            positionAttr = GLES30.glGetAttribLocation(program, "position");
            texCoordAttr = GLES30.glGetAttribLocation(program, "texCoord");
            textureUnif = GLES30.glGetUniformLocation(program, "texture");
            worldUnif = GLES30.glGetUniformLocation(program, "world");

            GLHelper.VertexArray verts = new GLHelper.VertexArray(4);
            verts.add(new GLHelper.Plane().verts);

            data = GLHelper.createBuffers(verts, new GLHelper.Plane().indices);
        }
    }

    public void destroy()
    {
//        GLES30.glDeleteProgram(program);

//        if (textureID != 0)
//            GLES30.glDeleteTextures(1, new int[] { textureID }, 0);

//        if (data != null)
//            data.destroy();
    }

    public synchronized void load(Bitmap bitmap, boolean storeHistory)
    {
        // clear listener
        setTouchLambda(null);

        forceTexLoad(bitmap, storeHistory);
    }

    protected synchronized void forceTexLoad(Bitmap bitmap, boolean storeHistory)
    {
        // history buffer
        if (storeHistory)
        {
            if (bitmap != null) {
                redoHist.clear();

                if (HIST_LEN <= history.size())
                    history.remove(0);

                history.add(bitmap.copy(bitmap.getConfig(), true));
                if(historyUpdate != null) {
                    historyUpdate.updateUI();
                }
            }
        }

        image = bitmap.copy(bitmap.getConfig(), true);

        if (textureID != 0)
            GLES30.glDeleteTextures(1, new int[] { textureID }, 0);

        textureID = loadTexture(image);
    }

    public boolean canUndo() {
        return history.size() > 1;
    }

    public boolean canRedo() {
        return redoHist.size() > 0;
    }

    public void undo()
    {
        if (history.size() <= 1)
            return;

        Bitmap undo = history.remove(history.size() - 1);
        redoHist.add(undo);

        this.load(history.get(history.size() - 1), false);
        if(historyUpdate != null) {
            historyUpdate.updateUI();
        }
    }

    public void redo()
    {
        if (redoHist.size() == 0)
            return;

        Bitmap redo = redoHist.remove(redoHist.size() - 1);
        history.add(redo);

        this.load(redo, false);
        if(historyUpdate != null) {
            historyUpdate.updateUI();
        }
    }

    public void setArgs(Args args) { }

    public void onDraw(float aspectRatio, int width, int height)
    {
        renderLock.lock();
        GLES30.glClearColor(0.f, 0.f, 0.f, 1.f);
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT);
        GLES30.glViewport(0, 0, width, height);

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

        GLES30.glEnableVertexAttribArray(positionAttr);
        GLES30.glVertexAttribPointer(positionAttr, 3, GLES30.GL_FLOAT, false, 4 * 5, 0);

        GLES30.glEnableVertexAttribArray(texCoordAttr);
        GLES30.glVertexAttribPointer(texCoordAttr, 2, GLES30.GL_FLOAT, false, 4 * 5, 4 * 3);

        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureID);
        GLES30.glUniform1ui(textureUnif, textureID);

        GLES30.glUniformMatrix4fv(worldUnif, 1, false, world, 0);

        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, data.indexBufferID[0]);
        GLES30.glDrawElements(GLES30.GL_TRIANGLES, data.numIndices, GLES30.GL_UNSIGNED_INT, 0);

        GLES30.glDisableVertexAttribArray(positionAttr);
        GLES30.glDisableVertexAttribArray(texCoordAttr);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);
        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, 0);
        renderLock.unlock();
    }

    protected void save(String path, Bitmap saveImg)
    {
        FileOutputStream ostream = null;
        try
        {
            ostream = new FileOutputStream(path);
            saveImg.compress(Bitmap.CompressFormat.PNG, 100, ostream);
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

    public void save(String path)
    {
        save(path, image);
    }

    synchronized void setTouchLambda(TouchLambda lambda)
    {
        onTouch.setTouchMethod(lambda);
    }

    public void setColor(int color) {
        renderLock.lock();
        this.color = color;
        renderLock.unlock();
    }
    public int getColor() {return color;}
    public static void saveHistory(Bundle state) {
        state.putParcelableArrayList("undo", history);
        state.putParcelableArrayList("redo", redoHist);
    }
    public void save(Bundle state) {
        state.putParcelable("image", image);
    }
    public static void restoreHistory(Bundle state) {
        history = state.getParcelableArrayList("undo");
        redoHist = state.getParcelableArrayList("redo");
    }
}
