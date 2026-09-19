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
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# ============ 讯飞 MSC SDK ============
-keep class com.iflytek.** { *; }
-dontwarn com.iflytek.**

# ============ xlog 日志库 ============
#-keep class com.elvishew.xlog.** { *; }
#-dontwarn com.elvishew.xlog.**

# ============ Gson ============
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# 保留数据模型类（Gson 反射需要）
-keep class com.hjst.gather.model.** { *; }

# 保留 Fragment 无参构造函数（MainActivity 通过 Class.forName 反射创建需要）
-keep class com.hjst.gather.fragments.** { <init>(); }