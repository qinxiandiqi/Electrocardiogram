package com.qinxiandiqi.electrocardiogram;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class SettingActivity extends Activity {

	SeekBar sensitivityBar;
	SeekBar tableSizeBar;
	EditText pathEdit;
	TextView sensitivityText;
	TextView tableSizeText;
	EditText inputEdit;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);
		
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		sensitivityBar = (SeekBar) findViewById(R.id.sensitivityBar);
		tableSizeBar = (SeekBar) findViewById(R.id.tableSizeBar);
		pathEdit = (EditText) findViewById(R.id.floderPath);
		sensitivityText = (TextView) findViewById(R.id.sensitivityText);
		tableSizeText = (TextView) findViewById(R.id.tableSizeText);
		
		sensitivityBar.setMax(100);
		sensitivityBar.setProgress(SettingUtil.getSensitivity(this));
		sensitivityText.setText(String.valueOf(sensitivityBar.getProgress()));
		tableSizeBar.setMax(100);
		tableSizeBar.setProgress(SettingUtil.getTableSize(this));
		tableSizeText.setText(String.valueOf(tableSizeBar.getProgress()));
		pathEdit.setText(SettingUtil.getFolderPath(this));
		
		sensitivityBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				int progress = seekBar.getProgress();
				if(progress ==0 ){
					progress = 1;
					seekBar.setProgress(1);
				}
				SettingUtil.setSensitivity(SettingActivity.this, progress);
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				sensitivityText.setText(String.valueOf(progress));
			}
		});
		
		tableSizeBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				int progress = seekBar.getProgress();
				if(progress == 0){
					progress = 1;
					seekBar.setProgress(1);
				}
				SettingUtil.setTableSize(SettingActivity.this, progress);
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				tableSizeText.setText(String.valueOf(progress));
			}
		});
		
		pathEdit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				inputEdit = new EditText(SettingActivity.this);
				inputEdit.setText(pathEdit.getText());
				new AlertDialog.Builder(SettingActivity.this)
						.setTitle(R.string.floder)
						.setView(inputEdit)
						.setIcon(android.R.drawable.ic_input_add)
						.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								SettingUtil.setFolderPath(SettingActivity.this, inputEdit.getText().toString());
							}
						})
						.setNegativeButton(android.R.string.cancel, null)
						.show();
			}
		});
	}
}
