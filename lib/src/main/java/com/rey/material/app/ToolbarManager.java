package com.rey.material.app;

import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.rey.material.drawable.NavigationDrawerDrawable;
import com.rey.material.drawable.ToolbarRippleDrawable;
import com.rey.material.util.ViewUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * A Manager class to help handling Toolbar used as ActionBar in ActionBarActivity.
 * It help grouping ActionItem in Toolbar and only show the items of current group.
 * It also help manager state of navigation icon.
 * Created by Rey on 1/6/2015.
 */
public class ToolbarManager {

    private AppCompatDelegate mAppCompatDelegate;
    private Toolbar mToolbar;
    private int mRippleStyle;
    private Animator mAnimator;
    private ActionMenuView mMenuView;
    private ToolbarRippleDrawable.Builder mBuilder;

    private int mCurrentGroup = 0;
    private boolean mGroupChanged = false;
    private boolean mMenuDataChanged = true;

    /**
     * Interface definition for a callback to be invoked when the current group is changed.
     */
    public interface OnToolbarGroupChangedListener {

        /**
         * Called when the current group changed.
         * @param oldGroupId The id of old group.
         * @param groupId The id of new group.
         */
        public void onToolbarGroupChanged(int oldGroupId, int groupId);

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
            if(mAppCompatDelegate != null)
                mAppCompatDelegate.invalidateOptionsMenu();
            else
                onPrepareMenu();
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }
    };

    private NavigationManager mNavigationManager;

    public ToolbarManager(AppCompatDelegate delegate, Toolbar toolbar, int defaultGroupId, int rippleStyle, int animIn, int animOut){
        this(delegate, toolbar, defaultGroupId, rippleStyle, new SimpleAnimator(animIn, animOut));
    }

    public ToolbarManager(AppCompatDelegate delegate, Toolbar toolbar, int defaultGroupId, int rippleStyle, Animator animator){
        mAppCompatDelegate = delegate;
        mToolbar = toolbar;
        mCurrentGroup = defaultGroupId;
        mRippleStyle = rippleStyle;
        mAnimator = animator;
        mAppCompatDelegate.setSupportActionBar(toolbar);
    }

    /**
     * Register a listener for current group changed event. Note that it doesn't hold any strong reference to listener, so don't use anonymous listener.
     */
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

    /**
     * Unregister a listener.
     * @param listener
     */
    public void unregisterOnToolbarGroupChangedListener(OnToolbarGroupChangedListener listener){
        for(int i = mListeners.size() - 1; i >= 0; i--){
            WeakReference<OnToolbarGroupChangedListener> ref = mListeners.get(i);
            if(ref.get() == null || ref.get() == listener)
                mListeners.remove(i);
        }
    }

    private void dispatchOnToolbarGroupChanged(int oldGroupId, int groupId){
        for(int i = mListeners.size() - 1; i >= 0; i--){
            WeakReference<OnToolbarGroupChangedListener> ref = mListeners.get(i);
            if(ref.get() == null)
                mListeners.remove(i);
            else
                ref.get().onToolbarGroupChanged(oldGroupId, groupId);
        }
    }

    /**
     * @return The current group of the Toolbar.
     */
    public int getCurrentGroup(){
        return mCurrentGroup;
    }

    /**
     * Set current group of the Toolbar.
     * @param groupId The id of group.
     */
    public void setCurrentGroup(int groupId){
        if(mCurrentGroup != groupId){
            int oldGroupId = mCurrentGroup;
            mCurrentGroup = groupId;
            mGroupChanged = true;
            dispatchOnToolbarGroupChanged(oldGroupId, mCurrentGroup);
            animateOut();
        }
    }

    /**
     * This funcction should be called in onCreateOptionsMenu of Activity or Fragment to inflate a new menu.
     * @param menuId
     */
    public void createMenu(int menuId){
        mToolbar.inflateMenu(menuId);
        mMenuDataChanged = true;
        if(mAppCompatDelegate == null)
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
                item.setVisible(item.getGroupId() == mCurrentGroup || item.getGroupId() == 0);
            }

            mMenuDataChanged = false;
        }
    }

    /**
     * Set a NavigationManager to manage navigation icon state.
     */
    public void setNavigationManager(NavigationManager navigationManager){
        mNavigationManager = navigationManager;
        notifyNavigationStateInvalidated();
    }

    /**
     * Notify the current state of navigation icon is invalid. It should update the state immediately without showing animation.
     */
    public void notifyNavigationStateInvalidated(){
        if(mNavigationManager != null)
            mNavigationManager.notifyStateInvalidated();
    }

    /**
     * Notify the current state of navigation icon is invalid. It should update the state immediately without showing animation.
     */
    public void notifyNavigationStateChanged(){
        if(mNavigationManager != null)
            mNavigationManager.notifyStateChanged();
    }

    /**
     * Notify the progress of animation between 2 states changed. Use this function to sync the progress with another animation.
     * @param isBackState the current state (the end state of animation) is back state or not.
     * @param progress the current progress of animation.
     */
    public void notifyNavigationStateProgressChanged(boolean isBackState, float progress){
        if(mNavigationManager != null)
            mNavigationManager.notifyStateProgressChanged(isBackState, progress);
    }

    /**
     * @return The navigation is in back state or not.
     */
    public boolean isNavigationBackState(){
        return mNavigationManager != null && mNavigationManager.isBackState();
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
        for(int i = 0, count = menuView == null ? 0 : menuView.getChildCount(); i < count; i++){
            View child = menuView.getChildAt(i);
            if(mRippleStyle != 0){
                if(child.getBackground() == null || !(child.getBackground() instanceof ToolbarRippleDrawable))
                    ViewUtil.setBackground(child, getBackground());
            }
        }

        if(mGroupChanged){
            animateIn();
            mGroupChanged = false;
        }
    }

    private void animateOut(){
        ActionMenuView menuView = getMenuView();
        int count = menuView == null ? 0 : menuView.getChildCount();
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

        for(int i = 0, count = menuView == null ? 0 : menuView.getChildCount(); i < count; i++){
            View child = menuView.getChildAt(i);
            Animation anim = mAnimator.getInAnimation(child, i);
            if(anim != null)
                child.startAnimation(anim);
        }
    }

    /**
     * Interface definition for creating animation of menu item view when switch group.
     */
    public interface Animator{

        /**
         * Get the animation of the menu item view will be removed.
         * @param v The menu item view.
         * @param position The position of item.
         * @return
         */
        public Animation getOutAnimation(View v, int position);

        /**
         * Get the animation of the menu item view will be added.
         * @param v The menu item view.
         * @param position The position of item.
         * @return
         */
        public Animation getInAnimation(View v, int position);
    }

    private static class SimpleAnimator implements Animator{
        private int mAnimationIn;
        private int mAnimationOut;

        public SimpleAnimator(int animIn, int animOut){
            mAnimationIn = animIn;
            mAnimationOut = animOut;
        }

        @Override
        public Animation getOutAnimation(View v, int position) {
            return mAnimationOut == 0 ? null : AnimationUtils.loadAnimation(v.getContext(), mAnimationOut);
        }

        @Override
        public Animation getInAnimation(View v, int position) {
            return mAnimationIn == 0 ?  null : AnimationUtils.loadAnimation(v.getContext(), mAnimationIn);
        }
    }

    /**
     * Abstract class to manage the state of navigation icon.
     */
    public static abstract class NavigationManager{

        protected NavigationDrawerDrawable mNavigationIcon;
        protected Toolbar mToolbar;

        /**
         * @param styleId the style res of navigation icon.
         */
        public NavigationManager(NavigationDrawerDrawable navigationIcon, Toolbar toolbar){
            mToolbar = toolbar;
            mNavigationIcon = navigationIcon;
            mToolbar.setNavigationIcon(mNavigationIcon);
            mToolbar.setNavigationOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    onNavigationClick();
                }
            });
        }

        /**
         * Check if current state of navigation icon is back state or not.
         * @return
         */
        public abstract boolean isBackState();

        /**
         * Hangle event click navigation icon. Subclass should override this function.
         */
        public abstract void onNavigationClick();

        /**
         * Notify the current state of navigation icon is invalid. It should update the state immediately without showing animation.
         */
        public void notifyStateInvalidated(){
            mNavigationIcon.switchIconState(isBackState() ? NavigationDrawerDrawable.STATE_ARROW : NavigationDrawerDrawable.STATE_DRAWER, false);
        }

        /**
         * Notify the current state of navigation icon is changed. It should update the state with animation.
         */
        public void notifyStateChanged(){
            mNavigationIcon.switchIconState(isBackState() ? NavigationDrawerDrawable.STATE_ARROW : NavigationDrawerDrawable.STATE_DRAWER, true);
        }

        /**
         * Notify the progress of animation between 2 states changed. Use this function to sync the progress with another animation.
         * @param isBackState the current state (the end state of animation) is back state or not.
         * @param progress the current progress of animation.
         */
        public void notifyStateProgressChanged(boolean isBackState, float progress){
            mNavigationIcon.setIconState(isBackState ? NavigationDrawerDrawable.STATE_ARROW : NavigationDrawerDrawable.STATE_DRAWER, progress);
        }

    }

    /**
     * A Base Navigation Manager that handle navigation state between fragment changing and navigation drawer.
     * If you want to handle state in another case, you should override isBackState(),  shouldSyncDrawerSlidingProgress(), and call notify notifyStateChanged() if need.
      */
    public static class BaseNavigationManager extends NavigationManager{
        protected DrawerLayout mDrawerLayout;
        protected FragmentManager mFragmentManager;
        protected boolean mSyncDrawerSlidingProgress = false;

        /**
         *
         * @param styleId the resourceId of navigation icon style.
         * @param drawerLayout can be null if you don't need to handle navigation state when open/close navigation drawer.
         */
        public BaseNavigationManager(int styleId, FragmentManager fragmentManager, Toolbar toolbar, DrawerLayout drawerLayout){
            super(new NavigationDrawerDrawable.Builder(toolbar.getContext(), styleId).build(), toolbar);
            mDrawerLayout = drawerLayout;
            mFragmentManager = fragmentManager;

            if(mDrawerLayout != null)
                mDrawerLayout.setDrawerListener(new DrawerLayout.DrawerListener() {

                    @Override
                    public void onDrawerSlide(View drawerView, float slideOffset) {
                        BaseNavigationManager.this.onDrawerSlide(drawerView, slideOffset);
                    }

                    @Override
                    public void onDrawerOpened(View drawerView) {
                        BaseNavigationManager.this.onDrawerOpened(drawerView);
                    }

                    @Override
                    public void onDrawerClosed(View drawerView) {
                        BaseNavigationManager.this.onDrawerClosed(drawerView);
                    }

                    @Override
                    public void onDrawerStateChanged(int newState) {
                        BaseNavigationManager.this.onDrawerStateChanged(newState);
                    }

                });

            mFragmentManager.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
                @Override
                public void onBackStackChanged() {
                    onFragmentChanged();
                }
            });
        }

        @Override
        public boolean isBackState() {
            return mFragmentManager.getBackStackEntryCount() > 1 || (mDrawerLayout != null && mDrawerLayout.isDrawerOpen(Gravity.START));
        }

        @Override
        public void onNavigationClick() {}

        /**
         * Check if should sync progress of drawer sliding animation with navigation state changing animation.
         */
        protected boolean shouldSyncDrawerSlidingProgress(){
            if(mFragmentManager.getBackStackEntryCount() > 1)
                return false;

            return true;
        }

        protected void onFragmentChanged(){
            notifyStateChanged();
        }

        /**
         * Handling onDrawerSlide event of DrawerLayout. It'll sync progress of drawer sliding animation with navigation state changing animation if needed.
         * If you also want to handle this event, make sure to call super method.
         */
        protected void onDrawerSlide(View drawerView, float slideOffset){
            if(!mSyncDrawerSlidingProgress)
                return;

            if(mDrawerLayout.isDrawerOpen(GravityCompat.START))
                notifyStateProgressChanged(false, 1f - slideOffset);
            else
                notifyStateProgressChanged(true, slideOffset);
        }

        protected void onDrawerOpened(View drawerView){}

        protected void onDrawerClosed(View drawerView) {}

        /**
         * Handling onDrawerStateChanged event of DrawerLayout. It'll check if should sync progress of drawer sliding animation with navigation state changing animation.
         * If you also want to handle this event, make sure to call super method.
         */
        protected void onDrawerStateChanged(int newState) {
            mSyncDrawerSlidingProgress = (newState == DrawerLayout.STATE_DRAGGING || newState == DrawerLayout.STATE_SETTLING) && shouldSyncDrawerSlidingProgress();
        }

    }

    /**
     * A Manager class extend from {@link BaseNavigationManager} class and add theme supporting.
     */
    public static class ThemableNavigationManager extends BaseNavigationManager implements ThemeManager.OnThemeChangedListener{

        private int mStyleId;
        private int mCurrentStyle;

        /**
         *
         * @param styleId the styleId of navigation icon.
         * @param drawerLayout can be null if you don't need to handle navigation state when open/close navigation drawer.
         */
        public ThemableNavigationManager(int styleId, FragmentManager fragmentManager, Toolbar toolbar, DrawerLayout drawerLayout){
            super(ThemeManager.getInstance().getCurrentStyle(styleId), fragmentManager, toolbar, drawerLayout);
            mStyleId = styleId;
            mCurrentStyle = ThemeManager.getInstance().getCurrentStyle(styleId);
            ThemeManager.getInstance().registerOnThemeChangedListener(this);
        }

        @Override
        public void onThemeChanged(@Nullable ThemeManager.OnThemeChangedEvent event) {
            int style = ThemeManager.getInstance().getCurrentStyle(mStyleId);
            if(mCurrentStyle != style){
                mCurrentStyle = style;
                NavigationDrawerDrawable drawable = new NavigationDrawerDrawable.Builder(mToolbar.getContext(), mCurrentStyle).build();
                drawable.switchIconState(mNavigationIcon.getIconState(), false);
                mNavigationIcon = drawable;
                mToolbar.setNavigationIcon(mNavigationIcon);
            }
        }

    }
}
