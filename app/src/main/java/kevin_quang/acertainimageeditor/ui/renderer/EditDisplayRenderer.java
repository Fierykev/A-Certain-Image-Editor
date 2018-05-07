package kevin_quang.acertainimageeditor.ui.renderer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.opengl.GLES31;
import android.opengl.GLSurfaceView;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import kevin_quang.acertainimageeditor.tool.GLHelper;
import kevin_quang.acertainimageeditor.tool.Tool;


/**
 * Created by Kevin on 3/24/2018.
 */

public class EditDisplayRenderer implements GLSurfaceView.Renderer {
    private float aspectRatio = 1.f;
    private Tool.Args args;
    private Tool tool = null, newTool = null;
    private boolean toolInit = false,
            toolUpdate = false,
            argsUpdate = false,
            undoUpdate = false,
            redoUpdate = false;
    private Bitmap bitmap;
    private Context context;

    private int width, height;

    private ArrayList<GLHelper.Point<Float>> pointList =
            new ArrayList<GLHelper.Point<Float>>();

    private Semaphore saveLock = new Semaphore(0);
    private AtomicInteger requestSave = new AtomicInteger(0);
    private AtomicReference<String> savePath = new AtomicReference<>();

    public synchronized void setContext(Context context)
    {
        this.context = context;
    }

    public synchronized void setArgs(Tool.Args args)
    {
        this.args = args;
        argsUpdate = true;
    }

    public synchronized void setBitmap(Bitmap bitmap)
    {
        this.bitmap = GLHelper.standardizeBitamp(bitmap);
        toolUpdate = true;
    }

    public synchronized void setTool(Tool tool)
    {
        this.newTool = tool;
        toolInit = true;
    }

    @Override
    public synchronized void onSurfaceCreated(GL10 unused, EGLConfig eglConfig) {
        // background
        GLES31.glClearColor(0.f, 0.f, 0.f, 1.f);
    }

    @Override
    public synchronized void onSurfaceChanged(GL10 unused, int width, int height) {
        // set viewport
        GLES31.glViewport(0, 0, width, height);
        aspectRatio = (float)width / (float)height;

        this.width = width;
        this.height = height;
    }

    @Override
    public synchronized void onDrawFrame(GL10 unused) {
        // check for tool init
        if (toolInit) {
            pointList.clear();

            Bitmap lastMap = bitmap;

            if (this.tool != null) {
                this.tool.destroy();
                lastMap = this.tool.image;
            }

            this.tool = newTool;

            this.tool.screenWidth = width;
            this.tool.screenHeight = height;

            this.tool.init(context);
            this.tool.load(lastMap, false);
            toolInit = false;
        }

        // check for tool update
        if (toolUpdate) {
            this.tool.load(bitmap, true);
            toolUpdate = false;
        }

        // pass args
        if (argsUpdate) {
            this.tool.setArgs(args);
            argsUpdate = false;
        }

        // check for undo
        if (undoUpdate) {
            this.tool.undo();
            undoUpdate = false;
        }

        // check for redo
        if (redoUpdate) {
            this.tool.redo();
            redoUpdate = false;
        }

        // store current bitmap as bitmap
        bitmap = this.tool.image;

        // request save
        if (requestSave.get() != 0)
        {
            tool.save(savePath.get());

            saveLock.release(requestSave.getAndSet(0));
        }

        // clear frame
        GLES31.glClear(GLES31.GL_COLOR_BUFFER_BIT);

        tool.onDraw(aspectRatio, width, height);
    }

    public synchronized int getBitmapWidth() {return bitmap.getWidth();}

    public synchronized int getBitmapHeight() {return bitmap.getHeight();}

    public void save(String path, GLSurfaceView view) {
        requestSave.addAndGet(1);
        savePath.set(path);

        view.requestRender();

        try {
            saveLock.acquire();
        } catch (InterruptedException e) {
            // TODO: deal with exception with message
            e.printStackTrace();
        }
    }

    public synchronized void rotate(int degrees) {
        Matrix mat = new Matrix();
        mat.postRotate(degrees);
        setBitmap(Bitmap.createBitmap(
                Bitmap.createScaledBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight(), true),
                0, 0, bitmap.getWidth(), bitmap.getHeight(), mat, true));
    }

    public synchronized void undo() {
        undoUpdate = true;
    }

    public synchronized void redo() {
        redoUpdate = true;
    }

    public synchronized boolean canUndo() {
        if (tool == null)
            return false;

        return tool.canUndo();
    }
    public synchronized boolean canRedo() {
        if (tool == null)
            return false;

        return tool.canRedo();
    }
}
