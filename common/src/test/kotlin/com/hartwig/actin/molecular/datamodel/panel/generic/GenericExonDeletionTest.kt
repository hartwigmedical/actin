package com.hartwig.actin.molecular.datamodel.panel.generic

import com.hartwig.actin.clinical.datamodel.PriorMolecularTest
import com.hartwig.actin.molecular.datamodel.FREE_TEXT_PANEL
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test

class GenericExonDeletionTest {

    @Test
    fun `Should parse exon deletion from PriorMolecularTest`() {
        val priorMolecularTest = priorMolecularTest(item = "gene", measure = "ex19 del")
        val genericExonDeletion = GenericExonDeletion.parse(priorMolecularTest)
        assertThat("gene").isEqualTo(genericExonDeletion.gene)
        assertThat(19).isEqualTo(genericExonDeletion.affectedExon)
    }

    @Test
    fun `Should throw exception on invalid input`() {
        val invalidInputs = listOf(
            priorMolecularTest(item = "gene", measure = null),
            priorMolecularTest(item = null, measure = "ex19 del"),
            priorMolecularTest(item = null, measure = "exon del"),
            priorMolecularTest(item = null, measure = null)
        )

        invalidInputs.forEach { priorMolecularTest ->
            assertThatThrownBy { GenericExonDeletion.parse(priorMolecularTest) }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("Expected gene and variant but got ${priorMolecularTest.item} and ${priorMolecularTest.measure}")
        }
    }
}

private fun priorMolecularTest(item: String?, measure: String?): PriorMolecularTest {
    return PriorMolecularTest(
        test = FREE_TEXT_PANEL,
        item = item,
        measure = measure,
        impliesPotentialIndeterminateStatus = false
    )
}