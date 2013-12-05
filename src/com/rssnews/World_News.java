package com.rssnews;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class World_News extends Activity {

	private ProgressDialog pDialog;
	ArrayList<HashMap<String, String>> rssFeedList;
	RSSParser rssParser = new RSSParser();
	RSSFeed rssFeed;
	String[] sqliteIds;
	public static String TAG_ID = "id";
	public static String TAG_TITLE = "title";
	public static String TAG_LINK = "link";
	ListView lv;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.world_tab);
		rssFeedList = new ArrayList<HashMap<String, String>>();

		/**
		 * Calling a background thread which will load web sites stored in
		 * SQLite database
		 * */
		new loadStoreSites().execute();
		lv = (ListView) findViewById(R.id.listWorld);
		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				String sqlite_id = ((TextView) view
						.findViewById(R.id.sqlite_idw)).getText().toString();
				Intent in = new Intent(getApplicationContext(),
						WorldListRSSItemsActivity.class);
				in.putExtra(TAG_ID, sqlite_id);
				startActivity(in);
			}
		});
	}

	/**
	 * Building a context menu for listview Long press on List row to see
	 * context menu
	 * */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		if (v.getId() == R.id.listWorld) {
			menu.setHeaderTitle("Options");
			menu.add(Menu.NONE, 0, 0, "Add to Favourites");
			menu.add(Menu.NONE, 1, 1, "Delete Feed");
		}
	}

	/**
	 * Responding to context menu selected option
	 * */
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
				.getMenuInfo();
		int menuItemIndex = item.getItemId();
		switch (menuItemIndex) {
		case 0: {
			RSSDatabaseHandler rssDb = new RSSDatabaseHandler(
					getApplicationContext());
			WebSite site = new WebSite();
			site.setId(Integer.parseInt(sqliteIds[info.position]));
			int id = site.getId();
			rssDb.favWorldSite(id, site);
			rssDb.close();
			Intent intent = new Intent(this, RSSNewsReaderPBActivity.class);
			finish();
			startActivity(intent);
		}
			break;
		case 1: {
			RSSDatabaseHandler rssDb = new RSSDatabaseHandler(
					getApplicationContext());
			WebSite site = new WebSite();
			site.setId(Integer.parseInt(sqliteIds[info.position]));
			rssDb.deleteWorldSite(site);
			rssDb.close();
			Intent intent = new Intent(this, RSSNewsReaderPBActivity.class);
			finish();
			startActivity(intent);
		}
			break;

		default:
			break;
		}
		return true;
	}

	/**
	 * Background Async Task to get RSS data from URL
	 * */
	class loadStoreSites extends AsyncTask<String, String, String> {
		/**
		 * Before starting background thread Show Progress Dialog
		 * */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(World_News.this);
			pDialog.setMessage("Loading websites ...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);
			pDialog.show();
		}

		/**
		 * getting all stored website from SQLite
		 * */
		@Override
		protected String doInBackground(String... args) {
			runOnUiThread(new Runnable() {
				public void run() {
					RSSDatabaseHandler rssDb = new RSSDatabaseHandler(
							getApplicationContext());
					List<WebSite> siteList = rssDb.getWorldSites();
					sqliteIds = new String[siteList.size()];
					for (int i = 0; i < siteList.size(); i++) {
						WebSite s = siteList.get(i);
						HashMap<String, String> map = new HashMap<String, String>();
						map.put(TAG_ID, s.getId().toString());
						map.put(TAG_TITLE, s.getTitle());
						map.put(TAG_LINK, s.getLink());
						rssFeedList.add(map);
						sqliteIds[i] = s.getId().toString();
					}
					/**
					 * Updating list view with websites
					 * */
					ListAdapter adapter = new SimpleAdapter(World_News.this,
							rssFeedList, R.layout.world_list_item,
							new String[] { TAG_ID, TAG_TITLE, TAG_LINK },
							new int[] { R.id.sqlite_idw, R.id.titlew,
									R.id.linkw });
					lv.setAdapter(adapter);
					registerForContextMenu(lv);
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
	public void onBackPressed() {
		finish();
	}
}
