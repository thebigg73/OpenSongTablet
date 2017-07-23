# Allow obfuscation of android.support.v7.internal.view.menu.**
# to avoid problem on Samsung 4.2.2 devices with appcompat v21
# see https://code.google.com/p/android/issues/detail?id=78377
-keep class !android.support.v7.internal.view.menu.**,android.support.** {*;}
-keep class com.itextpdf.** { *; }
-dontwarn com.itextpdf.**
-keep class org.apache.commons.compress.** { *; }
-dontwarn org.apache.commons.compress.**