package com.example.superapka;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.graphics.AvoidXfermode.Mode;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.util.Log;
import android.view.SurfaceHolder;

public class MainRenderer implements GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener {
	  private final String vss =
	      "attribute vec2 vPosition;\n" +
	      "attribute vec2 vTexCoord;\n" +
	      "varying vec2 texCoord;\n" +
	      "void main() {\n" +
	      "  texCoord = vTexCoord;\n" +
	      "  gl_Position = vec4 ( vPosition.x, vPosition.y, 0.0, 1.0 );\n" +
	      "}";
	 
	  private final String fss =
	      "#extension GL_OES_EGL_image_external : require\n" +
	      "precision mediump float;\n" +
	      "uniform samplerExternalOES sTexture;\n" +
	      "varying vec2 texCoord;\n" +
	      "void main() {\n" +
	      "  gl_FragColor = texture2D(sTexture,texCoord);\n" +
	      "}";

	  private int[] hTex;
	  private FloatBuffer pVertex;
	  private FloatBuffer pTexCoord;
	  private int hProgram;
	 
	  private Camera mCamera;
	  private SurfaceTexture mSTexture;
	 
	  private boolean mUpdateST = false;
	 
	  private MainView mView;
	 
	  MainRenderer ( MainView view ) {
	    mView = view;
	    float[] vtmp = { 1.0f, -1.0f, -1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f };
	    float[] ttmp = { 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f };
	    pVertex = ByteBuffer.allocateDirect(8*4).order(ByteOrder.nativeOrder()).asFloatBuffer();
	    pVertex.put ( vtmp );
	    pVertex.position(0);
	    pTexCoord = ByteBuffer.allocateDirect(8*4).order(ByteOrder.nativeOrder()).asFloatBuffer();
	    pTexCoord.put ( ttmp );
	    pTexCoord.position(0);
	  }
	 
	  public void close()
	  {
	    mUpdateST = false;
	    mSTexture.release();
	    mCamera.stopPreview();
	    mCamera = null;
	    deleteTex();
	  }
	 
	  public void onSurfaceCreated ( GL10 unused, EGLConfig config ) 
	  {
	       
	    initTex();
	    mSTexture = new SurfaceTexture ( hTex[0] );
	    mSTexture.setOnFrameAvailableListener(this);
	    
	    mCamera = Camera.open();
	    try {
	      mCamera.setPreviewTexture(mSTexture);
	    } catch ( IOException ioe ) {
	    }
	    
	    GLES20.glClearColor ( 0f, 0f, 0f, 0f );
	    
	    hProgram = loadShader ( vss, fss );
	  }
	 
	  public void onDrawFrame ( GL10 unused ) {
		  

	    GLES20.glClear( GLES20.GL_COLOR_BUFFER_BIT );
		
	    synchronized(this) {
	      if ( mUpdateST ) {
	        mSTexture.updateTexImage();
	        mUpdateST = false;
	      }
	    }

	    GLES20.glUseProgram(hProgram);

	    int ph = GLES20.glGetAttribLocation(hProgram, "vPosition");
	    int tch = GLES20.glGetAttribLocation ( hProgram, "vTexCoord" );
	    int th = GLES20.glGetUniformLocation ( hProgram, "sTexture" );
	  
	    GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
	    GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, hTex[0]);
	    GLES20.glUniform1i(th, 0);
	  
	    GLES20.glVertexAttribPointer(ph, 2, GLES20.GL_FLOAT, false, 4*2, pVertex);
	    GLES20.glVertexAttribPointer(tch, 2, GLES20.GL_FLOAT, false, 4*2, pTexCoord );
	    GLES20.glEnableVertexAttribArray(ph);
	    GLES20.glEnableVertexAttribArray(tch);
	  
	    GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
	    GLES20.glFlush();
	  }
	  
	  private void drawCanvasToTexture(String aText, float aFontSize)
	  {
	  if (aFontSize < 8.0f)
	  aFontSize = 8.0f;
	  if (aFontSize > 500.0f)
	  aFontSize = 500.0f;

	  Paint textPaint = new Paint();
	  textPaint.setTextSize(aFontSize);
	  textPaint.setFakeBoldText(true);
	  textPaint.setAntiAlias(true);
	  textPaint.setARGB(255, 255, 255, 255);
	  textPaint.setSubpixelText(true); 
	 // textPaint.setXfermode(new PorterDuffXfermode(Mode.SCREEN));
	  // Set hinting if available
	  //textPaint.setHinting(Paint.HINTING_ON);

	  float realTextWidth = textPaint.measureText(aText);

	  //Create a new mutable bitmap
	//  bitmapWidth = (int)(realTextWidth + 2.0f);//text.length() * textSize;
	//  bitmapHeight = (int)aFontSize + 2;

	  Bitmap textBitmap = Bitmap.createBitmap(200, 400, Bitmap.Config.ARGB_8888);
	  textBitmap.eraseColor(Color.argb(0, 255, 255, 255));
	  //creates a new canvas that will draw into a bitmap instead of rendering into the screen  
	  Canvas bitmapCanvas = new Canvas(textBitmap);
	  // Set start drawing position to [1, base_line_position]
	  // The base_line_position may vary from one font to another but it usually is equal to 75% of font size (height).
	  bitmapCanvas.drawText(aText, 1, 1.0f + aFontSize * 0.75f, textPaint);

	  int[] textureId =new int[1];  
	GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId[0]);
	  // Assign the OpenGL texture with the Bitmap  
	  GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, textBitmap, 0);  
	  //free memory resources associated with this texture  
	  textBitmap.recycle();

	  // After the image has been subloaded to texture, regenerate mipmaps
	  GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
	  }
	  
	  public void onSurfaceChanged ( GL10 unused, int width, int height )
	  {
			if(mCamera != null) {
	    		Camera.Parameters param = mCamera.getParameters();
	    		
	    		 Camera.Size previewSize = param.getPreferredPreviewSizeForVideo();   // .... select one of previewSizes here

	    	        param.setPreviewSize(previewSize.width, previewSize.height);

	    mCamera.setParameters ( param );
	    mCamera.startPreview();
	    
			}
	  }
	 
	  private void initTex() {
	    hTex = new int[1];
	    GLES20.glGenTextures ( 1, hTex, 0 );
	    GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, hTex[0]);
	    GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
	    GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
	    GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
	    GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
	  }
	 
	  private void deleteTex() {
	    GLES20.glDeleteTextures ( 1, hTex, 0 );
	  }
	 
	  public synchronized void onFrameAvailable ( SurfaceTexture st ) {
	    mUpdateST = true;
	    mView.requestRender();
	  }
	 
	  private static int loadShader ( String vss, String fss ) {
	    int vshader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
	    GLES20.glShaderSource(vshader, vss);
	    GLES20.glCompileShader(vshader);
	    int[] compiled = new int[1];
	    GLES20.glGetShaderiv(vshader, GLES20.GL_COMPILE_STATUS, compiled, 0);
	    if (compiled[0] == 0) {
	      Log.e("Shader", "Could not compile vshader");
	      Log.v("Shader", "Could not compile vshader:"+GLES20.glGetShaderInfoLog(vshader));
	      GLES20.glDeleteShader(vshader);
	      vshader = 0;
	    }
	  
	    int fshader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
	    GLES20.glShaderSource(fshader, fss);
	    GLES20.glCompileShader(fshader);
	    GLES20.glGetShaderiv(fshader, GLES20.GL_COMPILE_STATUS, compiled, 0);
	    if (compiled[0] == 0) {
	      Log.e("Shader", "Could not compile fshader");
	      Log.v("Shader", "Could not compile fshader:"+GLES20.glGetShaderInfoLog(fshader));
	      GLES20.glDeleteShader(fshader);
	      fshader = 0;
	    }

	    int program = GLES20.glCreateProgram();
	    GLES20.glAttachShader(program, vshader);
	    GLES20.glAttachShader(program, fshader);
	    GLES20.glLinkProgram(program);
	        
	    return program;
	  }
	  
	
	   public void surfaceDestroyed(SurfaceHolder holder) {
	       if (mCamera != null) {
	    	   mCamera.stopPreview();
	    	   mCamera.setPreviewCallback(null);
	    	   mCamera.release();
	    	   mCamera = null;
	       }
	   }

	   public void surfaceCreated(SurfaceHolder holder) {

	        try {
	            if (mCamera != null) {
	                mCamera.setPreviewDisplay(holder);
	            }
	        } catch (IOException exception) {
	            Log.e("TAG", "IOException caused by setPreviewDisplay()", exception);
	        }
	    }
	}