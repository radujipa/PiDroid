/*
  CameraController.java

  Copyright (C) 2015 Radu Traian Jipa
  License: http://www.gnu.org/licenses/gpl-2.0.txt GNU General Public License v2
*/


package radu.pidroid.Robot;

import radu.pidroid.Connector.Messenger;


public class CameraController {

    // the number by which we truncate the cameraX,Y values
    // e.g. cameraX = 0/20/40/60/80/100
    // TODO: the user should be able to change this in app
    private final static int CAMERA_VALUE_GRAIN = 20;


    // references to modules
    private Messenger messenger;

    // the camera motors position in percentage
    private int cameraX, cameraY;


    public CameraController(Messenger messenger) {
        this.messenger = messenger;

        this.cameraX = this.cameraY = 0;
    } // constructor


    public void updateCameraPosition(int x, int y) {
        // to avoid fined grained camera position updates, we'll truncate the
        // values we send to the camera motors to only a handful
        x -= x % CAMERA_VALUE_GRAIN;
        y -= y % CAMERA_VALUE_GRAIN;

        if (cameraX != x || cameraY != y) {
            cameraX = x; cameraY = y;
            messenger.updateCameraPosition(cameraX, cameraY);
        } // if
    } // updateCameraPosition

} // CameraController