package bot;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class Communication {
	final static int ENEMY_PASTR_COUNT_CHANNEL = 10001;
	final static int SOLDIER_DESTINATION_CHANNEL = 10002;
	
	final static int BUILD_PASTR_CHANNEL = 10003;
	final static int DO_NOT_BUILD_PASTR_VALUE = -1;
	
	final static int DESTINATION_INDEX_LENGTH = 5;
	final static int MAP_SIZE_OFFSET = 10000;
	
	private static int[] destinationIndex = new int[DESTINATION_INDEX_LENGTH];
	
	protected static void broadcastSegment(MapLocation destination,
			MapLocation segmentBegin, 
			MapLocation segmentEnd, 
			RobotController rc) 
			throws GameActionException
	{
		int offset = MAP_SIZE_OFFSET * getDestinationIndex(destination);
		if (offset < 0)
		{
			return;
		}
		
		rc.broadcast(offset + encodeMapLocation(segmentBegin),
				offset + encodeMapLocation(segmentEnd));
	}
	
	protected static MapLocation getSegment(MapLocation destination, 
			MapLocation segmentBegin,
			RobotController rc) 
			throws GameActionException
	{
		int offset = MAP_SIZE_OFFSET * getDestinationIndex(destination);
		
		int value =  rc.readBroadcast(offset + encodeMapLocation(segmentBegin));
		if (value == 0)
		{
			return null;
		}
		
		return decodeMapLocation(value);
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
	
	protected static void buildPastr(MapLocation location, RobotController rc) 
			throws GameActionException
	{
		rc.broadcast(BUILD_PASTR_CHANNEL, encodeMapLocation(location));
	}
	
	protected static void delayPastr(RobotController rc) throws GameActionException
	{
		rc.broadcast(BUILD_PASTR_CHANNEL, DO_NOT_BUILD_PASTR_VALUE);
	}
	
	protected static MapLocation getPastrLocation(RobotController rc) 
			throws GameActionException
	{
		int value = rc.readBroadcast(BUILD_PASTR_CHANNEL);
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
	
	private static int getDestinationIndex(MapLocation destination)
	{
		int value = encodeMapLocation(destination);
		for (int i = 0; i < DESTINATION_INDEX_LENGTH; i++)
		{
			if (destinationIndex[i] == value)
			{
				return i;
			}
			else if (destinationIndex[i] == 0)
			{
				destinationIndex[i] = value;
				return i;
			}
		}
		
		return -1;
	}
}
