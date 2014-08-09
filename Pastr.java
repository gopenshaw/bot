package bot;

import battlecode.common.*;

public class Pastr 
{
	public static void run(RobotController rc)
	{
		try
		{
			Communication.setPastrBuildingStatus(ConstructionStatus.COMPLETED, rc);
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
