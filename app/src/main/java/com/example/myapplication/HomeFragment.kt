package com.example.myapplication

import android.graphics.Color
import android.os.Bundle
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

        mFloatingActionButton = view.findViewById(R.id.floating_action_button)
        mFloatingActionButton!!.setOnClickListener {
            // TODO @YAN start camera activity here and take out this fragment transaction
            // TODO you need to do mFragmentManager!!.popBackStackImmediate() to get ride of home fragment
            mFragmentManager = fragmentManager
            val mFragmentTransaction = mFragmentManager!!.beginTransaction()
            mFragmentTransaction.replace(R.id.fragment_container, mAboutFragment)
            mFragmentTransaction.commit()
            mFragmentManager!!.executePendingTransactions()
        }


        makeCaloriePieChart(mCaloriePieChart, 2000.0, 1500.0)
        makeMacroPieChart(mMacroPieChart, 15.0, 20.0, 30.0)
        makeMacroLegendTable(15.0, 20.0, 30.0)
        makeBarGraph()

        return view

    }

    private fun makeBarGraph() {
        addInitialEntries()
        setUpBarGraphDisplay()
        setUpAxes()
        // TODO grab all the entries info from database and update them here

        mBarChart!!.layoutParams.height = 200 * 10

        val barDataSet = BarDataSet(entries, "")
        // TODO change color later by creating own class
        // TODO look at https://stackoverflow.com/questions/29888850/mpandroidchart-set-different-color-to-bar-in-a-bar-chart-based-on-y-axis-values
        barDataSet.setColors(green, red)
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

        // TODO Change Label and colors
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

    private inner class MyXAxisValueFormatter : ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            // Simple version. You should use a DateFormatter to specify how you want to textually represent your date.
            return allNutritionNames!![value.toInt()]
        }
    }

}