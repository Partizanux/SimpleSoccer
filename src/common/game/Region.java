package common.game;

import common.misc.Cgdi;
import static common.misc.Cgdi.gdi;
import common.D2.Vector2D;
import static common.misc.Utils.randInRange;
import static common.misc.Stream_Utility_function.ttos;
import static java.lang.Math.*;

/**
 *  Desc:   Defines a rectangular region. A region has an identifying
 *          number, and four corners.
 *
 */
public class Region {

    public static final region_modifier halfsize = region_modifier.halfsize;
    public static final region_modifier normal = region_modifier.normal;
    
    public enum region_modifier {
        halfsize, normal
    };
    protected double m_dTop;
    protected double m_dLeft;
    protected double m_dRight;
    protected double m_dBottom;
    protected double m_dWidth;
    protected double m_dHeight;
    protected Vector2D m_vCenter;
    protected int m_iID;

    public Region() {
        this(0, 0, 0, 0, -1);
    }

    public Region(double left,
            double top,
            double right,
            double bottom) {
        this(left, top, right, bottom, -1);
    }

    public Region(double left,
            double top,
            double right,
            double bottom,
            int id) {
        m_dTop = top;
        m_dRight = right;
        m_dLeft = left;
        m_dBottom = bottom;
        m_iID = id;

        //calculate center of region
        m_vCenter = new Vector2D((left + right) * 0.5, (top + bottom) * 0.5);

        m_dWidth = abs(right - left);
        m_dHeight = abs(bottom - top);
    }

    public void Render() {
        Render(false);
    }

    public void Render(boolean ShowID) {
        gdi.HollowBrush();
        gdi.GreenPen();
        gdi.Rect(m_dLeft, m_dTop, m_dRight, m_dBottom);

        if (ShowID) {
            gdi.TextColor(Cgdi.green);
            gdi.TextAtPos(Center(), ttos(ID()));
        }
    }

    /**
     * returns true if the given position lays inside the region. The
     * region modifier can be used to contract the region bounderies
     */
    public boolean Inside(Vector2D pos) {
        return Inside(pos, region_modifier.normal);
    }

    public boolean Inside(Vector2D pos, region_modifier r) {
        if (r == region_modifier.normal) {
            return ((pos.x > m_dLeft) && (pos.x < m_dRight)
                    && (pos.y > m_dTop) && (pos.y < m_dBottom));
        } else {
            final double marginX = m_dWidth * 0.25;
            final double marginY = m_dHeight * 0.25;

            return ((pos.x > (m_dLeft + marginX)) && (pos.x < (m_dRight - marginX))
                    && (pos.y > (m_dTop + marginY)) && (pos.y < (m_dBottom - marginY)));
        }
    }

    /** 
     * @return a vector representing a random location
     *          within the region
     */
    public Vector2D GetRandomPosition() {
        return new Vector2D(randInRange(m_dLeft, m_dRight),
                randInRange(m_dTop, m_dBottom));
    }

    //-------------------------------
    public double Top() {
        return m_dTop;
    }

    public double Bottom() {
        return m_dBottom;
    }

    public double Left() {
        return m_dLeft;
    }

    public double Right() {
        return m_dRight;
    }

    public double Width() {
        return abs(m_dRight - m_dLeft);
    }

    public double Height() {
        return abs(m_dTop - m_dBottom);
    }

    public double Length() {
        return max(Width(), Height());
    }

    public double Breadth() {
        return min(Width(), Height());
    }

    public Vector2D Center() {
        return new Vector2D(m_vCenter);
    }

    public int ID() {
        return m_iID;
    }
}