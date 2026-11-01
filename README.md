# KeyCursor – Android Physical Keyboard Mouse Controller

KeyCursor is a native, lightweight Android application engineered to provide system-wide mouse pointer navigation, clicking, context-menu triggers, and drag-and-drop gestures using the arrow keys and shortcuts of a physical USB or Bluetooth keyboard.

Designed specifically with high responsiveness for Android tablets like the **Lenovo Tab M10 (Android 10 / API 29, ARM64)** and compatible with Android 10 through Android 15+.

---

## 1. Features & Architecture

- **True System-Wide Pointer:** Renders an on-screen hardware-accelerated cursor overlay that stays visible above all Android applications, home launcher, system settings, and browsers.
- **Buttery-Smooth Continuous Motion:** 60fps tick-based movement engine with hold acceleration and diagonal vector normalization.
- **Real Android Touch Gestures:** Uses `AccessibilityService.dispatchGesture()` to dispatch real touch taps, long-presses, and swipe-drags.
- **Zero Battery & Low RAM Footprint:** Native Canvas vector rendering with minimal CPU overhead, tailored for low-end tablets.
- **Physical Keyboard Detection:** Automatically recognizes connected USB (via OTG) and Bluetooth keyboards.
- **Instant Normal Typing Toggle:** Press `Escape` at any time to temporarily suspend cursor interception so you can type freely into search bars, URL bars, or text documents.

---

## 2. Default Keyboard Controls

| Key Shortcut | Action | Technical Implementation |
|---|---|---|
| **Arrow Up (`↑`)** | Move cursor upward | Smooth continuous motion vector |
| **Arrow Down (`↓`)** | Move cursor downward | Smooth continuous motion vector |
| **Arrow Left (`←`)** | Move cursor left | Smooth continuous motion vector |
| **Arrow Right (`→`)** | Move cursor right | Smooth continuous motion vector |
| **Shift + Arrows** | Fast movement | 2.5x speed multiplier |
| **Enter** | Left-Click | Instant touch tap (`dispatchGesture` tap stroke) |
| **Shift + Enter** | Right-Click / Context Menu | Touch long-press (`dispatchGesture` 600ms hold) |
| **Space** | Enable / Disable Drag Mode | Records start coordinates, anchors path, and performs touch drag |
| **Escape** | Pause / Resume Cursor | Toggles between Cursor Navigation mode and Normal Text Typing mode |

---

## 3. Project Structure

```
KeyCursor/
├── app/
│   ├── src/main/
│   │   ├── AndroidManifest.xml                  # Service declaration & permissions
│   │   ├── java/com/example/
│   │   │   ├── MainActivity.kt                  # Glassmorphic M3 UI, diagnostics & sandbox
│   │   │   ├── controller/
│   │   │   │   └── CursorController.kt         # Coordinate math, screen clamping, state
│   │   │   ├── gesture/
│   │   │   │   └── GestureController.kt        # System tap, long-press, and drag gestures
│   │   │   ├── input/
│   │   │   │   └── KeyboardInputHandler.kt     # 60fps continuous tick loop & key interception
│   │   │   ├── manager/
│   │   │   │   ├── PermissionManager.kt        # Accessibility & keyboard hardware detection
│   │   │   │   └── SettingsManager.kt          # SharedPreferences persistence & StateFlow
│   │   │   ├── model/
│   │   │   │   └── CursorSettings.kt           # Speed, acceleration, theme data model
│   │   │   ├── overlay/
│   │   │   │   └── CursorOverlayView.kt        # Hardware-accelerated Canvas cursor & ripples
│   │   │   ├── service/
│   │   │   │   └── CursorAccessibilityService.kt # WindowManager overlay & event pipeline
│   │   │   └── ui/
│   │   │       ├── components/                 # GlassCard, Sandbox, Tuning sliders, Guides
│   │   │       └── theme/                      # Cyber-Chic dark luxury palette
│   │   └── res/
│   │       ├── xml/accessibility_service_config.xml # canPerformGestures & canRequestFilterKeyEvents
│   │       └── values/strings.xml, colors.xml
│   └── build.gradle.kts
└── README.md
```

---

## 4. How to Enable KeyCursor on Android 10 (Lenovo Tab M10)

1. **Install the APK** (see section below).
2. **Connect your Keyboard:**
   - **USB Keyboard:** Plug into the Lenovo Tab M10 using a USB-C or Micro-USB OTG cable.
   - **Bluetooth Keyboard:** Turn on pairing mode on the keyboard, open Android **Settings > Connected Devices > Bluetooth**, and pair the keyboard.
3. **Enable Accessibility Permission:**
   - Open KeyCursor and tap the **Enable** button on the dashboard (or go to Android **Settings > Accessibility > Downloaded Services > KeyCursor**).
   - Toggle **KeyCursor** to **ON** and accept the system prompt.
4. **Test Navigation:**
   - Use the arrow keys on your keyboard. The glowing pointer will appear on screen.
   - Test left-click on the "Live Test Sandbox" with `Enter`, long-press with `Shift + Enter`, and drag with `Space`.

---

## 5. Manual APK Compilation & Installation

### Step A: Compiling the Debug APK
Open a terminal in the root project folder:

```bash
gradle :app:assembleDebug
```

Upon completion, the compiled APK will be located at:
`app/build/outputs/apk/debug/app-debug.apk`

### Step B: Compiling the Release APK
To compile a signed release APK:

```bash
gradle :app:assembleRelease
```

The APK will be generated at:
`app/build/outputs/apk/release/app-release-unsigned.apk` (or signed if signing keys are supplied via environment variables `KEYSTORE_PATH`, `STORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`).

### Step C: Installing the APK on Lenovo Tab M10

**Method 1: Via ADB (Android Debug Bridge)**
1. Enable **Developer Options** and **USB Debugging** on your Lenovo Tab M10.
2. Connect your tablet to your computer via USB.
3. Run:
   ```bash
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```

**Method 2: Direct on Device**
1. Copy `app-debug.apk` to your tablet via Google Drive, SD card, or USB file transfer.
2. Open the **Files** app on the Lenovo Tab M10, tap the APK, and tap **Install** (allow unknown sources if prompted).

---

## 6. Automated GitHub Actions Build Workflow

KeyCursor includes a GitHub Actions CI workflow in `.github/workflows/build-apk.yml`.

### How It Works:
1. **On Every Push or Pull Request** to `main` or `master`:
   - Checks out the repository.
   - Sets up Java 21 (Temurin) and Gradle caching.
   - Decodes or initializes the keystore configuration and environment parameters.
   - Executes automated unit tests (`./gradlew testDebugUnitTest`).
   - Builds the debug APK (`./gradlew assembleDebug`).
   - Uploads `KeyCursor-debug.apk` as a downloadable GitHub Actions artifact (retained for 30 days).
2. **On Tag Push (`v*.*.*`)**:
   - Automatically publishes a GitHub Release with the compiled `KeyCursor-debug.apk` attached directly.
3. **Manual Trigger (`workflow_dispatch`)**:
   - You can trigger a build at any time by navigating to your GitHub repository -> **Actions** -> **Build KeyCursor APK** -> **Run workflow**.

---

## 7. Android Platform & Accessibility Limitations

Android enforces security boundaries around Accessibility Gestures:
1. **Secure Fields:** Android blocks touch gestures over password input boxes (`inputType="textPassword"`) and system permission dialogs for user safety.
2. **Hardware Key Focus:** Some apps like games with raw touch capture may capture raw inputs; using `Escape` toggles between KeyCursor control and native application input seamlessly.
3. **Keyboard Layout:** Physical keyboard layout is recognized using Android's standard input subsystem (`KEYBOARD_TYPE_ALPHABETIC`).
