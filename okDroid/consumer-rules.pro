##---------------Bas ProGuard

# 保持注解属性
-keepattributes *Annotation*

# for kotlin
-keep class kotlin.** { *; }
-keep @kotlin.Metadata class *
-keepclasseswithmembers @kotlin.Metadata class * { *; }
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    static void checkParameterIsNotNull(java.lang.Object, java.lang.String);
}

##---------------Bas ProGuard END

###### Converts ######
## 参与序列化和反序列化的类均需keep
## Java与Kotlin混合开发的工程建议使用Moshi作为序列化和反序列化工具，纯Kotlin开发的可以使用Moshi或者使用官方的kotlin serialization
## 使用反射方式序列化和反序列化的工具均需要“为反射序列化类添加保留规则”，比如Gson、Fastjson、Moshi反射方式等

####---------------Begin: proguard configuration for Moshi  ----------

## Moshi GitHub https://github.com/square/moshi
## Moshi R8/Proguard https://github.com/square/moshi#r8--proguard
## 即Moshi如果是使用的插件方式，则不需要添加混淆规则，使用反射的方式才需要“为反射序列化类添加保留规则”
## 原文：
## Moshi contains minimally required rules for its own internals to work without need for consumers to embed their own.
## However if you are using reflective serialization and R8 or ProGuard,
## you must add keep rules in your proguard configuration file for your reflectively serialized classes.

####---------------End: proguard configuration for Moshi  ----------

####---------------Begin: proguard configuration for Gson  ----------
## Gson Github https://github.com/google/gson
## Gson Proguard https://github.com/google/gson/tree/master/examples/android-proguard-example
## 关于Gson R8（需要翻墙）： https://r8.googlesource.com/r8/+/refs/heads/main/compatibility-faq.md#gson
## 以下内容来源：https://github.com/google/gson/blob/master/examples/android-proguard-example/proguard.cfg

# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*

# Gson specific classes
-dontwarn sun.misc.**
#-keep class com.google.gson.stream.** { *; }


# Prevent proguard from stripping interface information from TypeAdapter, TypeAdapterFactory,
# JsonSerializer, JsonDeserializer instances (so they can be used in @JsonAdapter)
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Prevent R8 from leaving Data object members always null
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

# Retain generic signatures of TypeToken and its subclasses with R8 version 3.0 and higher.
-keep,allowobfuscation,allowshrinking class com.google.gson.reflect.TypeToken
-keep,allowobfuscation,allowshrinking class * extends com.google.gson.reflect.TypeToken

####---------------End: proguard configuration for Gson  ----------


####---------------Begin: proguard configuration for FastJson  ----------

## 未找到官方的关于混淆的配置，待补充
## https://github.com/square/retrofit/wiki/Converters
## https://github.com/ligboy/retrofit-converter-fastjson
## https://github.com/alibaba/fastjson

## 以下为野路子方式，但是不可取，里面有效的最根本规则是“为反射序列化类保留规则”
## 野路子链接：https://blog.csdn.net/u014168208/article/details/78129391
## 野路子内容
#-dontskipnonpubliclibraryclassmembers
#-dontskipnonpubliclibraryclasses
#-keep class * implements java.io.Serializable { *; }
#-keepattributes *Annotation
#-keepattributes Signature
#-dontwarn com.alibaba.fastjson.**
#-keep class com.alibaba.fastjson.** { *; }

####---------------End: proguard configuration for FastJson  ----------

####---------------Begin: proguard configuration for Jackson & Jackson-kotlin-module  ----------

## Jackson https://github.com/FasterXML/jackson-dataformat-xml
## Jackson也未找到相关官方混淆说明，只存在野路子，不是很可信。总体而言Jackson使用了反射，因此核心应该是“为反射序列化类添加保留规则”
## Jackson Proguard 野路子：https://blog.csdn.net/u012573920/article/details/47260123

#-dontpreverify
#-dontwarn
#-dontnote
#-verbose
#-keepattributes Signature,*Annotation*,*EnclosingMethod*
#-keep class org.codehaus.** { *; }
#-keepnames class com.fasterxml.jackson.** { *; }
#-keepclassmembers public final enum org.codehaus.jackson.annotate.JsonAutoDetect$Visibility {
#public static final org.codehaus.jackson.annotate.JsonAutoDetect$Visibility *; }

##注意：野路子中提供了太多无关配置，因此不建议使用该规则


## jackson-module-kotlin GitHub https://github.com/FasterXML/jackson-module-kotlin
## jackson-module-kotlin proguard https://github.com/FasterXML/jackson-module-kotlin#caveats
## If using proguard:
## kotlin.Metadata annotations may be stripped, preventing deserialization.
## Add a proguard rule to keep the kotlin.Metadata class: -keep class kotlin.Metadata { *; }
## If you're getting java.lang.ExceptionInInitializerError, you may also need: -keep class kotlin.reflect.** { *; }
## If you're still running into problems, you might also need to add a proguard keep rule for the specific classes you want to (de-)serialize.
## For example, if all your models are inside the package com.example.models, you could add the rule -keep class com.example.models.** { *; }

#-keep class kotlin.Metadata { *; }
#-keep class kotlin.reflect.** { *; }
#-keep class “为反射序列化类添加保留规则”

## 由于目前不使用Jackson并且野路子规则不可信，因此不添加，避免存在问题

####---------------End: proguard configuration for FastJson  ----------
