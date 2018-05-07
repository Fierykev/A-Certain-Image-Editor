package kevin_quang.acertainimageeditor.tool;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;


public class BrushTool extends DrawHelper {

    public void processPoints(Path path)
    {
        Canvas canvas = new Canvas(super.image);
        Paint paint = new Paint();
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(color);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1);
        canvas.drawPath(path, paint);
        load(super.image, true);
    }

}
