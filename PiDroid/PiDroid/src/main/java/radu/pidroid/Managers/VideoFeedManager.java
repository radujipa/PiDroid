/*
  VideoFeedManager.java

  Copyright (C) 2015 Radu Traian Jipa
  License: http://www.gnu.org/licenses/gpl-2.0.txt GNU General Public License v2
*/


package radu.pidroid.Managers;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.net.URI;

import radu.pidroid.Activities.Controller;
import radu.pidroid.MjpegViewer.MjpegInputStream;
import radu.pidroid.MjpegViewer.MjpegView;
import radu.pidroid.R;


public class VideoFeedManager implements SensorsManager.TiltListener {

    // references to modules
    private SettingsManager settings;

    //
    public MjpegView videoFeedMjpegView;
    private VideoFeedTask videoFeedTask;

    //
    public boolean videoFeedON;


    public VideoFeedManager(Controller controller, SensorsManager sensors, SettingsManager settings) {
        this.settings = settings;

        videoFeedMjpegView = (MjpegView) controller.findViewById(R.id.videoFeedSurfaceView);
        sensors.setTiltListener(this);

        this.videoFeedTask = null;
        this.videoFeedON = false;
    } // constructor


    public boolean start() {
        String URL = "http://" + settings.serverIP + ":" + (settings.serverPort + 1) + "/?action=stream";

        try {
            videoFeedTask = new VideoFeedTask();
            videoFeedTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, URL);
        } catch (Exception exception) {
            Log.e("setupVideoFeed():", "Could not connect to host!");
            return false;
        } // try-catch

        return true;
    } // start


    public void stop() {
        // TODO: videoFeedTask.cancel(false); ?
        videoFeedMjpegView.stopPlayback();
    } // stop


    public void setCameraStabilisation(boolean cameraStabilisation) {
        videoFeedMjpegView.setCameraStabilisation(cameraStabilisation);
    } // setCameraStabilisation


    @Override
    public void onTilt(int tiltAngle) {
        if (settings.cameraStabilisationON)
            videoFeedMjpegView.setTiltAngle(tiltAngle - 90);
    } // onTilt


    public class VideoFeedTask extends AsyncTask<String, Void, MjpegInputStream> {

        @Override
        protected MjpegInputStream doInBackground(String... url) {
            HttpResponse res;
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpParams httpParams = httpClient.getParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, 5 * 1000);

            Log.d("VideoFeedTask:", "1. Sending http request");
            try {
                res = httpClient.execute(new HttpGet(URI.create(url[0])));
                Log.d("VideoFeedTask:", "2. Request finished, status = " + res.getStatusLine().getStatusCode());
                if(res.getStatusLine().getStatusCode()==401){
                    // You must turn off camera User Access Control before this will work
                    return null;
                }
                return new MjpegInputStream(res.getEntity().getContent());
            } catch (Exception e) {
                //e.printStackTrace();
                Log.e("VideoFeedTask:", "Caught exception: ", e.getCause());
            } // try-catch
            return null;
        }

        @Override
        protected void onPostExecute(MjpegInputStream result) {
            videoFeedMjpegView.setSource(result);

            if(result!=null) {
                result.setSkip(1);
                //videoFeedOn = true;
            } // if
            //else
            //videoFeedOn = false;

            videoFeedMjpegView.showFps(false);
        } // onPostExecute
    } // VideoFeedTask

} // VideoFeedManager
