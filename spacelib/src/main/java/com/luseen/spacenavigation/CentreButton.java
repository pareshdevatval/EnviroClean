package com.luseen.spacenavigation;

import android.content.Context;
import android.os.Build;
import android.widget.ImageView;
import androidx.annotation.RequiresApi;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.view.MotionEvent;

/**
 * Created by Chatikyan on 10.11.2016.
 */

public class CentreButton extends ImageView {

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public CentreButton(Context context) {
        super(context);
        setElevation(0);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        boolean result = super.onTouchEvent(ev);
        if (!result) {
            if (ev.getAction() == MotionEvent.ACTION_UP) {
                cancelLongPress();
            }
            setPressed(false);
        }
        return result;
    }
}
