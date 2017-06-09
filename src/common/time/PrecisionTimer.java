package common.time;

/**
 *  Desc: Windows timer class.
 */
public class PrecisionTimer {
    public static final double SMOOTHNESS = 5.0;
    private Long currentTime,
            lastTime,
            lastTimeInTimeElapsed,
            nextTime,
            startTime,
            frameTime,
            perfCountFreq;
    private double timeElapsed,
            lastTimeElapsed,
            timeScale;
    private double normalFPS;
    private double slowFPS;
    private boolean started;
    //if true a call to timeElapsed() will return 0 if the current
    //time elapsed is much smaller than the previous. Used to counter
    //the problems associated with the user using menus/resizing/moving 
    //a window etc
    private boolean smoothUpdates;

    public PrecisionTimer() {
        normalFPS = 0.0;
        slowFPS = 1.0;
        timeElapsed = 0.0;
        frameTime = 0L;
        lastTime = 0L;
        lastTimeInTimeElapsed = 0L;
        perfCountFreq = 0L;
        started = false;
        startTime = 0L;
        lastTimeElapsed = 0.0;
        smoothUpdates = false;

        //how many ticks per sec do we get
        //QueryPerformanceFrequency((LARGE_INTEGER *) & perfCountFreq);
        //using System.nanoSecond() it is obviously 1 000 000 000 nano second per second
        perfCountFreq = 1000000000L;

        timeScale = 1.0 / perfCountFreq;
    }

    /**
    /* use to specify FPS
     */
    public PrecisionTimer(double fps) {
        normalFPS = fps;
        slowFPS = 1.0;
        timeElapsed = 0.0;
        frameTime = 0L;
        lastTime = 0L;
        lastTimeInTimeElapsed = 0L;
        perfCountFreq = 0L;
        started = false;
        startTime = 0L;
        lastTimeElapsed = 0.0;
        smoothUpdates = false;

        //how many ticks per sec do we get
        //QueryPerformanceFrequency((LARGE_INTEGER *) & perfCountFreq);
        //using System.nanoSecond() it is obviously 1 000 000 000 nano second per second
        perfCountFreq = 1000000000L;//nanosec in sec

        timeScale = 1.0 / perfCountFreq;

        //calculate ticks per frame
        frameTime = (long) (perfCountFreq / normalFPS);
    }

    //only use this after a call to the above.
    //public double getTimeElapsed(){return timeElapsed;}
    public double currentTime() {
        //QueryPerformanceCounter((LARGE_INTEGER *) & currentTime);
        currentTime = System.nanoTime();

        return (currentTime - startTime) * timeScale;
    }

    public boolean isStarted() {
        return started;
    }

    public void smoothUpdatesOn() {
        smoothUpdates = true;
    }

    public void smoothUpdatesOff() {
        smoothUpdates = false;
    }

    /**
     * starts the timer
     */
    public void start() {
        started = true;

        timeElapsed = 0.0;

        //QueryPerformanceCounter((LARGE_INTEGER *) & lastTime);
        lastTime = System.nanoTime();

        //TODO move to separate method tick ?
        //keep a record of when the timer was started
        startTime = lastTimeInTimeElapsed = lastTime;

        //update time to render next frame
        nextTime = lastTime + frameTime;
    }

    /**
     * Determines if enough time has passed to move onto next frame
     * @return true if it is time to move on to the next frame step. To be used if
     * FPS is set
     */
    public boolean readyForNextFrame() {
        assert normalFPS != 0 : "PrecisionTimer::readyForNextFrame<No FPS set in timer>";

        //QueryPerformanceCounter((LARGE_INTEGER *) & currentTime);
        currentTime = System.nanoTime();

        if (currentTime > nextTime) {

            timeElapsed = (currentTime - lastTime) * timeScale;

            //TODO move to separate method tick ?
            lastTime = currentTime;

            //update time to render next frame
            nextTime = currentTime + frameTime;

            return true;
        }

        return false;
    }

    /**
     * @return time elapsed since last call to this function
     */
    public double timeElapsed() {
        lastTimeElapsed = timeElapsed;

        //QueryPerformanceCounter((LARGE_INTEGER *) & currentTime);
        currentTime = System.nanoTime();

        timeElapsed = (currentTime - lastTimeInTimeElapsed) * timeScale;

        lastTimeInTimeElapsed = currentTime;

        if (smoothUpdates) {
            if (timeElapsed < (lastTimeElapsed * SMOOTHNESS)) {
                return timeElapsed;
            } else {
                return 0.0;
            }
        } else {
            return timeElapsed;
        }
    }
}