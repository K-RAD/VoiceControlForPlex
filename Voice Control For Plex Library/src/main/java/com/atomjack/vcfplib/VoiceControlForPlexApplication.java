package com.atomjack.vcfplib;

import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import android.content.res.Resources.NotFoundException;

import com.atomjack.vcfplib.model.MediaContainer;
import com.atomjack.vcfplib.model.PlexServer;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

public class VoiceControlForPlexApplication
{
	public final static String PREF_FEEDBACK_VOICE = "pref.feedback.voice";

	private static ConcurrentHashMap<String, PlexServer> plexmediaServers = new ConcurrentHashMap<String, PlexServer>();
    
	public static ConcurrentHashMap<String, PlexServer> getPlexMediaServers() {
		return plexmediaServers;
	}
    
	private static Serializer serial = new Persister();

	public static Locale getVoiceLocale(String loc) {
		String[] voice = loc.split("-");

		Locale l = null;
		if(voice.length == 1)
			l = new Locale(voice[0]);
		else if(voice.length == 2)
			l = new Locale(voice[0], voice[1]);
		else if(voice.length == 3)
			l = new Locale(voice[0], voice[1], voice[2]);

		return l;
	}

	public static void addPlexServer(final PlexServer server) {
		Logger.d("ADDING PLEX SERVER: %s", server.name);
		if(server.name.equals("") || server.address.equals("")) {
			return;
		}
		if (!plexmediaServers.containsKey(server.name)) {
			try {
				String url = "http://" + server.address + ":" + server.port + "/library/sections/";
				AsyncHttpClient httpClient = new AsyncHttpClient();
				httpClient.get(url, new AsyncHttpResponseHandler() {
						@Override
						public void onSuccess(int statusCode, org.apache.http.Header[] headers, byte[] responseBody) {
//    		            Logger.d("HTTP REQUEST: %s", response);
								MediaContainer mc = new MediaContainer();
								try {
									mc = serial.read(MediaContainer.class, new String(responseBody, "UTF-8"));
								} catch (NotFoundException e) {
										e.printStackTrace();
								} catch (Exception e) {
										e.printStackTrace();
								}
								for(int i=0;i<mc.directories.size();i++) {
									if(mc.directories.get(i).type.equals("movie")) {
										server.addMovieSection(mc.directories.get(i).key);
									}
									if(mc.directories.get(i).type.equals("show")) {
										server.addTvSection(mc.directories.get(i).key);
									}
									if(mc.directories.get(i).type.equals("artist")) {
										server.addMusicSection(mc.directories.get(i).key);
									}
								}
								Logger.d("title1: %s", mc.title1);
								if(mc.directories != null)
									Logger.d("Directories: %d", mc.directories.size());
								else
									Logger.d("No directories found!");
								if(!server.name.equals("") && !server.address.equals("")) {
									plexmediaServers.putIfAbsent(server.name, server);
								}
						}
				});

			} catch (Exception e) {
				Logger.e("Exception getting clients: %s", e.toString());
			}
			Logger.d("Adding %s", server.name);
		} else {
			Logger.d("%s already added.", server.name);
		}
	}

	public static String normalizedVersion(String version) {
		return normalizedVersion(version, ".", 4);
	}

	public static String normalizedVersion(String version, String sep, int maxWidth) {
		String[] split = Pattern.compile(sep, Pattern.LITERAL).split(version);
		StringBuilder sb = new StringBuilder();
		for (String s : split) {
			sb.append(String.format("%" + maxWidth + 's', s));
		}
		return sb.toString();
	}

	public static boolean isVersionLessThan(String v1, String v2) {
		String s1 = VoiceControlForPlexApplication.normalizedVersion(v1);
		String s2 = VoiceControlForPlexApplication.normalizedVersion(v2);
		int cmp = s1.compareTo(s2);
		return cmp < 0;
	}

}