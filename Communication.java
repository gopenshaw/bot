package bot;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class Communication {
	final static int ENEMY_PASTR_COUNT_CHANNEL = 1;
	final static int SOLDIER_DESTINATION_CHANNEL = 0;
	
	protected static void setEnemyPastrCount(int count, RobotController rc) throws GameActionException
	{
		rc.broadcast(ENEMY_PASTR_COUNT_CHANNEL, count);
	}
	
	protected static int getEnemyPastrCount(RobotController rc) throws GameActionException
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
	
	private static int encodeMapLocation(MapLocation location)
	{
		return location.x + 100 * location.y;
	}
	
	private static MapLocation decodeMapLocation(int value) {
		return new MapLocation(value % 100, value / 100);
	}
}
