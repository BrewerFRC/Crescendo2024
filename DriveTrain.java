package frc.robot;

import com.ctre.phoenix6.StatusCode;
import com.ctre.phoenix6.configs.OpenLoopRampsConfigs;
import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.controls.Follower;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.signals.NeutralModeValue;
import com.kauailabs.navx.frc.*;
import edu.wpi.first.wpilibj.SPI;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;


public class DriveTrain {
    private double MAX_DRIVE_TELE = .95;  //Drive motors will be constrained to this maximum 
    private double MAX_DRIVE_AUTO_LOW = .65;  //Drive motors will be constrained to this maximum (Low speed runs)
    private double MAX_DRIVE_AUTO_HIGH = .8;  //Drive motors will be constrained to this maximum (High speed runs)
    private double MIN_DRIVE_TELE = .25; //min power sent to the drive motors, when very smal powers are provided. Tune this to just barely move the robot.
    private double MIN_DRIVE_AUTO = .4; //min power sent to the drive motors, when very smal powers are provided. Tune this to just barely move the robot. (was .38)
    private double MAX_TURN = .5; //Was .4
    private double MIN_TURN = .25; 
    private double MOTOR_RAMP = .75;
    private double COUNTS_PER_INCH = 104.0/163.0;    //Encoder counts per inches

    private double kP_DIST = 1.0/35.0, kI_DIST = 0, kD_DIST = 0;    //kP: Full power at 70 inches
    private double kP_TURN = .0038, kI_TURN = 0, kD_TURN = 0;
    private double kP_HEADING = .015, kI_HEADING = 0, kD_HEADING = 0;
    private PIDController distPID;
    private PIDController rotatePID;
    private PIDController headingPID;
    private double targetDistance = 0;
    private double targetAngle = 0; //+-180

    private AHRS navx;

    private TalonFXConfiguration talonFXConfiguration = new TalonFXConfiguration();
    private final TalonFX frontL = new TalonFX(Constants.DT_FRONT_LEFT);  //Master motor of left side
    private final TalonFX frontR = new TalonFX(Constants.DT_FRONT_RIGHT); //Master motor of Right side 
    private final TalonFX backL = new TalonFX(Constants.DT_REAR_LEFT);    //Follower motor of left side
    private final TalonFX backR = new TalonFX(Constants.DT_REAR_RIGHT);   //Follower motor of right side
    private final DifferentialDrive drive = new DifferentialDrive(frontL::set, frontR::set);

    private double drivePower = 0;  //Used for Arcade and Distance drive power calculation
    private double drivePowerRamped = 0;    //Ramped drivePower
    private double turnPower = 0;   //Used for Arcade drive power calculation
    private double leftPower = 0;   //Used for Tank driver power calcuation (distance drive only)
    private double rightPower = 0;  //Used for Tank driver power calcuation (distance drive only)
    private boolean brakeMode = false;  //Remember if we are in Brake or Coast mode
    private boolean highSpeedAuto = false;
    private double leftOffset = 0;
    private double rightOffset = 0;


    public enum States {
        IDLE,   //Drivetrain reset ready for use
        TELEOP,        // For normal joystick-based driving
        DIST_DRIVE,    // Drive to a number of inches
        ROTATE,          // Rotate to a heading
        VISION_TRACK,  // Drive to a selected target, adjusting turn and drive speed. Stop if stalled.
        COMPLETE,      // Drive complete, no hold
    }

    private States state = States.TELEOP;
    

    public void init(){
        // Start the Nav-X
        navx = new AHRS(SPI.Port.kMXP);
        // PIDs for distance drive with heading hold and a turn for Rotate in place
        distPID = new PIDController(kP_DIST, kI_DIST, kD_DIST);
        headingPID = new PIDController(kP_HEADING, kI_HEADING, kD_HEADING);
        headingPID.enableContinuousInput(-180, 180);
        rotatePID = new PIDController(kP_TURN, kI_TURN, kD_TURN);
        //talonFXConfiguration.OpenLoopRamps.withDutyCycleOpenLoopRampPeriod(2);  //Set ramp to 2 seconds on configuration object 
        frontR.getConfigurator().apply(talonFXConfiguration); //Use config object to apply to this Talon FX motor
        frontL.getConfigurator().apply(talonFXConfiguration); //Use config object to apply to this Talon FX motor
        backR.getConfigurator().apply(talonFXConfiguration); //Use config object to apply to this Talon FX motor
        backL.getConfigurator().apply(talonFXConfiguration); //Use config object to apply to this Talon FX motor
        // Set front motors to be the Leaders and the back motors to Follow
        backL.setControl(new Follower(frontL.getDeviceID(), false));
        backR.setControl(new Follower(frontR.getDeviceID(), false));
        //Set right side to run inverted from the left side
        frontR.setInverted(true);   //Only need to invert to Leader
        // TO DO **** Set Ramping on the Lead motors

        //Start out in brake mode
        brakeMode();
    }

    public void reset(){
        targetDistance = 0;
        targetAngle = 0;
        drivePower = 0;
        drivePowerRamped = 0;
        turnPower = 0;
        distPID.reset();
        rotatePID.reset();
        headingPID.reset();
        brakeMode();
        resetEncoder();
        stopMotors();
        resetGyro();
        state = States.IDLE;
    }

    private void stopMotors(){
        frontL.stopMotor();
        frontR.stopMotor();
        // NOTE: Do not stop the follower motors, as it breaks the follow setup.
        //backL.stopMotor();
        //backR.stopMotor();
    }

    public void coastMode(){
        if (brakeMode) {
            frontL.setNeutralMode(NeutralModeValue.Coast);
            frontR.setNeutralMode(NeutralModeValue.Coast);
            backL.setNeutralMode(NeutralModeValue.Coast);
            backR.setNeutralMode(NeutralModeValue.Coast);
            brakeMode = false;  
        }
    }

    public void brakeMode(){
        if (!brakeMode) {
            frontL.setNeutralMode(NeutralModeValue.Brake);
            frontR.setNeutralMode(NeutralModeValue.Brake);
            backL.setNeutralMode(NeutralModeValue.Brake);
            backR.setNeutralMode(NeutralModeValue.Brake);
            brakeMode = true;
        }
    }

    public void teleopDrive(double drive, double turn){
        drivePower = drive;
        turnPower = turn;
        state = States.TELEOP;
        Common.debug("dt: State = TELEOP");
        //Common.debug((Double.toString(drivePower)));
    }

    public void selectHighSpeedAuto(){
        highSpeedAuto = true;
    }

    public void selectLowSpeedAuto(){
        highSpeedAuto = false;
    }

    public double getEncoderCounts(){
        return (((frontL.getPosition().getValue() + leftOffset) + (frontR.getPosition().getValue() + rightOffset))/2.0);
    }

    public double getInches(){
        return countsToInches(getEncoderCounts());
    }

    public double countsToInches(double encoderCount){
        return encoderCount/COUNTS_PER_INCH;
    }

    public double inchesToCounts(double inches){
        return COUNTS_PER_INCH*inches;
    }

    public double getVelocity(){
        return (frontL.getVelocity().getValueAsDouble() + frontR.getVelocity().getValueAsDouble()) / 2;
    }
    /*
    public double getVelocity(){
        return ((frontL.getVelocity().getValue() * 10/COUNTS_PER_INCH + frontR.getVelocity().getValue() * 10/COUNTS_PER_INCH)/2);   //Def needs tweaking 
    }
    */

    public void resetEncoder(){
        leftOffset = -frontL.getPosition().getValue();
        rightOffset = -frontR.getPosition().getValue();
        /*frontL.getConfigurator().setPosition(0);
        frontR.getConfigurator().setPosition(0);
        backL.getConfigurator().setPosition(0);
        backR.getConfigurator().setPosition(0);
        */
    }
    
    public void resetGyro(){
        navx.reset();
    }

    public void distDrive(double inches, double atHeading){
        resetEncoder();
        targetDistance = inches;
        distPID.setSetpoint(inches);
        distPID.reset();
        setHeading(atHeading);
        state = States.DIST_DRIVE;
        Common.debug("dt: State = DIST_DRIVE");
    }

    public void setHeading(double degrees){
        targetAngle = degreesToAngle(degrees);
        rotatePID.setSetpoint(targetAngle);
        headingPID.setSetpoint(targetAngle);
    }

    //Return target heading 0-360
    public double getHeading(){
        return angleToDegrees(targetAngle);
    }

    public void rotateTo(double degrees){
        setHeading(degrees);
        state = States.ROTATE;
        Common.debug("dt: State = ROTATE");
    }

    // get Yaw from navX, 0-360 degrees
    public double getGyroYaw() {
        /*if (navx.isMagnetometerCalibrated()) {
          // We will only get valid fused headings if the magnetometer is calibrated
          return navx.getFusedHeading();
        }*/
        return navx.getYaw();
      }
      
      //Convert a 0-360 heading to +- 180
      public double degreesToAngle(double degrees){
        double angle = degrees;
        if (angle > 180) { 
          angle = angle - 360;
        }
        return angle;
      }

      //Convert a +- 180 angle to 0-360 heading
      public double angleToDegrees(double angle){
        double degrees = angle;
        if (angle < 0){
            degrees = angle + 360;
        }
        return degrees;
      }

     // Read gyro and covert heading in +/- 180 degree reading
     public double getSignedAngle() {
        double heading = getGyroYaw();
        return degreesToAngle(heading);
      }

    /* Use this to bypass the update() method and just do a direct arcade drive
     * Only use this for testing purposes.   Use teleopDrive() function otherwise.
     */
    public void drive(double power, double turn) {
        drive.arcadeDrive(power, turn);
    }

    public boolean isComplete() {
        if (state == States.COMPLETE) {
          stopMotors();
          return true;
        } else{
          return false;
        }
      }


    public void update() {
        switch(state) {
            case TELEOP:
                targetAngle = getSignedAngle();
            break;

            case DIST_DRIVE:  // Calculate Left/Right motor powers to drive to target Distance while turning to target heading
                double error = Math.abs(targetDistance - getInches());
                // Are we close enough to call it complete
                if (error < 2) {
                  leftPower = 0.0;
                  rightPower = 0.0;
                  state = States.COMPLETE;
                  Common.debug("dt: State = COMPLETE " + (getInches()));
                } else {
                    // Determine drive power based on PID distance error
                    drivePower = distPID.calculate(getInches());
                    drivePowerRamped = Common.ramp(drivePowerRamped, drivePower, 1.0/50.0);
                    // Make drive motors run at least at minimum 
                    if (drivePowerRamped > -MIN_DRIVE_AUTO && drivePowerRamped < MIN_DRIVE_AUTO && drivePower != 0) {  //Are we below the minimums and attempting to drive?
                        if (drivePower > 0) { // moving forward
                            drivePowerRamped = MIN_DRIVE_AUTO;
                        } else { // moving backwards
                            drivePowerRamped = -MIN_DRIVE_AUTO;
                        }
                    }
                    // Keep drive power constrained within MAX
                    if(highSpeedAuto){
                        drivePowerRamped = Common.constrain(drivePowerRamped, -MAX_DRIVE_AUTO_HIGH, MAX_DRIVE_AUTO_HIGH);
                    } else if(!highSpeedAuto){
                        drivePowerRamped = Common.constrain(drivePowerRamped, -MAX_DRIVE_AUTO_LOW, MAX_DRIVE_AUTO_LOW);
                    }
                    // Determine turn error power correction, which will be used to slow down the side that is too fast
                    turnPower = headingPID.calculate(getSignedAngle());
                    // At most, the turn power is allowed to slow down the drivePower to zero for the side that is slowing down
                    if(drivePowerRamped > 0){
                        if(turnPower > drivePowerRamped){
                        turnPower = drivePowerRamped;
                        } 
                    } else {
                        if (turnPower < drivePowerRamped){
                            turnPower = drivePowerRamped;
                        }
                    }

                    // If turn power is needed to correct heading, use it to reduce power on one side of the robot.
                    // Note: turnPower positive means turn right.
                    if (turnPower > 0){  // Turn right
                        if (drivePowerRamped > 0) {  //Driving foward, slow right side
                            leftPower = drivePowerRamped;
                            rightPower = drivePowerRamped - turnPower;
                        } else {  //Driving backwards, slow left side
                            leftPower = drivePowerRamped + turnPower;
                            rightPower = drivePowerRamped;
                        }
                    } else {  // Turn Left
                        if (drivePowerRamped > 0) { //Driving forwrd, slow left side}
                            leftPower = drivePowerRamped + turnPower;
                            rightPower = drivePowerRamped;
                        } else {  //Driving backwards, slow right side 
                            leftPower = drivePowerRamped;
                            rightPower = drivePowerRamped - turnPower;
                        }
                    }
                }
            break;

          
            case ROTATE:
                drivePower = 0.0;
                turnPower = rotatePID.calculate(getSignedAngle());
                if (Math.abs(rotatePID.getPositionError()) < 2) {
                  turnPower = 0.0;
                  state = States.COMPLETE;
                  Common.debug("dt: State = COMPLETE");
                }
            break;

          
            case VISION_TRACK:
                state = States.TELEOP;
            break;

            case COMPLETE:
                drivePower = 0.0;
                turnPower = 0.0;
            break;
        } // End Select Case block
        
        // Now power the drivetrain
        if (state == States.DIST_DRIVE && highSpeedAuto) {
            //Limit drive power to max drive
            leftPower = Common.constrain(leftPower, -MAX_DRIVE_AUTO_HIGH, MAX_DRIVE_AUTO_HIGH);
            rightPower = Common.constrain(rightPower, -MAX_DRIVE_AUTO_HIGH, MAX_DRIVE_AUTO_HIGH);
            drive.tankDrive(leftPower, rightPower); 
        } else if (state == States.DIST_DRIVE && !highSpeedAuto) {
            //Limit drive power to max drive
            leftPower = Common.constrain(leftPower, -MAX_DRIVE_AUTO_LOW, MAX_DRIVE_AUTO_LOW);
            rightPower = Common.constrain(rightPower, -MAX_DRIVE_AUTO_LOW, MAX_DRIVE_AUTO_LOW);
            drive.tankDrive(leftPower, rightPower);              
        } else {
            drivePowerRamped = Common.ramp(drivePowerRamped, drivePower, 1.0/50.0);
            //If drivePower is not 0, then make sure it meets minimum power.
            if(drivePower != 0){    //If the robot is supposed to be moving
                if(drivePowerRamped > -MIN_DRIVE_TELE && drivePowerRamped < MIN_DRIVE_TELE){   //If current power is below minimums
                    if(drivePower > 0){  //If target is forwards
                        drivePowerRamped = MIN_DRIVE_TELE;
                    } else {    //If target is backwards
                        drivePowerRamped = -MIN_DRIVE_TELE;
                    }
                }
            }
            drivePowerRamped = Common.constrain(drivePowerRamped, -MAX_DRIVE_TELE, MAX_DRIVE_TELE);
            turnPower = Common.constrain(turnPower, -MAX_TURN, MAX_TURN);
            //Common.debug((Double.toString(drivePower)));
            drive.arcadeDrive(drivePowerRamped, turnPower);
        }

        debug();

        // Clear out the powers each robot cycle. Important to stop power if Teleop input lapses.
        drivePower = 0.0;
        turnPower = 0.0;
        leftPower = 0;
        rightPower = 0;
      }

      public void debug(){
        Common.dashStr("dt: State ", state.toString());
        Common.dashNum("dt: drivePowerRamped", drivePowerRamped);
        Common.dashNum("dt: drivePower PID", drivePower);
        Common.dashNum("dt: turnPower ", turnPower);
        Common.dashNum("dt: leftPower ", leftPower);
        Common.dashNum("dt: rightPower ", rightPower);
        Common.dashNum("dt: targetDistance ", targetDistance);
        Common.dashNum("dt: currentInches ", getInches());
        Common.dashNum("dt: targetAngle ", targetAngle);
        Common.dashNum("dt: currentAngle ", getSignedAngle());
        Common.dashNum("dt: Velocity", getVelocity());
        Common.dashBool("dt: High speed auto", highSpeedAuto);
        //Common.dashNum("dt: left motor position", frontL.getPosition().getValue());
        //Common.dashNum("dt: right motor position", frontR.getPosition().getValue());
      }
  

}
