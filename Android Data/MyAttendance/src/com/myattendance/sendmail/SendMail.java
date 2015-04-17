package com.myattendance.sendmail;

import com.netcse.myattendance.R;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SendMail extends Activity {

	private EditText recipient;
	private EditText subject;
	private EditText body;
	
   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.send_mail);
      
      setTitle("Send Mail");

      recipient = (EditText) findViewById(R.id.recipient);
      subject = (EditText) findViewById(R.id.subject);
      body = (EditText) findViewById(R.id.body);
      
      recipient.setText("mhnetcse11@gmail.com");
 	 subject.setText("Attendance issue by MyAttendance Administrator");
 	 body.setText("Dear Student, \nYour attendace is below 60% on course CSE3200.\nSo that your will be unable to attend in exam.");
      
      Button sendBtn = (Button) findViewById(R.id.sendEmail);
      sendBtn.setOnClickListener(new View.OnClickListener() {
         public void onClick(View view) {
        	 sendEmail();
        	 // after sending the email, clear the fields
        	 //recipient.setText("");
        	 //subject.setText("");
        	 //body.setText("");
         }
   });

   }
   protected void sendEmail() {

      String[] recipients = {recipient.getText().toString()};
      Intent email = new Intent(Intent.ACTION_SEND, Uri.parse("mailto:"));
      // prompts email clients only
      email.setType("message/rfc822");

      email.putExtra(Intent.EXTRA_EMAIL, recipients);
      email.putExtra(Intent.EXTRA_SUBJECT, subject.getText().toString());
      email.putExtra(Intent.EXTRA_TEXT, body.getText().toString());

      try {
	    // the user can choose the email client
         startActivity(Intent.createChooser(email, "Choose an email client from..."));
     
      } catch (android.content.ActivityNotFoundException ex) {
         Toast.makeText(SendMail.this, "No email client installed.",
        		 Toast.LENGTH_LONG).show();
      }
   }
   
}