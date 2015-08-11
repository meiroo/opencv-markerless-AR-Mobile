package meiroo.cvgl;

import javax.microedition.khronos.opengles.GL10;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.imgproc.Imgproc;
import meiroo.cvgl.R;

import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.EGLConfig;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.opengl.GLSurfaceView;

public class CVGLActivity extends Activity implements CvCameraViewListener2 {
    private static final String    TAG = "OCVSample::Activity";

    private static final int       VIEW_MODE_RGBA     = 0;
    private static final int       VIEW_MODE_GRAY     = 1;
    private static final int       VIEW_MODE_CANNY    = 2;
    private static final int       VIEW_MODE_FEATURES = 5;

    private int                    mViewMode;
    private Mat                    mRgba;
    private Mat                    mIntermediateMat;
    private Mat                    mGray;

    private MenuItem               mItemPreviewRGBA;
    private MenuItem               mItemPreviewGray;
    private MenuItem               mItemPreviewCanny;
    private MenuItem               mItemPreviewFeatures;

    private CameraBridgeViewBase   mOpenCvCameraView;
    
    public long time = 0;
    public static int result = 0;

    private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");

                    // Load native library after(!) OpenCV initialization
                    System.loadLibrary("nonfree");
                    System.loadLibrary("cvgl");

                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public CVGLActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.tutorial2_surface_view);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.tutorial2_activity_surface_view);
        mOpenCvCameraView.setCvCameraViewListener(this);
        
        //Utils.CopyAssets(getResources().getAssets(), Environment.getExternalStorageDirectory().getPath(), "CVGL");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "called onCreateOptionsMenu");
        mItemPreviewRGBA = menu.add("Preview RGBA");
        mItemPreviewGray = menu.add("Preview GRAY");
        mItemPreviewCanny = menu.add("Canny");
        mItemPreviewFeatures = menu.add("Find features");
        return true;
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mIntermediateMat = new Mat(height, width, CvType.CV_8UC4);
        mGray = new Mat(height, width, CvType.CV_8UC1);
    }

    public void onCameraViewStopped() {
        mRgba.release();
        mGray.release();
        mIntermediateMat.release();
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        final int viewMode = mViewMode;
        switch (viewMode) {
        case VIEW_MODE_GRAY:
            // input frame has gray scale format
            Imgproc.cvtColor(inputFrame.gray(), mRgba, Imgproc.COLOR_GRAY2RGBA, 4);
            break;
        case VIEW_MODE_RGBA:
            // input frame has RBGA format
            mRgba = inputFrame.rgba();
            break;
        case VIEW_MODE_CANNY:
            // input frame has gray scale format
            mRgba = inputFrame.rgba();
            Imgproc.Canny(inputFrame.gray(), mIntermediateMat, 80, 100);
            Imgproc.cvtColor(mIntermediateMat, mRgba, Imgproc.COLOR_GRAY2RGBA, 4);
            break;
        case VIEW_MODE_FEATURES:
            // input frame has RGBA format
            mRgba = inputFrame.rgba();
            mGray = inputFrame.gray();
            
            

			if (SystemClock.uptimeMillis() - time >= 1000) {
				time = SystemClock.uptimeMillis();
				result = native_FindFeatures(mGray.getNativeObjAddr(), mRgba.getNativeObjAddr());
				//Log.i("GLAndroid","recog result = " + result);
			}
            
            break;
        }

        return mRgba;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);

        if (item == mItemPreviewRGBA) {
            mViewMode = VIEW_MODE_RGBA;
        } else if (item == mItemPreviewGray) {
            mViewMode = VIEW_MODE_GRAY;
        } else if (item == mItemPreviewCanny) {
            mViewMode = VIEW_MODE_CANNY;
        } else if (item == mItemPreviewFeatures) {
            mViewMode = VIEW_MODE_FEATURES;
        }

        return true;
    }
    static native int native_FindFeatures(long matAddrGr, long matAddrRgba);
    static native void native_start();
	static native void native_gl_resize(int w, int h);
	static native void native_gl_render();
	static native void native_key_event(int key, int status);
	static native void native_touch_event(float x, float y, int status);
}


class GlBufferView extends GLSurfaceView {
	 private static String TAG = "GLAndroid";

	public GlBufferView(Context context, AttributeSet attrs) {
		super(context, attrs);
		getHolder().setFormat(PixelFormat.TRANSLUCENT);
		setEGLConfigChooser(8, 8, 8, 8, 16, 0);

        /* We need to choose an EGLConfig that matches the format of
         * our surface exactly. This is going to be done in our
         * custom config chooser. See ConfigChooser class definition
         * below.
         */
        
		
		//setZOrderOnTop(true);
		setRenderer(new MyRenderer());
		/*
		requestFocus();
		setFocusableInTouchMode(true);
		*/
	}
	@Override
    public boolean onTouchEvent(final MotionEvent event)
	{
		queueEvent(new Runnable() {
			public void run() {
				CVGLActivity.native_touch_event(event.getX(), event.getY(), event.getAction());
			}
		});
		
		return true;
	}
	
	@Override
	public boolean onKeyDown(final int keyCode, final KeyEvent event)
	{		
		queueEvent(new Runnable() {
			public void run() {
				CVGLActivity.native_key_event(keyCode, event.getAction());
			}
		});
		return false;
	}
	
	@Override
	public boolean onKeyUp(final int keyCode, final KeyEvent event)
	{
		queueEvent(new Runnable() {
			public void run() {
				CVGLActivity.native_key_event(keyCode, event.getAction());
			}
		});
		
		return false;
	}

	class MyRenderer implements GLSurfaceView.Renderer {
		
		@Override
		public void onSurfaceCreated(GL10 gl, javax.microedition.khronos.egl.EGLConfig arg1) { 
			/* do nothing */ 
			CVGLActivity.native_start();
		}
		
		
		public void onSurfaceChanged(GL10 gl, int w, int h) {
			CVGLActivity.native_gl_resize(w, h);
		}

		public void onDrawFrame(GL10 gl) {
			time = SystemClock.uptimeMillis();

			if (time >= (frameTime + 1000.0f)) {
				frameTime = time;
				avgFPS += framerate;
				framerate = 0;
			}

			if (time >= (fpsTime + 3000.0f)) {
				fpsTime = time;
				avgFPS /= 3.0f;
				//Log.d("GLAndroid", "FPS: " + Float.toString(avgFPS));
				avgFPS = 0;
			}
			framerate++;
			
			if(CVGLActivity.result > 0){
				CVGLActivity.native_gl_render();
			}else{
				gl.glClearColor(0,0,0,0);
				gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
			}
		}
		public long time = 0;
		public short framerate = 0;
		public long fpsTime = 0;
		public long frameTime = 0;
		public float avgFPS = 0;
	}
}

