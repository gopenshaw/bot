package bot;

import battlecode.common.*;
import bot.Enums.*;
import bot.Robots.HQ;

public class Communication {

	private final static int POINT_OF_INTEREST_BASE_CHANNEL = 10000;
	
	private final static int PASTR_STATUS_CHANNEL = 10004;
	private final static int NOISE_TOWER_STATUS_CHANNEL = 10005;

	private final static int TACTIC_CHANNEL = 10007;
	
	private final static int NODE_MAPS_BEGINNING_CHANNEL = 20000;
	private final static int NODE_MAP_SIZE = MapNode.MAX_MAP_NODES;
	
	public static MapLocation getNodeDestination(PointOfInterest poi, MapLocation currentLocation, RobotController rc) 
			throws GameActionException
	{
		int nodeIndex = getNodeIndex(currentLocation, rc);
		return getNodeTarget(poi, nodeIndex, rc);
	}
	
	public static void setPointOfInterest(PointOfInterest poi, MapLocation location, RobotController rc) 
			throws GameActionException {
		rc.broadcast(POINT_OF_INTEREST_BASE_CHANNEL + poi.ordinal(), encode(location));
	}
	
	public static MapLocation getPointOfInterest(PointOfInterest poi, RobotController rc) 
			throws GameActionException
	{
		return decode(rc.readBroadcast(POINT_OF_INTEREST_BASE_CHANNEL + poi.ordinal()));
	}
	
	public static void setNodeTarget(int nodeIndex, PointOfInterest poi, MapLocation target, RobotController rc) 
			throws GameActionException
	{
		rc.broadcast(getMapChannel(poi, nodeIndex), encode(target));
	}
	
	public static void setNodeIndex(int index, MapLocation location, RobotController rc) 
			throws GameActionException
	{
		rc.broadcast(encode(location), index);
	}
	
	public static int getNodeIndex(MapLocation location, RobotController rc) 
			throws GameActionException
	{
		return rc.readBroadcast(encode(location));
	}
	
	public static void setTactic(
			Tactic tactic, RobotController rc) throws GameActionException
	{
		rc.setIndicatorString(HQ.TACTIC_INDICATOR_INDEX, "tactic set: " + tactic);
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
	
	private static MapLocation getNodeTarget(PointOfInterest poi, int currentNodeIndex, RobotController rc) 
			throws GameActionException

	{
		int value = rc.readBroadcast(getMapChannel(poi, currentNodeIndex));
		return value == 0 ? null : decode(value);
	}
	
	private static int getMapChannel(PointOfInterest poi, int nodeIndex)
	{
		return NODE_MAPS_BEGINNING_CHANNEL
				+ poi.ordinal() * NODE_MAP_SIZE 
				+ nodeIndex;
	}
	
	private static int encode(MapLocation location)
	{
		return location.x + 100 * location.y;
	}
	
	private static MapLocation decode(int value) 
	{
		return new MapLocation(value % 100, value / 100);
	}
}
