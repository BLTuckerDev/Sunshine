package com.example.brett.sunshine;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class DetailFragment extends Fragment {

	private String forecast;

	public DetailFragment(){
		this.setHasOptionsMenu(true);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		ShareActionProvider provider = (ShareActionProvider) MenuItemCompat.getActionProvider(menu.findItem(R.id.action_share_weather));
		provider.setShareIntent(getShareIntent());
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}


	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Bundle extras = getActivity().getIntent().getExtras();

		if(extras != null){
			forecast = extras.getString(DetailActivity.IntentExtras.Forecast);
		}
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

		TextView label = (TextView) rootView.findViewById(R.id.detail_fragment_label);
		label.setText(forecast);

		return rootView;
	}


	private Intent getShareIntent(){
		Intent shareIntent = new Intent(Intent.ACTION_SEND);

		shareIntent.setType("text/plain");
		shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Weather Forecast");
		shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
		shareIntent.putExtra(Intent.EXTRA_TEXT, forecast + "#sunshine");

		return shareIntent;
	}
}
