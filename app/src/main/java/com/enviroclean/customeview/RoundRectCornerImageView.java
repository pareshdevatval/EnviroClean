package com.enviroclean.customeview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageView;

/**
 * Created by imobdev-paresh on 03,October,2019
 */
public class RoundRectCornerImageView extends AppCompatImageView {

    private float radius = 18.0f;
    private Path path;
    private RectF rect;

    public RoundRectCornerImageView(Context context) {
        super(context);
        init();
    }

    public RoundRectCornerImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RoundRectCornerImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        path = new Path();

    }

    @Override
    protected void onDraw(Canvas canvas) {
        rect = new RectF(10, 10, this.getWidth(), this.getHeight());
        path.addRoundRect(rect, radius, 0, Path.Direction.CW);
        canvas.clipPath(path);
        super.onDraw(canvas);
    }
}
