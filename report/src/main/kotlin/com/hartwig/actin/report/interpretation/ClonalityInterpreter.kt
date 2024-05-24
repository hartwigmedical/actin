package com.hartwig.actin.report.interpretation

import com.hartwig.actin.molecular.datamodel.wgs.driver.WgsVariant

object ClonalityInterpreter {
    const val CLONAL_CUTOFF = 0.5
   
    fun isPotentiallySubclonal(variant: WgsVariant): Boolean {
        return variant.clonalLikelihood < CLONAL_CUTOFF
    }
}