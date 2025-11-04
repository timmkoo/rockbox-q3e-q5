# 🎵 Rockbox Android - Virtual iPod Clickwheel Edition

Transform your Android device into an iPod Classic-style music player with a fully functional virtual clickwheel!

## 🎯 What This Is

A fork of Rockbox Android configured for **portrait mode** with:
- **4:3 Rockbox display** at the top (non-touch, like a real iPod)
- **Virtual iPod clickwheel** at the bottom (5 buttons + circular scrolling)
- **Toggle button** to switch between clickwheel and standard touch modes
- **Optimized for 480x800 displays** (Innioasis Y1 and similar devices)
- **Compatible with Android 4.4 through Android 12+**

## ✨ Features

### Virtual Clickwheel Controls
- **TOP Button (MENU/BACK):** Navigate back, open menus
- **CENTER Button (SELECT):** Select items, play/pause
- **LEFT Button (PREV):** Previous track, navigate left
- **RIGHT Button (NEXT):** Next track, navigate right
- **BOTTOM Button (PLAY):** Play/pause control
- **Circular Scroll:** Counter-clockwise = scroll up, Clockwise = scroll down

### Dual Mode Operation
- **Clickwheel Mode** (default): Non-touch display + virtual wheel
- **Standard Touch Mode**: Full touchscreen on display, clickwheel hidden
- **Toggle:** Tap ⚙ button in top-right corner
- **Persistent:** Mode saves across app restarts

### Display
- **4:3 Aspect Ratio:** Maintains proper proportions
- **Full Width Scaling:** Minimal borders, maximum screen usage
- **240p Support:** iPod Classic/Video themes (320x240)
- **360p Support:** Native Y1 resolution themes (480x360)

## 📱 Compatibility

### Android Versions
- ✅ Android 4.4 KitKat (API 19) - Original Y1 target
- ✅ Android 5-9 (API 21-28) - Full support
- ✅ Android 10 (API 29) - Target SDK
- ✅ Android 11 (API 30) - Tested
- ✅ Android 12 (API 31) - Compatible

### Device Requirements
- **Architecture:** ARM (32-bit armeabi) - works on 64-bit devices
- **Display:** Any resolution, optimized for 480x800 portrait
- **Root:** Optional (enhanced features with root access)
- **Storage:** ~10MB for app + space for music library

## 📥 Installation

### Quick Install (Non-Root)

```bash
# Connect device via USB with USB debugging enabled
adb install -r rockbox-clickwheel.apk
```

### System Install (Root Required - Permanent)

```bash
# Follow install.md instructions
adb remount
adb push rockbox-clickwheel.apk /system/app/org.rockbox.apk
adb push libs/armeabi/* /data/data/org.rockbox/lib/
# etc...
```

### Files Needed
- `rockbox-clickwheel.apk` - Main application
- `libs/armeabi/*` - Native codec libraries (42 files)
- `.rockbox/` folder - Themes and configuration (on SD card)

## 🎨 Themes

### 240p Themes (320x240)
Compatible with iPod Classic and iPod Video themes:
- Download from: https://themes.rockbox.org/index.php?target=ipod6g
- Extract to device `.rockbox/` folder

### 360p Themes (480x360)
Native Y1 resolution themes:
- Theme pack: https://github.com/rockbox-y1/themes/releases/latest
- Extract to device `.rockbox/` folder

## 🛠️ Building from Source

### Prerequisites
- Android SDK with API 30
- Android NDK r16b
- Java 8 or newer
- Pre-built native libraries (included in `libs.tar.gz`)

### Build Commands

```bash
# Extract libraries
tar -xzf libs.tar.gz

# Set up build directory
mkdir build-manual
cd build-manual

# Build APK (automated steps in build scripts)
# See BUILD_INSTRUCTIONS.md for details
```

## 🧪 Testing

### On Device
1. Install APK
2. Launch Rockbox
3. Should see portrait layout with clickwheel
4. Test scrolling: Counter-clockwise = up, Clockwise = down
5. Test all 5 button zones
6. Test mode toggle (⚙ button)

### Logging
```bash
adb logcat | grep -i "rockbox\|clickwheel"
```

## 📖 Documentation

- `android/CLICKWHEEL_IMPLEMENTATION.md` - Technical details
- `ANDROID_11_UPDATE.md` - Modern Android compatibility
- `BUILD_INSTRUCTIONS.md` - Complete build guide
- `TESTING_GUIDE.md` - Testing procedures
- `install.md` - Original Y1 installation guide
- `STATUS.md` - Development status
- `FINAL_STATUS.md` - Current state summary

## 🔧 Technical Details

### Architecture
- **Portrait Layout:** RelativeLayout with AspectRatioFrameLayout
- **Touch Handling:** Custom ClickwheelView extends View
- **Button Mapping:** DPAD and MEDIA keycodes to Rockbox buttons
- **Native Integration:** JNI calls to Rockbox core

### Key Classes
- `ClickwheelView` - Touch gesture detection and rendering
- `AspectRatioFrameLayout` - 4:3 display container
- `RockboxFramebuffer` - Display rendering with mode control
- `RockboxActivity` - Activity lifecycle and mode management

### Scroll Detection
- **Angle threshold:** 5° minimum for scroll event
- **Velocity tracking:** Native button handler adapts vibration
- **Gesture recognition:** Distinguishes taps from scrolls
- **Visual feedback:** Button highlights and scroll indicators

## 🐛 Known Issues

1. **Emulator Support:** ARM emulators very slow on Apple Silicon Macs
2. **64-bit Libraries:** Only 32-bit ARM (works in compat mode)
3. **First Launch:** May need to configure storage permissions on Android 11+

## 🚀 What's Next

### Completed ✅
- Virtual clickwheel interface
- Portrait layout
- Android 10/11/12 support
- Mode toggle
- APK built and ready

### Potential Enhancements
- [ ] Native 64-bit libraries (arm64-v8a)
- [ ] Customizable scroll sensitivity
- [ ] Alternative visual themes for clickwheel
- [ ] Landscape mode support (optional)
- [ ] Tablet optimization

## 📷 Screenshots

*Screenshots will be added after device testing*

## 🤝 Credits

- **Rockbox Project:** Original open-source firmware
- **Rockbox Y1 Fork:** Innioasis Y1 optimizations
- **Clickwheel Implementation:** This fork (team-slide)

## 📄 License

GPL v2+ (same as Rockbox)

## 🔗 Links

- **GitHub:** https://github.com/team-slide/rockbox-q3e-q5
- **Branch:** y1
- **Rockbox Official:** https://www.rockbox.org/
- **Y1 Themes:** https://github.com/rockbox-y1/themes

---

## 🎉 Ready to Use!

The APK `rockbox-clickwheel.apk` is ready to install on your device.

Just connect via ADB and run:
```bash
adb install -r rockbox-clickwheel.apk
```

Enjoy your virtual iPod Classic experience! 🎵

