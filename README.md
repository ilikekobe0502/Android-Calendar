# Android-Calendar
add event、delete event、edit event



android Calendar Provider 有兩種處理方式，google官方比較推薦使用Intent的方式去做新增、修改、刪除的處理，當然如果需要自己實作
一個行事曆可能就不太適用了。
使用Intent做行事曆的東做簡單很多，如果只是要使用基本的行事曆組成建議使用Intent新增:
```
Calendar beginTime = Calendar.getInstance();
					beginTime.set(2012, 0, 19, 7, 30);
					Calendar endTime = Calendar.getInstance();
					endTime.set(2012, 0, 19, 8, 30);
					Intent intent = new Intent(Intent.ACTION_INSERT)
							.setData(Events.CONTENT_URI)
							.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTime.getTimeInMillis())
							.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime.getTimeInMillis())
							.putExtra(Events.TITLE, "Yoga")
							.putExtra(Events.DESCRIPTION, "Group class")
							.putExtra(Events.EVENT_LOCATION, "The gym")
							.putExtra(Events.AVAILABILITY, Events.AVAILABILITY_BUSY)
							.putExtra(Intent.EXTRA_EMAIL, "rowan@example.com,trevor@example.com");
					startActivity(intent);
```
另一種比較麻煩需要自己續了解calendar的相關參數才有辦法去做處理
URI:content://com.android.calendar/events來做對於每一筆事件的處理 
用法就像DB一樣下指令  
```
	Cursor cursor = context.getContentResolver()  
					.query(Uri.parse("content://com.android.calendar/events"),  
							new String[] { "calendar_id", "title", "description", //取得要處理的欄位
									"dtstart", "dtend", "eventLocation"}, null, 
							null, null);  
			cursor.moveToFirst();
```

新增:
```
	ContentResolver cr = getContentResolver();
	ContentValues values = new ContentValues();
	values.put(Events.DTSTART, startMillis);
					values.put(Events.DTEND, endMillis);
					values.put(Events.TITLE, mTitle);
					values.put(Events.DESCRIPTION, mDiscriber);
					values.put(Events.CALENDAR_ID, calID);
					values.put(Events.EVENT_TIMEZONE, "Taiwan/Taipei");
					values.put(Events.EVENT_LOCATION, mLocation);
					AsyncQueryHandler handler = new MyHandler(getContentResolver()); //搭配AsyncQueryHandler
					handler.startInsert(0, null, Events.CONTENT_URI, values);
```
需要搭配AsyncQueryHandler來做新增
```
	/**
	 * AsyncQueryHandler
	 */
	public class MyHandler extends AsyncQueryHandler {
		public MyHandler(ContentResolver cr) {
			super(cr);
		}
	}

```
修改:
```
	values.put(Events.TITLE, mTitle);//需要更改的欄位及更改後的值   {values.put(Events.欄位, 更改後的值);}
	Uri updateUri = ContentUris.withAppendedId(Events.CONTENT_URI, Event_ID); //Event_ID為要修改事件的Event_id
	getContentResolver().update(updateUri, values, null, null);
```
刪除:
```
	Uri deleteUri = ContentUris.withAppendedId(Events.CONTENT_URI, Event_ID);//Event_ID為要刪除事件的Event_id
	getApplication().getContentResolver().delete(deleteUri, null, null);
```

若是再深層一點的處理，例如要新增自己的Owner
URI:content://com.android.calendar/calendars則是做對於每一種類的資料表做處理  
新增:必須搭配Sync Adapters來做同步的動作
```
	/**
	 * Sync Adapters 搭配創建一個新的Calendars資料表
	 * @return
	 */
	private static Uri buildCalUri() {
		return CAL_URI
				.buildUpon()
				.appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
				.appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, "Neo_Hu@huanuage.com")
				.appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE,CalendarContract.ACCOUNT_TYPE_LOCAL)
				.build();
	}
```
然後放上新的Calendars的資料
```
/**
	 * 新的Calendars的基本資料
	 * @return
	 */
	private static ContentValues buildNewCalContentValues() {
		final ContentValues cv = new ContentValues();
		cv.put(CalendarContract.Calendars.ACCOUNT_NAME, "Neo_Hu@huanuage.com");
		cv.put(CalendarContract.Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL);
		cv.put(CalendarContract.Calendars.NAME, "Huanauge");
		cv.put(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, "Huanuage行事曆");
		cv.put(CalendarContract.Calendars.CALENDAR_COLOR, 0xEA8561);
		cv.put(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL, CalendarContract.Calendars.CAL_ACCESS_EDITOR);
//		cv.put(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL, CalendarContract.Calendars.CAL_ACCESS_READ);//使用者無法編輯
		cv.put(CalendarContract.Calendars.OWNER_ACCOUNT, "Neo_Hu@huanuage.com");
		cv.put(CalendarContract.Calendars.VISIBLE, 1);//是否顯示
		cv.put(CalendarContract.Calendars.SYNC_EVENTS, 1);//是否要再裝置上同步
		return cv;
	}
```
最後放上執行的code即可
```
ContentResolver cr = getContentResolver();
final ContentValues cv = buildNewCalContentValues();
Uri calUri = buildCalUri();
//insert the calendar into the database
cr.insert(calUri, cv);
```
刪除:
```
	//刪除Calendars資料表事件所屬種類ID = 6 的資料
	/*
	Uri deleteUri = ContentUris.withAppendedId(CalendarContract.Calendars.CONTENT_URI, 6);
	getApplication().getContentResolver().delete(deleteUri, null, null);
	*/
```
修改
```
//將Calendars資料表內 ID = 6 的資料改成4
/*
ContentValues values = new ContentValues();
Uri updateUri = ContentUris.withAppendedId(CalendarContract.Calendars.CONTENT_URI, 6);
values.put(CalendarContract.Calendars._ID , 4 );//將事件所屬種類ID = 6的資料改成4
getContentResolver().update(updateUri, values, null, null);
cursor.moveToNext();
*/
```
