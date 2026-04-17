# Preserve stack trace file/line info; rename the source file attribute.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep annotation info used by Retrofit, Hilt, Room, and kotlinx.serialization.
-keepattributes *Annotation*,Signature,Exceptions,InnerClasses,EnclosingMethod
-keepattributes RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations,AnnotationDefault

# ---------- kotlinx.serialization ----------
# Keep every generated $$serializer plus the companion `serializer()` entry points
# inside this app's packages. Library serializers ship their own consumer rules.
-keep,includedescriptorclasses class com.rakibjoy.problembuddy.**$$serializer { *; }
-keepclassmembers class com.rakibjoy.problembuddy.** {
    *** Companion;
}
-keepclasseswithmembers class com.rakibjoy.problembuddy.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep @Serializable DTOs and cache models intact (field names drive JSON keys).
-keep @kotlinx.serialization.Serializable class com.rakibjoy.problembuddy.core.network.dto.** { *; }
-keep @kotlinx.serialization.Serializable class com.rakibjoy.problembuddy.data.cache.** { *; }

# ---------- Retrofit (R8 full mode) ----------
# Retrofit builds proxies at runtime; keep service interfaces and response generics.
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface <1>
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# ---------- OkHttp / Okio ----------
-dontwarn okhttp3.internal.platform.**
-dontwarn org.bouncycastle.**
-dontwarn org.conscrypt.**
-dontwarn org.openjsse.**

# ---------- Coroutines ----------
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-dontwarn kotlinx.coroutines.debug.**

# ---------- WorkManager / Hilt Worker ----------
# Hilt auto-generates @AssistedInject factories; keep the worker so it's reachable by name.
-keep class com.rakibjoy.problembuddy.core.work.IngestWorker { *; }
