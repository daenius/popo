package com.popo.mrpopo;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.LinearLayout;
import android.widget.Scroller;

import com.popo.mrpopo.util.AppConstants;

/**
 * Created by dennizhu on 5/31/14.
 */
public class FragmentViewContainer extends LinearLayout {
    private View mapView;

    public View getLandmarkContent() {
        return landmarkContent;
    }

    private View landmarkContent;
    public enum ContentFragmentState {
        CLOSED, OPEN
    }

    public ContentFragmentState getCurrentContentFragmentState() {
        return currentContentFragmentState;
    }

    private ContentFragmentState currentContentFragmentState = ContentFragmentState.CLOSED;
    private final int contentFragmentMargin = 100;

    public FragmentViewContainer(Context context) {
        super(context);
    }

    public FragmentViewContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FragmentViewContainer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mapView = this.getChildAt(0);
        this.landmarkContent = this.getChildAt(1);
        this.landmarkContent.setVisibility(View.GONE);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (changed) {
            Log.d(AppConstants.LOG_TAG, "Changed");
            this.calculateViewDimensions();
        }
        this.mapView.layout(left, top, right, bottom);
        this.landmarkContent.layout(left + contentFragmentMargin, top, right, bottom);
    }

    public void toggleContent(){
        switch (this.currentContentFragmentState ){
            case CLOSED:
                this.landmarkContent.setVisibility(View.VISIBLE);
                this.currentContentFragmentState = ContentFragmentState.OPEN;
                break;
            case OPEN:
                this.currentContentFragmentState = ContentFragmentState.CLOSED;
                this.landmarkContent.setVisibility(View.GONE);
                break;
        }

        this.invalidate();
    }
    private void calculateViewDimensions(){
        calculateContentViewDimension();
        calculateMapViewDimension();
    }
    private void calculateMapViewDimension(){
        this.mapView.getLayoutParams().height = this.getHeight();
        this.mapView.getLayoutParams().width = this.getWidth();
    }
    private void calculateContentViewDimension(){
        this.landmarkContent.getLayoutParams().height = this.getHeight();
        this.landmarkContent.getLayoutParams().width = this.getWidth() - contentFragmentMargin;
    }
}
