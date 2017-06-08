package simpleSoccer;

import common.misc.iniFileLoaderBase;
import java.io.IOException;

public class ParamLoader extends iniFileLoaderBase {

    public final static ParamLoader Prm;

    static {
        try {
            Prm = new ParamLoader();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static ParamLoader Instance() {
        return Prm;
    }

    private ParamLoader() throws IOException {
        super(ParamLoader.class.getResourceAsStream("Params.ini"));
        GoalWidth                   = GetNextParameterDouble(); 
    
    NumSupportSpotsX            = GetNextParameterInt();    
    NumSupportSpotsY            = GetNextParameterInt();  
    
    Spot_PassSafeScore                     = GetNextParameterDouble();
    Spot_CanScoreFromPositionScore         = GetNextParameterDouble();
    Spot_DistFromControllingPlayerScore     = GetNextParameterDouble();
    Spot_ClosenessToSupportingPlayerScore  = GetNextParameterDouble();
    Spot_AheadOfAttackerScore              = GetNextParameterDouble();

    SupportSpotUpdateFreq       = GetNextParameterDouble(); 
    
    ChancePlayerAttemptsPotShot = GetNextParameterDouble();
    ChanceOfUsingArriveTypeReceiveBehavior = GetNextParameterDouble();
    
    BallSize                    = GetNextParameterDouble();    
    BallMass                    = GetNextParameterDouble();    
    Friction                    = GetNextParameterDouble(); 
    
    KeeperInBallRange           = GetNextParameterDouble();    
    PlayerInTargetRange         = GetNextParameterDouble(); 
    PlayerKickingDistance       = GetNextParameterDouble(); 
    PlayerKickFrequency         = GetNextParameterDouble();


    PlayerMass                  = GetNextParameterDouble(); 
    PlayerMaxForce              = GetNextParameterDouble();    
    PlayerMaxSpeedWithBall      = GetNextParameterDouble();   
    PlayerMaxSpeedWithoutBall   = GetNextParameterDouble();   
    PlayerMaxTurnRate           = GetNextParameterDouble();   
    PlayerScale                 = GetNextParameterDouble();      
    PlayerComfortZone           = GetNextParameterDouble();  
    PlayerKickingAccuracy       = GetNextParameterDouble();

    NumAttemptsToFindValidStrike = GetNextParameterInt();


    
    MaxDribbleForce             = GetNextParameterDouble();    
    MaxShootingForce            = GetNextParameterDouble();    
    MaxPassingForce             = GetNextParameterDouble();  
    
    WithinRangeOfHome           = GetNextParameterDouble();    
    WithinRangeOfSupportSpot    = GetNextParameterDouble();    
    
    MinPassDist                 = GetNextParameterDouble();
    GoalkeeperMinPassDist       = GetNextParameterDouble();
    
    GoalKeeperTendingDistance   = GetNextParameterDouble();    
    GoalKeeperInterceptRange    = GetNextParameterDouble();
    BallWithinReceivingRange    = GetNextParameterDouble();
    
    bStates                     = GetNextParameterBool();    
    bIDs                        = GetNextParameterBool(); 
    bSupportSpots               = GetNextParameterBool();     
    bRegions                    = GetNextParameterBool();
    bShowControllingTeam        = GetNextParameterBool();
    bViewTargets                = GetNextParameterBool();
    bHighlightIfThreatened      = GetNextParameterBool();

    FrameRate                   = GetNextParameterInt();

    SeparationCoefficient       = GetNextParameterDouble(); 
    ViewDistance                = GetNextParameterDouble(); 
    bNonPenetrationConstraint   = GetNextParameterBool(); 


    BallWithinReceivingRangeSq = BallWithinReceivingRange * BallWithinReceivingRange;
    KeeperInBallRangeSq      = KeeperInBallRange * KeeperInBallRange;
    PlayerInTargetRangeSq    = PlayerInTargetRange * PlayerInTargetRange;   
    PlayerKickingDistance   += BallSize;
    PlayerKickingDistanceSq  = PlayerKickingDistance * PlayerKickingDistance;
    PlayerComfortZoneSq      = PlayerComfortZone * PlayerComfortZone;
    GoalKeeperInterceptRangeSq     = GoalKeeperInterceptRange * GoalKeeperInterceptRange;
    WithinRangeOfSupportSpotSq = WithinRangeOfSupportSpot * WithinRangeOfSupportSpot;
    }
    
  public double GoalWidth;

  public int   NumSupportSpotsX;
  public int   NumSupportSpotsY;

  //these values tweak the various rules used to calculate the support spots
  public double Spot_PassSafeScore;
  public double Spot_CanScoreFromPositionScore;
  public double Spot_DistFromControllingPlayerScore;
  public double Spot_ClosenessToSupportingPlayerScore;
  public double Spot_AheadOfAttackerScore;  
  
  public double SupportSpotUpdateFreq ;

  public double ChancePlayerAttemptsPotShot; 
  public double ChanceOfUsingArriveTypeReceiveBehavior;

  public double BallSize;
  public double BallMass;
  public double Friction;

  public double KeeperInBallRange;
  public double KeeperInBallRangeSq;

  public double PlayerInTargetRange;
  public double PlayerInTargetRangeSq;
  
  public double PlayerMass;
  
  //max steering force
  public double PlayerMaxForce; 
  public double PlayerMaxSpeedWithBall;
  public double PlayerMaxSpeedWithoutBall;
  public double PlayerMaxTurnRate;
  public double PlayerScale;
  public double PlayerComfortZone;

  public double PlayerKickingDistance;
  public double PlayerKickingDistanceSq;

  public double PlayerKickFrequency; 

  public double  MaxDribbleForce;
  public double  MaxShootingForce;
  public double  MaxPassingForce;

  public double  PlayerComfortZoneSq;

  //in the range zero to 1.0. adjusts the amount of noise added to a kick,
  //the lower the value the worse the players get
  public double  PlayerKickingAccuracy;

  //the number of times the SoccerTeam::canShoot method attempts to find
  //a valid shot
  public int    NumAttemptsToFindValidStrike;

  //the distance away from the center of its home region a player
  //must be to be considered at home
  public double WithinRangeOfHome;

  //how close a player must get to a sweet spot before he can change state
  public double WithinRangeOfSupportSpot;
  public double WithinRangeOfSupportSpotSq;
 
  
  //the minimum distance a receiving player must be from the passing player
  public double   MinPassDist;
  public double   GoalkeeperMinPassDist;

  //this is the distance the keeper puts between the back of the net
  //and the ball when using the INTERPOSE steering behavior
  public double  GoalKeeperTendingDistance;

  //when the ball becomes within this distance of the goalkeeper he
  //changes state to intercept the ball
  public double  GoalKeeperInterceptRange;
  public double  GoalKeeperInterceptRangeSq;

  //how close the ball must be to a receiver before he starts chasing it
  public double  BallWithinReceivingRange;
  public double  BallWithinReceivingRangeSq;


  //these values control what debug info you can see
  public boolean  bStates;
  public boolean  bIDs;
  public boolean  bSupportSpots;
  public boolean  bRegions;
  public boolean  bShowControllingTeam;
  public boolean  bViewTargets;
  public boolean  bHighlightIfThreatened;

  public int FrameRate;

  
  public double SeparationCoefficient;

  //how close a neighbour must be before an agent perceives it
  public double ViewDistance;

  //zero this to turn the constraint off
  public boolean bNonPenetrationConstraint;
}
