package com.fccaikai.customcamera;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.ShutterCallback;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

public class MainActivity extends Activity implements
		android.hardware.Camera.AutoFocusCallback {

	private CameraView cv = null;// 继承surfaceView的自定义view 用于存放照相的图片
	private Camera camera; // 声明照相机
	private Button cameraPhoto; // 照相
	private Button cameraExit; // 关闭
	private boolean isPreview = false;
	private LinearLayout linearLayoutCamera;
	private LinearLayout linearLayoutImages;
	private ImageView focusview = null;
	int i = 0; // 删除按钮tag值，从0开始
	private String cameraPath;
	private List<String> listPath = new ArrayList<String>();
	private Button light_on; // 开启闪关灯
	private Button light_off; // 关闭闪关灯
	private boolean isTorch = false; // 是否开启闪关灯
	private boolean isCantaken = false; // 是否开启闪关灯
	private SeekBar seekBar; // 焦距
	private boolean takenphoto = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.check_camera);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // 屏幕长亮

		linearLayoutCamera = (LinearLayout) findViewById(R.id.preciew);
		linearLayoutImages = (LinearLayout) findViewById(R.id.linearlayout_images);
		cameraPhoto = (Button) findViewById(R.id.camera_photo);
		cameraExit = (Button) findViewById(R.id.camera_exit);
		focusview = (ImageView) findViewById(R.id.focus_view);

		light_on = (Button) findViewById(R.id.light_on);
		light_off = (Button) findViewById(R.id.light_off);

		seekBar = (SeekBar) this.findViewById(R.id.seekBar);
		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub

			}

			@SuppressLint("NewApi")
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				// TODO Auto-generated method stub
				Parameters p = camera.getParameters();
				p.setZoom(progress);
				camera.setParameters(p);
			}
		});
		cameraExit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		cameraPhoto.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isCantaken == false) {
					return;
				}
				isCantaken = false;
				Camera.Parameters parameters = camera.getParameters();
				if (isTorch == true)
					parameters.setFlashMode(parameters.FLASH_MODE_TORCH);
				camera.setParameters(parameters);
				camera.autoFocus(MainActivity.this);
				takenphoto = true;

			}
		});

		light_on.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				light_on.setVisibility(View.GONE);
				light_off.setVisibility(View.VISIBLE);
				isTorch = false;
				Toast.makeText(getApplicationContext(), "关闭闪光灯",
						Toast.LENGTH_SHORT).show();
			}
		});
		light_off.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				light_on.setVisibility(View.VISIBLE);
				light_off.setVisibility(View.GONE);
				isTorch = true;
				Toast.makeText(getApplicationContext(), "开启闪光灯",
						Toast.LENGTH_SHORT).show();
			}
		});
		// 打开相机
		openCamera();
	}

	// 主要的surfaceView，负责展示预览图片，camera的开关
	class CameraView extends SurfaceView {

		private SurfaceHolder holder = null;

		public CameraView(Context context) {
			super(context);
			holder = this.getHolder();
			holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
			holder.addCallback(new SurfaceHolder.Callback() {

				@SuppressLint("NewApi")
				@Override
				public void surfaceCreated(SurfaceHolder holder) {
					camera = Camera.open();
					try {
						camera.setDisplayOrientation(0);
						camera.setPreviewDisplay(holder);
					} catch (IOException e) {
						camera.release();
						camera = null;
						e.printStackTrace();
					}
				}

				@Override
				public void surfaceChanged(SurfaceHolder holder, int format,
						int width, int height) {
					initCamera();
				}

				@Override
				public void surfaceDestroyed(SurfaceHolder holder) {
					camera.stopPreview();
					camera.release();
					camera = null;
				}

			});
		}
	}

	// 初始化相机
	@SuppressLint("NewApi")
	public void initCamera() {
		if (isPreview) {
			camera.stopPreview();
		}
		if (null != camera) {
			Camera.Parameters myParam = camera.getParameters();

			myParam.setPictureFormat(PixelFormat.JPEG);// 设置拍照后存储的图片格式
			// 设置大小和方向等参数
			// height width
			// 2400 3200 0
			// 1944 2592 1
			// 1440 2560 2
			// 1536 2048 3
			// 1088 1920 4
			// 1200 1600 5
			// 768 1280 6
			// 720 1280 7
			// 768 1024 8
			// 600 800 9
			// 480 800 10
			// 480 640 11
			// 288 352 12
			// 240 320 13
			// 144 176 14
			myParam.setPictureSize(
					myParam.getSupportedPictureSizes().get(6).width, myParam
							.getSupportedPictureSizes().get(6).height);
			myParam.set("jpeg-quality", 100);
			// myParam.set("rotation", 90);
			// List<Size> sizes =
			camera.setDisplayOrientation(90);
			myParam.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
			try {
				camera.setParameters(myParam);
			} catch (Exception e) {

			}

			camera.startPreview();
			camera.autoFocus(MainActivity.this);
			isPreview = true;
		}
	}

	@Override
	public void onAutoFocus(boolean arg0, Camera arg1) {
		// TODO Auto-generated method stub
		if (arg0) {
			MediaPlayer.create(MainActivity.this, R.raw.plugin_vf_focus_ok)
					.start();
			focusview.setBackgroundResource(R.drawable.ic_focus_focused);
			if (takenphoto == true) {
				camera.takePicture(shutter, null, picture);
			}
		} else {

		}
	}

	private void openCamera() {
		linearLayoutCamera.removeAllViews();
		cv = new CameraView(MainActivity.this);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT);
		linearLayoutCamera.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					int x = (int) event.getX();
					int y = (int) event.getY();
					focusview.layout(x - 100, y - 100, x + 100, y + 100);
					focusview.setVisibility(View.VISIBLE);
					focusview
							.setBackgroundResource(R.drawable.ic_focus_focusing);
					focusview.postInvalidate();

					camera.autoFocus(MainActivity.this);
					break;
				}
				return false;
			}
		});
		linearLayoutCamera.addView(cv, params);
		isCantaken = true;
		takenphoto = false;
	}

	// 回调用的picture，实现里边的onPictureTaken方法，其中byte[]数组即为照相后获取到的图片信息
	private Camera.PictureCallback picture = new Camera.PictureCallback() {

		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			Bitmap mBitmap = null;
			if (null != data) {
				mBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);// data是字节数据，将其解析成位图
				camera.stopPreview();
			}
			// 设置FOCUS_MODE_CONTINUOUS_VIDEO)之后，myParam.set("rotation",
			// 90)失效。图片竟然不能旋转了，故这里要旋转下
			Matrix matrix = new Matrix();
			matrix.postRotate((float) 90.0);
			Bitmap rotaBitmap = Bitmap.createBitmap(mBitmap, 0, 0,
					mBitmap.getWidth(), mBitmap.getHeight(), matrix, false);

			new SavePicThread().execute(rotaBitmap);
		}
	};

	class SavePicThread extends AsyncTask<Bitmap, Void, Void> {

		@Override
		protected Void doInBackground(Bitmap... params) {
			// TODO Auto-generated method stub
			if (params[0] != null) {
				saveJpeg(params[0]);
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			getImageView(cameraPath);
			openCamera();// 重新打开相机
		}
	}

	Camera.ShutterCallback shutter = new ShutterCallback() {

		@Override
		public void onShutter() {
			// TODO Auto-generated method stub
			MediaPlayer.create(MainActivity.this, R.raw.camera2).start();
		}
	};

	private void getImageView(String path) {
		final View view = getLayoutInflater().inflate(R.layout.xxx, null);
		final ImageView imageView = (ImageView) view
				.findViewById(R.id.photoshare_item_image);
		try {
			imageView.setImageBitmap(getImageBitmap(path));
			imageView.setTag(path);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		imageView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// setCurrentIndex((String) imageView.getTag());
			}

		});
		linearLayoutImages.addView(view);
		listPath.add(path);
	}

	private Bitmap getImageBitmap(String path) throws FileNotFoundException,
			IOException {
		Bitmap bmp = null;
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, opts);
		opts.inSampleSize = ImageTools.computeSampleSize(opts, -1, 150 * 150);
		opts.inJustDecodeBounds = false;
		try {
			bmp = BitmapFactory.decodeFile(path, opts);
		} catch (OutOfMemoryError e) {
		}
		return bmp;
	}

	/* 给定一个Bitmap，进行保存 */
	public void saveJpeg(Bitmap bm) {
		String savePath = getSDPath() + "/customscamera";
		File folder = new File(savePath);
		if (!folder.exists()) // 如果文件夹不存在则创建
		{
			folder.mkdirs();
		}
		long dataTake = System.currentTimeMillis();

		// 产生随机四位数
		String s = "";
		while (s.length() < 4)
			s += (int) (Math.random() * 10);

		String jpegName = savePath + "/" + dataTake + "_" + s + ".jpg";
		cameraPath = jpegName;

		// Bitmap bitmap = compressImage(bm);
		Bitmap bitmap = createBitmap(bm);
		try {
			FileOutputStream fout = new FileOutputStream(jpegName);
			BufferedOutputStream bos = new BufferedOutputStream(fout);

			// //如果需要改变大小(默认的是宽960×高1280),如改成宽600×高800
			bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bos);
			bos.flush();
			bos.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// 压缩图片添加水印
	public static Bitmap createBitmap(Bitmap src) {
		int w = src.getWidth();
		int h = src.getHeight();
		String mstrTitle = "" + System.currentTimeMillis();
		Bitmap bmpTemp = Bitmap.createBitmap(w, h, Config.ARGB_8888);
		Canvas canvas = new Canvas(bmpTemp);
		Paint p = new Paint();
		String familyName = "宋体";
		Typeface font = Typeface.create(familyName, Typeface.BOLD);
		p.setColor(Color.WHITE);
		p.setTypeface(font);
		p.setTextSize(24);
		canvas.drawBitmap(src, 0, 0, p);
		canvas.drawText(mstrTitle, w - 320, h - 30, p);
		canvas.save(Canvas.ALL_SAVE_FLAG);
		canvas.restore();
		return bmpTemp;
	}

	private String getSDPath() {
		File sdDir = null;
		boolean sdCardExist = Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED);
		if (sdCardExist) {
			sdDir = Environment.getExternalStorageDirectory();
		}
		if (sdDir != null) {
			// System.out.println("Linsq--->"+sdDir.toString());
			return sdDir.toString();
		} else {
			return "";
		}
	}

}
