package com.att.testaab;

import android.util.Log;
import android.widget.TextView;

import com.att.api.aab.listener.ATTIAMListener;
import com.att.api.error.InAppMessagingError;

public class UnitTestListener implements ATTIAMListener {
	protected String strTestName = "Unknown";
	protected TextView txtDisplay;
	protected String strLogFilePath = null;
    
	public UnitTestListener(String testName, TextView txtView, String logFilePath) {
        this.strTestName = testName;
        this.txtDisplay = txtView;
        this.strLogFilePath = logFilePath;
    }
	
	@Override
	public void onSuccess(Object response) {
		updateTextDisplay("Failed: Please override " + strTestName + " onSuccess method. Response=\n" + response.toString());
	}

	@Override
	public void onError(InAppMessagingError error) {
		String strText = "Failed in " + strTestName + ": " + error.getErrorMessage();
		updateTextDisplay(strText);
	}
	
	public void updateTextDisplay(String strText) {
		String strOldText = (String) txtDisplay.getText();
		txtDisplay.setText(strOldText + "\n" + strText);		
		Log.i("TestAabUnitTest", strText);	
	}

}
