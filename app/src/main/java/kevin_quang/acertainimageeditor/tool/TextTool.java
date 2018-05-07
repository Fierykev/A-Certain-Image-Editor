package kevin_quang.acertainimageeditor.tool;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import kevin_quang.acertainimageeditor.R;
import kevin_quang.acertainimageeditor.ui.MainActivity;
import kevin_quang.acertainimageeditor.ui.toggle.Toggler;

public class TextTool extends Tool {

    private float x, y;
    private String text;
    private Canvas canvas;
    private Paint paint;
    private Bitmap bitmap, render;
    enum State {
        IDLE,
        NEW,
        UPDATE
    }
    private State state = State.IDLE;
    private Activity activity;

    public TextTool(Activity activity) {
        this.activity = activity;
        final View activityRootView = activity.findViewById(R.id.root);
        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            // navigation bar height
            int navigationBarHeight = 0;
            int resourceId = activity.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
            if (resourceId > 0) {
                navigationBarHeight = activity.getResources().getDimensionPixelSize(resourceId);
            }

            // status bar height
            int statusBarHeight = 0;
            resourceId = activity.getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (resourceId > 0) {
                statusBarHeight = activity.getResources().getDimensionPixelSize(resourceId);
            }

            // display window size for the app layout
            Rect rect = new Rect();
            activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);

            // screen height - (user app height + status + nav) ..... if non-zero, then there is a soft keyboard
            int keyboardHeight = activityRootView.getRootView().getHeight() - (statusBarHeight + navigationBarHeight + rect.height());

            if (keyboardHeight <= 0) {
                state = State.IDLE;
            } else {
                //onShowKeyboard(keyboardHeight);
            }
        });
        ((MainActivity)activity).setKeyListener((keyCode, event) -> {
            if(text != null) {
                if(keyCode == KeyEvent.KEYCODE_DEL && text.length() > 0) {
                    Log.d("Key", String.valueOf(text.length()));
                    text = text.substring(0,text.length() - 1);
                } else {
                    text += (char) event.getUnicodeChar();
                }
            }
        });
    }

    @Override
    public void load(Bitmap bitmap, boolean storeHistory) {
        super.load(bitmap, storeHistory);

        float width = super.image.getWidth();
        float height = super.image.getHeight();
        float vWidth = super.screenWidth;
        float vHeight = super.screenHeight;
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
        float xOffset = (vWidth / scale - width)/2;
        float yOffset = (vHeight / scale - height)/2;
        super.setTouchLambda((v, event)-> {
            if(event.getAction() != MotionEvent.ACTION_UP) {
                if(state == State.IDLE) {
                    x = (event.getX()) / scale - xOffset;
                    y = (event.getY()) / scale - yOffset;
                    state = State.NEW;
                    Log.d("Key", "K");
                }
                return true;
            }
            return false;
        });
    }

    public void renderText() {
        switch(state) {
            case NEW:
                InputMethodManager inputMethodManager =
                        (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.toggleSoftInputFromWindow(
                        activity.findViewById(R.id.root).getApplicationWindowToken(),
                        InputMethodManager.SHOW_FORCED, 0);
                text = "";
                bitmap = Bitmap.createBitmap(super.image);
                render = Bitmap.createBitmap(super.image);
                canvas = new Canvas(render);
                paint = new Paint();
                paint.setColor(color);
                paint.setTextSize(20);
                paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
                state = State.UPDATE;
                break;
            case UPDATE:
                Rect bounds = new Rect();
                render = Bitmap.createBitmap(bitmap);
                canvas = new Canvas(render);
                paint.getTextBounds(text, 0, text.length(), bounds);
                canvas.drawRect(new Rect((int)x + bounds.right + 2, (int)y + bounds.top,
                        (int)x + bounds.right + 3, (int)y + bounds.bottom), paint);
                canvas.drawText(text, x, y, paint);
                super.forceTexLoad(render, false);
                break;
            case IDLE:
                if(text != null) {
                    Log.d("Key","I");
                    render = Bitmap.createBitmap(bitmap);
                    canvas = new Canvas(render);
                    canvas.drawText(text, x, y, paint);
                    super.load(render, true);
                    text = null;
                    activity.runOnUiThread(() -> {
                        Toggler.toggle("brush_text");
                    });
                }
                break;
        }
    }

    @Override
    public void onDraw(float aspectRatio, int width, int height) {
        renderText();
        super.onDraw(aspectRatio, width, height);
    }

    @Override
    public void destroy() {
        super.destroy();
        ((MainActivity)activity).clearKeyListener();
    }
}
