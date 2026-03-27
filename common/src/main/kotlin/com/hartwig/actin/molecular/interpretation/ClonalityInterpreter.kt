package com.hartwig.actin.molecular.interpretation

import com.hartwig.actin.datamodel.molecular.driver.Variant

object ClonalityInterpreter {

    const val CLONAL_CUTOFF = 0.5

    fun isSubclonal(variant: Variant): Boolean {
        return variant.clonalLikelihood?.let { it < CLONAL_CUTOFF } == true
    }
}