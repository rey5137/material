package com.rey.material.drawable;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.BoringLayout;
import android.text.Layout;
import android.text.TextPaint;
import android.text.TextUtils;

/**
 * Created by Rey on 1/21/2015.
 */
public class ContactChipDrawable extends Drawable{

    private Paint mPaint;
    private int mPaddingLeft;
    private int mPaddingRight;
    private int mBackgroundColor;

    private CharSequence mContactName;
    private BoringLayout mBoringLayout;
    private BoringLayout.Metrics mMetrics;
    private TextPaint mTextPaint;
    private RectF mRect;

    private BitmapShader mBitmapShader;
    private Bitmap mBitmap;
    private Matrix mMatrix;

    public ContactChipDrawable(int paddingLeft, int paddingRight, Typeface typeface, int textColor, int textSize, int backgroundColor) {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(textColor);
        mPaint.setTypeface(typeface);
        mPaint.setTextSize(textSize);

        mTextPaint = new TextPaint(mPaint);
        mMetrics = new BoringLayout.Metrics();
        Paint.FontMetricsInt temp = mTextPaint.getFontMetricsInt();
        mMetrics.ascent = temp.ascent;
        mMetrics.bottom = temp.bottom;
        mMetrics.descent = temp.descent;
        mMetrics.top = temp.top;
        mMetrics.leading = temp.leading;

        mRect = new RectF();

        mMatrix = new Matrix();

        mPaddingLeft = paddingLeft;
        mPaddingRight = paddingRight;
        mBackgroundColor = backgroundColor;
    }

    public void setContactName(CharSequence name){
        mContactName = name;
        updateLayout();
        invalidateSelf();
    }

    public void setImage(Bitmap bm){
        if(mBitmap != bm){
            mBitmap = bm;
            if(mBitmap != null) {
                mBitmapShader = new BitmapShader(mBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
                updateMatrix();
            }
            invalidateSelf();
        }
    }

    private void updateLayout(){
        if(mContactName == null)
            return;

        Rect bounds = getBounds();
        if(bounds.width() == 0 || bounds.height() == 0)
            return;

        int outerWidth = Math.max(0, bounds.width() - bounds.height() - mPaddingLeft - mPaddingRight);
        mMetrics.width = Math.round(mTextPaint.measureText(mContactName, 0, mContactName.length()) + 0.5f);

        if(mBoringLayout == null)
            mBoringLayout = BoringLayout.make(mContactName, mTextPaint, outerWidth, Layout.Alignment.ALIGN_NORMAL, 1f, 1f, mMetrics, true, TextUtils.TruncateAt.END, outerWidth);
        else
            mBoringLayout = mBoringLayout.replaceOrMake(mContactName, mTextPaint, outerWidth, Layout.Alignment.ALIGN_NORMAL, 1f, 1f, mMetrics, true, TextUtils.TruncateAt.END, outerWidth);
    }

    private void updateMatrix(){
        if(mBitmap == null)
            return;

        Rect bounds = getBounds();
        if(bounds.width() == 0 || bounds.height() == 0)
            return;

        mMatrix.reset();
        float scale = bounds.height() / (float)Math.min(mBitmap.getWidth(), mBitmap.getHeight());
        mMatrix.setScale(scale, scale, 0, 0);
        mMatrix.postTranslate((bounds.height()  - mBitmap.getWidth() * scale) / 2, (bounds.height() - mBitmap.getHeight() * scale) / 2);

        mBitmapShader.setLocalMatrix(mMatrix);
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        updateLayout();
        updateMatrix();
    }

    @Override
    public void draw(Canvas canvas) {
        int saveCount = canvas.save();

        Rect bounds = getBounds();
        float halfHeight = bounds.height() / 2f;
        mPaint.setShader(null);
        mPaint.setColor(mBackgroundColor);
        mRect.set(1, 0, bounds.height() + 1, bounds.height());
        canvas.drawArc(mRect, 90, 180, true, mPaint);
        mRect.set(bounds.width() - bounds.height(), 0, bounds.width(), bounds.height());
        canvas.drawArc(mRect, 270, 180, true, mPaint);
        mRect.set(halfHeight, 0, bounds.width() - halfHeight, bounds.height());
        canvas.drawRect(mRect, mPaint);

        if(mBitmap != null){
            mPaint.setShader(mBitmapShader);
            canvas.drawCircle(halfHeight, halfHeight, halfHeight, mPaint);
        }

        if(mContactName != null && mBoringLayout != null) {
            canvas.translate(bounds.height() + mPaddingLeft, (bounds.height() - mBoringLayout.getHeight()) / 2f);
            mBoringLayout.draw(canvas);
        }

        canvas.restoreToCount(saveCount);
    }

    @Override
    public void setAlpha(int alpha) {
        mPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        mPaint.setColorFilter(cf);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}
