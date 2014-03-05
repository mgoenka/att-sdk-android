package com.att.iamsampleapp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.att.api.error.InAppMessagingError;
import com.att.api.immn.listener.ATTIAMListener;
import com.att.api.immn.service.IAMManager;
import com.att.api.immn.service.MessageContent;
import com.att.api.oauth.OAuthToken;

public class MMSContent extends Activity {

	private static final String TAG = "MMS Attachment Activity";
	String[] mmsContentName, mmsContentType, mmsContentUrl, mmsType;
	OAuthToken token;
	IAMManager iamManager;
	MessageContent msgResponse;
	ListView MessageContentListView;
	ArrayList<String> listItems = new ArrayList<String>();
	ArrayAdapter<String> adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mmscontent);

		Bundle ext = getIntent().getExtras();
		mmsContentName = (String[]) ext.get("MMSContentName");
		mmsContentType = (String[]) ext.get("MMSContentName");
		mmsContentUrl = (String[]) ext.get("MMSContentUrl");
		token = new OAuthToken(Config.token, OAuthToken.NO_EXPIRATION,
				Config.refreshToken);

		for (int n = 0; n < mmsContentName.length; n++) {

			if (mmsContentName[n].contains(".smil") || mmsContentName[n].length() == 0)
				continue;

			iamManager = new IAMManager(Config.fqdn, token,
					new getMessageContentListener());
			String mmsContentDetails[] = mmsContentUrl[n].split("/");
			iamManager.GetMessageContent(
					mmsContentDetails[mmsContentDetails.length - 3],
					mmsContentDetails[mmsContentDetails.length - 1]);
		}

		MessageContentListView = (ListView) findViewById(R.id.messagecontentList);
		adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, listItems);
		MessageContentListView.setAdapter(adapter);

		MessageContentListView
				.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View View,
							int position, long id) {

						String filePath = Environment
								.getExternalStorageDirectory().getPath()
								+ "/" + Config.iamDownloadDirectory + "/"
								+ listItems.get(position);

						String extension = MimeTypeMap
								.getFileExtensionFromUrl(filePath);
						MimeTypeMap mType = MimeTypeMap.getSingleton();
						String mimetype = mType
								.getMimeTypeFromExtension(extension
										.toLowerCase());

						Uri uri = getImageContentUri(getApplicationContext(),
								filePath);

						try {
							if (mimetype.contains("video")
									|| mimetype.contains("audio")
									|| mimetype.contains("image")) {
								Intent launchIntent = new Intent();
								launchIntent.setAction(Intent.ACTION_VIEW);
								launchIntent.setDataAndType(uri, mimetype);
								startActivity(launchIntent);
							} else if (mimetype.contains("text")) {

							} else {
								Utils.toastHere(getApplicationContext(), TAG,
										"Unable to recognize the media attachment ");
							}
						} catch (Exception e) {
							// TODO Auto-generated catch block
							Utils.toastHere(getApplicationContext(), TAG,
									        "Unable to open media attachment. Use " + filePath + " to open content." );
						}
					}
				});
		
		showProgressDialog("Fetching the Contents...");

	}
	ProgressDialog pDialog;

	public void showProgressDialog(String dialogMessage) {

		if (null == pDialog)
			pDialog = new ProgressDialog(this);
		pDialog.setCancelable(false);
		pDialog.setMessage(dialogMessage);
		pDialog.show();
	}
	
	public void dismissProgressDialog() {
		if (null != pDialog) {
			pDialog.dismiss();
		}
	}

	public static Uri getImageContentUri(Context context, String filePath) {

		Cursor cursor = context.getContentResolver().query(
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
				new String[] { MediaStore.Images.Media._ID },
				MediaStore.Images.Media.DATA + "=? ",
				new String[] { filePath }, null);

		if (cursor != null && cursor.moveToFirst()) {
			int id = cursor.getInt(cursor
					.getColumnIndex(MediaStore.MediaColumns._ID));
			Uri baseUri = Uri.parse("content://media/external/images/media");
			Uri uri = Uri.withAppendedPath(baseUri, "" + id);
			return uri;
		} else {
			ContentValues values = new ContentValues();
			values.put(MediaStore.Images.Media.DATA, filePath);
			return context.getContentResolver().insert(
					MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
		}
	}

	private class getMessageContentListener implements ATTIAMListener {

		@Override
		public void onSuccess(Object response) {
			
			dismissProgressDialog();
			msgResponse = (MessageContent) response;
/*
 			if (null != msgResponse) {
				Toast toast = Toast.makeText(getApplicationContext(),
						"getMessageContentListener onSuccess : Message : "
								+ msgResponse.getContentType(),
						Toast.LENGTH_LONG);
				toast.show();
			}
*/
			/*
			 * if(msgResponse.getContentType().contains("TEXT/PLAIN")){
			 * 
			 * String tmp = msgResponse.getStream().toString(); TextView txt =
			 * (TextView) findViewById(R.id.mmsmessage);
			 * txt.setText(msgResponse.getStream().toString()); }else{
			 */

			GetMessageContentTestTask getMessageContentTestTask = new GetMessageContentTestTask();
			getMessageContentTestTask.execute(msgResponse);
		}

		@Override
		public void onError(InAppMessagingError error) {
			dismissProgressDialog();
			Toast toast = Toast.makeText(getApplicationContext(), "Message : "
					+"getMessageContentListener Error Callback",
					Toast.LENGTH_LONG);
			toast.show();
		}
		
	}

	public class GetMessageContentTestTask extends
			AsyncTask<MessageContent, Void, String> {

		@Override
		protected String doInBackground(MessageContent... params) {

			InputStream instream = params[0].getStream();
			String rootPath = Environment.getExternalStorageDirectory()
					.getPath();

			String dirPath = rootPath + "/" + Config.iamDownloadDirectory + "/";

			File iamDir = new File(dirPath);
			if (!iamDir.exists()) {
				iamDir.mkdirs();
			}

			String[] contentTypeStr = params[0].getContentType().split("=");
			String[] fileName = contentTypeStr[1].split(";");
			// String filePath = dirPath + contentTypeStr[1];
			String filePath = dirPath + fileName[0];

			if (fileName[0].contains(".txt")) {

				BufferedReader r = new BufferedReader(new InputStreamReader(
						instream));
				StringBuilder total = new StringBuilder();
				String line;
				try {
					while ((line = r.readLine()) != null) {
						total.append(line);
					}
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				filePath = total.toString();
			} else {

				FileOutputStream output = null;
				try {
					output = new FileOutputStream(filePath);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				int bufferSize = 1024;
				byte[] buffer = new byte[bufferSize];
				int len = 0;
				try {
					while ((len = instream.read(buffer)) != -1) {
						output.write(buffer, 0, len);
					}
					output.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			return filePath;
		}

		@Override
		protected void onPostExecute(String filePath) {
			super.onPostExecute(filePath);
			if (null != filePath) {

/*				Toast toast = Toast.makeText(getApplicationContext(),
						"Content is not NULL", Toast.LENGTH_SHORT);
				toast.show();
*/
				String[] fileName = filePath.split(Config.iamDownloadDirectory + "/");
				if (fileName.length == 1) {

					TextView txt = (TextView) findViewById(R.id.mmsmessage);
					txt.setText(fileName[0]);
				} else {
					listItems.add(fileName[1]);
					adapter.notifyDataSetChanged();
				}
			} else {
				Toast toast = Toast.makeText(getApplicationContext(),
						"InputStream is NULL", Toast.LENGTH_SHORT);
				toast.show();
			}
		}
	}

	public void writeLog(byte[] text, String fileName) {

		if (!Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState()))
			return;

		File sdCard = Environment.getExternalStorageDirectory();
		File logFile = new File(sdCard.getAbsolutePath() + File.separator
				+ fileName);

		// delete the old file
		if (logFile.exists()) {
			logFile.delete();
		}

		// create a new shiny file
		try {
			logFile.createNewFile();
		} catch (IOException e) {
			new AlertDialog.Builder(this).setMessage(
					"Unable to create log email file: " + e.getMessage())
					.show();
		}

		FileOutputStream outFileStream = null;
		try {
			outFileStream = new FileOutputStream(logFile);
		} catch (FileNotFoundException e) {
			return;
		}

		try {
			outFileStream.write(text);
			outFileStream.close();
		} catch (IOException e) {
			new AlertDialog.Builder(this).setMessage(
					"Unable to create log email file: " + e.getMessage())
					.show();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.mmscontent, menu);
		return true;
	}
}