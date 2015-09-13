package com.example.brett.sunshine;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.example.brett.sunshine.data.WeatherContract;


public final class ForecastAdapter extends CursorAdapter {

	private final static int VIEW_TYPE_TODAY = 0;
	private final static int VIEW_TYPE_FUTURE_DAY = 1;

	private boolean useTodayLayout = false;

	public ForecastAdapter(Context context, Cursor c, int flags) {
		super(context, c, flags);
	}


	public void setUseTodayLayout(boolean useToday){
		this.useTodayLayout = useToday;
	}

	@Override
	public int getItemViewType(int position) {
		return (position == 0 && useTodayLayout) ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE_DAY;
	}


	@Override
	public int getViewTypeCount() {
		return 2;
	}


	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		int viewType = getItemViewType(cursor.getPosition());

		View inflatedView;

		if(viewType ==  VIEW_TYPE_TODAY){
			inflatedView = LayoutInflater.from(context).inflate(R.layout.list_item_forecast_today, parent, false);
		} else {
			inflatedView = LayoutInflater.from(context).inflate(R.layout.list_item_forecast, parent, false);
		}

		ForecastListItemViewHolder vh = new ForecastListItemViewHolder(inflatedView);
		inflatedView.setTag(vh);
		return inflatedView;
	}


	@Override
	public void bindView(View view, Context context, Cursor cursor) {

		ForecastListItemViewHolder viewHolder = (ForecastListItemViewHolder) view.getTag();
		WeatherFormatHelper formatHelper = new WeatherFormatHelper();

		int weatherId = cursor.getInt(cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID));
		this.bindIcon(viewHolder, weatherId, cursor.getPosition());


		viewHolder.dateView.setText(formatHelper.getFriendlyDayString(context, cursor.getString(cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_DATETEXT))));


		String descriptionString = WeatherResourceConverter.getConverter().getStringForWeatherCondition(context, weatherId);
		viewHolder.descriptionView.setText(descriptionString);
		viewHolder.descriptionView.setContentDescription(context.getString(R.string.a11y_forecast, descriptionString));


		boolean isMetric = formatHelper.isMetric(context);
		String highString = formatHelper.formatTemperature(context, cursor.getDouble(cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP)), isMetric);
		viewHolder.highView.setText(highString);
        viewHolder.highView.setContentDescription(context.getString(R.string.a11y_high_temp, highString));

		String lowString = formatHelper.formatTemperature(context, cursor.getDouble(cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP)), isMetric);
		viewHolder.lowView.setText(lowString);
        viewHolder.lowView.setContentDescription(context.getString(R.string.a11y_low_temp, lowString));

	}


	private void bindIcon(ForecastListItemViewHolder viewHolder, int weatherId, int position){

		int viewType = getItemViewType(position);
        int fallbackIconId;

		switch(viewType){
			case VIEW_TYPE_FUTURE_DAY:
                fallbackIconId = WeatherResourceConverter.getConverter().getArtResourceForWeatherCondition(weatherId);
				break;
            default:
                fallbackIconId = WeatherResourceConverter.getConverter().getIconResourceForWeatherCondition(weatherId);
				break;
		}

        Glide.with(mContext)
                .load(WeatherResourceConverter.getConverter().getArtUrlForWeatherCondition(mContext, weatherId))
                .error(fallbackIconId)
                .crossFade()
                .into(viewHolder.iconView);

	}

}
