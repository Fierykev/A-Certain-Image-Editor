package kevin_quang.acertainimageeditor;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Kevin on 3/24/2018.
 */

public class EditDisplayRenderer implements GLSurfaceView.Renderer {

    private Tool currentTool;

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig eglConfig) {
        // background
        GLES20.glClearColor(0.f, 0.f, 0.f, 1.f);
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        // set viewport
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        // clear frame
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        // TODO:
        //currentTool.onDraw();
    }
}
