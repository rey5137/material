package com.rey.material.text.style;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.text.BoringLayout;
import android.text.Layout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.ReplacementSpan;

/**
 * Created by Rey on 1/21/2015.
 */
public class ContactChipSpan extends ReplacementSpan {

    private Paint mPaint;
    private int mPaddingLeft;
    private int mPaddingRight;
    private int mBackgroundColor;
    private int mHeight;
    private int mWidth;

    private CharSequence mContactName;
    private BoringLayout mBoringLayout;
    private TextPaint mTextPaint;
    private RectF mRect;

    private BitmapShader mBitmapShader;
    private Bitmap mBitmap;
    private Matrix mMatrix;

    public ContactChipSpan(CharSequence name, int height, int maxWidth, int paddingLeft, int paddingRight, Typeface typeface, int textColor, int textSize, int backgroundColor) {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(textColor);
        mPaint.setTypeface(typeface);
        mPaint.setTextSize(textSize);

        mTextPaint = new TextPaint(mPaint);


        mRect = new RectF();

        mMatrix = new Matrix();

        mContactName = name;
        mPaddingLeft = paddingLeft;
        mPaddingRight = paddingRight;
        mBackgroundColor = backgroundColor;
        mHeight = height;
        mWidth = Math.round(Math.min(maxWidth, mPaint.measureText(name, 0, name.length()) + paddingLeft + paddingRight + height));

        int outerWidth = Math.max(0, mWidth - mPaddingLeft - mPaddingRight - mHeight);
        Paint.FontMetricsInt temp = mTextPaint.getFontMetricsInt();
        BoringLayout.Metrics mMetrics = new BoringLayout.Metrics();
        mMetrics.width = Math.round(mTextPaint.measureText(mContactName, 0, mContactName.length()) + 0.5f);
        mMetrics.ascent = temp.ascent;
        mMetrics.bottom = temp.bottom;
        mMetrics.descent = temp.descent;
        mMetrics.top = temp.top;
        mMetrics.leading = temp.leading;
        mBoringLayout = BoringLayout.make(mContactName, mTextPaint, outerWidth, Layout.Alignment.ALIGN_NORMAL, 1f, 1f, mMetrics, true, TextUtils.TruncateAt.END, outerWidth);
    }

    public void setImage(Bitmap bm){
        if(mBitmap != bm){
            mBitmap = bm;
            if(mBitmap != null) {
                mBitmapShader = new BitmapShader(mBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
                mMatrix.reset();
                float scale = mHeight / (float)Math.min(mBitmap.getWidth(), mBitmap.getHeight());
                mMatrix.setScale(scale, scale, 0, 0);
                mMatrix.postTranslate((mHeight  - mBitmap.getWidth() * scale) / 2, (mHeight - mBitmap.getHeight() * scale) / 2);

                mBitmapShader.setLocalMatrix(mMatrix);
            }
        }
    }

    @Override
    public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
        if (fm != null) {
            int cy = (fm.ascent + fm.descent) / 2;
            fm.ascent = Math.min(fm.ascent, cy - mHeight / 2);
            fm.descent = Math.max(fm.descent, cy + mHeight / 2);
            fm.top = Math.min(fm.top, fm.ascent);
            fm.bottom = Math.max(fm.bottom, fm.descent);
        }

        return mWidth;
    }

    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
        canvas.save();

        canvas.translate(x, top);

        float halfHeight = mHeight / 2f;
        mPaint.setShader(null);
        mPaint.setColor(mBackgroundColor);
        mRect.set(1, 0, mHeight + 1, mHeight);
        canvas.drawArc(mRect, 90, 180, true, mPaint);
        mRect.set(mWidth - mHeight, 0, mWidth, mHeight);
        canvas.drawArc(mRect, 270, 180, true, mPaint);
        mRect.set(halfHeight, 0, mWidth - halfHeight, mHeight);
        canvas.drawRect(mRect, mPaint);

        if(mBitmap != null){
            mPaint.setShader(mBitmapShader);
            canvas.drawCircle(halfHeight, halfHeight, halfHeight, mPaint);
        }

        if(mContactName != null && mBoringLayout != null) {
            canvas.translate(mHeight + mPaddingLeft, (mHeight - mBoringLayout.getHeight()) / 2f);
            mBoringLayout.draw(canvas);
        }

        canvas.restore();
    }
}
