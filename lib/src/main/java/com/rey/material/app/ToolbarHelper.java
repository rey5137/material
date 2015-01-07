package com.rey.material.app;

import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.rey.material.drawable.ToolbarRippleDrawable;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by Rey on 1/6/2015.
 */
public class ToolbarHelper {

    private ActionBarActivity mActivity;
    private Toolbar mToolbar;
    private int mContextualGroupId;
    private int mRippleStyle;
    private Animator mAnimator;
    private ActionMenuView mMenuView;
    private ToolbarRippleDrawable.Builder mBuilder;

    public interface OnContextualModeChangedListener{

        public void onContextualModeChanged(boolean enable);

    }

    private ArrayList<WeakReference<OnContextualModeChangedListener>> mListeners = new ArrayList<>();

    private boolean mIsContextualMode = false;
    private boolean mContextualModeChanged = false;
    private boolean mMenuDataChanged = true;

    private ViewTreeObserver.OnGlobalLayoutListener mOnGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            ToolbarHelper.this.onGlobalLayout();
        }
    };

    private ArrayList<ItemData> mData = new ArrayList<>();
    private View mOverflowButton;
    private int mOverflowButtonPosition;

    public ToolbarHelper(ActionBarActivity activity, Toolbar toolbar, int contextualGroupId, int rippleStyle, Animator animator){
        mActivity = activity;
        mToolbar = toolbar;
        mContextualGroupId = contextualGroupId;
        mRippleStyle = rippleStyle;
        mAnimator = animator;
    }

    public ToolbarHelper(ActionBarActivity activity, Toolbar toolbar, int contextualGroupId, int rippleStyle, int animIn, int animOut){
        mActivity = activity;
        mToolbar = toolbar;
        mContextualGroupId = contextualGroupId;
        mRippleStyle = rippleStyle;
        mAnimator = new SimpleAnimator(animIn, animOut);
    }

    public void registerOnContextualModeChangedListener(OnContextualModeChangedListener listener){
        for(int i = mListeners.size() - 1; i >= 0; i--){
            WeakReference<OnContextualModeChangedListener> ref = mListeners.get(i);
            if(ref.get() == null)
                mListeners.remove(i);
            else if(ref.get() == listener)
                return;
        }

        mListeners.add(new WeakReference<OnContextualModeChangedListener>(listener));
    }

    public void unregisterOnContextualModeChangedListener(OnContextualModeChangedListener listener){
        for(int i = mListeners.size() - 1; i >= 0; i--){
            WeakReference<OnContextualModeChangedListener> ref = mListeners.get(i);
            if(ref.get() == null || ref.get() == listener)
                mListeners.remove(i);
        }
    }

    private void dispatchOnContextualModeChanged(boolean enable){
        for(int i = mListeners.size() - 1; i >= 0; i--){
            WeakReference<OnContextualModeChangedListener> ref = mListeners.get(i);
            if(ref.get() == null)
                mListeners.remove(i);
            else
                ref.get().onContextualModeChanged(enable);
        }
    }

    public boolean isContextualMode(){
        return mIsContextualMode;
    }

    public void setContextualMode(boolean b){
        if(mIsContextualMode != b){
            mIsContextualMode = b;
            mContextualModeChanged = true;
            dispatchOnContextualModeChanged(mIsContextualMode);
            animateOut();
        }
    }

    public void createMenu(int menuId){
        mToolbar.inflateMenu(menuId);

        Menu menu = mToolbar.getMenu();
        int count = menu.size();
        while(mData.size() > count)
            mData.remove(mData.size() - 1);

        for(int i = 0; i < count; i ++){
            ItemData data;
            if(i < mData.size())
                data = mData.get(i);
            else {
                data = new ItemData();
                mData.add(data);
            }
            data.menuItem = menu.getItem(i);
        }

        mMenuDataChanged = true;
    }

    /**
     * This function should be called in onPrepareOptionsMenu(Menu) of Activity that use
     * Toolbar as ActionBar, or after inflating menu.
     */
    public void onPrepareMenu(){
        if(mContextualModeChanged || mMenuDataChanged){
            mToolbar.getViewTreeObserver().addOnGlobalLayoutListener(mOnGlobalLayoutListener);

            for(ItemData data: mData) {
                data.menuItem.setVisible((data.menuItem.getGroupId() == mContextualGroupId) == mIsContextualMode);
                data.view = null;
            }

            mOverflowButton = null;
            mMenuDataChanged = false;
        }
        else{
            for(ItemData data: mData)
                data.menuItem.setVisible((data.menuItem.getGroupId() == mContextualGroupId) == mIsContextualMode);
        }
    }

    private int indexOfItem(int itemId){
        for(int i = 0, count = mData.size(); i < count; i ++)
            if(mData.get(i).menuItem.getItemId() == itemId)
                return i;

        return -1;
    }

    private ToolbarRippleDrawable getBackground(){
        if(mBuilder == null)
            mBuilder = new ToolbarRippleDrawable.Builder(mToolbar.getContext(), mRippleStyle);

        return mBuilder.build();
    }

    private ActionMenuView getMenuView(){
        if(mMenuView == null){
            for (int i = 0; i < mToolbar.getChildCount(); i++) {
                View child = mToolbar.getChildAt(i);
                if (child instanceof ActionMenuView) {
                    mMenuView = (ActionMenuView) child;
                    break;
                }
            }
        }

        return mMenuView;
    }

    private void onGlobalLayout() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            mToolbar.getViewTreeObserver().removeOnGlobalLayoutListener(mOnGlobalLayoutListener);
        else
            mToolbar.getViewTreeObserver().removeGlobalOnLayoutListener(mOnGlobalLayoutListener);

        ActionMenuView menuView = getMenuView();

        for(int i = 0; i < menuView.getChildCount(); i++){
            View child = menuView.getChildAt(i);
            if(mRippleStyle != 0){
                if(child.getBackground() == null || !(child.getBackground() instanceof ToolbarRippleDrawable))
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                        child.setBackground(getBackground());
                    else
                        child.setBackgroundDrawable(getBackground());
            }

            int index = indexOfItem(child.getId());
            if(index >= 0) {
                mData.get(index).view = child;
                mData.get(index).pos = i;
            }
            else {
                mOverflowButton = child;
                mOverflowButtonPosition = i;
            }
        }

        if(mContextualModeChanged){
            animateIn();
            mContextualModeChanged = false;
        }
    }

    private void animateOut(){
        Animation slowestAnimation = null;

        for(ItemData data: mData)
            if(data.view != null) {
                data.animation = mAnimator.getOutAnimation(data.view, data.pos);
                if(slowestAnimation == null || slowestAnimation.getStartOffset() + slowestAnimation.getDuration() < data.animation.getStartOffset() + data.animation.getDuration())
                    slowestAnimation = data.animation;
            }

        Animation overflowButtonAnimation = null;
        if(mOverflowButton != null) {
            overflowButtonAnimation = mAnimator.getOutAnimation(mOverflowButton, mOverflowButtonPosition);
            if(slowestAnimation == null || slowestAnimation.getStartOffset() + slowestAnimation.getDuration() < overflowButtonAnimation.getStartOffset() + overflowButtonAnimation.getDuration())
                slowestAnimation = overflowButtonAnimation;
        }

        if(slowestAnimation == null)
            mActivity.supportInvalidateOptionsMenu();
        else {
            slowestAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mActivity.supportInvalidateOptionsMenu();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });

            for(ItemData data: mData)
                if(data.view != null) {
                    data.view.startAnimation(data.animation);
                    data.animation = null;
                }

            if(mOverflowButton != null)
                mOverflowButton.startAnimation(overflowButtonAnimation);
        }
    }

    private void animateIn(){
        for(ItemData data: mData)
            if(data.view != null)
                data.view.startAnimation(mAnimator.getInAnimation(data.view, data.pos));

        if(mOverflowButton != null)
            mOverflowButton.startAnimation(mAnimator.getInAnimation(mOverflowButton, mOverflowButtonPosition));
    }

    class ItemData{
        public MenuItem menuItem;
        public View view;
        public int pos;
        public Animation animation;
    }

    public interface Animator{

        public Animation getOutAnimation(View v, int position);

        public Animation getInAnimation(View v, int position);
    }

    private class SimpleAnimator implements Animator{
        private int mAnimationIn;
        private int mAnimationOut;

        public SimpleAnimator(int animIn, int animOut){
            mAnimationIn = animIn;
            mAnimationOut = animOut;
        }

        @Override
        public Animation getOutAnimation(View v, int position) {
            return AnimationUtils.loadAnimation(mToolbar.getContext(), mAnimationOut);
        }

        @Override
        public Animation getInAnimation(View v, int position) {
            return AnimationUtils.loadAnimation(mToolbar.getContext(), mAnimationIn);
        }
    }
}
