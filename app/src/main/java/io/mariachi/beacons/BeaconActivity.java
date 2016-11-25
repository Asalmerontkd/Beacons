package io.mariachi.beacons;

import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.Collection;

public class BeaconActivity extends AppCompatActivity implements BeaconConsumer {
    TextView estado;
    TextView distancia;
    private BeaconManager beaconManager;
    public static final String TAG = "BeaconsEverywhere";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beacon);
        estado = (TextView) findViewById(R.id.txtEstado);
        distancia = (TextView) findViewById(R.id.txtBeacon);

        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser()
                .setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));
        beaconManager.bind(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
    }

    //Identifier.parse("23a01af0-232a-4518-9c0e-323fb773f5ef")

    @Override
    public void onBeaconServiceConnect() {
        final Region region = new Region("myBeaons", null, null, null);

        beaconManager.setMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {
                try {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            estado.setText("Beacon dentro de la región.");
                        }
                    });
                    Log.d(TAG, "Beacon dentro de la región.");
                    beaconManager.startRangingBeaconsInRegion(region);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void didExitRegion(Region region) {
                try {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            estado.setText("Beacon Fuera de la región.");
                            distancia.setText("");
                        }
                    });
                    Log.d(TAG, "Beacon Fuera de la región.");
                    beaconManager.stopRangingBeaconsInRegion(region);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void didDetermineStateForRegion(int i, Region region) {

            }
        });

        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(final Collection<Beacon> collection, Region region) {
                for(final Beacon oneBeacon : collection) {

                    final double precision;
                    double ratio = oneBeacon.getRssi()*1.0/oneBeacon.getTxPower();
                    if (ratio < 1.0)
                    {
                        precision = Math.pow(ratio, 10);
                    }
                    else
                    {
                        precision = (0.89976)*Math.pow(ratio,7.7095) + 0.111;
                    }


                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            distancia.setText("Distancia: " + oneBeacon.getDistance() +"\nPrecisión: "+ precision +"\n id:" + oneBeacon.getId1() + " / " + oneBeacon.getId2() + " / " + oneBeacon.getId3());
                        }
                    });

                    Log.d(TAG, "distance: " + oneBeacon.getDistance() + " id:" + oneBeacon.getId1() + "/" + oneBeacon.getId2() + "/" + oneBeacon.getId3());
                }
            }
        });

        try {
            beaconManager.startMonitoringBeaconsInRegion(region);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
