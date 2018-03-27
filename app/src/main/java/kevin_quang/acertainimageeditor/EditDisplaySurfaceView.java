package kevin_quang.acertainimageeditor;

import android.content.Context;
import android.opengl.GLSurfaceView;

/**
 * Created by Kevin on 3/24/2018.
 */

public class EditDisplaySurfaceView extends GLSurfaceView {

    private EditDisplayRenderer renderer;
    private Tool currentTool;

    public EditDisplaySurfaceView(Context context)
    {
        super(context);

        setEGLContextClientVersion(Const.GL_VERSION);
        renderer = new EditDisplayRenderer();
        setRenderer(renderer);
    }

    public void setTool(Tool tool)
    {
        currentTool = tool;

        // swap touch listeners
        setOnTouchListener(tool.getTouchListener());

        // dismiss the other tool
    }
}
