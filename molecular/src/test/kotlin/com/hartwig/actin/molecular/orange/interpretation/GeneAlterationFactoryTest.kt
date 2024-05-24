package com.hartwig.actin.molecular.orange.interpretation

import com.hartwig.actin.molecular.datamodel.GeneRole
import com.hartwig.actin.molecular.datamodel.ProteinEffect
import com.hartwig.actin.molecular.evidence.known.TestServeKnownFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class GeneAlterationFactoryTest {

    @Test
    fun `Should convert null alterations`() {
        val nullAlteration = GeneAlterationFactory.convertAlteration("gene", null)
        assertThat(nullAlteration.gene).isEqualTo("gene")
        assertThat(nullAlteration.geneRole).isEqualTo(GeneRole.UNKNOWN)
        assertThat(nullAlteration.proteinEffect).isEqualTo(ProteinEffect.UNKNOWN)
        assertThat(nullAlteration.isAssociatedWithDrugResistance).isNull()
    }

    @Test
    fun `Should convert all roles and effects`() {
        for (geneRole in com.hartwig.serve.datamodel.common.GeneRole.values()) {
            for (proteinEffect in com.hartwig.serve.datamodel.common.ProteinEffect.values()) {
                val alteration = GeneAlterationFactory.convertAlteration(
                    "",
                    TestServeKnownFactory.createGeneAlteration(geneRole, proteinEffect)
                )
                assertThat(alteration.gene).isNotNull()
                assertThat(alteration.geneRole).isNotNull()
                assertThat(alteration.proteinEffect).isNotNull()
            }
        }
    }

    @Test
    fun `Should handle drug associations`() {
        val withDrugAssociation = GeneAlterationFactory.convertAlteration(
            "",
            TestServeKnownFactory.createGeneAlteration(
                com.hartwig.serve.datamodel.common.GeneRole.UNKNOWN,
                com.hartwig.serve.datamodel.common.ProteinEffect.UNKNOWN,
                true
            )
        )
        assertThat(withDrugAssociation.isAssociatedWithDrugResistance).isTrue()

        val withNoDrugAssociation = GeneAlterationFactory.convertAlteration(
            "",
            TestServeKnownFactory.createGeneAlteration(
                com.hartwig.serve.datamodel.common.GeneRole.UNKNOWN,
                com.hartwig.serve.datamodel.common.ProteinEffect.UNKNOWN,
                false
            )
        )
        assertThat(withNoDrugAssociation.isAssociatedWithDrugResistance == false).isTrue()
    }
}