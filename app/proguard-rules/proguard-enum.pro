-keepattributes InnerClasses, EnclosingMethod

-keep public enum **$** {
    **[] $VALUES;
    public *;
}

-keepclassmembers enum * {
    <fields>;
    public static **[] values();
    public static ** valueOf(java.lang.String);
}