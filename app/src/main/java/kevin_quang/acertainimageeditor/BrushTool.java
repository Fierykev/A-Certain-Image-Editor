package kevin_quang.acertainimageeditor;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

public class BrushTool extends DrawHelper {

    @Override
    void processPoints(Path path)
    {
        Bitmap bitmap = Bitmap.createBitmap(super.image);
        Canvas canvas = new Canvas(super.image);
        Paint paint = new Paint();
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1);
        canvas.drawPath(path, paint);
        load(super.image, true);
    }

    @Override
    void getLeftMenu() {

    }

    @Override
    void getRightMenu() {

    }
}
