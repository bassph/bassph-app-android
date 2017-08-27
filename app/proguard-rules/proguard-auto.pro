## Auto library specific rules ##
-keep public class * extends com.google.auto.factory.** { *;}
-keep class com.google.auto.** { *; }
# annotation-processor dependent libs are not included in build, so processor reports error. Ignore them!
-dontwarn com.google.auto.factory.processor.**
-dontwarn com.google.auto.service.processor.**
# https://github.com/frankiesardo/auto-parcel/issues/19
-dontwarn com.google.auto.common.**
-dontnote com.google.auto.**
-dontwarn com.google.common.**