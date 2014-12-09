package com.rey.material.demo;

import java.lang.reflect.Field;
import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.rey.material.drawable.NavigationDrawerDrawable;
import com.rey.material.util.ThemeUtil;
import com.rey.material.view.TabPageIndicator;

public class MainActivity extends ActionBarActivity implements AdapterView.OnItemClickListener {

	private DrawerLayout dl_navigator;
	private FrameLayout fl_drawer;
	private ListView lv_drawer;
	private ViewPager vp;
	private TabPageIndicator tpi;
	
	private DrawerAdapter mDrawerAdapter;
	private PagerAdapter mPagerAdapter;
	
	private Toolbar toolbar;
	private NavigationDrawerDrawable mNavigatorDrawable;
	
	private Tab[] mItems = new Tab[]{Tab.PROGRESS, Tab.BUTTONS, Tab.SWITCHES, Tab.TEXTFIELDS, Tab.SNACKBARS};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
				
		dl_navigator = (DrawerLayout)findViewById(R.id.main_dl);
		fl_drawer = (FrameLayout)findViewById(R.id.main_fl_drawer);
		lv_drawer = (ListView)findViewById(R.id.main_lv_drawer);
		toolbar = (Toolbar)findViewById(R.id.main_toolbar);
		vp = (ViewPager)findViewById(R.id.main_vp);
		tpi = (TabPageIndicator)findViewById(R.id.main_tpi);
		
		setSupportActionBar(toolbar);
		mNavigatorDrawable = new NavigationDrawerDrawable.Builder(this, null, R.style.NavigationDrawerDrawable).build();
		toolbar.setNavigationIcon(mNavigatorDrawable);
		
		toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dl_navigator.openDrawer(fl_drawer);
			}
			
		});
		
		dl_navigator.setDrawerListener(new DrawerLayout.DrawerListener() {
			
			@Override
			public void onDrawerStateChanged(int state) {}
			
			@Override
			public void onDrawerSlide(View v, float factor) {				
				if(dl_navigator.isDrawerOpen(GravityCompat.START))
					mNavigatorDrawable.setIconState(NavigationDrawerDrawable.STATE_DRAWER, 1f - factor);
				else
					mNavigatorDrawable.setIconState(NavigationDrawerDrawable.STATE_ARROW, factor);
			}
			
			@Override
			public void onDrawerOpened(View v) {}
			
			@Override
			public void onDrawerClosed(View v) {}
			
		});
		
		mDrawerAdapter = new DrawerAdapter();
		lv_drawer.setAdapter(mDrawerAdapter);		
		lv_drawer.setOnItemClickListener(this);
		
		mPagerAdapter = new PagerAdapter(getSupportFragmentManager(), mItems);
		vp.setAdapter(mPagerAdapter);
		tpi.setViewPager(vp);
		tpi.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int position) {
				mDrawerAdapter.setSelected(mItems[position]);				
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {}
			
			@Override
			public void onPageScrollStateChanged(int state) {}
			
		});
		
		vp.setCurrentItem(2);
		
//		FloatingActionButton fab = FloatingActionButton.make(this, R.style.FloatingActionButton);
//		fab.show(this, 100, 100, Gravity.LEFT);		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		new MenuInflater(this).inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		vp.setCurrentItem(position);
		dl_navigator.closeDrawer(fl_drawer);
	}
		
	public enum Tab {
	    PROGRESS ("Progresses"),
	    BUTTONS ("Buttons"),
	    SWITCHES ("Switches"),
	    TEXTFIELDS ("Textfields"),
	    SNACKBARS ("Snackbars");
	    private final String name;       

	    private Tab(String s) {
	        name = s;
	    }

	    public boolean equalsName(String otherName){
	        return (otherName != null) && name.equals(otherName);
	    }

	    public String toString(){
	       return name;
	    }

	}
	
	class DrawerAdapter extends BaseAdapter{

		private Tab mSelectedTab;
		
		public void setSelected(Tab tab){
			if(tab != mSelectedTab){
				mSelectedTab = tab;
				notifyDataSetInvalidated();
			}
		}
		
		public Tab getSelectedTab(){
			return mSelectedTab;
		}
		
		@Override
		public int getCount() {
			return mItems.length;
		}

		@Override
		public Object getItem(int position) {
			return mItems[position];
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if(v == null)
				v = LayoutInflater.from(MainActivity.this).inflate(R.layout.row_drawer, null);
			
			Tab tab = (Tab)getItem(position);
			((TextView)v).setText(tab.toString());
			
			if(tab == mSelectedTab)
				v.setBackgroundColor(ThemeUtil.colorPrimary(MainActivity.this, 0));			
			else
				v.setBackgroundResource(0);
			
			return v;
		}
		
	}
	
	private static class PagerAdapter extends FragmentStatePagerAdapter {
		
		Fragment[] mFragments = new Fragment[3];
		Tab[] mTabs; 
				
		private static final Field sActiveField;
		
		static {
			Field f = null;
			try {
				Class<?> c = Class.forName("android.support.v4.app.FragmentManagerImpl");
				f = c.getDeclaredField("mActive");
				f.setAccessible(true);   
			} catch (Exception e) {}
			
			sActiveField = f;
		}
		
        public PagerAdapter(FragmentManager fm, Tab[] tabs) {
            super(fm);    
            mTabs = tabs;
            mFragments = new Fragment[mTabs.length];
       
            
            //dirty way to get reference of cached fragment
            try{
    			ArrayList<Fragment> mActive = (ArrayList<Fragment>)sActiveField.get(fm);
    			if(mActive != null){
    				for(Fragment fragment : mActive){
    					if(fragment instanceof ProgressFragment)
    						setFragment(Tab.PROGRESS, fragment);
    					else if(fragment instanceof ButtonFragment)
    						setFragment(Tab.BUTTONS, fragment);
    					else if(fragment instanceof SwitchesFragment)
    						setFragment(Tab.SWITCHES, fragment);
    					else if(fragment instanceof TextfieldFragment)
    						setFragment(Tab.TEXTFIELDS, fragment);
    					else if(fragment instanceof SnackbarFragment)
    						setFragment(Tab.SNACKBARS, fragment);
    				}
    			}
    		}
    		catch(Exception e){}
        }
        
        private void setFragment(Tab tab, Fragment f){
        	for(int i = 0; i < mTabs.length; i++)
        		if(mTabs[i] == tab){
        			mFragments[i] = f;
        			break;
        		}
        }
        
		@Override
        public Fragment getItem(int position) {
			if(mFragments[position] == null){
	        	switch (mTabs[position]) {
					case PROGRESS:
						mFragments[position] = ProgressFragment.newInstance();
						break;
					case BUTTONS:
						mFragments[position] = ButtonFragment.newInstance();
						break;
					case SWITCHES:
						mFragments[position] = SwitchesFragment.newInstance();
						break;
					case TEXTFIELDS:
						mFragments[position] = TextfieldFragment.newInstance();
						break;
					case SNACKBARS:
						mFragments[position] = SnackbarFragment.newInstance();
						break;
				}
			}
						
			return mFragments[position];		
        }
				
		@Override
		public CharSequence getPageTitle(int position) {
			return mTabs[position].toString().toUpperCase();
		}

		@Override
        public int getCount() {
            return mFragments.length;
        }
    }
}
