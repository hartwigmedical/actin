package com.hartwig.actin.report.interpretation

import com.hartwig.actin.datamodel.molecular.Variant
import com.hartwig.actin.datamodel.molecular.driver.TestVariantFactory
import com.hartwig.actin.report.interpretation.ClonalityInterpreter.isPotentiallySubclonal
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ClonalityInterpreterTest {

    @Test
    fun `Should determine clonality according to threshold`() {
        assertThat(isPotentiallySubclonal(create(ClonalityInterpreter.CLONAL_CUTOFF + 0.01))).isFalse
        assertThat(isPotentiallySubclonal(create(ClonalityInterpreter.CLONAL_CUTOFF - 0.01))).isTrue
    }

    private fun create(clonalLikelihood: Double): Variant {
        return TestVariantFactory.createMinimal()
            .copy(extendedVariantDetails = TestVariantFactory.createMinimalExtended().copy(clonalLikelihood = clonalLikelihood))
    }
}