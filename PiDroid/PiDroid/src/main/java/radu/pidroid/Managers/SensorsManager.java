/*
  SensorsManager.java

  Copyright (C) 2015 Radu Traian Jipa
  License: http://www.gnu.org/licenses/gpl-2.0.txt GNU General Public License v2
*/


package radu.pidroid.Managers;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import radu.pidroid.Activities.Controller;


public class SensorsManager implements SensorEventListener {

    // custom listeners
    private TiltListener tiltListener;


    // Android SDK sensor managers
    private SensorManager mSensorManager;
    private Sensor mAccelerometer, mGeomagnetic;

    // raw sensor data from gyroscope
    private float [] accelerometerData = new float[3];
    private float [] geomagneticData = new float[3];

    //
    public int tiltAngle;
    private boolean sensorsON;


    public SensorsManager(Controller controller) {
        mSensorManager = (SensorManager) controller.getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGeomagnetic   = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        this.accelerometerData = new float[3];
        this.geomagneticData = new float[3];
        this.tiltAngle = 0;
        this.sensorsON = false;
    } // constructor


    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {

            case Sensor.TYPE_ACCELEROMETER:
                accelerometerData = event.values;
                break;

            case Sensor.TYPE_MAGNETIC_FIELD:
                geomagneticData = event.values;
                computeTiltAngle();
                break;

            default:
                Log.e("SensorsManager:", "onSensorChanged(): fell through default case!");
        } // switch
    } // onSensorChanged


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    } // onAccuracyChanged


    private void computeTiltAngle() {
        float [] mRotationMatrix = new float[9];
        float [] mOrientationMatrix = new float[3];

        //
        if (!SensorManager.getRotationMatrix(mRotationMatrix, null, accelerometerData, geomagneticData)) return;
        else SensorManager.getOrientation(mRotationMatrix, mOrientationMatrix);

        //
        int newTiltAngle = (int)Math.round(Math.toDegrees((double)mOrientationMatrix[1]) + 90);

        // update the tiltAngle with the new integer value and call the listener
        if (tiltAngle != newTiltAngle) {
            tiltAngle = newTiltAngle;
            tiltListener.onTilt(tiltAngle);
            Log.d("SensorsManager:", "computeTiltAngle(): tiltAngle = " + tiltAngle);
        }
    } // computeTiltAngle


    public void start() {
        if (!sensorsON) {
            mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
            mSensorManager.registerListener(this, mGeomagnetic, SensorManager.SENSOR_DELAY_FASTEST);
            sensorsON = true;
            Log.d("SensorsManager:", "start(): listeners registered");
        }
    } // registerSensors


    public void stop() {
        if (sensorsON) {
            mSensorManager.unregisterListener(this);
            sensorsON = false;
            Log.d("SensorsManager:", "stop(): listeners unregistered");
        }
    } // unregisterSensors


    public interface TiltListener {
        void onTilt(int tiltAngle);
    } // TiltListener


    public void setTiltListener(TiltListener listener) {
        this.tiltListener = listener;
    } // setTiltListener

} // SensorsManager
