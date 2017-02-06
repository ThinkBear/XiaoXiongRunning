package cn.thinkbear.app.running.thread;

import java.util.Random;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.SurfaceHolder;
import android.view.WindowManager;

import cn.thinkbear.app.running.App;
import cn.thinkbear.app.running.R;

/**
 * 绘画的线程类
 * @author ThinkBear
 */
public class MyRunnable implements Runnable {
	public static final int RUNNING_STATU = 0;// 正在跑的状态
	public static final int STANDOFF_STATU = 1;// 撞到柱子的状态
	public static final int GAMEOVER_STATU = 2;// 游戏结束的状态
	public static final int SPACING_TIME = 100;// 绘制下一张人物的时间间隔
	private int gameStatu = RUNNING_STATU;// 当前的游戏状态

	private Bitmap[] figures = null;//所有的主角人物图
	private Bitmap bgMain = null;//背景图
	private int figuresIndex = 0;//主角人物下标
	private int figuresIndexMax = 0;//主角人物最大下标
	private SurfaceHolder holder = null;//SurfaceHolder对象，用于取得画板对象
	private Paint paint = null;//画笔对象
	private int w = 0;//屏幕的宽度
	private int h = 0;//屏幕的高度
	private float figureY = 0;//主角人物在y轴的哪一位置奔跑
	private float figureYJumpMax = 0;//主角人物跳跃后最大的高度
	private float figureYJumpFlag = 0;//主角人物跳跃的高度标记
	private float figureYJumpAmong = 0;//主角人物跳跃到一半时的标记

	private float bgMainX = 0;//背景图片在X轴的哪一位置
	private float bgMainY = 0;//背景图片在Y轴的哪一位置
	private float bgMainXMax = 0;//背景图片在X轴的的最大位置

	private long overTime;// 游戏结束的时刻
	private long startTime;// 游戏开始的时刻
	private long lastTime;// 上次画人物的时间

	private boolean isPlaying;//是否在开始游戏了
	private boolean isPause;//是否暂停了
	private boolean isJump;//人物是否在中跳跃
	private boolean isJumpUp = true;//人物是否在往上跳
	private Rect figureRect = null;// 人物的矩形
	private Rect obstacleRect = null;// 障碍物矩形
	private int figuresHeight;//人物的高度
	private Random ran = null;//随机数对象
	private int ranObstacleSpacing = 0;//障碍物在X轴上哪个位置出现
	private int spacing = 3;//人物奔跑的速度
	private int figureSpacing = 0;//因为人物的图片有边距间距，减去此值 来取得 真正的人物 矩形对象
	private int obstacleWidth = 0;//障碍物的宽度
	private int color = Color.WHITE;//当前的背景颜色，由此值来决定是否为夜间或白天模式
	private int passCount = 0;// 跳跃的柱子个数
	private Handler myHandler = null;//Handler对象，游戏结束后，由此对象发送信息给主线程

	public MyRunnable(Context context, SurfaceHolder holder, Handler myHandler) {
		this.holder = holder;
		this.myHandler = myHandler;

		this.paint = new Paint();
		this.paint.setTextSize(25);//设置绘制的文字大小
		this.paint.setStrokeWidth(10);
		this.paint
				.setColor(context.getResources().getColor(R.color.paintColor));//设置画笔的颜色
		//取得窗口管理对象，并取得手机屏幕的宽和高
		WindowManager wm = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics dm = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(dm);
		this.w = dm.widthPixels;
		this.h = dm.heightPixels;
		//初始化好人物和背景的图片对象
		Resources res = context.getResources();
		this.figures = new Bitmap[] {
				BitmapFactory.decodeResource(res, R.drawable.a_1),
				BitmapFactory.decodeResource(res, R.drawable.a_2),
				BitmapFactory.decodeResource(res, R.drawable.a_3),
				BitmapFactory.decodeResource(res, R.drawable.a_4) };
		this.bgMain = BitmapFactory.decodeResource(res, R.drawable.bg_main);
		//初始化好人物的Y轴坐标，和跳跃的最高度和跳跃高度的一半
		this.figuresIndexMax = this.figures.length - 1;
		this.figuresHeight = this.figures[0].getHeight();
		this.figureY = 2 * this.h / 3 - this.figuresHeight;
		this.figureYJumpFlag = this.figureY;
		this.figureYJumpAmong = this.figureY - this.figuresHeight / 2;
		this.figureYJumpMax = this.figureY - this.figuresHeight * 1.4f;
		//初始化背景的Y轴坐标，和X轴的最大坐标
		this.bgMainY = this.figureY + this.figuresHeight / 2;
		this.bgMainXMax = -this.bgMain.getWidth() + this.w;
		//初始化好人物的矩形对象，其left 和 right的值固定不对，但top和bottom值会随着人物的跳跃而变化
		this.figureSpacing = this.figuresHeight / 10;
		this.figureRect = new Rect();
		this.figureRect.left = this.figureSpacing;
		this.figureRect.right = (int) (this.figureRect.left
				+ this.figures[0].getWidth() - this.figureSpacing * 2);
		//初始化好障碍物的矩形对象，同bottom值固定不对，但left和right会随着时间从右向左而发生改变，
		//而top值决定障碍物的高，在出现下一个障碍物的时候会重新赋值
		this.ran = new Random();
		this.obstacleRect = new Rect();
		this.obstacleRect.bottom = (int) this.figureY + this.figuresHeight
				- this.figuresHeight / 10;

	}
	/**
	 * 由此方法来决定游戏的难度
	 * @param gameType
	 */
	public void setGameType(int gameType) {
		switch (gameType) {
			case App.GAMETYPE_ONE://冒险
				this.obstacleWidth = (int) (this.figuresHeight / 2.5f);//障碍物的宽度，越宽表示难度越大
				this.spacing = 2;//背景从右向左的间隔值，越小表示速度越慢，即难度越小
				break;
			case App.GAMETYPE_TWO://王者
				this.obstacleWidth = (int) (this.figuresHeight / 2.3f);
				this.spacing = 4;
				break;
			case App.GAMETYPE_THREE://地狱
				this.obstacleWidth = (int) (this.figuresHeight / 2.2f);
				this.spacing = 6;
				break;

		}
		this.doInitObstacleRect();
	}
	/**
	 * 由此方法完成对下一个障碍物的初始化操作
	 */
	private void doInitObstacleRect() {
		this.ranObstacleSpacing = 3 * this.w / 5 + this.ran.nextInt(this.w);
		this.obstacleRect.top = this.obstacleRect.bottom - this.obstacleWidth
				- this.ran.nextInt(this.obstacleWidth);
		this.obstacleRect.left = this.ranObstacleSpacing;
		this.obstacleRect.right = this.ranObstacleSpacing + this.obstacleWidth;
	}

	public void run() {
		this.startTime = System.currentTimeMillis();//取得开始游戏时间
		while (this.isPlaying) {//游戏开始
			if (this.isPause) {//是否暂停
				try {
					Thread.sleep(1000);//休眠1秒
				} catch (InterruptedException e) {
				}
			} else {
				Canvas canvas = null;
				try {
					canvas = this.holder.lockCanvas();//取得画板对象
					if (canvas != null) {//画板对象不为空
						switch (this.gameStatu) {
							case RUNNING_STATU://在跑
								this.drawRunning(canvas);
								break;
							case STANDOFF_STATU://撞到障碍物
								this.drawStandoff(canvas);
								break;
							case GAMEOVER_STATU://游戏结束
								this.isPlaying = false;
								myHandler.sendMessage(myHandler.obtainMessage());//向主线程发送游戏结束信息
								break;
						}
					}

				} catch (Exception e) {

				} finally {
					if (canvas != null) {
						this.holder.unlockCanvasAndPost(canvas);
					}
				}
			}

		}

	}
	/**
	 * 当撞到障碍物时  执行的方法
	 * @param canvas
	 */
	private void drawStandoff(Canvas canvas) {

		if (System.currentTimeMillis() - overTime >= 2000) {//给2秒的时间让，游戏人物进入振动效果
			gameStatu = GAMEOVER_STATU;//游戏结束标记
		} else {
			canvas.drawRect(this.obstacleRect, this.paint);//绘制障碍物，此时为不移动
			canvas.drawBitmap(this.bgMain, this.bgMainX, this.bgMainY, paint);//绘制背景图，此时为不移动
			canvas.drawBitmap(this.figures[this.figuresIndex],
					this.ran.nextInt(5),
					this.figureYJumpFlag + this.ran.nextInt(5), paint);//绘制人物，x和y轴有小辐变化，达到振动效果
		}

	}
	/**
	 * 在跑时执行的方法
	 * @param canvas
	 */
	private void drawRunning(Canvas canvas) {
		canvas.drawColor(this.color);

		// 绘制右上角的时间文本
		String time = (System.currentTimeMillis() - startTime) / 1000f + "\'时间";
		float time_w = paint.measureText(time);
		canvas.drawText(time, w - time_w, -paint.ascent(), paint);

		// 绘制右上角的柱子文本
		String countStr = passCount + "柱子";
		float count_w = paint.measureText(countStr);
		canvas.drawText(countStr, w - count_w, -(paint.ascent() * 2), paint);

		//更新right和left值，实现柱子的移动
		this.obstacleRect.right = this.obstacleRect.right - this.spacing;
		this.obstacleRect.left = this.obstacleRect.left - this.spacing;
		if (this.obstacleRect.right <= 0) {//如果柱子已经到尽头
			this.doInitObstacleRect();//初始化下一条柱子
			this.passCount++;//通过柱子量加1
		}
		canvas.drawRect(this.obstacleRect, this.paint);//绘制柱子

		if (this.bgMainXMax >= this.bgMainX) {//如果背景的下标小于最大值了
			this.bgMainX = 0;//初始为0
		} else {
			this.bgMainX -= spacing;//继续减,实现背景的移动
		}
		canvas.drawBitmap(this.bgMain, this.bgMainX, this.bgMainY, paint);//绘制背景

		if (this.isJump) {//是否在跳跃
			if (this.isJumpUp) {//是否在向上跳跃

				if (this.figureYJumpFlag <= figureYJumpMax) {//跳跃的值已经大于或等于最大值了
					this.isJumpUp = false;//修改向上跳跃的标记，表示人物要降落了
				} else if (this.figureYJumpFlag <= figureYJumpAmong) {//是否超过跳跃高度一半了
					this.figureYJumpFlag -= this.spacing;//超过一半时 速度减慢效果
				} else {
					this.figureYJumpFlag -= this.spacing * 2;//没超过时，速度*2
				}
			} else {//人物在降落
				if (this.figureYJumpFlag >= this.figureY) {//判断是否着地了
					this.isJump = false;//跳跃标记为false
					this.isJumpUp = true;//向上跳跃为true
					this.figureYJumpFlag = this.figureY;//跳跃值为奔跑的y轴值
				} else if (this.figureYJumpFlag >= figureYJumpAmong) {//降落的值是否超过总跳跃高度的一半
					this.figureYJumpFlag += this.spacing * 2;//超过，速度*2
				} else {
					this.figureYJumpFlag += this.spacing;//没超过时，速度减慢效果
				}
			}
			canvas.drawBitmap(this.figures[this.figuresIndex], 0,
					this.figureYJumpFlag, paint);//绘制跳跃状态下的人物

		} else {
			long nowTime = System.currentTimeMillis();//取得当前时间
			if (nowTime - lastTime > SPACING_TIME) {//上一次更新人物下标的时间是否超过指定时间
				if (this.figuresIndex >= this.figuresIndexMax) {//如果人物的下标超过了最大值
					this.figuresIndex = 0;//从第一张开始
				} else {
					this.figuresIndex++;//设置为下一张
				}
				this.lastTime = nowTime;//更新上一次的时间为当前时间
			}
			canvas.drawBitmap(this.figures[this.figuresIndex], 0, this.figureY,
					paint);//绘制奔跑状态下的人物
		}
		//更新人物矩形的top和bottom值，得到当前人物的矩形
		this.figureRect.top = (int) this.figureYJumpFlag + this.figureSpacing;
		this.figureRect.bottom = (int) (this.figureRect.top
				+ this.figuresHeight - this.figureSpacing * 2);

		if (obstacleRect.intersect(figureRect)) {//如果人物的矩形和障碍物的矩形有交集，则表示相撞了
			this.gameStatu = STANDOFF_STATU;//游戏状态为撞到障碍物状态
			this.overTime = System.currentTimeMillis();//记录下结束的时间
		}
	}

	public int getGameStatu() {
		return gameStatu;
	}

	public void setGameStatu(int gameStatu) {
		this.gameStatu = gameStatu;
	}

	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}

	public boolean isPlaying() {
		return isPlaying;
	}

	public void setPlaying(boolean isPlaying) {
		this.isPlaying = isPlaying;
	}

	public boolean isPause() {
		return isPause;
	}

	public void setPause(boolean isPause) {
		this.isPause = isPause;
	}

	public long getGameTime() {
		return this.overTime - this.startTime;
	}

	public int getPassCount() {
		return this.passCount;
	}

	public boolean isJump() {
		return isJump;
	}

	public void setJump(boolean isJump) {
		this.isJump = isJump;
	}
}
