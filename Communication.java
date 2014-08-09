package bot;

import battlecode.common.*;

public class Communication {
	
	final static int ENEMY_PASTR_COUNT_CHANNEL = 0;
	final static int SOLDIER_DESTINATION_CHANNEL = 1;
	
	final static int BUILD_PASTR_COMMAND_CHANNEL = 2;
	final static int CURRENTLY_BUIDLING_PASTR_CHANNEL = 3;
	
	final static int PASTR_COMMAND_CHANNEL = 4;
	final static int PASTR_STATUS_CHANNEL = 5;
	final static int NOISE_TOWER_STATUS_CHANNEL = 6;
	
	final static int DO_NOT_BUILD_PASTR_VALUE = -1;
	
	protected static void setPastrBuildingStatus(
			ConstructionStatus status, RobotController rc) throws GameActionException
	{
		rc.broadcast(PASTR_STATUS_CHANNEL, status.ordinal());
	}
	
	protected static ConstructionStatus getPastrBuildingStatus(RobotController rc) 
			throws GameActionException
	{
		return ConstructionStatus.values()[rc.readBroadcast(PASTR_STATUS_CHANNEL)];
	}
	
	protected static void setNoiseTowerBuildingStatus(
			ConstructionStatus status, RobotController rc) throws GameActionException
	{
		rc.broadcast(NOISE_TOWER_STATUS_CHANNEL, status.ordinal());
	}
	
	protected static ConstructionStatus getNoiseTowerBuildingStatus(RobotController rc) 
			throws GameActionException
	{
		return ConstructionStatus.values()[rc.readBroadcast(NOISE_TOWER_STATUS_CHANNEL)];
	}
	
	protected static void setEnemyPastrCount(int count, RobotController rc) 
			throws GameActionException
	{
		rc.broadcast(ENEMY_PASTR_COUNT_CHANNEL, count);
	}
	
	protected static int getEnemyPastrCount(RobotController rc) 
			throws GameActionException
	{
		return rc.readBroadcast(ENEMY_PASTR_COUNT_CHANNEL);
	}
	
	protected static void broadcastDestination(MapLocation location, RobotController rc) 
			throws GameActionException {
		rc.broadcast(SOLDIER_DESTINATION_CHANNEL, encodeMapLocation(location));
	}
	
	protected static MapLocation getDestination(RobotController rc) 
			throws GameActionException
	{
		return decodeMapLocation(rc.readBroadcast(SOLDIER_DESTINATION_CHANNEL));
	}
	
	protected static void setPastrCommand(ConstructionCommand command, RobotController rc) 
			throws GameActionException
	{
		rc.broadcast(PASTR_COMMAND_CHANNEL, command.ordinal());
	}
	
	protected static ConstructionCommand getPastrCommand(RobotController rc)
			throws GameActionException
	{
		return ConstructionCommand.values()[rc.readBroadcast(PASTR_COMMAND_CHANNEL)];
	}
	
	protected static void buildPastr(MapLocation location, RobotController rc) 
			throws GameActionException
	{
		rc.broadcast(BUILD_PASTR_COMMAND_CHANNEL, encodeMapLocation(location));
	}
	
	protected static void delayPastr(RobotController rc) throws GameActionException
	{
		rc.broadcast(BUILD_PASTR_COMMAND_CHANNEL, DO_NOT_BUILD_PASTR_VALUE);
	}
	
	protected static MapLocation getPastrLocation(RobotController rc) 
			throws GameActionException
	{
		int value = rc.readBroadcast(BUILD_PASTR_COMMAND_CHANNEL);
		if (value == DO_NOT_BUILD_PASTR_VALUE)
		{
			return null;
		}
		
		return decodeMapLocation(value);
	}
	
	private static int encodeMapLocation(MapLocation location)
	{
		return location.x + 100 * location.y;
	}
	
	private static MapLocation decodeMapLocation(int value) {
		return new MapLocation(value % 100, value / 100);
	}
}
