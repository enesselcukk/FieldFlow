# FieldFlow app — R8 / ProGuard (release)

# Readable crash traces (Play Console / Firebase)
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# --- Kotlin ---
-dontwarn kotlin.reflect.jvm.internal.**

# --- kotlinx.serialization (runtime + app NavKey routes) ---
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keepclassmembers class com.example.fieldflow.navigation.** {
    *** Companion;
}
-keep,includedescriptorclasses class com.example.fieldflow.navigation.**$$serializer {
    *;
}

# --- Hilt / Dagger generated entry points ---
-keep class dagger.hilt.internal.aggregatedroot.** { *; }
-keep class * extends dagger.hilt.internal.GeneratedComponent
-keep class **_HiltComponents { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper

# --- WorkManager (+ @HiltWorker) ---
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.ListenableWorker
-keep class androidx.work.WorkerFactory

# --- Play Services (location) ---
-dontwarn com.google.android.gms.**
-keep class com.google.android.gms.location.** { *; }

# --- OSMDroid ---
-keep class org.osmdroid.** { *; }
-dontwarn org.osmdroid.**

# ML Kit, CameraX, Room, SQLCipher: :presentation + :data consumer-rules.pro

# --- Misc warnings from transitive libs ---
-dontwarn javax.annotation.**
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn edu.umd.cs.findbugs.annotations.SuppressFBWarnings
