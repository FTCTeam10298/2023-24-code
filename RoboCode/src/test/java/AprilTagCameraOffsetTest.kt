//import org.junit.Assert
//import org.junit.Test
//import us.brainstormz.localizer.PositionAndRotation
//import us.brainstormz.localizer.aprilTagLocalization.AprilTagLocalizationFunctions
//import us.brainstormz.robotTwo.RobotTwoHardware
//
//class AprilTagCameraOffsetTest {
//
////    @Ignore("it's right the delta")
//    @Test
//    fun `robot Is at 0,0 angle 0`() {
//        //given
//        val aprilTagLocalizationFunctions = AprilTagLocalizationFunctions(cameraXOffset= 0.0, cameraYOffset= 0.0)
//        val cameraPosition = PositionAndRotation(
//                x = 0.0,
//                y = 0.0,
//                r = 0.0
//        )
//
//        //when
//        val robotCentricPosition = aprilTagLocalizationFunctions.calcRobotPositionFromCameraPosition(
//                cameraPositionAndRotation = cameraPosition,
//                cameraOffsetFromRobotCenterYInches = 10.0
//        )
////        RobotTwoHardware.robotLengthInches/2
//
//        //then
//        val expectedRobotCentricPosition = PositionAndRotation(
//                x = 0.0,
//                y = 10.0,
//                r = 0.0
//        )
//        Assert.assertEquals(
//                expectedRobotCentricPosition,
//                robotCentricPosition,
//        )
//
//    }
//
//    @Test
//    fun `robot Is at 0,0 angle 358`() {
//        //given
//        val aprilTagLocalizationFunctions = AprilTagLocalizationFunctions(cameraXOffset= 0.0, cameraYOffset= 0.0)
//        val cameraPosition = PositionAndRotation(
//                x = 0.0,
//                y = 0.0,
//                r = 358.0
//        )
//
//        //when
//        val robotCentricPosition = aprilTagLocalizationFunctions.calcRobotPositionFromCameraPosition(
//                cameraPositionAndRotation = cameraPosition,
//                cameraOffsetFromRobotCenterYInches = 10.0
//        )
////        RobotTwoHardware.robotLengthInches/2
//
//        //then
//        val expectedRobotCentricPosition = PositionAndRotation(
//                x = 0.0,
//                y = 10.0,
//                r = 358.0
//        )
//        Assert.assertEquals(
//                expectedRobotCentricPosition,
//                robotCentricPosition,
//        )
//
//    }
//
//    @Test
//    fun `camera Is at 1,-10 angle 45`() {
//        //given
//        val aprilTagLocalizationFunctions = AprilTagLocalizationFunctions(cameraXOffset= 0.0, cameraYOffset= 0.0)
//        val cameraPosition = PositionAndRotation(
//                x = 1.0,
//                y = -10.0,
//                r = 45.0
//        )
//
//        //when
//        val robotCentricPosition = aprilTagLocalizationFunctions.calcRobotPositionFromCameraPosition(
//                cameraPositionAndRotation = cameraPosition,
//                cameraOffsetFromRobotCenterYInches = 10.0
//        )
////        RobotTwoHardware.robotLengthInches/2
//
//        //then
//        val expectedRobotCentricPosition = PositionAndRotation(
//                x = -6.071,
//                y = -2.929,
//                r = 45.0
//        )
//
//        Assert.assertEquals(
//                expectedRobotCentricPosition,
//                robotCentricPosition,
//        )
//
//    }
//
//    @Test
//    fun `camera Is at 1,-10 angle -30`() {
//        //given
//        val aprilTagLocalizationFunctions = AprilTagLocalizationFunctions(cameraXOffset= 0.0, cameraYOffset= 0.0)
//        val cameraPosition = PositionAndRotation(
//                x = 1.0,
//                y = -10.0,
//                r = -30.0
//        )
//
//        //when
//        val robotCentricPosition = aprilTagLocalizationFunctions.calcRobotPositionFromCameraPosition(
//                cameraPositionAndRotation = cameraPosition,
//                cameraOffsetFromRobotCenterYInches = RobotTwoHardware.robotLengthInches/2
//        )
//
//        //then
//        val expectedRobotCentricPosition = PositionAndRotation(
//                x = -3.4375,
//                y = -17.6859754586,
//                r = -30.0
//        )
//
//        Assert.assertEquals(
//                expectedRobotCentricPosition,
//                robotCentricPosition,
//        )
//
//    }
//
//}