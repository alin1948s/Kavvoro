-keep class org.jbox2d.** { *; }

# WorkManager 2.7 creates its Room database implementation through
# Room.getGeneratedImplementation(), which uses reflection on the generated
# WorkDatabase_Impl no-arg constructor. The dependency's legacy consumer rule
# keeps only the class name; R8 can therefore remove the constructor and the
# DAO accessors, causing a release-only crash in AndroidX Startup before the
# first activity frame. Keep the generated implementation and its members.
-keep class androidx.work.impl.WorkDatabase_Impl { *; }
