/*
  MjpegStabiliser.java

  Copyright (C) 2015 Radu Traian Jipa
  License: http://www.gnu.org/licenses/gpl-2.0.txt GNU General Public License v2
*/


package radu.pidroid.MjpegViewer;

import android.graphics.Bitmap;
import android.graphics.Matrix;


public class MjpegStabiliser {

    private final double maxTiltAngle = 30;
    private double tiltAngle;

    int screenWidth, screenHeight, streamHeight;
    int clippedStreamHeight, clippedStreamWidth;


    public MjpegStabiliser(int screenWidth, int screenHeight, int streamHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.streamHeight = streamHeight;

        computeClippedStreamSizes();
    } // constructor


    private void computeClippedStreamSizes() {
        double aspectRatio = (double)screenWidth / (double)screenHeight;

        double radius = (double)streamHeight / (2.0 * Math.cos(Math.toRadians(90.0 - maxTiltAngle)
                - Math.atan(1.0/aspectRatio)));

        clippedStreamHeight = (int)Math.ceil(2 * radius / Math.sqrt(Math.pow(aspectRatio, 2) + 1));
        clippedStreamWidth  = (int)Math.ceil(clippedStreamHeight * aspectRatio);
    } // computeClippedStreamSizes


    public void setTiltAngle(double angle) {
        if (angle <= maxTiltAngle && angle >= -maxTiltAngle)
            tiltAngle = angle;
    } // setStabiliserAngle


    public Bitmap getStabilisedBitmap(Bitmap bitmap) {
        Matrix stabiliserMatrix = new Matrix();

        //
        stabiliserMatrix.postRotate((float) tiltAngle, screenWidth / 2, screenHeight / 2);

        //
        Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                bitmap.getHeight(), stabiliserMatrix, false);

        return Bitmap.createBitmap(rotatedBitmap, rotatedBitmap.getWidth()/2 - clippedStreamWidth/2,
                rotatedBitmap.getHeight() / 2 - clippedStreamHeight / 2,
                clippedStreamWidth, clippedStreamHeight, new Matrix(), false);
    } // stabiliseBitmap

} // MjpegStabiliser
