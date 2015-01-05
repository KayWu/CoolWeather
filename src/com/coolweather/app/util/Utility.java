package com.coolweather.app.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.coolweather.app.model.City;
import com.coolweather.app.model.CoolWeatherDB;
import com.coolweather.app.model.County;
import com.coolweather.app.model.Province;

public class Utility {
	/**
	 * 解析和处理服务器返回的省级数据
	 */
	public synchronized static boolean handleProvincesResponse(CoolWeatherDB coolWeatherDB,
			String response) {
		if (!TextUtils.isEmpty(response)) {
			// 使用正则表达式筛选出""里面的内容
			Pattern pattern = Pattern.compile("\"(.*?)\"");
			Matcher matcher = pattern.matcher(response);
			int times = 0;
			Province province = null;
			while (matcher.find()) {
				String provinceInfo = matcher.group().replace("\"", "");
				if (times++ % 2 == 0) {
					province = new Province();
					province.setProvinceCode(provinceInfo);
				} else {
					province.setProvinceName(provinceInfo);
					coolWeatherDB.saveProvince(province);
				}
			}
			return true;
		}
		return false;
	}

	/**
	 * 解析和处理服务器返回的市级数据
	 */
	public static boolean handleCitiesResponse(CoolWeatherDB coolWeatherDB, String response,
			Province province) {
		if (!TextUtils.isEmpty(response)) {
			Pattern pattern = Pattern.compile("\"(.*?)\"");
			Matcher matcher = pattern.matcher(response);
			int times = 0;
			City city = null;
			while (matcher.find()) {
				String cityInfo = matcher.group().replace("\"", "");
				if (times++ % 2 == 0) {
					city = new City();
					city.setProvinceId(province.getId());
					city.setCityCode(province.getProvinceCode() + cityInfo);
				} else {
					city.setCityName(cityInfo);
					coolWeatherDB.saveCity(city);
				}
			}
			return true;
		}
		return false;
	}

	/**
	 * 解析和处理服务器返回的县级数据
	 */
	public static boolean handleCountiesResponse(CoolWeatherDB coolWeatherDB, String response,
			City city) {
		if (!TextUtils.isEmpty(response)) {
			Pattern pattern = Pattern.compile("\"(.*?)\"");
			Matcher matcher = pattern.matcher(response);
			int times = 0;
			County county = null;
			while (matcher.find()) {
				String countyInfo = matcher.group().replace("\"", "");
				if (times++ % 2 == 0) {
					county = new County();
					county.setCityId(city.getId());
					county.setCountyCode(city.getCityCode() + countyInfo);
				} else {
					county.setCountyName(countyInfo);
					coolWeatherDB.saveCounty(county);
				}
			}
			return true;
		}
		return false;
	}

	
	/**
	 * 解析服务器返回的JSON数据，并将解析出的数据存储到本地 
	 */
	public static boolean handleWeatherResponse(Context context, String response) {
		if (!TextUtils.isEmpty(response)) {
			try {
				JSONObject jsonObject = new JSONObject(response);
				JSONObject weatherInfo = jsonObject.getJSONObject("weatherinfo");
				String cityName = weatherInfo.getString("city");
				String countyCode = weatherInfo.getString("cityid");
				String temp1 = weatherInfo.getString("temp1");
				String temp2 = weatherInfo.getString("temp2");
				String weatherDesp = weatherInfo.getString("weather");
				String publishTime = weatherInfo.getString("ptime");
				saveWeatherInfo(context, cityName, countyCode, temp1, temp2, weatherDesp,
						publishTime);
				return true;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		} else {
			return false;
		}
	}

	/**
	 * 将服务器返回的所有天气信息存储到SharedPreferences文件中
	 */
	public static void saveWeatherInfo(Context context, String cityName, String countyCode,
			String temp1, String temp2, String weatherDesp, String publishTime) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy年M月d日", Locale.CHINA);
		SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context)
				.edit();
		editor.putBoolean("city_selected", true);
		editor.putString("city_name", cityName);
		editor.putString("county_code", countyCode);
		editor.putString("temp1", temp1);
		editor.putString("temp2", temp2);
		editor.putString("weather_desp", weatherDesp);
		editor.putString("publish_time", publishTime);
		editor.putString("current_date", sdf.format(new Date()));
		editor.commit();
	}

}
