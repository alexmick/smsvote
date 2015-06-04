package fr.micklewright.smsvote;


import android.app.Application;
import android.database.sqlite.SQLiteDatabase;

import fr.micklewright.smsvote.database.DaoMaster;
import fr.micklewright.smsvote.database.DaoSession;

public class DaoSessionApplication extends Application {
    public DaoSession daoSession;

    @Override
    public void onCreate() {
        super.onCreate();
        setupDatabase();
    }

    private void setupDatabase() {
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "example-db", null);
        SQLiteDatabase db = helper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(db);
        daoSession = daoMaster.newSession();
    }

    public DaoSession getDaoSession() {
        return daoSession;
    }
}
