# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Aturan umum untuk menjaga informasi debugging
-keepattributes SourceFile,LineNumberTable
-keepattributes *Annotation*

# ===================================================================
# ATURAN UNTUK KOIN (Dependency Injection)
# ===================================================================
-keep class org.koin.** { *; }
-keep class com.pillbox.laporbox.presentation.di.** { *; }
-keep class com.pillbox.laporbox.MyApplication { *; }
-keep class org.koin.androidx.workmanager.dsl.** { *; }

# ===================================================================
# ATURAN UNTUK RETROFIT, GSON, & OKHTTP (Jaringan)
# ===================================================================
-dontwarn retrofit2.Platform
-keep class retrofit2.** { *; }
-keep interface retrofit2.** { *; }
-keep class com.google.gson.** { *; }
-keepattributes Signature
-keepattributes InnerClasses
-dontwarn okhttp3.**
-dontwarn okio.**

# ===================================================================
# ATURAN SPESIFIK UNTUK APLIKASI ANDA
# ===================================================================

# Jaga semua Repository (interface dan implementasi)
-keep class com.pillbox.laporbox.domain.repository.** { *; }
-keep class com.pillbox.laporbox.data.repository.** { *; }

# Jaga semua model data (domain, remote, local)
-keep class com.pillbox.laporbox.domain.models.** { *; }
-keepclassmembers class com.pillbox.laporbox.domain.models.** { *; }
-keep class com.pillbox.laporbox.data.remote.** { *; }
-keepclassmembers class com.pillbox.laporbox.data.remote.** { *; }
-keep class com.pillbox.laporbox.data.local.** { *; }
-keepclassmembers class com.pillbox.laporbox.data.local.** { *; }

# Jaga semua UseCase
-keep class com.pillbox.laporbox.domain.usecase.** { *; }

# Jaga semua ViewModel
-keep class com.pillbox.laporbox.presentation.ui.screens.**.*ViewModel { *; }
-keep class com.pillbox.laporbox.MainViewModel { *; }