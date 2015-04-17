package com.myattendance.checkattendance;

import com.netcse.myattendance.R;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class CheckAttendance extends Activity implements OnClickListener{
	
	public static EditText CID, DPT, BATCH, GRP; 
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.check_attendance);
		
		setTitle("Check Attendance");
		
		CID = (EditText) findViewById(R.id.editText1);
		CID.setText("CSE3200");
		
		DPT = (EditText) findViewById(R.id.editText2);
		DPT.setText("CSE");
		DPT.setOnFocusChangeListener(new OnFocusChangeListener() 
		{
			
			@Override
			public void onFocusChange(View arg0, boolean arg1) 
			{
				String s = DPT.getText().toString() + "";
				Toast.makeText(getApplicationContext(), "Focus: " + s,
						Toast.LENGTH_SHORT).show();
				
				if(s.equalsIgnoreCase("ME") ||
						s.equalsIgnoreCase("CE")||
						s.equalsIgnoreCase("EEE"))
				{
					GRP.setEnabled(true);
				}
			}
		});
		
		BATCH = (EditText) findViewById(R.id.batchField);
		BATCH.setText("11");
		
		GRP = (EditText) findViewById(R.id.groupField);
		GRP.setText("A");
		GRP.setEnabled(false);
		
		Button button = (Button) findViewById(R.id.checkAttendance);
		button.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) 
	{
		switch (v.getId()) 
		{
		case R.id.checkAttendance:
			Log.d("check: ", CID.getText().toString() + " : " + DPT.getText().toString());
			Intent primaryIntent = new Intent(CheckAttendance.this, ShowPercentage.class);
			startActivity(primaryIntent);
			break;

		default:
			break;
		}
		
	}

}
