/* Copyright (c) 2018 FIRST. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted (subject to the limitations in the disclaimer below) provided that
 * the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list
 * of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this
 * list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * Neither the name of FIRST nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 *
 * NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
 * LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer.CameraDirection;
import org.firstinspires.ftc.robotcore.external.tfod.Recognition;
import org.firstinspires.ftc.robotcore.external.tfod.TFObjectDetector;

import java.util.List;


@Autonomous(name = "Beta")
@Disabled
public class Beta extends LinearOpMode {
    //preparation for these cool vuforia stuffs
    private static final String TFOD_MODEL_ASSET = "RoverRuckus.tflite";
    private static final String LABEL_GOLD_MINERAL = "Gold Mineral";
    private static final String LABEL_SILVER_MINERAL = "Silver Mineral";
    private VuforiaLocalizer vuforia;
    private TFObjectDetector tfod;
    private DcMotor core = null;
    private static final String VUFORIA_KEY = "AUrb6t//////AAABmZ7sUnVME0wvu2pmOKRP5ilgE5gzg4vWVqHNhc0ef2FEwf9NlosWkTS81UmRvZ0UTHFjPeQYLKL6iY60ZJQcJFcMftURUv/1nA/9YELScRwzltxrUAFpfMA/VE9VTaNPTQQYUfm1Z1wUwY6fAJBwDvZJP+UBqPD0AJxz0Gf8jgcdCVgu4A7VtVdk1PRMTSUkHdOEm+VmXzpjxL9X4d/v81mx3aqJbVc6+qhUD53umiep/wCgl9WxHYY6ZEM2tuS7Eih3TexL24HLFvdEu79t24yTzCFz6du/hB12nfyySO78UWbdlusHuHIv0ZI5/IWh4RigF057FaLWc4F+EluGBkO0c6ygIaciN5fHPS9l7dtj";
    String goldLocation;

    //configure motors

    private DcMotor left = null;
    private DcMotor right = null;
    private DcMotor lift = null;



    ElapsedTime runTime = new ElapsedTime();
    double checkpoint1 = 10;



    public void sample() {
        runTime.reset();
        initVuforia();
        initTfod();
        tfod.activate();

        goldLocation = "N";
        while (goldLocation == "N" && runTime.time() < checkpoint1) {
            if (tfod != null) {
                // getUpdatedRecognitions() will return null if no new information is available since
                // the last time that call was made.
                List<Recognition> updatedRecognitions = tfod.getUpdatedRecognitions();
                if (updatedRecognitions != null) {
                    telemetry.addData("# Object Detected", updatedRecognitions.size());
                    if (updatedRecognitions.size() == 3) {
                        int goldMineralX = -1;
                        int silverMineral1X = -1;
                        int silverMineral2X = -1;
                        for (Recognition recognition : updatedRecognitions) {
                            if (recognition.getLabel().equals(LABEL_GOLD_MINERAL)) {
                                goldMineralX = (int) recognition.getLeft();
                            } else if (silverMineral1X == -1) {
                                silverMineral1X = (int) recognition.getLeft();
                            } else {
                                silverMineral2X = (int) recognition.getLeft();
                            }
                        }
                        if (goldMineralX != -1 && silverMineral1X != -1 && silverMineral2X != -1) {
                            if (goldMineralX < silverMineral1X && goldMineralX < silverMineral2X) {
                                telemetry.addData("Gold Mineral Position", "Left");
                                goldLocation = "L";
                                break;

                            } else if (goldMineralX > silverMineral1X && goldMineralX > silverMineral2X) {
                                telemetry.addData("Gold Mineral Position", "Right");
                                goldLocation = "R";
                                break;

                            } else {
                                telemetry.addData("Gold Mineral Position", "Center");
                                goldLocation = "C";
                                break;
                            }
                        }
                    }
                    telemetry.update();
                }
            }
        }
        //Four endings for this story: left, right, center, nothing


    }

    private void initVuforia() {
        VuforiaLocalizer.Parameters parameters = new VuforiaLocalizer.Parameters();
        parameters.vuforiaLicenseKey = VUFORIA_KEY;
        parameters.cameraDirection = CameraDirection.BACK;
        vuforia = ClassFactory.getInstance().createVuforia(parameters);
    }

    private void initTfod() {
        int tfodMonitorViewId = hardwareMap.appContext.getResources().getIdentifier(
                "tfodMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        TFObjectDetector.Parameters tfodParameters = new TFObjectDetector.Parameters(tfodMonitorViewId);
        tfod = ClassFactory.getInstance().createTFObjectDetector(tfodParameters, vuforia);
        tfod.loadModelFromAsset(TFOD_MODEL_ASSET, LABEL_GOLD_MINERAL, LABEL_SILVER_MINERAL);
    }


    private void configureMotors() {

        left = hardwareMap.get(DcMotor.class, "mot0");
         right = hardwareMap.get(DcMotor.class, "mot1");
         lift = hardwareMap.get(DcMotor.class,"mot2");
        //set rotational direction

         left.setDirection(DcMotor.Direction.FORWARD);
         right.setDirection(DcMotor.Direction.REVERSE);
         lift.setDirection(DcMotor.Direction.FORWARD);


        waitForStart();
    }

    public void linearMove(int dx){//dx means displacement, dx=1 means moving 1 inch.
        runTime.reset();
        left.setPower(.5);//power depends on the the robot and case studies are needed
        right.setPower(.5);
        while (runTime.seconds()<dx){
            telemetry.addData("time",runTime.seconds());
            telemetry.update();
        }
        left.setPower(0);
        right.setPower(0);
    }
    public void angularMove(int dA){//dA means change in angular degree, dA = mean 1 degree
        runTime.reset();;
        left.setPower(.5);
        right.setPower(.5);//power depends on the the robot and case studies are needed
        while (runTime.seconds()<dA){
            telemetry.addData("time",runTime.seconds());
            telemetry.update();
        }
        left.setPower(0);
        right.setPower(0);
    }

    private void pitch() {//pitch the ball means game starts. lower down, leave the latch, come up right in front of the mineral

    }

    private void bat(String location) {

        if (location == "L") {
            angularMove(30);
            linearMove(2);
        }
        if (location == "R") {
            linearMove(2);
        }
        if (location == "C" ||location == "N") {
            linearMove(2);
            angularMove(30);

        }
    }

    private void homeRun() {

    }


    @Override
    public void runOpMode() {
        configureMotors();
        sample();
        bat(goldLocation);
        homeRun();

        }

    }



