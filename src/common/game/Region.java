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
    public enum RegionModifier {
        HALFSIZE, NORMAL
    }

    protected double top;
    protected double left;
    protected double right;
    protected double bottom;
    protected double width;
    protected double height;
    protected Vector2D center;
    protected int id;

    public Region() {
        this(0, 0, 0, 0, -1);
    }

    public Region(double left, double top, double right, double bottom) {
        this(left, top, right, bottom, -1);
    }

    public Region(double left, double top, double right, double bottom, int id) {
        this.top = top;
        this.right = right;
        this.left = left;
        this.bottom = bottom;
        this.id = id;

        //calculate center of region
        center = new Vector2D((left + right) * 0.5, (top + bottom) * 0.5);

        width = abs(right - left);
        height = abs(bottom - top);
    }

    public void Render() {
        Render(false);
    }

    public void Render(boolean ShowID) {
        gdi.HollowBrush();
        gdi.GreenPen();
        gdi.Rect(left, top, right, bottom);

        if (ShowID) {
            gdi.TextColor(Cgdi.green);
            gdi.TextAtPos(center(), ttos(id()));
        }
    }

    /**
     * returns true if the given position lays inside the region. The
     * region modifier can be used to contract the region bounderies
     */
    public boolean inside(Vector2D pos) {
        return inside(pos, RegionModifier.NORMAL);
    }

    public boolean inside(Vector2D pos, RegionModifier r) {
        if (r == RegionModifier.NORMAL) {
            return ((pos.x > left) && (pos.x < right)
                    && (pos.y > top) && (pos.y < bottom));
        } else {
            double marginX = width * 0.25;
            double marginY = height * 0.25;
            return ((pos.x > (left + marginX)) && (pos.x < (right - marginX))
                    && (pos.y > (top + marginY)) && (pos.y < (bottom - marginY)));
        }
    }

    /** 
     * @return a vector representing a random location
     *          within the region
     */
    public Vector2D getRandomPosition() {
        return new Vector2D(randInRange(left, right), randInRange(top, bottom));
    }

    public double top() {
        return top;
    }

    public double bottom() {
        return bottom;
    }

    public double left() {
        return left;
    }

    public double right() {
        return right;
    }

    public double width() {
        return abs(right - left);
    }

    public double height() {
        return abs(top - bottom);
    }

    public double length() {
        return max(width(), height());
    }

    public double breadth() {
        return min(width(), height());
    }

    public Vector2D center() {
        return new Vector2D(center);
    }

    public int id() {
        return id;
    }
}