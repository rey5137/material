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
public class ToolbarManager {

    private ActionBarActivity mActivity;
    private Toolbar mToolbar;
    private int mRippleStyle;
    private Animator mAnimator;
    private ActionMenuView mMenuView;
    private ToolbarRippleDrawable.Builder mBuilder;

    private int mCurrentGroup = 0;
    private boolean mGroupChanged = false;
    private boolean mMenuDataChanged = true;

    public interface OnToolbarGroupChangedListener {

        public void onToolbarGroupChanged(int groupId);

    }

    private ArrayList<WeakReference<OnToolbarGroupChangedListener>> mListeners = new ArrayList<>();

    private ViewTreeObserver.OnGlobalLayoutListener mOnGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            ToolbarManager.this.onGlobalLayout();
        }
    };

    private ArrayList<Animation> mAnimations = new ArrayList<>();

    private Animation.AnimationListener mOutAnimationEndListener = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            if(mActivity != null)
                mActivity.supportInvalidateOptionsMenu();
            else
                onPrepareMenu();
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }
    };

    public ToolbarManager(ActionBarActivity activity, Toolbar toolbar, int defaultGroupId, int rippleStyle, Animator animator){
        mActivity = activity;
        mToolbar = toolbar;
        mCurrentGroup = defaultGroupId;
        mRippleStyle = rippleStyle;
        mAnimator = animator;
    }

    public ToolbarManager(ActionBarActivity activity, Toolbar toolbar, int defaultGroupId, int rippleStyle, int animIn, int animOut){
        mActivity = activity;
        mToolbar = toolbar;
        mCurrentGroup = defaultGroupId;
        mRippleStyle = rippleStyle;
        mAnimator = new SimpleAnimator(animIn, animOut);
    }

    public void registerOnToolbarGroupChangedListener(OnToolbarGroupChangedListener listener){
        for(int i = mListeners.size() - 1; i >= 0; i--){
            WeakReference<OnToolbarGroupChangedListener> ref = mListeners.get(i);
            if(ref.get() == null)
                mListeners.remove(i);
            else if(ref.get() == listener)
                return;
        }

        mListeners.add(new WeakReference<OnToolbarGroupChangedListener>(listener));
    }

    public void unregisterOnToolbarGroupChangedListener(OnToolbarGroupChangedListener listener){
        for(int i = mListeners.size() - 1; i >= 0; i--){
            WeakReference<OnToolbarGroupChangedListener> ref = mListeners.get(i);
            if(ref.get() == null || ref.get() == listener)
                mListeners.remove(i);
        }
    }

    private void dispatchOnToolbarGroupChanged(int groupId){
        for(int i = mListeners.size() - 1; i >= 0; i--){
            WeakReference<OnToolbarGroupChangedListener> ref = mListeners.get(i);
            if(ref.get() == null)
                mListeners.remove(i);
            else
                ref.get().onToolbarGroupChanged(groupId);
        }
    }

    public int getCurrentGroup(){
        return mCurrentGroup;
    }

    public void setCurrentGroup(int groupId){
        if(mCurrentGroup != groupId){
            mCurrentGroup = groupId;
            mGroupChanged = true;
            dispatchOnToolbarGroupChanged(mCurrentGroup);
            animateOut();
        }
    }

    public void createMenu(int menuId){
        mToolbar.inflateMenu(menuId);
        mMenuDataChanged = true;
        if(mActivity == null)
            onPrepareMenu();
    }

    /**
     * This function should be called in onPrepareOptionsMenu(Menu) of Activity that use
     * Toolbar as ActionBar, or after inflating menu.
     */
    public void onPrepareMenu(){
        if(mGroupChanged || mMenuDataChanged){
            mToolbar.getViewTreeObserver().addOnGlobalLayoutListener(mOnGlobalLayoutListener);

            Menu menu = mToolbar.getMenu();
            for(int i = 0, count = menu.size(); i < count; i++){
                MenuItem item = menu.getItem(i);
                item.setVisible(item.getGroupId() == mCurrentGroup);
            }

            mMenuDataChanged = false;
        }
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
        }

        if(mGroupChanged){
            animateIn();
            mGroupChanged = false;
        }
    }

    private void animateOut(){
        ActionMenuView menuView = getMenuView();
        int count = menuView.getChildCount();
        Animation slowestAnimation = null;
        mAnimations.clear();
        mAnimations.ensureCapacity(count);

        for(int i = 0; i < count; i++){
            View child = menuView.getChildAt(i);
            Animation anim = mAnimator.getOutAnimation(child, i);
            mAnimations.add(anim);
            if(anim != null)
                if(slowestAnimation == null || slowestAnimation.getStartOffset() + slowestAnimation.getDuration() < anim.getStartOffset() + anim.getDuration())
                    slowestAnimation = anim;
        }

        if(slowestAnimation == null)
            mOutAnimationEndListener.onAnimationEnd(null);
        else {
            slowestAnimation.setAnimationListener(mOutAnimationEndListener);

            for(int i = 0; i < count; i++){
                Animation anim = mAnimations.get(i);
                if(anim != null)
                    menuView.getChildAt(i).startAnimation(anim);
            }
        }

        mAnimations.clear();
    }

    private void animateIn(){
        ActionMenuView menuView = getMenuView();

        for(int i = 0, count = menuView.getChildCount(); i < count; i++){
            View child = menuView.getChildAt(i);
            Animation anim = mAnimator.getInAnimation(child, i);
            if(anim != null)
                child.startAnimation(anim);
        }
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
