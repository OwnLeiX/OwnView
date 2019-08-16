package lx.own.view.online;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;

import androidx.appcompat.widget.AppCompatImageView;

/**
 * <b>无限旋转的ImageView</b><br/>
 *
 * @author Lei.X
 * Created on 6/27/2019.
 */
public class InfiniteRotateImageView extends AppCompatImageView {
    public InfiniteRotateImageView(Context context) {
        super(context);
    }

    public InfiniteRotateImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public InfiniteRotateImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        RotateAnimation animation = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setDuration(1000L);//设置动画持续时间
        animation.setFillAfter(true);//保持执行后的效果
        animation.setRepeatCount(RotateAnimation.INFINITE);//无限循环
        animation.setInterpolator(new LinearInterpolator());//加速器
        startAnimation(animation);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        clearAnimation();
    }
}
