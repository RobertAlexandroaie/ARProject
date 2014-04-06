package com.fii.arproject;

import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

import com.metaio.sdk.MetaioDebug;
import com.metaio.tools.io.AssetsManager;

public class MainActivity extends Activity {

    AssetsExtracter mTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_main);

	MetaioDebug.enableLogging(true);

	// extract all the assets
	mTask = new AssetsExtracter();
	mTask.execute(0);

	MetaioDebug.log(Log.ERROR, "This is a log");

	// starts the ARActivity class
	Intent intent = new Intent(getApplicationContext(), ARActivity.class);
	intent.putExtra(getPackageName()+".AREL_SCENE",  AssetsManager.getAssetPath("arelConfig.xml"));
	startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
	// Inflate the menu; this adds items to the action bar if it is present.
	getMenuInflater().inflate(R.menu.main, menu);
	return true;
    }

    /**
     * AssetsExtractor is a AsyncTask that extracts all the assets in a
     * background thread. It does not block the GUI.
     */
    private class AssetsExtracter extends AsyncTask<Integer, Integer, Boolean> {
	@Override
	protected Boolean doInBackground(Integer... params) {
	    try {
		AssetsManager.extractAllAssets(getApplicationContext(), true);
	    } catch (IOException e) {
		MetaioDebug.printStackTrace(Log.ERROR, e);
		return false;
	    }
	    return true;
	}
    }
}
