package kevin_quang.acertainimageeditor;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;

/**
 * Created by Kevin on 3/24/2018.
 */

abstract class Tool {

    private View.OnTouchListener touchListener;

    abstract void init(Context context);

    abstract void destroy();

    abstract void load(Bitmap bitmap);

    abstract void onDraw();

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
