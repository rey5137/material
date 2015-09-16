package com.rey.material.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.widget.TextView;

import com.rey.material.R;
import com.rey.material.app.ThemeManager;
import com.rey.material.drawable.RippleDrawable;
import com.rey.material.util.ThemeUtil;
import com.rey.material.util.ViewUtil;

/**
 * Created by Rey on 9/15/2015.
 */
public class TabIndicatorView extends RecyclerView implements ThemeManager.OnThemeChangedListener{

    protected int mStyleId;
    protected int mCurrentStyle = ThemeManager.THEME_UNDEFINED;

    private int mMode;
    private int mTabPadding;
    private int mTabRippleStyle;
    private int mTextAppearance;
    private boolean mTabSingleLine;

    private int mIndicatorOffset;
    private int mIndicatorWidth;
    private int mIndicatorHeight;
    private boolean mIndicatorAtTop;

    private Paint mPaint;

    public static final int MODE_SCROLL = 0;
    public static final int MODE_FIXED = 1;

    public static final int SCROLL_STATE_IDLE = 0;
    public static final int SCROLL_STATE_DRAGGING = 1;
    public static final int SCROLL_STATE_SETTLING = 2;

    private int mSelectedPosition;
    private boolean mScrolling;
    private boolean mIsRtl;

    private LayoutManager mLayoutManager;
    private Adapter mAdapter;

    private Runnable mTabAnimSelector;

    public TabIndicatorView(Context context) {
        super(context);

        init(context, null, 0, 0);
    }

    public TabIndicatorView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context, attrs, 0, 0);
    }

    public TabIndicatorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context, attrs, defStyleAttr, 0);
    }

    protected void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes){
        setHorizontalScrollBarEnabled(false);

        mTabPadding = -1;
        mTabSingleLine = true;
        mIndicatorHeight = -1;
        mIndicatorAtTop = false;
        mScrolling = false;
        mIsRtl = false;

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(ThemeUtil.colorAccent(context, 0xFFFFFFFF));

        mAdapter = new Adapter();
        setAdapter(mAdapter);
        mLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        setLayoutManager(mLayoutManager);
        setItemAnimator(new DefaultItemAnimator());
        addOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if(newState == RecyclerView.SCROLL_STATE_IDLE){
                    updateIndicator(mLayoutManager.findViewByPosition(mSelectedPosition));
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                updateIndicator(mLayoutManager.findViewByPosition(mSelectedPosition));
            }
        });

        applyStyle(context, attrs, defStyleAttr, defStyleRes);

        if(!isInEditMode())
            mStyleId = ThemeManager.getStyleId(context, attrs, defStyleAttr, defStyleRes);
    }

    public void applyStyle(int resId){
        ViewUtil.applyStyle(this, resId);
        applyStyle(getContext(), null, 0, resId);
    }

    protected void applyStyle(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes){
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TabPageIndicator, defStyleAttr, defStyleRes);

        int textAppearance = 0;
        int mode = -1;
        int rippleStyle = 0;

        for(int i = 0, count = a.getIndexCount(); i < count; i++){
            int attr = a.getIndex(i);
            if(attr == R.styleable.TabPageIndicator_tpi_tabPadding)
                mTabPadding = a.getDimensionPixelSize(attr, 0);
            else if(attr == R.styleable.TabPageIndicator_tpi_tabRipple)
                rippleStyle = a.getResourceId(attr, 0);
            else if(attr == R.styleable.TabPageIndicator_tpi_indicatorColor)
                mPaint.setColor(a.getColor(attr, 0));
            else if(attr == R.styleable.TabPageIndicator_tpi_indicatorHeight)
                mIndicatorHeight = a.getDimensionPixelSize(attr, 0);
            else if(attr == R.styleable.TabPageIndicator_tpi_indicatorAtTop)
                mIndicatorAtTop = a.getBoolean(attr, true);
            else if(attr == R.styleable.TabPageIndicator_tpi_tabSingleLine)
                mTabSingleLine = a.getBoolean(attr, true);
            else if(attr == R.styleable.TabPageIndicator_android_textAppearance)
                textAppearance = a.getResourceId(attr, 0);
            else if(attr == R.styleable.TabPageIndicator_tpi_mode)
                mode = a.getInteger(attr, 0);
        }

        a.recycle();

        if(mTabPadding < 0)
            mTabPadding = ThemeUtil.dpToPx(context, 12);

        if(mIndicatorHeight < 0)
            mIndicatorHeight = ThemeUtil.dpToPx(context, 2);

        boolean shouldNotify = false;

        if(mode >= 0 && mMode != mode){
            mMode = mode;
            mAdapter.setFixedWidth(0, 0);
            shouldNotify = true;
        }

        if(textAppearance != 0 && mTextAppearance != textAppearance){
            mTextAppearance = textAppearance;
            shouldNotify = true;
        }

        if(rippleStyle != 0 && rippleStyle != mTabRippleStyle){
            mTabRippleStyle = rippleStyle;
            shouldNotify = true;
        }

        if(shouldNotify)
            mAdapter.notifyDataSetChanged();

        requestLayout();
    }

    public void setTabFactory(TabFactory factory){
        mAdapter.setFactory(factory);
    }

    private void animateToTab(final int position) {
        if(position < 0 || position >= mAdapter.getItemCount())
            return;

        if (mTabAnimSelector != null)
            removeCallbacks(mTabAnimSelector);

        mTabAnimSelector = new Runnable() {
            public void run() {
                View v = mLayoutManager.findViewByPosition(position);
                if(!mScrolling)
                    updateIndicator(v);

                smoothScrollToPosition(mSelectedPosition);
                mTabAnimSelector = null;
            }
        };

        post(mTabAnimSelector);
    }

    private void updateIndicator(int offset, int width){
        mIndicatorOffset = offset;
        mIndicatorWidth = width;
        invalidate();
    }

    private void updateIndicator(View anchorView){
        if(anchorView != null) {
            updateIndicator(anchorView.getLeft(), anchorView.getMeasuredWidth());
            ((Checkable)anchorView).setChecked(true);
        }
        else {
            updateIndicator(getWidth(), 0);
        }
    }

    /**
     * Set the current tab of this TabIndicatorView.
     * @param position The position of current tab.
     */
    public void setCurrentTab(int position) {
        if(mSelectedPosition != position){
            View v = mLayoutManager.findViewByPosition(mSelectedPosition);
            if(v != null)
                ((Checkable)v).setChecked(false);
        }

        mSelectedPosition = position;
        View v = mLayoutManager.findViewByPosition(mSelectedPosition);
        if(v != null)
            ((Checkable)v).setChecked(true);

        animateToTab(position);
    }

    @Override
    public void onThemeChanged(ThemeManager.OnThemeChangedEvent event) {
        int style = ThemeManager.getInstance().getCurrentStyle(mStyleId);
        if(mCurrentStyle != style){
            mCurrentStyle = style;
            applyStyle(mCurrentStyle);
        }
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        // Re-post the selector we saved
        if (mTabAnimSelector != null)
            post(mTabAnimSelector);

        if(mStyleId != 0) {
            ThemeManager.getInstance().registerOnThemeChangedListener(this);
            onThemeChanged(null);
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mTabAnimSelector != null)
            removeCallbacks(mTabAnimSelector);

        if(mStyleId != 0)
            ThemeManager.getInstance().unregisterOnThemeChangedListener(this);
    }

    @Override
    public void onRtlPropertiesChanged(int layoutDirection) {
        boolean rtl = layoutDirection == LAYOUT_DIRECTION_RTL;
        if(mIsRtl != rtl) {
            mIsRtl = rtl;
            invalidate();
        }
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        super.onMeasure(widthSpec, heightSpec);

        if(mMode == MODE_FIXED){
            int totalWidth = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
            int count = mAdapter.getItemCount();
            if(count > 0) {
                int width = totalWidth / count;
                int lastWidth = totalWidth - width * (count - 1);
                mAdapter.setFixedWidth(width, lastWidth);
            }
            else
                mAdapter.setFixedWidth(totalWidth, totalWidth);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        updateIndicator(mLayoutManager.findViewByPosition(mSelectedPosition));
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        super.draw(canvas);

        int x = mIndicatorOffset;
        int y = mIndicatorAtTop ? 0 : getHeight() - mIndicatorHeight;
        canvas.drawRect(x, y, x + mIndicatorWidth, y + mIndicatorHeight, mPaint);

        //TODO: handle it

//        if(isInEditMode())
//            canvas.drawRect(getPaddingLeft(), y, getPaddingLeft() + mTabContainer.getChildAt(0).getWidth(), y + mIndicatorHeight, mPaint);
    }

    protected void onTabScrollStateChanged(int state){
        if(state == ViewPager.SCROLL_STATE_IDLE){
            mScrolling = false;
            View v = mLayoutManager.findViewByPosition(mSelectedPosition);
            updateIndicator(v);
        }
        else
            mScrolling = true;
    }

    protected void onTabScrolled(int position, float positionOffset) {
        View scrollView = mLayoutManager.findViewByPosition(position);
        View nextView = mLayoutManager.findViewByPosition(position + 1);

        if(scrollView != null && nextView != null){
            int width_scroll = scrollView.getMeasuredWidth();
            int width_next = nextView.getMeasuredWidth();
            float distance = (width_scroll + width_next) / 2f;

            int width =  (int)(width_scroll + (width_next - width_scroll) * positionOffset + 0.5f);
            int offset = (int)(scrollView.getLeft() + width_scroll / 2f + distance * positionOffset - width / 2f + 0.5f);
            updateIndicator(offset, width);
        }
    }

    protected void onTabSelected(int position){
        setCurrentTab(position);
    }

    public static abstract class TabFactory {

        private TabIndicatorView mView;

        public abstract int getTabCount();

        public abstract boolean isIconTab(int position);

        public abstract Drawable getIcon(int position);

        public abstract CharSequence getText(int position);

        public abstract void setCurrentTab(int position);

        public abstract int getCurrentTab();

        protected void setTabIndicatorView(TabIndicatorView view){
            mView = view;
        }

        public final void notifyTabScrollStateChanged(int state){
            mView.onTabScrollStateChanged(state);
        }

        public final void notifyTabScrolled(int position, float positionOffset) {
            mView.onTabScrolled(position, positionOffset);
        }

        public final void notifyTabSelected(int position){
            mView.onTabSelected(position);
        }

        public final void notifyDataSetChanged(){
            mView.getAdapter().notifyDataSetChanged();
        }

        public final void notifyTabChanged(int position) {
            mView.getAdapter().notifyItemRangeChanged(position, 1);
        }

        public final void notifyTabRangeChanged(int positionStart, int itemCount) {
            mView.getAdapter().notifyItemRangeChanged(positionStart, itemCount);
        }

        public final void notifyTabInserted(int position) {
            mView.getAdapter().notifyItemRangeInserted(position, 1);
        }

        public final void notifyTabMoved(int fromPosition, int toPosition) {
            mView.getAdapter().notifyItemMoved(fromPosition, toPosition);
        }

        public final void notifyTabRangeInserted(int positionStart, int itemCount) {
            mView.getAdapter().notifyItemRangeInserted(positionStart, itemCount);
        }

        public final void notifyTabRemoved(int position) {
            mView.getAdapter().notifyItemRangeRemoved(position, 1);
        }

        public final void notifyTabRangeRemoved(int positionStart, int itemCount) {
            mView.getAdapter().notifyItemRangeRemoved(positionStart, itemCount);
        }
    }

    class Adapter extends RecyclerView.Adapter<ViewHolder> implements OnClickListener {

        TabFactory mFactory;

        static final int TYPE_TEXT = 0;
        static final int TYPE_ICON = 1;

        int mFixedWidth;
        int mLastFixedWidth;

        public void setFactory(TabFactory factory){
            if(mFactory != null)
                mFactory.setTabIndicatorView(null);

            int prevCount = getItemCount();
            if(prevCount > 0)
                notifyItemRangeRemoved(0, prevCount);

            mFactory = factory;
            if(mFactory != null)
                mFactory.setTabIndicatorView(TabIndicatorView.this);
            int count = getItemCount();
            if(count > 0)
                notifyItemRangeInserted(0, count);

            if(mFactory != null)
                onTabSelected(mFactory.getCurrentTab());
        }

        public void setFixedWidth(int width, int lastWidth){
            if(mFixedWidth != width || mLastFixedWidth != lastWidth){
                mFixedWidth = width;
                mLastFixedWidth = lastWidth;

                int count = getItemCount();
                if(count > 0)
                    notifyItemRangeChanged(0, count);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = null;
            switch (viewType){
                case TYPE_TEXT:
                    v = new CheckedTextView(parent.getContext());
                    break;
                case TYPE_ICON:
                    v = new ImageButton(parent.getContext());
                    break;
            }

            ViewHolder holder = new ViewHolder(v);
            v.setTag(holder);
            v.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));

            switch (viewType){
                case TYPE_TEXT:
                    holder.textView.setCheckMarkDrawable(null);
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
                        holder.textView.setTextAlignment(TEXT_ALIGNMENT_GRAVITY);
                    holder.textView.setGravity(Gravity.CENTER);
                    holder.textView.setEllipsize(TextUtils.TruncateAt.END);
                    holder.textView.setOnClickListener(this);
                    holder.textView.setTextAppearance(getContext(), mTextAppearance);
                    if(mTabSingleLine)
                        holder.textView.setSingleLine(true);
                    else {
                        holder.textView.setSingleLine(false);
                        holder.textView.setMaxLines(2);
                    }
                    if(mTabRippleStyle > 0)
                        ViewUtil.setBackground(holder.textView, new RippleDrawable.Builder(getContext(), mTabRippleStyle).build());
                    holder.textView.setPadding(mTabPadding, 0, mTabPadding, 0);
                    break;
                case TYPE_ICON:
                    holder.iconView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                    if(mTabRippleStyle > 0)
                        ViewUtil.setBackground(holder.iconView, new RippleDrawable.Builder(getContext(), mTabRippleStyle).build());
                    holder.iconView.setPadding(mTabPadding, 0, mTabPadding, 0);
                    break;
            }

            return holder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            int viewType = getItemViewType(position);

            ViewGroup.LayoutParams params = holder.itemView.getLayoutParams();
            if(mFixedWidth > 0)
                params.width = position == getItemCount() - 1 ? mLastFixedWidth : mFixedWidth;
            else
                params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            holder.itemView.setLayoutParams(params);

            switch (viewType){
                case TYPE_TEXT:
                    holder.textView.setText(mFactory.getText(position));
                    holder.textView.setChecked(position == mSelectedPosition);
                    break;
                case TYPE_ICON:
                    holder.iconView.setImageDrawable(mFactory.getIcon(position));
                    holder.iconView.setChecked(position == mSelectedPosition);
                    break;
            }
        }

        @Override
        public int getItemViewType(int position) {
            return mFactory.isIconTab(position) ? TYPE_ICON : TYPE_TEXT;
        }

        @Override
        public int getItemCount() {
            return mFactory == null ? 0 : mFactory.getTabCount();
        }

        @Override
        public void onClick(View view) {
            ViewHolder holder = (ViewHolder)view.getTag();
            mFactory.setCurrentTab(holder.getAdapterPosition());
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        CheckedTextView textView;

        CheckedImageView iconView;

        public ViewHolder(View itemView) {
            super(itemView);
            if(itemView instanceof CheckedImageView)
                iconView = (CheckedImageView)itemView;
            else if(itemView instanceof CheckedTextView)
                textView = (CheckedTextView)itemView;
        }

    }

    public static class ViewPagerFactory extends TabFactory implements ViewPager.OnPageChangeListener {

        ViewPager mViewPager;

        public ViewPagerFactory(ViewPager vp){
            mViewPager = vp;
            mViewPager.addOnPageChangeListener(this);
        }

        @Override
        public int getTabCount() {
            return mViewPager.getAdapter().getCount();
        }

        @Override
        public boolean isIconTab(int position) {
            return false;
        }

        @Override
        public Drawable getIcon(int position) {
            return null;
        }

        @Override
        public CharSequence getText(int position) {
            return mViewPager.getAdapter().getPageTitle(position);
        }

        @Override
        public void setCurrentTab(int position) {
            mViewPager.setCurrentItem(position, true);
        }

        @Override
        public int getCurrentTab() {
            return mViewPager.getCurrentItem();
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            notifyTabScrolled(position, positionOffset);
        }

        @Override
        public void onPageSelected(int position) {
            notifyTabSelected(position);
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            switch (state){
                case ViewPager.SCROLL_STATE_IDLE:
                    notifyTabScrollStateChanged(SCROLL_STATE_IDLE);
                    break;
                case ViewPager.SCROLL_STATE_DRAGGING:
                    notifyTabScrollStateChanged(SCROLL_STATE_DRAGGING);
                    break;
                case ViewPager.SCROLL_STATE_SETTLING:
                    notifyTabScrollStateChanged(SCROLL_STATE_SETTLING);
                    break;
            }
        }
    }
}
