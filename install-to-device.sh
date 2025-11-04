#!/bin/bash
# Quick install script for Rockbox Android with clickwheel interface

set -e

echo "=== Rockbox Android Installer ==="
echo ""

# Check for device
if ! adb devices | grep -q "device$"; then
    echo "❌ No device connected!"
    echo "   Connect device and enable USB debugging"
    echo "   Run 'adb devices' to check connection"
    exit 1
fi

echo "✅ Device connected"
echo ""

# Ask which version to install
echo "Which version would you like to install?"
echo "  1) 240p (320x240) - Compatible with iPod Classic themes"
echo "  2) 360p (480x360) - Native resolution for Y1"
echo ""
read -p "Enter choice (1 or 2): " choice

case $choice in
    1)
        APK_PATH="build-240p/rockbox.apk"
        VERSION="240p"
        ;;
    2)
        APK_PATH="build-360p/rockbox.apk"
        VERSION="360p"
        ;;
    *)
        echo "Invalid choice"
        exit 1
        ;;
esac

# Check if APK exists
if [ ! -f "$APK_PATH" ]; then
    echo "❌ APK not found: $APK_PATH"
    echo "   Build it first with:"
    echo "   cd $(dirname $APK_PATH) && make apk"
    exit 1
fi

echo "📦 Installing $VERSION version..."
echo ""

# Ask for install method
echo "Installation method:"
echo "  1) Quick install (user app - faster, for testing)"
echo "  2) System install (permanent - recommended)"
echo ""
read -p "Enter choice (1 or 2): " method

case $method in
    1)
        echo "Installing as user app..."
        adb install -r "$APK_PATH"
        echo "✅ Installed successfully"
        echo ""
        echo "Launch with:"
        echo "  adb shell monkey -p org.rockbox -c android.intent.category.LAUNCHER 1"
        ;;
    2)
        echo "Installing as system app..."
        echo "⚠️  This requires root access"
        echo ""
        
        # Remount system as read-write
        echo "Remounting /system..."
        adb remount || {
            echo "❌ Failed to remount /system"
            echo "   Device must be rooted"
            exit 1
        }
        
        # Install APK
        echo "Pushing APK..."
        adb push "$APK_PATH" /system/app/org.rockbox.apk
        adb shell chmod 644 /system/app/org.rockbox.apk
        adb shell chown root:root /system/app/org.rockbox.apk
        
        # Check if libraries need to be installed
        if ! adb shell "[ -f /system/lib/librockbox.so ]" 2>/dev/null; then
            echo ""
            echo "Installing native libraries (first time setup)..."
            
            if [ ! -f "libs/armeabi/librockbox.so" ]; then
                echo "❌ Native libraries not found"
                echo "   Extract with: tar -xzf libs.tar.gz"
                exit 1
            fi
            
            echo "Pushing librockbox.so..."
            adb push libs/armeabi/librockbox.so /system/lib/
            adb shell chmod 644 /system/lib/librockbox.so
            adb shell chown root:root /system/lib/librockbox.so
            
            echo "Pushing codec libraries..."
            adb shell rm -rf /data/data/org.rockbox/lib
            adb shell mkdir -p /data/data/org.rockbox/lib
            adb push libs/armeabi/* /data/data/org.rockbox/lib/.
            
            echo "✅ Libraries installed"
        else
            echo "✅ Native libraries already present"
        fi
        
        echo ""
        echo "✅ System installation complete"
        echo ""
        read -p "Reboot device now? (y/n): " reboot
        if [ "$reboot" = "y" ] || [ "$reboot" = "Y" ]; then
            echo "Rebooting..."
            adb reboot
            echo "✅ Device rebooting"
            echo ""
            echo "After reboot, select Rockbox as default launcher"
        else
            echo "Reboot later with: adb reboot"
        fi
        ;;
esac

echo ""
echo "=== Installation Complete ==="
echo ""
echo "Testing the clickwheel interface:"
echo "  1. Launch Rockbox"
echo "  2. See virtual clickwheel at bottom of screen"
echo "  3. Try circular scrolling gestures"
echo "  4. Tap button zones: TOP (menu), CENTER (select), LEFT/RIGHT, BOTTOM (play)"
echo "  5. Tap ⚙ button (top-right) to toggle between clickwheel and standard touch"
echo ""
echo "To restart Rockbox:"
echo "  adb shell am force-stop org.rockbox"
echo "  adb shell monkey -p org.rockbox -c android.intent.category.LAUNCHER 1"
echo ""
echo "To check logs:"
echo "  adb logcat | grep -i rockbox"

