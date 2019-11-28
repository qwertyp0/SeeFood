package com.example.myapplication

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.NonNull
import androidx.fragment.app.Fragment
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.auth.FirebaseUser
//import com.google.firebase.database.*

// import com.google.fireBase.database.R

class AccountFragment : Fragment() {


    private var save: Button? = null
    private var calculate: Button? = null
    private var heightText: EditText? = null
    private var weightText: EditText? = null
    private var ageText: EditText? = null
    private var result: TextView? = null
    private var gender: RadioGroup? = null
    private var male: RadioButton? = null
    private var female: RadioButton? = null

    private var saved: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.account_fragment, container, false)

        save = view.findViewById(R.id.save)
        heightText = view.findViewById(R.id.height_value)
        weightText = view.findViewById(R.id.weight_value)
        ageText = view.findViewById(R.id.age_value)
        result = view.findViewById(R.id.result_value)
        gender = view.findViewById(R.id.gender_group)
        male = view.findViewById(R.id.male)
        female = view.findViewById(R.id.female)
        calculate = view.findViewById(R.id.calculate)

        var listOfEditText = arrayListOf(heightText, weightText, ageText)

        save!!.setOnClickListener{
            saveAccount()
        }

        calculate!!.setOnClickListener {

            var map = HashMap<EditText?, Int>()
            var empty = 0
            var emptyEditText: EditText? = null

            listOfEditText.forEach {
                if (it == null || it.text.toString().length == 0) {
                    empty++
                    emptyEditText = it
                } else {
                    map.put(it, Integer.parseInt(weightText!!.text.toString()))
                }
            }

            /* used Harris-Benedict Equation
                For men: BMR = 13.397W + 4.799H - 5.677A + 88.362
                For women: BMR = 9.247W + 3.098H - 4.330A + 447.593
            */

            if (empty == 0 && female!!.isChecked) {
                var femaleBMR =  FEMALE_WEIGHT_FACTOR * map.get(weightText)!!
                                + FEMALE_HEIGHT_FACTOR * map.get(heightText)!!
                                - FEMALE_AGE_FACTOR * map.get(ageText)!!
                                + FEMALE_BMR_CONSTANT
                result!!.setText(femaleBMR.toInt().toString())
                saved = true
            } else if (empty == 0 && male!!.isChecked) {
                var maleBMR =  MALE_WEIGHT_FACTOR * map.get(weightText)!!
                                + MALE_HEIGHT_FACTOR * map.get(heightText)!!
                                - MALE_AGE_FACTOR * map.get(ageText)!!
                                + MALE_BMR_CONSTANT
                result!!.setText(maleBMR.toInt().toString())
                saved = true
            } else if (empty == 1 && emptyEditText == heightText) {
                Toast.makeText(context, "Enter your weight", Toast.LENGTH_LONG).show()
            } else if (empty == 1 && emptyEditText == weightText) {
                Toast.makeText(context, "Enter your weight", Toast.LENGTH_LONG).show()
            } else if (empty == 1 && emptyEditText == ageText) {
                Toast.makeText(context, "Enter your age", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "Enter your Information", Toast.LENGTH_LONG).show()
            }
        }
        return view
    }

    private fun saveAccount() {
        if (!saved) {
            Toast.makeText(context, "Calculate calories before saving.", Toast.LENGTH_LONG).show()
        } else if (heightText!!.text.isEmpty() || weightText!!.text.isEmpty() || ageText!!.text.isEmpty()){
            Toast.makeText(context, "Fields are missing", Toast.LENGTH_LONG).show()
        } else {
            val data = HashMap<String, Int>(4)

            data[CALORIES] = Integer.parseInt(result!!.text.toString())

            data[HEIGHT] = Integer.parseInt(heightText!!.text.toString())
            data[WEIGHT] = Integer.parseInt(weightText!!.text.toString())
            data[AGE] = Integer.parseInt(ageText!!.text.toString())
            data[GENDER] = if (male!!.isChecked) 1 else 0

            // TODO put data in database here and other stuff
        }
    }

    companion object {
        const val MALE_WEIGHT_FACTOR = 13.397
        const val MALE_HEIGHT_FACTOR = 4.799
        const val MALE_AGE_FACTOR = 5.677
        const val MALE_BMR_CONSTANT = 88.362

        const val FEMALE_WEIGHT_FACTOR = 9.247
        const val FEMALE_HEIGHT_FACTOR = 3.098
        const val FEMALE_AGE_FACTOR = 4.330
        const val FEMALE_BMR_CONSTANT = 447.593

        const val HEIGHT = "height"
        const val WEIGHT = "weight"
        const val AGE = "age"
        const val GENDER = "gender"
        const val CALORIES = "calories"
    }
}