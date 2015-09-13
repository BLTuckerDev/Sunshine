package com.example.brett.sunshine;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public final class ForecastListItemViewHolder {

	public final ImageView iconView;
	public final TextView dateView;
	public final TextView descriptionView;
	public final TextView highView;
	public final TextView lowView;

	public ForecastListItemViewHolder(View parentView){
		iconView = (ImageView) parentView.findViewById(R.id.list_item_icon);
		dateView = (TextView) parentView.findViewById(R.id.list_item_date_textview);
		descriptionView = (TextView) parentView.findViewById(R.id.list_item_forecast_textview);
		highView = (TextView) parentView.findViewById(R.id.list_item_high_textview);
		lowView = (TextView) parentView.findViewById(R.id.list_item_low_textview);
	}


}
