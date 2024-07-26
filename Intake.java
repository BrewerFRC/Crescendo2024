package frc.robot;

import edu.wpi.first.units.Time;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.motorcontrol.Spark;

public class Intake {
    private Spark lowerMotor = new Spark(Constants.INTAKE_LOWER);
    private Spark upperMotor = new Spark(Constants.INTAKE_UPPER);
    Shooter shooter = new Shooter();

    public enum States {
        INIT,   //The intake needs to be initialized
        IDLE,   //The intake is ready for use and is in active driving mode
        INTAKING,   //Intake is trying to pick up note and will go idle when done
        PREP_TO_SHOOT,  //Backs note into the intake.
        WAIT_FOR_SHOTER,    //Waits for shooter to spin up.
        READY_TO_SHOOT, 	//Waiting to shoot.
        THROW_AT_SHOOTER,   //Throws note at shooter.
        SHOOTING;   //Waiting for note to clear out
    }
    private States state = States.INIT;
    private double powerUpper = 0; 
    private double powerLower = 0;
    private boolean hasNote;  // Has note is now just mimicking the sensor....see if we can work without latching this varialbe.
    private DigitalInput irSensor = new DigitalInput(Constants.MAG_IR);
    private Long startTime;
    
    public void init(){
        //state = States.INIT; 
        state = States.IDLE;
        Common.debug("Intake: State = IDLE");
        shooter.init();
    }

    public void startIntake(){
        //if (state == States.IDLE && !hasNote){
        if (state == States.IDLE && !isNoteSensed()){
                state = States.INTAKING;
            Common.debug("Intake: State = INTAKING");

        }
    }

    public void stopIntake(){
        if (state == States.INTAKING){
            state = States.IDLE;
            Common.debug("Intake: State = IDLE");
        }
    }

    public void toggleIntake(){
        if (state == States.IDLE){
            startIntake();
        } else if (state == States.INTAKING){
            stopIntake();
        }
    }

    public boolean isIntaking(){
        if(state == States.INTAKING){
            return true;
        } else {
            return false;
        }
    }

    public void prepToShoot(){
        //if (state == States.IDLE && hasNote){
        if (state == States.IDLE && isNoteSensed()){
            state = States.PREP_TO_SHOOT;
            Common.debug("Intake: State = PREP_TO_SHOOT");
        }
    }

    public void autoPrepToShoot(){
        //if (state == States.IDLE && hasNote){
        if (state == States.IDLE){
            state = States.PREP_TO_SHOOT;
            Common.debug("Intake: State = PREP_TO_SHOOT");
        }
    }

    public void cancelShoot(){
        if (state == States.PREP_TO_SHOOT || state == States.READY_TO_SHOOT /*|| state == States.WAIT_FOR_SHOOTER */){
            state = States.INTAKING;
            shooter.stop();
        }
    }

    /*public void togglePrepToShoot(){
        if (state == States.IDLE){
            prepToShoot();
        } else {
            cancelShoot();
        }
    }*/

    public void takeShot(){
        if(state == States.READY_TO_SHOOT){ 
            state = States.THROW_AT_SHOOTER;
            Common.debug("Intake: State = THROW_AT_SHOOTER");
        }
    }


    // Is note visible to sensor
    public boolean isNoteSensed(){
        if (irSensor.get() == true){
            return true;
        } else {
            return false;
        }
    }

    // If we are "ready to shoot" it means the note has backed away from the sensor and is in the intake.
    public boolean isNoteInIntake(){
        //if(isNoteSensed() == false && hasNote){
        if(state == States.READY_TO_SHOOT){
            return true;
        } else {
            return false;
        }
    }

    public boolean isComplete(){
        if(state == States.IDLE){
            return true;
        } else {
            return false;
        }
    }

    public boolean readyToShoot(){
        if(state == States.READY_TO_SHOOT){
            return true;
        } else {
            return false;
        }
    }

    public void update(){
        switch (state) {
            /*case INIT: {
                powerLower = 0;
                powerUpper = 0;
                //hasNote = isNoteSensed();
                state = States.IDLE;
                Common.debug("Intake: State = IDLE");
            break;
            } */

            case IDLE: {
                powerUpper = 0;
                powerLower = 0;
            break;
            }

            case INTAKING: {
                powerUpper = 0.6;
                powerLower = -1.0;
                if(isNoteSensed()){
                    //hasNote = true;
                    state = States.IDLE;
                    Common.debug("Intake: State = IDLE");
                }
            break;
            }

            case PREP_TO_SHOOT: {
                powerUpper = -0.5;
                powerLower= 0.5;
                shooter.prepToShoot();  //Tell shooter to back up slowly
//                if (isNoteInIntake()){
                if (!isNoteSensed()){ //We'll run backwards until we no longer see the note
                    shooter.spinUp();
                    state = States.WAIT_FOR_SHOTER;
                    Common.debug("Intake: State = WAIT_FOR_SHOOTER");
                }
            break;
            }
            case WAIT_FOR_SHOTER: {
                powerLower = 0;
                powerUpper = 0;
                if(shooter.isReadyToShoot()){
                    state = States.READY_TO_SHOOT;
                    Common.debug("Intake: State = READY_TO_SHOOT");
                }
            break;
            }

            case READY_TO_SHOOT: {
                powerUpper = 0;
                powerLower = 0;
            break;
            }

            case THROW_AT_SHOOTER: {
                powerUpper = 1;
                powerLower = -1;
                startTime = Common.time();
                state = States.SHOOTING;
                shooter.shoot();
                Common.debug("Intake: State = SHOOTING");
            break;
            }

            case SHOOTING: {
                powerUpper = 1;
                powerLower = -1;
                if(shooter.isIdle()) {  //Wait for shooter to finish its shot
                //if(Common.time() - startTime > 300){
                    state = States.IDLE;
                    powerUpper = 0;
                    powerLower = 0;
                    //shooter.stop();
                    //hasNote = false;
                }
            break;
            }
        }
        
//******** set upper and lower powers
        upperMotor.set(powerUpper);
        lowerMotor.set(powerLower);
        shooter.update();
        debug();

    }

    public void debug(){
        Common.dashStr("Intake: state", state.toString());
        Common.dashBool("Intake: is note sensed", isNoteSensed());
        Common.dashBool("Intake: hasNote (not using this value)", hasNote);
        shooter.debug();
    }
}
