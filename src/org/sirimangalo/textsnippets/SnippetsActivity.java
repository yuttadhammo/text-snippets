package org.sirimangalo.textsnippets;

import java.util.List;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class SnippetsActivity extends ListActivity {

	private SnippetsDataSource datasource;
	private ArrayAdapter<Snippet> adapter;
	private String TAG = "SnippetsActivity";
	private Activity activity;
	protected Intent refreshIntent;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		activity = this;
		refreshIntent = new Intent(SnippetWidgetProvider.REFRESH_ACTION);
	}
	@Override
	public void onResume(){
		super.onResume();
	    datasource = new SnippetsDataSource(this);
	    datasource.open();
		Bundle extras = getIntent().getExtras();
		if(extras != null) {
			if (extras.containsKey(Intent.EXTRA_TEXT))
				showSnippetDialog(extras.getCharSequence(Intent.EXTRA_TEXT).toString());
			else if (extras.containsKey("new_snippet"))
				showSnippetDialog("");
		}

		resetListView();
    	registerForContextMenu(getListView());
	}
	@Override
	public void onPause(){
		super.onPause();
		datasource.close();
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		super.onOptionsItemSelected(item);
		
		switch (item.getItemId()) {
	        case android.R.id.home:
	        	finish();
	            return true;
			case R.id.action_new:
				showSnippetDialog("");
				return true;

		}
		return false;
	}	

	private void showSnippetDialog(String string) {
		final LinearLayout ll = (LinearLayout) getLayoutInflater().inflate(R.layout.form, null);
		final EditText snippetView = (EditText) ll.findViewById(R.id.snippet);
		final EditText commentView = (EditText) ll.findViewById(R.id.comment);
		snippetView.setText(string);
		new AlertDialog.Builder(this)
		.setTitle(R.string.action_new)
		.setView(ll)
		.setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int whichButton) {
		    	Snippet snippet = datasource.createSnippet(snippetView.getText().toString(),commentView.getText().toString());
				adapter.add(snippet);
				adapter.notifyDataSetChanged();
				activity.sendBroadcast(refreshIntent);
				hideKeyboard(ll);
		    }
		}).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int whichButton) {
				hideKeyboard(ll);
		    }
		}).show();	
	}
	private void hideKeyboard(View v) {
		InputMethodManager imm = (InputMethodManager)getSystemService(
			      Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
		
	}
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Log.i(TAG,"clicked stream item");
		Snippet snippet = (Snippet) getListView().getItemAtPosition(position);
		String text = snippet.getSnippet();
		ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE); 
		ClipData clip = ClipData.newPlainText("Snippet", text);
		clipboard.setPrimaryClip(clip);
		Toast.makeText(this, R.string.copied, Toast.LENGTH_SHORT).show();
		
	}
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		android.view.MenuInflater inflater = getMenuInflater();
       	
		inflater.inflate(R.menu.context, menu);
        menu.setHeaderTitle(getString(R.string.snippet_options));
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
        int position = info.position;
        if(position == 0)
			menu.findItem(R.id.move_up).setVisible(false);
        if(position == getListView().getCount()-1)
			menu.findItem(R.id.move_down).setVisible(false);
        
        super.onCreateContextMenu(menu, v, menuInfo);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
	    final int index = info.position;
		final Snippet snippet = (Snippet) getListView().getItemAtPosition(index);
		switch (item.getItemId()) {
			case R.id.edit:
				final LinearLayout ll = (LinearLayout) getLayoutInflater().inflate(R.layout.form, null);
				final EditText snippetView = (EditText) ll.findViewById(R.id.snippet);
				final EditText commentView = (EditText) ll.findViewById(R.id.comment);
				snippetView.setText(snippet.getSnippet());
				commentView.setText(snippet.getComment());
				new AlertDialog.Builder(this)
				.setTitle(R.string.action_new)
				.setView(ll)
				.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
				    public void onClick(DialogInterface dialog, int whichButton) {
				    	snippet.setSnippet(snippetView.getText().toString(),commentView.getText().toString());
				    	datasource.editSnippet(snippet);
				    	resetListView();
						sendBroadcast(refreshIntent);
						hideKeyboard(ll);
				    }
				}).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
				    public void onClick(DialogInterface dialog, int whichButton) {
						hideKeyboard(ll);
				    }
				}).show();	
				return true;
			case R.id.remove:
		    	datasource.deleteSnippet(snippet);
				adapter.remove(snippet);
				adapter.notifyDataSetChanged();
				sendBroadcast(refreshIntent);
				return true;				
			case R.id.share:
				Intent i = new Intent(Intent.ACTION_SEND);
				i.putExtra(Intent.EXTRA_TEXT, snippet.getSnippet());
				i.setType("text/plain");
				startActivity(Intent.createChooser(i, getString(R.string.share_via)));
				return true;
			case R.id.move_up:
				final Snippet prevSnippet = (Snippet) getListView().getItemAtPosition(index-1);
				datasource.switchSnippets(snippet, prevSnippet);
				resetListView();
				sendBroadcast(refreshIntent);
				return true;				
			case R.id.move_down:
				final Snippet nextSnippet = (Snippet) getListView().getItemAtPosition(index+1);
				datasource.switchSnippets(snippet, nextSnippet);
				resetListView();
				sendBroadcast(refreshIntent);
				return true;				
			default:
				break;
		}
		
		return super.onContextItemSelected(item);
	}
	protected void resetListView() {
	    List<Snippet> values = datasource.getAllSnippets();
	    adapter = new SnippetAdapter(activity, values);
	    setListAdapter(adapter);		
	}
	
}
