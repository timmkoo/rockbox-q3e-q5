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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import org.rockbox.RockboxFramebuffer;

/**
 * Virtual iPod-style clickwheel view
 * Implements circular scrolling and 5 button zones:
 * - Top: BACK button
 * - Center: SELECT button  
 * - Left: LEFT button
 * - Right: RIGHT button
 * - Bottom: PLAY/PAUSE button
 */
public class ClickwheelView extends View {
    private static final String TAG = "ClickwheelView";
    
    // Touch detection thresholds
    private static final float CENTER_BUTTON_RATIO = 0.35f;  // Center button is 35% of wheel radius
    private static final float INNER_RING_RATIO = 0.55f;     // Inner edge of scrollable ring
    private static final float OUTER_RING_RATIO = 0.95f;     // Outer edge of scrollable ring
    private static final float SCROLL_THRESHOLD = 5.0f;      // Minimum angle change for scroll (degrees)
    private static final long SCROLL_TIMEOUT_MS = 150;       // Timeout for continuous scrolling
    private static final int BUTTON_REPEAT_DELAY_MS = 500;   // Delay before button repeat starts
    private static final int BUTTON_REPEAT_RATE_MS = 100;    // Rate of button repeat
    
    // Button zones (angles in degrees, 0° = right, 90° = bottom, 180° = left, 270° = top)
    private static final float BUTTON_SECTOR_SIZE = 70.0f;   // Each button covers 70° sector
    
    // Key codes for each button
    private static final int KEYCODE_TOP = KeyEvent.KEYCODE_BACK;           // BACK button
    private static final int KEYCODE_CENTER = KeyEvent.KEYCODE_DPAD_CENTER; // SELECT button
    private static final int KEYCODE_LEFT = KeyEvent.KEYCODE_DPAD_LEFT;     // LEFT button
    private static final int KEYCODE_RIGHT = KeyEvent.KEYCODE_DPAD_RIGHT;   // RIGHT button
    private static final int KEYCODE_BOTTOM = KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE; // PLAY button
    
    // For scroll events, we use DPAD_UP/DOWN as proxy for SCROLL_FWD/SCROLL_BACK
    private static final int KEYCODE_SCROLL_FWD = KeyEvent.KEYCODE_DPAD_DOWN;
    private static final int KEYCODE_SCROLL_BACK = KeyEvent.KEYCODE_DPAD_UP;
    
    // Paint objects for drawing
    private Paint wheelPaint;
    private Paint centerButtonPaint;
    private Paint buttonHighlightPaint;
    private Paint textPaint;
    private Paint scrollRingPaint;
    
    // Geometry
    private float centerX;
    private float centerY;
    private float wheelRadius;
    private float centerButtonRadius;
    private float innerRingRadius;
    private float outerRingRadius;
    
    // Touch state
    private boolean isTouching = false;
    private float lastTouchAngle = 0;
    private float accumulatedAngle = 0;
    private long lastScrollTime = 0;
    private int currentButton = -1;
    private boolean isScrolling = false;
    private boolean buttonPressed = false;
    
    // Button repeat handling
    private Runnable buttonRepeatRunnable;
    private boolean isRepeating = false;

    public ClickwheelView(Context context) {
        super(context);
        init();
    }

    public ClickwheelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ClickwheelView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // Initialize paint objects
        wheelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        wheelPaint.setColor(0xFF3A3A3A);
        wheelPaint.setStyle(Paint.Style.FILL);
        
        centerButtonPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        centerButtonPaint.setColor(0xFF2A2A2A);
        centerButtonPaint.setStyle(Paint.Style.FILL);
        
        buttonHighlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        buttonHighlightPaint.setColor(0xFF5A5A5A);
        buttonHighlightPaint.setStyle(Paint.Style.FILL);
        
        scrollRingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        scrollRingPaint.setColor(0xFF4A4A4A);
        scrollRingPaint.setStyle(Paint.Style.FILL);
        
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(0xFFCCCCCC);
        textPaint.setTextAlign(Paint.Align.CENTER);
        
        buttonRepeatRunnable = new Runnable() {
            @Override
            public void run() {
                if (buttonPressed && currentButton != -1 && currentButton != KEYCODE_CENTER) {
                    // Send repeat event
                    RockboxFramebuffer.buttonHandlerRepeat(currentButton);
                    isRepeating = true;
                    // Schedule next repeat
                    postDelayed(this, BUTTON_REPEAT_RATE_MS);
                }
            }
        };
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        
        // Calculate geometry
        centerX = w / 2.0f;
        centerY = h / 2.0f;
        wheelRadius = Math.min(w, h) / 2.0f * 0.85f; // 85% of available space
        centerButtonRadius = wheelRadius * CENTER_BUTTON_RATIO;
        innerRingRadius = wheelRadius * INNER_RING_RATIO;
        outerRingRadius = wheelRadius * OUTER_RING_RATIO;
        
        // Update text size based on wheel size
        textPaint.setTextSize(wheelRadius * 0.12f);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        if (wheelRadius <= 0) {
            return;
        }
        
        // Draw outer wheel ring
        canvas.drawCircle(centerX, centerY, outerRingRadius, wheelPaint);
        
        // Draw inner ring (scrollable area) - slightly lighter
        canvas.drawCircle(centerX, centerY, innerRingRadius, scrollRingPaint);
        
        // Draw button highlights if pressed
        if (buttonPressed && currentButton != -1 && currentButton != KEYCODE_CENTER) {
            drawButtonHighlight(canvas, currentButton);
        }
        
        // Draw center button
        canvas.drawCircle(centerX, centerY, centerButtonRadius, 
                         (buttonPressed && currentButton == KEYCODE_CENTER) ? 
                         buttonHighlightPaint : centerButtonPaint);
        
        // Draw button labels
        drawButtonLabels(canvas);
        
        // Draw scroll indicators if scrolling
        if (isScrolling) {
            drawScrollIndicator(canvas);
        }
    }

    private void drawButtonHighlight(Canvas canvas, int keycode) {
        float startAngle = getButtonStartAngle(keycode);
        Path path = new Path();
        RectF oval = new RectF(centerX - outerRingRadius, centerY - outerRingRadius,
                               centerX + outerRingRadius, centerY + outerRingRadius);
        RectF innerOval = new RectF(centerX - innerRingRadius, centerY - innerRingRadius,
                                     centerX + innerRingRadius, centerY + innerRingRadius);
        
        path.arcTo(oval, startAngle, BUTTON_SECTOR_SIZE);
        path.arcTo(innerOval, startAngle + BUTTON_SECTOR_SIZE, -BUTTON_SECTOR_SIZE);
        path.close();
        
        canvas.drawPath(path, buttonHighlightPaint);
    }

    private float getButtonStartAngle(int keycode) {
        // Convert button position to start angle for drawing
        // Top (BACK) = 270° center, so start at 270 - 35 = 235°
        // Right = 0° center, so start at -35° = 325°
        // Bottom (PLAY) = 90° center, so start at 55°
        // Left = 180° center, so start at 145°
        if (keycode == KEYCODE_TOP) return 270 - BUTTON_SECTOR_SIZE / 2;
        if (keycode == KEYCODE_RIGHT) return 360 - BUTTON_SECTOR_SIZE / 2;
        if (keycode == KEYCODE_BOTTOM) return 90 - BUTTON_SECTOR_SIZE / 2;
        if (keycode == KEYCODE_LEFT) return 180 - BUTTON_SECTOR_SIZE / 2;
        return 0;
    }

    private void drawScrollIndicator(Canvas canvas) {
        // Draw small arc indicators to show scrolling is active
        Paint indicatorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        indicatorPaint.setColor(0xFFFFFFFF);
        indicatorPaint.setStyle(Paint.Style.STROKE);
        indicatorPaint.setStrokeWidth(3);
        
        float indicatorRadius = (innerRingRadius + centerButtonRadius) / 2.0f;
        RectF oval = new RectF(centerX - indicatorRadius, centerY - indicatorRadius,
                               centerX + indicatorRadius, centerY + indicatorRadius);
        canvas.drawArc(oval, -30, 60, false, indicatorPaint);
    }

    private void drawButtonLabels(Canvas canvas) {
        float labelRadius = (innerRingRadius + outerRingRadius) / 2.0f;
        
        // Top - BACK/MENU
        canvas.drawText("MENU", centerX, centerY - labelRadius + textPaint.getTextSize() / 3, textPaint);
        
        // Bottom - PLAY
        canvas.drawText("▶||", centerX, centerY + labelRadius + textPaint.getTextSize() / 3, textPaint);
        
        // Left - PREV
        canvas.drawText("|◀", centerX - labelRadius, centerY + textPaint.getTextSize() / 3, textPaint);
        
        // Right - NEXT
        canvas.drawText("▶|", centerX + labelRadius, centerY + textPaint.getTextSize() / 3, textPaint);
        
        // Center - SELECT
        canvas.drawText("SELECT", centerX, centerY + textPaint.getTextSize() / 3, textPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                return handleTouchDown(x, y);
                
            case MotionEvent.ACTION_MOVE:
                return handleTouchMove(x, y);
                
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                return handleTouchUp();
        }
        
        return false;
    }

    private boolean handleTouchDown(float x, float y) {
        float dx = x - centerX;
        float dy = y - centerY;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        
        isTouching = true;
        lastTouchAngle = (float) Math.toDegrees(Math.atan2(dy, dx));
        if (lastTouchAngle < 0) lastTouchAngle += 360;
        
        accumulatedAngle = 0;
        isScrolling = false;
        
        // Check if touch is in center button
        if (distance <= centerButtonRadius) {
            currentButton = KEYCODE_CENTER;
            buttonPressed = true;
            RockboxFramebuffer.buttonHandler(currentButton, true);
            invalidate();
            return true;
        }
        
        // Check if touch is in scrollable ring
        if (distance >= innerRingRadius && distance <= outerRingRadius) {
            // In scrollable area - wait for movement to determine scroll vs button press
            lastScrollTime = System.currentTimeMillis();
            currentButton = getButtonAtAngle(lastTouchAngle);
            invalidate();
            return true;
        }
        
        // Touch outside wheel
        isTouching = false;
        return false;
    }

    private boolean handleTouchMove(float x, float y) {
        if (!isTouching) {
            return false;
        }
        
        float dx = x - centerX;
        float dy = y - centerY;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        float angle = (float) Math.toDegrees(Math.atan2(dy, dx));
        if (angle < 0) angle += 360;
        
        // If already scrolling or in scrollable ring, handle scroll
        if (distance >= innerRingRadius && distance <= outerRingRadius) {
            float angleDelta = angle - lastTouchAngle;
            
            // Handle angle wraparound
            if (angleDelta > 180) angleDelta -= 360;
            if (angleDelta < -180) angleDelta += 360;
            
            accumulatedAngle += angleDelta;
            lastTouchAngle = angle;
            
            // Check if we should start scrolling
            if (!isScrolling && !buttonPressed && Math.abs(accumulatedAngle) > SCROLL_THRESHOLD * 2) {
                isScrolling = true;
                currentButton = -1;
                invalidate();
            }
            
            // Send scroll events
            if (isScrolling && Math.abs(accumulatedAngle) >= SCROLL_THRESHOLD) {
                long currentTime = System.currentTimeMillis();
                
                if (currentTime - lastScrollTime < SCROLL_TIMEOUT_MS || lastScrollTime == 0) {
                    if (accumulatedAngle > 0) {
                        // Clockwise - scroll forward
                        RockboxFramebuffer.buttonHandler(KEYCODE_SCROLL_FWD, true);
                        RockboxFramebuffer.buttonHandler(KEYCODE_SCROLL_FWD, false);
                    } else {
                        // Counter-clockwise - scroll back
                        RockboxFramebuffer.buttonHandler(KEYCODE_SCROLL_BACK, true);
                        RockboxFramebuffer.buttonHandler(KEYCODE_SCROLL_BACK, false);
                    }
                    
                    accumulatedAngle = 0;
                    lastScrollTime = currentTime;
                }
            }
        }
        
        return true;
    }

    private boolean handleTouchUp() {
        if (!isTouching) {
            return false;
        }
        
        // Cancel button repeat
        removeCallbacks(buttonRepeatRunnable);
        
        // If a button was pressed (not scrolling), send button up event
        if (buttonPressed && currentButton != -1 && !isScrolling) {
            if (!isRepeating) {
                // Normal button press - already sent down, now send up
                RockboxFramebuffer.buttonHandler(currentButton, false);
            } else {
                // Was repeating - just release
                RockboxFramebuffer.buttonHandler(currentButton, false);
            }
        } else if (!isScrolling && currentButton != -1 && !buttonPressed) {
            // Quick tap on a button zone without scrolling
            buttonPressed = true;
            RockboxFramebuffer.buttonHandler(currentButton, true);
            // Small delay then release
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    RockboxFramebuffer.buttonHandler(currentButton, false);
                }
            }, 50);
        }
        
        // Reset state
        isTouching = false;
        isScrolling = false;
        buttonPressed = false;
        isRepeating = false;
        currentButton = -1;
        accumulatedAngle = 0;
        
        invalidate();
        return true;
    }

    private int getButtonAtAngle(float angle) {
        // Normalize angle to 0-360
        while (angle < 0) angle += 360;
        while (angle >= 360) angle -= 360;
        
        // Top button (BACK): 270° ± 35° = 235-305°
        if ((angle >= 270 - BUTTON_SECTOR_SIZE / 2 && angle <= 270 + BUTTON_SECTOR_SIZE / 2)) {
            return KEYCODE_TOP;
        }
        
        // Right button: 0° ± 35° = 325-35° (wraps around)
        if (angle >= 360 - BUTTON_SECTOR_SIZE / 2 || angle <= BUTTON_SECTOR_SIZE / 2) {
            return KEYCODE_RIGHT;
        }
        
        // Bottom button (PLAY): 90° ± 35° = 55-125°
        if (angle >= 90 - BUTTON_SECTOR_SIZE / 2 && angle <= 90 + BUTTON_SECTOR_SIZE / 2) {
            return KEYCODE_BOTTOM;
        }
        
        // Left button: 180° ± 35° = 145-215°
        if (angle >= 180 - BUTTON_SECTOR_SIZE / 2 && angle <= 180 + BUTTON_SECTOR_SIZE / 2) {
            return KEYCODE_LEFT;
        }
        
        // Between buttons - no button
        return -1;
    }
}

