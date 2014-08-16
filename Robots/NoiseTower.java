package bot.Robots;

import battlecode.common.*;
import bot.Communication;
import bot.Enums.Status;

public class NoiseTower 
{
	final float LONG_ATTACK_DISTANCE = 10;
	final float SHORT_ATTACK_DISTANCE = 1;
	final float NUMBER_OF_STEPS = 4;
	final float STEP_HOLD = 1;
	
	public void run(RobotController rc)
	{
		try 
		{
			Communication.setNoiseTowerBuildingStatus(Status.COMPLETED, rc);
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
				for (int i = 0; i < 4; i++)
				{
					for (int j = 0; j < NUMBER_OF_STEPS; j++)
					{
						for (int k = 0; k < STEP_HOLD; k++)
						{
							int attackDistance = Math.round(LONG_ATTACK_DISTANCE -
									(LONG_ATTACK_DISTANCE - SHORT_ATTACK_DISTANCE) / NUMBER_OF_STEPS * j);
							MapLocation attackSquare = location.add(Direction.values()[i * 2], attackDistance);
							if (rc.canAttackSquare(attackSquare))
							{
								rc.attackSquare(attackSquare);
							}
							rc.yield();
							rc.yield();
						}
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
