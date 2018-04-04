package kevin_quang.acertainimageeditor;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;

/**
 * Created by Kevin on 3/24/2018.
 */

abstract class Tool {

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

    abstract void init(Context context);

    abstract void destroy();

    abstract void load(Bitmap bitmap);

    void setArgs(Args args) { }

    abstract void onDraw(float aspectRatio);

    abstract void getLeftMenu();

    abstract void getRightMenu();

    View.OnTouchListener getTouchListener()
    {
        return touchListener;
    }

    void dismissMenu()
    {

    }
}
