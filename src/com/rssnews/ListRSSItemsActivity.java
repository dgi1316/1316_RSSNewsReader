package com.rssnews;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class ListRSSItemsActivity extends ListActivity {

	private ProgressDialog pDialog;
	ArrayList<HashMap<String, String>> rssItemList = new ArrayList<HashMap<String, String>>();
	RSSParser rssParser = new RSSParser();
	List<RSSItem> rssItems = new ArrayList<RSSItem>();
	String rss_link;
	RSSFeed rssFeed;

	private static String TAG_TITLE = "title";
	private static String TAG_LINK = "link";
	private static String TAG_DESRIPTION = "description";
	private static String TAG_PUB_DATE = "pubDate";
	// private static String TAG_GUID = "guid"; // not used

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.rss_item_list);

		Intent i = getIntent();
		Integer site_id = Integer.parseInt(i.getStringExtra("id"));
		RSSDatabaseHandler rssDB = new RSSDatabaseHandler(
				getApplicationContext());

		WebSite site = rssDB.getSite(site_id);
		rss_link = site.getRSSLink();
		/**
		 * Calling a backgroung thread will loads recent articles of a website
		 * 
		 * @param rss_url of website
		 * */
		new loadRSSFeedItems().execute(rss_link);

		ListView lv = getListView();
		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent in = new Intent(getApplicationContext(),
						DisPlayWebPageActivity.class);
				String page_url = ((TextView) view.findViewById(R.id.page_url))
						.getText().toString();
				in.putExtra("page_url", page_url);
				startActivity(in);
			}
		});
	}
	/**
	 * Background Async Task to get RSS Feed Items data from URL
	 * */
	class loadRSSFeedItems extends AsyncTask<String, String, String> {
		/**
		 * Before starting background thread Show Progress Dialog
		 * */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(ListRSSItemsActivity.this);
			pDialog.setMessage("Loading recent articles...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);
			pDialog.show();
		}
		/**
		 * getting all recent articles and showing them in listview
		 * */
		@Override
		protected String doInBackground(String... args) {
			String rss_url = args[0];
			rssItems = rssParser.getRSSFeedItems(rss_url);
			for (RSSItem item : rssItems) {
				HashMap<String, String> map = new HashMap<String, String>();
				map.put(TAG_TITLE, item.getTitle());
				map.put(TAG_LINK, item.getLink());
				map.put(TAG_PUB_DATE, item.getPubdate());

				String description = item.getDescription();
				if (description.length() > 100) {
					description = description.substring(0, 97) + "..";
				}
				map.put(TAG_DESRIPTION, description);
				rssItemList.add(map);
			}

			runOnUiThread(new Runnable() {
				public void run() {
					/**
					 * Updating parsed items into listview
					 * */
					ListAdapter adapter = new SimpleAdapter(
							ListRSSItemsActivity.this, rssItemList,
							R.layout.rss_item_list_row, new String[] {
									TAG_LINK, TAG_TITLE, TAG_PUB_DATE,
									TAG_DESRIPTION }, new int[] {
									R.id.page_url, R.id.title, R.id.pub_date,
									R.id.link });
					setListAdapter(adapter);
				}
			});
			return null;
		}

		/**
		 * After completing background task Dismiss the progress dialog
		 * **/
		protected void onPostExecute(String args) {
			pDialog.dismiss();
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater mInflater = getMenuInflater();
		mInflater.inflate(R.menu.refresh, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {			
		if(item.getItemId() == R.id.menuRefresh)
		{
			new loadRSSFeedItems().execute(rss_link);
		}
		return true;
	}
}
