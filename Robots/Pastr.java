package bot.Robots;

import battlecode.common.*;
import bot.Communication;
import bot.Enums.Status;

public class Pastr 
{
	public static void run(RobotController rc)
	{
		try
		{
			Communication.setPastrBuildingStatus(Status.COMPLETED, rc);
			while (true)
			{
				rc.yield();
			}
		}
		catch (Exception e)
		{
			System.out.println("Pastr Exception " + e.getMessage());
			e.printStackTrace();
		}
	}
}
