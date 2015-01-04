package com.example.brett.sunshine;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.v4.view.accessibility.AccessibilityManagerCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityEventSource;
import android.view.accessibility.AccessibilityManager;

public final class MyView extends View{


	private final static int minHeight = 100;
	private final static int minWidth = 100;

	private double windDirection = 0.0;
	private double windSpeed = 0.0;

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
		textPaint.setTextSize(20);
		textPaint.setTextAlign(Paint.Align.CENTER);

		needlePaint.setColor(Color.RED);
		needlePaint.setStyle(Paint.Style.STROKE);
		needlePaint.setStrokeCap(Paint.Cap.ROUND);
		needlePaint.setStrokeWidth(7);
		needlePaint.setAlpha(150);


		compassPointsPaint.setColor(Color.BLUE);
		compassPointsPaint.setStyle(Paint.Style.STROKE);
		compassPointsPaint.setTextSize(10);
		compassPointsPaint.setTextAlign(Paint.Align.CENTER);
		compassPointsPaint.setAlpha(150);

		RectF rectangle = new RectF(10,10, getMeasuredWidth() - 10, getMeasuredHeight() - 10);


		canvas.drawArc(rectangle, 0, 360, false, arcPaint);


		if(!helper.isMetric(getContext())){
			windSpeed = windSpeed * .621371192237334f;
		}
		canvas.drawText(String.format("%.0f %s", windSpeed, units), Math.round(getMeasuredWidth() * .5), Math.round(getMeasuredHeight() * .35), textPaint);

		double radians = Math.toRadians(windDirection - 90);
		float endX = (float) (centerX + radius * Math.cos(radians));
		float endY = (float) (centerY + radius * Math.sin(radians));



		canvas.drawLine(centerX, centerY, endX , endY, needlePaint);

		float endArrowLineTopX =  (float) (endX + 25 * Math.cos(Math.toRadians(225)));
		float endArrowLineTopY = (float) (endY + 25 * Math.sin(Math.toRadians(225)));

		canvas.drawLine(endX, endY, endArrowLineTopX, endArrowLineTopY, needlePaint);

		float endArrowBottomLineX =  (float) (endX + 25 * Math.cos(Math.toRadians(285)));
		float endArrowBottomLineY = (float) (endY + 25 * Math.sin(Math.toRadians(285)));

		canvas.drawLine(endX, endY, endArrowBottomLineX, endArrowBottomLineY, needlePaint);


		canvas.drawText("N", centerX, Math.round(getMeasuredHeight() * .15), compassPointsPaint);
		canvas.drawText("E", Math.round(getMeasuredWidth() * .85), centerY, compassPointsPaint);
		canvas.drawText("S", centerX, Math.round(getMeasuredHeight() * .85), compassPointsPaint);
		canvas.drawText("W", Math.round(getMeasuredWidth() * .15), centerY, compassPointsPaint);

	}


	public double getWindDirection() {
		return windDirection;
	}


	public void setWindDirection(double windDirection) {
		this.windDirection = windDirection;
		AccessibilityManager accessibilityManager =
				(AccessibilityManager) this.getContext().getSystemService(Context.ACCESSIBILITY_SERVICE);

		if(accessibilityManager.isEnabled()){
			sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED);
		}
	}


	public double getWindSpeed() {
		return windSpeed;
	}


	public void setWindSpeed(double windSpeed) {
		this.windSpeed = windSpeed;
		AccessibilityManager accessibilityManager =
				(AccessibilityManager) this.getContext().getSystemService(Context.ACCESSIBILITY_SERVICE);

		if(accessibilityManager.isEnabled()){
			sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED);
		}
	}


	@Override
	public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
		ListViewItemFormatHelper helper = new ListViewItemFormatHelper();
		event.getText().add(helper.getFormattedWind(this.getContext(), (float)this.windSpeed, (float)this.windDirection));
		return true;
	}
}
