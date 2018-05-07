package kevin_quang.acertainimageeditor;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
        renderer.setContext(getContext());

        // set listener to tool
        setOnTouchListener(Tool.onTouch);

        //ImageDrawTool tmp = new ImageDrawTool();

        // TODO: revert
        LiquifyTool tmp = new LiquifyTool();
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
    public void save(String path) {renderer.save(path);}
    public void rotate(int degrees) {renderer.rotate(degrees);}
    public void undo() {renderer.undo();}
    public void redo() {renderer.redo();}
    public boolean canUndo() {return renderer.canUndo();}
    public boolean canRedo() {return renderer.canRedo();}
}
