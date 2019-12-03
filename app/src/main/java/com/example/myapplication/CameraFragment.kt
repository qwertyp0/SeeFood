package com.example.myapplication

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.*
import android.widget.ProgressBar
import androidx.camera.core.*
import androidx.core.view.marginTop
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.chip.Chip
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

class CameraFragment : Fragment() {

    private var uiModel : UIModel? = null
    private var currUIState : UIState? = null
    private val executor = Executors.newSingleThreadExecutor()
    private lateinit var viewFinder : TextureView
    private lateinit var preview : Preview
    private lateinit var spinner : ProgressBar
    private lateinit var previewConfig : PreviewConfig
    private lateinit var imageAnalysis : UseCase

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_camera, container, false)
        viewFinder = view.findViewById(R.id.view_finder)
        viewFinder.setOnClickListener(activity as ScannerActivity)
        spinner = view.findViewById(R.id.spinner)

        // set up camera preview
        previewConfig = PreviewConfig.Builder()
            .apply { setTargetResolution(Size(1280, 960)) }
            .build()
        preview = Preview(previewConfig)
        preview.setOnPreviewOutputUpdateListener { previewOutput ->
            val parent = viewFinder.parent as ViewGroup
            parent.removeView(viewFinder)
            parent.addView(viewFinder, 0)
            viewFinder.surfaceTexture = previewOutput.surfaceTexture
        }

        // set up barcode analysis
        val imageAnalysisConfig = ImageAnalysisConfig.Builder()
            .setTargetResolution(Size(1280, 960))
            .setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
            .build()
        imageAnalysis = ImageAnalysis(imageAnalysisConfig).apply {
            setAnalyzer(executor, BarcodeAnalyzer())
        }

        // set up ui model
        uiModel = ViewModelProviders.of(activity!!).get(UIModel::class.java)
        val uiObserver = Observer<UIState> { uiState ->
            currUIState = uiState

            when (currUIState) {
                UIState.BARCODE, UIState.LABEL -> {
                    spinner.visibility = View.GONE
                    if (!CameraX.isBound(preview)) {
                        viewFinder.post { startCamera() }
                    }
                }
                UIState.LOADING -> {
                    spinner.visibility = View.VISIBLE
                    CameraX.unbindAll()
                }
                UIState.FORM -> {
                    spinner.visibility = View.GONE
                    CameraX.unbindAll()
                }
            }
        }
        uiModel!!.uiState.observeForever(uiObserver)
        return view
    }

    // Bind camera preview and barcode scanner
    private fun startCamera() {
        CameraX.bindToLifecycle(activity, preview, imageAnalysis)
    }

    // Called when viewfinder is tapped. Creates and sets nutrition label with text from img
    fun extractText() {
        val bmp : Bitmap? = viewFinder.bitmap
        val img = FirebaseVisionImage.fromBitmap(bmp!!)
        val detector = FirebaseVision.getInstance().onDeviceTextRecognizer
        detector.processImage(img)
            .addOnSuccessListener {
                val result = NutritionLabel()
                val regexPercent = Regex("\\d+%") // strip out percentages
                val regexOther = Regex("[a-zA-Z]|\\s")
                for (block in it.textBlocks) {
                    for (line in block.lines) {
                        val toParse = line.text.toLowerCase()
                        try {
                            val firstParse = regexPercent.replace(toParse, "")
                            val digits = regexOther.replace(firstParse, "").toInt()
                            when {
                                toParse.contains("calories") -> result.calories = digits
                                toParse.contains("total fat") -> result.totalFat = digits
                                toParse.contains("trans fat") -> result.transFat = digits
                                toParse.contains("saturated fat") -> result.saturatedFat = digits
                                toParse.contains("cholesterol") -> result.cholesterol = digits
                                toParse.contains("sodium") -> result.sodium = digits
                                toParse.contains("total carb") -> result.totalCarb = digits
                                toParse.contains("fiber") -> result.fiber = digits
                                toParse.contains("sugar") -> result.sugars = digits
                                toParse.contains("protein") -> result.protein = digits
                            }
                        } catch (e : Exception) {
                            Log.d("extractText()", e.toString())
                        }
                    }
                }
                uiModel?.setNutritionLabel(result.copy())
                uiModel?.setNutritionLabelExists(true)
                uiModel?.setUIState(UIState.FORM)
            }
            .addOnFailureListener {
                uiModel?.setNutritionLabelExists(false)
                uiModel?.setUIState(UIState.FORM)
            }
    }

    // Inner class for analyzing barcodes... so this object can set the UI model state
    inner class BarcodeAnalyzer : ImageAnalysis.Analyzer {
        private var lastAnalyzedTimestamp = 0L
        private val options = FirebaseVisionBarcodeDetectorOptions.Builder()
            .setBarcodeFormats(
                FirebaseVisionBarcode.FORMAT_UPC_A,
                FirebaseVisionBarcode.FORMAT_UPC_E
            )
            .build()

        private fun degreesToFirebaseRotation(degrees: Int): Int = when (degrees) {
            0 -> FirebaseVisionImageMetadata.ROTATION_0
            90 -> FirebaseVisionImageMetadata.ROTATION_90
            180 -> FirebaseVisionImageMetadata.ROTATION_180
            270 -> FirebaseVisionImageMetadata.ROTATION_270
            else -> throw Exception("Rotation must be 0, 90, 180, or 270.")
        }

        override fun analyze(image: ImageProxy, rotationDegrees: Int) {
            val currentTimestamp = System.currentTimeMillis()
            if (currentTimestamp - lastAnalyzedTimestamp >=
                TimeUnit.SECONDS.toMillis(1)
            ) {
                val mediaImage = image.image
                val imageRotation = degreesToFirebaseRotation(rotationDegrees)
                if (mediaImage != null) {
                    val img = FirebaseVisionImage.fromMediaImage(mediaImage, imageRotation)
                    val detector = FirebaseVision.getInstance().getVisionBarcodeDetector(options)
                    detector.detectInImage(img)
                        .addOnSuccessListener { barcodes ->
                            if (!barcodes.isNullOrEmpty() && currUIState == UIState.BARCODE) {
                                uiModel?.setUIState(UIState.LOADING)
                                val barcode : String = barcodes.first().rawValue!!
                                val key = "bdFBCIwbiOUx1GYS8oMQLCCIQCHioOqcUB4mGs2j"
                                val url = "https://api.nal.usda.gov/fdc/v1/search?api_key=$key"
                                var params = HashMap<String, String>()
                                params["generalSearchInput"] = barcode
                                val jsonObject = JSONObject(params as MutableMap<Any?, Any?>)
                                val request = JsonObjectRequest(
                                    Request.Method.POST, url, jsonObject,
                                    Response.Listener { response ->
                                        try {
                                            val foods : JSONArray = response.getJSONArray("foods")
                                            val resultId = (foods.get(0) as JSONObject).get("fdcId").toString()
                                            val detailsUrl = "https://api.nal.usda.gov/fdc/v1/$resultId?api_key=$key"
                                            val detailsRequest = JsonObjectRequest(Request.Method.GET, detailsUrl, null,
                                                Response.Listener {response ->
                                                    try {
                                                        var nutritionLabel = NutritionLabel()
                                                        val nutrients = response.getJSONObject("labelNutrients")
                                                        for (field in nutrients.keys()) {
                                                            val value = nutrients
                                                                .getJSONObject(field)
                                                                .getDouble("value")
                                                                .roundToInt()

                                                            when (field) {
                                                                "fat" -> nutritionLabel.totalFat = value
                                                                "saturatedFat" -> nutritionLabel.saturatedFat = value
                                                                "transFat" -> nutritionLabel.transFat = value
                                                                "cholesterol" -> nutritionLabel.cholesterol = value
                                                                "sodium" -> nutritionLabel.sodium = value
                                                                "carbohydrates" -> nutritionLabel.totalCarb = value
                                                                "fiber" -> nutritionLabel.fiber = value
                                                                "sugars" -> nutritionLabel.sugars = value
                                                                "protein" -> nutritionLabel.protein = value
                                                                "calories" -> nutritionLabel.calories = value
                                                            }
                                                        }

                                                        uiModel?.setNutritionLabel(nutritionLabel.copy())
                                                        uiModel?.setNutritionLabelExists(true)
                                                        uiModel?.setUIState(UIState.FORM)
                                                    } catch (e : Exception) {
                                                        uiModel?.setNutritionLabelExists(false)
                                                        uiModel?.setUIState(UIState.FORM)
                                                    }
                                                },
                                                Response.ErrorListener {
                                                    uiModel?.setNutritionLabelExists(false)

                                                    uiModel?.setUIState(UIState.FORM)
                                                })
                                            VolleySingleton.getInstance(context!!).addToRequestQueue(detailsRequest)

                                        } catch (e : Exception) {
                                            uiModel?.setNutritionLabelExists(false)
                                            uiModel?.setUIState(UIState.FORM)
                                        }
                                    },
                                    Response.ErrorListener {
                                        uiModel?.setNutritionLabelExists(false)
                                        uiModel?.setUIState(UIState.FORM)
                                    })
                                VolleySingleton.getInstance(context!!).addToRequestQueue(request)
                            }
                        }
                }
                lastAnalyzedTimestamp = currentTimestamp
            }
        }
    }

    companion object {
        const val TAG = "CAMERA"
    }
}

class VolleySingleton constructor(context: Context) {
    companion object {
        @Volatile
        private var INSTANCE: VolleySingleton? = null
        fun getInstance(context: Context) =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: VolleySingleton(context).also {
                    INSTANCE = it
                }
            }
    }
    private val requestQueue: RequestQueue by lazy {
        // applicationContext is key, it keeps you from leaking the
        // Activity or BroadcastReceiver if someone passes one in.
        Volley.newRequestQueue(context.applicationContext)
    }
    fun <T> addToRequestQueue(req: Request<T>) {
        requestQueue.add(req)
    }
}