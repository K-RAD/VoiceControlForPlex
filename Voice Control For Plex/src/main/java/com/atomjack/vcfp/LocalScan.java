package com.atomjack.vcfp;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;

import com.atomjack.vcfp.adapters.PlexListAdapter;
import com.atomjack.vcfp.model.PlexClient;
import com.atomjack.vcfp.model.PlexServer;

import java.util.concurrent.ConcurrentHashMap;

public class LocalScan {
	private Context context;
	private Class theClass;
	private Dialog searchDialog;
	private Dialog deviceSelectDialog = null;
	private ScanHandler scanHandler;

	public LocalScan(Context ctx, Class cls, ScanHandler handler) {
		context = ctx;
		theClass = cls;
		scanHandler = handler;
	}

	public void searchForPlexServers() {
		searchForPlexServers(false);
	}

	public void searchForPlexServers(boolean silent) {
		Logger.d("searchForPlexServers()");
		if(!VoiceControlForPlexApplication.isWifiConnected(context)) {
			VoiceControlForPlexApplication.showNoWifiDialog(context);
			return;
		}

		if(!silent) {
			searchDialog = new Dialog(context);

			searchDialog.setContentView(R.layout.search_popup);
			searchDialog.setTitle(context.getResources().getString(R.string.searching_for_plex_servers));

			searchDialog.show();
		}

		Intent mServiceIntent = new Intent(context, GDMService.class);
		mServiceIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		mServiceIntent.putExtra(VoiceControlForPlexApplication.Intent.EXTRA_SILENT, silent);
		mServiceIntent.putExtra(VoiceControlForPlexApplication.Intent.EXTRA_CLASS, theClass);
		mServiceIntent.putExtra(VoiceControlForPlexApplication.Intent.SCAN_TYPE, VoiceControlForPlexApplication.Intent.SCAN_TYPE_SERVER);
		context.startService(mServiceIntent);
	}

	public void showPlexServers() {
		showPlexServers(null);
	}

	public void showPlexServers(ConcurrentHashMap<String, PlexServer> servers) {
		if(searchDialog != null)
			searchDialog.dismiss();
		if(deviceSelectDialog == null) {
			deviceSelectDialog = new Dialog(context);
		}
		deviceSelectDialog.setContentView(R.layout.server_select);
		deviceSelectDialog.setTitle("Select a Plex Server");
		deviceSelectDialog.show();

		final ListView serverListView = (ListView) deviceSelectDialog.findViewById(R.id.serverListView);
		if(servers == null)
			servers = new ConcurrentHashMap<String, PlexServer>(VoiceControlForPlexApplication.servers);
		final PlexListAdapter adapter = new PlexListAdapter(context, PlexListAdapter.TYPE_SERVER);
		adapter.setServers(servers);
		serverListView.setAdapter(adapter);
		serverListView.setOnItemClickListener(new ListView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parentAdapter, View view, int position, long id) {
				Logger.d("Clicked position %d", position);
				PlexServer s = (PlexServer)parentAdapter.getItemAtPosition(position);
				deviceSelectDialog.dismiss();
				scanHandler.onDeviceSelected(s, false);
			}
		});
	}

  public void searchForPlexClients() {
    searchForPlexClients(false);
  }

  public void searchForPlexClients(boolean connectToClient) {
		Logger.d("searchForPlexClients()");
		if(!VoiceControlForPlexApplication.isWifiConnected(context)) {
			VoiceControlForPlexApplication.showNoWifiDialog(context);
			return;
		}

		searchDialog = new Dialog(context);

		searchDialog.setContentView(R.layout.search_popup);
		searchDialog.setTitle(context.getResources().getString(R.string.searching_for_plex_clients));

		searchDialog.show();

		Intent mServiceIntent = new Intent(context, GDMService.class);
		mServiceIntent.putExtra(GDMService.PORT, 32412); // Port for clients
		mServiceIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		mServiceIntent.putExtra(VoiceControlForPlexApplication.Intent.EXTRA_CLASS, theClass);
    mServiceIntent.putExtra(VoiceControlForPlexApplication.Intent.EXTRA_CONNECT_TO_CLIENT, connectToClient);
		mServiceIntent.putExtra(VoiceControlForPlexApplication.Intent.SCAN_TYPE, VoiceControlForPlexApplication.Intent.SCAN_TYPE_CLIENT);
		context.startService(mServiceIntent);
    VoiceControlForPlexApplication.hasDoneClientScan = true;
	}

	public void hideSearchDialog() {
		if(searchDialog != null)
			searchDialog.dismiss();
	}

	public void showPlexClients() {
		showPlexClients(false);
	}

	public void showPlexClients(boolean showResume) {
		showPlexClients(showResume, null);
	}

  public void deviceSelectDialogRefresh() {
    ListView serverListView = (ListView) deviceSelectDialog.findViewById(R.id.serverListView);
    PlexListAdapter adapter = (PlexListAdapter)serverListView.getAdapter();
    adapter.setClients(VoiceControlForPlexApplication.getAllClients());
    adapter.notifyDataSetChanged();
  }

  public boolean isDeviceDialogShowing() {
    return deviceSelectDialog != null && deviceSelectDialog.isShowing();
  }

	public void showPlexClients(boolean showResume, final ScanHandler onFinish) {
    if (searchDialog != null)
      searchDialog.dismiss();
    if (deviceSelectDialog == null) {
      deviceSelectDialog = new Dialog(context);
    }
    deviceSelectDialog.setContentView(R.layout.server_select);
    deviceSelectDialog.setTitle(R.string.select_plex_client);
    deviceSelectDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
      @Override
      public void onCancel(DialogInterface dialogInterface) {
        if (onFinish == null && scanHandler != null)
          scanHandler.onDeviceSelected(null, false);
        else if (onFinish != null)
          onFinish.onDeviceSelected(null, false);
      }
    });
    deviceSelectDialog.show();

    if (showResume) {
      CheckBox resumeCheckbox = (CheckBox) deviceSelectDialog.findViewById(R.id.serverListResume);
      resumeCheckbox.setVisibility(View.VISIBLE);
    }

    final ListView serverListView = (ListView) deviceSelectDialog.findViewById(R.id.serverListView);
    final PlexListAdapter adapter = new PlexListAdapter(context, PlexListAdapter.TYPE_CLIENT);
    adapter.setClients(VoiceControlForPlexApplication.getAllClients());
    serverListView.setAdapter(adapter);
    serverListView.setOnItemClickListener(new ListView.OnItemClickListener() {

      @Override
      public void onItemClick(AdapterView<?> parentAdapter, View view, int position,
                              long id) {
        PlexClient s = (PlexClient) parentAdapter.getItemAtPosition(position);
        deviceSelectDialog.dismiss();
        CheckBox resumeCheckbox = (CheckBox) deviceSelectDialog.findViewById(R.id.serverListResume);
        if (onFinish == null)
          scanHandler.onDeviceSelected(s, resumeCheckbox.isChecked());
        else
          onFinish.onDeviceSelected(s, resumeCheckbox.isChecked());
      }

    });
  }
}
