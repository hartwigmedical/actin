package com.hartwig.actin.algo.evaluation.bloodtransfusion

enum class TransfusionProduct(private val display: String) {
    ERYTHROCYTE("Erythrocyte"), THROMBOCYTE("Thrombocyte");

    fun display(): String {
        return display
    }
}