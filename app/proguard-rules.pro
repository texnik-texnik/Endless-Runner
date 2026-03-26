# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /sdk/tools/proguard/proguard-android.txt

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Koin
-keep class org.koin.** { *; }
-keep class * extends org.koin.core.module.Module { *; }

# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep data classes for serialization
-keep class com.endlessrunner.config.** { *; }

# Game specific rules
-keep class com.endlessrunner.entities.** { *; }
-keep class com.endlessrunner.systems.** { *; }

# Keep native methods if any
-keepclasseswithmembernames class * {
    native <methods>;
}
