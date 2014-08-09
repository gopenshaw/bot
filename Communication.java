package bot;

import battlecode.common.*;

public class Communication {
	
	private final static int ENEMY_PASTR_COUNT_CHANNEL = 0;
	private final static int SOLDIER_DESTINATION_CHANNEL = 1;
	
	private final static int BUILD_PASTR_COMMAND_CHANNEL = 2;
	
	private final static int PASTR_COMMAND_CHANNEL = 3;
	private final static int PASTR_STATUS_CHANNEL = 4;
	private final static int NOISE_TOWER_STATUS_CHANNEL = 7;
	
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
	
	protected static MapLocation getPastrLocation(RobotController rc) 
			throws GameActionException
	{	
		return decodeMapLocation(rc.readBroadcast(BUILD_PASTR_COMMAND_CHANNEL));
	}
	
	private static int encodeMapLocation(MapLocation location)
	{
		return location.x + 100 * location.y;
	}
	
	private static MapLocation decodeMapLocation(int value) {
		return new MapLocation(value % 100, value / 100);
	}
}
