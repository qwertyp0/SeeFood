package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

private const val REQUEST_CODE = 1


class FormFragment : Fragment() {

    private lateinit var inflatedView : View
    private lateinit var product : TextInputEditText
    private lateinit var servings : TextInputEditText
    private lateinit var calories : TextInputEditText
    private lateinit var totalFat : TextInputEditText
    private lateinit var cholesterol : TextInputEditText
    private lateinit var sodium : TextInputEditText
    private lateinit var totalCarbohydrate : TextInputEditText
    private lateinit var protein : TextInputEditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        inflatedView = inflater.inflate(R.layout.form_fragment, container, false)
        product = inflatedView.findViewById(R.id.product)
        servings = inflatedView.findViewById(R.id.servings)
        calories = inflatedView.findViewById(R.id.calories)
        totalFat = inflatedView.findViewById(R.id.total_fat)
        cholesterol = inflatedView.findViewById(R.id.cholesterol)
        sodium = inflatedView.findViewById(R.id.sodium)
        totalCarbohydrate = inflatedView.findViewById(R.id.total_carbohydrate)
        protein = inflatedView.findViewById(R.id.protein)

        val scanButton = inflatedView.findViewById<MaterialButton>(R.id.scan_button)
        scanButton.setOnClickListener {
            // TODO: finish up scanner
            /*
            val intent = Intent(this.activity, ScannerActivity::class.java)
            val nutritionLabel : NutritionLabel? = null
            intent.putExtra("NutritionLabel", nutritionLabel)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivityForResult(intent, REQUEST_CODE)
             */
        }

        val saveButton = inflatedView.findViewById<MaterialButton>(R.id.save_button)
        saveButton.setOnClickListener {
            // TODO: firebase upload result to database
            /*
            make sure text is not empty and not null and then construct result
            val result = NutritionLabel(
                product!!.text.toString(),
                servings!!.text.toString().toDouble(),
                calories!!.text.toString().toInt(),
                totalFat!!.text.toString().toInt(),
                cholesterol!!.text.toString().toInt(),
                sodium!!.text.toString().toInt(),
                totalCarbohydrate!!.text.toString().toInt(),
                protein!!.text.toString().toInt()
            )
            */

            product.text?.clear()
            servings.text?.clear()
            calories.text?.clear()
            totalFat.text?.clear()
            cholesterol.text?.clear()
            sodium.text?.clear()
            totalCarbohydrate.text?.clear()
            protein.text?.clear()
        }

        return inflatedView
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {

            val nutritionLabel : NutritionLabel? = data?.extras?.getParcelable("NutritionLabel")

            product.setText(nutritionLabel?.product)
            servings.setText(nutritionLabel?.servings.toString())
            calories.setText(nutritionLabel?.calories.toString())
            totalFat.setText(nutritionLabel?.totalFat.toString())
            cholesterol.setText(nutritionLabel?.cholesterol.toString())
            sodium.setText(nutritionLabel?.sodium.toString())
            totalCarbohydrate.setText(nutritionLabel?.totalCarbohydrate.toString())
            protein.setText(nutritionLabel?.protein.toString())
        }

    }

}