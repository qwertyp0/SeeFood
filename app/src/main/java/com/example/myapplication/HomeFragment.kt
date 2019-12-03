package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.graphics.Color
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

import java.text.DecimalFormat
import java.util.ArrayList


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
            startActivityForResult(intent, 1)
            // mFragmentManager!!.popBackStackImmediate()
        }

        makeCaloriePieChart(mCaloriePieChart, 2000.0, 1500.0)
        makeMacroPieChart(mMacroPieChart, 15.0, 20.0, 30.0)
        makeMacroLegendTable(15.0, 20.0, 30.0)
        makeBarGraph()

        return view

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            val nutritionLabel : NutritionLabel? =
                data?.getParcelableExtra<NutritionLabel>("result")
            // TODO: Abib upload the nutrtion label to firebase if not null
        }
        else
            Log.d("result", "failed")
    }

    private fun makeBarGraph() {
        addInitialEntries()
        setUpBarGraphDisplay()
        setUpAxes()
        // TODO grab all the entries info from database and update them here

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

    private fun addInitialEntries() {
        for (i in allNutritionNames!!.indices) {
            entries.add(BarEntry(i.toFloat(), floatArrayOf(0f, i.toFloat())))
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

}