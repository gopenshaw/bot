package bot;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class Communication {
	final static int BUILD_PASTR_INDEX = 0;
	final static int ENEMY_PASTR_COUNT_INDEX = 1;
	final static int ENEMY_PASTR_LOCATION_DATA_START = 2;
	final static int ENEMY_PASTR_LOCATION_DATA_SIZE = 20;
	
	protected static void broadcastEnemyPastrLocations(int index, MapLocation location, RobotController rc) 
			throws GameActionException {
		rc.broadcast(ENEMY_PASTR_LOCATION_DATA_START + index * 2, location.x);
		rc.broadcast(ENEMY_PASTR_LOCATION_DATA_START + index * 2 + 1, location.y);
	}
	
	protected static MapLocation getEnemyPastrLocation(int index, RobotController rc) 
			throws GameActionException
	{
		return new MapLocation(rc.readBroadcast(ENEMY_PASTR_LOCATION_DATA_START + index * 2),
				rc.readBroadcast(ENEMY_PASTR_LOCATION_DATA_START + index * 2 + 1));
	}
	
	protected static void setEnemyPastrCount(int count, RobotController rc) throws GameActionException
	{
		rc.broadcast(ENEMY_PASTR_COUNT_INDEX, count);
	}
	
	protected static int getEnemyPastrCount(RobotController rc) throws GameActionException
	{
		return rc.readBroadcast(ENEMY_PASTR_COUNT_INDEX);
	}
}
