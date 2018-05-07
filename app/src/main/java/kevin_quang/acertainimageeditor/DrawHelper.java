package kevin_quang.acertainimageeditor;

import android.graphics.Bitmap;
import android.graphics.Path;
import android.util.Log;
import android.util.Pair;
import android.view.MotionEvent;

import java.util.ArrayList;

public abstract class DrawHelper extends Tool {

    public static final GLHelper.Point<Float> END_POINT =
            new GLHelper.Point<>(-1f, -1f);

    private ArrayList<
            Pair<GLHelper.Point<Float>, GLHelper.Point<Float>>> points = new ArrayList<>();

    @Override
    void load(Bitmap bitmap, boolean storeHistory) {
        super.load(bitmap, storeHistory);

        float width = super.image.getWidth();
        float height = super.image.getHeight();
        float vWidth = super.screenWidth;
        float vHeight = super.screenHeight;
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
        super.setTouchLambda((v, event)-> {
                if(event.getAction() != MotionEvent.ACTION_UP) {
                    GLHelper.Point<Float> point =
                            new GLHelper.Point<>();
                    point.x = (event.getX()) / scale - xOffset;
                    point.y = (event.getY()) / scale - yOffset;
                    Log.d("Point", String.valueOf(point.x) + "," + String.valueOf(point.y));
                    points.add(new Pair<>(point, new GLHelper.Point<Float>()));

                    point.add(point);
                    return true;
                } else {
                    points.add(new Pair<>(END_POINT, new GLHelper.Point<Float>()));
                }
                return false;
            }
        );
    }

    abstract void processPoints(Path path);

    synchronized void processPointList()
    {
        ArrayList<Pair<GLHelper.Point<Float>, GLHelper.Point<Float>>> subList =
                new ArrayList<>();

        Pair<GLHelper.Point<Float>, GLHelper.Point<Float>> point = null;

        while (!points.isEmpty())
        {
            point = points.remove(0);

            if (point.first.x == END_POINT.x
                    && point.first.y == END_POINT.y)
            {
                if (!subList.isEmpty()) {
                    processPoints(getPath(subList));
                    subList.clear();
                }
            } else
            {
                subList.add(point);
            }
        }

        // force process
        if (!subList.isEmpty()) {
            processPoints(getPath(subList));
            subList.clear();
        }

        // clear out points
        if (point != null) {
            // add back last point
            points.add(0, point);
        }
    }

    @Override
    void onDraw(float aspectRatio, int width, int height)
    {
        processPointList();

        super.onDraw(aspectRatio, width, height);
    }

    public Path getPath(ArrayList<Pair<GLHelper.Point<Float>, GLHelper.Point<Float>>> points) {
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
}
