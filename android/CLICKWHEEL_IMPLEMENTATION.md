# Virtual iPod Clickwheel Implementation

## Overview

This implementation transforms the Rockbox Android app into a portrait-mode iPod Classic emulator with a fully functional virtual clickwheel interface optimized for 480x800 displays (Android 4.2-4.4).

## Features

### 1. Portrait Layout
- **Top Area**: 4:3 aspect ratio Rockbox display (480x360 on a 480x800 screen)
- **Bottom Area**: Virtual iPod clickwheel (fills remaining 440px)
- **Optimal Screen Usage**: Display scales to full width maintaining 4:3 ratio

### 2. Virtual Clickwheel
The clickwheel implements five interactive zones:

#### Button Zones
- **Top (MENU)**: Android BACK button → Rockbox BUTTON_BACK
- **Center (SELECT)**: DPAD_CENTER → Rockbox BUTTON_DPAD_CENTER  
- **Left (PREV)**: DPAD_LEFT → Rockbox BUTTON_DPAD_LEFT
- **Right (NEXT)**: DPAD_RIGHT → Rockbox BUTTON_DPAD_RIGHT
- **Bottom (PLAY)**: MEDIA_PLAY_PAUSE → Rockbox BUTTON_MULTIMEDIA_PLAYPAUSE

#### Circular Scrolling
- **Clockwise**: Sends DPAD_DOWN (scroll forward through lists)
- **Counter-Clockwise**: Sends DPAD_UP (scroll backward through lists)
- **Adaptive Vibration**: Intensity increases with scroll velocity
- **Acceleration Support**: Native button handling includes velocity tracking

### 3. Control Mode Toggle
A semi-transparent gear button (⚙) in the top-right corner switches between:
- **Clickwheel Mode** (default): Virtual wheel active, display is non-touch
- **Standard Touch Mode**: Virtual wheel hidden, display accepts touch input

The mode persists across app restarts via SharedPreferences.

## Implementation Details

### Files Created

1. **AspectRatioFrameLayout.java**
   - Custom FrameLayout enforcing 4:3 aspect ratio
   - Ensures Rockbox display maintains correct proportions on any screen

2. **ClickwheelView.java**
   - Custom View implementing iPod-style clickwheel
   - Handles touch gesture detection (taps vs. circular scrolling)
   - Draws visual representation with labels and highlights
   - Button zones: 70° sectors for each of 5 buttons
   - Scroll detection: Tracks angular movement with 5° threshold
   - Visual feedback for button presses and scrolling activity

### Files Modified

1. **main.xml**
   - Changed from simple LinearLayout to RelativeLayout
   - Added AspectRatioFrameLayout container for display
   - Added ClickwheelView for virtual wheel
   - Added toggle button overlay

2. **RockboxActivity.java**
   - Implements layout setup and view management
   - Handles control mode toggling with persistence
   - Manages view visibility based on mode
   - Coordinates between framebuffer and clickwheel

3. **RockboxFramebuffer.java**
   - Added `setClickwheelMode()` method
   - Disables touch events when in clickwheel mode
   - Works within constrained layout (not fullscreen)

4. **AndroidManifest.xml**
   - Changed `screenOrientation` from "landscape" to "portrait"

### Native Button Mapping

The implementation leverages existing Rockbox Android button infrastructure:

- **button-application.c**: Maps Android keycodes to Rockbox buttons
- **button-android.c**: Handles button events with vibration feedback
- **keymap-android.c**: Defines action mappings for UI navigation

DPAD_UP/DOWN events are already mapped to:
- `ACTION_STD_PREV/NEXT` in menus (list navigation)
- `ACTION_WPS_VOLDOWN/UP` in WPS (volume control)
- Repeat events for continuous scrolling

## Usage

### Building
The implementation requires no special build steps. The standard Rockbox Android build process will automatically compile the new Java classes.

```bash
cd rockbox-q3e-q5
./tools/configure
# Select Android target
make
make apk
```

### Operation

**In Clickwheel Mode (default):**
- Touch the clickwheel to navigate
- Circular gestures scroll through lists
- Button zones provide standard iPod controls
- Display area is non-touch (like real iPod)

**In Standard Touch Mode:**
- Toggle via ⚙ button in top-right
- Clickwheel disappears
- Full touchscreen functionality on display
- Android back button works normally

### Optimization for 480x800 Displays

The layout is specifically optimized for 480x800 portrait screens:
- Display: 480x360 (perfect for 360p native builds)
- Clickwheel: 480x440 (provides comfortable touch targets)
- 1:1 pixel mapping for 360p builds (no scaling artifacts)
- 240p builds scale 1.5x to 360p display area

## Technical Notes

### Scroll Wheel Physics
- **Threshold**: 5° minimum angle change to register scroll
- **Timeout**: 150ms between scroll events
- **Velocity Tracking**: Native code tracks press frequency for vibration
- **Debouncing**: Distinguishes between taps and scroll gestures

### Touch Zones
- **Center Button**: 35% of wheel radius
- **Scroll Ring**: Between 55% and 95% of wheel radius  
- **Button Sectors**: 70° each, positioned at 0°, 90°, 180°, 270°

### Visual Design
- Dark theme matching iPod Classic aesthetic
- Semi-transparent overlays
- Real-time visual feedback for interactions
- Unicode symbols for button labels (▶||, |◀, ▶|)

## Compatibility

- **Minimum SDK**: Android API 5 (Android 2.0)
- **Target Devices**: 480x800 portrait phones (Android 4.2-4.4)
- **Tested Resolutions**: Optimized for 480x800, adaptable to others
- **Rockbox Builds**: Works with both 240p and 360p builds

## Future Enhancements

Potential improvements for future versions:
- Scrollwheel sensitivity adjustment in settings
- Customizable button mappings
- Alternative visual themes
- Haptic feedback patterns
- Landscape mode support
- Tablet optimization

## Credits

Implementation based on Rockbox open-source project structure with inspiration from iPod Classic and Innioasis Y1 hardware interfaces.

