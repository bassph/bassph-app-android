-keep class com.facebook.** { *; }
-keepattributes Signature

-dontwarn bolts.**
-dontwarn com.facebook.**
-keep class com.parse.** { *; }

-keep class com.facebook.FacebookSdk {
   boolean isInitialized();
}
-keep class com.facebook.appevents.AppEventsLogger {
   com.facebook.appevents.AppEventsLogger newLogger(android.content.Context);
   void logSdkEvent(java.lang.String, java.lang.Double, android.os.Bundle);
}
