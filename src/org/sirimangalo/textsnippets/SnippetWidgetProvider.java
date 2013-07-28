package org.sirimangalo.textsnippets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

public class SnippetWidgetProvider extends AppWidgetProvider {

	private String TAG = this.getClass().toString();
    private static SnippetDataProviderObserver sDataObserver;
    private static Handler sWorkerQueue;
    private static HandlerThread sWorkerThread;

    public SnippetWidgetProvider() {
        // Start the worker thread
        sWorkerThread = new HandlerThread("SnippetWidgetProvider-worker");
        sWorkerThread.start();
        sWorkerQueue = new Handler(sWorkerThread.getLooper());
    }
    
    @Override
    public void onEnabled(Context context) {
        // Register for external updates to the data to trigger an update of the widget.  When using
        // content providers, the data is often updated via a background service, or in response to
        // user interaction in the main app.  To ensure that the widget always reflects the current
        // state of the data, we must listen for changes and update ourselves accordingly.
        final ContentResolver r = context.getContentResolver();
        if (sDataObserver == null) {
            final AppWidgetManager mgr = AppWidgetManager.getInstance(context);
            final ComponentName cn = new ComponentName(context, SnippetWidgetProvider.class);
            sDataObserver = new SnippetDataProviderObserver(mgr, cn, sWorkerQueue);
            r.registerContentObserver(MySQLiteHelper.URI_TABLE, true, sDataObserver);
        }
    }
	public Object test;

	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

    	Log.d(TAG,"updating widgets");

		final int N = appWidgetIds.length;

		// Create an Intent to launch SnippetsActivity
		Intent intent = new Intent(context, SnippetsActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

		// Get the layout for the App Widget and attach an on-click listener
		// to the button
		RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.appwidget);
		views.setOnClickPendingIntent(R.id.button, pendingIntent);

		Intent newIntent = new Intent(context, SnippetsActivity.class);
		newIntent.putExtra("new_snippet", true);
		PendingIntent newPendingIntent = PendingIntent.getActivity(context, 1, newIntent, PendingIntent.FLAG_CANCEL_CURRENT);
		views.setOnClickPendingIntent(R.id.add_button, newPendingIntent);
		
		// Perform this loop procedure for each App Widget that belongs to this provider
		for (int i=0; i<N; i++) {
			int appWidgetId = appWidgetIds[i];

	        // Specify the service to provide data for the collection widget.  Note that we need to
	        // embed the appWidgetId via the data otherwise it will be ignored.
	        final Intent listIntent = new Intent(context, SnippetWidgetService.class);
	        listIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
	        listIntent.setData(Uri.parse(listIntent.toUri(Intent.URI_INTENT_SCHEME)));
	        views.setRemoteAdapter(appWidgetId, R.id.snippet_list, listIntent);

	        // Set the empty view to be displayed if the collection is empty.  It must be a sibling
	        // view of the collection view.
	        views.setEmptyView(R.id.snippet_list, R.id.empty_view);

	        
            // Bind a click listener template for the contents of the list.  Note that we
            // need to update the intent's data if we set an extra, since the extras will be
            // ignored otherwise.
            final Intent onClickIntent = new Intent(context, SnippetWidgetProvider.class);
            onClickIntent.setAction(SnippetWidgetProvider.CLICK_ACTION);
            onClickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            onClickIntent.setData(Uri.parse(onClickIntent.toUri(Intent.URI_INTENT_SCHEME)));
            final PendingIntent onClickPendingIntent = PendingIntent.getBroadcast(context, 0,
                    onClickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            views.setPendingIntentTemplate(R.id.snippet_list, onClickPendingIntent);

			// Tell the AppWidgetManager to perform an update on the current app widget
			appWidgetManager.updateAppWidget(appWidgetId, views);
		}
        super.onUpdate(context, appWidgetManager, appWidgetIds);
	}

	public static String CLICK_ACTION = "org.sirimangalo.textsnippets.CLICK";
	public static String REFRESH_ACTION = "org.sirimangalo.textsnippets.REFRESH";

	@Override
	public void onReceive(Context ctx, Intent intent) {
		final String action = intent.getAction();
		if (action.equals(CLICK_ACTION)) {
			Log.i(TAG,"clicked stream item");

			String text = intent.getStringExtra("snippet");
			ClipboardManager clipboard = (ClipboardManager) ctx.getSystemService(ctx.CLIPBOARD_SERVICE); 
			ClipData clip = ClipData.newPlainText("Snippet", text);
			clipboard.setPrimaryClip(clip);
			Toast.makeText(ctx, R.string.copied, Toast.LENGTH_SHORT).show();

		}
		else {
			Log.i(TAG,"recevied widget update");
			AppWidgetManager mgr = AppWidgetManager.getInstance(ctx);
			ComponentName cn = new ComponentName(ctx, SnippetWidgetProvider.class);
			mgr.notifyAppWidgetViewDataChanged(mgr.getAppWidgetIds(cn), R.id.snippet_list);
		}

		super.onReceive(ctx, intent);
	}
}

/**
 * Our data observer just notifies an update for all snippet widgets when it detects a change.
 */
class SnippetDataProviderObserver extends ContentObserver {
    private AppWidgetManager mAppWidgetManager;
    private ComponentName mComponentName;
	private String TAG = this.getClass().toString();

    SnippetDataProviderObserver(AppWidgetManager mgr, ComponentName cn, Handler h) {
        super(h);
        mAppWidgetManager = mgr;
        mComponentName = cn;
    }

    @Override
    public void onChange(boolean selfChange) {
		Log.i(TAG,"widget data changed");
        // The data has changed, so notify the widget that the collection view needs to be updated.
        // In response, the factory's onDataSetChanged() will be called which will requery the
        // cursor for the new data.
        mAppWidgetManager.notifyAppWidgetViewDataChanged(
                mAppWidgetManager.getAppWidgetIds(mComponentName), R.id.snippet_list);
    }
}
