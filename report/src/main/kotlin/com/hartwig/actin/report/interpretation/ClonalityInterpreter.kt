package com.hartwig.actin.report.interpretation

import com.hartwig.actin.datamodel.molecular.driver.Variant

object ClonalityInterpreter {

    const val CLONAL_CUTOFF = 0.5

    fun isPotentiallySubclonal(variant: Variant): Boolean {
        return variant.clonalLikelihood?.let { it < CLONAL_CUTOFF } == true
    }
}