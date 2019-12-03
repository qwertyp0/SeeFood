package com.example.myapplication

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textview.MaterialAutoCompleteTextView
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class FormFragment : Fragment() {

    private var uiModel : UIModel? = null
    private lateinit var formView : View
    private var currNutritionLabel : NutritionLabel? = null
    private var exists : Boolean ? = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        formView = inflater.inflate(R.layout.fragment_form, container, false)
        uiModel = ViewModelProviders.of(activity!!).get(UIModel::class.java)

        // set up appbar
        val appBar = formView.findViewById<MaterialToolbar>(R.id.tool_bar_in_frag)
        appBar.setNavigationIcon(R.drawable.ic_close_black_24dp)
        appBar.setNavigationOnClickListener { activity!!.finish() }
        appBar.setOnClickListener(activity as ScannerActivity)
        val toolBar = formView.findViewById<MaterialToolbar>(R.id.tool_bar_in_frag)
        toolBar.inflateMenu(R.menu.form_toolbar_menu)
        toolBar.setOnMenuItemClickListener {
            when (it.title) {
                "clear" -> uiModel?.setNutritionLabelExists(false)
                "camera" -> uiModel?.setUIState(UIState.BARCODE)
            }
            super.onOptionsItemSelected(it)
        }

        formView.findViewById<TextInputLayout>(R.id.input_date_layout).apply {
            setEndIconDrawable(R.drawable.ic_today_24dp)
            isEndIconVisible = true
            setEndIconOnClickListener {
                formView.findViewById<TextInputEditText>(R.id.input_date).setText(
                    LocalDate.now().format(DateTimeFormatter.ofPattern("EEE, MMM d, yyyy"))
                )
            }
        }

        // set dropdown menu for meals
        val mealAdapter = ArrayAdapter<String>(context!!, R.layout.dropdown_menu,
            arrayOf("Breakfast", "Lunch", "Dinner", "Snacks"))
        formView.findViewById<MaterialAutoCompleteTextView>(R.id.input_meal)
            .setAdapter(mealAdapter)

        uiModel?.nutritionLabel?.observeForever {
            currNutritionLabel = it
        }

        uiModel?.nutritionLabelExists?.observeForever {
            Log.d("FORM", "${it} ${currNutritionLabel!=null}")
            exists = it
            if (it)
                setFields(currNutritionLabel)
            else
                setFields(null)
        }

        return formView
    }

    private fun setFields(nutritionLabel: NutritionLabel?) {
        if (nutritionLabel != null) {
            Log.d("FORM", "setting")
            formView.findViewById<TextInputEditText>(R.id.input_calories)
                .setText(nutritionLabel?.calories.toString(), TextView.BufferType.EDITABLE)
            formView.findViewById<TextInputEditText>(R.id.input_total_fat)
                .setText(nutritionLabel?.totalFat.toString(), TextView.BufferType.EDITABLE)
            formView.findViewById<TextInputEditText>(R.id.input_trans_fat)
                .setText(nutritionLabel?.transFat.toString(), TextView.BufferType.EDITABLE)
            formView.findViewById<TextInputEditText>(R.id.input_saturated_fat)
                .setText(nutritionLabel?.saturatedFat.toString(), TextView.BufferType.EDITABLE)
            formView.findViewById<TextInputEditText>(R.id.input_cholesterol)
                .setText(nutritionLabel?.cholesterol.toString(), TextView.BufferType.EDITABLE)
            formView.findViewById<TextInputEditText>(R.id.input_sodium)
                .setText(nutritionLabel?.sodium.toString(), TextView.BufferType.EDITABLE)
            formView.findViewById<TextInputEditText>(R.id.input_total_carb)
                .setText(nutritionLabel?.totalCarb.toString(), TextView.BufferType.EDITABLE)
            formView.findViewById<TextInputEditText>(R.id.input_fiber)
                .setText(nutritionLabel?.fiber.toString(), TextView.BufferType.EDITABLE)
            formView.findViewById<TextInputEditText>(R.id.input_sugar)
                .setText(nutritionLabel?.sugars.toString(), TextView.BufferType.EDITABLE)
            formView.findViewById<TextInputEditText>(R.id.input_protein)
                .setText(nutritionLabel?.protein.toString(), TextView.BufferType.EDITABLE)
        } else {
            Log.d("FORM", "clearing")
            formView.findViewById<TextInputEditText>(R.id.input_calories)
                .text?.clear()
            formView.findViewById<TextInputEditText>(R.id.input_total_fat)
                .text?.clear()
            formView.findViewById<TextInputEditText>(R.id.input_trans_fat)
                .text?.clear()
            formView.findViewById<TextInputEditText>(R.id.input_saturated_fat)
                .text?.clear()
            formView.findViewById<TextInputEditText>(R.id.input_cholesterol)
                .text?.clear()
            formView.findViewById<TextInputEditText>(R.id.input_sodium)
                .text?.clear()
            formView.findViewById<TextInputEditText>(R.id.input_total_carb)
                .text?.clear()
            formView.findViewById<TextInputEditText>(R.id.input_fiber)
                .text?.clear()
            formView.findViewById<TextInputEditText>(R.id.input_sugar)
                .text?.clear()
            formView.findViewById<TextInputEditText>(R.id.input_protein)
                .text?.clear()
        }
    }

    override fun onResume() {
        super.onResume()
        if (exists == true) // safety call
            setFields(currNutritionLabel)
    }

    fun newLabel() : NutritionLabel? {
        try {
            val date : Date = SimpleDateFormat("EEE, MMM d, yyyy").parse(
                formView.findViewById<TextInputEditText>(R.id.input_date).text!!.toString())
            val meal : Meal = Meal.valueOf(
                formView.findViewById<MaterialAutoCompleteTextView>(R.id.input_meal).text!!.toString().toUpperCase())
            val productName = formView.findViewById<TextInputEditText>(R.id.input_name).text!!.toString()
            val calories = formView.findViewById<TextInputEditText>(R.id.input_calories).text!!.toString().toInt()
            val servings = formView.findViewById<TextInputEditText>(R.id.input_servings).text!!.toString().toDouble()
            val totalFat = formView.findViewById<TextInputEditText>(R.id.input_total_fat).text!!.toString().toInt()
            val transFat = formView.findViewById<TextInputEditText>(R.id.input_trans_fat).text!!.toString().toInt()
            val satFat = formView.findViewById<TextInputEditText>(R.id.input_saturated_fat).text!!.toString().toInt()
            val cholesterol = formView.findViewById<TextInputEditText>(R.id.input_cholesterol).text!!.toString().toInt()
            val sodium = formView.findViewById<TextInputEditText>(R.id.input_sodium).text!!.toString().toInt()
            val totalCarb = formView.findViewById<TextInputEditText>(R.id.input_total_carb).text!!.toString().toInt()
            val sugar = formView.findViewById<TextInputEditText>(R.id.input_sugar).text!!.toString().toInt()
            val fiber = formView.findViewById<TextInputEditText>(R.id.input_fiber).text!!.toString().toInt()
            val protein = formView.findViewById<TextInputEditText>(R.id.input_protein).text!!.toString().toInt()

            return NutritionLabel(date, meal, productName, calories, servings, totalFat, transFat,
                satFat, cholesterol, sodium, totalCarb, fiber, sugar, protein)
        } catch (e : Exception) {
            Log.d("FORMERROR", e.toString())
            return null
        }
    }

    companion object {
        const val TAG = "FORM"
    }
}