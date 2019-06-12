package com.rey.material.drawable;

import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.drawable.Drawable;

import androidx.core.graphics.drawable.DrawableCompat;

/**
 * Created by Rey on 8/19/2015.
 */
public class PaddingDrawable extends Drawable implements Drawable.Callback {

    private Drawable mDrawable;

    private int mPaddingLeft;
    private int mPaddingTop;
    private int mPaddingRight;
    private int mPaddingBottom;

    public PaddingDrawable(Drawable drawable) {
        setWrappedDrawable(drawable);
    }

    public void setPadding(int paddingLeft, int paddingTop, int paddingRight, int paddingBottom){
        mPaddingLeft = paddingLeft;
        mPaddingTop = paddingTop;
        mPaddingRight = paddingRight;
        mPaddingBottom = paddingBottom;
    }

    public int getPaddingLeft(){
        return mPaddingLeft;
    }

    public int getPaddingTop(){
        return mPaddingTop;
    }

    public int getPaddingRight(){
        return mPaddingRight;
    }

    public int getPaddingBottom(){
        return mPaddingBottom;
    }

    @Override
    public void draw(Canvas canvas) {
        if(mDrawable != null)
            mDrawable.draw(canvas);
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        if(mDrawable != null)
            mDrawable.setBounds(bounds.left + mPaddingLeft, bounds.top + mPaddingTop, bounds.right - mPaddingRight, bounds.bottom - mPaddingBottom);
    }

    @Override
    public void setChangingConfigurations(int configs) {
        if(mDrawable != null)
            mDrawable.setChangingConfigurations(configs);
    }

    @Override
    public int getChangingConfigurations() {
        return mDrawable != null ? mDrawable.getChangingConfigurations() : 0;
    }

    @Override
    public void setDither(boolean dither) {
        if(mDrawable != null)
            mDrawable.setDither(dither);
    }

    @Override
    public void setFilterBitmap(boolean filter) {
        if(mDrawable != null)
            mDrawable.setFilterBitmap(filter);
    }

    @Override
    public void setAlpha(int alpha) {
        if(mDrawable != null)
            mDrawable.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        if(mDrawable != null)
            mDrawable.setColorFilter(cf);
    }

    @Override
    public boolean isStateful() {
        return mDrawable != null && mDrawable.isStateful();
    }

    @Override
    public boolean setState(final int[] stateSet) {
        return mDrawable != null && mDrawable.setState(stateSet);
    }

    @Override
    public int[] getState() {
        return mDrawable != null ? mDrawable.getState() : null;
    }

    public void jumpToCurrentState() {
        if(mDrawable != null)
            DrawableCompat.jumpToCurrentState(mDrawable);
    }

    @Override
    public Drawable getCurrent() {
        return mDrawable != null ? mDrawable.getCurrent() : null;
    }

    @Override
    public boolean setVisible(boolean visible, boolean restart) {
        return super.setVisible(visible, restart) || (mDrawable != null && mDrawable.setVisible(visible, restart));
    }

    @Override
    public int getOpacity() {
        return mDrawable != null ? mDrawable.getOpacity() : PixelFormat.UNKNOWN;
    }

    @Override
    public Region getTransparentRegion() {
        return mDrawable != null ? mDrawable.getTransparentRegion() : null;
    }

    @Override
    public int getIntrinsicWidth() {
        return (mDrawable != null ? mDrawable.getIntrinsicWidth() : 0) + mPaddingLeft + mPaddingRight;
    }

    @Override
    public int getIntrinsicHeight() {
        return (mDrawable != null ? mDrawable.getIntrinsicHeight() : 0) + mPaddingTop + mPaddingBottom;
    }

    @Override
    public int getMinimumWidth() {
        return (mDrawable != null ? mDrawable.getMinimumWidth() : 0) + mPaddingLeft + mPaddingRight;
    }

    @Override
    public int getMinimumHeight() {
        return (mDrawable != null ? mDrawable.getMinimumHeight() : 0) + mPaddingTop + mPaddingBottom;
    }

    @Override
    public boolean getPadding(Rect padding) {
        boolean hasPadding = mDrawable != null && mDrawable.getPadding(padding);
        if(hasPadding){
            padding.left += mPaddingLeft;
            padding.top += mPaddingTop;
            padding.right += mPaddingRight;
            padding.bottom += mPaddingBottom;
        }
        else{
            padding.set(mPaddingLeft, mPaddingTop, mPaddingRight, mPaddingBottom);
            hasPadding = mPaddingLeft != 0 || mPaddingTop != 0 || mPaddingRight != 0 || mPaddingBottom != 0;
        }

        return hasPadding;
    }

    /**
     * {@inheritDoc}
     */
    public void invalidateDrawable(Drawable who) {
        invalidateSelf();
    }

    /**
     * {@inheritDoc}
     */
    public void scheduleDrawable(Drawable who, Runnable what, long when) {
        scheduleSelf(what, when);
    }

    /**
     * {@inheritDoc}
     */
    public void unscheduleDrawable(Drawable who, Runnable what) {
        unscheduleSelf(what);
    }

    @Override
    protected boolean onLevelChange(int level) {
        return mDrawable != null && mDrawable.setLevel(level);
    }

    @Override
    public void setAutoMirrored(boolean mirrored) {
        if(mDrawable != null)
            DrawableCompat.setAutoMirrored(mDrawable, mirrored);
    }

    @Override
    public boolean isAutoMirrored() {
        return mDrawable != null && DrawableCompat.isAutoMirrored(mDrawable);
    }

    @Override
    public void setTint(int tint) {
        if(mDrawable != null)
            DrawableCompat.setTint(mDrawable, tint);
    }

    @Override
    public void setTintList(ColorStateList tint) {
        if(mDrawable != null)
            DrawableCompat.setTintList(mDrawable, tint);
    }

    @Override
    public void setTintMode(PorterDuff.Mode tintMode) {
        if(mDrawable != null)
            DrawableCompat.setTintMode(mDrawable, tintMode);
    }

    @Override
    public void setHotspot(float x, float y) {
        if(mDrawable != null)
            DrawableCompat.setHotspot(mDrawable, x, y);
    }

    @Override
    public void setHotspotBounds(int left, int top, int right, int bottom) {
        if(mDrawable != null)
            DrawableCompat.setHotspotBounds(mDrawable, left, top, right, bottom);
    }

    public Drawable getWrappedDrawable() {
        return mDrawable;
    }

    public void setWrappedDrawable(Drawable drawable) {
        if (mDrawable != null) {
            mDrawable.setCallback(null);
        }

        mDrawable = drawable;

        if (drawable != null) {
            drawable.setCallback(this);
        }

        onBoundsChange(getBounds());
        invalidateSelf();
    }
}
