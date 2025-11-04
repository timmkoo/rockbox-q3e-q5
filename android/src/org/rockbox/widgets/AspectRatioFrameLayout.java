/***************************************************************************
 *             __________               __   ___.
 *   Open      \______   \ ____   ____ |  | _\_ |__   _______  ___
 *   Source     |       _//  _ \_/ ___\|  |/ /| __ \ /  _ \  \/  /
 *   Jukebox    |    |   (  <_> )  \___|    < | \_\ (  <_> > <  <
 *   Firmware   |____|_  /\____/ \___  >__|_ \|___  /\____/__/\_ \
 *                     \/            \/     \/    \/            \/
 *
 * Copyright (C) 2024 
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This software is distributed on an "AS IS" basis, WITHOUT WARRANTY OF ANY
 * KIND, either express or implied.
 *
 ****************************************************************************/

package org.rockbox.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * FrameLayout that enforces a 4:3 aspect ratio (width:height)
 * Used to contain the Rockbox display at the correct aspect ratio
 */
public class AspectRatioFrameLayout extends FrameLayout {
    private static final float ASPECT_RATIO = 4.0f / 3.0f;

    public AspectRatioFrameLayout(Context context) {
        super(context);
    }

    public AspectRatioFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AspectRatioFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Get the width (fill parent width)
        int width = MeasureSpec.getSize(widthMeasureSpec);
        
        // Calculate height to maintain 4:3 aspect ratio
        int height = (int) (width / ASPECT_RATIO);
        
        // Set exact measurements
        int exactWidthSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
        int exactHeightSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
        
        super.onMeasure(exactWidthSpec, exactHeightSpec);
    }
}

