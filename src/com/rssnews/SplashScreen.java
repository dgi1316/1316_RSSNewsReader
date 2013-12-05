package com.rssnews;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

public class SplashScreen extends Activity {
	
	Boolean isInternetPresent = false;

	public boolean isConnectingToInternet(){
        ConnectivityManager connectivity = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
          if (connectivity != null)
          {
              NetworkInfo[] info = connectivity.getAllNetworkInfo();
              if (info != null)
                  for (int i = 0; i < info.length; i++)
                      if (info[i].getState() == NetworkInfo.State.CONNECTED)
                      {
                          return true;
                      }
 
          }
          return false;
    }
	public void showAlertDialog(String title, String message, Boolean status) {
		AlertDialog alertDialog = new AlertDialog.Builder(this).create();
		alertDialog.setTitle(title);
		alertDialog.setMessage(message);
		alertDialog.setIcon((status) ? R.drawable.tick : R.drawable.error);
		alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {
		finish();}});
		alertDialog.show();
		}

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash_screen);

		isInternetPresent=isConnectingToInternet();
		if (!isInternetPresent) { //Internet Connection is not Present, tell user to make connection
            showAlertDialog("No Internet Connection",
                    "You don't have Internet Connection. Please Connect.", false);
		} else { /*Internet Connection is Present make HTTP requests
            //showAlertDialog("Internet Connection", "You have Internet connection. Please Proceed.", true);*/
        	Thread timer = new Thread() {
    			@Override
    			public void run() {
    				
    				try {
    					sleep(2000);
    				} catch (Exception e) {
    					e.printStackTrace();
    				} finally {
    					Intent iSplash = new Intent(SplashScreen.this,
    							RSSNewsReaderPBActivity.class);
    					startActivity(iSplash);
    				}
    			}
    		};
    		timer.start();
		}
	}
	@Override
	protected void onPause() {
		super.onPause();
		finish();

	}

}
