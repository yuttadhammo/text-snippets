package org.sirimangalo.textsnippets;

import java.util.List;

import android.widget.ArrayAdapter;
import android.app.Activity;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class SnippetAdapter extends ArrayAdapter<Snippet> {

	protected String TAG = "SnippetAdapter";
	
	public SnippetAdapter(Activity activity, List<Snippet> _snippets) {
		super(activity, 0, _snippets);
		
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {

		final Activity activity = (Activity) getContext();
		LayoutInflater inflater = activity.getLayoutInflater();

		// Inflate the views from XML
		View rowView;
		
		Snippet snippet = getItem(position);
		
		rowView =  inflater.inflate(R.layout.list_item, null);
		
		TextView snippetView = (TextView) rowView.findViewById(R.id.snippet);
		snippetView.setText(snippet.getSnippet());
		
		String comment = snippet.getComment();
		if(comment != null && comment.length() > 0) {
			TextView commentView = (TextView) rowView.findViewById(R.id.comment);
			commentView.setVisibility(View.VISIBLE);
			commentView.setText(comment);
		}
		
		return rowView;

	}
}
