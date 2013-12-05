package com.rssnews;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class AddNewFeed extends Activity {
	Button btnSubmit;
	Button btnCancel;
	EditText txtUrl;
	TextView textViewMessage;

	RSSParser rssParser = new RSSParser();
	RSSFeed rssFeed;

	private ProgressDialog pDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.addnewfeed);

		btnSubmit = (Button) findViewById(R.id.btnSubmit);
		btnCancel = (Button) findViewById(R.id.btnCancel);
		txtUrl = (EditText) findViewById(R.id.txtUrl);
		textViewMessage = (TextView) findViewById(R.id.textViewMessage);

		btnSubmit.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				String url = txtUrl.getText().toString();
				Log.d("URL Length", "" + url.length());
				if (url.length() > 0) {
					textViewMessage.setText("");
					String urlPattern = "^http(s{0,1})://[a-zA-Z0-9_/\\-\\.]+\\.([A-Za-z/]{2,5})[a-zA-Z0-9_/\\&\\?\\=\\-\\.\\~\\%]*";
					if (url.matches(urlPattern)) {
						new loadRSSFeed().execute(url);
					} else {
						textViewMessage.setText("Please enter a valid url");
					}
				} else {
					textViewMessage.setText("Please enter website url");
				}
			}
		});

		btnCancel.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});
	}

	/**
	 * Background Async Task to get RSS data from URL
	 * */
	class loadRSSFeed extends AsyncTask<String, String, String> {

		/**
		 * Before starting background thread Show Progress Dialog
		 * */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(AddNewFeed.this);
			pDialog.setMessage("Fetching RSS Information ...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);
			pDialog.show();
			
		}

		/**
		 * getting
		 * */
		@Override
		protected String doInBackground(String... args) {
			String url = args[0];
			rssFeed = rssParser.getRSSFeed(url);
			Log.d("rssFeed", " " + rssFeed);
			if (rssFeed != null) {
				Log.e("RSS URL",
						rssFeed.getTitle() + "" + rssFeed.getLink() + ""
								+ rssFeed.getDescription() + ""
								+ rssFeed.getLanguage());
				RSSDatabaseHandler rssDb = new RSSDatabaseHandler(
						getApplicationContext());
				WebSite site = new WebSite(rssFeed.getTitle(),
						rssFeed.getLink(), rssFeed.getRSSLink(),
						rssFeed.getDescription());
				rssDb.addSite(site);
				Intent i = new Intent(getApplicationContext(),
						RSSNewsReaderPBActivity.class);
				// send result code 100 to notify about product update
				setResult(100, i);
				startActivity(i);
				return null;
			} else {
				runOnUiThread(new Runnable() {
					public void run() {
						textViewMessage
								.setText("Rss url not found. Please check the url or try again");
					}
				});
			}
			return null;
		}

		/**
		 * After completing background task Dismiss the progress dialog
		 * **/
		protected void onPostExecute(String args) {
			pDialog.dismiss();
			runOnUiThread(new Runnable() {
				public void run() {
					if (rssFeed != null) {
					}
				}
			});
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		//pDialog.dismiss();
		finish();
	}

	@Override
	public void onBackPressed() {
		finish();
	}
}
