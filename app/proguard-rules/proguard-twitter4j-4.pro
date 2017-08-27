# twitter4j (external library for twitter)
-keep class twitter4j.** { *; }
-dontwarn twitter4j.management.**
-dontwarn twitter4j.TwitterAPIMonitor
-dontwarn twitter4j.internal.**
-dontwarn twitter4j.Annotation

-keep class javax.** { *; }
-dontwarn javax.management.**
-dontwarn javax.xml.**
-dontwarn java.lang.**
-dontwarn javax.faces.**
-dontwarn org.slf4j.**

-keep class org.apache.http.** { *; }
-dontwarn org.apache.**

