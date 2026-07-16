package com.example.krishivani.ml

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class DiseaseAnalyzer(
    private val context: Context,
    private val onResult: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private var interpreter: Interpreter
    private var labels: List<String>

    private val INPUT_SIZE = 224
    private val PIXEL_SIZE = 3
    private val IMAGE_MEAN = 0
    private val IMAGE_STD = 255.0f

    init {
        interpreter = Interpreter(loadModelFile("plant_disease_model.tflite"))
        labels = loadLabelList("plant_labels.txt")
    }

    private fun loadModelFile(modelPath: String): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(modelPath)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    private fun loadLabelList(labelPath: String): List<String> {
        return context.assets.open(labelPath).bufferedReader().useLines { it.toList() }
    }

    override fun analyze(image: ImageProxy) {
        val bitmap = image.toBitmap()


        val scaledBitmap = cropAndScale(bitmap)
        val byteBuffer = convertBitmapToByteBuffer(scaledBitmap)

        val result = Array(1) { FloatArray(labels.size) }
        interpreter.run(byteBuffer, result)

        val maxIndex = result[0].indices.maxByOrNull { result[0][it] } ?: -1
        if (maxIndex != -1) {
            val confidence = result[0][maxIndex]



            if (confidence < 0.60f) {
                onResult("કેમેરાને પાંદડા પર બરાબર ફોકસ કરો\n(No clear leaf detected)")
            } else {
                val label = labels[maxIndex]


                val formattedLabel = label.split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }

                val outputText = "$formattedLabel (${String.format("%.1f", confidence * 100)}%)"
                onResult(outputText)
            }
        }

        image.close()
    }

    private fun cropAndScale(bitmap: Bitmap): Bitmap {
        val minDim = Math.min(bitmap.width, bitmap.height)
        val matrix = Matrix()
        val scale = INPUT_SIZE.toFloat() / minDim
        matrix.postScale(scale, scale)

        val xOffset = (bitmap.width - minDim) / 2
        val yOffset = (bitmap.height - minDim) / 2

        return Bitmap.createBitmap(bitmap, xOffset, yOffset, minDim, minDim, matrix, true)
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(4 * INPUT_SIZE * INPUT_SIZE * PIXEL_SIZE)
        byteBuffer.order(ByteOrder.nativeOrder())
        val intValues = IntArray(INPUT_SIZE * INPUT_SIZE)

        bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        var pixel = 0
        for (i in 0 until INPUT_SIZE) {
            for (j in 0 until INPUT_SIZE) {
                val value = intValues[pixel++]

                byteBuffer.putFloat(((value.shr(16) and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
                byteBuffer.putFloat(((value.shr(8) and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
                byteBuffer.putFloat(((value and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
            }
        }
        return byteBuffer
    }

    fun close() {
        interpreter.close()
    }
}
