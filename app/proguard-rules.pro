# Keep native symbols for crash analysis; strip in CI release signing if needed.
-keep class com.touchpad.app.audio.NativeAudioEngine { *; }
-keepclassmembers class com.touchpad.app.audio.NativeAudioEngine {
    native <methods>;
}
