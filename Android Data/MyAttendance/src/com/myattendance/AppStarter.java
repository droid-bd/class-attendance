package com.myattendance;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.netcse.myattendance.R;
import com.myattendance.checkattendance.CheckAttendance;
import com.myattendance.checkattendance.DetailsPage;
import com.myattendance.takeattendance.AttendanceInfo;
import com.myattendance.teacherlogin.TeacherLgin;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class AppStarter extends Activity implements OnClickListener
{
	private Button btnView;	
	private ProgressDialog pd;
	
	public static final String POST_URL = "http://netcsemilonkuet.byethost16.com/getAttendance.php"; //for web server
	//public static final String POST_URL = "http://10.0.2.2/MyAttendance/getAttendance.php"; //for local host

	public static final int SUCCESS = 1;
	public static final int FAILURE = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_app_starter);
		setTitle("Welcome to MyAttendance!");
		
		/*
		 * Initialize database
		 */
		db = openOrCreateDatabase("MYATTENDANCE", MODE_PRIVATE, null);
		//db.execSQL("DROP TABLE ATTENDANCE;");
		db.execSQL("CREATE TABLE IF NOT EXISTS ATTENDANCE" +
				" (TEACHERID INT(7), STUDENTID INT(7), GROUPD VARCHAR(1), " + 
				" COURSEID VARCHAR(7), DEPTID VARCHAR(4), DATE VARCHAR(20));");
		
		/*
		 * Here three button
		 * 	1.	Take Attendance Button
		 * 		This button is the gateway to take attendance.
		 * 		Course information, taking attendance windows
		 * 		will appear gradually from this window.
		 * 
		 * 		Saving in Phone and Server option is
		 * 		available here.
		 * 	
		 *	2.	Check or Print Attendance Button
		 * 		Here user will get the feature to checkout
		 * 		already taken attendance. Saving in excel file,
		 * 		generating structured PDF file, sending message,
		 * 		sending e-mail options are available.
		 * 
		 * 	3.	Update Lost Information
		 * 		When the phone is lost or unwantedly the software
		 * 		is uninstalled from phone then user can restore
		 * 		data from online. Here it should be keep in mind
		 * 		that online data available only if the user save the
		 * 		data on online which is a feature of this software.
		 * 		If the data not saved in server then user will be
		 * 		unable to restore data.
		 * 
		 */
		
		Button takeAttendance = (Button) findViewById(R.id.takeAttendanceButton);
		takeAttendance.setOnClickListener(this);
		
		Button checkAttendance = (Button) findViewById(R.id.checkAttendance);
		checkAttendance.setOnClickListener(this);
		
		Button dropTable = (Button) findViewById(R.id.updateLostDataButton);
		dropTable.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) 
	{
		switch (v.getId()) 
		{
		/*
		 * Take attendance button action
		 */
		case R.id.takeAttendanceButton:
			Intent primaryIntent = new Intent(AppStarter.this, AttendanceInfo.class);
			startActivity(primaryIntent);
			break;
		/*
		 * Check attendance button action
		 */
		case R.id.checkAttendance:
			Intent intent = new Intent(AppStarter.this, CheckAttendance.class);
			startActivity(intent);
			break;
			
		case R.id.updateLostDataButton:
			try
			{
				if (isNetworkAvailable()) 
				{
					pd = ProgressDialog.show(this, "",
							"Checking internet connection...", false, true);
					saveAttendanceToServer();
				}
				else Toast.makeText(getApplicationContext(), "No network found.", Toast.LENGTH_SHORT).show();
				
			}catch(Exception e)
			{
				Toast.makeText(getApplicationContext(),
						"No data found to delete.",
						Toast.LENGTH_SHORT).show();
			}
			break;

		default:
			break;
		}
	}
	
	/*
	 * Here checking if the network is available or not
	 */
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
	
	public void saveAttendanceToServer() 
	{
		PostThread pt = new PostThread();
		pt.start();
	}
	
	class PostThread extends Thread
	{
		public void run() 
		{
			try 
			{
				DefaultHttpClient client = new DefaultHttpClient();
				String url = POST_URL;// +"?category=programming";
				//HttpPost postReq = new HttpPost(url);
				HttpPost getReq = new HttpPost(POST_URL);
				
				List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
				params.add(new BasicNameValuePair("t_id", TeacherLgin.TID.getText() + ""));
				params.add(new BasicNameValuePair("password", TeacherLgin.TPWD.getText() + ""));
				
				
				getReq.setEntity(new UrlEncodedFormEntity(params)); //sending teacher_id and password
				
				HttpResponse resp = client.execute(getReq); //receiving information like as roll_id
				
				if(resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
				{
					HttpEntity entity = resp.getEntity();
					String jsonStr = EntityUtils.toString(entity);
					Log.d("json str: ", jsonStr);
					//postHandler.sendEmptyMessage(SUCCESS);
					
					JSONObject jsonObj = new JSONObject(jsonStr);
					int success = jsonObj.getInt("success");
					if (success == 1)
					{
						// successfully inserted
						
						JSONArray attendanceArray = jsonObj.getJSONArray("teacher");
						int size = attendanceArray.length();
						for(int i=0; i<size; i++)
						{
							JSONObject attendanceObj = attendanceArray.getJSONObject(i);
							String t_id = attendanceObj.getString("t_id");
							String s_id = attendanceObj.getString("s_id");
							String g_id = attendanceObj.getString("g_id");
							String c_no = attendanceObj.getString("c_no");
							String d_id = attendanceObj.getString("d_id");
							String date = attendanceObj.getString("date");
							String password = attendanceObj.getString("password");
							
							Cursor cursor = db.rawQuery("SELECT COUNT(TEACHERID) AS CNT FROM ATTENDANCE WHERE " +
									"TEACHERID=" + t_id +
									" AND STUDENTID=" + s_id +
									" AND GROUPD='" + g_id + "'" +
									" AND COURSEID='" + c_no + "'" +
									" AND DEPTID='" + d_id + "'" +
									" AND DATE='" + date + "'",
									null);
							cursor.moveToFirst();
							String name;
							name = cursor.getString(cursor.getColumnIndex("CNT"));
							int toc = Integer.parseInt(name);
							if(toc == 0) 
							{
								db.execSQL("INSERT INTO ATTENDANCE VALUES("+ 
										t_id + ", "	+ 
										s_id +", '" + g_id + "', '" + 
										c_no + "', '"+ 
										d_id +"', '"+ 
										date +"');");
								Log.d("roll updated: ", s_id);
							}
							//else continue;
							
							//Toast.makeText(getApplicationContext(), s_id + d_id, Toast.LENGTH_SHORT).show();
							
						}
						
						postHandler.sendEmptyMessage(SUCCESS);
					} 
					else 
					{
						// insertion failed
						postHandler.sendEmptyMessage(FAILURE);
					}
					//Toast.makeText(getApplicationContext(), "data received " + jsonStr, Toast.LENGTH_SHORT).show();
					//postHandler.sendEmptyMessage(SUCCESS);
				}
				else
				{
					postHandler.sendEmptyMessage(FAILURE);
				}
				
			} catch (Exception e) 
			{
				e.printStackTrace();
				//Toast.makeText(getApplicationContext(), "data not received: " + e.toString(), Toast.LENGTH_SHORT).show();
				postHandler.sendEmptyMessage(FAILURE);
			}
		}
	}

	Handler postHandler = new Handler()
	{
		public void handleMessage(android.os.Message msg)
		{
			pd.dismiss();
			if (msg.what == SUCCESS) 
			{
				Toast.makeText(getApplicationContext(),
						"Attendance updated successfully.",
						Toast.LENGTH_SHORT).show();
				
			} 
			else 
			{
				Toast.makeText(getApplicationContext(),
						"Error occured. Not updated from server.",
						Toast.LENGTH_LONG).show();
			}
		}
	};

	
	SQLiteDatabase db;

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.activity_app_starter, menu);
		return true;
	}
	
	public boolean onOptionsItemSelected(android.view.MenuItem item)
	{
		int id = item.getItemId();
		switch (id) 
		{
		case R.id.about:
			Intent localIntent = new Intent(AppStarter.this, DetailsPage.class);
	          localIntent.putExtra("url", "file:///android_asset/www/aboutus.htm");
	          localIntent.putExtra("title", "About Us!");
	          AppStarter.this.startActivity(localIntent);
			break;
		}
		return super.onOptionsItemSelected(item);
	};
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if ((keyCode == KeyEvent.KEYCODE_BACK)) {
	        Log.d(this.getClass().getName(), "back button pressed");
	        finish();
	    }
	    return super.onKeyDown(keyCode, event);
	}
	
	TextView nameView, rollView, deptView;
}
