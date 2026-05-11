-keep class net.sqlcipher.** { *; }
-dontwarn net.sqlcipher.**

# Room (consumed by app release R8)
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**
