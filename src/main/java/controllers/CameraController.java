package controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.User;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Size;
import org.bytedeco.opencv.opencv_videoio.VideoCapture;
import services.FaceRecognitionService;
import services.SecurityLogService;
import services.UserService;

import java.io.ByteArrayInputStream;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CameraController {

    @FXML private ImageView cameraView;
    @FXML private VBox permissionBox;
    @FXML private Label lblStatus;
    @FXML private Button btnAllow;

    private FaceRecognitionService faceService = new FaceRecognitionService();
    private UserService userService = new UserService();
    private SecurityLogService logService = new SecurityLogService();
    
    private VideoCapture capture;
    private ScheduledExecutorService timer;
    private String targetEmail;
    private int targetUserId;
    private boolean isClosing = false;
    private LoginController loginController;

    public void initData(String email, int userId, LoginController loginController) {
        this.targetEmail = email;
        this.targetUserId = userId;
        this.loginController = loginController;
    }

    @FXML
    void handleAllow(ActionEvent event) {
        permissionBox.setVisible(false);
        startCamera();
    }

    @FXML
    void handleDeny(ActionEvent event) {
        closeCamera();
        ((Stage) btnAllow.getScene().getWindow()).close();
    }

    @FXML
    void handleCancel(ActionEvent event) {
        closeCamera();
        ((Stage) btnAllow.getScene().getWindow()).close();
    }

    private void startCamera() {
        capture = new VideoCapture(0); // Open default camera
        if (capture.isOpened()) {
            lblStatus.setText("Caméra active. Analyse en cours...");
            
            // Create a timer to capture frames every 100ms
            timer = Executors.newSingleThreadScheduledExecutor();
            timer.scheduleAtFixedRate(this::processFrame, 0, 100, TimeUnit.MILLISECONDS);
        } else {
            lblStatus.setText("Erreur: Impossible d'ouvrir la caméra.");
            lblStatus.setStyle("-fx-text-fill: red;");
        }
    }

    private int noFaceCount = 0;

    private void processFrame() {
        if (isClosing) return;

        Mat frame = new Mat();
        try {
            if (capture.read(frame)) {
                // Perform Face Detection
                Mat gray = new Mat();
                opencv_imgproc.cvtColor(frame, gray, opencv_imgproc.COLOR_BGR2GRAY);
                opencv_imgproc.equalizeHist(gray, gray);
                
                org.bytedeco.opencv.opencv_core.RectVector faces = new org.bytedeco.opencv.opencv_core.RectVector();
                faceService.getFaceDetector().detectMultiScale(gray, faces);

                // Draw rectangle on the ORIGINAL frame for display
                for (int i = 0; i < faces.size(); i++) {
                    org.bytedeco.opencv.opencv_core.Rect rect = faces.get(i);
                    opencv_imgproc.rectangle(frame, rect, new org.bytedeco.opencv.opencv_core.Scalar(0, 255, 0, 0));
                }

                // 1. Display frame in JavaFX
                Image image = matToImage(frame);
                Platform.runLater(() -> cameraView.setImage(image));

                // 2. Perform Face Recognition with detailed feedback
                if (faces.size() > 0) {
                    noFaceCount = 0;
                    org.bytedeco.opencv.opencv_core.Rect rect = faces.get(0);
                    Mat face = new Mat(gray, rect);
                    double confidence = faceService.compareFaces(face, String.valueOf(targetUserId));
                    
                    Platform.runLater(() -> {
                        if (confidence >= 60.0) {
                            onSuccess();
                        } else if (confidence >= 0) {
                            lblStatus.setText("Visage détecté : Ce n'est pas la bonne personne (" + String.format("%.0f", confidence) + "%)");
                            lblStatus.setStyle("-fx-text-fill: #e74c3c;"); // Red for mismatch
                        } else {
                            lblStatus.setText("Erreur lors de l'analyse.");
                        }
                    });
                } else {
                    noFaceCount++;
                    Platform.runLater(() -> {
                        if (noFaceCount > 10) {
                            lblStatus.setText("Aucun visage détecté. Vérifiez l'éclairage !");
                            lblStatus.setStyle("-fx-text-fill: #e67e22;");
                        } else {
                            lblStatus.setText("Analyse en cours...");
                            lblStatus.setStyle("-fx-text-fill: #7f8c8d;");
                        }
                    });
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur traitement frame: " + e.getMessage());
        } finally {
            if (frame != null) frame.release();
        }
    }

    private void onSuccess() {
        isClosing = true;
        Platform.runLater(() -> {
            lblStatus.setText("Visage reconnu ! Redirection...");
            lblStatus.setStyle("-fx-text-fill: green;");
            
            // Log success
            logService.logEvent(targetUserId, "FACE_AUTH_SUCCESS", "Reconnaissance live réussie pour " + targetEmail);
            
            // Delay slightly to show success message then close and login
            new Thread(() -> {
                try {
                    Thread.sleep(1500);
                    Platform.runLater(() -> {
                        closeCamera();
                        loginController.completeFaceLogin(targetEmail);
                        ((Stage) cameraView.getScene().getWindow()).close();
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        });
    }

    private void closeCamera() {
        isClosing = true;
        if (timer != null && !timer.isShutdown()) {
            timer.shutdown();
        }
        if (capture != null && capture.isOpened()) {
            capture.release();
        }
    }

    private Image matToImage(Mat frame) {
        BytePointer bytePointer = new BytePointer();
        opencv_imgcodecs.imencode(".bmp", frame, bytePointer);
        byte[] byteArray = bytePointer.getStringBytes();
        return new Image(new ByteArrayInputStream(byteArray));
    }
}
