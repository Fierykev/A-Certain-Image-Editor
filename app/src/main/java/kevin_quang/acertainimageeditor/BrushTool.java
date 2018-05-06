package kevin_quang.acertainimageeditor;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.Log;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

public class BrushTool extends Tool {

    private ArrayList<
            Pair<GLHelper.Point<Float>, GLHelper.Point<Float>>> points = new ArrayList<>();

    public BrushTool(final EditDisplaySurfaceView editDisplaySurfaceView) {
        float width = editDisplaySurfaceView.getBitmapWidth();
        float height = editDisplaySurfaceView.getBitmapHeight();
        float vWidth = editDisplaySurfaceView.getWidth();
        float vHeight = editDisplaySurfaceView.getHeight();
        Log.d("Point", String.valueOf(width) + "," + String.valueOf(height) + " " + String.valueOf(vWidth) + "," + String.valueOf(vHeight));
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
                    GLHelper.Point<Float> point =
                            new GLHelper.Point<>();
                    point.x = (event.getX()) / scale - xOffset;
                    point.y = (event.getY()) / scale - yOffset;
                    Log.d("Point", String.valueOf(point.x) + "," + String.valueOf(point.y));
                    points.add(new Pair<>(point, new GLHelper.Point<Float>()));

                    editDisplaySurfaceView.addPoint(point);

                    Path path = getPath();

                    Bitmap bitmap = Bitmap.createBitmap(image);
                    Canvas canvas = new Canvas(image);
                    Paint paint = new Paint();
                    paint.setColor(Color.RED);
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeWidth(1);
                    canvas.drawPath(path,paint);
                    editDisplaySurfaceView.setBitmap(image);
                    return true;
                } else {
                    editDisplaySurfaceView.addPoint(
                            EditDisplayRenderer.END_POINT);
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
            Pair<GLHelper.Point<Float>, GLHelper.Point<Float>> point =
                    points.get(i);
            if(i == 0) {
                Pair<GLHelper.Point<Float>, GLHelper.Point<Float>>
                        next = points.get(i + 1);
                point.second.x = (next.first.x - point.first.x)/3;
                point.second.y = (next.first.y - point.first.y)/3;
            } else if(i < points.size() - 1) {
                Pair<GLHelper.Point<Float>, GLHelper.Point<Float>> prev =
                        points.get(i - 1);
                Pair<GLHelper.Point<Float>, GLHelper.Point<Float>> next =
                        points.get(i + 1);
                point.second.x = (next.first.x - prev.first.x)/3;
                point.second.y = (next.first.y - prev.first.y)/3;
            } else {
                Pair<GLHelper.Point<Float>, GLHelper.Point<Float>> prev =
                        points.get(i - 1);
                point.second.x = (point.first.x - prev.first.x)/3;
                point.second.y = (point.first.y - prev.first.y)/3;
            }
        }
        for(int i = 0; i < points.size(); i++) {
            Pair<GLHelper.Point<Float>, GLHelper.Point<Float>> point =
                    points.get(i);
            if(i == 0) {
                path.moveTo(point.first.x, point.first.y);
            } else {
                Pair<GLHelper.Point<Float>, GLHelper.Point<Float>> prev =
                        points.get(i - 1);
                path.cubicTo(prev.first.x + prev.second.x, prev.first.y + prev.second.y,
                             point.first.x - point.second.x, point.first.y - point.second.y,
                             point.first.x, point.first.y);
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
