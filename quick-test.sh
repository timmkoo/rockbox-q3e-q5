#!/bin/bash
# Quick test and install script for clickwheel APK

set -e

echo "🎮 Rockbox Clickwheel APK - Quick Install & Test"
echo "=================================================="
echo ""

# Check device connection
echo "Checking for device..."
if ! adb devices | grep -q "device$"; then
    echo "❌ No device connected!"
    echo ""
    echo "Please:"
    echo "  1. Connect your rooted device via USB"
    echo "  2. Enable USB debugging"
    echo "  3. Accept the USB debugging prompt"
    echo "  4. Run this script again"
    echo ""
    echo "Then run: adb devices"
    exit 1
fi

DEVICE=$(adb devices | grep "device$" | awk '{print $1}')
echo "✅ Device connected: $DEVICE"
echo ""

# Uninstall old version if exists
echo "Removing old Rockbox version (if any)..."
adb uninstall org.rockbox 2>/dev/null || echo "  No previous version found"
echo ""

# Install as system app
echo "Installing Rockbox with clickwheel interface..."
echo "  This requires root access (you mentioned device has permissive Magisk)"
echo ""

adb remount 2>&1 | grep -v "remount succeeded" || true

echo "📦 Pushing APK to /system/app..."
adb push rockbox-clickwheel-unified.apk /system/app/org.rockbox.apk
adb shell chmod 644 /system/app/org.rockbox.apk
adb shell chown root:root /system/app/org.rockbox.apk
echo "✅ APK installed"
echo ""

# Check if libs need to be installed
if ! adb shell "[ -f /system/lib/librockbox.so ]" 2>/dev/null; then
    echo "📚 Installing native libraries (first time)..."
    adb push libs/armeabi/librockbox.so /system/lib/
    adb shell chmod 644 /system/lib/librockbox.so
    adb shell chown root:root /system/lib/librockbox.so
    
    adb shell rm -rf /data/data/org.rockbox/lib 2>/dev/null || true
    adb shell mkdir -p /data/data/org.rockbox/lib
    adb push libs/armeabi/* /data/data/org.rockbox/lib/. 2>&1 | grep -v "bytes in"
    echo "✅ Libraries installed"
else
    echo "✅ Native libraries already present"
fi
echo ""

echo "🔄 Rebooting device..."
adb reboot
echo ""
echo "⏳ Device is rebooting... This will take about 30 seconds"
echo ""
echo "After reboot:"
echo "  1. Select Rockbox as default launcher (if prompted)"
echo "  2. App should launch in PORTRAIT mode"
echo "  3. You'll see:"
echo "     - 4:3 Rockbox display at top"
echo "     - Virtual iPod clickwheel at bottom"
echo "     - ⚙ toggle button in top-right corner"
echo ""
echo "Testing checklist:"
echo "  ✓ Touch the virtual clickwheel"
echo "  ✓ Try circular scrolling (counter-clockwise = up, clockwise = down)"
echo "  ✓ Test button zones:"
echo "     - TOP: MENU/BACK"
echo "     - CENTER: SELECT"
echo "     - LEFT: PREV (previous track)"
echo "     - RIGHT: NEXT (next track)"
echo "     - BOTTOM: PLAY/PAUSE"
echo "  ✓ Tap ⚙ button to toggle between clickwheel and standard touch modes"
echo ""
echo "To check logs after reboot:"
echo "  adb logcat | grep -i rockbox"
echo ""
echo "To restart Rockbox:"
echo "  adb shell am force-stop org.rockbox"
echo "  adb shell monkey -p org.rockbox -c android.intent.category.LAUNCHER 1"

