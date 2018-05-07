package kevin_quang.acertainimageeditor.ui.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.EGL14;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;

import kevin_quang.acertainimageeditor.R;
import kevin_quang.acertainimageeditor.tool.Const;
import kevin_quang.acertainimageeditor.tool.ImageDrawTool;
import kevin_quang.acertainimageeditor.tool.Tool;
import kevin_quang.acertainimageeditor.ui.renderer.EditDisplayRenderer;

/**
 * Created by Kevin on 3/24/2018.
 */

public class EditDisplaySurfaceView extends GLSurfaceView {

    private EditDisplayRenderer renderer;
    private Tool currentTool;

    class Config implements GLSurfaceView.EGLConfigChooser
    {
        @Override
        public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {
            int attribs[] = {
                    EGL14.EGL_LEVEL, 0,
                    EGL14.EGL_RENDERABLE_TYPE, 4,  // EGL_OPENGL_ES2_BIT
                    EGL14.EGL_COLOR_BUFFER_TYPE, EGL10.EGL_RGB_BUFFER,
                    EGL14.EGL_RED_SIZE, 8,
                    EGL14.EGL_GREEN_SIZE, 8,
                    EGL14.EGL_BLUE_SIZE, 8,
                    EGL14.EGL_DEPTH_SIZE, 16,
                    EGL14.EGL_SAMPLE_BUFFERS, 1,
                    EGL14.EGL_SAMPLES, 4,  // This is for 4x MSAA.
                    EGL14.EGL_NONE
            };
            EGLConfig[] configs = new EGLConfig[1];
            int[] configCounts = new int[1];
            egl.eglChooseConfig(display, attribs, configs, 1, configCounts);

            if (configCounts[0] == 0) {
                // Failed! Error handling.
                return null;
            } else {
                return configs[0];
            }
        }
    }

    public EditDisplaySurfaceView(Context context)
    {
        super(context);

        setEGLConfigChooser(new Config());

        setEGLContextClientVersion(Const.GL_VERSION);
        renderer = new EditDisplayRenderer();
        renderer.setContext(getContext());

        // set listener to tool
        setOnTouchListener(Tool.onTouch);

        ImageDrawTool tmp = new ImageDrawTool();

        // TODO: revert
        //LiquifyTool tmp = new LiquifyTool();
        Bitmap bMap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.test);

        renderer.setBitmap(bMap);

        setTool(tmp);

        setRenderer(renderer);
    }

    public void setTool(Tool tool)
    {
        currentTool = tool;
        renderer.setTool(currentTool);
    }

    public void setBitmap(Bitmap bitmap)
    {
        renderer.setBitmap(bitmap);
    }

    public void passArgs(Tool.Args args)
    {
        renderer.setArgs(args);
    }
    public int getBitmapWidth() {return renderer.getBitmapWidth();}
    public int getBitmapHeight() {return renderer.getBitmapHeight();}
    public void save(String path) {
        renderer.save(path, this);
    }
    public void rotate(int degrees) {renderer.rotate(degrees);}
    public void undo() {renderer.undo();}
    public void redo() {renderer.redo();}
    public boolean canUndo() {return renderer.canUndo();}
    public boolean canRedo() {return renderer.canRedo();}
    public void setToolColor(int color) {currentTool.setColor(color);}
    public int getToolColor() {return currentTool.getColor();}
    public void save(Bundle state) {
        Tool.saveHistory(state);
        currentTool.save(state);
    }
    public void restore(Bundle state) {
        Tool.restoreHistory(state);
        setBitmap(state.getParcelable("image"));
    }
}
