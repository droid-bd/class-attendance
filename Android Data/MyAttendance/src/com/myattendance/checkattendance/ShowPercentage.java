package com.myattendance.checkattendance;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

import com.netcse.myattendance.R;
import com.myattendance.AppStarter;
import com.myattendance.sendmail.SendMail;
import com.myattendance.sendmessage.SendMessage;
import com.myattendance.teacherlogin.TeacherLgin;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import android.R.integer;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

public class ShowPercentage extends Activity {
	String groupAB;
	SQLiteDatabase db;
	int totalClass = 0;
	ProgressDialog progress;
 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.show_percentage);

		setTitle("Attendance of "
				+
				/* Dept. */CheckAttendance.DPT.getText().toString()
						.toUpperCase()
				+ ", "
				+
				/* Course No. */CheckAttendance.CID.getText().toString()
						.toUpperCase());

		/*
		 * parseRollId initialising textView into roll[65] array
		 */

		parseRollId();

		this.progress = new ProgressDialog(this);
		this.progress.setTitle("Please wait!");
		this.progress.setMessage("Printing is getting ready...");

		db = openOrCreateDatabase("MYATTENDANCE", MODE_PRIVATE, null);

		/*
		 * sectionParse method computing section and assigning it into groupAB
		 * variable.
		 */
		sectionParse();

		/*
		 * assignAttendanceInfo method initialising the computation of
		 * attendance of all student
		 */
		assignAttendanceInfo();

		/*
		 * buttonAction method performs the printing operation
		 */
		//buttonAction();

		// new GenerateExcel(this, "newnetcse.xls", groupAB);

	}

	/*
	 * assignAttendanceInfo method initialising the computation of attendance of
	 * all student
	 */
	public void assignAttendanceInfo() {
		Cursor cursor = null;
		try {
			/*
			 * Query for counting total number of class taken.
			 */
			cursor = db.rawQuery(
					"SELECT COUNT(DISTINCT DATE) AS CNT FROM ATTENDANCE WHERE "
							+ "COURSEID='"
							+ CheckAttendance.CID.getText().toString()
									.toUpperCase()
							+ "' AND DEPTID='"
							+ CheckAttendance.DPT.getText().toString()
									.toUpperCase() + "' AND GROUPD='" + groupAB
							+ "'", null);
			cursor.moveToFirst();
			String name = cursor.getString(cursor.getColumnIndex("CNT"));
			int toc = Integer.parseInt(name);
			totalClass = toc; // toc means total number of class
			int ctoc, ptg; /*
							 * ctoc means total number of class of a particular
							 * student, ptg means percentage of particular
							 * student.
							 */

			for (int i = 0; i < 60; i++) {
				int rll = 0;
				String Roll;
				if (groupAB.equalsIgnoreCase("A")) {
					rll = i;
				} else if (groupAB.equalsIgnoreCase("B")) {
					rll = 60 + i;
				}

				if (rll < 9) {
					Roll = CheckAttendance.BATCH.getText().toString()
							+ getDept() + "00" + (rll + 1);
				} else if (rll < 99) {
					Roll = CheckAttendance.BATCH.getText().toString()
							+ getDept() + "0" + (rll + 1);
				} else {
					Roll = CheckAttendance.BATCH.getText().toString()
							+ getDept() + "" + (rll + 1);
				}

				cursor = db.rawQuery(
						"SELECT COUNT(STUDENTID) AS CNT FROM ATTENDANCE WHERE "
								+ "COURSEID='"
								+ CheckAttendance.CID.getText().toString()
										.toUpperCase()
								+ "' AND DEPTID='"
								+ CheckAttendance.DPT.getText().toString()
										.toUpperCase() + "' AND GROUPD='"
								+ groupAB + "' AND STUDENTID=" + Roll, null);
				cursor.moveToFirst();

				name = cursor.getString(cursor.getColumnIndex("CNT"));
				ctoc = Integer.parseInt(name);
				ptg = (int) (((float) ctoc / toc) * 100);

				roll[i].setText(Roll);
				attend[i].setText(ctoc + " of " + toc);
				pctg[i].setText(ptg + "%");
			}

			/* Start of extra student show */
			cursor = db.rawQuery(
					"SELECT STUDENTID AS CNT FROM ATTENDANCE WHERE "
							+ "COURSEID='"
							+ CheckAttendance.CID.getText().toString()
									.toUpperCase()
							+ "' AND DEPTID='"
							+ CheckAttendance.DPT.getText().toString()
									.toUpperCase() + "' AND GROUPD='" + groupAB
							+ "'", null);
			cursor.moveToFirst();

			name = cursor.getString(cursor.getColumnIndex("CNT"));
			int startRoll = (Integer.parseInt(name)) / 1000;
			if (groupAB.equals("A"))
				startRoll = (startRoll * 1000) + 1;
			else if (groupAB.equals("B"))
				startRoll = (startRoll * 1000) + 61;

			cursor = db.rawQuery(
					"SELECT COUNT(DISTINCT STUDENTID) AS CNT FROM ATTENDANCE WHERE "
							+ "COURSEID='"
							+ CheckAttendance.CID.getText().toString()
									.toUpperCase()
							+ "' AND DEPTID='"
							+ CheckAttendance.DPT.getText().toString()
									.toUpperCase() + "' AND GROUPD='" + groupAB
							+ "'" + " AND STUDENTID NOT BETWEEN " + startRoll
							+ " AND " + (startRoll + 60), null);
			cursor.moveToFirst();
			toc = Integer.parseInt(cursor.getString(cursor
					.getColumnIndex("CNT")));

			/*
			 * Toast.makeText(getApplicationContext(), "Extra student: " + toc,
			 * Toast.LENGTH_SHORT).show();
			 */

			if (toc != 0) {
				cursor = db.rawQuery(
						"SELECT DISTINCT STUDENTID FROM ATTENDANCE WHERE "
								+ "COURSEID='"
								+ CheckAttendance.CID.getText().toString()
										.toUpperCase()
								+ "' AND DEPTID='"
								+ CheckAttendance.DPT.getText().toString()
										.toUpperCase() + "' AND GROUPD='"
								+ groupAB + "'" + " AND STUDENTID NOT BETWEEN "
								+ startRoll + " AND " + (startRoll + 60), null);
				cursor.moveToFirst();
				// int Roll =
				// Integer.parseInt(cursor.getString(cursor.getColumnIndex("STUDENTID")));
				for (int j = 0; j < toc; j++) {
					String tempRoll = cursor.getString(cursor
							.getColumnIndex("STUDENTID"));
					tempRoll = tempRoll.length() < 7 ? ("0" + tempRoll)
							: tempRoll;

					roll[60 + j].setText(tempRoll);

					Cursor crsr = db.rawQuery(
							"SELECT COUNT(STUDENTID) AS CNT FROM ATTENDANCE WHERE "
									+ "COURSEID='"
									+ CheckAttendance.CID.getText().toString()
											.toUpperCase()
									+ "' AND DEPTID='"
									+ CheckAttendance.DPT.getText().toString()
											.toUpperCase() + "' AND GROUPD='"
									+ groupAB + "' AND STUDENTID=" + tempRoll,
							null);
					crsr.moveToFirst();

					name = crsr.getString(crsr.getColumnIndex("CNT"));
					ctoc = Integer.parseInt(name);
					ptg = (int) (((float) ctoc / totalClass) * 100);

					attend[60 + j].setText(ctoc + " of " + totalClass);
					pctg[60 + j].setText(ptg + "%");
					cursor.moveToNext();
				}
			}
			for (int i = toc; i < 5; i++) {
				roll[60 + i].setText("-");
				attend[60 + i].setText("-");
				pctg[60 + i].setText("-");
			}

			/*
			 * Toast.makeText(getApplicationContext(), "Start Roll: " +
			 * startRoll, Toast.LENGTH_SHORT).show();
			 */
			/* End of extra student show */

			cursor = db.rawQuery(
					"SELECT COUNT(DISTINCT DATE) AS CNT FROM ATTENDANCE WHERE "
							+ "COURSEID='"
							+ CheckAttendance.CID.getText().toString()
									.toUpperCase()
							+ "' AND DEPTID='"
							+ CheckAttendance.DPT.getText().toString()
									.toUpperCase() + "' AND GROUPD='" + groupAB
							+ "'", null);
			cursor.moveToFirst();

			name = cursor.getString(cursor.getColumnIndex("CNT"));

			Toast.makeText(getApplicationContext(),
					"Total number of class taken: " + name, Toast.LENGTH_SHORT)
					.show();
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), "No data found to show.",
					Toast.LENGTH_SHORT).show();
		}
	}

	public void printButtonAction() {
		
				if (totalClass == 0) {
					Toast.makeText(getApplicationContext(),
							"Nothing to print.", Toast.LENGTH_SHORT).show();
					return;
				}

				Toast.makeText(getApplicationContext(),
						"Printing getting ready...", Toast.LENGTH_SHORT).show();

				WebView webView = new WebView(ShowPercentage.this);
				webView.setWebViewClient(new WebViewClient() {
					public boolean shouldOverrideUrlLoading(WebView view,
							String url) {
						return false;
					}

					@Override
					public void onPageFinished(WebView view, String url) {
						createWebPrintJob(view);
						myWebView = null;
					}
				});
				try {
					webView.loadDataWithBaseURL(null, generateHtml(),
							"text/HTML", "UTF-8", null);
				} catch (Exception e) {
					Toast.makeText(getApplicationContext(),
							"Error occured in printing.", Toast.LENGTH_SHORT)
							.show();
				}
				myWebView = webView;
	}

	private void createWebPrintJob(WebView webView) {
		PrintManager printManager = (PrintManager) this
				.getSystemService(Context.PRINT_SERVICE);
		PrintDocumentAdapter printAdapter = webView
				.createPrintDocumentAdapter();
		String jobName = getString(R.string.app_name) + " Print Test";

		printManager.print("netcse Attendance", printAdapter,
				new PrintAttributes.Builder().build());
	}

	public void sectionParse() {
		groupAB = CheckAttendance.GRP.getText().toString().toUpperCase();
		if (groupAB.equalsIgnoreCase("A"))
			groupAB = "A";
		else if (groupAB.equalsIgnoreCase("B"))
			groupAB = "B";
	}

	public String getDept() {
		if (CheckAttendance.DPT.getText().toString().equalsIgnoreCase("CSE"))
			return "07";
		else if (CheckAttendance.DPT.getText().toString()
				.equalsIgnoreCase("EEE"))
			return "03";
		else if (CheckAttendance.DPT.getText().toString()
				.equalsIgnoreCase("CE"))
			return "01";
		else if (CheckAttendance.DPT.getText().toString()
				.equalsIgnoreCase("ME"))
			return "05";
		else if (CheckAttendance.DPT.getText().toString()
				.equalsIgnoreCase("LE"))
			return "09";
		else if (CheckAttendance.DPT.getText().toString()
				.equalsIgnoreCase("IPE"))
			return "11";
		else if (CheckAttendance.DPT.getText().toString()
				.equalsIgnoreCase("TE"))
			return "11";
		else if (CheckAttendance.DPT.getText().toString()
				.equalsIgnoreCase("ECE"))
			return "09";
		else if (CheckAttendance.DPT.getText().toString()
				.equalsIgnoreCase("BECM"))
			return "05";
		else if (CheckAttendance.DPT.getText().toString()
				.equalsIgnoreCase("BME"))
			return "05";
		else if (CheckAttendance.DPT.getText().toString()
				.equalsIgnoreCase("URP"))
			return "17";
		else
			return "00";
	}

	public void parseRollId() {
		roll[0] = (TextView) findViewById(R.id.textView10);
		roll[1] = (TextView) findViewById(R.id.textView20);
		roll[2] = (TextView) findViewById(R.id.textView30);
		roll[3] = (TextView) findViewById(R.id.textView40);
		roll[4] = (TextView) findViewById(R.id.textView50);
		roll[5] = (TextView) findViewById(R.id.textView60);
		roll[6] = (TextView) findViewById(R.id.textView70);
		roll[7] = (TextView) findViewById(R.id.textView80);
		roll[8] = (TextView) findViewById(R.id.textView90);
		roll[9] = (TextView) findViewById(R.id.textView100);
		roll[10] = (TextView) findViewById(R.id.textView110);
		roll[11] = (TextView) findViewById(R.id.textView120);
		roll[12] = (TextView) findViewById(R.id.textView130);
		roll[13] = (TextView) findViewById(R.id.textView140);
		roll[14] = (TextView) findViewById(R.id.textView150);
		roll[15] = (TextView) findViewById(R.id.textView160);
		roll[16] = (TextView) findViewById(R.id.textView170);
		roll[17] = (TextView) findViewById(R.id.textView180);
		roll[18] = (TextView) findViewById(R.id.textView190);
		roll[19] = (TextView) findViewById(R.id.textView200);
		roll[20] = (TextView) findViewById(R.id.textView210);
		roll[21] = (TextView) findViewById(R.id.textView220);
		roll[22] = (TextView) findViewById(R.id.textView230);
		roll[23] = (TextView) findViewById(R.id.textView240);
		roll[24] = (TextView) findViewById(R.id.textView250);
		roll[25] = (TextView) findViewById(R.id.textView260);
		roll[26] = (TextView) findViewById(R.id.textView270);
		roll[27] = (TextView) findViewById(R.id.textView280);
		roll[28] = (TextView) findViewById(R.id.textView290);
		roll[29] = (TextView) findViewById(R.id.textView300);
		roll[30] = (TextView) findViewById(R.id.textView310);
		roll[31] = (TextView) findViewById(R.id.textView320);
		roll[32] = (TextView) findViewById(R.id.textView330);
		roll[33] = (TextView) findViewById(R.id.textView340);
		roll[34] = (TextView) findViewById(R.id.textView350);
		roll[35] = (TextView) findViewById(R.id.textView360);
		roll[36] = (TextView) findViewById(R.id.textView370);
		roll[37] = (TextView) findViewById(R.id.textView380);
		roll[38] = (TextView) findViewById(R.id.textView390);
		roll[39] = (TextView) findViewById(R.id.textView400);
		roll[40] = (TextView) findViewById(R.id.textView410);
		roll[41] = (TextView) findViewById(R.id.textView420);
		roll[42] = (TextView) findViewById(R.id.textView430);
		roll[43] = (TextView) findViewById(R.id.textView440);
		roll[44] = (TextView) findViewById(R.id.textView450);
		roll[45] = (TextView) findViewById(R.id.textView460);
		roll[46] = (TextView) findViewById(R.id.textView470);
		roll[47] = (TextView) findViewById(R.id.textView480);
		roll[48] = (TextView) findViewById(R.id.textView490);
		roll[49] = (TextView) findViewById(R.id.textView500);
		roll[50] = (TextView) findViewById(R.id.textView510);
		roll[51] = (TextView) findViewById(R.id.textView520);
		roll[52] = (TextView) findViewById(R.id.textView530);
		roll[53] = (TextView) findViewById(R.id.textView540);
		roll[54] = (TextView) findViewById(R.id.textView550);
		roll[55] = (TextView) findViewById(R.id.textView560);
		roll[56] = (TextView) findViewById(R.id.textView570);
		roll[57] = (TextView) findViewById(R.id.textView580);
		roll[58] = (TextView) findViewById(R.id.textView590);
		roll[59] = (TextView) findViewById(R.id.textView600);
		roll[60] = (TextView) findViewById(R.id.textView610);
		roll[61] = (TextView) findViewById(R.id.textView620);
		roll[62] = (TextView) findViewById(R.id.textView630);
		roll[63] = (TextView) findViewById(R.id.textView640);
		roll[64] = (TextView) findViewById(R.id.textView650);

		attend[0] = (TextView) findViewById(R.id.textView11);
		attend[1] = (TextView) findViewById(R.id.textView21);
		attend[2] = (TextView) findViewById(R.id.textView31);
		attend[3] = (TextView) findViewById(R.id.textView41);
		attend[4] = (TextView) findViewById(R.id.textView51);
		attend[5] = (TextView) findViewById(R.id.textView61);
		attend[6] = (TextView) findViewById(R.id.textView71);
		attend[7] = (TextView) findViewById(R.id.textView81);
		attend[8] = (TextView) findViewById(R.id.textView91);
		attend[9] = (TextView) findViewById(R.id.textView101);
		attend[10] = (TextView) findViewById(R.id.textView111);
		attend[11] = (TextView) findViewById(R.id.textView121);
		attend[12] = (TextView) findViewById(R.id.textView131);
		attend[13] = (TextView) findViewById(R.id.textView141);
		attend[14] = (TextView) findViewById(R.id.textView151);
		attend[15] = (TextView) findViewById(R.id.textView161);
		attend[16] = (TextView) findViewById(R.id.textView171);
		attend[17] = (TextView) findViewById(R.id.textView181);
		attend[18] = (TextView) findViewById(R.id.textView191);
		attend[19] = (TextView) findViewById(R.id.textView201);
		attend[20] = (TextView) findViewById(R.id.textView211);
		attend[21] = (TextView) findViewById(R.id.textView221);
		attend[22] = (TextView) findViewById(R.id.textView231);
		attend[23] = (TextView) findViewById(R.id.textView241);
		attend[24] = (TextView) findViewById(R.id.textView251);
		attend[25] = (TextView) findViewById(R.id.textView261);
		attend[26] = (TextView) findViewById(R.id.textView271);
		attend[27] = (TextView) findViewById(R.id.textView281);
		attend[28] = (TextView) findViewById(R.id.textView291);
		attend[29] = (TextView) findViewById(R.id.textView301);
		attend[30] = (TextView) findViewById(R.id.textView311);
		attend[31] = (TextView) findViewById(R.id.textView321);
		attend[32] = (TextView) findViewById(R.id.textView331);
		attend[33] = (TextView) findViewById(R.id.textView341);
		attend[34] = (TextView) findViewById(R.id.textView351);
		attend[35] = (TextView) findViewById(R.id.textView361);
		attend[36] = (TextView) findViewById(R.id.textView371);
		attend[37] = (TextView) findViewById(R.id.textView381);
		attend[38] = (TextView) findViewById(R.id.textView391);
		attend[39] = (TextView) findViewById(R.id.textView401);
		attend[40] = (TextView) findViewById(R.id.textView411);
		attend[41] = (TextView) findViewById(R.id.textView421);
		attend[42] = (TextView) findViewById(R.id.textView431);
		attend[43] = (TextView) findViewById(R.id.textView441);
		attend[44] = (TextView) findViewById(R.id.textView451);
		attend[45] = (TextView) findViewById(R.id.textView461);
		attend[46] = (TextView) findViewById(R.id.textView471);
		attend[47] = (TextView) findViewById(R.id.textView481);
		attend[48] = (TextView) findViewById(R.id.textView491);
		attend[49] = (TextView) findViewById(R.id.textView501);
		attend[50] = (TextView) findViewById(R.id.textView511);
		attend[51] = (TextView) findViewById(R.id.textView521);
		attend[52] = (TextView) findViewById(R.id.textView531);
		attend[53] = (TextView) findViewById(R.id.textView541);
		attend[54] = (TextView) findViewById(R.id.textView551);
		attend[55] = (TextView) findViewById(R.id.textView561);
		attend[56] = (TextView) findViewById(R.id.textView571);
		attend[57] = (TextView) findViewById(R.id.textView581);
		attend[58] = (TextView) findViewById(R.id.textView591);
		attend[59] = (TextView) findViewById(R.id.textView601);
		attend[60] = (TextView) findViewById(R.id.textView611);
		attend[61] = (TextView) findViewById(R.id.textView621);
		attend[62] = (TextView) findViewById(R.id.textView631);
		attend[63] = (TextView) findViewById(R.id.textView641);
		attend[64] = (TextView) findViewById(R.id.textView651);

		pctg[0] = (TextView) findViewById(R.id.textView12);
		pctg[1] = (TextView) findViewById(R.id.textView22);
		pctg[2] = (TextView) findViewById(R.id.textView32);
		pctg[3] = (TextView) findViewById(R.id.textView42);
		pctg[4] = (TextView) findViewById(R.id.textView52);
		pctg[5] = (TextView) findViewById(R.id.textView62);
		pctg[6] = (TextView) findViewById(R.id.textView72);
		pctg[7] = (TextView) findViewById(R.id.textView82);
		pctg[8] = (TextView) findViewById(R.id.textView92);
		pctg[9] = (TextView) findViewById(R.id.textView102);
		pctg[10] = (TextView) findViewById(R.id.textView112);
		pctg[11] = (TextView) findViewById(R.id.textView122);
		pctg[12] = (TextView) findViewById(R.id.textView132);
		pctg[13] = (TextView) findViewById(R.id.textView142);
		pctg[14] = (TextView) findViewById(R.id.textView152);
		pctg[15] = (TextView) findViewById(R.id.textView162);
		pctg[16] = (TextView) findViewById(R.id.textView172);
		pctg[17] = (TextView) findViewById(R.id.textView182);
		pctg[18] = (TextView) findViewById(R.id.textView192);
		pctg[19] = (TextView) findViewById(R.id.textView202);
		pctg[20] = (TextView) findViewById(R.id.textView212);
		pctg[21] = (TextView) findViewById(R.id.textView222);
		pctg[22] = (TextView) findViewById(R.id.textView232);
		pctg[23] = (TextView) findViewById(R.id.textView242);
		pctg[24] = (TextView) findViewById(R.id.textView252);
		pctg[25] = (TextView) findViewById(R.id.textView262);
		pctg[26] = (TextView) findViewById(R.id.textView272);
		pctg[27] = (TextView) findViewById(R.id.textView282);
		pctg[28] = (TextView) findViewById(R.id.textView292);
		pctg[29] = (TextView) findViewById(R.id.textView302);
		pctg[30] = (TextView) findViewById(R.id.textView312);
		pctg[31] = (TextView) findViewById(R.id.textView322);
		pctg[32] = (TextView) findViewById(R.id.textView332);
		pctg[33] = (TextView) findViewById(R.id.textView342);
		pctg[34] = (TextView) findViewById(R.id.textView352);
		pctg[35] = (TextView) findViewById(R.id.textView362);
		pctg[36] = (TextView) findViewById(R.id.textView372);
		pctg[37] = (TextView) findViewById(R.id.textView382);
		pctg[38] = (TextView) findViewById(R.id.textView392);
		pctg[39] = (TextView) findViewById(R.id.textView402);
		pctg[40] = (TextView) findViewById(R.id.textView412);
		pctg[41] = (TextView) findViewById(R.id.textView422);
		pctg[42] = (TextView) findViewById(R.id.textView432);
		pctg[43] = (TextView) findViewById(R.id.textView442);
		pctg[44] = (TextView) findViewById(R.id.textView452);
		pctg[45] = (TextView) findViewById(R.id.textView462);
		pctg[46] = (TextView) findViewById(R.id.textView472);
		pctg[47] = (TextView) findViewById(R.id.textView482);
		pctg[48] = (TextView) findViewById(R.id.textView492);
		pctg[49] = (TextView) findViewById(R.id.textView502);
		pctg[50] = (TextView) findViewById(R.id.textView512);
		pctg[51] = (TextView) findViewById(R.id.textView522);
		pctg[52] = (TextView) findViewById(R.id.textView532);
		pctg[53] = (TextView) findViewById(R.id.textView542);
		pctg[54] = (TextView) findViewById(R.id.textView552);
		pctg[55] = (TextView) findViewById(R.id.textView562);
		pctg[56] = (TextView) findViewById(R.id.textView572);
		pctg[57] = (TextView) findViewById(R.id.textView582);
		pctg[58] = (TextView) findViewById(R.id.textView592);
		pctg[59] = (TextView) findViewById(R.id.textView602);
		pctg[60] = (TextView) findViewById(R.id.textView612);
		pctg[61] = (TextView) findViewById(R.id.textView622);
		pctg[62] = (TextView) findViewById(R.id.textView632);
		pctg[63] = (TextView) findViewById(R.id.textView642);
		pctg[64] = (TextView) findViewById(R.id.textView652);

	}

	public String generateHtml() {
		Cursor cursor = db.rawQuery(
				"SELECT COUNT(DISTINCT DATE) AS CNT FROM ATTENDANCE WHERE "
						+ "COURSEID='"
						+ CheckAttendance.CID.getText().toString()
								.toUpperCase()
						+ "' AND DEPTID='"
						+ CheckAttendance.DPT.getText().toString()
								.toUpperCase() + "' AND GROUPD='" + groupAB
						+ "'", null);

		int totalClass = 0;

		cursor.moveToFirst();
		totalClass = Integer.parseInt(cursor.getString(cursor
				.getColumnIndex("CNT")));

		String s = "";
		cursor = db.rawQuery("SELECT DATE FROM ATTENDANCE WHERE "
				+ "COURSEID='"
				+ CheckAttendance.CID.getText().toString().toUpperCase()
				+ "' AND DEPTID='"
				+ CheckAttendance.DPT.getText().toString().toUpperCase()
				+ "' AND GROUPD='" + groupAB + "' GROUP BY DATE", null);

		s = "<html>"
				+ "<body>"
				+ "<center>"
				+ "<h2><b style=\"color:green\">Khulna University of Engineering & Technology (KUET)<br/>"
				+ "<img style=\"margin-left:-430px\" \" src=\"file:///android_asset/www/kuetlogo.png\" alt=\"KUET Logo\"/>"
				+ "<p style=\"margin-top:-70px\">Department of "
				+ CheckAttendance.DPT.getText().toString().toUpperCase()
				+ "</h2>"
				+ "</p><p style=\"margin-top:-25px\">Course No.: "
				+ CheckAttendance.CID.getText().toString().toUpperCase()
				+ ", (Teacher id: "
				+ TeacherLgin.TID.getText().toString()
				+ ")<br/>"
				+ "<b style=\"color:blue\">Attendance Sheet of 2K"
				+ CheckAttendance.BATCH.getText().toString()
				+ " batch</b>"
				+ "</p><table cellspacing=\"0\" cellpadding=\"0\" cellpadding=\"0\" border=\"1\" width=\"60%\" style=\"border: 1px solid #c3c3c3; border-collapse: collapse;\">"
				+ "<tr>" + "<th>Roll</th>";

		String tempDate = "";
		String arrayOfDate[] = new String[50];

		cursor.moveToFirst();

		for (int i = 0; i < totalClass; i++) {
			try {
				tempDate = cursor.getString(cursor.getColumnIndex("DATE"));
				s += "<th>" + tempDate + "</th>";
				arrayOfDate[i] = tempDate;

				cursor.moveToNext();
			} catch (Exception e) {
				Toast.makeText(getApplicationContext(),
						"Exception in date access", Toast.LENGTH_SHORT).show();
			}
		}

		s += "<th>Present</th>";
		s += "<th>Percentage</th>";
		s += "</tr>";

		for (int i = 0; i < 60; i++) {
			int rll = 0;
			if (groupAB.equals("A")) {
				rll = i;
			} else if (groupAB.equals("B")) {
				rll = 60 + i;
			}
			String RLL;
			if (rll < 9) {
				RLL = CheckAttendance.BATCH.getText().toString() + getDept()
						+ "00" + (rll + 1);
			} else if (rll < 99) {
				RLL = CheckAttendance.BATCH.getText().toString() + getDept()
						+ "0" + (rll + 1);
			} else {
				RLL = CheckAttendance.BATCH.getText().toString() + getDept()
						+ "" + (rll + 1);
			}

			s += "<tr>";

			cursor = db.rawQuery(
					"SELECT COUNT(STUDENTID) AS CNT FROM ATTENDANCE WHERE "
							+ "COURSEID='"
							+ CheckAttendance.CID.getText().toString()
									.toUpperCase()
							+ "' AND DEPTID='"
							+ CheckAttendance.DPT.getText().toString()
									.toUpperCase() + "' AND GROUPD='" + groupAB
							+ "' AND STUDENTID=" + RLL, null);
			cursor.moveToFirst();

			String name = cursor.getString(cursor.getColumnIndex("CNT"));
			int ctoc = Integer.parseInt(name);
			int pctg = (int) (((float) ctoc / totalClass) * 100);

			if (pctg < 60)
				s += "<td style=\"color:red\">" + RLL + "</td>";
			else
				s += "<td style=\"color:green\">" + RLL + "</td>";

			cursor = db.rawQuery("SELECT DATE FROM ATTENDANCE WHERE "
					+ "COURSEID='"
					+ CheckAttendance.CID.getText().toString().toUpperCase()
					+ "' AND DEPTID='"
					+ CheckAttendance.DPT.getText().toString().toUpperCase()
					+ "' AND GROUPD='" + groupAB + "' AND STUDENTID=" + RLL
					+ " GROUP BY DATE", null);

			cursor.moveToFirst();

			for (int j = 0; j < totalClass; j++) {
				try {
					if (arrayOfDate[j].equals(cursor.getString(cursor
							.getColumnIndex("DATE")))) {
						s += "<td style=\"color:green\">&#10004</td>";
						cursor.moveToNext();
					} else {
						s += "<td style=\"color:red\">X</td>";
					}
				} catch (Exception e) {
					s += "<td style=\"color:red\">X</td>";
				}
			}

			s += "<td>" + ctoc + "/" + totalClass + "</td>";
			if (pctg < 60)
				s += "<td style=\"color:red\">" + pctg + "%</td>";
			else
				s += "<td style=\"color:green\">" + pctg + "%</td>";
			s += "</tr>";
		}

		/* Start of extra student show */
		try {

			cursor = db.rawQuery(
					"SELECT DISTINCT STUDENTID AS CNT FROM ATTENDANCE WHERE "
							+ "COURSEID='"
							+ CheckAttendance.CID.getText().toString()
									.toUpperCase()
							+ "' AND DEPTID='"
							+ CheckAttendance.DPT.getText().toString()
									.toUpperCase() + "' AND GROUPD='" + groupAB
							+ "'", null);
			cursor.moveToFirst();

			String name = cursor.getString(cursor.getColumnIndex("CNT"));
			int startRoll = (Integer.parseInt(name)) / 1000;
			if (groupAB.equals("A"))
				startRoll = (startRoll * 1000) + 1;
			else if (groupAB.equals("B"))
				startRoll = (startRoll * 1000) + 61;

			cursor = db.rawQuery(
					"SELECT COUNT(DISTINCT STUDENTID) AS CNT FROM ATTENDANCE WHERE "
							+ "COURSEID='"
							+ CheckAttendance.CID.getText().toString()
									.toUpperCase()
							+ "' AND DEPTID='"
							+ CheckAttendance.DPT.getText().toString()
									.toUpperCase() + "' AND GROUPD='" + groupAB
							+ "'" + " AND STUDENTID NOT BETWEEN " + startRoll
							+ " AND " + (startRoll + 60), null);
			cursor.moveToFirst();
			int toc = Integer.parseInt(cursor.getString(cursor
					.getColumnIndex("CNT")));

			/*
			 * Toast.makeText(getApplicationContext(), "Extra student: " + toc,
			 * Toast.LENGTH_SHORT).show();
			 */

			if (toc != 0) {
				cursor = db.rawQuery(
						"SELECT DISTINCT STUDENTID FROM ATTENDANCE WHERE "
								+ "COURSEID='"
								+ CheckAttendance.CID.getText().toString()
										.toUpperCase()
								+ "' AND DEPTID='"
								+ CheckAttendance.DPT.getText().toString()
										.toUpperCase() + "' AND GROUPD='"
								+ groupAB + "'" + " AND STUDENTID NOT BETWEEN "
								+ startRoll + " AND " + (startRoll + 60), null);
				cursor.moveToFirst();
				// int Roll =
				// Integer.parseInt(cursor.getString(cursor.getColumnIndex("STUDENTID")));
				for (int j = 0; j < toc; j++) {
					String tempRoll = cursor.getString(cursor
							.getColumnIndex("STUDENTID"));
					tempRoll = tempRoll.length() < 7 ? ("0" + tempRoll)
							: tempRoll;

					roll[60 + j].setText(tempRoll);

					Cursor crsr = db.rawQuery(
							"SELECT COUNT(STUDENTID) AS CNT FROM ATTENDANCE WHERE "
									+ "COURSEID='"
									+ CheckAttendance.CID.getText().toString()
											.toUpperCase()
									+ "' AND DEPTID='"
									+ CheckAttendance.DPT.getText().toString()
											.toUpperCase() + "' AND GROUPD='"
									+ groupAB + "' AND STUDENTID=" + tempRoll,
							null);
					crsr.moveToFirst();

					name = crsr.getString(crsr.getColumnIndex("CNT"));
					int ctoc = Integer.parseInt(name);
					int ptg = (int) (((float) ctoc / totalClass) * 100);

					attend[60 + j].setText(ctoc + " of " + totalClass);
					pctg[60 + j].setText(ptg + "%");

					s += "<tr>";

					crsr = db.rawQuery(
							"SELECT COUNT(STUDENTID) AS CNT FROM ATTENDANCE WHERE "
									+ "COURSEID='"
									+ CheckAttendance.CID.getText().toString()
											.toUpperCase()
									+ "' AND DEPTID='"
									+ CheckAttendance.DPT.getText().toString()
											.toUpperCase() + "' AND GROUPD='"
									+ groupAB + "' AND STUDENTID=" + tempRoll,
							null);
					crsr.moveToFirst();

					name = crsr.getString(crsr.getColumnIndex("CNT"));
					ctoc = Integer.parseInt(name);
					int percentage = (int) (((float) ctoc / totalClass) * 100);

					if (percentage < 60)
						s += "<td style=\"color:red\">" + tempRoll + "</td>";
					else
						s += "<td style=\"color:green\">" + tempRoll + "</td>";

					crsr = db.rawQuery("SELECT DATE FROM ATTENDANCE WHERE "
							+ "COURSEID='"
							+ CheckAttendance.CID.getText().toString()
									.toUpperCase()
							+ "' AND DEPTID='"
							+ CheckAttendance.DPT.getText().toString()
									.toUpperCase() + "' AND GROUPD='" + groupAB
							+ "' AND STUDENTID=" + tempRoll + " GROUP BY DATE",
							null);
					crsr.moveToFirst();

					for (int i = 0; i < totalClass; i++) {
						try {
							if (arrayOfDate[i].equals(crsr.getString(crsr
									.getColumnIndex("DATE")))) {
								s += "<td style=\"color:green\">&#10004</td>";
								crsr.moveToNext();
							} else {
								s += "<td style=\"color:red\">X</td>";
							}
						} catch (Exception e) {
							s += "<td style=\"color:red\">X</td>";
						}
					}

					s += "<td>" + ctoc + "/" + totalClass + "</td>";
					if (percentage < 60)
						s += "<td style=\"color:red\">" + percentage + "%</td>";
					else
						s += "<td style=\"color:green\">" + percentage
								+ "%</td>";
					s += "</tr>";

					cursor.moveToNext();
				}
			}

			/*
			 * Toast.makeText(getApplicationContext(), "Start Roll: " +
			 * startRoll, Toast.LENGTH_SHORT).show();
			 */
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(),
					"Error in extra student printing:  " + e.toString(),
					Toast.LENGTH_SHORT).show();
		}
		/* End of extra student show */

		s += "</table><br/>"
				+ "<footer style=\"color:blue\">The page is printed from MyAttendance software.<br/>All rights reserved by Developers.</footer>"
				+ "</p>" + "</center>" + "</body>" + "</html>";

		Toast.makeText(getApplicationContext(),
				"Attendance is ready to print.", Toast.LENGTH_SHORT).show();
		String htmlDocument = s;
		return htmlDocument;
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.show_percentage, menu);
		return true;
	}

	public boolean onOptionsItemSelected(android.view.MenuItem item) {
		int id = item.getItemId();
		switch (id) {
		case R.id.excelMenu:
			if (saveExcelFile(ShowPercentage.this, "netcse.xls"))
				Toast.makeText(getApplicationContext(),
						"Data saved successfully to excel file.",
						Toast.LENGTH_SHORT).show();
			else
				Toast.makeText(getApplicationContext(),
						"Data not saved to excel file.", Toast.LENGTH_SHORT).show();
			break;
		case R.id.printMenu:
			printButtonAction();
			break;
		case R.id.sendMailOption:
			Intent localIntent = new Intent(ShowPercentage.this, SendMail.class);
			ShowPercentage.this.startActivity(localIntent);
			break;
		case R.id.sendMessageOption:
			Intent intent = new Intent(ShowPercentage.this, SendMessage.class);
			ShowPercentage.this.startActivity(intent);
			break;
		}
		return super.onOptionsItemSelected(item);
	};

	private boolean saveExcelFile(Context context, String fileName) {
		
		// check if available and not read only
		if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {
			Log.w("FileUtils", "Storage not available or read only");
			return false;
		}

		boolean success = false;

		// New Workbook
		Workbook wb = new HSSFWorkbook();

		Cell c = null;

		// Cell style for header row
		CellStyle cs = wb.createCellStyle();
		// cs.setFillForegroundColor(HSSFColor.LIME.index);
		// cs.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);

		Cursor cursor = db.rawQuery(
				"SELECT COUNT(DISTINCT DATE) AS CNT FROM ATTENDANCE WHERE "
						+ "COURSEID='"
						+ CheckAttendance.CID.getText().toString()
								.toUpperCase()
						+ "' AND DEPTID='"
						+ CheckAttendance.DPT.getText().toString()
								.toUpperCase() + "' AND GROUPD='" + groupAB
						+ "'", null);
		cursor.moveToFirst();

		int totalClass = 0;
		totalClass = Integer.parseInt(cursor.getString(cursor
				.getColumnIndex("CNT")));

		cursor = db.rawQuery("SELECT DATE FROM ATTENDANCE WHERE "
				+ "COURSEID='"
				+ CheckAttendance.CID.getText().toString().toUpperCase()
				+ "' AND DEPTID='"
				+ CheckAttendance.DPT.getText().toString().toUpperCase()
				+ "' AND GROUPD='" + groupAB + "' GROUP BY DATE", null);
		cursor.moveToFirst();

		String tempDate = "";
		String arrayOfDate[] = new String[50];

		// New Sheet
		Sheet sheet1 = null;
		sheet1 = wb.createSheet(CheckAttendance.CID.getText().toString()
				.toUpperCase());

		// Generate column headings
		Row row = sheet1.createRow(0);
		c = row.createCell(0);
		c.setCellValue("Roll");
		c.setCellStyle(cs);

		int i;
		for (i = 0; i < totalClass; i++) {
			try {
				tempDate = cursor.getString(cursor.getColumnIndex("DATE"));

				c = row.createCell(i + 1);
				c.setCellValue(tempDate);
				c.setCellStyle(cs);

				arrayOfDate[i] = tempDate;

				cursor.moveToNext();
			} catch (Exception e) {
				Toast.makeText(getApplicationContext(),
						"Exception in date access", Toast.LENGTH_SHORT).show();
			}
		}

		c = row.createCell(i + 1);
		c.setCellValue("Present");
		c.setCellStyle(cs);

		c = row.createCell(i + 2);
		c.setCellValue("Percentage");
		c.setCellStyle(cs);

		for (i = 0; i < 60; i++) {
			int rll = 0;
			if (groupAB.equals("A")) {
				rll = i;
			} else if (groupAB.equals("B")) {
				rll = 60 + i;
			}
			String RLL;
			if (rll < 9) {
				RLL = CheckAttendance.BATCH.getText().toString() + getDept()
						+ "00" + (rll + 1);
			} else if (rll < 99) {
				RLL = CheckAttendance.BATCH.getText().toString() + getDept()
						+ "0" + (rll + 1);
			} else {
				RLL = CheckAttendance.BATCH.getText().toString() + getDept()
						+ "" + (rll + 1);
			}

			cursor = db.rawQuery(
					"SELECT COUNT(STUDENTID) AS CNT FROM ATTENDANCE WHERE "
							+ "COURSEID='"
							+ CheckAttendance.CID.getText().toString()
									.toUpperCase()
							+ "' AND DEPTID='"
							+ CheckAttendance.DPT.getText().toString()
									.toUpperCase() + "' AND GROUPD='" + groupAB
							+ "' AND STUDENTID=" + RLL, null);
			cursor.moveToFirst();

			String name = cursor.getString(cursor.getColumnIndex("CNT"));
			int ctoc = Integer.parseInt(name);
			int pctg = (int) (((float) ctoc / totalClass) * 100);

			row = sheet1.createRow(i + 1);

			c = row.createCell(0);
			c.setCellValue(RLL);
			c.setCellStyle(cs);

			cursor = db.rawQuery("SELECT DATE FROM ATTENDANCE WHERE "
					+ "COURSEID='"
					+ CheckAttendance.CID.getText().toString().toUpperCase()
					+ "' AND DEPTID='"
					+ CheckAttendance.DPT.getText().toString().toUpperCase()
					+ "' AND GROUPD='" + groupAB + "' AND STUDENTID=" + RLL
					+ " GROUP BY DATE", null);

			cursor.moveToFirst();

			int j;
			for (j = 0; j < totalClass; j++) {
				try {
					if (arrayOfDate[j].equals(cursor.getString(cursor
							.getColumnIndex("DATE")))) {
						c = row.createCell(j + 1);
						c.setCellValue("✔");
						c.setCellStyle(cs);

						cursor.moveToNext();
					} else {
						c = row.createCell(j + 1);
						c.setCellValue("X");
						c.setCellStyle(cs);
					}
				} catch (Exception e) {
					c = row.createCell(j + 1);
					c.setCellValue("X");
					c.setCellStyle(cs);
				}
			}

			c = row.createCell(j + 1);
			c.setCellValue(ctoc + "/" + totalClass);
			c.setCellStyle(cs);

			c = row.createCell(j + 2);
			c.setCellValue(pctg + "%");
			c.setCellStyle(cs);
		}

		/*
		 * sheet1.setColumnWidth(0, (15 * 500)); sheet1.setColumnWidth(1, (15 *
		 * 500)); sheet1.setColumnWidth(2, (15 * 500));
		 */

		/* Start of extra student show */
		try {

			cursor = db.rawQuery(
					"SELECT DISTINCT STUDENTID AS CNT FROM ATTENDANCE WHERE "
							+ "COURSEID='"
							+ CheckAttendance.CID.getText().toString()
									.toUpperCase()
							+ "' AND DEPTID='"
							+ CheckAttendance.DPT.getText().toString()
									.toUpperCase() + "' AND GROUPD='" + groupAB
							+ "'", null);
			cursor.moveToFirst();

			String name = cursor.getString(cursor.getColumnIndex("CNT"));
			int startRoll = (Integer.parseInt(name)) / 1000;
			if (groupAB.equals("A"))
				startRoll = (startRoll * 1000) + 1;
			else if (groupAB.equals("B"))
				startRoll = (startRoll * 1000) + 61;

			cursor = db.rawQuery(
					"SELECT COUNT(DISTINCT STUDENTID) AS CNT FROM ATTENDANCE WHERE "
							+ "COURSEID='"
							+ CheckAttendance.CID.getText().toString()
									.toUpperCase()
							+ "' AND DEPTID='"
							+ CheckAttendance.DPT.getText().toString()
									.toUpperCase() + "' AND GROUPD='" + groupAB
							+ "'" + " AND STUDENTID NOT BETWEEN " + startRoll
							+ " AND " + (startRoll + 60), null);
			cursor.moveToFirst();
			int toc = Integer.parseInt(cursor.getString(cursor
					.getColumnIndex("CNT")));

			/*
			 * Toast.makeText(getApplicationContext(), "Extra student: " + toc,
			 * Toast.LENGTH_SHORT).show();
			 */

			if (toc != 0) {
				cursor = db.rawQuery(
						"SELECT DISTINCT STUDENTID FROM ATTENDANCE WHERE "
								+ "COURSEID='"
								+ CheckAttendance.CID.getText().toString()
										.toUpperCase()
								+ "' AND DEPTID='"
								+ CheckAttendance.DPT.getText().toString()
										.toUpperCase() + "' AND GROUPD='"
								+ groupAB + "'" + " AND STUDENTID NOT BETWEEN "
								+ startRoll + " AND " + (startRoll + 60), null);
				cursor.moveToFirst();
				// int Roll =
				// Integer.parseInt(cursor.getString(cursor.getColumnIndex("STUDENTID")));
				for (int j = 0; j < toc; j++) {
					String tempRoll = cursor.getString(cursor
							.getColumnIndex("STUDENTID"));
					tempRoll = tempRoll.length() < 7 ? ("0" + tempRoll)
							: tempRoll;

					roll[60 + j].setText(tempRoll);

					row = sheet1.createRow(61 + j);
					c = row.createCell(0);
					c.setCellValue(tempRoll);
					c.setCellStyle(cs);

					Cursor crsr = db.rawQuery(
							"SELECT COUNT(STUDENTID) AS CNT FROM ATTENDANCE WHERE "
									+ "COURSEID='"
									+ CheckAttendance.CID.getText().toString()
											.toUpperCase()
									+ "' AND DEPTID='"
									+ CheckAttendance.DPT.getText().toString()
											.toUpperCase() + "' AND GROUPD='"
									+ groupAB + "' AND STUDENTID=" + tempRoll,
							null);
					crsr.moveToFirst();

					name = crsr.getString(crsr.getColumnIndex("CNT"));
					int ctoc = Integer.parseInt(name);
					int ptg = (int) (((float) ctoc / totalClass) * 100);

					attend[60 + j].setText(ctoc + " of " + totalClass);
					pctg[60 + j].setText(ptg + "%");

					crsr = db.rawQuery(
							"SELECT COUNT(STUDENTID) AS CNT FROM ATTENDANCE WHERE "
									+ "COURSEID='"
									+ CheckAttendance.CID.getText().toString()
											.toUpperCase()
									+ "' AND DEPTID='"
									+ CheckAttendance.DPT.getText().toString()
											.toUpperCase() + "' AND GROUPD='"
									+ groupAB + "' AND STUDENTID=" + tempRoll,
							null);
					crsr.moveToFirst();

					name = crsr.getString(crsr.getColumnIndex("CNT"));
					ctoc = Integer.parseInt(name);
					int percentage = (int) (((float) ctoc / totalClass) * 100);

					crsr = db.rawQuery("SELECT DATE FROM ATTENDANCE WHERE "
							+ "COURSEID='"
							+ CheckAttendance.CID.getText().toString()
									.toUpperCase()
							+ "' AND DEPTID='"
							+ CheckAttendance.DPT.getText().toString()
									.toUpperCase() + "' AND GROUPD='" + groupAB
							+ "' AND STUDENTID=" + tempRoll + " GROUP BY DATE",
							null);
					crsr.moveToFirst();

					for (i = 0; i < totalClass; i++) {
						try {
							if (arrayOfDate[i].equals(crsr.getString(crsr
									.getColumnIndex("DATE")))) {
								c = row.createCell(i + 1);
								c.setCellValue("✔");
								c.setCellStyle(cs);

								crsr.moveToNext();
							} else {
								c = row.createCell(i + 1);
								c.setCellValue("X");
								c.setCellStyle(cs);
							}
						} catch (Exception e) {
							c = row.createCell(i + 1);
							c.setCellValue("X");
							c.setCellStyle(cs);
						}
					}

					c = row.createCell(i + 1);
					c.setCellValue(ctoc + "/" + totalClass);
					c.setCellStyle(cs);

					c = row.createCell(i + 2);
					c.setCellValue(percentage + "%");
					c.setCellStyle(cs);

					cursor.moveToNext();
				}
			}
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(),
					"Error in extra student printing:  " + e.toString(),
					Toast.LENGTH_SHORT).show();
		}
		/* End of extra student show */

		// Create a path where we will place our List of objects on external
		// storage
		fileName = CheckAttendance.CID.getText().toString().toUpperCase()
				+ ".xls";
		File file = new File(context.getExternalFilesDir(null), fileName);
		FileOutputStream os = null;

		try {
			os = new FileOutputStream(file);
			wb.write(os);
			Log.w("FileUtils", "Writing file" + file);
			success = true;
		} catch (IOException e) {
			Log.w("FileUtils", "Error writing " + file, e);
		} catch (Exception e) {
			Log.w("FileUtils", "Failed to save file", e);
		} finally {
			try {
				if (null != os)
					os.close();
			} catch (Exception ex) {
			}
		}

		return success;
	}

	public boolean isExternalStorageReadOnly() {
		String extStorageState = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState)) {
			return true;
		}
		
		Toast.makeText(getApplicationContext(),
				"Sir, your external storage is read only.",
				Toast.LENGTH_SHORT).show();
		return false;
	}

	public boolean isExternalStorageAvailable() {
		String extStorageState = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(extStorageState)) {
			return true;
		}
		Toast.makeText(getApplicationContext(),
				"Sir, your external storage is not available.",
				Toast.LENGTH_SHORT).show();
		return false;
	}

	private WebView myWebView;

	TextView roll[] = new TextView[65];
	TextView attend[] = new TextView[65];
	TextView pctg[] = new TextView[65];

}
