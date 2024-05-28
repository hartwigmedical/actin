package com.hartwig.actin.report.interpretation

import com.hartwig.actin.molecular.datamodel.hmf.driver.ExhaustiveVariant

object ClonalityInterpreter {
    const val CLONAL_CUTOFF = 0.5
   
    fun isPotentiallySubclonal(variant: ExhaustiveVariant): Boolean {
        return variant.clonalLikelihood < CLONAL_CUTOFF
    }
}