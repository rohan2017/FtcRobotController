package org.firstinspires.ftc.teamcode.CustomCV;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.openftc.easyopencv.OpenCvPipeline;

public class BTIIBlueDetector extends OpenCvPipeline {

    private Telemetry telemetry;
    private Mat workingMat = new Mat();
    private Mat frameSized = new Mat();

    public enum Location {
        LEFT,
        RIGHT
    }

    private Location location;

    private double[] leftValue;
    private double leftDist;

    private static final Rect LEFT_ROI = new Rect(new Point(125, 65), new Point(195, 110));

    private static final double[] targetColor = {125, 153, 145}; // in HSV z

    @Override
    public Mat processFrame(Mat input) {
        // Downsize for faster processing & averaged pixels
        //Imgproc.resize(input, frameSized, new Size(0, 0), 0.5, 0.5, Imgproc.INTER_AREA);
        Imgproc.cvtColor(input, workingMat, Imgproc.COLOR_RGB2HSV);

        // Select ROIs out of image
        Mat left = workingMat.submat(LEFT_ROI);

        // Average pixel values within ROIs
        leftValue = Core.sumElems(left).val.clone();

        left.release();

        averageValue(leftValue, LEFT_ROI.area());

        // Transform to find color distance
        leftDist = colorDistance(leftValue);

        // Find closest pixel
        if(leftDist < 100) { // cant be right
            location = Location.LEFT;
        }else { // cant be left
            location = Location.RIGHT;
        }

        Scalar colorObject = new Scalar(255, 0, 0);
        Scalar colorNoObject = new Scalar(0, 255, 0);

        Imgproc.rectangle(input, LEFT_ROI, location == Location.LEFT? colorNoObject:colorObject);

        return input;
    }

    public static double min(double a, double b, double c) {
        return Math.min(Math.min(a, b), c);
    }
    public Location getLocation() {
        return location;
    }


    private void averageValue(double[] pixel, double area) {
        for(int i=0; i<3; i++) {
            pixel[i] = pixel[i] / area;
        }
    }

    private double colorDistance(double[] pixel) {
        double distance = 0;
        distance += Math.pow(pixel[0] - targetColor[0], 2)*0.7;
        distance += Math.pow(pixel[1] - targetColor[1], 2)*0.2;
        distance += Math.pow(pixel[2] - targetColor[2], 2)*0.1;
        return Math.sqrt(distance);
    }

    public double[] getLeftValue(){
        return leftValue;
    }

}
