package com.hartwig.actin.molecular.datamodel.panel.generic

import com.hartwig.actin.clinical.datamodel.PriorMolecularTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test

class GenericWgsVariantTest {

    @Test
    fun `Should parse variant from prior molecular test`() {
        val priorMolecularTest = priorMolecularTest(item = "gene", measure = "c.A>T")
        val genericVariant = GenericVariant.parseVariant(priorMolecularTest)
        assertThat("gene").isEqualTo(genericVariant.gene)
        assertThat("c.A>T").isEqualTo(genericVariant.hgvsCodingImpact)
    }

    @Test
    fun `Should throw exception on invalid input`() {
        val invalidInputs = listOf(
            priorMolecularTest(item = "gene", measure = null),
            priorMolecularTest(item = null, measure = "c.A>T"),
            priorMolecularTest(item = null, measure = null)
        )

        invalidInputs.forEach { priorMolecularTest ->
            assertThatThrownBy { GenericVariant.parseVariant(priorMolecularTest) }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("Expected item and measure for variant but got ${priorMolecularTest.item} and ${priorMolecularTest.measure}")
        }
    }

    companion object {
        private fun priorMolecularTest(item: String?, measure: String?): PriorMolecularTest {
            return PriorMolecularTest(
                test = "test",
                item = item,
                measure = measure,
                impliesPotentialIndeterminateStatus = false
            )
        }
    }
}