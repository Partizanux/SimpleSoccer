package simpleSoccer;

import java.util.HashMap;

/**
 * This class substitute C++ preprocessor.
 * Works only inside methods.
 *
 */
public class DEFINE {
   private static HashMap<Integer,Boolean> defined = new HashMap<Integer,Boolean>();
   
   public static final int DEBUG = 0;
   public static final int SHOW_TEAM_STATE = 1;
   public static final int SHOW_SUPPORTING_PLAYERS_TARGET = 2;
   public static final int SHOW_MESSAGING_INFO = 3;
   public static final int DEBUG_TEAM_STATES = 4;
   public static final int GOALY_STATE_INFO_ON = 5;
   public static final int PLAYER_STATE_INFO_ON = 6;
   
   static { 
       //define(DEBUG);
       //define(SHOW_TEAM_STATE);
       //define(SHOW_SUPPORTING_PLAYERS_TARGET);
       //define(SHOW_MESSAGING_INFO);
       //define(DEBUG_TEAM_STATES);
       //define(GOALY_STATE_INFO_ON);
       //define(PLAYER_STATE_INFO_ON);
   }
   public static boolean def(Integer D) {
       Boolean def =  defined.get(D);
       if(def == null) return false;
       return def;
   } 
   
   public static void define(Integer D) {
       defined.put(D, Boolean.TRUE);
   }
   
   public static void undef(Integer D) {
       defined.put(D, Boolean.FALSE);
   }
}
