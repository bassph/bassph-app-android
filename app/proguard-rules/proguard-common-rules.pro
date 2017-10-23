# defualt
-keep class org.projectbass.bass.model.** { *; }
-dontwarn io.requery.sql.platform.PostgresSQL
-dontwarn javax.sql.rowset.serial.**

# google play
-keep class * extends java.util.ListResourceBundle {
    protected Object[][] getContents();
}

-keep public class com.google.android.gms.common.internal.safeparcel.SafeParcelable {
    public static final *** NULL;
}

-keepnames @com.google.android.gms.common.annotation.KeepName class *
-keepclassmembernames class * {
    @com.google.android.gms.common.annotation.KeepName *;
}

-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient { *; }

-keepnames class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# google
-keep class sun.misc.Unsafe { *; }
-keepclassmembers class * extends com.actionbarsherlock.ActionBarSherlock {
    <init>(android.app.Activity, int);
}
-dontwarn com.actionbarsherlock.**
-keep class com.google.gson
-keep class Gson**
-keepclassmembers class Gson** {
    *;
}

# ViewPagerIndicator
-dontwarn com.viewpagerindicator.**

# BuildTools 23 update removes android.app.Notification.setLatestEventInfo
-dontwarn com.google.android.gms.**

-dontwarn org.codehaus.mojo.animal_sniffer.**

-keep class com.crashlytics.** { *; }
-keep class com.crashlytics.android.**
-keep class com.crashlytics.** { *; }
-dontwarn com.crashlytics.**
-keepattributes *Annotation*,SourceFile,LineNumberTable
-dontwarn sun.misc.Unsafe
