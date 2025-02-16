package org.firstinspires.ftc.teamcode.Controllers;

/*
This is a gated PID controller. If the error is over threshold, the output of this controller is
constant, if error is under threshold, then it switches to a PID
*/

public class GatedPID extends Controller {

    private final double pGain, iGain, dGain;
    private double errorSum, sumLimit, lastError, errorSlope;
    private double P, I, D, correctUpLimit, correctLowLimit;
    private final double threshold, constant;
    private boolean firstLoop = true;

    // PID constructor: kP, kI, kD, limit of errorSum, limit of correction (all limits are positive and constrained about 0)
    public GatedPID(double threshold, double constant, double pGain, double iGain, double dGain, double sumLimit, double correctUpLimit, double correctLowLimit) {

        this.threshold = threshold;
        this.constant = constant;
        this.pGain = pGain;
        this.iGain = iGain;
        this.dGain = dGain;
        this.sumLimit = sumLimit;
        this.correctUpLimit = correctUpLimit;
        this.correctLowLimit = correctLowLimit;

    }

    @Override
    public void update(double setPoint, double current){

        error = setPoint - current;

        errorSum += error;
        if(errorSum > sumLimit){
            errorSum = sumLimit;
        }else if(errorSum < -sumLimit){
            errorSum = -sumLimit;
        }

        if(firstLoop){
            firstLoop = false;
            errorSlope = 0;
        }else{
            errorSlope = error - lastError;
        }
        lastError = error;

        P = error * pGain;
        I = errorSum * iGain;
        D = errorSlope * dGain;

        if(error > threshold){
            correction = constant;
        }else{
            correction = P + I + D;
        }

        if(correction > correctUpLimit){
            correction = correctUpLimit;
        }else if(correction < -correctUpLimit){
            correction = -correctUpLimit;
        }else if(correction > 0 && correction < correctLowLimit){
            correction = correctLowLimit;
        }else if(correction < 0 && correction > -correctLowLimit){
            correction = -correctLowLimit;
        }

    }

}