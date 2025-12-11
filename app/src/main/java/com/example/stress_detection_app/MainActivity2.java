//package com.example.stress_detection_app;
//
//import android.content.res.AssetFileDescriptor;
//import android.os.Bundle;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.TextView;
//
//import androidx.activity.EdgeToEdge;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.graphics.Insets;
//import androidx.core.view.ViewCompat;
//import androidx.core.view.WindowInsetsCompat;
//
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.nio.MappedByteBuffer;
//import java.nio.channels.FileChannel;
//
//import ai.onnxruntime.OnnxTensor;
//
//public class MainActivity2 extends AppCompatActivity {
//
//    private OnnxRuntimeModel model;
//    private OnnxRuntimeInference inferenceSession;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        EditText et1 = findViewById(R.id.et1); // Sepal Length
//        EditText et2 = findViewById(R.id.et2); // Sepal Width
//        EditText et3 = findViewById(R.id.et3); // Petal Length
//        EditText et4 = findViewById(R.id.et4); // Petal Width
//        Button btn = findViewById(R.id.predictBtn);
//        TextView resultText = findViewById(R.id.resultText);
//
//        // Load the ONNX model
//        try {
//            inferenceSession = new OnnxRuntimeInference(loadModelFile());
//        } catch (IOException e) {
//            resultText.setText("Error loading model: " + e.getMessage());
//            return;
//        }
//
//        // Prediction logic
//        btn.setOnClickListener(v -> {
//            try {
//                // Get inputs
//                float[] input = new float[]{
//                        Float.parseFloat(et1.getText().toString()),
//                        Float.parseFloat(et2.getText().toString()),
//                        Float.parseFloat(et3.getText().toString()),
//                        Float.parseFloat(et4.getText().toString())
//                };
//
//                // Prepare input tensor
//                OnnxTensor inputTensor = OnnxTensor.createTensor(getApplicationContext(), input);
//
//                // Run the model inference
//                OnnxValue result = inferenceSession.run(inputTensor);
//
//                float[][] output = (float[][]) result.getValue();
//                int predicted = argMax(output[0]);
//                String[] labels = {"Setosa", "Versicolor", "Virginica"};
//                resultText.setText("Predicted Class: " + labels[predicted]);
//
//            } catch (Exception e) {
//                resultText.setText("Prediction failed: " + e.getMessage());
//            }
//        });
//    }
//
//    private MappedByteBuffer loadModelFile() throws IOException {
//        AssetFileDescriptor fileDescriptor = getAssets().openFd("iris_rf.onnx");
//        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
//        FileChannel fileChannel = inputStream.getChannel();
//        return fileChannel.map(FileChannel.MapMode.READ_ONLY, fileDescriptor.getStartOffset(), fileDescriptor.getDeclaredLength());
//    }
//
//    private int argMax(float[] arr) {
//        int maxIndex = 0;
//        for (int i = 1; i < arr.length; i++) {
//            if (arr[i] > arr[maxIndex]) maxIndex = i;
//        }
//        return maxIndex;
//    }
//}