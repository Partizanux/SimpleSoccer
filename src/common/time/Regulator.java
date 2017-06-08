package common.time;

import static common.misc.Utils.*;

public class Regulator {

    //the time period between updates 
    private double m_dUpdatePeriod;
    //the next time the regulator allows code flow
    private long m_dwNextUpdateTime;

    public Regulator(double NumUpdatesPerSecondRqd) {
        m_dwNextUpdateTime = (long) (System.currentTimeMillis() + randFloat() * 1000);

        if (NumUpdatesPerSecondRqd > 0) {
            m_dUpdatePeriod = 1000.0 / NumUpdatesPerSecondRqd;
        } else if (isEqual(0.0, NumUpdatesPerSecondRqd)) {
            m_dUpdatePeriod = 0.0;
        } else if (NumUpdatesPerSecondRqd < 0) {
            m_dUpdatePeriod = -1;
        }
    }
    //the number of milliseconds the update period can vary per required
    //update-step. This is here to make sure any multiple clients of this class
    //have their updates spread evenly
    private static final double UpdatePeriodVariator = 10.0;

    /**
     * @return true if the current time exceeds m_dwNextUpdateTime
     */
    public boolean isReady() {
        //if a regulator is instantiated with a zero freq then it goes into
        //stealth mode (doesn't regulate)
        if (isEqual(0.0, m_dUpdatePeriod)) {
            return true;
        }

        //if the regulator is instantiated with a negative freq then it will
        //never allow the code to flow
        if (m_dUpdatePeriod < 0) {
            return false;
        }

        long CurrentTime = System.currentTimeMillis();

        if (CurrentTime >= m_dwNextUpdateTime) {
            m_dwNextUpdateTime = (long) (CurrentTime + m_dUpdatePeriod + randInRange(-UpdatePeriodVariator, UpdatePeriodVariator));
            return true;
        }

        return false;
    }
}
