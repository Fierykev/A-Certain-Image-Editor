package kevin_quang.acertainimageeditor;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Kevin on 3/24/2018.
 */

public class EditDisplayRenderer implements GLSurfaceView.Renderer {

    private float aspectRatio = 1.f;
    private Tool.Args args;
    private Tool tool;
    private boolean toolInit = false, toolUpdate = false, argsUpdate = false;
    private Bitmap bitmap;
    private Context context;

    void setContext(Context context)
    {
        this.context = context;
    }

    void setArgs(Tool.Args args)
    {
        this.args = args;
        argsUpdate = true;
    }

    void setBitmap(Bitmap bitmap)
    {
        this.bitmap = bitmap;
        toolUpdate = true;
    }

    void setTool(Tool tool)
    {
        this.tool = tool;
        toolInit = true;
    }

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig eglConfig) {
        // background
        GLES30.glClearColor(0.f, 0.f, 0.f, 1.f);
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        // set viewport
        GLES30.glViewport(0, 0, width, height);
        aspectRatio = (float)width / (float)height;
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        // check for tool init
        if (toolInit) {
            this.tool.init(context);
            this.tool.load(bitmap);
            toolInit = false;
        }

        // check for tool update
        if (toolUpdate) {
            this.tool.load(bitmap);
            toolUpdate = false;
        }

        // pass args
        if (argsUpdate) {
            this.tool.setArgs(args);
            argsUpdate = false;
        }

        // clear frame
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT);

        tool.onDraw(aspectRatio);
    }

    public int getBitmapWidth() {return bitmap.getWidth();}
    public int getBitmapHeight() {return bitmap.getHeight();}
    public void save(String path) {}
}
