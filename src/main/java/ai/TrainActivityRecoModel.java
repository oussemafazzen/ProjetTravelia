package ai;

import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.ConverterUtils.DataSource;

import java.io.File;

/**
 * Run this class BEFORE launching the app to train the Activity recommendation model.
 * Right-click → Run 'TrainActivityRecoModel.main()' in IntelliJ.
 */
public class TrainActivityRecoModel {

    public static void main(String[] args) throws Exception {

        String arffPath = "travelia_reco_activite.arff";
        String modelDir = "src/main/resources/ai-models/";
        String modelPath = modelDir + "travelia-reco-activite-v1.model";

        // Create directory if not exists
        new File(modelDir).mkdirs();

        // 1) Export ARFF from DB
        ActivityArffExporter exporter = new ActivityArffExporter();
        exporter.exportToArff(arffPath);
        System.out.println("✅ ARFF exported: " + arffPath);

        // 2) Load ARFF
        Instances data = DataSource.read(arffPath);
        if (data.classIndex() == -1) data.setClassIndex(data.numAttributes() - 1);

        System.out.println("📊 Training data: " + data.numInstances() + " instances, " 
                         + data.numAttributes() + " attributes");
        System.out.println("🎯 Class attribute: " + data.classAttribute().name() 
                         + " (" + data.classAttribute().numValues() + " values)");

        // 3) RandomForest classifier
        RandomForest rf = new RandomForest();
        rf.setNumIterations(200);
        rf.setSeed(42);

        FilteredClassifier fc = new FilteredClassifier();
        fc.setClassifier(rf);

        // 4) Build model
        System.out.println("🔧 Training RandomForest with 200 trees...");
        fc.buildClassifier(data);

        // 5) Save model
        SerializationHelper.write(modelPath, fc);
        System.out.println("✅ Model saved: " + modelPath);
        System.out.println("🚀 You can now run the application — Activity AI recommendations are ready!");
    }
}
