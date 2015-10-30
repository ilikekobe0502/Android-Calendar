package com.examples.android.calendar;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import android.app.*;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.provider.CalendarContract.Events;

public class CalendarView extends Activity {
	private static final String DEBUG_TAG = "CalendarView";

	private Button mButton_newEvent, mButton_edit, mButton_delete, mButton_huanuage;

	public GregorianCalendar month, itemmonth;// calendar instances.

	public CalendarAdapter adapter;// adapter instance
	public Handler handler;// for grabbing some event values for showing the dot
							// marker.
	public ArrayList<String> items; // container to store calendar items which
									// needs showing the event marker
	ArrayList<String> event;
	LinearLayout rLayout;
	ArrayList<String> date;
	ArrayList<String> desc;
	Uri uri;
	private long Event_ID = 0;
	private long calID = 6;
	private long startMillis = 0;
	private long endMillis = 0;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.calendar);
		Locale.setDefault(Locale.US);

		mButton_newEvent = (Button)findViewById(R.id.button_newEvent);
        mButton_newEvent.setText(R.string.newEvent);
		mButton_edit = (Button)findViewById(R.id.button_edit);
		mButton_edit.setText(R.string.edit);
		mButton_delete = (Button)findViewById(R.id.button_delete);
		mButton_delete.setText(R.string.delete);
		mButton_huanuage = (Button)findViewById(R.id.button_huanuage);
		mButton_huanuage.setText(R.string.huanuage);

		rLayout = (LinearLayout) findViewById(R.id.text);
		month = (GregorianCalendar) GregorianCalendar.getInstance();
		itemmonth = (GregorianCalendar) month.clone();

		items = new ArrayList<String>();

		adapter = new CalendarAdapter(this, month);

		GridView gridview = (GridView) findViewById(R.id.gridview);
		gridview.setAdapter(adapter);

		handler = new Handler();
		handler.post(calendarUpdater);

		TextView title = (TextView) findViewById(R.id.title);
		title.setText(android.text.format.DateFormat.format("MMMM yyyy", month));

		RelativeLayout previous = (RelativeLayout) findViewById(R.id.previous);

		//切換上個月
		previous.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				setPreviousMonth();
				refreshCalendar();
			}
		});

		//切換下個月
		RelativeLayout next = (RelativeLayout) findViewById(R.id.next);
		next.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				setNextMonth();
				refreshCalendar();

			}
		});

		/**
		 * 新增按鈕
		 */
		mButton_newEvent.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {

				new MyDialog(CalendarView.this, 0).show();


			}
		});

		/**
		 * 修改按鈕
		 */
		mButton_edit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				new MyDialog(CalendarView.this, 1).show();


			}
		});

		/**
		 * 刪除按鈕
		 */
		mButton_delete.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				//substitue your calendar id into the 0
				Uri deleteUri = ContentUris.withAppendedId(Events.CONTENT_URI, Event_ID);
				getApplication().getContentResolver().delete(deleteUri, null, null);
				Log.i(DEBUG_TAG, "刪除: " + Event_ID);

				refreshCalendar();
			}
		});

		/**
		 *點選日曆中的某一天
		 */
		gridview.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {



                // removing the previous view if added
                if (((LinearLayout) rLayout).getChildCount() > 0) {
                    ((LinearLayout) rLayout).removeAllViews();
                }
                desc = new ArrayList<String>();
                date = new ArrayList<String>();
                ((CalendarAdapter) parent.getAdapter()).setSelected(v);
                String selectedGridDate = CalendarAdapter.dayString
                        .get(position);
                String[] separatedTime = selectedGridDate.split("-");
                String gridvalueString = separatedTime[2].replaceFirst("^0*",
                        "");// taking last part of date. ie; 2 from 2012-12-02.
                int gridvalue = Integer.parseInt(gridvalueString);
                // navigate to next or previous month on clicking offdays.
                if ((gridvalue > 10) && (position < 8)) {
                    setPreviousMonth();
                    refreshCalendar();
                } else if ((gridvalue < 7) && (position > 28)) {
                    setNextMonth();
                    refreshCalendar();
                }
                ((CalendarAdapter) parent.getAdapter()).setSelected(v);

                for (int i = 0; i < Utility.startDates.size(); i++) {
                    if (Utility.startDates.get(i).equals(selectedGridDate)) {
                        desc.add(Utility.nameOfEvent.get(i));
						desc.add(Utility.calendar_id.get(i));
						desc.add(String.valueOf(Utility.ListSelectedCalendars(getApplication(), Utility.nameOfEvent.get(i))));
						Event_ID = Utility.ListSelectedCalendars(getApplication(), Utility.nameOfEvent.get(i));

					}
                }

                if (desc.size() > 0) {
                    for (int i = 0; i < desc.size(); i++) {
                        TextView rowTextView = new TextView(CalendarView.this);

                        // set some properties of rowTextView or something
                        rowTextView.setText("Event:" + desc.get(i));
                        rowTextView.setTextColor(Color.BLACK);

                        // add the textview to the linearlayout
                        rLayout.addView(rowTextView);
                    }

                }

                desc = null;

            }

        });
	}

	/**
	 * 新增、修改事件
	 * @param mTitle
	 * @param mDiscriber
	 * @param mLocation
	 */
	public void newEvent(String mTitle, String mDiscriber, String mLocation, int type){
		ContentResolver cr = getContentResolver();
		ContentValues values = new ContentValues();
		switch (type) {
			case 0://新增

				values.put(Events.DTSTART, startMillis);
				values.put(Events.DTEND, endMillis);
				values.put(Events.TITLE, mTitle);
				values.put(Events.DESCRIPTION, mDiscriber);
				values.put(Events.CALENDAR_ID, calID);
				values.put(Events.EVENT_TIMEZONE, "Taiwan/Taipei");
				values.put(Events.EVENT_LOCATION, mLocation);
	//		uri = cr.insert(Events.CONTENT_URI, values);
				AsyncQueryHandler handler = new MyHandler(getContentResolver());
				handler.startInsert(0, null, Events.CONTENT_URI, values);

				Log.i(DEBUG_TAG, "新增: " + mTitle);

				refreshCalendar();

				/**
				 * 意圖新增event
				 */
	//				Calendar beginTime = Calendar.getInstance();
	//				beginTime.set(2012, 0, 19, 7, 30);
	//				Calendar endTime = Calendar.getInstance();
	//				endTime.set(2012, 0, 19, 8, 30);
	//				Intent intent = new Intent(Intent.ACTION_INSERT)
	//						.setData(Events.CONTENT_URI)
	//						.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTime.getTimeInMillis())
	//						.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime.getTimeInMillis())
	//						.putExtra(Events.TITLE, "Yoga")
	//						.putExtra(Events.DESCRIPTION, "Group class")
	//						.putExtra(Events.EVENT_LOCATION, "The gym")
	//						.putExtra(Events.AVAILABILITY, Events.AVAILABILITY_BUSY)
	//						.putExtra(Intent.EXTRA_EMAIL, "rowan@example.com,trevor@example.com");
	//				startActivity(intent);
				break;
			case 1://修改
				Uri updateUri = null;
				// The new title for the event
				if(!mTitle.toString().equals("")) {
					values.put(Events.TITLE, mTitle);
					updateUri = ContentUris.withAppendedId(Events.CONTENT_URI, Event_ID);
					getContentResolver().update(updateUri, values, null, null);
					Log.i(DEBUG_TAG, "修改: " + values);
					refreshCalendar();
				}
				break;
		}
	}

	/**
	 * 編輯行事曆資訊
	 */
	public class MyDialog extends Dialog{
		private Button mButotn_ok;
		public DatePicker mDatePicker;
		public TimePicker mTimePicker_start, mTimePicker_end;
		private EditText mEditText_Title, mEditText_Discriber, mEditText_Location;

		private String mTitle, mDiscriber, mLocation;

		public MyDialog(Context context, int type) {
			super(context, android.R.style.Theme_Light);
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			setContentView(R.layout.dialog_new_event);

			mButotn_ok = (Button)findViewById(R.id.button_ok);
			mDatePicker = (DatePicker)findViewById(R.id.datePicker);
			mTimePicker_start = (TimePicker)findViewById(R.id.timePicker_start);
			mTimePicker_end = (TimePicker)findViewById(R.id.timePicker_end);
			mEditText_Title = (EditText)findViewById(R.id.editText_Title);
			mEditText_Discriber = (EditText)findViewById(R.id.editText_Discriber);
			mEditText_Location = (EditText)findViewById(R.id.editText_Location);


			button(type);
		}

		private void button(final int type) {
			mButotn_ok.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Calendar beginTime = Calendar.getInstance();
					beginTime.set(mDatePicker.getYear(), mDatePicker.getMonth(), mDatePicker.getDayOfMonth(), mTimePicker_start.getCurrentHour(), mTimePicker_start.getCurrentMinute());
					startMillis = beginTime.getTimeInMillis();

					Calendar endTime = Calendar.getInstance();
					endTime.set(mDatePicker.getYear(), mDatePicker.getMonth(), mDatePicker.getDayOfMonth(), mTimePicker_end.getCurrentHour(), mTimePicker_end.getCurrentMinute());
					endMillis = endTime.getTimeInMillis();

					mTitle = mEditText_Title.getText().toString();
					mDiscriber = mEditText_Discriber.getText().toString();
					mLocation = mEditText_Location.getText().toString();

					newEvent(mTitle, mDiscriber, mLocation,type);
					MyDialog.this.onBackPressed();

				}
			});
		}

	}


	protected void setNextMonth() {
		if (month.get(GregorianCalendar.MONTH) == month
				.getActualMaximum(GregorianCalendar.MONTH)) {
			month.set((month.get(GregorianCalendar.YEAR) + 1),
					month.getActualMinimum(GregorianCalendar.MONTH), 1);
		} else {
			month.set(GregorianCalendar.MONTH,
					month.get(GregorianCalendar.MONTH) + 1);
		}

	}

	protected void setPreviousMonth() {
		if (month.get(GregorianCalendar.MONTH) == month
				.getActualMinimum(GregorianCalendar.MONTH)) {
			month.set((month.get(GregorianCalendar.YEAR) - 1),
					month.getActualMaximum(GregorianCalendar.MONTH), 1);
		} else {
			month.set(GregorianCalendar.MONTH,
					month.get(GregorianCalendar.MONTH) - 1);
		}

	}

	protected void showToast(String string) {
		Toast.makeText(this, string, Toast.LENGTH_SHORT).show();

	}

	/**
	 * 更新行事曆
	 */
	public void refreshCalendar() {
		TextView title = (TextView) findViewById(R.id.title);

		adapter.refreshDays();
		adapter.notifyDataSetChanged();
		handler.post(calendarUpdater); // generate some calendar items

		title.setText(android.text.format.DateFormat.format("MMMM yyyy", month));
	}



	/**
	 * AsyncQueryHandler
	 */
	public class MyHandler extends AsyncQueryHandler {
		public MyHandler(ContentResolver cr) {
			super(cr);
		}
	}

	/**
	 * 	AsyncQueryHandler Runnable
	 */
	public Runnable calendarUpdater = new Runnable() {

		@Override
		public void run() {
			items.clear();

			// Print dates of the current week
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
			String itemvalue;
			event = Utility.readCalendarEvent(CalendarView.this);
			Log.d("=====Event====", event.toString());
			Log.d("=====Date ARRAY====", Utility.startDates.toString());

			for (int i = 0; i < Utility.startDates.size(); i++) {
				itemvalue = df.format(itemmonth.getTime());
				itemmonth.add(GregorianCalendar.DATE, 1);
				items.add(Utility.startDates.get(i).toString());
			}
			adapter.setItems(items);
			adapter.notifyDataSetChanged();
		}
	};

}
