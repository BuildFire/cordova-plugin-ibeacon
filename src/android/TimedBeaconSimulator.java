package com.ibeaconbg.www;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.util.Log;

import org.altbeacon.beacon.AltBeacon;
import org.altbeacon.beacon.Beacon;

public class TimedBeaconSimulator implements org.altbeacon.beacon.simulator.BeaconSimulator {
    protected static final String TAG = "TimedBeaconSimulator";
    private List<Beacon> beacons;

    /*
     * You may simulate detection of beacons by creating a class like this in your project.
     * This is especially useful for when you are testing in an Emulator or on a device without BluetoothLE capability.
     *
     * Uncomment the lines in BeaconReferenceApplication starting with:
     *     // If you wish to test beacon detection in the Android Emulator, you can use code like this:
     * Then set USE_SIMULATED_BEACONS = true to initialize the sample code in this class.
     * If using a Bluetooth incapable test device (i.e. Emulator), you will want to comment
     * out the verifyBluetooth() in MonitoringActivity.java as well.
     *
     * Any simulated beacons will automatically be ignored when building for production.
     */
    public boolean USE_SIMULATED_BEACONS = false;

    /**
     *  Creates empty beacons ArrayList.
     */
    public TimedBeaconSimulator(){
        beacons = new ArrayList<Beacon>();
    }

    /**
     * Required getter method that is called regularly by the Android Beacon Library.
     * Any beacons returned by this method will appear within your test environment immediately.
     */
    public List<Beacon> getBeacons(){
        return beacons;
    }

    private ScheduledExecutorService scheduleTaskExecutor;


    /**
     * Simulates a new beacon every 10 seconds until it runs out of new ones to add.
     */
    public void createTimedSimulatedBeacons(){
        if (USE_SIMULATED_BEACONS){
            beacons = new ArrayList<Beacon>();

            Beacon beacon1 = new AltBeacon.Builder().setId1("5603e134-e0d2-11ea-87d0-0242ac130003").setRssi(-55).setTxPower(-55).build();

            beacons.add(beacon1);



            final List<Beacon> finalBeacons = new ArrayList<Beacon>(beacons);

            //Clearing beacons list to prevent all beacons from appearing immediately.
            //These will be added back into the beacons list from finalBeacons later.
            beacons.clear();

            scheduleTaskExecutor= Executors.newScheduledThreadPool(5);

            // This schedules an beacon to appear every 10 seconds:
            scheduleTaskExecutor.scheduleAtFixedRate(new Runnable() {
                public void run() {
                    try {
                        //putting a single beacon back into the beacons list.
                        if (finalBeacons.size() > beacons.size()) {
                            Log.e(TAG, "running simulated beacon");
                            beacons.add(finalBeacons.get(beacons.size()));
                        }else{
                            Log.e(TAG, "not running simulated beacon");

                            scheduleTaskExecutor.shutdown();
                        }
                    }catch(Exception e){
                        Log.e(TAG, "failed running simulated beacon");
                        e.printStackTrace();
                    }
                }
            }, 30, 15, TimeUnit.SECONDS);
        }
    }

}