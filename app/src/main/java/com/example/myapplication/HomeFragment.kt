package com.example.myapplication

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.*
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import java.util.ArrayList


class HomeFragment : Fragment() {

    private var green = Color.rgb(5, 205, 110) //Green
    private var yellow = Color.rgb(254, 158, 15)
    private var light_gray = Color.rgb(220, 220, 220) //Light Gray
    private var red = Color.rgb(223, 61, 61) //Red

    private var mCaloriePieChart: PieChart? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.home_fragment, container, false)
        mCaloriePieChart = view.findViewById(R.id.calorie_pie_chart)
        makePieChart(mCaloriePieChart, 2000.0, 1500.0)

        return view
    }

    fun makePieChart(view: View?, totalCaloriesAvailable: Double, totalCaloriesConsumed: Double): PieChart {
        val pieChart = view as PieChart

        // Center Circle Display
        pieChart.holeRadius = 80f
        pieChart.transparentCircleRadius = 85f
        pieChart.centerText = "Calories\n" + totalCaloriesConsumed.toInt().toString() + "/" + totalCaloriesAvailable.toInt().toString()
        pieChart.setCenterTextSize(17f)
        pieChart.setCenterTextColor(Color.GRAY)


        pieChart.setExtraOffsets(20f, 20f, 20f, 20f)

        pieChart.legend.isEnabled = false
        pieChart.description = null
        pieChart.isRotationEnabled = false


        // TODO Change Label
        val pieData = ArrayList<PieEntry>()
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

        val data = PieData(pieDataSet)
        pieChart.data = data

        return pieChart
    }

}