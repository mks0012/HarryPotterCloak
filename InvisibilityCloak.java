import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;

public class InvisibilityCloak {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args) throws InterruptedException {

        // Open webcam
        VideoCapture cap = new VideoCapture(0);
        if (!cap.isOpened()) {
            System.out.println("Error: Cannot open camera");
            return;
        }

        // Warm-up: Capture background frame
        Mat background = new Mat();
        Thread.sleep(2000); // Wait for camera to auto-adjust
        for (int i = 0; i < 60; i++) {
            cap.read(background);
        }

        // Flip background horizontally
        Core.flip(background, background, 1);

        // Main loop
        Mat frame = new Mat();
        while (true) {
            cap.read(frame);
            if (frame.empty()) {
                break;
            }

            // Flip the frame horizontally
            Core.flip(frame, frame, 1);

            // Convert BGR to HSV
            Mat hsv = new Mat();
            Imgproc.cvtColor(frame, hsv, Imgproc.COLOR_BGR2HSV);

            // Red color range 1
            Scalar lowerRed1 = new Scalar(0, 120, 50);
            Scalar upperRed1 = new Scalar(10, 255, 255);

            // Red color range 2
            Scalar lowerRed2 = new Scalar(170, 120, 70);
            Scalar upperRed2 = new Scalar(180, 255, 255);

            // Create masks for red
            Mat mask1 = new Mat();
            Mat mask2 = new Mat();
            Core.inRange(hsv, lowerRed1, upperRed1, mask1);
            Core.inRange(hsv, lowerRed2, upperRed2, mask2);

            // Combine masks
            Mat redMask = new Mat();
            Core.add(mask1, mask2, redMask);

            // Morphological operations
            Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
            Imgproc.morphologyEx(redMask, redMask, Imgproc.MORPH_OPEN, kernel);
            Imgproc.morphologyEx(redMask, redMask, Imgproc.MORPH_DILATE, kernel);

            // Segment out non-red part from frame
            Mat inverseMask = new Mat();
            Core.bitwise_not(redMask, inverseMask);

            Mat res1 = new Mat();
            Core.bitwise_and(frame, frame, res1, inverseMask);

            // Segment red part from background
            Mat res2 = new Mat();
            Core.bitwise_and(background, background, res2, redMask);

            // Final output
            Mat finalOutput = new Mat();
            Core.addWeighted(res1, 1.0, res2, 1.0, 0.0, finalOutput);

            HighGui.imshow("Invisibility Cloak - Java", finalOutput);

            // Break on ESC key
            if (HighGui.waitKey(30) == 27) {
                break;
            }
        }

        cap.release();
        HighGui.destroyAllWindows();
    }
}
