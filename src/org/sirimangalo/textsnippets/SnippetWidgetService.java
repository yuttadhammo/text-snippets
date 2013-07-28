package org.sirimangalo.textsnippets;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

/**
 * This is the service that provides the factory to be bound to the collection service.
 */
public class SnippetWidgetService extends RemoteViewsService {
	private String TAG = this.getClass().toString();

	private Object test;
	
	@Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
    	Log.d(TAG,"getting widget adapter");
        return new StackRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}

/**
 * This is the factory that will provide data to the collection widget.
 */
class StackRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    private Context mContext;
    private Cursor mCursor;
    private int mAppWidgetId;
	private SnippetsDataSource datasource;
	private String TAG = this.getClass().toString();

    public StackRemoteViewsFactory(Context context, Intent intent) {
        mContext = context;
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
        datasource = new SnippetsDataSource(context);
        datasource.open();
        mCursor = datasource.getAllSnippetsCursor();
    }

    public void onCreate() {
        // Since we reload the cursor in onDataSetChanged() which gets called immediately after
        // onCreate(), we do nothing here.
    }

    public void onDestroy() {
        if (mCursor != null) {
            mCursor.close();
        }
        if (datasource != null)
        	datasource.close();
    }

    public int getCount() {
    	Log.d(TAG,"cursor count: "+mCursor.getCount());
        return mCursor.getCount();
    }

    public RemoteViews getViewAt(int position) {
    	Log.d(TAG,"getting for position: "+position);
    	if (!mCursor.moveToPosition(position))
    		return null;
    	
        String snippet = mCursor.getString(1);

        // Return a proper item with the proper day and temperature
        final int itemId = R.layout.widget_item;
        RemoteViews rv = new RemoteViews(mContext.getPackageName(), itemId);
        rv.setTextViewText(R.id.snippet, snippet);

        // Set the click intent so that we can handle it and show a toast message
        final Intent fillInIntent = new Intent();
        final Bundle extras = new Bundle();
        extras.putString("snippet", snippet);
        fillInIntent.putExtras(extras);
        rv.setOnClickFillInIntent(R.id.list_item, fillInIntent);

        return rv;
    }
    public RemoteViews getLoadingView() {
        // We aren't going to return a default loading view in this sample
        return null;
    }

    public int getViewTypeCount() {
        // Technically, we have two types of views (the dark and light background views)
        return 2;
    }

    public long getItemId(int position) {
        return position;
    }

    public boolean hasStableIds() {
        return true;
    }

    public void onDataSetChanged() {
        // Refresh the cursor
        if (mCursor != null) {
            mCursor.close();
        }
        mCursor = datasource.getAllSnippetsCursor();
    }
}