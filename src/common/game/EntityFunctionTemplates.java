package common.game;

import simpleSoccer.agents.BaseGameEntity;
import common.D2.Vector2D;
import static common.D2.Vector2D.*;
import static common.D2.geometry.*;
import java.util.List;
import java.util.ListIterator;

public class EntityFunctionTemplates {
//////////////////////////////////////////////////////////////////////////
//
//  Some useful template functions
//
//////////////////////////////////////////////////////////////////////////

    /**
     *  tests to see if an entity is overlapping any of a number of entities
     *  stored in a std container
     */
    public static <T extends BaseGameEntity, conT extends List<? extends BaseGameEntity>>
            boolean Overlapped(final T ob, final conT conOb) {
        return Overlapped(ob, conOb, 40.0);
    }

    public static <T extends BaseGameEntity, conT extends List<? extends BaseGameEntity>>
            boolean Overlapped(final T ob, final conT conOb, double MinDistBetweenObstacles) {
        ListIterator<? extends BaseGameEntity> it = conOb.listIterator();

        while (it.hasNext()) {
            BaseGameEntity curOb = it.next();
            if (TwoCirclesOverlapped(ob.pos(),
                    ob.getBRadius() + MinDistBetweenObstacles,
                    curOb.pos(),
                    curOb.getBRadius())) {
                return true;
            }
        }

        return false;
    }

    /**
     * tags any entities contained in a std container that are within the
     * radius of the single entity parameter
     */
    public static <T extends BaseGameEntity, conT extends List<? extends T>> 
            void TagNeighbors(T entity, conT others, final double radius) {
        ListIterator<? extends T> it = others.listIterator();

        //iterate through all entities checking for range
        while (it.hasNext()) {
            T curOb = it.next();
            //first clear any current tag
            curOb.unTag();

            //work in distance squared to avoid sqrts
            Vector2D to = sub(curOb.pos(), entity.pos());

            //the bounding radius of the other is taken into account by adding it 
            //to the range
            double range = radius + curOb.getBRadius();

            //if entity within range, tag for further consideration
            if ((curOb != entity) && (to.LengthSq() < range * range)) {
                curOb.tag();
            }

        }//next entity
    }

    /**
     * Given a pointer to an entity and a std container of pointers to nearby
     * entities, this function checks to see if there is an overlap between
     * entities. If there is, then the entities are moved away from each
     * other
     */
    public static <T extends BaseGameEntity, conT extends List<? extends BaseGameEntity>> 
            void EnforceNonPenetrationContraint(T entity, final conT others) {
        ListIterator<? extends BaseGameEntity> it = others.listIterator();

        //iterate through all entities checking for any overlap of bounding
        //radii
        while (it.hasNext()) {
            BaseGameEntity curOb = it.next();
            //make sure we don't check against this entity
            if (curOb == entity) {
                continue;
            }

            //calculate the distance between the positions of the entities
            Vector2D ToEntity = sub(entity.pos(), curOb.pos());

            double DistFromEachOther = ToEntity.Length();

            //if this distance is smaller than the sum of their radii then this
            //entity must be moved away in the direction parallel to the
            //ToEntity vector   
            double AmountOfOverLap = curOb.getBRadius() + entity.getBRadius()
                    - DistFromEachOther;

            if (AmountOfOverLap >= 0) {
                //move the entity a distance away equivalent to the amount of overlap.
                entity.setPos(add(entity.pos(), mul(div(ToEntity, DistFromEachOther),
                        AmountOfOverLap)));
            }
        }//next entity
    }
}