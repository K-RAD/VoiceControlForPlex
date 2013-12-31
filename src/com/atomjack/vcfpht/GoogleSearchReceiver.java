package com.atomjack.vcfpht;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class GoogleSearchReceiver extends BroadcastReceiver {

	private static final String TAG = MainActivity.TAG;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		String queryText = intent.getStringExtra(GoogleSearchApi.KEY_QUERY_TEXT).toLowerCase();
		
		if(queryText.startsWith("watch") || queryText.startsWith("resume watching")) {
			Intent i = new Intent(context, PlayMediaActivity.class);
			i.putExtra("ORIGIN", "GoogleSearchReceiver");
			i.putExtra("queryText", queryText);
			i.addFlags(Intent.FLAG_FROM_BACKGROUND);
			i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(i);
		}
	}
}