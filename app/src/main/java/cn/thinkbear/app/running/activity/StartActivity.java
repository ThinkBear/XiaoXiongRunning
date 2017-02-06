package cn.thinkbear.app.running.activity;

import android.app.Activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;


import cn.thinkbear.app.running.App;
import cn.thinkbear.app.running.R;

/**
 * 游戏的开始菜单页，用户可选择难度和退出游戏操作
 * @author ThinkBear
 *
 */
public class StartActivity extends Activity {
	private TextView one = null;
	private TextView two = null;
	private TextView three = null;
	private TextView exit = null;
	private ImageView logo = null;
	private AnimationDrawable ad = null;
	private MyClickEvent myClickEvent = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.start);
		this.one = (TextView) super.findViewById(R.id.one);
		this.two = (TextView) super.findViewById(R.id.two);
		this.three = (TextView) super.findViewById(R.id.three);
		this.exit = (TextView) super.findViewById(R.id.exit);
		this.logo = (ImageView) super.findViewById(R.id.logo);
		this.ad = (AnimationDrawable) this.logo.getBackground();
		this.myClickEvent = new MyClickEvent();
		this.one.setOnClickListener(this.myClickEvent);
		this.two.setOnClickListener(this.myClickEvent);
		this.three.setOnClickListener(this.myClickEvent);
		this.exit.setOnClickListener(this.myClickEvent);
	}
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if(hasFocus){
			this.ad.start();
		}
	}
	@Override
	public void onBackPressed() {
		new AlertDialog.Builder(this).setTitle(R.string.app_name)
				.setMessage("确定要退出游戏？")
				.setPositiveButton("退出",
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog,
												int which) {
								finish();
							}
						}).setNegativeButton("取消", null).show();
	}
	private class MyClickEvent implements OnClickListener {

		public void onClick(View v) {

			int gameType = -1;
			switch (v.getId()) {
				case R.id.one:
					gameType = App.GAMETYPE_ONE;
					break;
				case R.id.two:
					gameType = App.GAMETYPE_TWO;
					break;
				case R.id.three:
					gameType = App.GAMETYPE_THREE;
					break;
				case R.id.exit:
					new AlertDialog.Builder(StartActivity.this).setTitle(R.string.app_name)
							.setMessage("确定要退出游戏？")
							.setPositiveButton("退出",
									new DialogInterface.OnClickListener() {

										public void onClick(DialogInterface dialog,
															int which) {
											finish();
										}
									}).setNegativeButton("取消", null).show();
					break;
			}
			if (gameType != -1) {
				Intent intent = new Intent(StartActivity.this,
						RunningGameActivity.class);
				intent.putExtra(App.GAMETYPE, gameType);
				startActivity(intent);
				finish();
			}

		}
	}
}
