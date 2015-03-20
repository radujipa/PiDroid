/*
  JoystickView.java

  Copyright (C) 2015 Radu Traian Jipa
  License: http://www.gnu.org/licenses/gpl-2.0.txt GNU General Public License v2
*/


package radu.pidroid.Joystick;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import radu.pidroid.R;


public class JoystickView extends View {

    // The bitmaps which will contain the images "joystick_background" and "joystick_thumb" in /res
    private Bitmap joystickBackground;
    private Bitmap joystickThumb;

    // The position of the joystick in view coordinates.
    private double positionX;
    private double positionY;

    private Context context;
    private String tag;

    private MoveListener moveListener;
    private int activePointerID = MotionEvent.INVALID_POINTER_ID;


    public JoystickView(Context context) {
        super(context);
        this.context = context;
    } // constructor


    public JoystickView(Context context, AttributeSet attributes) {
        super(context, attributes);
        this.context = context;
    } // constructor


    private void getJoystickResources() {
        Resources resources = context.getResources();

        joystickThumb = BitmapFactory.decodeResource(resources, R.drawable.joystick_thumb);
        joystickThumb = Bitmap.createScaledBitmap(joystickThumb, (int)(getWidth() * 0.79), (int)(getHeight() * 0.79), false);

        joystickBackground = BitmapFactory.decodeResource(resources, R.drawable.joystick_background);
        joystickBackground = Bitmap.createScaledBitmap(joystickBackground,
                (int)(getWidth() * 0.8), (int)(getHeight() * 0.8), false);

        positionX = getCentreX();
        positionY = getCentreY();
    } // getJoystickResources


    public void setTag(String tag) {
        this.tag = tag;
    }


    //**********************************************************************************************
    //                                  ACCESSOR METHODS
    //**********************************************************************************************


    public double getJoystickX() {
        return positionX;
    } // getX


    public double getJoystickY() {
        return positionY;
    } // getY


    public double getCentreX() {
        return getWidth() / 2.0;
    } // getCentreX


    public double getCentreY() {
        return getHeight() / 2.0;
    } // getCentreY


    //**********************************************************************************************
    //                                  DRAWING FUNCTIONS
    //**********************************************************************************************


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Measure the dimensions and resize to make a square.
        int dimension = Math.min(measure(widthMeasureSpec), measure(heightMeasureSpec));
        setMeasuredDimension(dimension, dimension);
    } // onMeasure


    private int measure(int measureSpec) {
        // Get the measurement specifications.
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        // If no bounds are specified, return a default value. Otherwise, return the size.
        if (specMode == MeasureSpec.UNSPECIFIED)
            return 100;
        else
            return specSize;
    } // measure


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        // Load images and set initial X,Y.
        getJoystickResources();
    } // onLayout


    @Override
    protected void onDraw(Canvas canvas) {

        // Draw the joystick background centred in the view.
        canvas.drawBitmap(joystickBackground, (int)((getWidth() - joystickBackground.getWidth()) / 2.0),
                (int)((getHeight() - joystickBackground.getHeight()) / 2.0), null);

        // Draw the joystick thumb centred on the X,Y positions of the joystick.
        canvas.drawBitmap(joystickThumb, (int)(positionX - joystickThumb.getWidth() / 2.0),
                (int)(positionY - joystickThumb.getHeight() / 2.0), null);
    } // onDraw


    //**********************************************************************************************
    //                                   TOUCH AND DRAG FUNCTIONS
    //**********************************************************************************************


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                positionX = (int)getCentreX();
                positionY = (int)getCentreY();
                invalidate();

                moveListener.onRelease(this);
                activePointerID = MotionEvent.INVALID_POINTER_ID;
                break;

            case MotionEvent.ACTION_POINTER_UP:
                if (activePointerID == event.getPointerId(event.getActionIndex()))
                    activePointerID = MotionEvent.INVALID_POINTER_ID;
                break;

            case MotionEvent.ACTION_DOWN:
                if (activePointerID == MotionEvent.INVALID_POINTER_ID)
                    activePointerID = event.getPointerId(0);
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                if (activePointerID == MotionEvent.INVALID_POINTER_ID)
                    activePointerID = event.getPointerId(event.getActionIndex());
                break;

            case MotionEvent.ACTION_MOVE:
                if (activePointerID == event.getPointerId(event.getActionIndex())) {
                    // Process the move event.
                    onActionMoveEvent(event);

                    // Tell the View to call onDraw() and redraw the Joystick.
                    invalidate();
                } // if
                break;
        } // switch

        // The event has been handled.
        return true;
    } // onTouchEvent


    private void onActionMoveEvent(MotionEvent event) {
        int activePointerIndex = event.findPointerIndex(activePointerID);

        double touchDistanceFromCentreX = event.getX(activePointerIndex) - getCentreX();
        double touchDistanceFromCentreY = event.getY(activePointerIndex) - getCentreY();

        double touchDistanceFromCentre = Math.sqrt(Math.pow(touchDistanceFromCentreX, 2)
                + Math.pow(touchDistanceFromCentreY, 2));

        double maxDistanceFromCentre = getWidth() - joystickThumb.getWidth();

        if (touchDistanceFromCentre > maxDistanceFromCentre) {
            positionX = touchDistanceFromCentreX * maxDistanceFromCentre
                      / touchDistanceFromCentre + getCentreX();
            positionY = touchDistanceFromCentreY * maxDistanceFromCentre
                      / touchDistanceFromCentre + getCentreY();
        } // if
        else {
            positionX = event.getX(activePointerIndex);
            positionY = event.getY(activePointerIndex);
        } // else

        // Execute the implementation of onMove() on MoveListener.
        double strengthX = (positionX - getCentreX()) / maxDistanceFromCentre;
        double strengthY = (positionY - getCentreY()) / maxDistanceFromCentre;

        moveListener.onMove(this, strengthX, strengthY);
    } // onActionMoveEvent


    //**********************************************************************************************
    //                                  JOYSTICK LISTENER(S)
    //**********************************************************************************************


    public interface MoveListener {
        public void onMove(JoystickView view, double x, double y);
        public void onRelease(JoystickView view);
    } // MoveListener


    public void setMoveListener(MoveListener listener) {
        this.moveListener = listener;
    } // setMoveListener

} // JoystickView
