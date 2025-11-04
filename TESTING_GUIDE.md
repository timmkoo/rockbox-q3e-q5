# Testing the Virtual iPod Clickwheel

## ✅ APK Built Successfully

**Location:** `/Users/ryan/Documents/Rockbox-Timmkoo/rockbox-q3e-q5/rockbox-clickwheel-unified.apk`  
**Size:** 4.5 MB  
**Status:** Ready to install

## 📱 Installation Methods

### Method 1: Physical Device (Recommended)

Once your device shows as "device" in `adb devices`:

```bash
cd /Users/ryan/Documents/Rockbox-Timmkoo/rockbox-q3e-q5

# Quick install for testing
adb install -r rockbox-clickwheel-unified.apk

# OR system install (permanent)
./quick-test.sh
```

### Method 2: Share APK for External Testing

The APK can be shared for testing on any Android device:

```bash
# Copy to Downloads or share via cloud
cp rockbox-clickwheel-unified.apk ~/Downloads/
```

Anyone can install it with: Settings → Security → Install from Unknown Sources → Install APK

### Method 3: Emulator (Advanced)

For x86_64 Mac or Linux, create an emulator:

```bash
# Install system image
sdkmanager "system-images;android-30;google_apis;x86_64"

# Create AVD
avdmanager create avd -n rockbox_test -k "system-images;android-30;google_apis;x86_64"

# Run emulator
emulator -avd rockbox_test

# Install APK
adb install -r rockbox-clickwheel-unified.apk
```

**Note:** ARM-based Macs (M1/M2/M3) may have issues running Android emulators.

## 🎮 What to Test

### Portrait Mode Check
- [  ] App launches in portrait orientation (not landscape)
- [  ] Display area at top is 4:3 aspect ratio
- [  ] Clickwheel visible at bottom of screen
- [  ] ⚙ toggle button visible in top-right corner

### Clickwheel Functionality
- [  ] **TOP button** (MENU/BACK) - labeled "MENU"
- [  ] **CENTER button** (SELECT) - labeled "SELECT"  
- [  ] **LEFT button** (PREV) - labeled "|◀"
- [  ] **RIGHT button** (NEXT) - labeled "▶|"
- [  ] **BOTTOM button** (PLAY/PAUSE) - labeled "▶||"

### Circular Scrolling
- [  ] **Counter-clockwise** gesture scrolls UP in lists (sends DPAD_UP)
- [  ] **Clockwise** gesture scrolls DOWN in lists (sends DPAD_DOWN)
- [  ] Scroll velocity affects how fast it scrolls
- [  ] Visual feedback when scrolling is active

### Mode Toggle
- [  ] Tap ⚙ button switches to standard touch mode
- [  ] Clickwheel disappears in standard mode
- [  ] Touchscreen on display works in standard mode
- [  ] Tap ⚙ again to return to clickwheel mode
- [  ] Mode persists after app restart

### Display Rendering
- [  ] 4:3 display area scales to full width
- [  ] No distortion (maintains aspect ratio)
- [  ] Display is non-touch in clickwheel mode
- [  ] Display accepts touch in standard mode

### Button Press Types
- [  ] Short tap registers button press
- [  ] Long press (hold) detected correctly
- [  ] Button repeat works for directional buttons
- [  ] Release events processed correctly

## 📊 Implementation Details

### Java Classes Created
```
android/src/org/rockbox/widgets/
├── AspectRatioFrameLayout.java  (60 lines)
└── ClickwheelView.java           (470 lines)
```

### Modified Files
```
android/AndroidManifest.xml           - Portrait orientation
android/res/layout/main.xml           - New portrait layout
android/src/org/rockbox/
├── RockboxActivity.java              - Mode management
└── RockboxFramebuffer.java           - Touch disable in clickwheel mode
```

### Key Features Implemented
- **Touch Gesture Detection**: Distinguishes taps from circular scrolls
- **Button Zone Detection**: 5 zones with 70° sectors each
- **Scroll Direction**: Counter-clockwise=UP, Clockwise=DOWN
- **Visual Feedback**: Button highlights and scroll indicators
- **Mode Persistence**: SharedPreferences saves user preference
- **Non-Touch Display**: Authentic iPod experience in clickwheel mode

### Technical Specs
- **Scroll Threshold**: 5° minimum angle change
- **Scroll Timeout**: 150ms between events
- **Center Button Radius**: 35% of wheel radius
- **Scroll Ring**: 55%-95% of wheel radius
- **Button Sectors**: 70° each at 0°, 90°, 180°, 270°

## 🐛 Known Limitations

1. **Emulator Support**: May not work well on ARM Macs (M1/M2/M3) due to architecture mismatch
2. **Touch Calibration**: Sensitive to device screen size variations
3. **First Launch**: May need theme selection if no .rockbox folder exists

## 📝 Testing Logs

To view logs during testing:
```bash
adb logcat | grep -i "rockbox\|clickwheel"
```

Look for these log tags:
- `RockboxFramebuffer`: Touch mode changes
- `RockboxButton`: Button events
- `ClickwheelView`: Touch gestures (if debug logging added)

## 🎯 Success Criteria

✅ **Minimum Viable Test:**
- App launches in portrait with clickwheel visible
- At least one button responds to touch
- Circular scrolling gesture is detected

✅ **Full Feature Test:**
- All 5 buttons work correctly
- Scroll direction is correct (CCW=up, CW=down)
- Mode toggle switches between clickwheel and touch
- Mode persists across restarts

## 📷 Expected Visual Layout

```
┌─────────────────────┐
│                     │
│   Rockbox Display   │ ← 4:3 ratio, non-touch
│   (Menu/WPS/etc)    │    in clickwheel mode
│                     │
├─────────────────────┤
│                     │
│    ╭─────────╮      │
│   ╱   MENU   ╲      │
│  │             │     │
│ |◀   SELECT   ▶|    │ ← Virtual clickwheel
│  │             │     │    with 5 zones
│   ╲   ▶||    ╱      │
│    ╰─────────╯      │
│                     │
│        ⚙            │ ← Toggle button
└─────────────────────┘
```

## 🚀 Next Steps After Testing

1. **Report Issues**: Document any bugs or unexpected behavior
2. **Test Themes**: Try different Rockbox themes (240p vs 360p)
3. **Adjust Sensitivity**: Tweak scroll thresholds if needed
4. **Add Haptics**: Consider vibration feedback for better UX
5. **Test Edge Cases**: Very fast scrolls, rapid button presses, etc.

## 📦 APK Information

**Package:** org.rockbox  
**Version Code:** 1  
**Version Name:** Based on Rockbox Y1 fork  
**Min SDK:** 5 (Android 2.0)  
**Target SDK:** 19 (Android 4.4)  
**Architecture:** armeabi-v7a  
**Permissions:** WRITE_EXTERNAL_STORAGE, VIBRATE, WAKE_LOCK, etc.

The APK is signed with debug certificate and ready for installation!

