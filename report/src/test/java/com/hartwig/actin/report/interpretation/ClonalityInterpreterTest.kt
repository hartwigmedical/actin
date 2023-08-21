package com.hartwig.actin.report.interpretation

import com.hartwig.actin.molecular.datamodel.driver.TestVariantFactory
import com.hartwig.actin.molecular.datamodel.driver.Variant
import com.hartwig.actin.report.interpretation.ClonalityInterpreter.isPotentiallySubclonal
import org.junit.Assert
import org.junit.Test

class ClonalityInterpreterTest {
    @Test
    fun canDetermineClonality() {
        Assert.assertFalse(isPotentiallySubclonal(create(ClonalityInterpreter.CLONAL_CUTOFF + 0.01)))
        Assert.assertTrue(isPotentiallySubclonal(create(ClonalityInterpreter.CLONAL_CUTOFF - 0.01)))
    }

    companion object {
        private fun create(clonalLikelihood: Double): Variant {
            return TestVariantFactory.builder().clonalLikelihood(clonalLikelihood).build()
        }
    }
}