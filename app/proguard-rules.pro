# Conservar clases de jME
-keep class com.jme3.** { *; }
-keep class jme3tools.** { *; }
-keep class com.jme3.bullet.** { *; }
-keepattributes *Annotation*, Signature, InnerClasses
-dontwarn com.jme3.**
-dontwarn javax.**
-dontwarn java.awt.**
-dontwarn org.lwjgl.**
-dontwarn jme3tools.**
# Clases del juego (se instancian por reflexion)
-keep class com.miempresa.mifpsgame.** { *; }
-keep class com.github.stephengold.** { *; }
-dontwarn com.github.stephengold.**
