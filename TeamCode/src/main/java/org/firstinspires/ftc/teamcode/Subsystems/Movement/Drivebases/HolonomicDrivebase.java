package org.firstinspires.ftc.teamcode.Subsystems.Movement.Drivebases;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import org.firstinspires.ftc.teamcode.Hardware.RobotHardware;

public class HolonomicDrivebase extends Drivebase {

    public HolonomicDrivebase(LinearOpMode opMode, RobotHardware hardware) {
        super(opMode, hardware);
    }

    @Override
    public void initialize() {}
    public void update() {}
    public void freeze() {}

    public void setRelativeAcceleration(double velX , double velY, double velHeading) {}

}