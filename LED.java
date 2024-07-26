//code by wyatt :D
package frc.robot;

import edu.wpi.first.wpilibj.AddressableLED;
import edu.wpi.first.wpilibj.AddressableLEDBuffer;
import frc.robot.Auto.AmpAndSquatStates;
import frc.robot.Auto.Modes;

public class LED{
    Intake intake;
    Climber climber;
    Auto auto;
    Robot robot;

    AddressableLED led = new AddressableLED(Constants.PWM_LED);
    // Reuse buffer
    // Default to a length of 30, start empty output
    // Length is expensive to set, so only set it once, then just update data
    AddressableLEDBuffer ledBuffer = new AddressableLEDBuffer(47);

    public LED(Intake intake, Climber climber, Auto auto, Robot robot){
        this.intake = intake;
        this.climber = climber;
        this.auto = auto;
        this.robot = robot;
    }
  
    //@Override
    public void init(){
        led.setLength(ledBuffer.getLength());
        // Set the data
        led.setData(ledBuffer);
        led.start();
    }

    public void intaking(){
        for (var i = 0; i < ledBuffer.getLength(); i++) {
        // Sets the specified LED to the RGB values for white (now non blinding!)
        ledBuffer.setRGB(i, 80,80,80);
        }
        led.setData(ledBuffer);
    }

    public void haveNote(){
        for (var i = 0; i < ledBuffer.getLength(); i++) {
        // Sets the specified LED to the RGB values for orange
        ledBuffer.setRGB(i,255,100,0);
        }
        led.setData(ledBuffer);
    }

    public void targetAmp(){ //done
        for (var i = 0; i < ledBuffer.getLength(); i++) {
        //Sets the specified LED to the RGB values for green
        ledBuffer.setRGB(i, 0, 255, 0);
        }
        led.setData(ledBuffer);
    }

    public void speaker() { //done
        for (var i = 0; i < ledBuffer.getLength(); i++) {
        //Sets the specified LED to the RGB values for purple
        ledBuffer.setRGB(i, 150, 0, 255);
        }
        led.setData(ledBuffer);
    }

    public void yeeter() { //done
        for (var i = 0; i < ledBuffer.getLength(); i++) {
        //Sets the specified LED to the RGB values for purple
        ledBuffer.setRGB(i, 255, 0, 127);
        }
        led.setData(ledBuffer);
    }

    public void climbMode() {
        for (var i = 0; i < ledBuffer.getLength(); i++) {
        //Sets the specified LED to the RGB values for blue
            ledBuffer.setRGB(i, 0, 0, 255);
        }
        led.setData(ledBuffer);
    }

    public void off() {
        for (var i = 0; i < ledBuffer.getLength(); i++) {
            ledBuffer.setRGB(i, 0, 0, 0);
        }
        led.setData(ledBuffer);
    }

    //For auto set up
    public void blueLights() {
        for (var i = 0; i < ledBuffer.getLength(); i++){
            if(auto.getRobotDirection() == "FRONT" && i > (int)(ledBuffer.getLength()/2)){
                ledBuffer.setRGB(i, 0, 0 , 255);
            } else if (auto.getRobotDirection() == "BACK" && i <= (int)(ledBuffer.getLength()/2)){
                ledBuffer.setRGB(i, 0, 0, 255);
            } else {
                ledBuffer.setRGB(i, 0, 0, 0);
            }
        }
        led.setData(ledBuffer);
    }

    public void redLights() {
        for (var i = 0; i < ledBuffer.getLength(); i++){
            if(auto.getRobotDirection() == "FRONT" && i > (int)(ledBuffer.getLength()/2)){
                ledBuffer.setRGB(i, 255, 0 , 0);
            } else if (auto.getRobotDirection() == "BACK" && i <= (int)(ledBuffer.getLength()/2)){
                ledBuffer.setRGB(i, 255, 0, 0);
            } else {
                ledBuffer.setRGB(i, 0, 0, 0);
            }
        }
        led.setData(ledBuffer);
    }
//amoungus
    public void update(){
        if(auto.getSideColor() == "RED" && robot.isDisabled()){
            redLights();
        } else {
            if(auto.getSideColor() == "BLUE" && robot.isDisabled()){
                blueLights();
            } else {
                if(climber.isClimbing()){
                    climbMode();
                } else {
                    if(intake.isIntaking()){
                        intaking();
                    } else {
                        if(intake.shooter.currentTarget() == "Amp" && !intake.isComplete()){
                            targetAmp();
                        } else {
                            if(intake.shooter.currentTarget() == "Speaker" && !intake.isComplete()){
                                speaker();
                            } else {
                                if(intake.shooter.currentTarget() == "Yeeter" && !intake.isComplete()){
                                    yeeter();
                                } else{
                                    if(intake.isNoteSensed()){
                                        haveNote();
                                    } else {                            
                                        off();
                                    }    
                                }
                            }
                        }
                    }
                }
            }
                    
        }
    }
}