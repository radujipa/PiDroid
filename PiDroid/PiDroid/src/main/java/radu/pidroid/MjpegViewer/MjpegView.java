/*
  Code taken and adapted from:
  SimpleMjpegView: https://bitbucket.org/neuralassembly/simplemjpegview/src/6f7977956109475ad9d932db3a70fd0e52d20fb6/src/com/camera/simplemjpeg/MjpegView.java?at=master

  Documentation:
  Canvas and Drawables: http://developer.android.com/guide/topics/graphics/2d-graphics.html
*/


package radu.pidroid.MjpegViewer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;


public class MjpegView extends SurfaceView implements SurfaceHolder.Callback {

    SurfaceHolder holder;
    Context UIContext;

    private MjpegStabiliser stabiliser;
    private MjpegViewThread thread;
    private MjpegInputStream mIn = null;
    private boolean showFps = true;
    private boolean mRun = false;
    private boolean surfaceDone = false;

    private Paint overlayPaint;
    private int overlayTextColor;
    private int overlayBackgroundColor;
    private int ovlPos;
    private int dispWidth;
    private int dispHeight;
    private int displayMode;

    private boolean suspending = false;
    private boolean cameraStabilisationOn = true;



    public MjpegView(Context context) {
        super(context);
        init(context);
    }

    public MjpegView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }


    private void init(Context context) {
        holder = getHolder();
        UIContext = context;
        holder.addCallback(this);
        thread = new MjpegViewThread(holder, context);
        setFocusable(true);
        overlayPaint = new Paint();
        overlayPaint.setTextAlign(Paint.Align.LEFT);
        overlayPaint.setTextSize(12);
        overlayPaint.setTypeface(Typeface.DEFAULT);
        overlayTextColor = Color.WHITE;
        overlayBackgroundColor = Color.BLACK;
        //ovlPos = MjpegView.POSITION_LOWER_RIGHT;
        dispWidth = getWidth();
        dispHeight = getHeight();
    }


    public void startPlayback() {
        if(mIn != null) {
            mRun = true;
            if(thread==null){
                thread = new MjpegViewThread(holder, UIContext);
            }
            thread.start();
        }
    }


    public void resumePlayback() {
        if(suspending && mIn != null) {
            mRun = true;
            SurfaceHolder holder = getHolder();
            holder.addCallback(this);
            thread = new MjpegViewThread(holder, UIContext);
            thread.start();
            suspending=false;
        } // if
    } // resumePlayback


    public void stopPlayback() {
        if(mRun){
            suspending = true;
        }
        mRun = false;
        if(thread != null) {
            boolean retry = true;
            while(retry) {
                try {
                    thread.join();
                    retry = false;
                } catch (InterruptedException e) {}
            }
            thread = null;
        }
        if(mIn!=null){
            try{
                mIn.close();
            }catch(IOException e){}
            mIn = null;
        }
    } // stopPlayback


    public void setTiltAngle(double angle) {
        if (stabiliser != null) stabiliser.setTiltAngle(angle);
    } // setTiltAngle


    public void setCameraStabilisation(boolean cameraStabilisation) {
        cameraStabilisationOn = cameraStabilisation;
    } // setCameraStabilisation


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        surfaceDone = true;
    } // surfaceCreated


    @Override
    public void surfaceChanged(SurfaceHolder holder, int f, int w, int h) {
        if (thread != null) thread.setSurfaceSize(w, h);
    } // surfaceChanged


    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        surfaceDone = false;
        stopPlayback();
    } // surfaceDestroyed


    public void showFps(boolean b) {
        showFps = b;
    } // showFps


    public void setSource(MjpegInputStream source) {
        mIn = source;
        if(!suspending) startPlayback();
        else            resumePlayback();
    } // setSource


    //**********************************************************************************************
    //                                     MJPEG THREAD
    //**********************************************************************************************


    public class MjpegViewThread extends Thread {

        private SurfaceHolder mSurfaceHolder;
        private int frameCounter = 0;
        private long start;
        private String fps = "";


        public MjpegViewThread(SurfaceHolder surfaceHolder, Context context) {
            mSurfaceHolder = surfaceHolder;
        } // MjpegViewThread


        public void setSurfaceSize(int width, int height) {
            synchronized (mSurfaceHolder) {
                dispWidth = width;
                dispHeight = height;
            } // synchronized
        } // setSurfaceSize


        private Bitmap makeFpsOverlay(Paint p) {
            Rect b = new Rect();
            p.getTextBounds(fps, 0, fps.length(), b);

            // false indentation to fix forum layout
            Bitmap bm = Bitmap.createBitmap(b.width(), b.height(), Bitmap.Config.ARGB_8888);

            Canvas c = new Canvas(bm);
            p.setColor(overlayBackgroundColor);
            c.drawRect(0, 0, b.width(), b.height(), p);
            p.setColor(overlayTextColor);
            c.drawText(fps, -b.left, b.bottom-b.top-p.descent(), p);

            return bm;
        } // makeFpsOverlay


        @Override
        public void run() {
            start = System.currentTimeMillis();
            PorterDuffXfermode mode = new PorterDuffXfermode(PorterDuff.Mode.DST_OVER);

            int width, height;
            Paint p = new Paint();
            Bitmap videoImage, ovl = null;

            while (mRun) {

                Rect destRect;
                Canvas canvas = null;

                if(surfaceDone) {
                    try {
                        videoImage = mIn.readMjpegFrame();

                        if (stabiliser == null)
                            stabiliser = new MjpegStabiliser(getWidth(), getHeight(), videoImage.getHeight());

                        if (cameraStabilisationOn)
                            videoImage = stabiliser.getStabilisedBitmap(videoImage);

                        destRect = new Rect(0, 0, getWidth(), getHeight());
                        canvas = mSurfaceHolder.lockCanvas();
                        synchronized (mSurfaceHolder) {

                            canvas.drawBitmap(videoImage, null, destRect, p);

                            if(showFps) {
                                p.setXfermode(mode);
                                if(ovl != null) {
                                    // false indentation to fix forum layout
                                    height = ((ovlPos & 1) == 1) ? destRect.top : destRect.bottom - ovl.getHeight();
                                    width  = ((ovlPos & 8) == 8) ? destRect.left : destRect.right - ovl.getWidth();

                                    canvas.drawBitmap(ovl, width, height, null);
                                }
                                p.setXfermode(null);
                                frameCounter++;
                                if((System.currentTimeMillis() - start) >= 1000) {
                                    fps = String.valueOf(frameCounter)+"fps";
                                    frameCounter = 0;
                                    start = System.currentTimeMillis();
                                    if(ovl!=null) ovl.recycle();

                                    ovl = makeFpsOverlay(overlayPaint);
                                }
                            }
                        }
                    }catch (IOException e){
                        Log.e("MjpegViewThread", "run(): something bad happened");
                    }finally {
                        if (canvas != null) mSurfaceHolder.unlockCanvasAndPost(canvas);
                    } // try-catch-finally
                }
            }
        } // run()
    } // MjpegViewThread

} // MjpegView
