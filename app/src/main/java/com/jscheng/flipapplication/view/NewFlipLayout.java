package com.jscheng.flipapplication.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Camera;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.Transformation;
import android.widget.FrameLayout;

import com.jscheng.flipapplication.R;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by cheng on 16-10-8.
 */
public class NewFlipLayout extends FrameLayout implements OnSwipeListener{
    private static final String TAG = "FlipLayout";
    public static final int ANIM_DURATION_MILLIS = 1000;
    private static final Interpolator DefaultInterpolator = new DecelerateInterpolator();
    private View leftView;
    private View rightView;
    private OnSwipeTouchListener touchListener;
    private Direction direction;
    private flipAnimation animatior;
    private RollType rollType;
    private boolean isFlip;
    private AtomicBoolean isFliping;
    public NewFlipLayout(Context context) {
        this(context,null);
    }

    public NewFlipLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NewFlipLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context,attrs);
    }

    private void init(Context context) {
        direction = Direction.Default;
        rollType = RollType.up;
        isFlip = false;
        isFliping = new AtomicBoolean(false);
        touchListener =  new OnSwipeTouchListener(context);
        animatior = new flipAnimation();
        animatior.setDuration(ANIM_DURATION_MILLIS);
        animatior.setInterpolator(DefaultInterpolator);
        animatior.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                isFliping.set(true);
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                isFliping.set(false);
            }
            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

    }

    private void init(Context context,AttributeSet attrs){
        init(context);
        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.flip);
            int direction = ta.getInt(R.styleable.flip_direction,0);
            switch (direction){
                case 0: this.direction = Direction.Default;break;
                case 1: this.direction = Direction.Vertical;break;
                case 2: this.direction = Direction.Horizontal;break;
                default:this.direction = Direction.Default;break;
            }
            ta.recycle();
        }
    }

    private void initViewState(){
        leftView.setVisibility(VISIBLE);
        rightView.setVisibility(INVISIBLE);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if(getChildCount()!=2){
            Log.e(TAG, "onFinishInflate: IllegalState" );
            throw new IllegalStateException();
        }
        leftView = getChildAt(0);
        leftView.setOnTouchListener(touchListener);
        leftView.setClickable(true);
        rightView = getChildAt(1);
        rightView.setOnTouchListener(touchListener);
        rightView.setClickable(true);
        touchListener.addSwipeListener(NewFlipLayout.this);
        initViewState();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if(touchListener!=null)
            touchListener.removeSwipeListener(this);
    }

    private void toggleLeft(){
        if(direction==Direction.Vertical )
            return;
        if(isFliping.get()==true)
            return;
        rollType = rollType.left;
        startAnimation(animatior);
    }

    private void toggleRight(){
        if(direction==Direction.Vertical)
            return;
        if(isFliping.get()==true)
            return;
        rollType = RollType.right;
        startAnimation(animatior);
    }

    private void toggleUp(){
        if(direction==Direction.Horizontal)
            return;
        if(isFliping.get()==true)
            return;
        rollType = RollType.up;
        startAnimation(animatior);
    }

    private void toggleDown(){
        if(direction==Direction.Horizontal)
            return;
        if(isFliping.get()==true)
            return;
        rollType = RollType.down;
        startAnimation(animatior);
    }

    public void changeViewState() {
        if (isFlip) {
            leftView.setVisibility(VISIBLE);
            rightView.setVisibility(INVISIBLE);
        } else {
            rightView.setVisibility(VISIBLE);
            leftView.setVisibility(INVISIBLE);
        }
        isFlip =!isFlip;
    }

    @Override
    public void onSwipeLeft() {
        toggleLeft();
    }

    @Override
    public void onSwipeRight() {
        toggleRight();
    }

    @Override
    public void onSwipeUp() {
        toggleUp();
    }

    @Override
    public void onSwipeDown() {
        toggleDown();
    }

    private enum Direction {
        Horizontal,Vertical,Default
    }

    private enum RollType {
        down,up,right,left
    }

    private class flipAnimation extends Animation{
        private static final float EXPERIMENTAL_VALUE = 50.f;
        int centerX;
        int centerY;
        Camera camera;
        boolean swapView;
        @Override
        public void initialize(int width, int height, int parentWidth, int parentHeight) {
            super.initialize(width, height, parentWidth, parentHeight);
            setFillAfter(false);
            centerX = width/2;
            centerY = height/2;
            camera = new Camera();
            swapView = false;
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            final double radians = Math.PI * interpolatedTime;
            float degrees = (float) (180.0 * radians / Math.PI);

            if (rollType == RollType.up||rollType==RollType.left) {
                degrees = -degrees;
            }

            if (interpolatedTime >= 0.5f) {
                if (rollType == RollType.up||rollType==RollType.left) {
                    degrees += 180.f;
                }

                if (rollType == RollType.down||rollType == RollType.right) {
                    degrees -= 180.f;
                }
                if(!swapView) {
                    changeViewState();
                    swapView = true;
                }
            }

            Matrix matrix = t.getMatrix();
            camera.save();
            camera.translate(0.0f, 0.0f, (float) (EXPERIMENTAL_VALUE * Math.sin(radians)));
            if(rollType==RollType.down||rollType==RollType.up) {
                camera.rotateX(degrees);
                camera.rotateY(0);
            }else{
                camera.rotateX(0);
                camera.rotateY(degrees);
            }
            camera.rotateZ(0);
            camera.getMatrix(matrix);
            camera.restore();
            // 以View的中心点为旋转中心,如果不加这两句，就是以（0,0）点为旋转中心
            matrix.preTranslate(-centerX, -centerY);
            matrix.postTranslate(centerX, centerY);
        }
    }
}
