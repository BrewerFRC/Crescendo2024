package frc.robot;

import edu.wpi.first.wpilibj.motorcontrol.Spark;

public class Shooter {
    private final double BACK_OUT_POWER_UPPER = 0.3;
    private final double BACK_OUT_POWER_LOWER = 0.3;
    private final double AIM_AMPLIFIER_POWER_UPPER = -0.27;  //
    private final double AIM_AMPLIFIER_POWER_LOWER = -0.35; //was .36, .34
    private final double AIM_SPEAKER_POWER_UPPER = -0.73; //Was -.68, -.69, -.70, -.71
    private final double AIM_SPEAKER_POWER_LOWER = -0.68; //Was -.63, -.64, -.65, -.66
    private final double AIM_TRAP_POWER_UPPER = -0.3;
    private final double AIM_TRAP_POWER_LOWER = -0.3;
    private final double FULL_SEND_LOWER = -1;
    private final double FULL_SEND_UPPER = -1;
    private Spark lowerMotor = new Spark(Constants.SHOOTER_MOTOR_LOWER);
    private Spark upperMotor = new Spark(Constants.SHOOTER_MOTOR_UPPER);

    public enum States {
        IDLE,           //Shooter motors are off and ready to be used
        PREP_TO_SHOOT,      //Shooter backs note into intake
        SPIN_UP,        //Bringing shooter motor up to speed for desired target.  transitions to READY_TO_SHOOT
        READY_TO_SHOOT,     //Shooter at speed, ready to shoot
        SHOOTING;       //shooting note and will go idle when done
    }
    
    private States state = States.IDLE;
    private double powerUpper = 0; //this is the power that will be sent...
    private double powerLower = 0; //...to the shooter motors by the update method
    private double aimPowerUpper = 0; //this is the power based on which field...
    private double aimPowerLower = 0; //...element is being targeted
    private Long startTime = (long) 0;
    private String target = "Amp";
    private double ampPowerOffset = 0;

    public void init(){
        state = States.IDLE;
        aimAmplifier();
    }

    public void addAmpPower(){
        if(ampPowerOffset < .02){
            ampPowerOffset = ampPowerOffset + .01; 
        }
    }

    public void decreaseAmpPower(){
        if(ampPowerOffset > -.02){
            ampPowerOffset = ampPowerOffset - .01;
        }
    }


    public void prepToShoot() {
        if (state == States.IDLE){
            state = States.PREP_TO_SHOOT;
            Common.debug("Shooter: State = PREP_TO_SHOOT");
        }
    }

    public void spinUp() {
        if (state == States.PREP_TO_SHOOT){
            startTime = Common.time();
            state = States.SPIN_UP;
            Common.debug("Shooter: State = SPIN_UP");
        }
    }

    public void stop() {
        state = States.IDLE;
        Common.debug("Shooter: State = IDLE");
    }

    public void shoot() {
        if (state == States.READY_TO_SHOOT) {
            startTime = Common.time();
            state = States.SHOOTING;
            Common.debug("Shooter: State = SHOOTING");
        }
    }

    public boolean isReadyToShoot(){
        if (state == States.READY_TO_SHOOT){
            return true;
        } else {
            return false;
        }
    }

    public boolean isIdle(){
        if (state == States.IDLE){
            return true;
        } else {
            return false;
        }
    }

    public void aimAmplifier(){
        aimPowerUpper = AIM_AMPLIFIER_POWER_UPPER - ampPowerOffset;
        aimPowerLower = AIM_AMPLIFIER_POWER_LOWER - ampPowerOffset;
        target = "Amp";
    }

    public void aimSpeaker(){
        aimPowerUpper = AIM_SPEAKER_POWER_UPPER;
        aimPowerLower = AIM_SPEAKER_POWER_LOWER;
        target = "Speaker";
    }

    public void aimTrap(){
        aimPowerUpper = AIM_TRAP_POWER_UPPER;
        aimPowerLower = AIM_TRAP_POWER_LOWER;
        target = "Trap";
    }

    public void aimYeeter(){  // Shoots with Max power
        aimPowerUpper = FULL_SEND_UPPER;
        aimPowerLower = FULL_SEND_LOWER;
        target = "Yeeter";
    }

    public String currentTarget() {
         return target;
    }

    public void update(){
        switch (state) {
            case IDLE: 
                powerLower = 0;
                powerUpper = 0;
                break;
            
            case PREP_TO_SHOOT: 
                powerUpper = BACK_OUT_POWER_UPPER;
                powerLower = BACK_OUT_POWER_LOWER;
                break;
            
            case SPIN_UP: 
                powerUpper = aimPowerUpper;
                powerLower = aimPowerLower;
                if (Common.time() >= startTime + 700){ //Was 450
                    state = States.READY_TO_SHOOT;
                    Common.debug("Shooter: State = READY_TO_SHOOT");
                }
                break;
            
            case READY_TO_SHOOT: 
                powerUpper = aimPowerUpper;
                powerLower = aimPowerLower;
                break;
            
            case SHOOTING: 
                powerUpper = aimPowerUpper;
                powerLower = aimPowerLower;
                if (Common.time() >= startTime + 1000){
                    state = States.IDLE;
                    Common.debug("Shooter: State = IDLE");
                }
                break;
            

        } 
        upperMotor.set(powerUpper);
        lowerMotor.set(powerLower);
    }

    void debug(){
        Common.dashStr("Shooter: state", state.toString());
        Common.dashNum("Shooter: powerUpper", powerUpper);
        Common.dashNum("Shooter: powerLower", powerLower);
        Common.dashStr("Shooter: target", target);
        //Common.dashNum("time", Common.time());
        //Common.dashNum("Shooter: time", startTime + 250);
        Common.dashNum("Shooter: amp power upper", aimPowerUpper);
    }
} 