/*
  MotorsController.java

  Copyright (C) 2015 Radu Traian Jipa
  License: http://www.gnu.org/licenses/gpl-2.0.txt GNU General Public License v2
*/


package radu.pidroid.Robot;

import radu.pidroid.Connector.Messenger;


public class MotorsController {

    // the number by which we truncate the motorsPower values
    // e.g. motorsPower = 0/20/40/60/80/100
    // TODO: the user should be able to change this in app
    public final static int MOTORS_POWER_GRAIN = 20;
    public final static int MOTORS_ANGLE_GRAIN = 10;


    public enum Direction {
        FORWARDS, BACKWARDS
    } // Direction

    public enum State {
        ACCELERATING, DECELERATING, STOPPED
    } // State


    // references to modules
    private Messenger messenger;

    //
    private int motorsPower;
    private int turnAngle;
    private Direction motorsDirection;
    private State motorsState;


    public MotorsController(Messenger messenger) {
        this.messenger = messenger;

        // initialising motors state
        this.motorsPower = 0;
        this.motorsDirection = Direction.FORWARDS;
        this.motorsState = State.STOPPED;
    } // constructor


    public void updateMotorsDirection(Direction direction) {
        this.motorsDirection = direction;
    } // updateMotorsDirection


    public void updateMotorsState(State state) {
        this.motorsState = state;
    } // updateMotorsState


    public void updateMotorsPower(int power) {
        // if we're going backwards, we'll consider the motors power as being negative
        power = (motorsDirection == Direction.BACKWARDS) ? -power : power;

        // to avoid fined grained motor speed updates, we'll truncate the
        // values we send to the motors to only a handful
        power -= power % MOTORS_POWER_GRAIN;

        // if we have a new power value, we'll update the motorsPower on the robot
        if (power != motorsPower) {
            motorsPower = power;
            messenger.updateRoverSpeed(motorsPower);
        } // if
    } // updateMotorsPower


    public void updateMotorsTurnAngle(int angle) {
        // TODO: refactor this & comment code!
        angle = angle > 90 ? angle / 10 * 10 : (angle + 10) / 10 * 10;
        angle = angle % 20 == 0 ? angle - 10 : angle;

        // if we have a new turn angle, notify the robot such that it performs a turn
        if (angle != turnAngle) {
            turnAngle = angle;
            messenger.updateTurnAngle(angle);
        }
    } // updateMotorsTurnAngle

} // MotorsController
