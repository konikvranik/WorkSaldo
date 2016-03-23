# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /home/hpa/Android/Sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-dontwarn javax.activation.**
-dontwarn javax.annotation.**
-dontwarn javax.xml.**
-dontwarn org.joda.convert.**
-dontwarn java.beans.**
-dontwarn org.osgi.framework.**
-dontwarn org.apache.tools.ant.**
-dontwarn java.applet.**
-dontwarn com.sun.jdi.**
-dontwarn sun.misc.**
-dontwarn jersey.repackaged.**
-dontwarn org.xml.**
-dontwarn net.sf.cglib.**
-dontwarn org.glassfish.jersey.server.**
-dontwarn java.awt.**
-dontwarn javax.imageio.**
-dontwarn com.google.inject.**
-dontwarn org.w3c.dom.bootstrap.**
-keepattributes InnerClasses