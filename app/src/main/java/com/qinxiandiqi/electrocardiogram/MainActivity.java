package com.qinxiandiqi.electrocardiogram;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.HorizontalScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements SensorEventListener {

	static final int SHOW_PROGRESS = 0;
	static final int UPDATE_PROGRESS = 1;
	static final int CALLDRAW = 2;
	static final int SHOW_TOAST_ID = 3;
	static final int CALLBACK = 4;
	static final int DISMISS_PROGRESSDIALOG = 5;
	static final int SHOW_TOAST_STRING = 6;
	
	int largeSaveWidth = 10*1024*1024;
	
	int sensitivity = 50;
	int tableSize = 50;
	String path;
	
	boolean isSave = true;
	boolean isCallDraw = false;
	boolean isCallBack = false;
	
	SensorManager sm = null;
	Sensor sensor;
	TextView sensorText;
	HorizontalScrollView heardScroll;
	HeardSurfaceView heardSurfaceView;
	SurfaceHolder heardHolder;
	ProgressDialog progressDialog;
	Builder builder;
	
	int distanceX = 0;
	int distanceY = 0;
	float average;
	ArrayList<Float> averageList = new ArrayList<Float>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sm = (SensorManager) getSystemService(SENSOR_SERVICE);
		heardSurfaceView = new HeardSurfaceView(this);
		setContentView(heardSurfaceView);
		Point point = new Point();
		getWindowManager().getDefaultDisplay().getRealSize(point);
		distanceX = point.x;
		distanceY = point.y/2;
		largeSaveWidth = largeSaveWidth/point.y;
		heardHolder = heardSurfaceView.getHeardHolder();
		heardSurfaceView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				MainActivity.this.openOptionsMenu();
			}
		});
		
		
	}

	@Override
	protected void onStart() {
		super.onStart();
		sensitivity = SettingUtil.getSensitivity(this);
		tableSize = SettingUtil.getTableSize(this);
		path = SettingUtil.getFolderPath(this);
		File folder = new File(path);
		if(!folder.canExecute()) folder.mkdirs();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		sensor = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		if (sensor != null)
			sm.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
	}

	boolean isFirstTime = true;
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if(isFirstTime){
			openOptionsMenu();
			isFirstTime = false;
		}
	}
	
	@Override
	protected void onStop() {
		if (sensor != null)
			sm.unregisterListener(this);
		isFirstTime = true;
		super.onStop();
	}

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 0, 0, R.string.start).setIcon(android.R.drawable.presence_online);
		menu.add(0, 1, 1, R.string.stop).setIcon(android.R.drawable.presence_offline);
		menu.add(0, 2, 2, R.string.save).setIcon(android.R.drawable.ic_menu_save);
		menu.add(0, 3, 3, R.string.setting).setIcon(android.R.drawable.ic_menu_preferences);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			if(!isSave){
				isCallBack = true;
				showMessageDialog();
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
			case 0: 
				heardSurfaceView.startDraw();
				return true;
			case 1:
				heardSurfaceView.stopDraw();
				return true;
			case 2:
				saveAsBitmap();
				return true;
			case 3:
				Intent intent = new Intent(this,SettingActivity.class);
				startActivity(intent);
				return true;
			default:
				return false;
		}
	}
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub

	}
 
	@Override
	public void onSensorChanged(SensorEvent event) {
		if (sensor != null) {
			float tempX = event.values[0];
			float tempY = event.values[1];
			float tempZ = event.values[2];
			float largeAbsValue = 0;
			if(Math.abs(tempZ) >= Math.abs(tempX))largeAbsValue = tempZ;
			else largeAbsValue = tempX;
			if(Math.abs(largeAbsValue) < Math.abs(tempY))largeAbsValue = tempY;
			float tempAverage = (float)Math.sqrt(tempX * tempX + tempY * tempY + tempZ * tempZ);
			if(largeAbsValue>0){
				average = tempAverage * sensitivity - SensorManager.GRAVITY_EARTH * sensitivity;
			}else{
				average = -(tempAverage*sensitivity + SensorManager.GRAVITY_EARTH * sensitivity);
			}
		}
	}

	private void saveAsBitmap(){
		if(isSave){
			Toast.makeText(this, R.string.nothingToSave, Toast.LENGTH_SHORT).show();
		}else{
			isRun = false;
			SaveThread saveThread = new SaveThread();
			saveThread.start();
		}
	}
	
	private void showMessageDialog(){
		if(builder == null) {
			builder = new Builder(this);
			builder.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle(R.string.save)
				.setMessage(R.string.saveTips)
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						saveAsBitmap();
					}
				})
				.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if(isCallDraw){
							isSave = true;
							mHandler.sendEmptyMessage(CALLDRAW);
						}
						if(isCallBack)mHandler.sendEmptyMessage(CALLBACK);
					}
				});
		}
		isRun = false;
		builder.show();
	}
	
	private void showProgressDialog(){
		progressDialog = new ProgressDialog(this);
		progressDialog.setIcon(android.R.drawable.ic_menu_info_details);
		progressDialog.setTitle(R.string.save);
		progressDialog.setMessage(getString(R.string.saving));
		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progressDialog.setMax(averageList.size());
		progressDialog.show();
	}
	
	@SuppressLint("HandlerLeak")
	Handler mHandler = new Handler(){
		@Override
		public void dispatchMessage(Message msg) {
			switch (msg.what) {
			case SHOW_PROGRESS:
				showProgressDialog();
				break;
			case UPDATE_PROGRESS:
				if(progressDialog != null){
					int progress = msg.arg1 + 1;
					if(progress >= averageList.size()){
						progressDialog.dismiss();
					}
					progressDialog.setProgress(progress);
				}
				break;
			case CALLDRAW:
				if(isCallDraw) heardSurfaceView.startDraw();
				break;
			case SHOW_TOAST_ID:
				Toast.makeText(MainActivity.this, msg.arg1, Toast.LENGTH_SHORT).show();
				break;
			case CALLBACK:
				if(isCallBack) MainActivity.this.finish();
				break;
			case DISMISS_PROGRESSDIALOG:
				if(progressDialog != null && progressDialog.isShowing()) progressDialog.dismiss();
				break;
			case SHOW_TOAST_STRING:
				Toast.makeText(MainActivity.this, (String)msg.obj, Toast.LENGTH_SHORT).show();
				break;
			default:
				break;
			}
		}
	};
	
	public boolean isRun;
	public boolean isReady;

	class HeardThread extends Thread {

		@Override
		public void run() {
			while (isRun&&isReady) {
				try {
					synchronized (heardHolder) {
						averageList.add(average);
						Canvas canvas = heardHolder.lockCanvas();
						if(canvas == null) return;
						canvas.drawColor(Color.BLACK);
						
						Paint mTablePaint = new Paint();
						mTablePaint.setColor(Color.BLUE);
						for(int i=0;i<=distanceY;i=i+tableSize){
							canvas.drawLine(0, distanceY-i, distanceX, distanceY-i, mTablePaint);
							canvas.drawLine(0, distanceY+i, distanceX, distanceY+i, mTablePaint);
						}
						for(int i=tableSize;i<=distanceX;i=i+tableSize){
							canvas.drawLine(i, 0, i, distanceY*2, mTablePaint);
						}
						
						Paint mPaint = new Paint();
						mPaint.setColor(Color.GREEN);
						if (averageList.size() <= 0) {
							heardHolder.unlockCanvasAndPost(canvas);
							return;
						}
						float oldX = 0;
						float oldY = averageList.get(0) + distanceY;
						int drawWidth = averageList.size()%distanceX;
						for (int i = averageList.size() - drawWidth; i < averageList.size(); i++) {
							float newX = (float) (oldX + 1);
							float newY = (float) (averageList.get(i) + distanceY);
							canvas.drawLine(oldX, oldY, newX, newY, mPaint);
							oldX= (newX>=distanceX)?0:newX;
							oldY = newY;
						}
						heardHolder.unlockCanvasAndPost(canvas);
						Thread.sleep(10);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	};
	
	class SaveThread extends Thread{
		@Override
		public void run() {
			if(!isSave){
				int count = averageList.size()/largeSaveWidth;
				int lastWidth = averageList.size()%largeSaveWidth;
				count = lastWidth>0?(count+1):count;
				
				Calendar calendar = Calendar.getInstance();
				String fileName = "" + calendar.get(Calendar.YEAR) 
						+ (calendar.get(Calendar.MONTH)<9?"0":"") + (calendar.get(Calendar.MONDAY)+1) 
						+ calendar.get(Calendar.DAY_OF_MONTH) + calendar.get(Calendar.HOUR_OF_DAY) 
						+ calendar.get(Calendar.MINUTE) + "-page";
				
				mHandler.sendEmptyMessage(SHOW_PROGRESS);
				for(int i=0;i<count;i++){
					if(i==count-1)createAndSaveBitmap(fileName+i+".png", lastWidth, i*largeSaveWidth);
					else createAndSaveBitmap(fileName+i+".png", largeSaveWidth, i*largeSaveWidth);
				}
				mHandler.sendEmptyMessage(DISMISS_PROGRESSDIALOG);
				isSave = true;
				if(isCallBack)mHandler.sendEmptyMessage(CALLBACK);
				if(isCallDraw)mHandler.sendEmptyMessage(CALLDRAW);
			}
		}
		
		public boolean createAndSaveBitmap(String fileName,int width,int startX){
			Bitmap bm = Bitmap.createBitmap(width, distanceY*2, Config.RGB_565);
			Canvas canvas = new Canvas(bm);
			
			Paint mTablePaint = new Paint();
			mTablePaint.setColor(Color.BLUE);
			for(int i=0;i<=distanceY;i=i+tableSize){
				canvas.drawLine(0, distanceY-i, width, distanceY-i, mTablePaint);
				canvas.drawLine(0, distanceY+i, width, distanceY+i, mTablePaint);
			}
			for(int i=tableSize;i<=width;i=i+tableSize){
				canvas.drawLine(i, 0, i, distanceY*2, mTablePaint);
			}
			
			Paint mPaint = new Paint();
			mPaint.setColor(Color.GREEN);
			float oldX = 0;
			float oldY = averageList.get(startX) + distanceY;
			for (int i = startX; i < startX + width; i++) {
				float newX = (float) (oldX + 1);
				float newY = (float) (averageList.get(i) + distanceY);
				canvas.drawLine(oldX, oldY, newX, newY, mPaint);
				oldX= newX;
				oldY = newY;
				mHandler.sendMessage(mHandler.obtainMessage(UPDATE_PROGRESS, i, i));
			}
			
			canvas.save(Canvas.ALL_SAVE_FLAG);
			canvas.restore();
			
			File file = new File(path+"/"+fileName);
			FileOutputStream fout = null;
			try {
				if(!file.canExecute())file.createNewFile();
				fout = new FileOutputStream(file);
				bm.compress(Bitmap.CompressFormat.PNG, 50, fout);
				fout.close();
			} catch (FileNotFoundException e) {
				mHandler.sendMessage(mHandler.obtainMessage(SHOW_TOAST_STRING, fileName+MainActivity.this.getString(R.string.saveFail)));
				e.printStackTrace();
				return false;
			} catch (IOException e) {
				mHandler.sendMessage(mHandler.obtainMessage(SHOW_TOAST_STRING, fileName+MainActivity.this.getString(R.string.saveFail)));
				e.printStackTrace();
				return false;
			}finally{
				bm.recycle();
				bm = null;
			}
			mHandler.sendMessage(mHandler.obtainMessage(SHOW_TOAST_STRING, fileName + MainActivity.this.getString(R.string.saveSuccess)));
			return true;
		}
	}

	class HeardSurfaceView extends SurfaceView implements
			SurfaceHolder.Callback {

		HeardThread heardThread;
		SurfaceHolder holder;

		public HeardSurfaceView(Context context) {
			super(context);
			this.setKeepScreenOn(true);
			holder = this.getHolder();
			holder.addCallback(this);
		}

		public SurfaceHolder getHeardHolder() {
			return holder;
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			// TODO Auto-generated method stub

		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			isReady = true;
			initView();
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			isReady = false;
		}
		
		public boolean initView(){
			if(isReady){
				Canvas canvas = heardHolder.lockCanvas();
				if(canvas == null) return false;
				canvas.drawColor(Color.BLACK);
				
				Paint mTablePaint = new Paint();
				mTablePaint.setColor(Color.BLUE);
				for(int i=0;i<=distanceY;i=i+tableSize){
					canvas.drawLine(0, distanceY-i, distanceX, distanceY-i, mTablePaint);
					canvas.drawLine(0, distanceY+i, distanceX, distanceY+i, mTablePaint);
				}
				for(int i=tableSize;i<=distanceX;i=i+tableSize){
					canvas.drawLine(i, 0, i, distanceY*2, mTablePaint);
				}
				
				Paint mPaint = new Paint();
				mPaint.setColor(Color.GREEN);
				if (averageList.size() <= 0) {
					heardHolder.unlockCanvasAndPost(canvas);
					return true;
				}
				float oldX = 0;
				float oldY = averageList.get(0) + distanceY;
				int drawWidth = averageList.size()%distanceX;
				for (int i = averageList.size() - drawWidth; i < averageList.size(); i++) {
					float newX = (float) (oldX + 1);
					float newY = (float) (averageList.get(i) + distanceY);
					canvas.drawLine(oldX, oldY, newX, newY, mPaint);
					oldX= (newX>=distanceX)?0:newX;
					oldY = newY;
				}
				heardHolder.unlockCanvasAndPost(canvas);
				return true;
			}else return false;
		}
		
		public void startDraw(){
			if(!isSave){
				isCallDraw = true;
				showMessageDialog();
			}else{
				heardThread = new HeardThread();
				isRun = true;
				isSave = false;
				averageList.clear();
				heardThread.start();
				isCallDraw = false;
			}
			
		}
		
		public void stopDraw(){
			isRun = false;
		}

	}

}
