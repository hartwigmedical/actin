package com.hartwig.actin.report.interpretation

import com.hartwig.actin.molecular.datamodel.driver.TestVariantFactory
import com.hartwig.actin.molecular.datamodel.hmf.driver.ExtendedVariant
import com.hartwig.actin.report.interpretation.ClonalityInterpreter.isPotentiallySubclonal
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ClonalityInterpreterTest {
    @Test
    fun shouldDetermineClonalityAccordingToThreshold() {
        assertThat(isPotentiallySubclonal(create(ClonalityInterpreter.CLONAL_CUTOFF + 0.01))).isFalse
        assertThat(isPotentiallySubclonal(create(ClonalityInterpreter.CLONAL_CUTOFF - 0.01))).isTrue
    }

    private fun create(clonalLikelihood: Double): ExtendedVariant {
        return TestVariantFactory.createMinimal().copy(clonalLikelihood = clonalLikelihood)
    }
}