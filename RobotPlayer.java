package bot;

import battlecode.common.*;

import java.util.*;

public class RobotPlayer {
	static Random rand;
	
	public static void run(RobotController rc) {
		
		if (rc.getType() == RobotType.SOLDIER)
		{
			Soldier.run(rc);
		}
		else if (rc.getType() == RobotType.HQ)
		{
			HQ.run(rc);
		}
		else if (rc.getType() == RobotType.PASTR)
		{
			Pastr.run(rc);
		}
		else if (rc.getType() == RobotType.NOISETOWER)
		{
			NoiseTower.run(rc);
		}
	}
}
