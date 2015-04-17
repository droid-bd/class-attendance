package com.myattendance.sendmessage;

import com.netcse.myattendance.R;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class SendMessage extends Activity {

	EditText mobNumber, messageField; 
	Button sendMessageButton;
	Button deleteNumberButton;
	SQLiteDatabase db;
	String VARSITYNAME;
	int TOTALSTUDENTNUMBER;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.send_message);
		
		setTitle("Send Message");
		
		mobNumber = (EditText) findViewById(R.id.mobNumField);
		messageField = (EditText) findViewById(R.id.messageField);
		
		sendMessageButton = (Button) findViewById(R.id.sendMessageButton);
		
		sendMessageButton.setOnClickListener(new View.OnClickListener() {
	         public void onClick(View view) {
	            sendSMSMessage();
	         }
	      });
		
		mobNumber.setText("01763070751");
		messageField.setText("The message from MyAttendace Administrator.");
	}
	
	protected void sendSMSMessage() 
	{	      
	      String phoneNo = mobNumber.getText().toString();
	      
	      String message = messageField.getText().toString();
	      
	      try {
	    	//Cursor cursor = db.rawQuery("SELECT PHONENUMBER FROM NUMINFO WHERE VARSITYNAME='KUET'", null);		
	  		//cursor.moveToFirst();
	  		String numarray[] = new String[50];
	  		String sampleNumber = mobNumber.getText().toString();
	  		
	  		int pos=0;
	  		for(int i=0; i<(sampleNumber.length()/12); i++)
	  		{
	  			numarray[i] = sampleNumber.substring(pos, pos+11);
	  			pos += 12;
	  				  			
	  			 SmsManager smsManager = SmsManager.getDefault();
		         smsManager.sendTextMessage(numarray[i], null, message, null, null);
		         Toast.makeText(getApplicationContext(), "SMS sent successfully to " + numarray[i],
		         Toast.LENGTH_LONG).show();
	  		}
	      } catch (Exception e) {
	         Toast.makeText(getApplicationContext(),
	         "SMS faild, please try again.",
	         Toast.LENGTH_LONG).show();
	         e.printStackTrace();
	      }
	   }
}
