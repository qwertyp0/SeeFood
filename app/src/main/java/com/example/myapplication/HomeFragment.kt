package com.example.myapplication

import android.widget.DatePicker
import android.app.DatePickerDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.util.Log
import android.widget.TextView
import androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.*
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.LegendEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*


import java.text.DecimalFormat
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*


class HomeFragment : Fragment() {

    // TODO add in colors in REs color folder

    private var green = Color.rgb(5, 205, 110) //Green
    private var yellow = Color.rgb(254, 158, 15)
    private var light_gray = Color.rgb(220, 220, 220) //Light Gray
    private var red = Color.rgb(223, 61, 61) //Red

    private var proteinColor: Int? = null
    private var carbohydrateColor: Int? = null
    private var fatsColor: Int? = null


    private var mCaloriePieChart: PieChart? = null
    private var mMacroPieChart: PieChart? = null

    private var mCarbohydrateTotal: TextView? = null
    private var mCarbohydratePercent: TextView? = null

    private var mProteinTotal: TextView? = null
    private var mProteinPercent: TextView? = null

    private var mFatTotal: TextView? = null
    private var mFatPercent: TextView? = null

    private var mDatabaseReference: DatabaseReference? = null
    private var mDatabase: FirebaseDatabase? = null
    private var mAuth: FirebaseAuth? = null

    private var userId: String? = null
    private var cal: Calendar? = null


    private var mDateView: TextView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.home_fragment, container, false)

        mCaloriePieChart = view.findViewById(R.id.calorie_pie_chart)
        mMacroPieChart = view.findViewById(R.id.macros_pie_chart)

        mCarbohydrateTotal = view.findViewById(R.id.carbohydrate_total)
        mCarbohydratePercent = view.findViewById(R.id.carbohydrate_percent)

        mProteinTotal = view.findViewById(R.id.protein_total)
        mProteinPercent = view.findViewById(R.id.protein_percent)

        mFatTotal = view.findViewById(R.id.fat_total)
        mFatPercent = view.findViewById(R.id.fat_percent)

        proteinColor = getColor(context!!, R.color.protein)
        carbohydrateColor = getColor(context!!, R.color.carbohydrates)
        fatsColor = getColor(context!!, R.color.fat)

        mDatabase = FirebaseDatabase.getInstance()
        mDatabaseReference = mDatabase!!.reference.child("Users")
        mAuth = FirebaseAuth.getInstance()

        mDateView = view.findViewById(R.id.date) as TextView
        mDateView?.setText(getCurrentDate())
        cal = Calendar.getInstance()
        userId = mAuth!!.getCurrentUser()?.uid.toString()
        testScannerManually()
        val dateSetListener = object : DatePickerDialog.OnDateSetListener {
            override fun onDateSet(view: DatePicker, year: Int, monthOfYear: Int,
                                   dayOfMonth: Int) {
                cal?.set(Calendar.YEAR, year)
                cal?.set(Calendar.MONTH, monthOfYear)
                cal?.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                updateDateInView()
                fillCharts(userId.toString(),mDateView!!.text.toString())
            }
        }

        mDateView!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                DatePickerDialog(context!!,
                    dateSetListener,
                    // set DatePickerDialog to point to today's date when it loads up
                    cal!!.get(Calendar.YEAR),
                    cal!!.get(Calendar.MONTH),
                    cal!!.get(Calendar.DAY_OF_MONTH)).show()
            }

        })
//        makeCaloriePieChart(mCaloriePieChart, 2000.0, 1500.0)
//        makeMacroPieChart(mMacroPieChart, 15.0, 20.0, 30.0)
//        makeMacroLegendTable(15.0, 20.0, 30.0)


        return view
    }

    private fun makeMacroLegendTable(carbsTotal: Double, fatsTotal: Double, proteinsTotal: Double) {
        val totalMacros = carbsTotal + fatsTotal + proteinsTotal
        val df = DecimalFormat("#####.#")

        mCarbohydrateTotal!!.text = df.format(carbsTotal).toString()
        mCarbohydratePercent!!.text = (df.format((carbsTotal/totalMacros) * 100).toString() + "%")

        mProteinTotal!!.text = df.format(fatsTotal).toString()
        mProteinPercent!!.text = (df.format((fatsTotal/totalMacros) * 100).toString() + "%")

        mFatTotal!!.text = df.format(proteinsTotal).toString()
        mFatPercent!!.text = (df.format((proteinsTotal/totalMacros) * 100).toString() + "%")

    }

    private fun makeMacroPieChart(view: View?, carbsTotal: Double, fatsTotal: Double, proteinsTotal: Double) : PieChart? {
        val pieChart = view as PieChart

        pieChart.legend.isEnabled = false
        pieChart.description = null
        pieChart.isRotationEnabled = false

        pieChart.setUsePercentValues(true)
        val pieData = ArrayList<PieEntry>()

        pieData.add(PieEntry(carbsTotal.toFloat(), "Carbs"))
        pieData.add(PieEntry(fatsTotal.toFloat() , "Fats"))
        pieData.add(PieEntry(proteinsTotal.toFloat(), "Proteins"))

        pieChart.setEntryLabelColor(Color.rgb(0, 0, 0))

        val pieDataSet = PieDataSet(pieData, "")

        pieDataSet.setDrawValues(false)
        pieDataSet.setColors(carbohydrateColor!!, fatsColor!!, proteinColor!!)

        pieChart.data = PieData(pieDataSet)

        return pieChart
    }

    private fun makeCaloriePieChart(view: View?, totalCaloriesAvailable: Double, totalCaloriesConsumed: Double): PieChart {
        val pieChart = view as PieChart

        pieChart.holeRadius = 80f
        pieChart.transparentCircleRadius = 85f
        pieChart.centerText = "Calories\n" + totalCaloriesConsumed.toInt().toString() + "/" + totalCaloriesAvailable.toInt().toString()
        pieChart.setCenterTextSize(17f)
        pieChart.setCenterTextColor(Color.GRAY)


        // pieChart.setExtraOffsets(20f, 20f, 20f, 20f)

        pieChart.legend.isEnabled = false
        pieChart.description = null
        pieChart.isRotationEnabled = false

        // TODO Change Label
        var pieData = ArrayList<PieEntry>()
        if (totalCaloriesConsumed > totalCaloriesAvailable) {
            pieData.add(PieEntry((totalCaloriesConsumed / totalCaloriesAvailable).toFloat() * 100f, "Calories Consumed (Exceeded)"))
        } else {
            pieChart.setUsePercentValues(true)
            pieData.add(PieEntry(totalCaloriesConsumed.toFloat(), "Calories Consumed"))
            pieData.add(PieEntry((totalCaloriesAvailable - totalCaloriesConsumed).toFloat(), "Calories Available"))
        }

        pieChart.setEntryLabelColor(Color.rgb(0, 0, 0))

        val pieDataSet = PieDataSet(pieData, "")

        pieDataSet.setDrawValues(false)

        // TODO change colors later
        if (totalCaloriesConsumed > totalCaloriesAvailable) {
            pieDataSet.setColors(red)
        } else {
            pieDataSet.setColors(red, green)
        }

        pieChart.data = PieData(pieDataSet)

        return pieChart
    }

    //When the app launches, always start with Today's date
    private fun getCurrentDate(): String {
        var sdf = SimpleDateFormat("EEE, MMM d, yyyy" )
        var currentDateandTime = sdf.format(Date())
        Log.i("Current Date","The current date is: "+ currentDateandTime)
        return currentDateandTime
    }

    private fun testScannerManually() {
        var listExists = false
        var currList = arrayListOf<FoodItem>()
        var dailyscans =  arrayListOf<FoodItem>()
        var map: MutableMap<String, Any?> = mutableMapOf()
        var food1: FoodItem = FoodItem("abcdeEghksjT","pizza",
            40.0 ,60.0,30.0,
            1.0,500.0,22.0,30.0)
        var food2: FoodItem = FoodItem("zyx7ljkLfehi","Chicken",
            30.0,70.0,24.0,
            2.0,300.0,22.0,30.0)

        var food3: FoodItem = FoodItem("abcde232dksd","Bbq",
            40.0,60.0,30.0,
            1.0,500.0,22.0,30.0)
        var food4: FoodItem = FoodItem("4ryklhl2hids","potatoes",
            30.0,70.0,24.0,
            2.0,500.0,22.0,30.0)

        addFood(userId.toString(),getCurrentDate(),food1)
        addFood(userId.toString(),getCurrentDate(),food2)
        addFood(userId.toString(),getCurrentDate(),food3)
        addFood(userId.toString(),getCurrentDate(),food4)

        //dailyscans.add(food1)
        //dailyscans.add(food2)

        //dailyscans.add(food3)
        //dailyscans.add(food4)

        //map.put(getCurrentDate(),dailyscans)
        //mDatabaseReference?.child(userId.toString())?.updateChildren(map)

    }

    private fun fillCharts(userId: String, date:String) {
        mDatabaseReference?.child(userId)?.addValueEventListener(object: ValueEventListener {
            //
            override fun onDataChange(data: DataSnapshot) {
                var caloriesConsumed = 0.0
                var totalProtein = 0.0
                var totalCarbs = 0.0
                var totalFats = 0.0
                var totalServings = 0.0
                var totalCaloriesAvailable = 0.0

                if (data.hasChild(date)) {
                    data.children.forEachIndexed { index, _ ->
                        totalServings = data?.child(date)?.child(index.toString())?.child("servings")
                            .value.toString().toDouble()
                        totalProtein += (totalServings) * data?.child(date)?.child(index.toString())?.child("totalProtein")
                            .value.toString().toDouble()
                        totalCarbs += (totalServings) * data?.child(date)?.child(index.toString())?.child("totalCarbohydrate")
                            .value.toString().toDouble()
                        totalFats += (totalServings) * data?.child(date)?.child(index.toString())?.child("totalFat")
                            .value.toString().toDouble()
                        caloriesConsumed +=(totalServings) * data?.child(index.toString())?.child("calories")
                            .value.toString().toDouble()
                        totalCaloriesAvailable = data?.child("account_settings")?.child("calories")
                            .value.toString().toDouble()
                    }
                    makeCaloriePieChart(mCaloriePieChart, totalCaloriesAvailable, caloriesConsumed)
                    makeMacroPieChart(mMacroPieChart, totalCarbs, totalFats, totalProtein)
                    makeMacroLegendTable(totalCarbs, totalFats, totalProtein)
                }

            }
            override fun onCancelled(p0: DatabaseError) {
                Log.i("Test show charts","Could not find the charts")
            }
        })
    }

    private fun updateDateInView() {
        val myFormat = "EEE, MMM d, yyyy" // mention the format you need
        val sdf = SimpleDateFormat(myFormat, Locale.US)
        mDateView!!.text = sdf.format(cal!!.time)
    }

    private fun addFood(userId:String,date:String,foodItem: FoodItem) {

        var map: MutableMap<String, Any?> = mutableMapOf()
        var dailyscans =  arrayListOf<FoodItem>()
        //var hello: MutableMap<String, Any?> = mutableMapOf()

        mDatabaseReference?.child(userId)?.addListenerForSingleValueEvent(object:ValueEventListener {
            override fun onDataChange(data: DataSnapshot) {
                if (data.hasChild(date)) {
                    Log.i("SHOWING DATA","Showing dates foods: "+ data?.child(date).value)
                    var foodItems = data?.child(date).value as ArrayList<FoodItem>

                    foodItems.add(foodItem)
                    map.put(date,foodItems)
                    mDatabaseReference?.child(userId)?.setValue(map)

                }
                else {
                    dailyscans.add(foodItem)
                    map.put(date,dailyscans)
                    mDatabaseReference?.child(userId)?.setValue(map)

                }
            }
            override fun onCancelled(data: DatabaseError) {
                Log.i("Hello","data cancelled")
            }

        })
        Log.i("Daily Scans", "Daily scans: " + dailyscans)
        mDatabaseReference?.child(userId)?.child(date)?.setValue(dailyscans)



    }

}