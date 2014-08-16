package bot;

import battlecode.common.*;
import bot.Enums.*;

public class Communication {
	
//	private final static int TEAM_HQ_LOCATION_CHANNEL = 10000;
//	private final static int ENEMY_PASTR_COUNT_CHANNEL = 10001;
	private final static int ENEMY_PASTR_LOCATION_CHANNEL = 10002;
	private final static int RALLY_POINT_LOCATION_CHANNEL = 10003;
	private final static int TEAM_PASTR_LOCATION_CHANNEL = 10009;
//	
	private final static int PASTR_STATUS_CHANNEL = 10004;
	private final static int NOISE_TOWER_STATUS_CHANNEL = 10005;

	private final static int TACTIC_CHANNEL = 10007;
//	private final static int NAVIGATION_MODE_CHANNEL = 10008;
//	
	private final static int NODE_OFFSET_VALUE = 20000;
//	
	public static MapLocation getDestinationFrom(MapLocation location, RobotController rc) 
			throws GameActionException
	{
		int nodeIndex = getNodeThatContains(location, rc);
		return decodeMapLocation(rc.readBroadcast(NODE_OFFSET_VALUE + nodeIndex));
	}
	
	public static void setNodeTarget(int nodeIndex, MapLocation location, RobotController rc) 
			throws GameActionException
	{
		rc.broadcast(NODE_OFFSET_VALUE + nodeIndex, encodeMapLocation(location));
	}
	
	public static void setNodeIndex(int index, MapLocation location, RobotController rc) 
			throws GameActionException
	{
		rc.broadcast(encodeMapLocation(location), index);
	}
	
	public static int getNodeThatContains(MapLocation location, RobotController rc) 
			throws GameActionException
	{
		return rc.readBroadcast(encodeMapLocation(location));
	}
//	
//	public static void setNavigationMode(
//			NavigationMode status, RobotController rc) throws GameActionException
//	{
//		rc.broadcast(NAVIGATION_MODE_CHANNEL, status.ordinal());
//	}
//	
//	public static NavigationMode GetNavigationMode(RobotController rc) 
//			throws GameActionException
//	{
//		return NavigationMode.values()[rc.readBroadcast(NAVIGATION_MODE_CHANNEL)];
//	}
//	
//	public static void setTeamHQ(MapLocation location, RobotController rc) 
//			throws GameActionException
//	{
//		rc.broadcast(TEAM_HQ_LOCATION_CHANNEL, encodeMapLocation(location));
//	}
//	
//	public static MapLocation getTeamHQ(RobotController rc) 
//			throws GameActionException
//	{
//		return decodeMapLocation(rc.readBroadcast(TEAM_HQ_LOCATION_CHANNEL));
//	}
//	
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

	public static void setPastrBuildingStatus(
			Status status, RobotController rc) throws GameActionException
	{
		rc.broadcast(PASTR_STATUS_CHANNEL, status.ordinal());
	}
	
	public static Status getPastrBuildingStatus(RobotController rc) 
			throws GameActionException
	{
		return Status.values()[rc.readBroadcast(PASTR_STATUS_CHANNEL)];
	}
	
	public static void setNoiseTowerBuildingStatus(
			Status status, RobotController rc) throws GameActionException
	{
		rc.broadcast(NOISE_TOWER_STATUS_CHANNEL, status.ordinal());
	}
	
	public static Status getNoiseTowerBuildingStatus(RobotController rc) 
			throws GameActionException
	{
		return Status.values()[rc.readBroadcast(NOISE_TOWER_STATUS_CHANNEL)];
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
		rc.broadcast(TEAM_PASTR_LOCATION_CHANNEL, encodeMapLocation(location));
	}
	
	public static MapLocation getPastrLocation(RobotController rc) 
			throws GameActionException
	{
		return decodeMapLocation(rc.readBroadcast(TEAM_PASTR_LOCATION_CHANNEL));
	}
	
	public static void setRallyPoint(MapLocation location, RobotController rc) 
			throws GameActionException {
		rc.broadcast(RALLY_POINT_LOCATION_CHANNEL, encodeMapLocation(location));
	}
	
	public static MapLocation getRallyPoint(RobotController rc) 
			throws GameActionException
	{
		return decodeMapLocation(rc.readBroadcast(RALLY_POINT_LOCATION_CHANNEL));
	}
	
	private static int encodeMapLocation(MapLocation location)
	{
		return location.x + 100 * location.y;
	}
	
	private static MapLocation decodeMapLocation(int value) {
		return new MapLocation(value % 100, value / 100);
	}
}
