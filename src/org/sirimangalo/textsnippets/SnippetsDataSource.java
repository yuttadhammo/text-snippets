package org.sirimangalo.textsnippets;
import java.util.ArrayList;
import java.util.List;

import android.app.backup.BackupManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class SnippetsDataSource {

	// Database fields
	private SQLiteDatabase database;
	private MySQLiteHelper dbHelper;
	private String[] allColumns = { 
			MySQLiteHelper.COLUMN_ID,
				MySQLiteHelper.COLUMN_SNIPPET, 
				MySQLiteHelper.COLUMN_COMMENT 
			};

	public SnippetsDataSource(Context context) {
		dbHelper = new MySQLiteHelper(context);
	}

	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}

	public Snippet createSnippet(String snippet, String comment) {
		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.COLUMN_SNIPPET, snippet);
		values.put(MySQLiteHelper.COLUMN_COMMENT, comment);
        long insertId;
		synchronized (MySQLiteHelper.dbLock) {
			insertId = database.insert(MySQLiteHelper.TABLE_SNIPPETS, null,
					values);
        }
		Cursor cursor = database.query(MySQLiteHelper.TABLE_SNIPPETS,
				allColumns, MySQLiteHelper.COLUMN_ID + " = " + insertId, null,
				null, null, null);
		cursor.moveToFirst();
		Snippet newSnippet = cursorToSnippet(cursor);
		cursor.close();
		BackupManager.dataChanged("org.sirimangalo.textsnippets");
		return newSnippet;
	}

	public void deleteSnippet(Snippet snippet) {
		long id = snippet.getId();
		synchronized (MySQLiteHelper.dbLock) {
			database.delete(MySQLiteHelper.TABLE_SNIPPETS, MySQLiteHelper.COLUMN_ID
					+ " = " + id, null);
		}
		BackupManager.dataChanged("org.sirimangalo.textsnippets");
	}

	public void editSnippet(Snippet snippet) {
			ContentValues values = new ContentValues();
			values.put(MySQLiteHelper.COLUMN_ID, snippet.getId());
			values.put(MySQLiteHelper.COLUMN_SNIPPET, snippet.getSnippet());
			values.put(MySQLiteHelper.COLUMN_COMMENT, snippet.getComment());
			synchronized (MySQLiteHelper.dbLock) {
				database.replace(MySQLiteHelper.TABLE_SNIPPETS, null, values);
			}
			BackupManager.dataChanged("org.sirimangalo.textsnippets");
	}
	
	public List<Snippet> getAllSnippets() {
		List<Snippet> snippets = new ArrayList<Snippet>();

		Cursor cursor = database.query(MySQLiteHelper.TABLE_SNIPPETS,
				allColumns, null, null, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Snippet snippet = cursorToSnippet(cursor);
			snippets.add(snippet);
			cursor.moveToNext();
		}
		// Make sure to close the cursor
		cursor.close();
		return snippets;
	}

	public Cursor getAllSnippetsCursor() {
		return database.query(MySQLiteHelper.TABLE_SNIPPETS,
				allColumns, null, null, null, null, null);
	}
	
	private Snippet cursorToSnippet(Cursor cursor) {
		Snippet snippet = new Snippet();
		snippet.setId(cursor.getLong(0));
		snippet.setSnippet(cursor.getString(1),cursor.getString(2));
		return snippet;
	}

	public void switchSnippets(Snippet snippet, Snippet otherSnippet) {
		long id1 = snippet.getId();
		long id2 = otherSnippet.getId();
		snippet.setId(id2);
		otherSnippet.setId(id1);
		editSnippet(snippet);
		editSnippet(otherSnippet);
	}
} 
