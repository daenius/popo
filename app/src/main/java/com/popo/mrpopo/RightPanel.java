package com.popo.mrpopo;

import android.animation.TimeInterpolator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;


/**
 * Created by jpowang on 6/7/14.
 */
public class RightPanel extends FrameLayout {
    private int windowWidth;

    public float getPreviousDownX() {
        return previousDownX;
    }

    private float previousDownX;
    private float previousDownY;

    public enum ContentFragmentState {
        CLOSED, OPEN, CLOSING, OPENING;
    }

    public ContentFragmentState getCurrentContentFragmentState() {
        return currentContentFragmentState;
    }

    public void setCurrentContentFragmentState(ContentFragmentState currentContentFragmentState) {
        this.currentContentFragmentState = currentContentFragmentState;
    }

    public ContentFragmentState currentContentFragmentState;

    public RightPanel(Context context) {
        super(context);
        init();
    }

    public RightPanel(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RightPanel(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public int getWindowWidth() {
        return windowWidth;
    }

    public void setWindowWidth(int windowWidth) {
        this.windowWidth = windowWidth;
    }

    private void init() {

        this.currentContentFragmentState = ContentFragmentState.CLOSED;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        int index = motionEvent.getActionIndex();
        int action = motionEvent.getActionMasked();
        int pointerId = motionEvent.getPointerId(index);
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                this.previousDownX = motionEvent.getX(index);
                this.previousDownY = motionEvent.getY(index);
                break;
            case MotionEvent.ACTION_MOVE:
                float dx = Math.abs(motionEvent.getX() - previousDownX);
                float dy = Math.abs(motionEvent.getY() - previousDownY);
                double angle = Math.atan2(dy, dx);

                if (angle <= Math.PI / 6) {

                    return true;
                }
                break;
        }
        return false;
    }

    public void togglePanel() {

        switch (this.currentContentFragmentState) {
            case CLOSED:
                animateOpen();
                break;
            case OPEN:
                animateClose();
                break;
        }
    }

    public void animateOpen() {
        this.setVisibility(View.VISIBLE);
        this.animate().setInterpolator(new TimeInterpolator() {
            @Override
            public float getInterpolation(float v) {
                if (v <= 0.5f) {
                    return v * 1.6f;
                }
                return (float) Math.pow(v - 1, 5) + 1;
            }
        }).translationX(0);
        this.currentContentFragmentState = ContentFragmentState.OPEN;
    }

    public void animateClose() {
        this.animate().translationX(this.windowWidth);
        this.currentContentFragmentState = ContentFragmentState.CLOSED;
    }

    public void animateSwipeOpen() {
        this.setVisibility(View.VISIBLE);
        this.animate().translationX(0);
        this.currentContentFragmentState = ContentFragmentState.OPEN;
    }

    public void animateSwipeClose() {
        this.animate().translationX(this.windowWidth);
        this.currentContentFragmentState = ContentFragmentState.CLOSED;
    }

}
