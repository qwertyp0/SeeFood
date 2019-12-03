package com.example.myapplication

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

enum class Meal { BREAKFAST, LUNCH, DINNER, SNACK }

@Parcelize
data class NutritionLabel(var date : Date? = null,
                          var meal : Meal? = null,
                          var name : String? = null,
                          var calories : Int = 0,
                          var servings : Double = 1.0,
                          var totalFat : Int = 0,
                          var transFat : Int = 0,
                          var saturatedFat : Int = 0,
                          var cholesterol : Int = 0,
                          var sodium : Int = 0,
                          var totalCarb : Int = 0,
                          var fiber : Int = 0,
                          var sugars : Int = 0,
                          var protein : Int = 0) : Parcelable {
    override fun toString(): String {
        return "START\n" +
                "date ${date?.toString()}\n" +
                "meal ${meal?.toString()}\n" +
                "name $name\n" +
                "calories $calories\n" +
                "servings $servings\n" +
                "totalFat $totalFat\n" +
                "transFat $transFat\n" +
                "saturatedFat $saturatedFat\n" +
                "cholesterol $cholesterol\n" +
                "sodium $sodium\n" +
                "totalCarb $totalCarb\n" +
                "fiber $fiber\n" +
                "sugars $sugars\n" +
                "protein $protein\n" +
                "END\n"
    }

    fun copy() : NutritionLabel {
        return NutritionLabel(
            this.date,
            this.meal,
            this.name,
            this.calories,
            this.servings,
            this.totalFat,
            this.transFat,
            this.saturatedFat,
            this.cholesterol,
            this.sodium,
            this.totalCarb,
            this.fiber,
            this.sugars,
            this.protein
        )
    }
}