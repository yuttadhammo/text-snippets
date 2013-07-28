package org.sirimangalo.textsnippets;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

public class MySQLiteHelper extends SQLiteOpenHelper {
	public static final Uri URI_TABLE = 
		    Uri.parse("sqlite://org.sirimangalo.textsnippets/snippets");
	public static final String TABLE_SNIPPETS = "snippets";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_SNIPPET = "snippet";
	public static final String COLUMN_COMMENT = "comment";

	private static final String DATABASE_NAME = "snippets.db";
	private static final int DATABASE_VERSION = 2;

	// Database creation sql statement
	private static final String DATABASE_CREATE = "create table "
			+ TABLE_SNIPPETS + "(" 
			+ COLUMN_ID + " integer primary key autoincrement, " 
			+ COLUMN_SNIPPET + " text,"
			+ COLUMN_COMMENT + " text"
			+");";

	public MySQLiteHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(DATABASE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (oldVersion < 2) {
				db.execSQL("ALTER TABLE " + TABLE_SNIPPETS + " ADD COLUMN " + COLUMN_COMMENT + " text");
		}
	}

}
