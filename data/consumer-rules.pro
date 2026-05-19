-keep class net.zetetic.database.** { *; }
-dontwarn net.zetetic.database.**

# Room (consumed by app release R8)
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**
