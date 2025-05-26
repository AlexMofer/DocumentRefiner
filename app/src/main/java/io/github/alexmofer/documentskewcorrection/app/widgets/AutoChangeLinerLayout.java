package io.github.alexmofer.documentskewcorrection.app.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

/**
 * 宽大于高垂直，高大于宽水平
 * Created by Alex on 2025/5/26.
 */
public class AutoChangeLinerLayout extends LinearLayout {

    public AutoChangeLinerLayout(Context context) {
        super(context);
    }

    public AutoChangeLinerLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public AutoChangeLinerLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int width = MeasureSpec.getSize(widthMeasureSpec);
        final int height = MeasureSpec.getSize(heightMeasureSpec);
        if (width == height) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        final int orientation = getOrientation();
        if (width > height) {
            if (orientation != HORIZONTAL) {
                setOrientation(HORIZONTAL);
            }
        } else {
            if (orientation != VERTICAL) {
                setOrientation(VERTICAL);
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
