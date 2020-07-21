package com.rey.material.app;

import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.os.Build;
import android.os.SystemClock;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.ActionMenuView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.TranslateAnimation;

import com.rey.material.drawable.NavigationDrawerDrawable;
import com.rey.material.drawable.ToolbarRippleDrawable;
import com.rey.material.util.ViewUtil;

import java.util.ArrayList;
import java.util.List;

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
        void onToolbarGroupChanged(int oldGroupId, int groupId);

    }

    private ArrayList<OnToolbarGroupChangedListener> mListeners = new ArrayList<>();

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
     * Register a listener for current group changed event.
     */
    public void registerOnToolbarGroupChangedListener(OnToolbarGroupChangedListener listener){
        if(!mListeners.contains(listener))
            mListeners.add(listener);
    }

    /**
     * Unregister a listener.
     * @param listener
     */
    public void unregisterOnToolbarGroupChangedListener(OnToolbarGroupChangedListener listener){
        mListeners.remove(listener);
    }

    private void dispatchOnToolbarGroupChanged(int oldGroupId, int groupId){
        for(OnToolbarGroupChangedListener listener : mListeners)
            listener.onToolbarGroupChanged(oldGroupId, groupId);
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

    public boolean isNavigationVisisble(){
        return mNavigationManager != null && mNavigationManager.isNavigationVisible();
    }

    public void setNavigationVisisble(boolean visible, boolean animation){
        if(mNavigationManager != null)
            mNavigationManager.setNavigationVisible(visible, animation);
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

        protected boolean mNavigationVisible = true;

        protected long mAnimationDuration;
        private long mAnimTime;
        private List<Object> mAnimations = new ArrayList<>();

        public NavigationManager(NavigationDrawerDrawable navigationIcon, Toolbar toolbar){
            mToolbar = toolbar;
            mNavigationIcon = navigationIcon;
            mToolbar.setNavigationIcon(mNavigationVisible ? mNavigationIcon : null);
            mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onNavigationClick();
                }
            });
            mAnimationDuration = toolbar.getResources().getInteger(android.R.integer.config_shortAnimTime);
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
            mNavigationIcon.switchIconState(isBackState() ? NavigationDrawerDrawable.STATE_ARROW : NavigationDrawerDrawable.STATE_DRAWER, mNavigationVisible);
        }

        /**
         * Notify the progress of animation between 2 states changed. Use this function to sync the progress with another animation.
         * @param isBackState the current state (the end state of animation) is back state or not.
         * @param progress the current progress of animation.
         */
        public void notifyStateProgressChanged(boolean isBackState, float progress){
            mNavigationIcon.setIconState(isBackState ? NavigationDrawerDrawable.STATE_ARROW : NavigationDrawerDrawable.STATE_DRAWER, progress);
        }

        public boolean isNavigationVisible(){
            return mNavigationVisible;
        }

        public void setNavigationVisible(boolean visible, boolean animation){
            if(mNavigationVisible != visible){
                mNavigationVisible = visible;
                long time = SystemClock.uptimeMillis();

                if(!animation) {
                    mToolbar.setNavigationIcon(mNavigationVisible ? mNavigationIcon : null);
                    mAnimTime = time;
                    if(!mNavigationVisible)
                        mNavigationIcon.cancel();
                }
                else{
                    if(mNavigationVisible)
                        animateNavigationIn(time);
                    else
                        animateNavigationOut(time);
                }

            }
        }

        protected Interpolator getInterpolator(boolean in){
            return new DecelerateInterpolator();
        }

        private void cancelAllAnimations(){
            for(Object obj : mAnimations){
                if(obj instanceof Animation)
                    ((Animation)obj).cancel();
                else if(obj instanceof ValueAnimator)
                    ((ValueAnimator)obj).cancel();
            }

            mAnimations.clear();
        }

        private void animateNavigationOut(long time){
            mAnimTime = time;
            cancelAllAnimations();
            mToolbar.setNavigationIcon(null);
            doOnPreDraw(mToolbar, new AnimRunnable(time) {
                @Override
                void doWork() {
                    final ViewData viewData = new ViewData(mToolbar);
                    mToolbar.setNavigationIcon(mNavigationIcon);
                    doOnPreDraw(mToolbar, new AnimRunnable(mTime) {
                        @Override
                        void doWork() {
                            boolean first = true;
                            for (int i = 0, count = mToolbar.getChildCount(); i < count; i++) {
                                View child = mToolbar.getChildAt(i);
                                if (!(child instanceof ActionMenuView)) {
                                    int nextLeft = viewData.getLeft(child);
                                    if (nextLeft < 0)
                                        nextLeft = -child.getLeft() - child.getWidth();

                                    if (first) {
                                        animateViewOut(child, nextLeft, new Runnable() {
                                            @Override
                                            public void run() {
                                                mToolbar.setNavigationIcon(null);
                                                mNavigationIcon.cancel();
                                            }
                                        });
                                        first = false;
                                    } else
                                        animateViewOut(child, nextLeft, null);
                                }
                            }

                            if (first)
                                mToolbar.setNavigationIcon(null);
                        }
                    });
                }
            });
        }

        private void animateViewOut(final View view, final int nextLeft, final Runnable doOnEndRunnable){
            final Interpolator interpolator = getInterpolator(false);
            final int prevLeft = view.getLeft();

            ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
            animator.setDuration(mAnimationDuration);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    float factor = interpolator.getInterpolation(valueAnimator.getAnimatedFraction());
                    float left = prevLeft + (nextLeft - prevLeft) * factor;
                    view.offsetLeftAndRight((int) (left - view.getLeft()));

                    if (valueAnimator.getAnimatedFraction() == 1f) {
                        if (doOnEndRunnable != null)
                            doOnEndRunnable.run();
                    }
                }

            });

            animator.addListener(new AnimatorListener() {
                @Override
                public void onAnimationStart(android.animation.Animator animator) {

                }

                @Override
                public void onAnimationEnd(android.animation.Animator animator) {
                    mAnimations.remove(animator);
                }

                @Override
                public void onAnimationCancel(android.animation.Animator animator) {

                }

                @Override
                public void onAnimationRepeat(android.animation.Animator animator) {

                }
            });

            animator.start();
            mAnimations.add(animator);
        }

        private void animateNavigationIn(long time){
            mAnimTime = time;
            cancelAllAnimations();
            mToolbar.setNavigationIcon(null);
            doOnPreDraw(mToolbar, new AnimRunnable(time) {
                @Override
                void doWork() {
                    final ViewData viewData = new ViewData(mToolbar);
                    mToolbar.setNavigationIcon(mNavigationIcon);
                    doOnPreDraw(mToolbar, new AnimRunnable(mTime) {
                        @Override
                        void doWork() {
                            for (int i = 0, count = mToolbar.getChildCount(); i < count; i++) {
                                View child = mToolbar.getChildAt(i);
                                if (!(child instanceof ActionMenuView)) {
                                    int prevLeft = viewData.getLeft(child);
                                    if (prevLeft < 0)
                                        prevLeft = -child.getLeft() - child.getWidth();

                                    animateViewIn(child, prevLeft);
                                }
                            }
                        }
                    });
                }
            });
        }

        private void animateViewIn(View view, int prevLeft){
            TranslateAnimation anim = new TranslateAnimation(
                    TranslateAnimation.ABSOLUTE, prevLeft - view.getLeft(), TranslateAnimation.ABSOLUTE, 0,
                    TranslateAnimation.ABSOLUTE, 0, TranslateAnimation.ABSOLUTE, 0);

            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mAnimations.remove(animation);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            anim.setInterpolator(getInterpolator(true));
            anim.setDuration(mAnimationDuration);
            view.startAnimation(anim);
            mAnimations.add(anim);
        }

        private void doOnPreDraw(final View v, final Runnable runnable){
            v.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    runnable.run();
                    v.getViewTreeObserver().removeOnPreDrawListener(this);
                    return false;
                }
            });
        }

        static class ViewData{

            List<View> views;
            List<Integer> lefts;

            public ViewData(Toolbar toolbar){
                int count = toolbar.getChildCount();
                views = new ArrayList<>(count);
                lefts = new ArrayList<>(count);

                for(int i = 0; i < count; i++){
                    View child = toolbar.getChildAt(i);
                    if(!(child instanceof ActionMenuView)) {
                        views.add(child);
                        lefts.add(child.getLeft());
                    }
                }
            }

            public int getLeft(View view){
                for(int i = 0, size = views.size(); i < size; i++)
                    if(views.get(i) == view)
                        return lefts.get(i);

                return -1;
            }

        }

        abstract class AnimRunnable implements Runnable{

            long mTime;

            public AnimRunnable(long time){
                mTime = time;
            }

            @Override
            public void run() {
                if(mTime == mAnimTime)
                    doWork();
            }

            abstract void doWork();
        }

    }

    /**
     * A Base Navigation Manager that handle navigation state between fragment changing and navigation drawer.
     * If you want to handle state in another case, you should override isBackState(),  shouldSyncDrawerSlidingProgress(), and call notify notifyStateChanged() if need.
      */
    public static class BaseNavigationManager extends NavigationManager{
        protected DrawerLayout mDrawerLayout;
        protected FragmentManager mFragmentManager;

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
            return mFragmentManager.getBackStackEntryCount() > 1 || (mDrawerLayout != null && mDrawerLayout.isDrawerOpen(GravityCompat.START));
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
            if(!shouldSyncDrawerSlidingProgress()){
                notifyStateInvalidated();
            }
            else {
                if (mDrawerLayout.isDrawerOpen(GravityCompat.START))
                    notifyStateProgressChanged(false, 1f - slideOffset);
                else
                    notifyStateProgressChanged(true, slideOffset);
            }
        }

        protected void onDrawerOpened(View drawerView){}

        protected void onDrawerClosed(View drawerView) {}

        protected void onDrawerStateChanged(int newState) {}

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
                mToolbar.setNavigationIcon(mNavigationVisible ? mNavigationIcon : null);
            }
        }

    }
}
