# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified in the
# Android SDK's proguard-android-optimize.txt file.

# Retrofit — keep type tokens
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.squareup.retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# Gson — keep model/DTO classes
-keep class com.haps.android.data.remote.dto.** { *; }
-keep class com.haps.android.domain.model.** { *; }
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# OkHttp
-dontwarn okhttp3.**
-keep class okhttp3.** { *; }

# Hilt
-keep class dagger.hilt.** { *; }
-keep @dagger.hilt.android.AndroidEntryPoint class * { *; }

# Room — keep entity and DAO classes
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Compose
-keep class androidx.compose.** { *; }
