package com.examples.android.calendar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class Utility {
	/**
	 * Calender Event Use
	 */
	public static ArrayList<String> calendar_id = new ArrayList<String>();//行事曆ID(種類)
	public static ArrayList<String> nameOfEvent = new ArrayList<String>();//事件名稱
	public static ArrayList<String> startDates = new ArrayList<String>();//開始時間
	public static ArrayList<String> endDates = new ArrayList<String>();//結束時間
	public static ArrayList<String> descriptions = new ArrayList<String>();//描述
	public static ArrayList<String> eventLocation = new ArrayList<String>();//地點
	public static ArrayList<String> event_id = new ArrayList<String>();//地點

	public static String a = null;
	public static String b = null;
	public static String c = null;
	public static String d = null;
	public static String e = null;
	public static String f = null;
	public static int g = 0;

	/**
	 * Calendars Use
	 */
	public static ArrayList<String> accunt_name = new ArrayList<String>();
	public static ArrayList<String> calendar_displayname = new ArrayList<String>();
	public static ArrayList<String> id = new ArrayList<String>();

	/**
	 * 讀取Calendars
	 * @param context
	 * @return
	 */
	public static ArrayList<String> readCalendars(Context context){

		Cursor cursor = context.getContentResolver()
				.query(Uri.parse("content://com.android.calendar/calendars"),//指向Calendars的資料表
						new String[] { "account_name","calendar_displayName","_id"}, null,//設定好要取出的欄位
						null, null);
		cursor.moveToFirst();

		String CNames[] = new String[cursor.getCount()];
		accunt_name.clear();
		calendar_displayname.clear();
		id.clear();

		for (int i = 0; i < CNames.length; i++) {
			accunt_name.add(cursor.getString(0));
			calendar_displayname.add(cursor.getString(1));
			id.add(cursor.getString(2));
			cursor.moveToNext();
		}
		Log.i("帳號 = ", "---" + accunt_name);
		Log.i("名稱 = ", "---" + calendar_displayname);
		Log.i("id = ", "---" + id);

		return accunt_name;
	}

	/**
	 * 讀取Calendar Event
	 * @param context
	 * @return
	 */
	public static ArrayList<String> readCalendarEvent(Context context) {
		Cursor cursor = context.getContentResolver()
				.query(Uri.parse("content://com.android.calendar/events"),
						new String[] { "calendar_id", "title", "description",
								"dtstart", "dtend", "eventLocation"}, null,
						null, null);
		cursor.moveToFirst();

		// fetching calendars name
		String CNames[] = new String[cursor.getCount()];


		// fetching calendars id
		calendar_id.clear();
		nameOfEvent.clear();
		startDates.clear();
		endDates.clear();
		descriptions.clear();
		eventLocation.clear();
		event_id.clear();
		for (int i = 0; i < CNames.length; i++) {
			calendar_id.add(cursor.getString(0));//1:google 2:google plus 3:系統 4:本機
			nameOfEvent.add(cursor.getString(1));
			descriptions.add(cursor.getString(2));
			startDates.add(getDate(Long.parseLong(cursor.getString(3))));
			endDates.add(getDate(Long.parseLong(cursor.getString(4))));
			eventLocation.add(cursor.getString(5));
			CNames[i] = cursor.getString(1);
			cursor.moveToNext();
//			Log.d("欄位6 =", ListSelectedCalendars(context, Utility.nameOfEvent.get(i)) + "  " + nameOfEvent.get(i));
		}
//		a = cursor.getColumnName(0);
//		b = cursor.getColumnName(1);
//		c = cursor.getColumnName(2);
//		d = cursor.getColumnName(3);
//		e = cursor.getColumnName(4);
//		f = cursor.getColumnName(5);


		Log.d("欄位0 = ", a + "---" + calendar_id);
		Log.d("欄位1 = ", b + "---" + nameOfEvent );
		Log.d("欄位2 = ", c + "---" + descriptions );
		Log.d("欄位3 = ", d + "---" + startDates );
		Log.d("欄位4 = ", e + "---" + endDates );
		Log.d("欄位5 = ", f + "---" + eventLocation);

		return nameOfEvent;
	}

	public static String getDate(long milliSeconds) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(milliSeconds);
		return formatter.format(calendar.getTime());
	}

	/**
	 * 取得EVENT_ID
	 * @param eventtitle
	 * @return
	 */
	public static int ListSelectedCalendars(Context context, String eventtitle) {


		Uri eventUri;
		if (android.os.Build.VERSION.SDK_INT <= 7) {
			// the old way

			eventUri = Uri.parse("content://calendar/events");
		} else {
			// the new way

			eventUri = Uri.parse("content://com.android.calendar/events");
		}

		int result = 0;
		String projection[] = { "_id", "title" };
		Cursor cursor = context.getContentResolver().query(eventUri, null, null, null,
				null);

		if (cursor.moveToFirst()) {

			String calName;
			String calID;

			int nameCol = cursor.getColumnIndex(projection[1]);
			int idCol = cursor.getColumnIndex(projection[0]);
			do {
				calName = cursor.getString(nameCol);
				calID = cursor.getString(idCol);

				if (calName != null && calName.contains(eventtitle)) {
					result = Integer.parseInt(calID);
				}

			} while (cursor.moveToNext());
			cursor.close();
		}

		return result;

	}


}
