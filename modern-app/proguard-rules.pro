# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep Hilt classes
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# Keep Room classes
-keep class androidx.room.** { *; }

# Keep coroutines
-keep class kotlinx.coroutines.** { *; }
