package com.example.brett.sunshine;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public final class MyView extends View{


	private final static int minHeight = 100;
	private final static int minWidth = 100;

	private double windDirection = 0.0;
	private int windSpeed = 0;

	private final Paint arcPaint = new Paint();
	private final Paint textPaint = new Paint();
	private final Paint needlePaint = new Paint();
	private final Paint compassPointsPaint = new Paint();

	public MyView(Context context) {
		super(context);
	}


	public MyView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}


	public MyView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}


	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int width = this.measureWidth(widthMeasureSpec);
		int height = this.measureHeight(heightMeasureSpec);

		this.setMeasuredDimension(width, height);
	}


	private int measureHeight(int heightMeasureSpec) {
		final int mode = MeasureSpec.getMode(heightMeasureSpec);
		final int measuredHeight = MeasureSpec.getSize(heightMeasureSpec);

		if(mode == MeasureSpec.EXACTLY){
			if(measuredHeight < 100){
				return minHeight;
			} else {
				return measuredHeight;
			}
		} else {
			return minHeight;
		}
	}


	private int measureWidth(int widthMeasureSpec) {

		final int mode = MeasureSpec.getMode(widthMeasureSpec);
		final int measuredWidth = MeasureSpec.getSize(widthMeasureSpec);

		if(mode == MeasureSpec.EXACTLY){
			if(measuredWidth < 100){
				return minWidth;
			} else {
				return measuredWidth;
			}
		} else {
			return minWidth;
		}
	}


	@Override
	protected void onDraw(Canvas canvas) {

		float centerX = getMeasuredWidth() / 2;
		float centerY = getMeasuredHeight() / 2;
		int radius = (getMeasuredWidth() / 2) - 20;

		ListViewItemFormatHelper helper = new ListViewItemFormatHelper();

		final String units = helper.isMetric(this.getContext()) ? " kph" : " mph";

		arcPaint.setColor(Color.BLACK);
		arcPaint.setStyle(Paint.Style.STROKE);
		arcPaint.setStrokeWidth(15);

		textPaint.setColor(Color.BLACK);
		textPaint.setStyle(Paint.Style.FILL);
		textPaint.setTextSize(40);
		textPaint.setTextAlign(Paint.Align.CENTER);

		needlePaint.setColor(Color.RED);
		needlePaint.setStyle(Paint.Style.STROKE);
		needlePaint.setStrokeWidth(7);
		needlePaint.setAlpha(150);


		compassPointsPaint.setColor(Color.BLUE);
		compassPointsPaint.setStyle(Paint.Style.STROKE);
		compassPointsPaint.setTextSize(10);
		compassPointsPaint.setTextAlign(Paint.Align.CENTER);
		compassPointsPaint.setAlpha(150);

		RectF rectangle = new RectF(10,10, getMeasuredWidth() - 10, getMeasuredHeight() - 10);


		canvas.drawArc(rectangle, 0, 360, false, arcPaint);

		canvas.drawText(this.windSpeed + units, Math.round(getMeasuredWidth() * .5), Math.round(getMeasuredHeight() * .35), textPaint);

		Log.d("MYVIEW", "Wind Direction: " + windDirection);
		double radians = Math.toRadians(windDirection - 90);
		float endX = (float) (centerX + radius * Math.cos(radians));
		float endY = (float) (centerY + radius * Math.sin(radians));

		canvas.drawLine(centerX, centerY, endX , endY, needlePaint);


		float northX = (float) (centerX + radius * Math.cos(Math.toRadians(0 - 90)));
		float northY = (float) (centerY + radius * Math.sin(Math.toRadians(0 - 90)));

		canvas.drawText("N", northX, northY, compassPointsPaint);

	}


	public double getWindDirection() {
		return windDirection;
	}


	public void setWindDirection(double windDirection) {
		this.windDirection = windDirection;
	}


	public int getWindSpeed() {
		return windSpeed;
	}


	public void setWindSpeed(int windSpeed) {
		this.windSpeed = windSpeed;
	}
}
