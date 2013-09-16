package org.sirimangalo.textsnippets;

import java.io.IOException;

import android.app.backup.BackupAgentHelper;
import android.app.backup.BackupDataInput;
import android.app.backup.BackupDataOutput;
import android.app.backup.FileBackupHelper;
import android.os.ParcelFileDescriptor;
import android.util.Log;

public class TextSnippetsBackupAgent extends BackupAgentHelper {

    private static final String DB_NAME = "snippets.db";

    @Override
    public void onCreate(){

        FileBackupHelper dbs = new FileBackupHelper(this,
    		    "../databases/" + DB_NAME);
        
        addHelper("adbs", dbs);
    	
    }

    @Override
    public void onBackup(ParcelFileDescriptor oldState, BackupDataOutput data,
         ParcelFileDescriptor newState) throws IOException {
            synchronized (MySQLiteHelper.dbLock) {
                    super.onBackup(oldState, data, newState);
            }
    }

    @Override
    public void onRestore(BackupDataInput data, int appVersionCode,
                    ParcelFileDescriptor newState) throws IOException {
            Log.d("TextSnippetsBackupAgent", "onRestore called");

            synchronized (MySQLiteHelper.dbLock) {
                    Log.d("TextSnippetsBackupAgent", "onRestore in-lock");

                    super.onRestore(data, appVersionCode, newState);
            }
    }
    
}