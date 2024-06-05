package com.hartwig.actin.report.interpretation

import com.hartwig.actin.molecular.datamodel.orange.driver.ExtendedVariant

object ClonalityInterpreter {
    const val CLONAL_CUTOFF = 0.5
   
    fun isPotentiallySubclonal(variant: ExtendedVariant): Boolean {
        return variant.clonalLikelihood < CLONAL_CUTOFF
    }
}