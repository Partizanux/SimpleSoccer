package simpleSoccer.agents;

import common.D2.Vector2D;
import common.messaging.Telegram;
import static common.misc.Utils.*;

public abstract class BaseGameEntity {
    public static int DEFAULT_ENTITY_TYPE = -1;
    private static int nextValidID = 0;
    private final int id;
    //every entity has a type associated with it (health, troll, ammo etc)
    private int type;
    private boolean tagged;
    protected Vector2D pos = new Vector2D();
    protected Vector2D scale = new Vector2D();
    //the magnitude of this object's bounding radius
    protected double boundingRadius;

    public BaseGameEntity() {
        boundingRadius = 0.0;
        scale = new Vector2D(1.0, 1.0);
        type = DEFAULT_ENTITY_TYPE;
        tagged = false;

        id = nextValidID++;
    }

    abstract public void update();

    abstract public void render();

    public boolean handleMessage(Telegram msg) {
        return false;
    }

    public static void resetNextValidID() {
        nextValidID = 0;
    }

    public Vector2D pos() {
        return new Vector2D(pos);
    }

    public void setPos(Vector2D new_pos) {
        pos = new Vector2D(new_pos);
    }

    public double getBRadius() {
        return boundingRadius;
    }

    public void bRadius(double r) {
        boundingRadius = r;
    }

    public int getId() {
        return id;
    }

    public boolean isTagged() {
        return tagged;
    }

    public void tag() {
        tagged = true;
    }

    public void unTag() {
        tagged = false;
    }

    public Vector2D scale() {
        return new Vector2D(scale);
    }

    public void setScale(Vector2D val) {
        boundingRadius *= MaxOf(val.x, val.y) / MaxOf(scale.x, scale.y);
        scale = new Vector2D(val);
    }

    public void setScale(double val) {
        boundingRadius *= (val / MaxOf(scale.x, scale.y));
        scale = new Vector2D(val, val);
    }

    public int getEntityType() {
        return type;
    }

    public void setEntityType(int new_type) {
        type = new_type;
    }
}
