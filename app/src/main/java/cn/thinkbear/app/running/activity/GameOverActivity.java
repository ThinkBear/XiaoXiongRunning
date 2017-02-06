package cn.thinkbear.app.running.activity;

import android.app.Activity;


import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import cn.thinkbear.app.running.R;

import cn.thinkbear.app.running.App;

/**
 * 游戏结束后  跳转的界面
 * @author ThinkBear
 *
 */
public class GameOverActivity extends Activity {

	private ImageView over = null;//游戏结束的视图对象
	private TextView score1 = null;//得分详情
	private TextView score2 = null;//得分详情

	private TextView replay = null;//再玩一遍
	private TextView backMenu = null;//返回主菜单
	private AnimationDrawable ad = null;//游戏结束动画
	private MyClickEvent myClickEvent = null;
	private int passCount = 0;//柱子数
	private int gameType = 0;//游戏难度
	private long gameTime = 0;//单局游戏时间
	private Resources res = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.gameover);
		if (savedInstanceState != null) {
			this.passCount = savedInstanceState.getInt(App.PASSCOUNT);
			this.gameTime = savedInstanceState.getLong(App.GAMETIME);
			this.gameType = savedInstanceState.getInt(App.GAMETYPE);
		} else {
			this.passCount = super.getIntent().getIntExtra(App.PASSCOUNT, 0);
			this.gameTime = super.getIntent().getLongExtra(App.GAMETIME, 0);
			this.gameType = super.getIntent().getIntExtra(App.GAMETYPE, 0);
		}
		this.doInitView();
		this.doSetView();
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus) {
			this.ad.start();
		}
	}
	@Override
	public void onBackPressed() {
		startActivity(new Intent(GameOverActivity.this,
				StartActivity.class));
		finish();
	}
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putInt(App.PASSCOUNT, this.passCount);
		outState.putInt(App.GAMETYPE, this.gameType);
		outState.putLong(App.GAMETIME, this.gameTime);
		super.onSaveInstanceState(outState);
	}
	private void doInitView() {
		this.over = (ImageView) super.findViewById(R.id.over);
		this.score1 = (TextView) super.findViewById(R.id.score1);
		this.score2 = (TextView) super.findViewById(R.id.score2);
		this.replay = (TextView) super.findViewById(R.id.replay);
		this.backMenu = (TextView) super.findViewById(R.id.backMenu);
		this.ad = (AnimationDrawable) this.over.getBackground();
		this.myClickEvent = new MyClickEvent();
		this.res = super.getResources();
	}

	private void doSetView() {
		this.replay.setOnClickListener(this.myClickEvent);
		this.backMenu.setOnClickListener(this.myClickEvent);
		int time = (int) (this.gameTime / 1000);
		String mode = "冒险";
		String end = "";
		switch (this.gameType) {
			case App.GAMETYPE_ONE:
				if (time < 60) {
					end = "1分钟都跑不完，你是猪么？";
				} else if (time > 60 && time <= 120) {
					end = "已经摆脱菜鸟级别了！加油！";
				} else if (time > 120 && time <= 300) {
					end = "不错，再加把劲就可以超神了";
				} else {
					end = "呦！超神了，小子不错嘛！\n有种挑战王者啊！";
				}
				break;
			case App.GAMETYPE_TWO:
				if (time < 60) {
					end = "唉，还是去“冒险”水经验吧，这儿不适合你！";
				} else if (time > 60 && time <= 120) {
					end = "嘿，看来你已经适应了嘛，加油";
				} else if (time > 120 && time <= 300) {
					end = "不错，再加把劲就可以在王者世界称霸了";
				} else {
					end = "去吧！我把所有的宝藏都放在地狱里！\n来地狱决一死战吧！";
				}
				mode = "王者";
				break;
			case App.GAMETYPE_THREE:
				if (time < 60) {
					end = "看来这儿还不适合你这种弱者！";
				} else if (time > 60 && time <= 120) {
					end = "挺有潜力的嘛！不过也只能到这了！";
				} else if (time > 120 && time <= 300) {
					end = "很久没见到像你这样的强者了！";
				} else {
					end = "说好的宝藏呢！！！\n感觉你对本游戏的大力支持";
				}

				mode = "地狱";
				break;
		}

		StringBuffer buf = new StringBuffer();
		buf.append("在");
		buf.append(mode);
		buf.append("模式下，你坚持了");
		buf.append(time);
		buf.append("秒，跳跃了");
		buf.append(this.passCount);
		buf.append("条柱子!");

		this.score1.setText(buf.toString());
		this.score2.setText(end);

	}

	private class MyClickEvent implements OnClickListener {

		public void onClick(View v) {
			switch (v.getId()) {
				case R.id.replay:
					Intent intent = new Intent(GameOverActivity.this,
							RunningGameActivity.class);
					intent.putExtra(App.GAMETYPE, gameType);
					startActivity(intent);
					finish();
					break;
				case R.id.backMenu:
					startActivity(new Intent(GameOverActivity.this,
							StartActivity.class));
					finish();
					break;
			}
		}
	}

	private SpannableString getColorSizeString(String content, int colorId,
											   int sizeId) {
		SpannableString spanString = this.getColorString(content, colorId);
		spanString.setSpan(
				new AbsoluteSizeSpan(this.res.getDimensionPixelOffset(sizeId)),
				0, content.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		return spanString;
	}

	private SpannableString getColorString(String content, int colorId) {
		SpannableString spanString = new SpannableString(content);
		spanString.setSpan(new ForegroundColorSpan(this.res.getColor(colorId)),
				0, content.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		return spanString;
	}

	private SpannableString getColorSizeBoldString(String content, int colorId,
												   int sizeId) {
		SpannableString spanString = this.getColorSizeString(content, colorId,
				sizeId);
		spanString.setSpan(new StyleSpan(Typeface.BOLD), 0, content.length(),
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		return spanString;

	}
}
