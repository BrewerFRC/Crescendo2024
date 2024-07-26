//ade by wyatt :D
package frc.robot;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;

public class Vision {
    private NetworkTable table;
    private NetworkTableEntry tx, ty, ta;       //tx is degrees off target left right, ty is degrees off target up down, ta is target area


    public void init(){
        table = NetworkTableInstance.getDefault().getTable("limelight");
        tx = table.getEntry("tx");
        ty = table.getEntry("ty");
        ta = table.getEntry("ta");
        NetworkTableInstance.getDefault().getTable("limelight").getEntry("pipeline").setNumber(0);
    }

    //returns turn error in degrees off target
    // + error means target is to the right, left is -
    //returms zero if no target
    public double getTurnError(){
        double x = tx.getDouble(0.0);
        return x;
    }



}
