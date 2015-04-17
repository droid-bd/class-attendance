package com.myattendance.takeattendance;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import com.netcse.myattendance.R;
import com.myattendance.teacherlogin.TeacherLgin;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("HandlerLeak")
public class AttendancePage extends Activity implements OnClickListener {

	private Button saveInPhoneButton, saveInServerButton;
	public static final String POST_URL = "http://netcsemilonkuet.byethost16.com/update_attendance.php"; //web server
	//public static final String POST_URL = "http://10.0.2.2/MyAttendance/update_attendance.php"; // local host

	public static final int SUCCESS = 1; // message for successful online operation
	public static final int FAILURE = 0;
	
	private int StartRoll;

	private ProgressDialog pd;
	SQLiteDatabase db;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_attendance_page);

		initiaLizeView();
		
		initializeAttendance();
	}
	
	public boolean wasStudentPresent(int roll)
	{
		int rll=0;
		String Roll = getFormattedRoll(roll);
		/*if(groupAB.equalsIgnoreCase("A"))
		{
			rll = roll;
		}
		else if(groupAB.equalsIgnoreCase("B"))
		{
			rll = 60 + roll;
		}
		
		if(rll<9) 
		{
			Roll = AttendanceInfo.batchField.getText().toString() + getDept() +"00"+ (rll+1);
		}
		else if(rll<99)
		{
			Roll = AttendanceInfo.batchField.getText().toString() + getDept() +"0"+ (rll+1);
		}
		else 
		{
			Roll = AttendanceInfo.batchField.getText().toString() + getDept() +""+ (rll+1);
		}*/
		
		Cursor cursor = db.rawQuery("SELECT COUNT(TEACHERID) AS CNT FROM ATTENDANCE WHERE " +
				"TEACHERID='" + TeacherLgin.TID.getText().toString() +
				"' AND STUDENTID='" + Roll +
				"' AND GROUPD='" + groupAB + "'" +
				" AND COURSEID='" + AttendanceInfo.courseNoField.getText().toString().toUpperCase() + "'" +
				" AND DEPTID='" + AttendanceInfo.deptField.getText().toString().toUpperCase() + "'" +
				" AND DATE='" + AttendanceInfo.dateField.getText().toString() + "'",
				null);
		cursor.moveToFirst();
		int toc = Integer.parseInt(cursor.getString(cursor.getColumnIndex("CNT")));
		
		if(toc != 0) return true;
		else return false;
	}
	
	public String getDept()
	{
		if(AttendanceInfo.deptField.getText().toString().equalsIgnoreCase("CSE"))
			return "07";
		else if(AttendanceInfo.deptField.getText().toString().equalsIgnoreCase("EEE"))
			return "03";
		else if(AttendanceInfo.deptField.getText().toString().equalsIgnoreCase("CE"))
			return "01";
		else if(AttendanceInfo.deptField.getText().toString().equalsIgnoreCase("ME"))
			return "05";
		else if(AttendanceInfo.deptField.getText().toString().equalsIgnoreCase("LE"))
			return "09";
		else if(AttendanceInfo.deptField.getText().toString().equalsIgnoreCase("IPE"))
			return "11";
		else if(AttendanceInfo.deptField.getText().toString().equalsIgnoreCase("TE"))
			return "11";
		else if(AttendanceInfo.deptField.getText().toString().equalsIgnoreCase("ECE"))
			return "09";
		else if(AttendanceInfo.deptField.getText().toString().equalsIgnoreCase("BECM"))
			return "05";
		else if(AttendanceInfo.deptField.getText().toString().equalsIgnoreCase("BME"))
			return "05";
		else if(AttendanceInfo.deptField.getText().toString().equalsIgnoreCase("URP"))
			return "17";
		else return "00";
	}

	@Override
	public void onClick(View v) 
	{
		switch (v.getId()) 
		{
		case R.id.saveInPhone:
			Log.d("Button", "save in phone");
			saveAttendanceToPhone();
			break;
			
		/*	
		case R.id.show:
			
			break;*/

		case R.id.saveInServer:
			if (isNetworkAvailable()) 
			{
				pd = ProgressDialog.show(this, "",
						"Checking internet connection...", false, true);
				saveAttendanceToServer();
			}
			else Toast.makeText(getApplicationContext(), "No network found.", Toast.LENGTH_SHORT).show();
			break;

		default:
			break;
		}
	}

	private boolean isNetworkAvailable()
	{
		ConnectivityManager cManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cManager.getActiveNetworkInfo();
		if (netInfo != null)
		{
			if (netInfo.isAvailable() && netInfo.isConnected()) 
			{
				return true;
			}
		}

		return false;
	}

	public String presentRoll = "";
	int counter = 1;
	String groupAB;

	public void saveAttendanceToPhone()
	{
		
		Calendar cal = Calendar.getInstance();
		int month = cal.get(Calendar.MONTH);
		int day = cal.get(Calendar.DAY_OF_MONTH);
		int year = cal.get(Calendar.YEAR);
		
		String dt = day + "-" + (month+1) + "-" + year;
		Toast.makeText(getApplicationContext(),
				"Checking to save attendance...",
				Toast.LENGTH_SHORT).show();
		
		Cursor cursor = null;
		groupAB = AttendanceInfo.groupField.getText().toString();
		if(groupAB.equalsIgnoreCase("A")) groupAB = "A";
		else if(groupAB.equalsIgnoreCase("B")) groupAB = "B";

		counter = 0;
		for (int i = 0; i < 60; i++) 
		{
			int rll = 0;
			String Roll = getFormattedRoll(i);
			
			if (rollCheckBox[i].isChecked()) 
			{				
				Log.d("Present Students:", Roll);
				
				cursor = db.rawQuery("SELECT COUNT(TEACHERID) AS CNT FROM ATTENDANCE WHERE " +
						"TEACHERID='" + TeacherLgin.TID.getText().toString() +
						"' AND STUDENTID='" + Roll +
						"' AND GROUPD='" + groupAB + "'" +
						" AND COURSEID='" + AttendanceInfo.courseNoField.getText().toString().toUpperCase() + "'" +
						" AND DEPTID='" + AttendanceInfo.deptField.getText().toString().toUpperCase() + "'" +
						" AND DATE='" + AttendanceInfo.dateField.getText().toString() + "'",
						null);
				cursor.moveToFirst();
				String name;
				name = cursor.getString(cursor.getColumnIndex("CNT"));
				int toc;
				toc = Integer.parseInt(name);
				if(toc != 0) 
				{
					continue;
				}
				
				db.execSQL("INSERT INTO ATTENDANCE VALUES('"+ 
						TeacherLgin.TID.getText().toString() + "', '"	+ 
						Roll +"', '" + groupAB + "', '" + 
						AttendanceInfo.courseNoField.getText().toString().toUpperCase() + "', '"+ 
						AttendanceInfo.deptField.getText().toString().toUpperCase() +"', '"+ 
						AttendanceInfo.dateField.getText().toString() +"');");
				
				presentRoll += Roll + ", ";
				counter++;
			}
			else
			{
				try
				{
					db.execSQL("DELETE FROM ATTENDANCE WHERE" +
							" TEACHERID='" + TeacherLgin.TID.getText().toString() +
							"' AND STUDENTID='" + Integer.parseInt(Roll) +
							"' AND GROUPD='" + groupAB + "'" +
							" AND COURSEID='" + AttendanceInfo.courseNoField.getText().toString().toUpperCase() + "'" +
							" AND DEPTID='" + AttendanceInfo.deptField.getText().toString().toUpperCase() + "'" +
							" AND DATE='" + AttendanceInfo.dateField.getText().toString() + "';");
				}catch(Exception e)
				{
					Toast.makeText(getApplicationContext(),
							//"Sorry sir! You have already taken attendance.",
							"" + Roll + " is not deleted.",
							Toast.LENGTH_SHORT).show();
				}
			}
		}
		
		/* Start of deleting extra student who ran out today*/
		cursor = db.rawQuery("SELECT COUNT(STUDENTID) AS CNT FROM ATTENDANCE WHERE " +
				"TEACHERID='" + TeacherLgin.TID.getText().toString() +
				"' AND GROUPD='" + groupAB + "'" +
				" AND COURSEID='" + AttendanceInfo.courseNoField.getText().toString().toUpperCase() + "'" +
				" AND DEPTID='" + AttendanceInfo.deptField.getText().toString().toUpperCase() + "'" +
				" AND DATE='" + AttendanceInfo.dateField.getText().toString() + "'" +
				" AND STUDENTID NOT BETWEEN " + StartRoll + " AND " + (StartRoll+60),
				null);
		cursor.moveToFirst();
		int toc = Integer.parseInt(cursor.getString(cursor.getColumnIndex("CNT")));
		
		/*Toast.makeText(getApplicationContext(),
				"Extra student: " + toc,
				Toast.LENGTH_SHORT).show();*/
		
		if(toc != 0)
		{
			cursor = db.rawQuery("SELECT STUDENTID FROM ATTENDANCE WHERE " +
					"TEACHERID='" + TeacherLgin.TID.getText().toString() +
					"' AND GROUPD='" + groupAB + "'" +
					" AND COURSEID='" + AttendanceInfo.courseNoField.getText().toString().toUpperCase() + "'" +
					" AND DEPTID='" + AttendanceInfo.deptField.getText().toString().toUpperCase() + "'" +
					" AND DATE='" + AttendanceInfo.dateField.getText().toString() + "'" +
					" AND STUDENTID NOT BETWEEN " + StartRoll + " AND " + (StartRoll+60),
					null);
			cursor.moveToFirst();
			//int Roll = Integer.parseInt(cursor.getString(cursor.getColumnIndex("STUDENTID")));
			for(int j=0; j<toc; j++)
			{
				String tempRoll = cursor.getString(cursor.getColumnIndex("STUDENTID"));
				tempRoll = tempRoll.length()<7? ("0"+tempRoll) : tempRoll; 
				
				db.execSQL("DELETE FROM ATTENDANCE WHERE" +
						" TEACHERID='" + TeacherLgin.TID.getText().toString() +
						"' AND STUDENTID='" + tempRoll +
						"' AND GROUPD='" + groupAB + "'" +
						" AND COURSEID='" + AttendanceInfo.courseNoField.getText().toString().toUpperCase() + "'" +
						" AND DEPTID='" + AttendanceInfo.deptField.getText().toString().toUpperCase() + "'" +
						" AND DATE='" + AttendanceInfo.dateField.getText().toString() + "';");
				cursor.moveToNext();
			}
		}
		
		/* end of deleting extra student who ran out today*/
		
		for(int i=0; i<5; i++)
		{			
			if (rollCheckBox[60+i].isChecked() && !extraRoll[i].getText().toString().equals("")) 
			{				
				Log.d("Present Students:", extraRoll[i].getText().toString());
				
				String tempRoll = extraRoll[i].getText().toString();
				tempRoll = tempRoll.length()<7? ("0"+tempRoll) : tempRoll; 
				
				cursor = db.rawQuery("SELECT COUNT(TEACHERID) AS CNT FROM ATTENDANCE WHERE " +
						"TEACHERID='" + TeacherLgin.TID.getText().toString() +
						"' AND STUDENTID='" + tempRoll +
						"' AND GROUPD='" + groupAB + "'" +
						" AND COURSEID='" + AttendanceInfo.courseNoField.getText().toString().toUpperCase() + "'" +
						" AND DEPTID='" + AttendanceInfo.deptField.getText().toString().toUpperCase() + "'" +
						" AND DATE='" + AttendanceInfo.dateField.getText().toString() + "'",
						null);
				cursor.moveToFirst();
				String name;
				name = cursor.getString(cursor.getColumnIndex("CNT"));
				toc = Integer.parseInt(name);
				if(toc != 0) 
				{
					continue;
				}
				
				tempRoll = extraRoll[i].getText().toString();
				tempRoll = tempRoll.length()<7? ("0"+tempRoll) : tempRoll; 
				
				/*Toast.makeText(getApplicationContext(),
						tempRoll,
						Toast.LENGTH_SHORT).show();*/
				
				db.execSQL("INSERT INTO ATTENDANCE VALUES("+ 
						TeacherLgin.TID.getText().toString() + ", "	+ 
						tempRoll +", '" + groupAB + "', '" + 
						AttendanceInfo.courseNoField.getText().toString().toUpperCase() + "', '"+ 
						AttendanceInfo.deptField.getText().toString().toUpperCase() +"', '"+ 
						AttendanceInfo.dateField.getText().toString() +"');");
			}
		}
		
		Toast.makeText(getApplicationContext(),
				"Attendance saved successfully.",
				Toast.LENGTH_SHORT).show();
	}

	public void saveAttendanceToServer() 
	{
		counter = 0;
		for (int i = 0; i < 60; i++) 
		{
			if (rollCheckBox[i].isChecked()) 
			{
				String s = i < 9 ? (AttendanceInfo.batchField.getText() + getDept() + "00" + (i + 1)) : (AttendanceInfo.batchField.getText() + getDept() + "0" + (i + 1));

				presentRoll += s + ", ";
				counter++;
			}
		}

		updatedRoll = counter;
		Log.d("Present Students:", counter + "");
		Log.d("Present Students:", presentRoll);
		PostThread pt = new PostThread();
		pt.start();
	}

	int updatedRoll = 0;

	class PostThread extends Thread
	{
		public void run() 
		{
			// post data to server
			DefaultHttpClient client = new DefaultHttpClient();
			String url = POST_URL;// +"?category=programming";
			HttpPost postReq = new HttpPost(url);

			counter = 0;
			int success = 0;
			for (int i = 0; i < 60; i++)
			{
				int rll = 0;
				String Roll = getFormattedRoll(i);
				/*if(groupAB.equalsIgnoreCase("A")){
					rll = i;
				}
				else if(groupAB.equalsIgnoreCase("B")){
					rll = 60 + i;
				}
				
				if(rll<9) 
				{
					Roll = AttendanceInfo.batchField.getText().toString() + getDept() +"00"+ (rll+1);
				}
				else if(rll<99)
				{
					Roll = AttendanceInfo.batchField.getText().toString() + getDept() +"0"+ (rll+1);
				}
				else 
				{
					Roll = AttendanceInfo.batchField.getText().toString() + getDept() +""+ (rll+1);
				}*/
				if (rollCheckBox[i].isChecked()) 
				{
					String s = i < 9 ? (AttendanceInfo.batchField.getText() + getDept() + "00" + (i + 1))
							: (AttendanceInfo.batchField.getText() + getDept() + "0" + (i + 1));

					presentRoll += s + ", ";
					counter++;

					List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
					params.add(new BasicNameValuePair("t_id", TeacherLgin.TID.getText() + ""));
					params.add(new BasicNameValuePair("s_id", Roll + ""));
					params.add(new BasicNameValuePair("grp", AttendanceInfo.groupField.getText().toString().toUpperCase() + ""));
					params.add(new BasicNameValuePair("c_id", AttendanceInfo.courseNoField.getText().toString().toUpperCase() + ""));
					params.add(new BasicNameValuePair("d_id", AttendanceInfo.deptField.getText().toString() + ""));
					params.add(new BasicNameValuePair("date", AttendanceInfo.dateField.getText().toString() + ""));
					params.add(new BasicNameValuePair("password", TeacherLgin.TPWD.getText().toString() + ""));

					try 
					{
						postReq.setEntity(new UrlEncodedFormEntity(params));
						HttpResponse resp = client.execute(postReq);
						if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
						{
							HttpEntity entity = resp.getEntity();
							String jsonStr = EntityUtils.toString(entity);
							Log.e("json response", jsonStr);
							JSONObject jsonObj = new JSONObject(jsonStr);
							success = jsonObj.getInt("success");
							if (success == 1)
							{
								// successfully inserted
								postHandler.sendEmptyMessage(SUCCESS);
							} 
							else 
							{
								// insertion failed
								postHandler.sendEmptyMessage(FAILURE);
							}
						}
					} catch (Exception e) 
					{
						e.printStackTrace();
						postHandler.sendEmptyMessage(FAILURE);
					}
				}
			}
			
			/* Save attendance for extra student */
			for(int i=0; i<5; i++)
			{			
				if (rollCheckBox[60+i].isChecked() && !extraRoll[i].getText().toString().equals("")) 
				{				
					List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
					params.add(new BasicNameValuePair("t_id", TeacherLgin.TID.getText() + ""));
					params.add(new BasicNameValuePair("s_id", extraRoll[i].getText().toString() + ""));
					params.add(new BasicNameValuePair("grp", AttendanceInfo.groupField.getText().toString().toUpperCase() + ""));
					params.add(new BasicNameValuePair("c_id", AttendanceInfo.courseNoField.getText().toString().toUpperCase() + ""));
					params.add(new BasicNameValuePair("d_id", AttendanceInfo.deptField.getText().toString() + ""));
					params.add(new BasicNameValuePair("date", AttendanceInfo.dateField.getText().toString() + ""));
					params.add(new BasicNameValuePair("password", TeacherLgin.TPWD.getText().toString() + ""));					
					
					try 
					{
						postReq.setEntity(new UrlEncodedFormEntity(params));
						HttpResponse resp = client.execute(postReq);
						if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
						{
							HttpEntity entity = resp.getEntity();
							String jsonStr = EntityUtils.toString(entity);
							Log.e("json response", jsonStr);
							JSONObject jsonObj = new JSONObject(jsonStr);
							success = jsonObj.getInt("success");
							if (success == 1)
							{
								// successfully inserted
								postHandler.sendEmptyMessage(SUCCESS);
							} 
							else 
							{
								// insertion failed
								postHandler.sendEmptyMessage(FAILURE);
							}
						}
					} catch (Exception e) 
					{
						e.printStackTrace();
						postHandler.sendEmptyMessage(FAILURE);
					}
				}
			}
		}
	}

	@SuppressLint("HandlerLeak")
	Handler postHandler = new Handler()
	{
		public void handleMessage(android.os.Message msg)
		{
			pd.dismiss();
			if (msg.what == SUCCESS) 
			{
				if (counter != updatedRoll){
					/*Toast.makeText(getApplicationContext(),
							counter + " of " + updatedRoll + " saved.",
							Toast.LENGTH_LONG).show();*/
				}
				else 
				{
					Toast.makeText(getApplicationContext(),
							"Attendance saved successfully.",
							Toast.LENGTH_SHORT).show();
					
					//btnView2.setEnabled(false);
				}
			} 
			else 
			{
				Toast.makeText(getApplicationContext(),
						"Error occured. Not saved in server.",
						Toast.LENGTH_LONG).show();
			}
		}
	};


	/*<start> checking if already attendance is taken or not*/
	public boolean isAlreadyTakenAttendance()
	{
		Cursor cursor = null;
		groupAB = AttendanceInfo.groupField.getText().toString();
		if(groupAB.equalsIgnoreCase("A")) groupAB = "A";
		else if(groupAB.equalsIgnoreCase("B")) groupAB = "B";
		
		/*
		 * here checking that if the attendance is already taken or not.
		 * By counting the teacher id from attendance table we can check
		 * that the value is zero or not. The value zero shows that the 
		 * attendance is not taken.
		 */
		
		cursor = db.rawQuery("SELECT COUNT(TEACHERID) AS CNT FROM ATTENDANCE WHERE " +
				"TEACHERID=" + TeacherLgin.TID.getText().toString() +
				" AND GROUPD='" + groupAB + "'" +
				" AND COURSEID='" + AttendanceInfo.courseNoField.getText().toString().toUpperCase() + "'" +
				" AND DEPTID='" + AttendanceInfo.deptField.getText().toString().toUpperCase() + "'" +
				" AND DATE='" + AttendanceInfo.dateField.getText().toString() + "'",
				null);
		cursor.moveToFirst();
		String name;
		name = cursor.getString(cursor.getColumnIndex("CNT"));
		int toc = Integer.parseInt(name);
		if(toc != 0) return true;
		else return false;
	}
	/*<end> checking if already attendance is taken or not*/
	
	public void initializeRollCheckBoxOfExtraStudent()
	{
		Cursor cursor = db.rawQuery("SELECT COUNT(DISTINCT STUDENTID) AS CNT FROM ATTENDANCE WHERE " +
				"TEACHERID='" + TeacherLgin.TID.getText().toString() +
				"' AND GROUPD='" + groupAB + "'" +
				" AND COURSEID='" + AttendanceInfo.courseNoField.getText().toString().toUpperCase() + "'" +
				" AND DEPTID='" + AttendanceInfo.deptField.getText().toString().toUpperCase() + "'" +
				//" AND DATE='" + AttendanceInfo.dateField.getText().toString() + "'" +
				" AND STUDENTID NOT BETWEEN " + StartRoll + " AND " + (StartRoll+60),
				null);
		cursor.moveToFirst();
		int toc = Integer.parseInt(cursor.getString(cursor.getColumnIndex("CNT")));
		
		Toast.makeText(getApplicationContext(),
				"Irregular student: " + toc,
				Toast.LENGTH_SHORT).show();
		if(toc != 0)
		{
			cursor = db.rawQuery("SELECT DISTINCT(STUDENTID) FROM ATTENDANCE WHERE " +
					"TEACHERID='" + TeacherLgin.TID.getText().toString() +
					"' AND GROUPD='" + groupAB + "'" +
					" AND COURSEID='" + AttendanceInfo.courseNoField.getText().toString().toUpperCase() + "'" +
					" AND DEPTID='" + AttendanceInfo.deptField.getText().toString().toUpperCase() + "'" +
					//" AND DATE='" + AttendanceInfo.dateField.getText().toString() + "'" +
					" AND STUDENTID NOT BETWEEN " + StartRoll + " AND " + (StartRoll+60),
					null);
			cursor.moveToFirst();
			//int Roll = Integer.parseInt(cursor.getString(cursor.getColumnIndex("STUDENTID")));
			Boolean ATTENDANCEALREADYTAKEN = isAlreadyTakenAttendance();
			int j;
			for(j=0; j<toc; j++)
			{
				String tempRoll = cursor.getString(cursor.getColumnIndex("STUDENTID"));
				tempRoll = tempRoll.length()<7? ("0"+tempRoll) : tempRoll; 
				cursor.moveToNext();
				
				Cursor tempCursor = null;
				if(ATTENDANCEALREADYTAKEN)
				{
					tempCursor = db.rawQuery("SELECT COUNT(STUDENTID) AS CNT FROM ATTENDANCE WHERE " +
							"TEACHERID=" + TeacherLgin.TID.getText().toString() +
							" AND GROUPD='" + groupAB + "'" +
							" AND COURSEID='" + AttendanceInfo.courseNoField.getText().toString().toUpperCase() + "'" +
							" AND DEPTID='" + AttendanceInfo.deptField.getText().toString().toUpperCase() + "'" +
							" AND DATE='" + AttendanceInfo.dateField.getText().toString() + "'" +
							" AND STUDENTID=" + tempRoll,
							null);
				}
				else
				{
					tempCursor = db.rawQuery("SELECT COUNT(STUDENTID) AS CNT FROM ATTENDANCE WHERE " +
							"TEACHERID=" + TeacherLgin.TID.getText().toString() +
							" AND GROUPD='" + groupAB + "'" +
							" AND COURSEID='" + AttendanceInfo.courseNoField.getText().toString().toUpperCase() + "'" +
							" AND DEPTID='" + AttendanceInfo.deptField.getText().toString().toUpperCase() + "'" +
							//" AND DATE='" + AttendanceInfo.dateField.getText().toString() + "'" +
							" AND STUDENTID=" + tempRoll,
							null);
				}
				
				tempCursor.moveToFirst();
				
				int tempPresent = Integer.parseInt(tempCursor.getString(tempCursor.getColumnIndex("CNT")));
				if(tempPresent != 0) { 
					rollCheckBox[60+j].setChecked(true);
				}
				else rollCheckBox[60+j].setChecked(false);
				
				extraRoll[j].setText(tempRoll);					
			}	
		}
	}
	
	public int parseRollId(int roll) 
	{
		if (roll == 1)
			return R.id.roll01;
		else if (roll == 2)
			return R.id.roll02;
		else if (roll == 3)
			return R.id.roll03;
		else if (roll == 4)
			return R.id.roll04;
		else if (roll == 5)
			return R.id.roll05;
		else if (roll == 6)
			return R.id.roll06;
		else if (roll == 7)
			return R.id.roll07;
		else if (roll == 8)
			return R.id.roll08;
		else if (roll == 9)
			return R.id.roll09;
		else if (roll == 10)
			return R.id.roll10;
		else if (roll == 11)
			return R.id.roll11;
		else if (roll == 12)
			return R.id.roll12;
		else if (roll == 13)
			return R.id.roll13;
		else if (roll == 14)
			return R.id.roll14;
		else if (roll == 15)
			return R.id.roll15;
		else if (roll == 16)
			return R.id.roll16;
		else if (roll == 17)
			return R.id.roll17;
		else if (roll == 18)
			return R.id.roll18;
		else if (roll == 19)
			return R.id.roll19;
		else if (roll == 20)
			return R.id.roll20;
		else if (roll == 21)
			return R.id.roll21;
		else if (roll == 22)
			return R.id.roll22;
		else if (roll == 23)
			return R.id.roll23;
		else if (roll == 24)
			return R.id.roll24;
		else if (roll == 25)
			return R.id.roll25;
		else if (roll == 26)
			return R.id.roll26;
		else if (roll == 27)
			return R.id.roll27;
		else if (roll == 28)
			return R.id.roll28;
		else if (roll == 29)
			return R.id.roll29;
		else if (roll == 30)
			return R.id.roll30;
		else if (roll == 31)
			return R.id.roll31;
		else if (roll == 32)
			return R.id.roll32;
		else if (roll == 33)
			return R.id.roll33;
		else if (roll == 34)
			return R.id.roll34;
		else if (roll == 35)
			return R.id.roll35;
		else if (roll == 36)
			return R.id.roll36;
		else if (roll == 37)
			return R.id.roll37;
		else if (roll == 38)
			return R.id.roll38;
		else if (roll == 39)
			return R.id.roll39;
		else if (roll == 40)
			return R.id.roll40;
		else if (roll == 41)
			return R.id.roll41;
		else if (roll == 42)
			return R.id.roll42;
		else if (roll == 43)
			return R.id.roll43;
		else if (roll == 44)
			return R.id.roll44;
		else if (roll == 45)
			return R.id.roll45;
		else if (roll == 46)
			return R.id.roll46;
		else if (roll == 47)
			return R.id.roll47;
		else if (roll == 48)
			return R.id.roll48;
		else if (roll == 49)
			return R.id.roll49;
		else if (roll == 50)
			return R.id.roll50;
		else if (roll == 51)
			return R.id.roll51;
		else if (roll == 52)
			return R.id.roll52;
		else if (roll == 53)
			return R.id.roll53;
		else if (roll == 54)
			return R.id.roll54;
		else if (roll == 55)
			return R.id.roll55;
		else if (roll == 56)
			return R.id.roll56;
		else if (roll == 57)
			return R.id.roll57;
		else if (roll == 58)
			return R.id.roll58;
		else if (roll == 59)
			return R.id.roll59;
		else if (roll == 60)
			return R.id.roll60;
		else if (roll == 61)
			return R.id.roll61;
		else if (roll == 62)
			return R.id.roll62;
		else if (roll == 63)
			return R.id.roll63;
		else if (roll == 64)
			return R.id.roll64;
		else if (roll == 65)
			return R.id.roll65;
		else
			return R.id.roll01;
	}

	public void initiaLizeView()
	{
		rollView[0] = (TextView) findViewById(R.id.rolltxt01);
		rollCheckBox[0] = (CheckBox) findViewById(R.id.roll01);
		rollView[1] = (TextView) findViewById(R.id.rolltxt02);
		rollCheckBox[1] = (CheckBox) findViewById(R.id.roll02);
		rollView[2] = (TextView) findViewById(R.id.rolltxt03);
		rollCheckBox[2] = (CheckBox) findViewById(R.id.roll03);
		rollView[3] = (TextView) findViewById(R.id.rolltxt04);
		rollCheckBox[3] = (CheckBox) findViewById(R.id.roll04);
		rollView[4] = (TextView) findViewById(R.id.rolltxt05);
		rollCheckBox[4] = (CheckBox) findViewById(R.id.roll05);
		rollView[5] = (TextView) findViewById(R.id.rolltxt06);
		rollCheckBox[5] = (CheckBox) findViewById(R.id.roll06);
		rollView[6] = (TextView) findViewById(R.id.rolltxt07);
		rollCheckBox[6] = (CheckBox) findViewById(R.id.roll07);
		rollView[7] = (TextView) findViewById(R.id.rolltxt08);
		rollCheckBox[7] = (CheckBox) findViewById(R.id.roll08);
		rollView[8] = (TextView) findViewById(R.id.rolltxt09);
		rollCheckBox[8] = (CheckBox) findViewById(R.id.roll09);
		rollView[9] = (TextView) findViewById(R.id.rolltxt10);
		rollCheckBox[9] = (CheckBox) findViewById(R.id.roll10);
		rollView[10] = (TextView) findViewById(R.id.rolltxt11);
		rollCheckBox[10] = (CheckBox) findViewById(R.id.roll11);
		rollView[11] = (TextView) findViewById(R.id.rolltxt12);
		rollCheckBox[11] = (CheckBox) findViewById(R.id.roll12);
		rollView[12] = (TextView) findViewById(R.id.rolltxt13);
		rollCheckBox[12] = (CheckBox) findViewById(R.id.roll13);
		rollView[13] = (TextView) findViewById(R.id.rolltxt14);
		rollCheckBox[13] = (CheckBox) findViewById(R.id.roll14);
		rollView[14] = (TextView) findViewById(R.id.rolltxt15);
		rollCheckBox[14] = (CheckBox) findViewById(R.id.roll15);
		rollView[15] = (TextView) findViewById(R.id.rolltxt16);
		rollCheckBox[15] = (CheckBox) findViewById(R.id.roll16);
		rollView[16] = (TextView) findViewById(R.id.rolltxt17);
		rollCheckBox[16] = (CheckBox) findViewById(R.id.roll17);
		rollView[17] = (TextView) findViewById(R.id.rolltxt18);
		rollCheckBox[17] = (CheckBox) findViewById(R.id.roll18);
		rollView[18] = (TextView) findViewById(R.id.rolltxt19);
		rollCheckBox[18] = (CheckBox) findViewById(R.id.roll19);
		rollView[19] = (TextView) findViewById(R.id.rolltxt20);
		rollCheckBox[19] = (CheckBox) findViewById(R.id.roll20);
		rollView[20] = (TextView) findViewById(R.id.rolltxt21);
		rollCheckBox[20] = (CheckBox) findViewById(R.id.roll21);
		rollView[21] = (TextView) findViewById(R.id.rolltxt22);
		rollCheckBox[21] = (CheckBox) findViewById(R.id.roll22);
		rollView[22] = (TextView) findViewById(R.id.rolltxt23);
		rollCheckBox[22] = (CheckBox) findViewById(R.id.roll23);
		rollView[23] = (TextView) findViewById(R.id.rolltxt24);
		rollCheckBox[23] = (CheckBox) findViewById(R.id.roll24);
		rollView[24] = (TextView) findViewById(R.id.rolltxt25);
		rollCheckBox[24] = (CheckBox) findViewById(R.id.roll25);
		rollView[25] = (TextView) findViewById(R.id.rolltxt26);
		rollCheckBox[25] = (CheckBox) findViewById(R.id.roll26);
		rollView[26] = (TextView) findViewById(R.id.rolltxt27);
		rollCheckBox[26] = (CheckBox) findViewById(R.id.roll27);
		rollView[27] = (TextView) findViewById(R.id.rolltxt28);
		rollCheckBox[27] = (CheckBox) findViewById(R.id.roll28);
		rollView[28] = (TextView) findViewById(R.id.rolltxt29);
		rollCheckBox[28] = (CheckBox) findViewById(R.id.roll29);
		rollView[29] = (TextView) findViewById(R.id.rolltxt30);
		rollCheckBox[29] = (CheckBox) findViewById(R.id.roll30);
		rollView[30] = (TextView) findViewById(R.id.rolltxt31);
		rollCheckBox[30] = (CheckBox) findViewById(R.id.roll31);
		rollView[31] = (TextView) findViewById(R.id.rolltxt32);
		rollCheckBox[31] = (CheckBox) findViewById(R.id.roll32);
		rollView[32] = (TextView) findViewById(R.id.rolltxt33);
		rollCheckBox[32] = (CheckBox) findViewById(R.id.roll33);
		rollView[33] = (TextView) findViewById(R.id.rolltxt34);
		rollCheckBox[33] = (CheckBox) findViewById(R.id.roll34);
		rollView[34] = (TextView) findViewById(R.id.rolltxt35);
		rollCheckBox[34] = (CheckBox) findViewById(R.id.roll35);
		rollView[35] = (TextView) findViewById(R.id.rolltxt36);
		rollCheckBox[35] = (CheckBox) findViewById(R.id.roll36);
		rollView[36] = (TextView) findViewById(R.id.rolltxt37);
		rollCheckBox[36] = (CheckBox) findViewById(R.id.roll37);
		rollView[37] = (TextView) findViewById(R.id.rolltxt38);
		rollCheckBox[37] = (CheckBox) findViewById(R.id.roll38);
		rollView[38] = (TextView) findViewById(R.id.rolltxt39);
		rollCheckBox[38] = (CheckBox) findViewById(R.id.roll39);
		rollView[39] = (TextView) findViewById(R.id.rolltxt40);
		rollCheckBox[39] = (CheckBox) findViewById(R.id.roll40);
		rollView[40] = (TextView) findViewById(R.id.rolltxt41);
		rollCheckBox[40] = (CheckBox) findViewById(R.id.roll41);
		rollView[41] = (TextView) findViewById(R.id.rolltxt42);
		rollCheckBox[41] = (CheckBox) findViewById(R.id.roll42);
		rollView[42] = (TextView) findViewById(R.id.rolltxt43);
		rollCheckBox[42] = (CheckBox) findViewById(R.id.roll43);
		rollView[43] = (TextView) findViewById(R.id.rolltxt44);
		rollCheckBox[43] = (CheckBox) findViewById(R.id.roll44);
		rollView[44] = (TextView) findViewById(R.id.rolltxt45);
		rollCheckBox[44] = (CheckBox) findViewById(R.id.roll45);
		rollView[45] = (TextView) findViewById(R.id.rolltxt46);
		rollCheckBox[45] = (CheckBox) findViewById(R.id.roll46);
		rollView[46] = (TextView) findViewById(R.id.rolltxt47);
		rollCheckBox[46] = (CheckBox) findViewById(R.id.roll47);
		rollView[47] = (TextView) findViewById(R.id.rolltxt48);
		rollCheckBox[47] = (CheckBox) findViewById(R.id.roll48);
		rollView[48] = (TextView) findViewById(R.id.rolltxt49);
		rollCheckBox[48] = (CheckBox) findViewById(R.id.roll49);
		rollView[49] = (TextView) findViewById(R.id.rolltxt50);
		rollCheckBox[49] = (CheckBox) findViewById(R.id.roll50);
		rollView[50] = (TextView) findViewById(R.id.rolltxt51);
		rollCheckBox[50] = (CheckBox) findViewById(R.id.roll51);
		rollView[51] = (TextView) findViewById(R.id.rolltxt52);
		rollCheckBox[51] = (CheckBox) findViewById(R.id.roll52);
		rollView[52] = (TextView) findViewById(R.id.rolltxt53);
		rollCheckBox[52] = (CheckBox) findViewById(R.id.roll53);
		rollView[53] = (TextView) findViewById(R.id.rolltxt54);
		rollCheckBox[53] = (CheckBox) findViewById(R.id.roll54);
		rollView[54] = (TextView) findViewById(R.id.rolltxt55);
		rollCheckBox[54] = (CheckBox) findViewById(R.id.roll55);
		rollView[55] = (TextView) findViewById(R.id.rolltxt56);
		rollCheckBox[55] = (CheckBox) findViewById(R.id.roll56);
		rollView[56] = (TextView) findViewById(R.id.rolltxt57);
		rollCheckBox[56] = (CheckBox) findViewById(R.id.roll57);
		rollView[57] = (TextView) findViewById(R.id.rolltxt58);
		rollCheckBox[57] = (CheckBox) findViewById(R.id.roll58);
		rollView[58] = (TextView) findViewById(R.id.rolltxt59);
		rollCheckBox[58] = (CheckBox) findViewById(R.id.roll59);
		rollView[59] = (TextView) findViewById(R.id.rolltxt60);
		rollCheckBox[59] = (CheckBox) findViewById(R.id.roll60);
		
		extraRoll[0] = (EditText) findViewById(R.id.rolltxt61);
		rollCheckBox[60] = (CheckBox) findViewById(R.id.roll61);
		extraRoll[1] = (EditText) findViewById(R.id.rolltxt62);
		rollCheckBox[61] = (CheckBox) findViewById(R.id.roll62);
		extraRoll[2] = (EditText) findViewById(R.id.rolltxt63);
		rollCheckBox[62] = (CheckBox) findViewById(R.id.roll63);
		extraRoll[3] = (EditText) findViewById(R.id.rolltxt64);
		rollCheckBox[63] = (CheckBox) findViewById(R.id.roll64);
		extraRoll[4] = (EditText) findViewById(R.id.rolltxt65);
		rollCheckBox[64] = (CheckBox) findViewById(R.id.roll65);
	}
	
	public void initializeAttendance()
	{
		setTitle("Attendance of "+ 
				AttendanceInfo.deptField.getText().toString().toUpperCase() +
				", 2K"+  AttendanceInfo.batchField.getText().toString().toUpperCase() +"!");
		
		/*
		 * Initialising database
		 */
		db = openOrCreateDatabase("MYATTENDANCE", MODE_PRIVATE, null);
		//db.execSQL("DROP TABLE ATTENDANCE;");
		db.execSQL("CREATE TABLE IF NOT EXISTS ATTENDANCE" +
				" (TEACHERID INT(7), STUDENTID INT(7), GROUPD VARCHAR(1), " + 
				" COURSEID VARCHAR(7), DEPTID VARCHAR(4), DATE VARCHAR(20));");
		
		/*
		 * Displaying a message with course information which the teacher taking attendance
		 */
		TextView messageView = (TextView) findViewById(R.id.message);
		messageView.setText(
				"------------------Info------------------\t"+
				"\nCourse No.:\t" + AttendanceInfo.courseNoField.getText().toString().toUpperCase() +
				"\nDept.:\t" +  AttendanceInfo.deptField.getText().toString().toUpperCase() +
				"\nBatch: 2K" + AttendanceInfo.batchField.getText().toString().toUpperCase() +
				"\nSection: " + AttendanceInfo.groupField.getText().toString().toUpperCase() +
				"\nDate: " + AttendanceInfo.dateField.getText().toString().toUpperCase() +
				"\n------------------------------------------"
				);

		/*
		 * Here checking for 60 students
		 */
		for (int i = 0; i < 60; i++) {
			int roll = 0;
			if(AttendanceInfo.groupField.getText().toString().equalsIgnoreCase("A")) roll = i;
			else roll = 60 + i;
			String Roll;
			if(roll < 9) Roll = AttendanceInfo.batchField.getText() + getDept() + "00" + (roll+1);
			else if(roll < 99) Roll = AttendanceInfo.batchField.getText() + getDept() + "0" + (roll+1);
			else Roll =  AttendanceInfo.batchField.getText() + getDept() + "" + (roll+1);
			
			rollView[i].setText(Roll);
			
			if(i == 0 ) StartRoll = Integer.parseInt(Roll);
			
			if(isAlreadyTakenAttendance()) //if already attendance taken
			{				
				if(wasStudentPresent(i)) { 
					rollCheckBox[i].setChecked(true);
				}
				else rollCheckBox[i].setChecked(false);
			}
			else rollCheckBox[i].setChecked(true); // Attendance not taken yet
		}
		
		initializeRollCheckBoxOfExtraStudent();
		
		saveInPhoneButton = (Button) findViewById(R.id.saveInPhone);
		saveInPhoneButton.setEnabled(true);
		saveInPhoneButton.setOnClickListener(this);

		/*Button showButton = (Button) findViewById(R.id.show);
		showButton.setEnabled(false);
		showButton.setOnClickListener(this);*/
		
		saveInServerButton = (Button) findViewById(R.id.saveInServer);
		saveInServerButton.setOnClickListener(this);
	}
	
	public String getFormattedRoll(int i)
	{
		int rll = i;
		String Roll;
		if(groupAB.equalsIgnoreCase("A")){
			rll = i;
		}
		else if(groupAB.equalsIgnoreCase("B")){
			rll = 60 + i;
		}
		
		if(rll<9) 
		{
			Roll = AttendanceInfo.batchField.getText().toString() + getDept() +"00"+ (rll+1);
		}
		else if(rll<99)
		{
			Roll = AttendanceInfo.batchField.getText().toString() + getDept() +"0"+ (rll+1);
		}
		else 
		{
			Roll = AttendanceInfo.batchField.getText().toString() + getDept() +""+ (rll+1);
		}
		
		return Roll;
	}
	
	CheckBox rollCheckBox[] = new CheckBox[65];
	TextView nameView[] = new TextView[65];
	TextView rollView[] = new TextView[65];
	EditText extraRoll[] = new EditText[5]; 
}
