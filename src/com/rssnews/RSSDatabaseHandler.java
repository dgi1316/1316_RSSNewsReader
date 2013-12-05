package com.rssnews;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

public class RSSDatabaseHandler extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 1;
	private static String DATABASE_PATH = "/data/data/com.rssnews/databases/";
	private static final String DATABASE_NAME = "rssnewsreader";
	
	private static final String TABLE_INDIA_RSS = "india_rss";
	private static final String TABLE_WORLD_RSS = "world_rss";
	private static final String TABLE_FAVOURITES = "favourites";
	private static final String TABLE_RSS_NEW_ADD = "rss_new_add";

	private static final String KEY_ID = "id";
	private static final String KEY_TITLE = "title";
	private static final String KEY_LINK = "link";
	private static final String KEY_RSS_LINK = "rss_link";
	private static final String KEY_DESCRIPTION = "description";

	private SQLiteDatabase rssDatabase;
	private final Context rssContext;

	public RSSDatabaseHandler(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.rssContext = context;
	}

	public void createDataBase() throws IOException {
		boolean dbExist = checkDataBase();
		if (dbExist) {
		}
		else {
			this.getReadableDatabase();
			try {
				copyDataBase();
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	/**
	 * Check if the database already exist to "avoid re-copying" the file each
	 * time you open the application.
	 * 
	 * @return true if it exists, false if it doesn't
	 */
	private boolean checkDataBase() {

		File dbFile = null;
		try {
			// String myPath = DATABASE_PATH + DATABASE_NAME;
			// checkDB = SQLiteDatabase.openDatabase(myPath, null,
			// SQLiteDatabase.OPEN_READONLY);
			dbFile = new File(DATABASE_PATH + DATABASE_NAME);
			return dbFile.exists();
		} catch (Exception e) {
			e.printStackTrace();
		}
		/*if(checkDB != null){
		 checkDB.close();}
		 return checkDB != null ? true : false;*/
		return dbFile != null ? true : false;
	}

	/**
	 * Copies your database from your local assets-folder to the just created
	 * empty database in the system folder, from where it can be accessed and
	 * handled. This is done by transfering bytestream.
	 * */
	private void copyDataBase() throws IOException {

		InputStream myInput = rssContext.getAssets().open(DATABASE_NAME);
		String outFileName = DATABASE_PATH + DATABASE_NAME;
		OutputStream myOutput = new FileOutputStream(outFileName);
		byte[] buffer = new byte[1024];
		int length;
		while ((length = myInput.read(buffer)) > 0) {
			myOutput.write(buffer, 0, length);
		}
		myOutput.flush();
		myOutput.close();
		myInput.close();
	}
	
	public void openDataBase() throws SQLException {

		String myPath = DATABASE_PATH + DATABASE_NAME;
		//String myPath = "/data/data/com.rssnews/databases/rssnewsreader";
		rssDatabase = SQLiteDatabase.openDatabase(myPath, null,
				SQLiteDatabase.OPEN_READWRITE);
		close();
	}

	@Override
	public synchronized void close() {
		if (rssDatabase != null && rssDatabase.isOpen()) {
			rssDatabase.close();
			super.close();
		}
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		/* String CREATE_INDIA_RSS_TABLE = "CREATE TABLE " + TABLE_INDIA_RSS +
		 * "(" + KEY_ID + " INTEGER PRIMARY KEY," + KEY_TITLE + " TEXT," +
		 * KEY_LINK + " TEXT," + KEY_RSS_LINK + " TEXT," + KEY_DESCRIPTION +
		 * " TEXT" + ")";
		 * String CREATE_WORLD_RSS_TABLE = "CREATE TABLE " +
		 * TABLE_WORLD_RSS + "(" + KEY_ID + " INTEGER PRIMARY KEY," + KEY_TITLE
		 * + " TEXT," + KEY_LINK + " TEXT," + KEY_RSS_LINK + " TEXT," +
		 * KEY_DESCRIPTION + " TEXT" + ")";
		 * String CREATE_FAVOURITES_TABLE =
		 * "CREATE TABLE " + TABLE_FAVOURITES + "(" + KEY_ID + " INTEGER," +
		 * KEY_TITLE + " TEXT," + KEY_LINK + " TEXT," + KEY_RSS_LINK + " TEXT,"
		 * + KEY_DESCRIPTION + " TEXT" + ")"; 
		 * String CREATE_RSS_NEW_ADD_TABLE
		 * = "CREATE TABLE " + TABLE_RSS_NEW_ADD + "(" + KEY_ID +
		 * " INTEGER PRIMARY KEY," + KEY_TITLE + " TEXT," + KEY_LINK + " TEXT,"
		 * + KEY_RSS_LINK + " TEXT," + KEY_DESCRIPTION + " TEXT" + ")";
		 * db.execSQL(CREATE_INDIA_RSS_TABLE);
		 * db.execSQL(CREATE_WORLD_RSS_TABLE);
		 * db.execSQL(CREATE_RSS_NEW_ADD_TABLE);
		 * db.execSQL(CREATE_FAVOURITES_TABLE); onCreate(db); */
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		/* db.execSQL("DROP TABLE IF EXISTS " + TABLE_INDIA_RSS);
		 * db.execSQL("DROP TABLE IF EXISTS " + TABLE_WORLD_RSS);
		 * db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVOURITES);
		 * db.execSQL("DROP TABLE IF EXISTS " + TABLE_RSS_NEW_ADD); // Create
		 * tables again onCreate(db); */
	}

	/**
	 * Adding a new website in websites table Function will check if a site
	 * already existed in database. If existed will update the old one else
	 * creates a new row
	 * */
	public void addSite(WebSite site) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(KEY_TITLE, site.getTitle());
		values.put(KEY_LINK, site.getLink());
		values.put(KEY_RSS_LINK, site.getRSSLink());
		values.put(KEY_DESCRIPTION, site.getDescription());
		if (!isRSSTabSiteAdded(db, site.getRSSLink())) {
			db.insert(TABLE_RSS_NEW_ADD, null, values);
			db.close();
		} else { 
			//Toast.makeText(rssContext, " This site may already exist in RSS Feeds. ", Toast.LENGTH_SHORT).show();
			updateAddSite(site);
			db.close();
		}
	}

	/**
	 * Reading all rows from database
	 * */
	public List<WebSite> getAllSites() {
		List<WebSite> siteList = new ArrayList<WebSite>();
		SQLiteDatabase db = this.getReadableDatabase();
		String selectIndiaQuery = "SELECT  * FROM " + TABLE_INDIA_RSS
				+ " ORDER BY id";

		Cursor cursorIndia = db.rawQuery(selectIndiaQuery, null);
		if (cursorIndia.moveToFirst()) {
			do {
				WebSite site = new WebSite();
				site.setId(Integer.parseInt(cursorIndia.getString(0)));
				site.setTitle(cursorIndia.getString(1));
				site.setLink(cursorIndia.getString(2));
				site.setRSSLink(cursorIndia.getString(3));
				site.setDescription(cursorIndia.getString(4));
				siteList.add(site);
			} while (cursorIndia.moveToNext());
		}
		cursorIndia.close();
		db.close();
		return siteList;
	}

	public List<WebSite> getWorldSites() {
		List<WebSite> siteLists = new ArrayList<WebSite>();
		SQLiteDatabase db = this.getReadableDatabase();
		String selectWorldQuery = "SELECT  * FROM " + TABLE_WORLD_RSS
				+ " ORDER BY id";

		Cursor cursorWorld = db.rawQuery(selectWorldQuery, null);
		if (cursorWorld.moveToFirst()) {
			do {
				WebSite site = new WebSite();
				site.setId(Integer.parseInt(cursorWorld.getString(0)));
				site.setTitle(cursorWorld.getString(1));
				site.setLink(cursorWorld.getString(2));
				site.setRSSLink(cursorWorld.getString(3));
				site.setDescription(cursorWorld.getString(4));
				siteLists.add(site);
			} while (cursorWorld.moveToNext());
		}
		cursorWorld.close();
		db.close();
		return siteLists;
	}

	public List<WebSite> getFavSites() {
		List<WebSite> siteLists = new ArrayList<WebSite>();
		SQLiteDatabase db = this.getReadableDatabase();
		String selectFavQuery = "SELECT  * FROM " + TABLE_FAVOURITES
				+ " ORDER BY id DESC";

		Cursor cursorFav = db.rawQuery(selectFavQuery, null);
		if (cursorFav.moveToFirst()) {
			do {
				WebSite site = new WebSite();
				site.setId(Integer.parseInt(cursorFav.getString(0)));
				site.setTitle(cursorFav.getString(1));
				site.setLink(cursorFav.getString(2));
				site.setRSSLink(cursorFav.getString(3));
				site.setDescription(cursorFav.getString(4));
				siteLists.add(site);
			} while (cursorFav.moveToNext());
		}
		cursorFav.close();
		db.close();
		return siteLists;
	}
	
	public List<WebSite> getRSSTabSites() {
		List<WebSite> siteLists = new ArrayList<WebSite>();
		SQLiteDatabase db = this.getReadableDatabase();
		String selectWorldQuery = "SELECT  * FROM " + TABLE_RSS_NEW_ADD
				+ " ORDER BY id desc";

		Cursor cursorrt = db.rawQuery(selectWorldQuery, null);
		if (cursorrt.moveToFirst()) {
			do {
				WebSite site = new WebSite();
				site.setId(Integer.parseInt(cursorrt.getString(0)));
				site.setTitle(cursorrt.getString(1));
				site.setLink(cursorrt.getString(2));
				site.setRSSLink(cursorrt.getString(3));
				site.setDescription(cursorrt.getString(4));
				siteLists.add(site);
			} while (cursorrt.moveToNext());
		}
		cursorrt.close();
		db.close();
		return siteLists;
	}

	/**
	 * Updating a single row row will be identified by rss link
	 * */
	public int updateSite(WebSite site) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(KEY_TITLE, site.getTitle());
		values.put(KEY_LINK, site.getLink());
		values.put(KEY_RSS_LINK, site.getRSSLink());
		values.put(KEY_DESCRIPTION, site.getDescription());
		int update = db.update(
				TABLE_FAVOURITES,
				values, KEY_TITLE + " = ?",
				new String[] { String.valueOf(site.getTitle()) });
		db.close();
		return update;
	}
	
	public int updateAddSite(WebSite site) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(KEY_TITLE, site.getTitle());
		values.put(KEY_LINK, site.getLink());
		values.put(KEY_RSS_LINK, site.getRSSLink());
		values.put(KEY_DESCRIPTION, site.getDescription());
		int update = db.update(
				TABLE_RSS_NEW_ADD,
				values, KEY_RSS_LINK + " = ?",
				new String[] { String.valueOf(site.getRSSLink()) });
		db.close();
		return update;
	}

	/**
	 * Reading a row (website) row is identified by row id
	 * */
	public WebSite getSite(int id) {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.query(
				TABLE_INDIA_RSS,
				new String[] { KEY_ID, KEY_TITLE, KEY_LINK, KEY_RSS_LINK,
						KEY_DESCRIPTION }, KEY_ID + "=?",
				new String[] { String.valueOf(id) }, null, null, null, null);
		if (cursor != null)
			cursor.moveToFirst();

		WebSite site = new WebSite(cursor.getString(1), cursor.getString(2),
				cursor.getString(3), cursor.getString(4));
		site.setId(Integer.parseInt(cursor.getString(0)));
		site.setTitle(cursor.getString(1));
		site.setLink(cursor.getString(2));
		site.setRSSLink(cursor.getString(3));
		site.setDescription(cursor.getString(4));
		cursor.close();
		db.close();
		return site;
	}

	public WebSite getWorldSite(int id) {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursorworld = db.query(TABLE_WORLD_RSS, new String[] { KEY_ID,
				KEY_TITLE, KEY_LINK, KEY_RSS_LINK, KEY_DESCRIPTION }, KEY_ID
				+ "=?", new String[] { String.valueOf(id) }, null, null, null,
				null);
		if (cursorworld != null)
			cursorworld.moveToFirst();

		WebSite site = new WebSite(cursorworld.getString(1),
				cursorworld.getString(2), cursorworld.getString(3),
				cursorworld.getString(4));
		site.setId(Integer.parseInt(cursorworld.getString(0)));
		site.setTitle(cursorworld.getString(1));
		site.setLink(cursorworld.getString(2));
		site.setRSSLink(cursorworld.getString(3));
		site.setDescription(cursorworld.getString(4));
		cursorworld.close();
		db.close();
		return site;
	}

	public WebSite getFavSite(int id) {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursorfav = db.query(TABLE_FAVOURITES, new String[] { KEY_ID,
				KEY_TITLE, KEY_LINK, KEY_RSS_LINK, KEY_DESCRIPTION }, KEY_ID
				+ "=?", new String[] { String.valueOf(id) }, null, null, null,
				null);
		if (cursorfav != null)
			cursorfav.moveToFirst();

		WebSite site = new WebSite(cursorfav.getString(1),
				cursorfav.getString(2), cursorfav.getString(3),
				cursorfav.getString(4));
		site.setId(Integer.parseInt(cursorfav.getString(0)));
		site.setTitle(cursorfav.getString(1));
		site.setLink(cursorfav.getString(2));
		site.setRSSLink(cursorfav.getString(3));
		site.setDescription(cursorfav.getString(4));
		cursorfav.close();
		db.close();
		return site;
	}
	
	public WebSite getRSSTabSite(int id) {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursorrt = db.query(TABLE_RSS_NEW_ADD, new String[] { KEY_ID,
				KEY_TITLE, KEY_LINK, KEY_RSS_LINK, KEY_DESCRIPTION }, KEY_ID
				+ "=?", new String[] { String.valueOf(id) }, null, null, null,
				null);
		if (cursorrt != null)
			cursorrt.moveToFirst();

		WebSite site = new WebSite(cursorrt.getString(1),
				cursorrt.getString(2), cursorrt.getString(3),
				cursorrt.getString(4));

		site.setId(Integer.parseInt(cursorrt.getString(0)));
		site.setTitle(cursorrt.getString(1));
		site.setLink(cursorrt.getString(2));
		site.setRSSLink(cursorrt.getString(3));
		site.setDescription(cursorrt.getString(4));
		cursorrt.close();
		db.close();
		return site;
	}

	/**
	 * Deleting single row
	 * */
	public void deleteSite(WebSite site) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_INDIA_RSS,
				KEY_ID + " = ?", new String[] { String.valueOf(site.getId()) });
		db.close();
	}

	public void deleteWorldSite(WebSite site) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_WORLD_RSS, KEY_ID + " = ?",
				new String[] { String.valueOf(site.getId()) });
		db.close();
	}
	public void deleteFavSite(WebSite site) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_FAVOURITES, KEY_ID + " = ?",
				new String[] { String.valueOf(site.getId()) });
		db.close();
	}
	public void deleteRSSTabSite(WebSite site) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_RSS_NEW_ADD, KEY_ID + " = ?",
				new String[] { String.valueOf(site.getId()) });
		db.close();
	}
	
	public WebSite favIndiaSite(int id, WebSite site) {
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.query(
				TABLE_INDIA_RSS,
				new String[] { KEY_ID, KEY_TITLE, KEY_LINK, KEY_RSS_LINK,
						KEY_DESCRIPTION }, KEY_ID + "=?",
				new String[] { String.valueOf(id) }, null, null, null, null);
		if (cursor != null)
			cursor.moveToFirst();
		
		String title=cursor.getString(1);
		ContentValues values = new ContentValues();
		values.put(KEY_ID, Integer.parseInt(cursor.getString(0)));
		values.put(KEY_TITLE, title);
		values.put(KEY_LINK, cursor.getString(2));
		values.put(KEY_RSS_LINK, cursor.getString(3));
		values.put(KEY_DESCRIPTION, cursor.getString(4));

		if (!isSiteExists(db, title)) {
			db.insert(TABLE_FAVOURITES, null, values);
			cursor.close();
			db.close();
		} else {
			Log.d("exist", "This site already exist in Favourites");
			Toast.makeText(rssContext, " This site already exist in Favourites ", Toast.LENGTH_SHORT).show();
			updateSite(site);
			cursor.close();
			db.close();
		}
		return site;
	}

	public WebSite favWorldSite(int id, WebSite site) {
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.query(
				TABLE_WORLD_RSS,
				new String[] { KEY_ID, KEY_TITLE, KEY_LINK, KEY_RSS_LINK,
						KEY_DESCRIPTION }, KEY_ID + "=?",
				new String[] { String.valueOf(id) }, null, null, null, null);
		if (cursor != null)
			cursor.moveToFirst();
		
		int worldid= Integer.parseInt(cursor.getString(0))+500;
		String title=cursor.getString(1);
		ContentValues values = new ContentValues();
		values.put(KEY_ID, worldid);
		values.put(KEY_TITLE, cursor.getString(1));
		values.put(KEY_LINK, cursor.getString(2));
		values.put(KEY_RSS_LINK, cursor.getString(3));
		values.put(KEY_DESCRIPTION, cursor.getString(4));

		if (!isWorldSiteExists(db, title)) {
			db.insert(TABLE_FAVOURITES, null, values);
			cursor.close();
			db.close();
		} else {
			Log.d("exist", "This site already exist in Favourites");
			Toast.makeText(rssContext, " This site already exist in Favourites ", Toast.LENGTH_SHORT).show();
			updateSite(site);
			cursor.close();
			db.close();
		}
		return site;
	}
	
	public WebSite favRSSTabSite(int id, WebSite site) {
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.query(TABLE_RSS_NEW_ADD,
				new String[] { KEY_ID, KEY_TITLE, KEY_LINK, KEY_RSS_LINK,
						KEY_DESCRIPTION }, KEY_ID + "=?",
				new String[] { String.valueOf(id) }, null, null, null, null);
		if (cursor != null)
			cursor.moveToFirst();
		
		int rssid= Integer.parseInt(cursor.getString(0))+1000;
		String title=cursor.getString(1);
		ContentValues values = new ContentValues();
		values.put(KEY_ID, rssid);
		values.put(KEY_TITLE, cursor.getString(1));
		values.put(KEY_LINK, cursor.getString(2));
		values.put(KEY_RSS_LINK, cursor.getString(3));
		values.put(KEY_DESCRIPTION, cursor.getString(4));

		if (!isRSSTabSiteExists(db, title)) {
			db.insert(TABLE_FAVOURITES, null, values);
			cursor.close();
			db.close();
		} else {
			Log.d("exist", "This site already exist in Favourites");
			Toast.makeText(rssContext, " This site already exist in Favourites ", Toast.LENGTH_SHORT).show();
			updateSite(site);
			cursor.close();
			db.close();
		}
		return site;
	}

	/**
	 * Checking whether a site is already existed check is done by matching rss
	 * link
	 * */
	public boolean isSiteExists(SQLiteDatabase db, String title) {
		Cursor cursor = db.rawQuery("SELECT 1 FROM " + TABLE_INDIA_RSS
				+ " INNER JOIN "+TABLE_FAVOURITES +" ON "+ TABLE_INDIA_RSS+".title="+TABLE_FAVOURITES+".title WHERE "+TABLE_INDIA_RSS+".title = '" + title + "'", new String[] {});
		boolean exists = (cursor.getCount() > 0);
		cursor.close();
		return exists;
	}

	public boolean isWorldSiteExists(SQLiteDatabase db, String title) {
		Cursor cursor = db.rawQuery("SELECT 1 FROM " + TABLE_WORLD_RSS
				+ " INNER JOIN "+TABLE_FAVOURITES +" ON "+ TABLE_WORLD_RSS+".title="+TABLE_FAVOURITES+".title WHERE "+TABLE_WORLD_RSS+".title = '" + title + "'", new String[] {});
		boolean exists = (cursor.getCount() > 0);
		return exists;
	}
	
	public boolean isRSSTabSiteExists(SQLiteDatabase db, String title) {
		Cursor cursor = db.rawQuery("SELECT 1 FROM " + TABLE_RSS_NEW_ADD
				+ " INNER JOIN "+TABLE_FAVOURITES +" ON "+ TABLE_RSS_NEW_ADD+".title="+TABLE_FAVOURITES+".title WHERE "+TABLE_RSS_NEW_ADD+".title = '" + title + "'", new String[] {});
		boolean exists = (cursor.getCount() > 0);
		cursor.close();
		return exists;
	}
	public boolean isRSSTabSiteAdded(SQLiteDatabase db, String rss_link) {
		Cursor cursor = db.rawQuery("SELECT 1 FROM " + TABLE_RSS_NEW_ADD
				+ " WHERE rss_link = '" + rss_link + "'", new String[] {});
		boolean exists = (cursor.getCount() > 0);
		return exists;

	}
}