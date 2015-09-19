package com.example.brett.sunshine;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.example.brett.sunshine.data.WeatherContract;


public final class ForecastAdapter extends RecyclerView.Adapter<ForecastListItemViewHolder> {

    private final static int VIEW_TYPE_TODAY = 0;
    private final static int VIEW_TYPE_FUTURE_DAY = 1;

    public static interface ForecastAdapterOnClickHandler{
        void onClick(int adapterPosition, ForecastListItemViewHolder viewHolder);
    }

    private boolean useTodayLayout = false;

    private Cursor weatherCursor;
    private final Context context;

    private final View emptyView;
    private final ForecastAdapterOnClickHandler clickHandler;


    public ForecastAdapter(Context context, ForecastAdapterOnClickHandler clickHandler, View emptyView) {
        this.context = context;
        this.clickHandler = clickHandler;
        this.emptyView = emptyView;
    }


    @Override
    public ForecastListItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        if (!(viewGroup instanceof RecyclerView)) {
            throw new RuntimeException("Not bound to a RecycleViewSelection");
        }

        int layoutId = -1;

        switch (viewType) {

            case VIEW_TYPE_TODAY:
                layoutId = R.layout.list_item_forecast_today;
                break;

            case VIEW_TYPE_FUTURE_DAY:
                layoutId = R.layout.list_item_forecast;
                break;
        }

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(layoutId, viewGroup, false);
        view.setFocusable(true);

        return new ForecastListItemViewHolder(view, clickHandler);

    }


    @Override
    public void onBindViewHolder(ForecastListItemViewHolder viewHolder, int position) {
        weatherCursor.moveToPosition(position);

        int weatherId = weatherCursor.getInt(weatherCursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID));
        WeatherFormatHelper formatHelper = new WeatherFormatHelper();

        this.bindIcon(viewHolder, weatherId, weatherCursor.getPosition());

        viewHolder.dateView.setText(formatHelper.getFriendlyDayString(context, weatherCursor.getString(weatherCursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_DATETEXT))));


        String descriptionString = WeatherResourceConverter.getConverter().getStringForWeatherCondition(context, weatherId);
        viewHolder.descriptionView.setText(descriptionString);
        viewHolder.descriptionView.setContentDescription(context.getString(R.string.a11y_forecast, descriptionString));


        boolean isMetric = formatHelper.isMetric(context);
        String highString = formatHelper.formatTemperature(context, weatherCursor.getDouble(weatherCursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP)), isMetric);
        viewHolder.highView.setText(highString);
        viewHolder.highView.setContentDescription(context.getString(R.string.a11y_high_temp, highString));

        String lowString = formatHelper.formatTemperature(context, weatherCursor.getDouble(weatherCursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP)), isMetric);
        viewHolder.lowView.setText(lowString);
        viewHolder.lowView.setContentDescription(context.getString(R.string.a11y_low_temp, lowString));

    }


    @Override
    public int getItemCount() {
        if (null == weatherCursor) {
            return 0;
        } else {
            return weatherCursor.getCount();
        }
    }

    public void swapCursor(Cursor newCursor) {
        weatherCursor = newCursor;
        notifyDataSetChanged();
    }

    public Cursor getWeatherCursor() {
        return weatherCursor;
    }

    public void setUseTodayLayout(boolean useToday) {
        this.useTodayLayout = useToday;
    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0 && useTodayLayout) ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE_DAY;
    }


    private void bindIcon(ForecastListItemViewHolder viewHolder, int weatherId, int position) {

        int viewType = getItemViewType(position);
        int fallbackIconId;

        switch (viewType) {
            case VIEW_TYPE_FUTURE_DAY:
                fallbackIconId = WeatherResourceConverter.getConverter().getArtResourceForWeatherCondition(weatherId);
                break;
            default:
                fallbackIconId = WeatherResourceConverter.getConverter().getIconResourceForWeatherCondition(weatherId);
                break;
        }


        Glide.with(this.context)
                .load(WeatherResourceConverter.getConverter().getArtUrlForWeatherCondition(this.context, weatherId))
                .error(fallbackIconId)
                .crossFade()
                .into(viewHolder.iconView);

    }

}
