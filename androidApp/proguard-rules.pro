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

# --- Ktor (ağ istemcisi) ---
# Ktor, çağrı zinciri boyunca jenerik/reflection tabanlı yapı kullanır;
# agresif optimizasyon ve isim değişimi bazı iç sınıfları kırabilir.
-keep class io.ktor.** { *; }
-keepclassmembers class io.ktor.** { volatile <fields>; }
-dontwarn io.ktor.**
-dontwarn kotlinx.coroutines.**

# OkHttp/Okio (Ktor'un Android motoru) — her iki kütüphanenin de
# kendi öndeğer kuralları bazı R8 sürümlerinde eksik uyarı üretir.
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn org.codehaus.mojo.animal_sniffer.*

# --- kotlinx.serialization ---
# @Serializable sınıfların derleyicinin ürettiği $serializer companion'ı
# korunmazsa, minify sonrası JSON (de)serileştirme ClassNotFoundException /
# NoSuchMethodError ile çöker (backend ile konuşan TÜM istek/yanıt modelleri).
-keepattributes *Annotation*, InnerClasses, EnclosingMethod, Signature
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.kuzeykapisi.app.**$$serializer { *; }
-keepclassmembers class com.kuzeykapisi.app.** {
    *** Companion;
}
-keepclasseswithmembers class com.kuzeykapisi.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# --- Coil (görsel yükleme) ---
# Bu sürümde SVG/GIF/video eklentileri kullanılmıyor; yalnızca olası
# reflection tabanlı bileşen keşfi için eksik uyarılar bastırılır.
-dontwarn coil3.**