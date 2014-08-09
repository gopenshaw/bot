package bot;

import battlecode.common.*;

public class NoiseTower 
{
	public static void run(RobotController rc)
	{
		try 
		{
			Communication.setNoiseTowerBuildingStatus(ConstructionStatus.COMPLETED, rc);
		} 
		catch (GameActionException e) 
		{
			e.printStackTrace();
		}
		
		MapLocation location = rc.getLocation();
		
		while (true)
		{
			try
			{
				for (int i = 0; i < 8; i++)
				{
					MapLocation attackSquare = location.add(Direction.values()[i], 6);
					if (rc.canAttackSquare(attackSquare))
					{
						rc.attackSquare(attackSquare);
					}
					rc.yield();
					rc.yield();
				}
			}
			catch (GameActionException e) 
			{
				System.out.println("Noise Tower Exception " + e.getMessage());
				e.printStackTrace();
			}
		}
	}
}
