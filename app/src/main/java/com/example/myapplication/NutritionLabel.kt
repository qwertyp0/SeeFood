package com.example.myapplication

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class NutritionLabel(val product : String = "product",
                          val servings : Double = 0.0,
                          val calories : Int = 0,
                          val totalFat : Int = 0,
                          val cholesterol : Int = 0,
                          val sodium : Int = 0,
                          val totalCarbohydrate : Int = 0,
                          val protein : Int = 0) : Parcelable