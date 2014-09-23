package com.example.superapka;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.SurfaceHolder;

class MainView extends GLSurfaceView {
	  MainRenderer mRenderer;
	 
	  MainView ( Context context ) {
	    super ( context );
	    mRenderer = new MainRenderer(this);
	    setEGLContextClientVersion ( 2 );
	    setRenderer ( mRenderer );
	    setRenderMode ( GLSurfaceView.RENDERMODE_WHEN_DIRTY );
	  }
	 
	  public void surfaceCreated ( SurfaceHolder holder ) {
	    super.surfaceCreated ( holder );
	  }
	 
	  public void surfaceDestroyed ( SurfaceHolder holder ) {
	    mRenderer.close();
	    super.surfaceDestroyed ( holder );
	  }
	 
	  public void surfaceChanged ( SurfaceHolder holder, int format, int w, int h ) {
	    super.surfaceChanged ( holder, format, w, h );
	  }
	}