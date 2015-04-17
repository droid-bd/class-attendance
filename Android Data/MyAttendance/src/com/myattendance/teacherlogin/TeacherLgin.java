package com.myattendance.teacherlogin;

import com.netcse.myattendance.R;
import com.myattendance.AppStarter;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ViewFlipper;

public class TeacherLgin extends Activity {

	SQLiteDatabase db;
	private ViewFlipper viewFlipper;
	public static EditText TID; //log in lid
	public static EditText TPWD; //log in password
	
	Button submitButton;
	EditText teachIDField, passwordFiled, reTypedPasswordFiled;
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
	
		viewFlipper = (ViewFlipper) findViewById(R.id.viewflipper);
		
		if(isAlreadyAccountCreated())
		{
			setTitle("Log in");
		}
		else
		{
			setTitle("Create New Account");
			viewFlipper.showNext();			
		}
		//else viewFlipper.sho
        	//break;
		/*
		 * Initialize database
		 */
		
		
		TID = (EditText) findViewById(R.id.editText1);
		//TID.setText("1107030");
		TPWD = (EditText) findViewById(R.id.editText2);
		//TPWD.setText("1107030");
		
		/*
		 * loginButton handles the log in of the user. If ipnput user name and 
		 * password is correct then appear main menu page to the user. Else stay with
		 * this page.
		 */
		Button LoginButton = (Button) findViewById(R.id.button1);
		LoginButton.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View arg0) 
			{
				actionForLogInButton();
			}
		});
		
		teachIDField = (EditText) findViewById(R.id.teacherIDField);
		passwordFiled = (EditText) findViewById(R.id.passwordField);
		reTypedPasswordFiled = (EditText) findViewById(R.id.rePasswordFiled);
		
		/*
		 * This button handle the operation of creating new account.
		 */
		
		submitButton = (Button) findViewById(R.id.submitButton);
		submitButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				
				actionForCreateAccountButton();
				
			}
		});
	}
	
	public Boolean isAlreadyAccountCreated()
	{
		/*
		 * Here checking if already account created or not. If account exists then simply
		 * display the log in page to the user. Otherwise stay with this page.
		 */
		db = openOrCreateDatabase("MYATTENDANCE", MODE_PRIVATE, null);
		//db.execSQL("DROP TABLE ATTENDANCE;");
		db.execSQL("CREATE TABLE IF NOT EXISTS TEACHERINFO" +
				" (TEACHERID INT(7), PASSWORD VARCHAR(50));");
		Cursor cursor = db.rawQuery("SELECT COUNT(TEACHERID) AS CNT FROM TEACHERINFO",	null);
		cursor.moveToFirst();
		String name;
		name = cursor.getString(cursor.getColumnIndex("CNT"));
		int toc;
		toc = Integer.parseInt(name);
		if(toc == 0) 
		{
			return false;
		}
		return true;
	}

	public void actionForLogInButton()
	{
		try{
		Cursor cursor = db.rawQuery("SELECT * FROM TEACHERINFO",	null);
		cursor.moveToFirst();
		String name, pwd;
		name = cursor.getString(cursor.getColumnIndex("TEACHERID"));
		pwd = cursor.getString(cursor.getColumnIndex("PASSWORD"));
		
		//Log.d("TLG: ", "ID: " + TID.getText().toString() + ";" + " TPWD: " + TPWD.getText().toString());
		if(TID.getText().toString().equals(name) && TPWD.getText().toString().equals(pwd))
		{
			finish();
			
			Toast.makeText(getApplicationContext(),	"Log in successfull as " + TID.getText().toString(), Toast.LENGTH_LONG).show();
			Intent primaryIntent = new Intent(TeacherLgin.this, AppStarter.class);
			startActivity(primaryIntent);					
		}
		else Toast.makeText(getApplicationContext(),	"Incorrect information. Please try again..", Toast.LENGTH_LONG).show();
		}
		catch (Exception e) {
			// TODO: handle exception
			Toast.makeText(getApplicationContext(), "Error in log in. " + e.toString(), Toast.LENGTH_SHORT).show();
		}
	}

	public void actionForCreateAccountButton()
	{
		String teacherID, password, reTypedPassword;
		teacherID = "" + teachIDField.getText();
		password = "" + passwordFiled.getText();
		reTypedPassword = "" + reTypedPasswordFiled.getText();
		
		if(password.length()==0 || reTypedPassword.length()==0 || teacherID.length()==0)
			return;
		
		if(password.equals(reTypedPassword))
		{
			db.execSQL("INSERT INTO TEACHERINFO VALUES("+ teacherID +", '"+ password +"');");
			Toast.makeText(getApplicationContext(), "Account created successful.", Toast.LENGTH_SHORT).show();
			
			viewFlipper.showPrevious();
			//finish();
			//startActivity(new Intent(AuthenticationCheck.this, TeacherLgin.class));
		}
		else Toast.makeText(getApplicationContext(), "Password not matched. Please try again.", Toast.LENGTH_SHORT).show();
	}
}
