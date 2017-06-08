package common.game;

import simpleSoccer.agents.BaseGameEntity;
import java.util.HashMap;

public class EntityManager {
    public static final EntityManager EntityMgr = new EntityManager();

    private class EntityMap extends HashMap<Integer, BaseGameEntity> {
    }
    //to facilitate quick lookup the entities are stored in a std::map, in which
    //pointers to entities are cross referenced by their identifying number
    private EntityMap m_EntityMap = new EntityMap();

    private EntityManager() {
    }

//copy ctor and assignment should be private
    private EntityManager(final EntityManager m) {
    }

//--------------------------- Instance ----------------------------------------
//   this class is a singleton
//-----------------------------------------------------------------------------
    public static EntityManager Instance() {
        return EntityMgr;
    }

    /**
     * this method stores a pointer to the entity in the std::vector
     * m_Entities at the index position indicated by the entity's getId
     * (makes for faster access)
     */
    public void RegisterEntity(BaseGameEntity NewEntity) {
        m_EntityMap.put(NewEntity.getId(), NewEntity);
    }

    /**
     * @return a pointer to the entity with the getId given as a parameter
     */
    public BaseGameEntity GetEntityFromID(int id) {
        //find the entity
        BaseGameEntity ent = m_EntityMap.get(id);

        //assert that the entity is a member of the map
        assert (ent != null) : "<EntityManager::GetEntityFromID>: invalid getId";

        return ent;
    }

    /**
     * this method removes the entity from the list
     */
    public void RemoveEntity(BaseGameEntity pEntity) {
        m_EntityMap.remove(pEntity.getId());
    }

    /**
     * clears all entities from the entity map
     */
    public void Reset() {
        m_EntityMap.clear();
    }
}
