package org.firstinspires.ftc.teamcode.OpModes;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.teamcode.CustomCV.BTIIBlueDetector;
import org.firstinspires.ftc.teamcode.CustomCV.BTIIRedDetector;
import org.firstinspires.ftc.teamcode.CustomCV.BlueDetector;
import org.firstinspires.ftc.teamcode.CustomCV.RedDetector;
import org.firstinspires.ftc.teamcode.MathFunctions.PointEx;
import org.firstinspires.ftc.teamcode.Robots.FFRobot;
import org.firstinspires.ftc.teamcode.Subsystems.State;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraFactory;
import org.openftc.easyopencv.OpenCvCameraRotation;
import org.openftc.easyopencv.OpenCvInternalCamera;

@Autonomous(name="BTII Red Auto", group="OpMode")
public class AutoBTIIRed extends LinearOpMode {

    // Declare OpMode Members
    private FFRobot bot = new FFRobot(this);

    OpenCvCamera phoneCam;

    private final int allianceTurret = 68;
    private final int allianceSlide = 134;
    private final int allianceTilt = 30;

    @Override
    public void runOpMode() {
        int cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        phoneCam = OpenCvCameraFactory.getInstance().createInternalCamera(OpenCvInternalCamera.CameraDirection.BACK, cameraMonitorViewId);
        BTIIRedDetector detector = new BTIIRedDetector();
        phoneCam.setPipeline(detector);

        phoneCam.openCameraDeviceAsync(new OpenCvCamera.AsyncCameraOpenListener()
        {
            @Override
            public void onOpened()
            {
                phoneCam.startStreaming(320,240, OpenCvCameraRotation.SIDEWAYS_RIGHT);
            }

            @Override
            public void onError(int errorCode) {

            }
        });

        initialize();
        waitForStart();
        telemetry.addData("status","running");
        telemetry.update();
        bot.time.initialize();

        int pos = 0;
        // INITIAL BARCODE DROP-OFF
        BTIIRedDetector.Location en = detector.getLocation();
        if (en == BTIIRedDetector.Location.LEFT){
            pos = 1;
        } else if (en == BTIIRedDetector.Location.RIGHT){
            pos = 0;
        }

        if(pos == 0) {
            bot.time.delaySeconds(1.5);
            bot.outtake.setTargets(60, 6, 83, 1);
        }else {
            bot.time.delaySeconds(1.5);
            bot.outtake.setTargets(allianceTurret-5, allianceTilt, allianceSlide-30, 1);
        }
        // Wait for extend
        while(bot.time.state != State.CONVERGED && opModeIsActive()) {
            bot.update();
        }
        // Drop off
        bot.outtake.setBoxState(2);
        bot.time.delaySeconds(0.3);
        while(bot.time.state != State.CONVERGED && opModeIsActive()) {
            bot.update();
        }
        // Retract slides
        bot.outtake.setTargets(bot.outtake.getTurretAngle(), bot.outtake.tiltPosition, 30, 1);
        // and Deploy intake
        bot.intake.setPower(0.7);
        bot.intake.setFlipPosition(0.6);
        bot.intake.setExtendPosition(0.06);
        bot.time.delaySeconds(0.8);
        while(bot.time.state != State.CONVERGED && opModeIsActive()) {
            bot.update();
        }
        // Home tilt and turret
        bot.outtake.setTargets(0, 0, 20, 1);
        bot.time.delaySeconds(0.3);
        while(bot.time.state != State.CONVERGED && opModeIsActive()) {
            bot.update();
        }
        // Home slides (no delay needed)
        while(!bot.outtake.homeSlides() && opModeIsActive()) {
            bot.update();
        }
        // Home everything
        bot.outtake.setTargets(0, 0, 0, 0);
        bot.time.delaySeconds(0.1);
        while(bot.time.state != State.CONVERGED && opModeIsActive()) {
            bot.update();
        }

        // Park
        if(pos == 0) {
            bot.movement.setTarget(new PointEx(150, 70, -90));
        }else {
            bot.movement.setTarget(new PointEx(80, 70, -90));
        }

        while(bot.movement.state != State.CONVERGED && opModeIsActive()) {
            bot.update();
        }
        bot.drivebase.freeze();
    }

    private void initialize() {
        bot.initialize(hardwareMap);
        bot.outtake.setTurretOffsetAngle(77.2);
        bot.update();
        telemetry.addData("status","initialized");
        telemetry.update();
    }
}