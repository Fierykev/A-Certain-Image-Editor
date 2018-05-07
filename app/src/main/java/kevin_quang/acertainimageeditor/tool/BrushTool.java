package kevin_quang.acertainimageeditor.tool;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

import java.util.ArrayList;


public class BrushTool extends DrawHelper {

    @Override
    void processPoints(Path path)
    {
        Canvas canvas = new Canvas(super.image);
        Paint paint = new Paint();
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(color);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1);
        canvas.drawPath(path, paint);
    }

    @Override
    synchronized void processPointList()
    {
        if(cursor.equals(END_POINT) && points.size() > 0) {
            points.remove(points.size() - 1);
            processPoints(getPath(points));
            load(super.image, true);
            points.clear();
        } else {
            processPoints(getPath(points));
            load(super.image, false);
        }
    }

}
