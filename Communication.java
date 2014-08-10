package bot;

import battlecode.common.*;
import bot.Enums.ConstructionStatus;
import bot.Enums.Tactic;

public class Communication {
	
	private final static int ENEMY_PASTR_COUNT_CHANNEL = 0;
	private final static int ENEMY_PASTR_LOCATION_CHANNEL = 1;
	private final static int PASTR_LOCATION_CHANNEL = 2;
	
	private final static int PASTR_STATUS_CHANNEL = 4;
	private final static int NOISE_TOWER_STATUS_CHANNEL = 5;
	
	private final static int MAP_CENTER_CHANNEL = 6;
	private final static int TACTIC_CHANNEL = 7;
	
	public static void setTactic(
			Tactic tactic, RobotController rc) throws GameActionException
	{
		rc.broadcast(TACTIC_CHANNEL, tactic.ordinal());
	}
	
	public static Tactic getTactic(RobotController rc) 
			throws GameActionException
	{
		return Tactic.values()[rc.readBroadcast(TACTIC_CHANNEL)];
	}
	
	public static void setMapCenter(
			MapLocation location, RobotController rc) throws GameActionException
	{
		rc.broadcast(MAP_CENTER_CHANNEL, encodeMapLocation(location));
	}
	
	public static MapLocation getMapCenter(RobotController rc) 
			throws GameActionException
	{
		return decodeMapLocation(rc.readBroadcast(MAP_CENTER_CHANNEL));
	}
	
	public static void setPastrBuildingStatus(
			ConstructionStatus status, RobotController rc) throws GameActionException
	{
		rc.broadcast(PASTR_STATUS_CHANNEL, status.ordinal());
	}
	
	public static ConstructionStatus getPastrBuildingStatus(RobotController rc) 
			throws GameActionException
	{
		return ConstructionStatus.values()[rc.readBroadcast(PASTR_STATUS_CHANNEL)];
	}
	
	public static void setNoiseTowerBuildingStatus(
			ConstructionStatus status, RobotController rc) throws GameActionException
	{
		rc.broadcast(NOISE_TOWER_STATUS_CHANNEL, status.ordinal());
	}
	
	public static ConstructionStatus getNoiseTowerBuildingStatus(RobotController rc) 
			throws GameActionException
	{
		return ConstructionStatus.values()[rc.readBroadcast(NOISE_TOWER_STATUS_CHANNEL)];
	}
	
	public static void setEnemyPastrCount(int count, RobotController rc) 
			throws GameActionException
	{
		rc.broadcast(ENEMY_PASTR_COUNT_CHANNEL, count);
	}
	
	public static int getEnemyPastrCount(RobotController rc) 
			throws GameActionException
	{
		return rc.readBroadcast(ENEMY_PASTR_COUNT_CHANNEL);
	}
	
	public static void setEnemyPastrLocation(MapLocation location, RobotController rc) 
			throws GameActionException {
		rc.broadcast(ENEMY_PASTR_LOCATION_CHANNEL, encodeMapLocation(location));
	}
	
	public static MapLocation getEnemyPastrLocation(RobotController rc) 
			throws GameActionException
	{
		return decodeMapLocation(rc.readBroadcast(ENEMY_PASTR_LOCATION_CHANNEL));
	}
	
	public static void setPastrLocation(MapLocation location, RobotController rc) 
			throws GameActionException {
		rc.broadcast(PASTR_LOCATION_CHANNEL, encodeMapLocation(location));
	}
	
	public static MapLocation getPastrLocation(RobotController rc) 
			throws GameActionException
	{
		return decodeMapLocation(rc.readBroadcast(PASTR_LOCATION_CHANNEL));
	}
	
	private static int encodeMapLocation(MapLocation location)
	{
		return location.x + 100 * location.y;
	}
	
	private static MapLocation decodeMapLocation(int value) {
		return new MapLocation(value % 100, value / 100);
	}
}
