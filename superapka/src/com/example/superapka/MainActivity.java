package com.example.superapka;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.Window;
import android.view.WindowManager;

public class MainActivity extends Activity {
	  private MainView mView;
	  private WakeLock mWL;

	  @Override
	  public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    // full screen & full brightness
	    requestWindowFeature ( Window.FEATURE_NO_TITLE);
	    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
	    getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	    mWL = ((PowerManager)getSystemService ( Context.POWER_SERVICE )).newWakeLock(PowerManager.FULL_WAKE_LOCK, "WakeLock");
	    mWL.acquire();
	    mView = new MainView(this);
	    setContentView ( mView );
	  }
	  
	  @Override
	  protected void onPause() {
	    if ( mWL.isHeld() )
	      mWL.release();
	    mView.onPause();
	    super.onPause();
	  }
	    
	  @Override
	  protected void onResume() {
	    super.onResume();
	    mView.onResume();
	    mWL.acquire();
	  }
	}