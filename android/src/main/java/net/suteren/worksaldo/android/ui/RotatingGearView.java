package net.suteren.worksaldo.android.ui;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PictureDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGParseException;
import net.suteren.worksaldo.android.R;

/**
 * Created by hpa on 11.3.16.
 */
public class RotatingGearView extends ImageView {

    private PictureDrawable animationDrawable;

    public RotatingGearView(Context context) {
        super(context);
        prepareAnimation();

    }

    public RotatingGearView(Context context, AttributeSet attrs) {
        super(context, attrs);
        prepareAnimation();

    }

    public RotatingGearView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        prepareAnimation();

    }

    public RotatingGearView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        prepareAnimation();
    }

    Drawable prepareAnimation() {
        Log.d("RotationGearView", "getDrawable");
        try {
            SVG svgImage2 = SVG.getFromResource(getContext(), R.raw.high_resolution_gear);
            animationDrawable = new PictureDrawable(svgImage2.renderToPicture());
            setImageDrawable(animationDrawable);
            setAnimation(getMyAnimation());
            return animationDrawable;
        } catch (SVGParseException e) {
            Log.e("RotationGearView", "Unable to parse SVG", e);
        }
        return super.getDrawable();
    }

    private Animation getMyAnimation() {
        RotateAnimation animation = new RotateAnimation(0, 360, getWidth(), getHeight());
        animation.setInterpolator(new LinearInterpolator());
        animation.setRepeatCount(Animation.INFINITE);
        animation.setDuration(16000);
        startAnimation(animation);
        return animation;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (getAnimation() != null) getAnimation().cancel();
        setAnimation(getMyAnimation());
    }

    @Override
    public void setVisibility(int visibility) {
        if (getAnimation() != null) {
            if (visibility == View.VISIBLE) {
                getAnimation().start();
            } else {
                getAnimation().cancel();

            }
        }
        super.setVisibility(visibility);
        //animationDrawable.setVisible(visibility == View.VISIBLE, visibility == View.VISIBLE);

    }
}
