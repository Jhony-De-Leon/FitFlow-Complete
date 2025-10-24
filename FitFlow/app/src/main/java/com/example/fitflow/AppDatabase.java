package com.example.fitflow;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.annotation.NonNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {User.class, WorkoutLog.class}, version = 3, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract UserDao userDao();
    public abstract WorkoutLogDao workoutLogDao();

    private static volatile AppDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    static final ExecutorService databaseWriteExecutor = 
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    // Migración CORREGIDA de la versión 2 a la 3
    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // surname y gender se esperan como nullable (notNull=false)
            database.execSQL("ALTER TABLE users ADD COLUMN surname TEXT"); // Permite NULL
            database.execSQL("ALTER TABLE users ADD COLUMN age INTEGER NOT NULL DEFAULT 0"); // Correcto: NOT NULL
            database.execSQL("ALTER TABLE users ADD COLUMN gender TEXT"); // Permite NULL
            database.execSQL("ALTER TABLE users ADD COLUMN weight REAL NOT NULL DEFAULT 0.0"); // Correcto: NOT NULL
            database.execSQL("ALTER TABLE users ADD COLUMN height INTEGER NOT NULL DEFAULT 0"); // Correcto: NOT NULL
        }
    };

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "fitflow_database")
                            .addMigrations(MIGRATION_2_3)
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
