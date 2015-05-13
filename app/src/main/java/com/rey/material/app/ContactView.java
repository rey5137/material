package com.rey.material.app;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.view.GravityCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.rey.material.demo.R;
import com.rey.material.drawable.BlankDrawable;
import com.rey.material.util.ThemeUtil;
import com.rey.material.util.ViewUtil;
import com.rey.material.widget.ImageButton;
import com.rey.material.widget.RippleManager;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

/**
 * Created by Administrator on 3/2/2015.
 */
public class ContactView extends FrameLayout implements Target{

    private TextView mNameView;
    private TextView mAddressView;
    private AvatarDrawable mAvatarDrawable;
    private ImageButton mButton;

    private int mAvatarSize;
    private int mSpacing;
    private int mMinHeight;

    private int mButtonSize;

    private RippleManager mRippleManager = new RippleManager();

    public ContactView(Context context) {
        super(context);

        init(context, null, 0, 0);
    }

    public ContactView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context, attrs, 0, 0);
    }

    public ContactView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context, attrs, defStyleAttr, 0);
    }

    public ContactView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);

        init(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes){
        setWillNotDraw(false);

        mRippleManager.onCreate(this, context, attrs, defStyleAttr, defStyleRes);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ContactView, defStyleAttr, defStyleRes);

        mAvatarSize = a.getDimensionPixelSize(R.styleable.ContactView_cv_avatarSize, ThemeUtil.dpToPx(context, 40));
        mSpacing = a.getDimensionPixelOffset(R.styleable.ContactView_cv_spacing, ThemeUtil.dpToPx(context, 8));
        mMinHeight = a.getDimensionPixelOffset(R.styleable.ContactView_android_minHeight, 0);
        int avatarSrc = a.getResourceId(R.styleable.ContactView_cv_avatarSrc, 0);

        mNameView = new TextView(context);
        mNameView.setGravity(GravityCompat.START);
        mNameView.setSingleLine(true);
        mNameView.setEllipsize(TextUtils.TruncateAt.END);
        int nameTextSize = a.getDimensionPixelSize(R.styleable.ContactView_cv_nameTextSize, 0);
        ColorStateList nameTextColor = a.getColorStateList(R.styleable.ContactView_cv_nameTextColor);
        int nameTextAppearance = a.getResourceId(R.styleable.ContactView_cv_nameTextAppearance, 0);
        if(nameTextAppearance > 0)
            mNameView.setTextAppearance(context, nameTextAppearance);
        if(nameTextSize > 0)
            mNameView.setTextSize(TypedValue.COMPLEX_UNIT_PX, nameTextSize);
        if(nameTextColor != null)
            mNameView.setTextColor(nameTextColor);
        setNameText(a.getString(R.styleable.ContactView_cv_name));

        mAddressView = new TextView(context);
        mAddressView.setGravity(GravityCompat.START);
        mAddressView.setSingleLine(true);
        mAddressView.setEllipsize(TextUtils.TruncateAt.END);
        int addressTextSize = a.getDimensionPixelSize(R.styleable.ContactView_cv_addressTextSize, 0);
        ColorStateList addressTextColor = a.getColorStateList(R.styleable.ContactView_cv_addressTextColor);
        int addressTextAppearance = a.getResourceId(R.styleable.ContactView_cv_addressTextAppearance, 0);
        if(addressTextAppearance > 0)
            mAddressView.setTextAppearance(context, addressTextAppearance);
        if(addressTextSize > 0)
            mAddressView.setTextSize(TypedValue.COMPLEX_UNIT_PX, addressTextSize);
        if(addressTextColor != null)
            mAddressView.setTextColor(addressTextColor);
        setAddressText(a.getString(R.styleable.ContactView_cv_address));

        mButtonSize = a.getDimensionPixelOffset(R.styleable.ContactView_cv_buttonSize, 0);

        if(mButtonSize > 0){
            mButton = new ImageButton(context);
            int resId = a.getResourceId(R.styleable.ContactView_cv_buttonSrc, 0);
            if(resId != 0)
                mButton.setImageResource(resId);
            ViewUtil.setBackground(mButton, BlankDrawable.getInstance());
            mButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            mButton.setFocusableInTouchMode(false);
            mButton.setFocusable(false);
            mButton.setClickable(false);
        }

        a.recycle();

        addView(mNameView);
        addView(mAddressView);
        if(mButton != null)
            addView(mButton);

        mAvatarDrawable = new AvatarDrawable();
        if(avatarSrc != 0)
            setAvatarResource(avatarSrc);
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        if(l == mRippleManager)
            super.setOnClickListener(l);
        else{
            mRippleManager.setOnClickListener(l);
            setOnClickListener(mRippleManager);
        }
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        boolean result = super.onTouchEvent(event);
        return  mRippleManager.onTouchEvent(event) || result;
    }

    public void setAvatarBitmap(Bitmap bm){
        mAvatarDrawable.setImage(bm);
        invalidate();
    }

    public void setAvatarResource(int id){
        if(id == 0)
            return;

        Bitmap bm = BitmapFactory.decodeResource(getContext().getResources(), id);
        setAvatarBitmap(bm);
    }

    public void setAvatarDrawable(Drawable drawable) {
        if(drawable == null)
            return;

        if (drawable instanceof BitmapDrawable)
            setAvatarBitmap(((BitmapDrawable)drawable).getBitmap());
        else{
            Bitmap bm = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bm);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            setAvatarBitmap(bm);
        }
    }

    public void setNameText(CharSequence name){
        mNameView.setText(name);
        mNameView.setVisibility(TextUtils.isEmpty(name) ? View.GONE : View.VISIBLE);
    }

    public void setAddressText(CharSequence address){
        mAddressView.setText(address);
        mAddressView.setVisibility(TextUtils.isEmpty(address) ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
        setAvatarBitmap(bitmap);
    }

    @Override
    public void onBitmapFailed(Drawable errorDrawable) {
        setAvatarDrawable(errorDrawable);
    }

    @Override
    public void onPrepareLoad(Drawable placeHolderDrawable) {
        setAvatarDrawable(placeHolderDrawable);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int nonTextWidth = mAvatarSize + mSpacing * 3 + mButtonSize;

        int ws = MeasureSpec.makeMeasureSpec(widthSize - nonTextWidth, widthMode == MeasureSpec.UNSPECIFIED ? widthMode : MeasureSpec.AT_MOST);
        int hs = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);

        mNameView.measure(ws, hs);
        mAddressView.measure(ws, hs);

        if(mButton != null)
            mButton.measure(MeasureSpec.makeMeasureSpec(mButtonSize, MeasureSpec.EXACTLY), hs);

        int width = widthMode == MeasureSpec.EXACTLY ? widthSize : Math.max(mNameView.getMeasuredWidth(), mAddressView.getMeasuredWidth()) + nonTextWidth;
        int height = Math.max(mAvatarSize + mSpacing * 2, mNameView.getMeasuredHeight() + mAddressView.getMeasuredHeight());

        switch (heightMode){
            case MeasureSpec.EXACTLY:
                height = heightSize;
                break;
            case MeasureSpec.AT_MOST:
                height = Math.min(height, heightSize);
                break;
        }

        height = Math.max(mMinHeight, height);

        if(mButton != null)
            mButton.measure(MeasureSpec.makeMeasureSpec(mButtonSize, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int childRight = right - left;
        int childBottom = bottom - top;
        int childLeft = 0;
        int childTop = 0;

        int y = (childBottom - mAvatarSize) / 2;
        childLeft += mSpacing;
        mAvatarDrawable.setBounds(childLeft, y, childLeft + mAvatarSize, y + mAvatarSize);

        childLeft += mAvatarSize + mSpacing;

        if(mButton != null){
            mButton.layout(childRight - mButtonSize, childTop, childRight, childBottom);
            childRight -= mButtonSize;
        }

        if(mNameView.getVisibility() == View.VISIBLE){
            if(mAddressView.getVisibility() == View.VISIBLE){
                childTop = (childBottom - mNameView.getMeasuredHeight() - mAddressView.getMeasuredHeight()) / 2;
                mNameView.layout(childLeft, childTop, childRight - mSpacing, childTop + mNameView.getMeasuredHeight());
                childTop += mNameView.getMeasuredHeight();
                mAddressView.layout(childLeft, childTop, childRight - mSpacing, childTop + mAddressView.getMeasuredHeight());
            }
            else{
                childTop = (childBottom - mNameView.getMeasuredHeight()) / 2;
                mNameView.layout(childLeft, childTop, childRight - mSpacing, childTop + mNameView.getMeasuredHeight());
            }
        }
        else if(mAddressView.getVisibility() == View.VISIBLE){
            childTop = (childBottom - mAddressView.getMeasuredHeight()) / 2;
            mAddressView.layout(childLeft, childTop, childRight - mSpacing, childTop + mAddressView.getMeasuredHeight());
        }
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        mAvatarDrawable.draw(canvas);
    }

    private class AvatarDrawable extends Drawable{

        private Paint mPaint;
        private BitmapShader mBitmapShader;
        private Bitmap mBitmap;
        private Matrix mMatrix;

        public AvatarDrawable(){
            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mPaint.setStyle(Paint.Style.FILL);

            mMatrix = new Matrix();
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

        private void updateMatrix(){
            if(mBitmap == null)
                return;

            Rect bounds = getBounds();
            if(bounds.width() == 0 || bounds.height() == 0)
                return;

            mMatrix.reset();
            float scale = bounds.height() / (float)Math.min(mBitmap.getWidth(), mBitmap.getHeight());
            mMatrix.setScale(scale, scale, 0, 0);
            mMatrix.postTranslate(bounds.exactCenterX()  - mBitmap.getWidth() * scale / 2, bounds.exactCenterY() - mBitmap.getHeight() * scale / 2);

            mBitmapShader.setLocalMatrix(mMatrix);
        }

        @Override
        protected void onBoundsChange(Rect bounds) {
            updateMatrix();
        }

        @Override
        public void draw(Canvas canvas) {
            if(mBitmap != null){
                Rect bounds = getBounds();
                float x = bounds.exactCenterX();
                float y = bounds.exactCenterY();
                float radius = bounds.height() / 2f;
                mPaint.setShader(mBitmapShader);
                canvas.drawCircle(x, y, radius, mPaint);
            }
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
}
