package kevin_quang.acertainimageeditor;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

public class BrushTool extends Tool {

    private ArrayList<Point> points = new ArrayList<Point>();

    public BrushTool(final EditDisplaySurfaceView editDisplaySurfaceView) {
        float width = editDisplaySurfaceView.getBitmapWidth();
        float height = editDisplaySurfaceView.getBitmapHeight();
        float vWidth = editDisplaySurfaceView.getWidth();
        float vHeight = editDisplaySurfaceView.getHeight();
        final float scale;
        if(width > vWidth || height > vHeight) {
            if(width / vWidth < height / vHeight) {
                scale = width / vWidth;
            } else {
                scale = height / vHeight;
            }
        } else {
            if(vWidth / width < vHeight / height) {
                scale = vWidth / width;
            } else {
                scale = vHeight / height;
            }
        }
        final float xOffset = (vWidth / scale - width)/2;
        final float yOffset = (vHeight / scale - height)/2;
        touchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() != MotionEvent.ACTION_UP) {
                    Point point = new Point();
                    point.x = (event.getX()) / scale - xOffset;
                    point.y = (event.getY()) / scale - yOffset;
                    points.add(point);
                    Path path = getPath();

                    Canvas canvas = new Canvas(image);
                    Paint paint = new Paint();
                    paint.setColor(Color.RED);
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeWidth(1);
                    canvas.drawPath(path,paint);
                    editDisplaySurfaceView.setBitmap(image);
                    return true;
                } else {
                    points.clear();
                }
                return false;
            }
        };
    }

    public Path getPath() {
        Path path = new Path();
        if(points.size() == 1) {
            return path;
        }
        for(int i = 0; i < points.size(); i++) {
            Point point = points.get(i);
            if(i == 0) {
                Point next = points.get(i + 1);
                point.dx = (next.x - point.x)/3;
                point.dy = (next.y - point.y)/3;
            } else if(i < points.size() - 1) {
                Point prev = points.get(i - 1);
                Point next = points.get(i + 1);
                point.dx = (next.x - prev.x)/3;
                point.dy = (next.y - prev.y)/3;
            } else {
                Point prev = points.get(i - 1);
                point.dx = (point.x - prev.x)/3;
                point.dy = (point.y - prev.y)/3;
            }
        }
        for(int i = 0; i < points.size(); i++) {
            Point point = points.get(i);
            if(i == 0) {
                path.moveTo(point.x, point.y);
            } else {
                path.lineTo(point.x, point.y);
                /*Point prev = points.get(i - 1);
                path.cubicTo(prev.x + prev.dx, prev.y + prev.dy,
                             point.x - point.dx, point.y - point.dy,
                             point.x, point.y);*/
            }
        }
        return path;
    }

    @Override
    void getLeftMenu() {

    }

    @Override
    void getRightMenu() {

    }

}
