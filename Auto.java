package frc.robot;

public class Auto {
    DriveTrain dt;
    Intake intake;
    Vision vision;
    int DELAY = 250; //Auto delay in miliseconds
    long delayTime;
    boolean autoDelay = false; // When true, Auto start is delayed

    private boolean delayComplete(){
        if (Common.time() > delayTime){
            return true;
        } else {
            return false;
        }
    }

    public void startDelayTimer(){
        if(autoDelay){
            delayTime = Common.time() + DELAY;
        } else {
            delayTime = 0;
        }
    }

    public void toggleAutoDelay(){
        autoDelay = !autoDelay;
    }

    public enum AmpAndSquatStates{
        START,  //Initiate drive to amp
        DRIVE_TO_AMP1,  //Drive to amp to drop off initial hoop
        PREP_SHOT1, //Prepare to shoot
        START_SHOT1, //Score  the hoop
        COMPLETE_SHOT1,
        DRIVE_TO_HOOP1, //Drive to 1st hoop on the ground and intake
        DRIVE_TO_AMP2,  //When intake is done drive back to amp
        PREP_SHOT2, //Prepare to shoot
        START_SHOT2,
        COMPLETE_SHOT2, //Score second hoop
        DRIVE_TO_HOOP2, //Drive towards the final hoop.
        SQUAT;  //Auto finished (Complete)
    }

    public enum SpeakUpStates{
        START,  //Initiate shoot to speaker
        START_SHOT1, //Start the shooter
        COMPLETE_SHOT1, //Score the hoop
        DRIVE_TO_HOOP1, //Drive to 1st hoop on the ground and intake
        DRIVE_TO_SPEAK,  //When intake is done drive back to speaker
        START_SHOT2, //Start the shooter
        COMPLETE_SHOT2, //Score second hoop
        DRIVE_TO_HOOP2, //Drive towards the final hoop.
        SPEAK;  //Auto finished (Complete)
    }

    public enum ThreePieceWanderStates{
        START,
        SHOOT_READY,
        START_SHOT1,
        DRIVE_TO_HOOP1,
        TURN_TO_HOOP1,
        FINISH_DRIVE1,
        DRIVE_TO_SPEAKER1,
        TURN_TO_SPEAKER1,
        FINISH_DRIVE_TO_SPEAKER1,
        START_SHOT2,
        COMPLETE_SHOT2,
        DRIVE_TO_HOOP2,
        TURN_TO_HOOP2,
        FINISH_DRIVE2,
        DRIVE_TO_SPEAKER2,
        TURN_TO_SPEAKER2,
        FINISH_DRIVE_TO_SPEAKER2,
        START_FINAL_SHOT,
        COMPLETE;
    }

    
    public enum SpeakerSweepStates{
        START,  //Initiate shoot to speaker
        SHOOT_READY, // Waits for the shooter to spin up
        TAKE_SHOT1, //Initiate shot (with delay toggle) and then drive
        START_DRIVE_TO_MID_HOOP, //Drive to the hoop and intake
        START_DRIVE_BACK_TO_SPEAKER1, //Drive back to the speaker after intaking hoop
        TAKE_SHOT2, //Initiate second shot
        START_DRIVE_TO_TOP_HOOP, //Drive to the top-most hoop and intake
        TURN_TO_TOP_HOOP,   //Initiate a turn towards the top hoop
        START_DRIVE_BACK_TO_SPEAKER2, //Drive back to the speaker after intaking hoop
        TURN_TO_SPEAKER2,   //Initiate a turn towards the speaker
        TAKE_SHOT3, //Initiate third shot
        START_DRIVE_TO_LOWER_HOOP, //Drive to the lowest hoop and intake
        TURN_TO_LOWER_HOOP, //Initiate a turn towards the lowest hoop
        START_DRIVE_BACK_TO_SPEAKER3, //Drive back to the speaker after intaking
        TURN_TO_SPEAKER3,   //Initiate a turn towards the speaker
        TAKE_SHOT_FINAL; //Initiate the final shot
    }

    public enum TaxiStates{
        START, //Prepo shot
        SHOOT_READY, //Waits for the shooter to spin up
        TAKE_SHOT1, //Shoot shot (with delay toggle)
        MOVE_OUT_THE_WAY, //Drive out of the way
        TAXI, //Drive out of the starting area
        COMPLETE;
    }

    public enum Modes{
        AMP_AND_SQUAT,
        SPEAKER_SWEEP,
        TWO_PIECE_WANDER,
        THREE_PIECE_WANDER,
        TAXI;
    }

    public enum SideColor{  //Used to determine what side of the field you are on
        RED,
        BLUE;
    }

    public enum RobotDirection{ //Robot's orientation to the field
        FRONT,
        BACK;
    }

    private AmpAndSquatStates squatState = AmpAndSquatStates.START;
    private SpeakUpStates speakUpState = SpeakUpStates.START;
    private ThreePieceWanderStates threePieceWanderState = ThreePieceWanderStates.START;
    private SpeakerSweepStates sweepState = SpeakerSweepStates.START;
    private TaxiStates taxiState = TaxiStates.START;
    private RobotDirection robotDirection = RobotDirection.FRONT;
    private Modes autoMode = Modes.SPEAKER_SWEEP; //was AMP_AND_SQUAT
    private Long startTime;
    public SideColor sideColor = SideColor.RED;

    public Auto(DriveTrain dt, Intake intake, Vision vision){
        this.dt = dt;
        this.intake = intake;
        this.vision = vision;
    }

    public void reset(){
        dt.reset();
        squatState = AmpAndSquatStates.START;
        sweepState = SpeakerSweepStates.START;
        threePieceWanderState = ThreePieceWanderStates.START;
        taxiState = TaxiStates.START;
        //autoMode = Modes.AMP_AND_SQUAT;
        //intake.preloadNote();
    }

    public void selectAmpAndSquat(){
        autoMode = Modes.AMP_AND_SQUAT;
        robotDirection = RobotDirection.FRONT;
        dt.selectLowSpeedAuto();
    }

    public void selectSpeakerSweep(){
        autoMode = Modes.SPEAKER_SWEEP;
        robotDirection = RobotDirection.BACK;
        dt.selectLowSpeedAuto();
    }

    public void selectTwoPieceWander(){
        autoMode = Modes.TWO_PIECE_WANDER;
        robotDirection = RobotDirection.BACK;
        dt.selectHighSpeedAuto();
    }

    public void selectThreePieceWander(){
        autoMode = Modes.THREE_PIECE_WANDER;
        robotDirection = RobotDirection.BACK;
        dt.selectHighSpeedAuto();
    }

    public void selectTaxi(){
        autoMode = Modes.TAXI;
        robotDirection = RobotDirection.BACK;
        dt.selectLowSpeedAuto();
    }

    public void selectRedSide(){    //Select red side of the field
        sideColor = SideColor.RED;
    }

    public void selectBlueSide(){   //Select blue side of the field
        sideColor = SideColor.BLUE;
    }

    public String getSideColor(){
        return sideColor.toString();
    }

    public String getAutoMode(){
        return autoMode.toString();
    }

    public void selectNextAuto(){
        if(autoMode == Modes.AMP_AND_SQUAT){
            selectSpeakerSweep();
        } else {
            if(autoMode == Modes.SPEAKER_SWEEP){
                selectThreePieceWander();
            } else {
                if(autoMode == Modes.THREE_PIECE_WANDER){
                    selectTwoPieceWander();
                } else {
                    if(autoMode == Modes.TWO_PIECE_WANDER){
                        selectTaxi();
                    } else {
                        if(autoMode == Modes.TAXI){
                            selectAmpAndSquat();
                        }
                    }
                }
            }
        }
    }

    public String getRobotDirection(){
        return robotDirection.toString();
    }

    //Adjusts heading using vision tracking error
    private void visionCorrect(){
        double error = vision.getTurnError() * .02;
        dt.setHeading(dt.getHeading() + error);
    }

    // Update method for ampAndSquat auto
    public void ampAndSquat(){
        switch(squatState) {
            case START:
            if(sideColor == SideColor.RED){
            dt.distDrive(-64, 0);  //Begin a drive to the amp 
            } else {
                dt.distDrive(-72, 0);
            }
            squatState = AmpAndSquatStates.DRIVE_TO_AMP1;
            Common.debug("AUTO: State = DRIVE_TO_AMP1");
            break;

            case DRIVE_TO_AMP1:
            if (Math.abs(dt.getInches()) > (sideColor == SideColor.RED ? 43 : 39)){   //After 37 inches start the turn towards the amp
                if(sideColor == SideColor.RED){
                    dt.setHeading(90);
                } else {
                    dt.setHeading(-90);
                } 
                //intake.shooter.aimAmplifier();    //Aim at the amp
                squatState = AmpAndSquatStates.PREP_SHOT1;
                Common.debug("AUTO: State = PREP_SHOT1");
            }
            break;

            case PREP_SHOT1:
                if(dt.isComplete()){
                    intake.autoPrepToShoot();   //Prep the shot
                    squatState = AmpAndSquatStates.START_SHOT1;
                    Common.debug("AUTO: State = START_SHOT1");
                }
            break;

            case START_SHOT1:
            if(dt.isComplete() && intake.readyToShoot()){    //If the distance drive has been completed
                intake.takeShot();
                squatState = AmpAndSquatStates.COMPLETE_SHOT1;
                Common.debug("AUTO: State = COMPLETE_SHOT1");
            }
            break;

            case COMPLETE_SHOT1:
            if(intake.isComplete()){   //If 1 second has passed after starting shot
                if (sideColor == SideColor.RED){
                    dt.distDrive(59, 90);    //Drive 58 inches to hoop
                } else {
                    dt.distDrive(58, -90);    //Drive 58 inches to hoop
                }
                squatState = AmpAndSquatStates.DRIVE_TO_HOOP1;
                Common.debug("AUTO: State = DRIVE_TO_HOOP1");
            }
            break;
            

            case DRIVE_TO_HOOP1:
            if(sideColor == SideColor.RED){
                if(dt.getInches() > 13){   //After (was 11) inches turn so intake is facing hoop
                    dt.setHeading(180);
                }
            } else {
                if (dt.getInches() > 17){
                    dt.setHeading(180);
                }
            }
            if(dt.getInches() > 25){
                intake.startIntake();  //If close enough, toggle intake
            }
            if(dt.isComplete() && intake.isComplete()){
                if (sideColor == SideColor.RED){
                    dt.distDrive(-59, 180);  //Begin drive to amp
                } else {
                    dt.distDrive(-56, -180);  //Begin drive to amp
                }
                squatState = AmpAndSquatStates.DRIVE_TO_AMP2;
                Common.debug("AUTO: State = DRIVE_TO_AMP2");
            }
            break;

            case DRIVE_TO_AMP2: //Drive to amp and initiate shot
                if(dt.getInches() < (sideColor == SideColor.RED ? -13 : -17)){    //Turn towards amp after 13 inches of driving
                    if(sideColor == SideColor.RED){
                        dt.setHeading(90);
                    } else {
                        dt.setHeading(-90);
                    }
                    //intake.shooter.aimAmplifier();    //Aim at the amp
                    squatState = AmpAndSquatStates.PREP_SHOT2;
                    Common.debug("AUTO: State = PREP_SHOT2");
                }
            break;

            case PREP_SHOT2:
                if(dt.isComplete()){
                    intake.autoPrepToShoot();
                    squatState = AmpAndSquatStates.START_SHOT2;
                    Common.debug("AUTO: START_SHOT2");
                }
            break;

            case START_SHOT2:
                if(intake.readyToShoot()){
                    intake.takeShot();
                    squatState = AmpAndSquatStates.COMPLETE_SHOT2;
                    Common.debug("AUTO: State = COMPLETE_SHOT2");
                }
            break;

            case COMPLETE_SHOT2:
            if(intake.isComplete()){   //Waiting for shot to be completed
                if(sideColor == SideColor.RED){
                    dt.distDrive(250, 90); //Begin the drive to the final hoop
                } else {
                    dt.distDrive(250, -90); //Begin the drive to the final hoop
                }
                squatState = AmpAndSquatStates.DRIVE_TO_HOOP2;
                Common.debug("AUTO: State = DRIVE_TO_HOOP2");
            }
            break;

            case DRIVE_TO_HOOP2:
                if(dt.getInches() > (sideColor == SideColor.RED ? 0 : 7)){    //maybe Needs a blue color alternative heading
                    if (sideColor == SideColor.RED){
                        dt.setHeading(180);
                    } else {
                        dt.setHeading(-180);
                    }
                    squatState = AmpAndSquatStates.SQUAT;
                    Common.debug("AUTO: State = SQUAT");
                }
            break;

            case SQUAT: //Squatter's rights
                visionCorrect();
                if(dt.getInches() > 215){   //After driving 250 inches
                    intake.startIntake();  //Toggle the intake when close to the hoop
                }
            break;

        }
    }

    public void speakerSweep(){
        switch(sweepState){
            case START:
                intake.shooter.aimSpeaker();
                intake.autoPrepToShoot();
                sweepState = SpeakerSweepStates.SHOOT_READY;
                Common.debug("AUTO: State = SHOOT_READY");
            break;

            case SHOOT_READY:
                if (intake.readyToShoot()){
                    startDelayTimer();
                    sweepState = SpeakerSweepStates.TAKE_SHOT1;
                    Common.debug("AUTO: State = TAKE_SHOT1");
                }
            break;

            case TAKE_SHOT1:
                if (delayComplete()){
                    intake.takeShot();
                    sweepState = SpeakerSweepStates.START_DRIVE_TO_MID_HOOP;
                    Common.debug("AUTO: State = START_DRIVE_TO_MID_HOOP");
                }
            break;

            case START_DRIVE_TO_MID_HOOP:
                if(intake.isComplete()){
                    dt.distDrive(55, 0);
                    sweepState = SpeakerSweepStates.START_DRIVE_BACK_TO_SPEAKER1;
                    Common.debug("AUTO: State = START_DRIVE_BACK_TO_SPEAKER1");
                }
            break;

            case START_DRIVE_BACK_TO_SPEAKER1:
                if(dt.getInches() > 30){
                    intake.startIntake();
                }
                if(dt.isComplete() && intake.isComplete()){
                    dt.distDrive(-55, 0);
                    intake.autoPrepToShoot();
                    sweepState =SpeakerSweepStates.TAKE_SHOT2;
                    Common.debug("AUTO: State = TAKE_SHOT2");
                }
            break;

            case TAKE_SHOT2:
                if(dt.isComplete()){
                    intake.takeShot();
                    sweepState = SpeakerSweepStates.START_DRIVE_TO_TOP_HOOP;
                    Common.debug("AUTO: State = DRIVE_TO_TOP_HOOP");
                }
            break;

            case START_DRIVE_TO_TOP_HOOP:
                if(intake.isComplete()){
                    //if (sideColor == SideColor.RED){
                    dt.distDrive(78, -43); 
                    /* } else {
                        dt.distDrive(78, 43);
                    }*/
                    intake.startIntake(); 
                    sweepState = SpeakerSweepStates.START_DRIVE_BACK_TO_SPEAKER2;
                    Common.debug("AUTO: State = START_DRIVE_BACK_TO_SPEAKER2");
                }
            break;
            
            /*case TURN_TO_TOP_HOOP:
                if(dt.getInches() > 0){ //Correct distance is?
                    dt.setHeading(-43);  //Correct heading?
                    intake.startIntake(); //Start later?
                    sweepState = SpeakerSweepStates.START_DRIVE_BACK_TO_SPEAKER2;
                    Common.debug("AUTO: State = START_DRIVE_BACK_TO_SPEAKER2");
                }
            break;*/
                

            case START_DRIVE_BACK_TO_SPEAKER2:
                if(dt.isComplete() && intake.isComplete()){
                    //if (sideColor == SideColor.RED){
                    dt.distDrive(-78, -50); 
                    /* } else {
                        dt.distDrive(-78, 50);
                    }*/
                    sweepState = SpeakerSweepStates.TURN_TO_SPEAKER2;   //TURN_TO_SPEAKER instead?
                    Common.debug("AUTO: State = TURN_TO_SPEAKER2");
                }
            break;

            case TURN_TO_SPEAKER2:  //TURN_TO_SPEAKER instead?
                if (Math.abs(dt.getInches()) > 54){ //Correct distance is?
                    dt.setHeading(0);
                    intake.autoPrepToShoot();
                    sweepState = SpeakerSweepStates.TAKE_SHOT3;
                    Common.debug("AUTO: State = TAKE_SHOT3");
                }
            break;

            case TAKE_SHOT3:
                if(dt.isComplete() && intake.readyToShoot()){
                    intake.takeShot();
                    sweepState = SpeakerSweepStates.START_DRIVE_TO_LOWER_HOOP;
                    Common.debug("AUTO: State = START_DRIVE_TO_LOWER_HOOP");
                }
            break;

            case START_DRIVE_TO_LOWER_HOOP:
                if(intake.isComplete()){
                    dt.distDrive(76, 0); //Was 80
                    sweepState = SpeakerSweepStates.TURN_TO_LOWER_HOOP;
                    Common.debug("AUTO: State = TURN_TO_LOWER_HOOP");
                }
            break;

            case TURN_TO_LOWER_HOOP:
                if(dt.getInches() > 0){
                    //if (sideColor == SideColor.RED){
                        dt.setHeading(49); //Was 47, 45, 42, 41, 42, 47
                    /* } else {
                        dt.setHeading(-47);
                    }*/
                    intake.startIntake();   //Start later?
                    sweepState = SpeakerSweepStates.START_DRIVE_BACK_TO_SPEAKER3;
                    Common.debug("AUTO: State = START_DRIVE_BACK_TO_SPEAKER3");
                }
            break;

            case START_DRIVE_BACK_TO_SPEAKER3:
                if(dt.isComplete() && intake.isComplete()){
                    //if (sideColor == SideColor.RED){
                        dt.distDrive(-63, 30); //was 25
                    /* } else {
                        dt.distDrive(-63, -25);
                    }*/
                    intake.autoPrepToShoot();
                    sweepState = SpeakerSweepStates.TAKE_SHOT_FINAL; //TURN_TO_SPEAKER2 instead?
                    Common.debug("AUTO: State = TAKE_SHOT_FINAL");
                }
            break;

            /*case TURN_TO_SPEAKER3:  //TURN_TO_SPEAKER2 instead?
                if(Math.abs(dt.getInches()) > 40){  //Correct distance?
                    dt.setHeading(0);
                    sweepState = SpeakerSweepStates.TAKE_SHOT_FINAL;
                    Common.debug("AUTO: State = TAKE_SHOT_FINAL");
                }
            break;*/

            case TAKE_SHOT_FINAL:
                if(dt.isComplete() && intake.readyToShoot()){
                    intake.takeShot();
                }
        }
    }

    /*public void speakUp(){
        switch(speakUpState){
            case START:
                intake.prepToShoot();
                speakUpState = SpeakUpStates.START_SHOT1;
                Common.debug("AUTO: State = START_SHOT1");
            break;

            case START_SHOT1:
                if (intake.readyToShoot()) {
                    intake.takeShot();
                    speakUpState = SpeakUpStates.COMPLETE_SHOT1;
                    Common.debug("AUTO: State = COMPLETE_SHOT1");
                }

            case COMPLETE_SHOT1:
                if (intake.isComplete()){
                    dt.distDrive(60, 0);
                    speakUpState = SpeakUpStates.DRIVE_TO_HOOP1;
                    Common.debug("AUTO: State = DRIVE_TO_HOOP1");
                }
            break;

            case DRIVE_TO_HOOP1:
                if (dt.getInches() > 30){
                    intake.startIntake();
                }
                if (dt.isComplete() && intake.isComplete()){
                    dt.distDrive(-60, 0);
                    speakUpState = SpeakUpStates.DRIVE_TO_SPEAK;
                    Common.debug("AUTO: State = DRIVE_TO_SPEAK");
                }
            break;

            case DRIVE_TO_SPEAK:
                if (dt.isComplete()) {
                    intake.prepToShoot();
                    speakUpState = SpeakUpStates.START_SHOT2;
                    Common.debug("AUTO: State = START_SHOT2");
                }
            break;
            
            case START_SHOT2:
                if (intake.isComplete()) {
                    intake.takeShot();
                    speakUpState = SpeakUpStates.COMPLETE_SHOT2;
                    Common.debug("AUTO: State = COMPLETE_SHOT2");
                }
            break;

            case COMPLETE_SHOT2:
                if (intake.isComplete()) {
                    dt.distDrive(69, 69); //put in better nombers for the states onder dis >:[D]
                    speakUpState = SpeakUpStates.DRIVE_TO_HOOP2;
                    Common.debug("AUTO: State = DRIVE_TO_HOOP2");
                }
            break;

            case DRIVE_TO_HOOP2:
                if (dt.isComplete()) {
                    dt.distDrive(-60, 0);
                    speakUpState = SpeakUpStates.SPEAK;
                    Common.debug("AUTO: State = SPEAK");
                }
            break;

            case SPEAK:
                if (dt.isComplete()) {
                    
                }
        }
    } */

    public void threePieceWander(){
        switch(threePieceWanderState){
            case START:
            intake.shooter.aimSpeaker();
            intake.autoPrepToShoot();
            threePieceWanderState = ThreePieceWanderStates.SHOOT_READY;
            Common.debug("AUTO: State = SHOOT_READY");
            break;

            case SHOOT_READY:
            if(intake.readyToShoot()){
                startDelayTimer();
                threePieceWanderState = ThreePieceWanderStates.START_SHOT1;
                Common.debug("AUTO: State = START_SHOT1");
            }
            break;

            case START_SHOT1:
            if (delayComplete()){
                intake.takeShot();
                if(autoMode != Modes.TWO_PIECE_WANDER){
                    threePieceWanderState = ThreePieceWanderStates.DRIVE_TO_HOOP1;
                    Common.debug("AUTO: State = DRIVE_TO_HOOP1");
                } else {
                    threePieceWanderState = ThreePieceWanderStates.DRIVE_TO_HOOP2;
                    Common.debug("AUTO: State = DRIVE_TO_HOOP2");
                }
            }
            break;
            case DRIVE_TO_HOOP1:
            if(intake.isComplete()){
                if(sideColor == SideColor.RED){
                    dt.distDrive(72, 0); //drive at hoop
                } else {
                    dt.distDrive(74, 0); //drive at hoop
                }
                threePieceWanderState = ThreePieceWanderStates.TURN_TO_HOOP1;
                Common.debug("AUTO: dt inches: " + dt.getInches());
                Common.debug("AUTO: State = TURN_TO_HOOP1");
            }
            break;

            case TURN_TO_HOOP1:
            if (dt.getInches() > (sideColor == SideColor.RED ? 7 : 5)){
                if (sideColor == SideColor.RED){
                        dt.setHeading(60);
                    } else {
                        dt.setHeading(-60);
                    }
                intake.startIntake();
                threePieceWanderState = ThreePieceWanderStates.DRIVE_TO_SPEAKER1;
                Common.debug("AUTO: dt inches: " + dt.getInches());
                Common.debug("AUTO: State = DRIVE_TO_SPEAKER1");
            }
            break;

            /*case FINISH_DRIVE1:
            if(dt.isComplete()){
                dt.distDrive(40, 45);
                intake.startIntake();
                threePieceWanderState = ThreePieceWanderStates.DRIVE_TO_SPEAKER1;
            }
            break;*/

            case DRIVE_TO_SPEAKER1:
            if(dt.isComplete() && intake.isComplete()){
                if (sideColor == SideColor.RED){
                        dt.distDrive(-72, 60); 
                    } else {
                        dt.distDrive(-73, -60);
                    }
                threePieceWanderState = ThreePieceWanderStates.TURN_TO_SPEAKER1;
                Common.debug("AUTO: dt inches: " + dt.getInches());
                Common.debug("AUTO: State = TURN_TO_SPEAKER1");
            }
            break;

            case TURN_TO_SPEAKER1:
            if (Math.abs(dt.getInches()) > (sideColor == SideColor.RED ? 31 : 39)){
                dt.setHeading(0);
                intake.autoPrepToShoot();
                threePieceWanderState = ThreePieceWanderStates.START_SHOT2;
                Common.debug("AUTO: dt inches: " + dt.getInches());
                Common.debug("AUTO: State = START_SHOT2");
            }
            break;

            /*case FINISH_DRIVE_TO_SPEAKER1:
            if(dt.isComplete()){
                dt.distDrive(-22, 0);
                intake.prepToShoot();
                threePieceWanderState = ThreePieceWanderStates.START_SHOT2;
            }
            break;*/
            
            case START_SHOT2:
            if(dt.isComplete() && intake.readyToShoot()){
                intake.takeShot();
                threePieceWanderState = ThreePieceWanderStates.DRIVE_TO_HOOP2;
                Common.debug("AUTO: dt inches: " + dt.getInches());
                Common.debug("AUTO: State = DRIVE_TO_HOOP2");
            }
            break;

            case DRIVE_TO_HOOP2:
            if(intake.isComplete()){
                if(sideColor == SideColor.RED){
                    dt.distDrive(337, 0); //Both 343 but changed for blue. Most recently 331
                } else {                  
                    dt.distDrive(337, 0); //Both 343 but changed for blue. Most recently 331
                }
                threePieceWanderState = ThreePieceWanderStates.TURN_TO_HOOP2;
                Common.debug("AUTO: dt inches: " + dt.getInches());
                Common.debug("AUTO: State = TURN_TO_HOOP2");
            }
            break;

            case TURN_TO_HOOP2:
            if(dt.getInches() > (sideColor == SideColor.RED ? 110 : 92)){ //Was 104
                if (sideColor == SideColor.RED){
                        dt.setHeading(55); 
                    } else {
                        dt.setHeading(-49);
                    }
                intake.startIntake();
                threePieceWanderState = ThreePieceWanderStates.DRIVE_TO_SPEAKER2;
                startTime = Common.time();
                Common.debug("AUTO: dt inches: " + dt.getInches());
                Common.debug("AUTO: State = DRIVE_TO_SPEAKER2");
            }
            break;

            /*case FINISH_DRIVE2:
            if(dt.isComplete()){
                dt.distDrive(270, 45);
                intake.startIntake();
                threePieceWanderState = ThreePieceWanderStates.DRIVE_TO_SPEAKER2;
            }
            break;*/

            case DRIVE_TO_SPEAKER2: //Added code in case first note is missed. Turn to next note
            visionCorrect();
            if(dt.isComplete() && intake.isComplete()){
                if (sideColor == SideColor.RED){
                        dt.distDrive(-340, 49); // Was -343
                    } else {
                        dt.distDrive(-337, -49); //Was -333, -338, -341
                    }
                threePieceWanderState = ThreePieceWanderStates.TURN_TO_SPEAKER2;
                Common.debug("AUTO: dt inches: " + dt.getInches());
                Common.debug("AUTO: State = TURN_TO_SPEAKER2");
            } else if(dt.isComplete() && !intake.isComplete() && Common.time() >= startTime + 4500){    //6500
                if (sideColor == SideColor.RED){
                    dt.distDrive(70, 150);  //145
                } else{
                    dt.distDrive(70, -144); //-139
                }
                intake.startIntake();
                threePieceWanderState = ThreePieceWanderStates.COMPLETE;
                Common.debug("AUTO: dt inches: " + dt.getInches());
                Common.debug("AUTO: State = COMPLETE");
            }
            break;

            case TURN_TO_SPEAKER2:
            if(Math.abs(dt.getInches()) > (sideColor == SideColor.RED ? 188 : 212)){ //Was 192 -> 185 for red, 207 -> 217 for blue
                dt.setHeading(0);
                if (autoMode == Modes.THREE_PIECE_WANDER){
                    intake.autoPrepToShoot();
                }
                threePieceWanderState = ThreePieceWanderStates.START_FINAL_SHOT;
                Common.debug("AUTO: dt inches: " + dt.getInches());
                Common.debug("AUTO: State = START_FINAL_SHOT");
            }
            break;

            /*case FINISH_DRIVE_TO_SPEAKER2:
            if(dt.isComplete()){
                dt.distDrive(-73, 0);
                intake.prepToShoot();
                threePieceWanderState = ThreePieceWanderStates.START_FINAL_SHOT;
            }
            break;*/

            case START_FINAL_SHOT:
            if(dt.isComplete() && intake.readyToShoot() && autoMode == Modes.THREE_PIECE_WANDER){
                intake.takeShot();
                threePieceWanderState = ThreePieceWanderStates.COMPLETE;
                Common.debug("AUTO: State = COMPLETE");
            } else if (dt.isComplete() && autoMode == Modes.TWO_PIECE_WANDER){
                intake.prepToShoot();      
                threePieceWanderState = ThreePieceWanderStates.COMPLETE;
                Common.debug("AUTO: State = COMPLETE");
            }
            break;

            case COMPLETE:
            if(intake.readyToShoot() && autoMode == Modes.TWO_PIECE_WANDER){
                intake.takeShot();
            }
            break;
        }
    }

    public void taxi(){
        switch (taxiState) {
            case START:
                intake.shooter.aimSpeaker();
                intake.autoPrepToShoot();
                taxiState = TaxiStates.SHOOT_READY;
                Common.debug("AUTO: State = SHOOT_READY");
            break;

            case SHOOT_READY:
                if(intake.readyToShoot()){
                    startDelayTimer();
                    taxiState = TaxiStates.TAKE_SHOT1;
                    Common.debug("AUTO: State = TAKE_SHOT1");
                }
            break;
            
            case TAKE_SHOT1:
                if(delayComplete()){
                    intake.takeShot();
                    taxiState = TaxiStates.MOVE_OUT_THE_WAY;
                    Common.debug("AUTO: State = MOVE_OUT_THE_WAY");
                }
            break;

            case MOVE_OUT_THE_WAY:
                if(intake.isComplete()){
                    if(sideColor == SideColor.RED){
                        dt.distDrive(36, 30);
                    } else {
                        dt.distDrive(36, -30);
                    }
                    startTime = Common.time();
                    taxiState = TaxiStates.TAXI;
                    Common.debug("AUTO: State = TAXI");
                }
            break;

            case TAXI:
                if(Common.time() >= startTime + 10500){
                    if(sideColor == SideColor.RED){
                        dt.distDrive(75, -60);
                    } else {
                        dt.distDrive(75, 60);
                    }
                    taxiState = TaxiStates.COMPLETE;
                    Common.debug("AUTO: State = COMPLETE");
                }
            break;

            case COMPLETE:
            break;

        }
    }

    public void update(){
        switch(autoMode){
            case AMP_AND_SQUAT:
                ampAndSquat();
            break;

            case SPEAKER_SWEEP:
                speakerSweep();
            break;

            case THREE_PIECE_WANDER:
                threePieceWander();
            break;

            case TWO_PIECE_WANDER:
                threePieceWander();
            break;

            case TAXI:
                taxi();
            break;
        }
        dt.update();
        intake.update();
        debug();
    }

    public void debug(){
        Common.dashStr("Auto: squatState", squatState.toString());
        Common.dashStr("Auto: taxiState", taxiState.toString());
        Common.dashStr("Auto: autoMode", autoMode.toString());
        Common.dashStr("Auto: side color", getSideColor());
        Common.dashBool("Auto: Auto Delay", autoDelay);
    }
}
