package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.widget.DatePicker
import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.floatingactionbutton.FloatingActionButton

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

import java.text.DecimalFormat
import java.util.ArrayList
import java.text.SimpleDateFormat
import java.util.*


class HomeFragment : Fragment() {

    private var proteinColor: Int? = null
    private var carbohydrateColor: Int? = null
    private var fatsColor: Int? = null

    private var caloriesConsumed: Int? = null
    private var caloriesAvailable: Int? = null

    private var barGraphColor: Int? = null;


    private var mCaloriePieChart: PieChart? = null
    private var mMacroPieChart: PieChart? = null
    private var mBarChart: BarChart? = null

    private var mCarbohydrateTotal: TextView? = null
    private var mCarbohydratePercent: TextView? = null

    private var mProteinTotal: TextView? = null
    private var mProteinPercent: TextView? = null

    private var mFatTotal: TextView? = null
    private var mFatPercent: TextView? = null

    private var mFloatingActionButton: FloatingActionButton? = null
    private var mFragmentManager: FragmentManager? = null

    // TODO Remove once FAB has camera activity
    private val mAboutFragment = AboutFragment()

    private var allNutritionNames: Array<String>? = arrayOf("Protein", "Sugar", "Fiber", "Carbohydrates", "Sodium", "Cholesterol", "Trans Fat", "Saturated Fat", "Total Fat")
    private val entries: ArrayList<BarEntry> = ArrayList()


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
        mBarChart = view.findViewById(R.id.bar_chart)

        mCarbohydrateTotal = view.findViewById(R.id.carbohydrate_total)
        mCarbohydratePercent = view.findViewById(R.id.carbohydrate_percent)

        mProteinTotal = view.findViewById(R.id.protein_total)
        mProteinPercent = view.findViewById(R.id.protein_percent)

        mFatTotal = view.findViewById(R.id.fat_total)
        mFatPercent = view.findViewById(R.id.fat_percent)

        proteinColor = getColor(context!!, R.color.protein)
        carbohydrateColor = getColor(context!!, R.color.carbohydrates)
        fatsColor = getColor(context!!, R.color.fat)

        caloriesConsumed = getColor(context!!, R.color.caloriesConsumed)
        caloriesAvailable = getColor(context!!, R.color.caloriesAvailable)

        barGraphColor = getColor(context!!, R.color.barGraph)

        //initializing database stuff
        mDatabase = FirebaseDatabase.getInstance()
        mDatabaseReference = mDatabase!!.reference.child("Users")
        mAuth = FirebaseAuth.getInstance()

        //initializing today's date
        mDateView = view.findViewById(R.id.date) as TextView
        mDateView?.setText(getCurrentDate())
        cal = Calendar.getInstance()
        userId = mAuth!!.getCurrentUser()?.uid.toString()
        mFragmentManager = fragmentManager


        val dateSetListener = object : DatePickerDialog.OnDateSetListener {
            override fun onDateSet(view: DatePicker, year: Int, monthOfYear: Int,
                                   dayOfMonth: Int) {
                cal?.set(Calendar.YEAR, year)
                cal?.set(Calendar.MONTH, monthOfYear)
                cal?.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                mFragmentManager!!.popBackStackImmediate()
                updateDateInView()
                fillCharts(userId.toString(),mDateView!!.text.toString())
                makeBarGraph()

                //fillCharts(userId.toString(),mDateView!!.text.toString())
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

        mFloatingActionButton = view.findViewById(R.id.floating_action_button)
        mFloatingActionButton!!.setOnClickListener {
            // TODO @YAN start camera activity here and take out this fragment transaction
            // TODO you need to do mFragmentManager!!.popBackStackImmediate() to get ride of home fragment
//            mFragmentManager = fragmentManager
//            val mFragmentTransaction = mFragmentManager!!.beginTransaction()
//            mFragmentTransaction.replace(R.id.fragment_container, mAboutFragment)
//            mFragmentTransaction.commit()
//            mFragmentManager!!.executePendingTransactions()
            var intent = Intent(context, ScannerActivity::class.java)
            mFragmentManager!!.popBackStackImmediate()
            startActivityForResult(intent, 1)

        }


        fillCharts(userId.toString(),mDateView!!.text.toString())
        makeBarGraph()

        return view

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            val nutritionLabel : NutritionLabel? =
                data?.getParcelableExtra<NutritionLabel>("result")
            // TODO: Abib upload the nutrtion label to firebase if not null
            var map: MutableMap<String, Any?> = mutableMapOf()
            var dailyscans =  arrayListOf<NutritionLabel>()
            var date = mDateView!!.text.toString()
            mDatabaseReference?.child(userId.toString())?.child("daily_scans")?.addListenerForSingleValueEvent(object:ValueEventListener {
                override fun onDataChange(data: DataSnapshot) {
                    //if there is already items in daily_scans
                    if (data.hasChild(date)) {
                        Log.i("SHOWING DATA", "Showing dates foods: " + data?.child(date).value)
                            var foodItems = data?.child(date).value as ArrayList<NutritionLabel>

                            if (nutritionLabel != null) {
                                foodItems.add(nutritionLabel)
                            }
                            map.put(date, foodItems)
                            mDatabaseReference?.child(userId.toString())?.child("daily_scans")?.setValue(map)

                    } else {
                            if (nutritionLabel != null) {
                                dailyscans.add(nutritionLabel)
                            }
                            map.put(date, dailyscans)
                            mDatabaseReference?.child(userId.toString())?.child("daily_scans")?.setValue(map)

                    }
                    fillCharts(userId.toString(),date)
                    makeBarGraph()


                }
                    override fun onCancelled(data: DatabaseError) {
                        Log.i("Hello","data cancelled")
                    }
            })

        }
        else
            Log.d("result", "failed, RequestCode = " + requestCode + ", resultcode=" + resultCode)
    }

    private fun makeBarGraph() {
        addInitialEntries()

        mDatabaseReference?.child(userId.toString())?.addListenerForSingleValueEvent(object:ValueEventListener {
            override fun onDataChange(data: DataSnapshot) {
                var date = mDateView!!.text.toString()
                var totalTransFat = 0.0
                var totalProtein = 0.0
                var totalCarbs = 0.0
                var totalFats = 0.0
                var totalCholesterol = 0.0
                var totalSodium = 0.0
                var totalSugar = 0.0
                var totalSatFat = 0.0
                var totalFiber = 0.0
                var totalServings = 0.0
                if (data.hasChild("daily_scans")) {

                    if (data?.child("daily_scans")?.hasChild(date)) {
                        Log.i("ITEM INFO","THE items: " + data?.child("daily_scans"))
                        data?.child("daily_scans")?.child(date)
                            ?.children.forEachIndexed { index, _ ->
                            totalServings =
                                data?.child("daily_scans")?.child(date)?.child(index.toString())
                                    ?.child("servings")
                                    .value.toString().toDouble()
                            totalProtein += (totalServings) * data?.child("daily_scans")?.child(date)?.child(
                                index.toString()
                            )?.child("protein")
                                .value.toString().toDouble()
                            totalCarbs += (totalServings) * data?.child("daily_scans")?.child(date)?.child(
                                index.toString()
                            )?.child("totalCarb")
                                .value.toString().toDouble()
                            totalFats += (totalServings) * data?.child("daily_scans")?.child(date)?.child(
                                index.toString()
                            )?.child("totalFat")
                                .value.toString().toDouble()
                            totalTransFat += (totalServings) * data?.child("daily_scans")?.child(
                                date
                            )?.child(index.toString())?.child("transFat")
                                .value.toString().toDouble()
                            totalSatFat += (totalServings) * data?.child("daily_scans")?.child(date)?.child(
                                index.toString()
                            )?.child("saturatedFat")
                                .value.toString().toDouble()
                            totalCholesterol += (totalServings) * data?.child("daily_scans")?.child(
                                date
                            )?.child(index.toString())?.child("cholesterol")
                                .value.toString().toDouble()
                            totalSodium += (totalServings) * data?.child("daily_scans")?.child(date)?.child(
                                index.toString()
                            )?.child("sodium")
                                .value.toString().toDouble()
                            totalSugar += (totalServings) * data?.child("daily_scans")?.child(date)?.child(
                                index.toString()
                            )?.child("sugars")
                                .value.toString().toDouble()
                            totalFiber += (totalServings) * data?.child("daily_scans")?.child(date)?.child(
                                index.toString()
                            )?.child("fiber")
                                .value.toString().toDouble()
                        }

                        // TODO ABIB put in value as a float so replace all the 1000.0f with correct value
                        changeEntry("Total Fat", totalFats.toFloat())
                        changeEntry("Saturated Fat", totalSatFat.toFloat())
                        changeEntry("Trans Fat", totalTransFat.toFloat())
                        changeEntry("Cholesterol", totalCholesterol.toFloat())
                        changeEntry("Sodium", totalSodium.toFloat())
                        changeEntry("Carbohydrates", totalCarbs.toFloat())
                        changeEntry("Fiber", totalFiber.toFloat())
                        changeEntry("Sugar", totalSugar.toFloat())
                        changeEntry("Protein", totalProtein.toFloat())
                    }
                    else {
                        //if there is no data for the given date
                        changeEntry("Total Fat", 0.0f)
                        changeEntry("Saturated Fat", 0.0f)
                        changeEntry("Trans Fat", 0.0f)
                        changeEntry("Cholesterol", 0.0f)
                        changeEntry("Sodium", 0.0f)
                        changeEntry("Carbohydrates",0.0f)
                        changeEntry("Fiber", 0.0f)
                        changeEntry("Sugar", 0.0f)
                        changeEntry("Protein", 0.0f)
                    }
                }

            }

            override fun onCancelled(p0: DatabaseError) {
                Log.i("Bar graph data","Not retrieved")
            }
        })
        setUpBarGraphDisplay()
        setUpAxes()





        mBarChart!!.layoutParams.height = 200 * 10

        val barDataSet = BarDataSet(entries, "")
        barDataSet.setColors(barGraphColor!!)
        barDataSet.valueTextSize = 15f
        barDataSet.isHighlightEnabled = false
        barDataSet.axisDependency = YAxis.AxisDependency.RIGHT

        val data = BarData(barDataSet)
        data.barWidth = 0.75f // Width of Bars
        mBarChart!!.data = (data)

        mBarChart!!.xAxis.labelCount = entries.size
        mBarChart!!.invalidate()
    }

    // ("Protein", "Sugar", "Fiber", "Carbohydrates", "Sodium", "Cholesterol", "Trans Fat", "Saturated Fat", "Total Fat")
    // reverse order of this

    private fun addInitialEntries() {
        for (i in allNutritionNames!!.indices) {
            entries.add(BarEntry(i.toFloat(), floatArrayOf(0f, i.toFloat() * 100)))
        }
    }

    private fun changeEntry(nutritionName: String, value: Float) {
        for (i in allNutritionNames!!.indices) {
            if (allNutritionNames!![i] === nutritionName) {
                val dataEntry = entries[i]
                val x = dataEntry.x
                val y = dataEntry.yVals
                var y0 = y[0] + value
                var y1 = y[1]

                if (y0 > 100) {
                    y1 = y0
                    y0 = 0f
                }
                entries.removeAt(i)
                entries.add(i, BarEntry(x, floatArrayOf(y0, y1)))
                break
            }
        }
    }

    private fun setUpBarGraphDisplay() {
        mBarChart!!.legend.isEnabled = false
        mBarChart!!.setDrawBarShadow(false)
        mBarChart!!.description.isEnabled = false
        mBarChart!!.setScaleEnabled(false)
        mBarChart!!.setFitBars(true)
        mBarChart!!.setDrawGridBackground(false)
        mBarChart!!.animateY(500)


        mBarChart!!.extraTopOffset = 10f
        mBarChart!!.extraBottomOffset = (-(entries.size * 200)).toFloat()
        mBarChart!!.extraLeftOffset = 20f
        mBarChart!!.extraLeftOffset = 5f
    }

    private fun setUpAxes() {
        // Left X-axis
        val xl = mBarChart!!.xAxis
        xl.position = XAxis.XAxisPosition.BOTTOM
        xl.setDrawAxisLine(true)
        xl.setDrawGridLines(false)
        xl.textSize = 14.5f
        xl.granularity = 1f

        // Replace Number Labels with String Labels
        xl.valueFormatter = MyXAxisValueFormatter()

        // Left Y-axis
        val yl = mBarChart!!.axisLeft
        yl.setDrawAxisLine(true)
        yl.setDrawGridLines(true)
        yl.axisMinimum = 0f
        yl.textSize = 15f

        // Right Y-Axis
        val yr = mBarChart!!.axisRight
        yr.setDrawAxisLine(true)
        yr.setDrawGridLines(false)
        yr.axisMinimum = 0f
        yr.textSize = 15f
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
        pieChart.animateY(1000)

        return pieChart
    }

    private fun makeCaloriePieChart(view: View?, totalCaloriesAvailable: Double, totalCaloriesConsumed: Double): PieChart {
        val pieChart = view as PieChart

        pieChart.holeRadius = 80f
        pieChart.transparentCircleRadius = 85f
        pieChart.centerText = "Calories\n" + totalCaloriesConsumed.toInt().toString() + "/" + totalCaloriesAvailable.toInt().toString()
        pieChart.setCenterTextSize(17f)
        pieChart.setCenterTextColor(Color.GRAY)


        pieChart.legend.isEnabled = false
        pieChart.description = null
        pieChart.isRotationEnabled = false

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

        if (totalCaloriesConsumed > totalCaloriesAvailable) {
            pieDataSet.setColors(caloriesConsumed!!)
        } else {
            pieDataSet.setColors(caloriesConsumed!!, caloriesAvailable!!)
        }

        pieChart.data = PieData(pieDataSet)
        pieChart.animateY(1000)
        return pieChart
    }

    private inner class MyXAxisValueFormatter : ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            return allNutritionNames!![value.toInt()]
        }
    }

    //Abib's database stuff
    private fun getCurrentDate(): String {
        var sdf = SimpleDateFormat("EEE, MMM d, yyyy" )
        var currentDateandTime = sdf.format(Date())
        Log.i("Current Date","The current date is: "+ currentDateandTime)
        return currentDateandTime
    }
    private fun updateDateInView() {
        val myFormat = "EEE, MMM d, yyyy" // mention the format you need
        val sdf = SimpleDateFormat(myFormat, Locale.US)
        mDateView!!.text = sdf.format(cal!!.time)
    }
    private fun fillCharts(userId: String, date:String) {
        mDatabaseReference?.child(userId)?.addListenerForSingleValueEvent(object: ValueEventListener {
            //
            override fun onDataChange(data: DataSnapshot) {
                var caloriesConsumed = 0.0
                var totalProtein = 0.0
                var totalCarbs = 0.0
                var totalFats = 0.0
                var totalServings = 0.0
                var totalCaloriesAvailable = 0.0

                if (data.hasChild("daily_scans")) {

                    if (data?.child("daily_scans")?.hasChild(date)) {
                        data?.child("daily_scans")?.child(date)?.children.forEachIndexed { index, _ ->
                            totalServings = data?.child("daily_scans")?.child(date)?.child(index.toString())?.child("servings")
                                .value.toString().toDouble()
                            totalProtein += (totalServings) * data?.child("daily_scans")?.child(date)?.child(index.toString())?.child("protein")
                                .value.toString().toDouble()
                            totalCarbs += (totalServings) * data?.child("daily_scans")?.child(date)?.child(index.toString())?.child("totalCarb")
                                .value.toString().toDouble()
                            totalFats += (totalServings) * data?.child("daily_scans")?.child(date)?.child(index.toString())?.child("totalFat")
                                .value.toString().toDouble()
                            caloriesConsumed +=(totalServings) * data?.child("daily_scans")?.child(date)?.child(index.toString())?.child("calories")
                                .value.toString().toDouble()

                        }
                    }
                    if (data.hasChild("account_settings")){
                        totalCaloriesAvailable =data?.child("account_settings")?.child("calories").value.toString().toDouble()
                        makeCaloriePieChart(mCaloriePieChart, totalCaloriesAvailable, caloriesConsumed)
                    }

                    makeMacroPieChart(mMacroPieChart, totalCarbs, totalFats, totalProtein)
                    makeMacroLegendTable(totalCarbs, totalFats, totalProtein)
                }

                else {
                    makeCaloriePieChart(mCaloriePieChart, 2000.0, 0.0)
                    makeMacroPieChart(mMacroPieChart, 0.0, 0.0, 0.0)
                    makeMacroLegendTable(0.0, 0.0, 0.0)
                }

            }
            override fun onCancelled(p0: DatabaseError) {
                Log.i("Test show charts","Could not find the charts")
            }
        })
    }
    private fun testScannerManually() {

    }

}