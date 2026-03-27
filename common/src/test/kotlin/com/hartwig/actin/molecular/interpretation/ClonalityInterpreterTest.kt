package com.hartwig.actin.molecular.interpretation

import com.hartwig.actin.datamodel.molecular.driver.TestVariantFactory
import com.hartwig.actin.datamodel.molecular.driver.Variant
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

class ClonalityInterpreterTest {

    @Test
    fun `Should determine clonality according to threshold`() {
        Assertions.assertThat(ClonalityInterpreter.isSubclonal(create(ClonalityInterpreter.CLONAL_CUTOFF + 0.01))).isFalse
        Assertions.assertThat(ClonalityInterpreter.isSubclonal(create(ClonalityInterpreter.CLONAL_CUTOFF - 0.01))).isTrue
    }

    private fun create(clonalLikelihood: Double): Variant {
        return TestVariantFactory.createMinimal().copy(clonalLikelihood = clonalLikelihood)
    }
}