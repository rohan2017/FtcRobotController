package org.firstinspires.ftc.teamcode.Subsystems.Localization;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.teamcode.Hardware.RobotHardware;

public class Odometer3W extends Odometer{

    private LinearOpMode opMode;
    private RobotHardware hardware;

    /* Top-Down View of The Bottom of a Robot
    /===================\
    |                   |
    |  A------X------B  |   A represents the left vertical dead-wheel
    |     ^          |  |   B represents the right vertical dead-wheel, and has to be in line with A
    |     H          |  |
    |            V-> |  |   C represents the horizontal dead-wheel, and can be placed anywhere
    |                C  |
    |                   |
    \===================/
    The above diagram is a top-down view of the bottom of the robot, meaning that when you are
    looking at the robot from the top, A is to the left, B is to the right, and C is to the back.
    X denotes the center of the robot
    H denotes the horizontalOffset variable, or distance from A to B divided by 2.
    V denotes the verticalOffset variable, or the vertical distance from C to X. Does not matter very much
    Odometer measurements can be in whatever units you want, as long as you use the same units for every constant
    */

    private double horizontalOffset = 35.5/2;
    private double verticalOffset = -7.5;

    // These variables allow you to set the direction of the encoders globally for all robot hardware
    // Not recommended, it's better to change directions in the hardware class
    private double rightVerticalDirection = 1;
    private double leftVerticalDirection = 1;
    private double horizontalDirection = 1;

    // Encoder Variables
    public double leftVertical, rightVertical, horizontal;
    private double lastLeftVertical, lastRightVertical, lastHorizontal;
    private double ticksToDistance;
    private double[] positionChangeVertical = {0, 0}; //Position change vector from vertical encoders
    private double[] positionChangeHorizontal = {0, 0}; //Position change vector from horizontal encoder
    private double[] totalRelativeMovement = {0, 0};
    private double[] totalPositionChange = {0, 0};

    public Odometer3W(LinearOpMode opMode, RobotHardware robothardware){
        this.opMode = opMode;
        this.hardware = robothardware;

    }

    @Override
    public void initialize(){

        lastLeftVertical = 0;
        lastRightVertical = 0;
        lastHorizontal = 0;

        ticksToDistance = wheelRadius*2*Math.PI/ticksPerRevolution*gearRatio;
    }

    @Override
    public void update(){
        if(opMode.opModeIsActive()) {

            leftVertical = hardware.getMotor("leftVerticalEncoder").getCurrentPosition() * ticksToDistance * leftVerticalDirection;
            rightVertical = hardware.getMotor("rightVerticalEncoder").getCurrentPosition() * ticksToDistance * rightVerticalDirection;
            horizontal = hardware.getMotor("horizontalEncoder").getCurrentPosition() * ticksToDistance * horizontalDirection;

            double leftVerticalChange = leftVertical - lastLeftVertical;
            double rightVerticalChange = rightVertical - lastRightVertical;
            double horizontalChange = horizontal - lastHorizontal;

            // Math Variables
            double headingChange = (rightVerticalChange - leftVerticalChange) / 2 / horizontalOffset;

            headingRadians += headingChange;

            // Calculating the position-change-vector from two vertical encoders
            double centerArc = (leftVerticalChange + rightVerticalChange) / 2;

            double turnRadius;
            if(headingChange == 0) { // Robot has gone straight/not moved

                positionChangeVertical[0] = 0;
                positionChangeVertical[1] = centerArc;

            }else if(Math.abs(rightVerticalChange) < Math.abs(leftVerticalChange)){ //Left encoder is on inside of the turn

                turnRadius = centerArc / headingChange; //Always positive

                positionChangeVertical[0] = turnRadius - Math.cos(headingChange) * turnRadius;
                positionChangeVertical[1] = Math.sin(headingChange) * turnRadius;

            }else{ //Right encoder is on inside of the turn

                turnRadius = centerArc /-headingChange; //Always positive

                positionChangeVertical[0] = turnRadius - Math.cos(-headingChange) * turnRadius;
                positionChangeVertical[1] = Math.sin(-headingChange) * turnRadius;

            }

            //Calculating the position-change-vector from horizontal encoder
            double horizontalAdjust = verticalOffset * headingChange;
            double horizontalExtra = horizontalChange - horizontalAdjust;

            positionChangeHorizontal[0] = Math.cos(headingChange) * horizontalExtra;
            positionChangeHorizontal[1] = Math.sin(headingChange) * horizontalExtra;


            //Add the two vectors together
            totalRelativeMovement[0] = positionChangeVertical[0] + positionChangeHorizontal[0];
            totalRelativeMovement[1] = positionChangeVertical[1] + positionChangeHorizontal[1];

            //Rotate the vector
            totalPositionChange[0] = totalRelativeMovement[0] * Math.cos(lastHeadingRadians) - totalRelativeMovement[1] * Math.sin(lastHeadingRadians);
            totalPositionChange[1] = totalRelativeMovement[0] * Math.sin(lastHeadingRadians) + totalRelativeMovement[1] * Math.cos(lastHeadingRadians);

            x = lastX + totalPositionChange[0];
            y = lastY + totalPositionChange[1];

            lastX = x;
            lastY = y;
            lastHeadingRadians = headingRadians;

            lastLeftVertical = leftVertical;
            lastRightVertical = rightVertical;
            lastHorizontal = horizontal;

            heading = Math.toDegrees(headingRadians);
        }
    }

    // Utility Methods
    public void setEncoderDirections(double rightVerticalDirection, double leftVerticalDirection, double horizontalDirection){

        this.rightVerticalDirection = rightVerticalDirection;
        this.leftVerticalDirection = leftVerticalDirection;
        this.horizontalDirection = horizontalDirection;

    }

}