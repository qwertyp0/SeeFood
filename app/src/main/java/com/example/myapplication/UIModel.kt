package com.example.myapplication

import android.app.Application
import androidx.annotation.MainThread
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

class UIModel(application: Application) : AndroidViewModel(application) {

    val uiState = MutableLiveData<UIState>()
    val nutritionLabel = MutableLiveData<NutritionLabel>()
    val nutritionLabelExists = MutableLiveData<Boolean>()

    @MainThread
    fun setUIState(uiState: UIState) {
        this.uiState.value = uiState
    }

    @MainThread
    fun setNutritionLabel(nutritionLabel: NutritionLabel) {
        this.nutritionLabel.value = nutritionLabel
    }

    @MainThread
    fun setNutritionLabelExists(boolean : Boolean) {
        this.nutritionLabelExists.value = boolean
    }
}

enum class UIState {
    BARCODE,
    LABEL,
    LOADING,
    FORM
}