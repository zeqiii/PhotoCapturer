package com.example.photocapturer;

import java.io.File;
import java.io.FileOutputStream;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.RelativeLayout;

public class PhotoCapService extends Service {

	public static final String ACTION = "my.test.service";

	private WindowManager wm;
	private LayoutParams param;
	private SurfaceView sView;
	private SurfaceHolder sHolder;
	private Camera mCamera;
	private RelativeLayout rLayout;
	private boolean isPreview = false;
	private String path =
	Environment.getExternalStorageDirectory().getPath() +
	"/secretPic/";

	PictureCallback jpegCallback = new PictureCallback() {
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			final Bitmap bm = BitmapFactory.decodeByteArray(data, 0,
					data.length);
			File dir = new File(path);
			if(!dir.exists())
				dir.mkdirs();
			
			File file = new File(dir, String.valueOf(System.currentTimeMillis()) + ".jpg");
			Log.d("path", path);
			try {
				if (!file.exists())
					file.createNewFile();
				FileOutputStream out = new FileOutputStream(file);
				bm.compress(CompressFormat.JPEG, 100, out);
				out.close();
				Log.d("taken", "filename:" + file.getAbsolutePath());
			} catch (Exception e) {
				e.printStackTrace();
			}
			camera.stopPreview();
			camera.startPreview();
			isPreview = true;
		}
	};

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		init();
	}

	private void init() {
		// init window manager
		wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
		// init parameter
		param = new WindowManager.LayoutParams();
		param.format = PixelFormat.RGBA_8888;
		param.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
		param.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
				| WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
				| WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
		param.width = 1;
		param.height = 1;
		// init rLayout
		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		rLayout = (RelativeLayout) inflater.inflate(R.layout.mylayout, null);
		// init sView
		sView = new SurfaceView(rLayout.getContext());
		// init sHolder
		sHolder = sView.getHolder();
	}

	private void initCamera() {
		if (!isPreview) {
			mCamera = Camera.open();
		}
		if (mCamera != null && !isPreview) {
			try {
				Camera.Parameters parameters = mCamera.getParameters();
				mCamera.setParameters(parameters);
				mCamera.setPreviewDisplay(sHolder);
				mCamera.startPreview();
				mCamera.autoFocus(null);
			} catch (Exception e) {
				e.printStackTrace();
			}
			isPreview = true;
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		rLayout.addView(sView);
		wm.addView(rLayout, param);
		sHolder.addCallback(new Callback() {
			@Override
			public void surfaceChanged(SurfaceHolder holder, int format,
					int width, int height) {
			}

			@Override
			public void surfaceCreated(SurfaceHolder holder) {
				initCamera();
			}

			@Override
			public void surfaceDestroyed(SurfaceHolder holder) {
				if (mCamera != null) {
					if (isPreview)
						mCamera.stopPreview();
					mCamera.release();
					mCamera = null;
				}
			}
		});
		new Thread() {
			@Override
			public void run() {
				try {
					while (true) {
						Thread.sleep(5000);
						if (mCamera != null && isPreview) {
							mCamera.takePicture(null, null, jpegCallback);
						}
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		}.start();
		return super.onStartCommand(intent, flags, startId);
	}
}
