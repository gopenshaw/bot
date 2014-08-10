package bot.Robots;

import battlecode.common.*;
import bot.Communication;
import bot.Enums.ConstructionStatus;

public class NoiseTower 
{
	final static int ATTACK_DISTANCE = 6;
	final static int DIRECTION_HOLD = 2;
	
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
					for (int j = 0; j < DIRECTION_HOLD; j++)
					{
						MapLocation attackSquare = location.add(Direction.values()[i], ATTACK_DISTANCE);
						if (rc.canAttackSquare(attackSquare))
						{
							rc.attackSquare(attackSquare);
						}
						rc.yield();
						rc.yield();
					}
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
