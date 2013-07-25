package org.sirimangalo.textsnippets;

public class Snippet {
	private long id;
	private String snippet;
	private String comment;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		  this.id = id;
	}

	public String getSnippet() {
		  return snippet;
	}
	public String getComment() {
		  return comment;
	}

	public void setSnippet(String snippet, String comment) {
		this.snippet = snippet;
		this.comment = comment;
	}

	// Will be used by the ArrayAdapter in the ListView
	@Override
	public String toString() {
		return snippet;
	}
}
