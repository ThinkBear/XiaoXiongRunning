package cn.thinkbear.app.running.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.TextView;

import cn.thinkbear.app.running.R;

import cn.thinkbear.app.running.App;
import cn.thinkbear.app.running.thread.MyRunnable;
/**
 * 游戏主界面
 * @author ThinkBear
 *
 */
public class RunningGameActivity extends Activity implements
		SurfaceHolder.Callback, OnTouchListener {
	private SurfaceView main = null;
	private MyRunnable run = null;
	private TextView timeStatu = null;
	private TextView gameStatu = null;
	private MyClickEvent myClickEvent = null;

	private int gameType = 0;
	private Handler myHandler = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.main);
		if (savedInstanceState != null) {
			this.gameType = savedInstanceState.getInt(App.GAMETYPE);
		} else {
			this.gameType = super.getIntent().getIntExtra(App.GAMETYPE, 0);
		}
		this.doInitView();
		this.doSetView();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putInt(App.GAMETYPE, this.gameType);
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onBackPressed() {//用户点击了返回按钮
		run.setPause(true);//先暂定游戏，并弹出对话框
		new AlertDialog.Builder(this).setTitle(R.string.app_name)
				.setMessage("确定要返回主菜单？")
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						startActivity(new Intent(RunningGameActivity.this,
								StartActivity.class));//跳转到主菜单页面
						finish();
					}
				})
				.setNegativeButton("继续游戏",
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog,
												int which) {
								run.setPause(false);//继续游戏
							}
						}).show();
	}

	private void doInitView() {
		this.main = (SurfaceView) super.findViewById(R.id.main);

		this.timeStatu = (TextView) super.findViewById(R.id.timeStatu);
		this.gameStatu = (TextView) super.findViewById(R.id.gameStatu);

		this.main.getHolder().addCallback(this);
		this.myClickEvent = new MyClickEvent();
		this.myHandler = new Handler(new Handler.Callback() {

			public boolean handleMessage(Message msg) {
				Intent intent = new Intent(RunningGameActivity.this,
						GameOverActivity.class);
				intent.putExtra(App.GAMETIME, run.getGameTime());//取得游戏的总时长
				intent.putExtra(App.PASSCOUNT, run.getPassCount());//取得跳跃的柱子数
				intent.putExtra(App.GAMETYPE, gameType);//取得游戏的难度值
				startActivity(intent);//开始跳转
				finish();//结束本页面
				return false;
			}
		});

	}

	private void doSetView() {
		this.main.setOnTouchListener(this);
		this.timeStatu.setOnClickListener(this.myClickEvent);
		this.gameStatu.setOnClickListener(this.myClickEvent);

	}

	private class MyClickEvent implements OnClickListener {

		public void onClick(View v) {
			switch (v.getId()) {
				case R.id.timeStatu://修改夜间或白天模式
					if (run.getColor() == Color.BLACK) {// 当前为夜间模式
						run.setColor(Color.WHITE);
						timeStatu.setText(R.string.night);
					} else {//
						run.setColor(Color.BLACK);
						timeStatu.setText(R.string.day);
					}
					break;
				case R.id.gameStatu://暂定或继续游戏操作
					run.setPause(!run.isPause());
					gameStatu.setText(run.isPause() ? R.string.conti
							: R.string.pause);

					break;
			}
		}

	}

	public void surfaceCreated(SurfaceHolder holder) {//SurfaceView的回调方法
		this.run = new MyRunnable(super.getApplicationContext(),
				this.main.getHolder(), this.myHandler);//初始化绘制的线程对象
		this.run.setPlaying(true);//设置开始游戏标记为true
		this.run.setGameType(this.gameType);//设置好游戏的难度
		new Thread(this.run).start();//开始绘制

	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width,
							   int height) {

	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		if (this.run != null) {
			this.run.setPlaying(false);
		}

	}

	public boolean onTouch(View v, MotionEvent event) {
		if (this.run != null) {//绘制线程对象已存在
			switch (this.run.getGameStatu()) {
				case MyRunnable.RUNNING_STATU://当前的游戏状态为在跑中
					if (event.getAction() == MotionEvent.ACTION_DOWN) {//用户点击了屏幕
						run.setJump(true);//设置跳跃标记为true
					}
					break;
			}
		}

		return false;
	}

}