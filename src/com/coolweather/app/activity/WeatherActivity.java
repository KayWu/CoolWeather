package com.coolweather.app.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.coolweather.app.R;
import com.coolweather.app.service.AutoUpdateService;
import com.coolweather.app.util.HttpCallbackListener;
import com.coolweather.app.util.HttpUtil;
import com.coolweather.app.util.Utility;

public class WeatherActivity extends Activity implements OnClickListener {

	private LinearLayout layoutWeatherInfo;
	private TextView tvCity;
	private TextView tvPublish;
	private TextView tvWeatherDesp;
	private TextView tvTemp1;
	private TextView tvTemp2;
	private TextView tvCurrentDate;
	private Button btnSwitchCity;
	private Button btnRefreshWeather;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.weather_layout);

		// 初始化各控件
		layoutWeatherInfo = (LinearLayout) findViewById(R.id.layout_weather_info);
		tvCity = (TextView) findViewById(R.id.tv_city);
		tvPublish = (TextView) findViewById(R.id.tv_publish);
		tvWeatherDesp = (TextView) findViewById(R.id.tv_weather_desp);
		tvTemp1 = (TextView) findViewById(R.id.tv_temp1);
		tvTemp2 = (TextView) findViewById(R.id.tv_temp2);
		tvCurrentDate = (TextView) findViewById(R.id.tv_current_date);
		btnSwitchCity = (Button) findViewById(R.id.btn_switch_city);
		btnRefreshWeather = (Button) findViewById(R.id.btn_refresh_weather);
		btnSwitchCity.setOnClickListener(this);
		btnRefreshWeather.setOnClickListener(this);

		String countyCode = getIntent().getStringExtra("county_code");
		if (!TextUtils.isEmpty(countyCode)) {
			// 有县级代号时就去查询天气
			tvPublish.setText("同步中...");
			layoutWeatherInfo.setVisibility(View.INVISIBLE);
			tvCity.setVisibility(View.INVISIBLE);
			queryWeatherInfo(countyCode);
		} else {
			// 没有县级代码时就显示本地天气
			showWeather();
		}

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_switch_city:
			Intent intent = new Intent(this, ChooseAreaActivity.class);
			intent.putExtra("from_weather_activity", true);
			startActivity(intent);
			finish();
			break;
		case R.id.btn_refresh_weather:
			tvPublish.setText("同步中...");
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			String countyCode = prefs.getString("county_code", "");
			if (!TextUtils.isEmpty(countyCode)) {
				queryWeatherInfo(countyCode);
			}
			break;
		}
	}

	public void queryWeatherInfo(String countyCode) {
		String address = "http://www.weather.com.cn/data/cityinfo/" + countyCode + ".html";
		queryFromServer(address);
	}

	/**
	 * 根据传入的地址去服务器查询天气信息
	 */
	private void queryFromServer(final String address) {
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {

			@Override
			public void onFinish(String response) {
				// 处理服务器返回的天气信息
				if (Utility.handleWeatherResponse(WeatherActivity.this, response)) {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							showWeather();
						}
					});
				} else {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							tvPublish.setText("同步失败");
						}
					});
				}
			}

			@Override
			public void onError(Exception e) {
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						tvPublish.setText("同步失败");
					}
				});
			}
		});
	}

	private void showWeather() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		tvCity.setText(prefs.getString("city_name", ""));
		tvTemp1.setText(prefs.getString("temp1", ""));
		tvTemp2.setText(prefs.getString("temp2", ""));
		tvWeatherDesp.setText(prefs.getString("weather_desp", ""));
		tvPublish.setText("今天" + prefs.getString("publish_time", "") + "发布");
		tvCurrentDate.setText(prefs.getString("current_date", ""));
		layoutWeatherInfo.setVisibility(View.VISIBLE);
		tvCity.setVisibility(View.VISIBLE);
		Intent intent = new Intent(this, AutoUpdateService.class);
		startService(intent);
	}

}
