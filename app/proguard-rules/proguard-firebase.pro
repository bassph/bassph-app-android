# Add this global rule
-keepattributes Signature
# This rule will properly ProGuard all the model classes in
# the package com.yourcompany.models. Modify to fit the structure
# of your app.
# https://firebase.google.com/docs/database/android/start/
-keepclassmembers class ph.yoyo.popslide.model.entity.** { *; }