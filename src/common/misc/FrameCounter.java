package common.misc;

public class FrameCounter {

    public static final FrameCounter TickCounter = new FrameCounter();
    private long m_lCount = 0;
    private int m_iFramesElapsed = 0;

    private FrameCounter() {
    }

    //copy ctor and assignment should be private
    private FrameCounter(final FrameCounter c) {
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    public FrameCounter Instance() {
        return TickCounter;
    }

    public void Update() {
        ++m_lCount;
        ++m_iFramesElapsed;
    }

    public long GetCurrentFrame() {
        return m_lCount;
    }

    public void Reset() {
        m_lCount = 0;
    }

    public void Start() {
        m_iFramesElapsed = 0;
    }

    public int FramesElapsedSinceStartCalled() {
        return m_iFramesElapsed;
    }
}