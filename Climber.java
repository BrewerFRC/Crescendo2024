package frc.robot;

import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.signals.NeutralModeValue;

import edu.wpi.first.wpilibj.DigitalInput;
//import edu.wpi.first.wpilibj.drive.DifferentialDrive;
//import edu.wpi.first.wpilibj.motorcontrol.PWMSparkMax;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;


public class Climber {
    DigitalInput limitSwitch = new DigitalInput(Constants.DIO_CLIMBER_LIMIT);
    private final TalonFX climbMotor = new TalonFX(Constants.CAN_CLIMBER);  
    private double climbPower = 0; //positive means extend negative retract (range: +1, -1)
                                   //update() applies the power and then clear it each robot cycle.
    private double MAX_POWER = 0.6;  // Max power allowed to send to climber motor
    
    public enum States{
        INIT,       //retract to limit switch
        IDLE,       //ready to climb
        CLIMBING;    //in climb mode, joystick will extend/retract
    }
    private States state = States.INIT; 

    public void init() {
        state = States.INIT;
        climbMotor.setInverted(true);
        climbMotor.setNeutralMode(NeutralModeValue.Brake);

    }

    public boolean atLimit() {
        if (limitSwitch.get()) {    //When true, it is not pressed
            return false;
        } else {                    //When false, it is pressed
            return true;
        }
    }

    // When enabled you can move the climber using moveClimber().
    public void enableClimber(){
        if (state == States.IDLE){
            state = States.CLIMBING;
        }
    }

    public void disableClimber() {
        if (state == States.CLIMBING) {
            state = States.IDLE;
        }
    }

    public boolean isClimbing(){
        if(state == States.CLIMBING){
            return true;
        } else {
            return false;
        }
    }

    // if climber is in CLIMBING state, then you can set a power to extend (+) or retract (-).
    // Update logic will apply this power and then set it back to zero.  So you need to call this
    // each robot cycle for continuous movement.
    public void moveClimber(double power) {
        if (state == States.CLIMBING) {
            climbPower = power;
        }
    }

    public void update() {
        switch(state){
            case INIT:
                if (atLimit()) {  //If at limit Stop retract
                    climbPower = 0;
                    state = States.IDLE;
                } else {
                    climbPower = -0.1;
                }
            break;

            case IDLE:  //Climber is initialized but not enabled to climb
                climbPower = 0;
            break;

            case CLIMBING:
                if (atLimit()) {            //If at limit switch...
                    if (climbPower < 0) {   //...don't allow further retraction
                        climbPower = 0;                        
                    }
                }
            break;
        }
        //Send the desired power level to the motor, constrained to MAX_POWER
        climbPower = Common.constrain(climbPower,-MAX_POWER, +MAX_POWER);
        climbMotor.set(climbPower);
        debug();
        //Clear the power each robot cycle, so it will stop moving if no new action sets a power level next robot cycle.
        climbPower = 0; 
    }

    public void debug(){
        SmartDashboard.putNumber("Climber: climbPower", climbPower);
        SmartDashboard.putString("Climber: state", state.toString());
        SmartDashboard.putBoolean("Climber: atLimit", atLimit());
        
    }
    
}