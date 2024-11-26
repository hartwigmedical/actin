package com.hartwig.actin.algo.evaluation.bloodtransfusion

import com.hartwig.actin.datamodel.Displayable

enum class TransfusionProduct(private val display: String) : Displayable {
    ERYTHROCYTE("Erythrocyte"),
    THROMBOCYTE("Thrombocyte");

    override fun display(): String {
        return display
    }
}