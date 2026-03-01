package services;

import models.FaceData;
import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_objdetect.*;
import org.bytedeco.opencv.opencv_face.*;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.global.opencv_core;
import java.io.*;
import java.nio.IntBuffer;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FaceRecognitionService {

    private Connection cnx;
    private CascadeClassifier faceDetector;
    private LBPHFaceRecognizer faceRecognizer;
    private String lastErrorMessage = "";

    public FaceRecognitionService() {
        cnx = utils.MyDataBase.getInstance().getConnection();
        initializeOpenCV();
    }

    private void initializeOpenCV() {
        try {
            // Load the face detector (Haar Cascade)
            File cascadeFile = new File("src/main/resources/models/haarcascade_frontalface_default.xml");
            if (!cascadeFile.exists()) {
                // Try from classpath if not found as file
                InputStream is = getClass().getResourceAsStream("/models/haarcascade_frontalface_default.xml");
                if (is != null) {
                    cascadeFile = File.createTempFile("haarcascade", ".xml");
                    cascadeFile.deleteOnExit();
                    try (FileOutputStream os = new FileOutputStream(cascadeFile)) {
                        byte[] buffer = new byte[8192];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }
                    }
                }
            }

            if (cascadeFile != null && cascadeFile.exists()) {
                faceDetector = new CascadeClassifier(cascadeFile.getAbsolutePath());
            } else {
                System.err.println("ERREUR: Impossible de charger le modèle Haar Cascade.");
            }

            // Initialize the face recognizer
            faceRecognizer = LBPHFaceRecognizer.create();
            trainRecognizer(); // Train with existing data if any

        } catch (Exception e) {
            System.err.println("Erreur initialisation OpenCV: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public String getLastErrorMessage() {
        return lastErrorMessage;
    }

    public CascadeClassifier getFaceDetector() {
        return faceDetector;
    }

    /**
     * Détecte un visage dans un fichier, le recadre, le redimensionne et le sauvegarde comme template.
     * @return Le chemin du fichier recadré (template), ou null si non détecté.
     */
    public String detectFace(File imageFile) {
        lastErrorMessage = "";
        try {
            Mat image = opencv_imgcodecs.imread(imageFile.getAbsolutePath());
            if (image.empty()) {
                lastErrorMessage = "Impossible de lire l'image.";
                return null;
            }

            Mat face = detectFace(image);
            if (face != null) {
                // Resize to standard size (200x200)
                Mat resizedFace = new Mat();
                opencv_imgproc.resize(face, resizedFace, new Size(200, 200));

                // Generate a unique filename for the template
                String originalName = imageFile.getName();
                String fileName = "face_" + System.currentTimeMillis() + "_" + originalName;
                File outputFolder = new File("src/main/resources/images/faces");
                if (!outputFolder.exists()) outputFolder.mkdirs();

                File outputFile = new File(outputFolder, fileName);
                String templatePath = outputFile.getAbsolutePath();

                // Save the cropped face
                if (opencv_imgcodecs.imwrite(templatePath, resizedFace)) {
                    System.out.println("DEBUG Face: Template enregistré : " + templatePath);
                    return templatePath;
                } else {
                    lastErrorMessage = "Erreur lors de l'enregistrement du template.";
                    return null;
                }
            }
            return null;
        } catch (Exception e) {
            lastErrorMessage = "Erreur détection/recadrage: " + e.getMessage();
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Détecte un visage dans un objet Mat (OpenCV).
     * @return L'objet Mat du premier visage détecté (en niveaux de gris), ou null.
     */
    public Mat detectFace(Mat image) {
        lastErrorMessage = "";
        if (faceDetector == null) {
            lastErrorMessage = "Détecteur non initialisé.";
            return null;
        }
        try {
            Mat gray = new Mat();
            if (image.channels() > 1) {
                opencv_imgproc.cvtColor(image, gray, opencv_imgproc.COLOR_BGR2GRAY);
            } else {
                image.copyTo(gray);
            }
            opencv_imgproc.equalizeHist(gray, gray);

            RectVector faces = new RectVector();
            faceDetector.detectMultiScale(gray, faces);

            if (faces.size() > 0) {
                Rect rect = faces.get(0);
                Mat face = new Mat(gray, rect).clone(); // Clone to ensure it's independent
                return face;
            } else {
                lastErrorMessage = "Aucun visage détecté.";
                return null;
            }
        } catch (Exception e) {
            lastErrorMessage = "Erreur détection Mat: " + e.getMessage();
            System.err.println(lastErrorMessage);
            return null;
        }
    }

    /**
     * Compare une image capturée avec les données enregistrées d'un utilisateur.
     */
    public double compareFaces(String capturedImagePath, String storedUserId) {
        lastErrorMessage = "";
        try {
            Mat capturedImage = opencv_imgcodecs.imread(capturedImagePath, opencv_imgcodecs.IMREAD_GRAYSCALE);
            if (capturedImage.empty()) {
                lastErrorMessage = "Image capturée invalide.";
                return -1;
            }
            return compareFaces(capturedImage, storedUserId);
        } catch (Exception e) {
            lastErrorMessage = "Erreur comparaison locale: " + e.getMessage();
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Compare un objet Mat (visage détecté) avec un utilisateur enregistré.
     */
    public double compareFaces(Mat faceMat, String storedUserId) {
        lastErrorMessage = "";
        if (faceRecognizer == null) {
            lastErrorMessage = "Reconnaisseur non initialisé.";
            return -1;
        }
        try {
            int id = Integer.parseInt(storedUserId);
            int[] outLabel = new int[1];
            double[] outConfidence = new double[1];
            
            Mat resizedImage = new Mat();
            opencv_imgproc.resize(faceMat, resizedImage, new Size(200, 200));
            
            faceRecognizer.predict(resizedImage, outLabel, outConfidence);

            System.out.println("DEBUG Face: --- Analyse de Reconnaissance ---");
            System.out.println("DEBUG Face: ID Recherché: " + id);
            System.out.println("DEBUG Face: ID Prédit:   " + outLabel[0]);
            System.out.println("DEBUG Face: Distance RAW: " + String.format("%.2f", outConfidence[0]));
            System.out.println("DEBUG Face: Image Size:   " + resizedImage.cols() + "x" + resizedImage.rows());

            if (outLabel[0] == id) {
                // Seuil de distance pour LBPH : 0-50 (excellent), 50-80 (bon/moyen), 80-120 (faible).
                // On utilise une formule plus tolérante : 100 - (distance * 0.5)
                // Ainsi, une distance de 80 donne encore 60%.
                double score = Math.max(0, 100 - (outConfidence[0] * 0.5)); 
                System.out.println("DEBUG Face: MATCH! Score calculé: " + String.format("%.2f", score) + "% (Distance RAW: " + String.format("%.2f", outConfidence[0]) + ")");
                return score;
            } else {
                System.out.println("DEBUG Face: MISMATCH! Attendu=" + id + ", Reçu=" + outLabel[0] + " (Distance: " + outConfidence[0] + ")");
                return 0;
            }
        } catch (NumberFormatException e) {
            lastErrorMessage = "Erreur format ID: " + storedUserId;
            System.err.println(lastErrorMessage);
            return -1;
        } catch (Exception e) {
            lastErrorMessage = "Erreur comparaison Mat: " + e.getMessage();
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Entraîne le reconnaisseur avec toutes les données de la base.
     */
    private void trainRecognizer() {
        try {
            String req = "SELECT user_id, face_token FROM face_data";
            List<Mat> images = new ArrayList<>();
            List<Integer> labels = new ArrayList<>();

            try (PreparedStatement ps = cnx.prepareStatement(req);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String path = rs.getString("face_token");
                    File imgFile = new File(path);
                    if (imgFile.exists()) {
                        Mat img = opencv_imgcodecs.imread(path, opencv_imgcodecs.IMREAD_GRAYSCALE);
                        if (!img.empty()) {
                            opencv_imgproc.resize(img, img, new Size(200, 200));
                            opencv_imgproc.equalizeHist(img, img); // NORMALIZATION
                            images.add(img);
                            labels.add(rs.getInt("user_id"));
                        }
                    }
                }
            }

            if (!images.isEmpty()) {
                MatVector matVector = new MatVector(images.size());
                int[] labelsArray = new int[labels.size()];
                for (int i = 0; i < images.size(); i++) {
                    matVector.put(i, images.get(i));
                    labelsArray[i] = labels.get(i);
                }
                Mat labelsMat = new Mat(labels.size(), 1, opencv_core.CV_32SC1);
                IntBuffer labelsBuf = labelsMat.createBuffer();
                labelsBuf.put(labelsArray);
                
                faceRecognizer.train(matVector, labelsMat);
                System.out.println("Reconnaisseur entraîné avec " + images.size() + " visages.");
            }
        } catch (Exception e) {
            System.err.println("Erreur entraînement: " + e.getMessage());
        }
    }

    public void storeFaceData(int userId, String imagePath, String encoding) throws SQLException {
        // En mode local, on stocke le chemin de l'image comme token
        String insertReq = "INSERT INTO face_data (user_id, face_token, face_encoding) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE face_token = ?, created_at = NOW()";
        try (PreparedStatement ps = cnx.prepareStatement(insertReq)) {
            ps.setInt(1, userId);
            ps.setString(2, imagePath);
            ps.setString(3, ""); // encoding non utilisé en LBPH simple
            ps.setString(4, imagePath);
            ps.executeUpdate();
        }
        trainRecognizer(); // Re-train after new data
    }

    public FaceData getFaceDataByUserId(int userId) throws SQLException {
        String req = "SELECT * FROM face_data WHERE user_id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    FaceData fd = new FaceData();
                    fd.setId(rs.getInt("id"));
                    fd.setUserId(rs.getInt("user_id"));
                    fd.setFaceToken(rs.getString("face_token"));
                    fd.setCreatedAt(rs.getTimestamp("created_at"));
                    return fd;
                }
            }
        }
        return null;
    }

    public FaceData getFaceDataByEmail(String email) throws SQLException {
        String req = "SELECT fd.* FROM face_data fd INNER JOIN client c ON fd.user_id = c.id WHERE c.email = ?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    FaceData fd = new FaceData();
                    fd.setId(rs.getInt("id"));
                    fd.setUserId(rs.getInt("user_id"));
                    fd.setFaceToken(rs.getString("face_token"));
                    fd.setCreatedAt(rs.getTimestamp("created_at"));
                    return fd;
                }
            }
        }
        return null;
    }

    /**
     * Vérifie si un visage correspond localement (Seuil de confiance inversé pour LBPH).
     */
    public boolean verifyFace(File capturedImage, int userId) throws SQLException {
        String token = capturedImage.getAbsolutePath();
        double score = compareFaces(token, String.valueOf(userId));
        return score >= 60.0;
    }

    /**
     * Vérifie si un visage dans un flux Mat correspond à un utilisateur.
     */
    public boolean verifyFace(Mat frame, int userId) {
        Mat face = detectFace(frame);
        if (face != null) {
            double score = compareFaces(face, String.valueOf(userId));
            return score >= 60.0;
        }
        return false;
    }
}
