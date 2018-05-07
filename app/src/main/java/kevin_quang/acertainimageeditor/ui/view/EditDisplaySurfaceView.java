package kevin_quang.acertainimageeditor.ui.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;

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

    public EditDisplaySurfaceView(Context context)
    {
        super(context);

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
    public void save(String path) {renderer.save(path);}
    public void rotate(int degrees) {renderer.rotate(degrees);}
    public void undo() {renderer.undo();}
    public void redo() {renderer.redo();}
    public boolean canUndo() {return renderer.canUndo();}
    public boolean canRedo() {return renderer.canRedo();}
    public void setToolColor(int color) {currentTool.setColor(color);}
    public int getToolColor() {return currentTool.getColor();}
}
