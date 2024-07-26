// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

//import javax.print.DocFlavor.INPUT_STREAM;

//import com.fasterxml.jackson.databind.ser.std.StdArraySerializers.IntArraySerializer;

import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.XboxController;



public class Robot extends TimedRobot {
  private XboxController driver = new XboxController(0);
  DriveTrain dt = new DriveTrain();
  Intake intake = new Intake();
  Climber climber = new Climber();
  Vision vision = new Vision();
  Auto auto = new Auto(dt, intake, vision);
  LED led = new LED(intake, climber, auto, this);
  //private long startTime = (long) 0;

  public Robot() {
  }

  @Override
  public void robotInit() {
    dt.init();
    auto.reset();
    intake.init();
    climber.init();
    led.init();
    vision.init();
    Common.debug("---robotInit() completed---");
  }

  @Override
  public void disabledInit() {}

  @Override
  public void disabledPeriodic() {
    if(driver.getBackButtonPressed()){
      dt.resetGyro();
    }
    if(driver.getStartButtonPressed()){
      dt.resetEncoder();
    }

    if(dt.getVelocity() == 0){
      dt.coastMode();
    }

    //Choose starting side color
    if(driver.getXButtonPressed()){
      auto.selectBlueSide();
    }
    if(driver.getBButtonPressed()){
      auto.selectRedSide();
    }

    //Choose between autos
    if(driver.getLeftBumperPressed()){
      auto.selectNextAuto();
    }

    // Choose if the Auto Delay is enabled
    if(driver.getRightBumperPressed()){
      auto.toggleAutoDelay();
    }

    //Common.debug(Double.toString(driver.getLeftY()));
    dt.debug();
    intake.debug();
    climber.debug();
    auto.debug();
  }



  @Override
  public void robotPeriodic() {
    led.update();
  }

  
  @Override
  public void autonomousInit() {
    dt.reset();
    auto.reset();
    intake.init();
  }

  @Override
  public void autonomousPeriodic() {
    auto.update();
  }

  @Override
  public void teleopInit() {
    dt.reset();
    intake.init();
    climber.init();
    Common.debug("---teleopInit() completed---");
  }

  @Override
  public void teleopPeriodic() {
    //Drivetrain

    double y = driver.getLeftY();
    double x = driver.getLeftX();
    y = Common.deadzone(y,.1);
    x = Common.deadzone(x,.1);

    if(x != 0 || y != 0){
      dt.teleopDrive(-y, -x);
    }


    /*if(driver.getBackButtonPressed()){
      dt.resetGyro();
    }*/

    dt.update();

    //Intake
    if(driver.getAButtonPressed()){
      intake.toggleIntake();
    }

    if(driver.getBButtonPressed()){
      intake.cancelShoot();
    }

    if(driver.getXButtonPressed()){
      intake.shooter.aimAmplifier();
      intake.prepToShoot();
    }

    if(driver.getYButtonPressed()){
      intake.shooter.aimSpeaker();
      intake.prepToShoot();
    }

    if(driver.getRightBumperPressed()){
      intake.shooter.aimYeeter();
      intake.prepToShoot();
    }

    /*if(driver.getPOV() == 0 && driver.getRightStickButtonPressed()){
      intake.shooter.addAmpPower();
    }

    if (driver.getPOV() == 180 && driver.getRightStickButtonPressed()){
      intake.shooter.decreaseAmpPower();
    }*/

    if(driver.getRightTriggerAxis() == 1){
      intake.takeShot();
    }

    if(driver.getBackButtonPressed()){
      intake.shooter.decreaseAmpPower();
    }

    if(driver.getStartButtonPressed()){
      intake.shooter.addAmpPower();
    }

    intake.update();

    // Climber

    if (driver.getRightStickButtonPressed()) {
      climber.enableClimber();
    }

    double rightY = driver.getRightY();
    rightY = Common.deadzone(rightY,.1);

    climber.moveClimber(-rightY);  //Power the climber.  Climber will only move if enabled.
    
    climber.update();
  }

  public boolean robotDisabled(){
    return isDisabled();
  }

}