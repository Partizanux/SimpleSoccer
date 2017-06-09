package common.time;

/**
 *  Desc: Windows timer class.
 */
public class PrecisionTimer {
    private Long currentTime,
            lastTime,
            nextTime,
            startTime,
            frameTime,
            scale;
    private double timeScale;
    private double normalFPS;
    private boolean started;

    public PrecisionTimer() {
        normalFPS = 0.0;
        frameTime = 0L;
        lastTime = 0L;
        scale = 0L;
        started = false;
        startTime = 0L;

        //how many ticks per sec do we get
        //QueryPerformanceFrequency((LARGE_INTEGER *) & scale);
        //using System.nanoSecond() it is obviously 1 000 000 000 nano second per second
        scale = 1000000000L;

        timeScale = 1.0 / scale;
    }

    /**
    /* use to specify FPS
     */
    public PrecisionTimer(double fps) {
        normalFPS = fps;
        frameTime = 0L;
        lastTime = 0L;
        scale = 0L;
        started = false;
        startTime = 0L;

        //nanosec in sec
        scale = 1000000000L;

        timeScale = 1.0 / scale;

        //calculate ticks per frame
        frameTime = (long) (scale / normalFPS);
    }

    public double currentTime() {
        //QueryPerformanceCounter((LARGE_INTEGER *) & currentTime);
        currentTime = System.nanoTime();

        return (currentTime - startTime) * timeScale;
    }

    public boolean isStarted() {
        return started;
    }

    /**
     * starts the timer
     */
    public void start() {
        started = true;

        //QueryPerformanceCounter((LARGE_INTEGER *) & lastTime);
        lastTime = System.nanoTime();
        startTime = lastTime;

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
            lastTime = currentTime;

            //update time to render next frame
            nextTime = currentTime + frameTime;
            return true;
        }

        return false;
    }
}