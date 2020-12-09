/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
 */

package com.ibeaconbg.www;

import android.app.NotificationManager;
import android.content.SharedPreferences;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.Region;
import org.apache.cordova.*;

import java.util.HashSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainCordovaActivity extends CordovaActivity
{
    private static MainCordovaActivity sInstance;

    private static String eventType;
    private static int state;
    private static Region parsedRegion;


    Context context;
    SharedPreferences sharedpreferences;

    private static ScheduledExecutorService scheduledTaskExecutor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        try {
            NotificationManager notificationManager =
                    (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        sInstance = this;

        // enable Cordova apps to be started in the background
        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.getBoolean("cdvStartInBackground", false)) {
            moveTaskToBack(true);
        }


        try {
            sharedpreferences = context.getSharedPreferences("iBeacon_hits", 0);

            String mUniqueId = sharedpreferences.getString("mUniqueId", "null");
            String id1 = sharedpreferences.getString("uuid", "null");
            String id2 = sharedpreferences.getString("id2", "null");
            String id3 = sharedpreferences.getString("id3", "null");

            eventType = sharedpreferences.getString("eventType", "null");
            state = sharedpreferences.getInt("state", -1);

            Log.e(TAG, "mUniqueId: " + mUniqueId);
            Log.e(TAG, "eventType: " + eventType);
            Log.e(TAG, "state: " + state);

            if (!mUniqueId.equals("null") && !eventType.equals("null") && state > -1) {
                Identifier uuid = !id1.equals("null") ? Identifier.parse(id1) : null;
                Identifier major = !id2.equals("null")? Identifier.parse(id2) : null;;
                Identifier minor = !id3.equals("null") ? Identifier.parse(id3) : null;;

                parsedRegion = new Region(mUniqueId, uuid, major, minor);
            }

            SharedPreferences.Editor editor = sharedpreferences.edit();

            editor.putString("eventType", null);
            editor.putString("mUniqueId", null);
            editor.putString("uuid", null);
            editor.putString("id2", null);
            editor.putString("id3", null);
            editor.putInt("state", -1);

            editor.commit();

        } catch (Exception e) {
            Log.e(TAG, "MainCordovaActivity::onCreate FAILED", e);
            e.printStackTrace();
        } finally {
            // Set by <content src="index.html" /> in config.xml
            loadUrl(launchUrl);
        }
    }

   @Override
   public void onDestroy() {
       super.onDestroy();
       sInstance = null;
   }

    public static MainCordovaActivity getInstance() {
        return MainCordovaActivity.sInstance;
    }

    public static void onInit() {
        ScheduledExecutorService scheduledTaskExecutor =
                Executors.newScheduledThreadPool(5);

        scheduledTaskExecutor.schedule(new Runnable() {
            public void run() {
                try {
                    Log.e(TAG, "MainCordovaActivity::onInit");
                    if (eventType != null && parsedRegion != null) {
                        Log.e(TAG, "MainCordovaActivity::onInit CALLING => didDetermineStateForRegion");
                        Main.getInstance().didDetermineStateForRegion(state, parsedRegion);
                    }

                } catch (Exception e) {
                    Log.e(TAG, "MainCordovaActivity::onInit FAILED", e);

                    e.printStackTrace();
                }
            }
        }, 2500, TimeUnit.MILLISECONDS);
    }
}
