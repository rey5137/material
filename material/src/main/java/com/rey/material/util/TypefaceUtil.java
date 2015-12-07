package com.rey.material.util;

import android.content.Context;
import android.graphics.Typeface;

import java.util.HashMap;

/**
 * Created by Rey on 12/23/2014.
 */
public class TypefaceUtil {

    private static final HashMap<String, Typeface> sCachedFonts = new HashMap<String, Typeface>();
    private static final String PREFIX_ASSET = "asset:";

    private TypefaceUtil() {
    }

    /**
     * @param familyName if start with 'asset:' prefix, then load font from asset folder.
     * @return
     */
    public static Typeface load(Context context, String familyName, int style) {
        if(familyName != null && familyName.startsWith(PREFIX_ASSET))
            synchronized (sCachedFonts) {
                try {
                    if (!sCachedFonts.containsKey(familyName)) {
                        final Typeface typeface = Typeface.createFromAsset(context.getAssets(), familyName.substring(PREFIX_ASSET.length()));
                        sCachedFonts.put(familyName, typeface);
                        return typeface;
                    }
                } catch (Exception e) {
                    return Typeface.DEFAULT;
                }

                return sCachedFonts.get(familyName);
            }

        return Typeface.create(familyName, style);
    }
}
