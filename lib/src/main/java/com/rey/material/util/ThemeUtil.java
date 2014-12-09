package com.rey.material.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources.Theme;
import android.os.Build;
import android.util.TypedValue;

import com.rey.material.R;

public class ThemeUtil {
		
	private static TypedValue value;
	
	public static int dpToPx(Context context, int dp){
		return (int)(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics()) + 0.5f);
	}
	
	public static int spToPx(Context context, int sp){
		return (int)(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.getResources().getDisplayMetrics()) + 0.5f);
	}
	
	private static int getColor(Context context, int id, int defaultValue){
		if(value == null)
			value = new TypedValue();
		
		try{
			Theme theme = context.getTheme();		
			if(theme != null && theme.resolveAttribute(id, value, true) && value.type >= TypedValue.TYPE_FIRST_COLOR_INT && value.type <= TypedValue.TYPE_LAST_COLOR_INT)
				return value.data;
		}
		catch(Exception ex){}
		
		return defaultValue;
	}
	
	public static int windowBackground(Context context, int defaultValue){
		return getColor(context, android.R.attr.windowBackground, defaultValue);
	}
	
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public static int colorPrimary(Context context, int defaultValue){
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
			return getColor(context, android.R.attr.colorPrimary, defaultValue);
		
		return getColor(context, R.attr.colorPrimary, defaultValue);
	}
	
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public static int colorPrimaryDark(Context context, int defaultValue){
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
			return getColor(context, android.R.attr.colorPrimaryDark, defaultValue);
		
		return getColor(context, R.attr.colorPrimaryDark, defaultValue);
	}
	
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public static int colorAccent(Context context, int defaultValue){
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
			return getColor(context, android.R.attr.colorAccent, defaultValue);
		
		return getColor(context, R.attr.colorAccent, defaultValue);
	}
	
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public static int colorControlNormal(Context context, int defaultValue){
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
			return getColor(context, android.R.attr.colorControlNormal, defaultValue);
		
		return getColor(context, R.attr.colorControlNormal, defaultValue);
	}
	
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public static int colorControlActivated(Context context, int defaultValue){
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
			return getColor(context, android.R.attr.colorControlActivated, defaultValue);
		
		return getColor(context, R.attr.colorControlActivated, defaultValue);
	}
	
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public static int colorControlHighlight(Context context, int defaultValue){
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
			return getColor(context, android.R.attr.colorControlHighlight, defaultValue);
		
		return getColor(context, R.attr.colorControlHighlight, defaultValue);
	}
	
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public static int colorButtonNormal(Context context, int defaultValue){
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
			return getColor(context, android.R.attr.colorButtonNormal, defaultValue);
		
		return getColor(context, R.attr.colorButtonNormal, defaultValue);
	}
	
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public static int colorSwitchThumbNormal(Context context, int defaultValue){		
		return getColor(context, R.attr.colorSwitchThumbNormal, defaultValue);
	}
	
}
