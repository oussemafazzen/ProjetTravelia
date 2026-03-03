package ai;

import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.ConverterUtils.DataSource;

import java.io.File;

public class TrainTraveliaRecoModel {

    public static void main(String[] args) throws Exception {

        String arffPath = "travelia_reco_pays.arff";
        String modelDir = "src/main/resources/ai-models/";
        String modelPath = modelDir + "travelia-reco-pays-v1.model";

        // Create directory if not exists
        new File(modelDir).mkdirs();

        // 1) exporter ARFF depuis DB
        TraveliaArffExporter exporter = new TraveliaArffExporter();
        exporter.exportToArff(arffPath);
        System.out.println("✅ ARFF exported: " + arffPath);

        // 2) charger ARFF
        Instances data = DataSource.read(arffPath);
        if (data.classIndex() == -1) data.setClassIndex(data.numAttributes() - 1);

        // 3) modèle (RandomForest = bon et stable)
        RandomForest rf = new RandomForest();
        rf.setNumIterations(200);
        rf.setSeed(42);

        FilteredClassifier fc = new FilteredClassifier();
        fc.setClassifier(rf);

        // 5) build
        fc.buildClassifier(data);

        // 6) save model
        SerializationHelper.write(modelPath, fc);
        System.out.println("✅ Model saved: " + modelPath);
    }
}
