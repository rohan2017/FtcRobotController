
package org.firstinspires.ftc.teamcode.Subsystems.Movement;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.teamcode.Controllers.PID;
import org.firstinspires.ftc.teamcode.Controllers.SCurve;
import org.firstinspires.ftc.teamcode.MathFunctions.PointEx;
import org.firstinspires.ftc.teamcode.Subsystems.Localization.Odometer;
import org.firstinspires.ftc.teamcode.Subsystems.Movement.Drivebases.DrivebaseHolonomic;
import org.firstinspires.ftc.teamcode.Subsystems.State;

import static org.firstinspires.ftc.teamcode.MathFunctions.MyMath.*;
import java.util.ArrayList;

public class MovementHolonomic extends Movement {

    public double targetX, targetY, targetHeading;
    public double distanceThreshold = 2;
    public double headingThreshold = 2;

    private DrivebaseHolonomic drivebase;

    private PID orient;
    private PID speedFinder;

    public MovementHolonomic (LinearOpMode opmode, DrivebaseHolonomic drivebase, Odometer odometer) {
        super(opmode, odometer);
        this.drivebase = drivebase;
    }

    public void initialize() {
        targetX = 0;
        targetY = 0;
        targetHeading = 0;

        orient = new PID(0.02,0,0.02,0,0.3,0);
        //speedFinder = new TrapezoidalCurve(10, 0.8);
        speedFinder = new PID(0.05,0,0.02,0,0.5,0);
        state = State.IDLE;
    }

    public void update() {
        if(opMode.opModeIsActive()) {
            if(state != State.IDLE) {
                // State determination
                if (distance(odometer.x, odometer.y, targetX, targetY) < distanceThreshold && (Math.abs(odometer.heading - targetHeading) < headingThreshold)) {
                    state = State.CONVERGED;
                }else {
                    state = State.TRANSIENT;
                }
                // Actions
                if (state == State.TRANSIENT) {

                    double xDist, yDist, distance, heading;
                    double targSpeed, scale;
                    double targVX, targVY, hCorrect;

                    xDist = targetX - odometer.x;
                    yDist = targetY - odometer.y;
                    distance = Math.hypot(xDist, yDist);
                    heading = odometer.heading;

                    targSpeed = Math.abs(speedFinder.correction);
                    scale = targSpeed / distance;

                    targVX = xDist * scale;
                    targVY = yDist * scale;
                    // Verified ^

                    speedFinder.update(0, distance);
                    orient.update(targetHeading, heading);

                    hCorrect = orient.correction;

                    setGlobalVelocity(targVX, targVY, hCorrect);

                    drivebase.update();
                } else if (state == State.CONVERGED) {
                    drivebase.freeze();
                }
                odometer.update();
            }else {
                drivebase.freeze();
            }
        }
    }

    public void setGlobalVelocity(double xVel, double yVel, double hVel) { // Verified
        if(opMode.opModeIsActive()) {
            double h = odometer.heading;
            double xRelVel = cosine(-h) * xVel - sine(-h) * yVel;
            double yRelVel = sine(-h) * xVel + cosine(-h) * yVel;
            drivebase.setRelativeVelocity(xRelVel, yRelVel, hVel, odometer.xVel, odometer.yVel, odometer.headingVel);
        }
    }

    public void setTargets(double X, double Y, double Heading) {
        this.targetX = X;
        this.targetY = Y;
        this.targetHeading = Heading;
        //updateControllers();
        state = State.TRANSIENT;
    }

    public void setTarget(PointEx target) {
        setTargets(target.x, target.y, target.heading);
    }

    public void setTargetPath(ArrayList<PointEx> path) {}

    private void updateControllers() {
        //speedFinder = new SCurve(distance(targetX, targetY, odometer.x, odometer.y));
    }

    public double getDistance() {
        return distance(targetX, targetY, odometer.x, odometer.y);
    }
    public double getSpeed() {
        return speedFinder.correction;
    }
}
