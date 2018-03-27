package kevin_quang.acertainimageeditor;

import android.view.GestureDetector;
import android.view.MotionEvent;

/**
 * Created by Kevin on 3/24/2018.
 */

abstract class EdgeSwipe extends GestureDetector.SimpleOnGestureListener {

    private static final int INTERACTION_AREA = 50;
    private static final int INTERACTION_DIST = 50;
    private static final int VELOCITY = 50;

    private int width = 0, height = 0;

    abstract void bottomUp();

    abstract void topDown();

    abstract void leftRight();

    abstract void rightLeft();

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

        // swipe up
        if (e1.getY() < INTERACTION_AREA
                && INTERACTION_DIST < e2.getY() - e1.getY()
                && VELOCITY < Math.abs(velocityY))
        {
            bottomUp();
        }

        return false;
    }
}
