package simpleSoccer.agents;

import common.D2.Vector2D;
import common.D2.C2DMatrix;
import static common.D2.Vector2D.*;

abstract public class MovingEntity extends BaseGameEntity {
    protected Vector2D velocity;
    //a normalized vector pointing in the direction the entity is heading. 
    protected Vector2D heading;
    //a vector perpendicular to the heading vector
    protected Vector2D side;
    protected double mass;
    //the maximum speed this entity may travel at.
    protected double maxSpeed;
    //the maximum force this entity can produce to power itself 
    //(think rockets and thrust)
    protected double maxForce;
    //the maximum rate (radians per second)this vehicle can rotate         
    protected double maxTurnRate;

    public MovingEntity(Vector2D position, double radius, Vector2D velocity, double max_speed,
                        Vector2D heading, double mass, Vector2D scale, double turn_rate, double max_force) {
        super();
        this.heading = new Vector2D(heading);
        this.velocity = new Vector2D(velocity);
        this.mass = mass;
        this.scale = new Vector2D(scale);
        side = this.heading.Perp();
        maxSpeed = max_speed;
        maxTurnRate = turn_rate;
        maxForce = max_force;
        pos = new Vector2D(position);
        boundingRadius = radius;
    }

    //accessors
    public Vector2D velocity() {
        return new Vector2D(velocity);
    }

    public void setVelocity(Vector2D NewVel) {
        velocity = NewVel;
    }

    public double mass() {
        return mass;
    }

    public Vector2D side() {
        return side;
    }

    public double maxSpeed() {
        return maxSpeed;
    }

    public void setMaxSpeed(double new_speed) {
        maxSpeed = new_speed;
    }

    public double maxForce() {
        return maxForce;
    }

    public void setMaxForce(double mf) {
        maxForce = mf;
    }

    public boolean isSpeedMaxedOut() {
        return maxSpeed * maxSpeed >= velocity.LengthSq();
    }

    public double speed() {
        return velocity.Length();
    }

    public double speedSq() {
        return velocity.LengthSq();
    }

    public Vector2D heading() {
        return heading;
    }

    double maxTurnRate() {
        return maxTurnRate;
    }

    void setMaxTurnRate(double val) {
        maxTurnRate = val;
    }

    /**
     *  Given a target position, this method rotates the entity's heading and
     *  side vectors by an amount not greater than maxTurnRate until it
     *  directly faces the target.
     *
     *  @return true when the heading is facing in the desired direction
     */
    public boolean rotateHeadingToFacePosition(Vector2D target) {
        Vector2D toTarget = Vec2DNormalize(sub(target, pos));

        //first determine the angle between the heading vector and the target
        double angle = Math.acos(heading.Dot(toTarget));
        
        //sometimes heading.Dot(toTarget) == 1.000000002
        if(Double.isNaN(angle)) { 
            angle = 0;
        }
        //return true if the player is facing the target
        if (angle < 0.00001) {
            return true;
        }

        //clamp the amount to turn to the max turn rate
        if (angle > maxTurnRate) {
            angle = maxTurnRate;
        }

        //The next few lines use a rotation matrix to rotate the player's heading
        //vector accordingly
        C2DMatrix RotationMatrix = new C2DMatrix();

        //notice how the direction of rotation has to be determined when creating
        //the rotation matrix
        RotationMatrix.Rotate(angle * heading.Sign(toTarget));
        RotationMatrix.TransformVector2Ds(heading);
        RotationMatrix.TransformVector2Ds(velocity);

        //finally recreate side
        side = heading.Perp();

        return false;
    }

    /**
     *  first checks that the given heading is not a vector of zero length. If the
     *  new heading is valid this function sets the entity's heading and side
     *  vectors accordingly
     */
    public void setHeading(Vector2D new_heading) {
        if (new_heading.LengthSq() - 1.0 < 0.00001) {
            heading = new_heading;
            side = heading.Perp();
        }
    }
}