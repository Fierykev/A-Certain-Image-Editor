package kevin_quang.acertainimageeditor;

import android.view.View;

/**
 * Created by Kevin on 3/24/2018.
 */

abstract class Tool {

    private View.OnTouchListener touchListener;

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
