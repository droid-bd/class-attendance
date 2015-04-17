package com.myattendance.takeattendance;

import java.util.Calendar;

import com.netcse.myattendance.R;

import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class AttendanceInfo extends Activity 
{
	public static String departmentName, courseNo;
	public static EditText deptField, courseNoField, dateField, batchField, groupField;
	//public static AutoCompleteTextView deptField;
	private Button TakeAttendanceButton;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_attendance_info);
		
	    setTitle("Take Attendance!");
		
	    Calendar cal = Calendar.getInstance();
		int month = cal.get(Calendar.MONTH);
		int day = cal.get(Calendar.DAY_OF_MONTH);
		int year = cal.get(Calendar.YEAR);
		int day_no = cal.get(Calendar.DAY_OF_WEEK);
		
		String dt = day + "/" + (month+1) + "/" + year;
	    
		String[] countries = getResources().
				   getStringArray(R.array.list_of_dept);
		ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1, countries);
		
		deptField = (EditText) findViewById(R.id.deptID);
		//deptField = (AutoCompleteTextView) findViewById(R.id.deptID);
		//deptField.setAdapter(adapter);
		deptField.setText("CSE");
		deptField.setOnFocusChangeListener(new OnFocusChangeListener() 
		{
			@Override
			public void onFocusChange(View arg0, boolean arg1) 
			{
				String s = deptField.getText().toString() + "";
				if(s.equalsIgnoreCase("ME") ||
						s.equalsIgnoreCase("CE")||
						s.equalsIgnoreCase("EEE"))
				{
					groupField.setEnabled(true);
				}
			}
		});
		courseNoField = (EditText) findViewById(R.id.courseNo);
		courseNoField.setText("CSE3200");
		dateField = (EditText) findViewById(R.id.dateField);
		dateField.setText(dt);
		batchField = (EditText) findViewById(R.id.batchField);
		batchField.setText("11");
		groupField = (EditText) findViewById(R.id.groupField);
		groupField.setText("A");
		groupField.setEnabled(false);	


		TakeAttendanceButton = (Button) findViewById(R.id.takeAttendanceButton);
		TakeAttendanceButton.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View arg0) 
			{
				if(groupField.getText().toString().equalsIgnoreCase("A") || 
					groupField.getText().toString().equalsIgnoreCase("B"))
				{
					//Log.d("TLG: ", "ID: " + courseNoField.getText().toString() + ";" + " TPWD: " + deptField.getText().toString());
					Toast.makeText(getApplicationContext(),	"Taking attendance for Dept.: " + deptField.getText().toString().toUpperCase() + ", " + batchField.getText().toString() + " Batch.", Toast.LENGTH_LONG).show();
					Intent primaryIntent = new Intent(AttendanceInfo.this, AttendancePage.class);
					startActivity(primaryIntent);
				}
				else 
				{
					Toast.makeText(getApplicationContext(),	"Please check Section name.", Toast.LENGTH_LONG).show();
					return;
				}
			}
		});
	}

}
