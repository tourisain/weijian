# Keep only runtime-sensitive entry points and metadata. Avoid broad app-wide
# keep rules so R8 can shrink, optimize and obfuscate the release build.
-keepattributes Signature,*Annotation*,InnerClasses,EnclosingMethod

-keep @androidx.annotation.Keep class * { *; }
-keepclassmembers class * {
    @androidx.annotation.Keep *;
}

# Android manifest / platform-created components.
-keep public class * extends android.app.Application
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.appwidget.AppWidgetProvider
-keep public class * extends androidx.work.ListenableWorker

# Room databases and entities.
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Database class * { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }

# Data classes serialized or reflected by Gson / Kotlin serialization.
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken { *; }
-keep class com.tourisain.weijian.data.database.entity.** { *; }
-keep class com.tourisain.weijian.data.model.** { *; }
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement

# Libraries with optional desktop/server classes.
-dontwarn java.awt.**
-dontwarn javax.imageio.**
-dontwarn org.slf4j.impl.StaticLoggerBinder
-dontwarn com.google.errorprone.annotations.**

-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
}
