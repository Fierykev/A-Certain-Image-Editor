package kevin_quang.acertainimageeditor.ui.toggle;

import android.graphics.Color;
import android.widget.ImageButton;

import java.util.HashMap;

import kevin_quang.acertainimageeditor.R;

public class Toggler {

    private class Toggleable {
        public ImageButton button;
    }

    private static HashMap<String, ImageButton> toggleables = new HashMap<>();
    private static String active = null;

    public static synchronized void add(String name, ImageButton button) {
        toggleables.put(name, button);
    }

    public static synchronized void remove(String name) {
        if(name.equals(active)) {
            active = null;
        }
        toggleables.remove(name);
    }

    public static synchronized boolean toggle(String name) {
        if(name == null) {
            if(active != null) {
                toggleables.get(active).setColorFilter(Color.WHITE);
                active = null;
            }
            return false;
        }
        ImageButton button = toggleables.get(name);
        if(name.equals(active)) {
            button.setColorFilter(Color.WHITE);
            active = null;
            return true;
        } else {
            if(active != null) {
                toggleables.get(active).setColorFilter(Color.WHITE);
            }
            active = name;
            button.setColorFilter(button.getContext().getColor(R.color.highlight));
            return false;
        }
    }
}
