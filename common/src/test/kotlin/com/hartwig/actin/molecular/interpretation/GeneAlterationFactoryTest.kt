package com.hartwig.actin.molecular.interpretation

import com.hartwig.actin.datamodel.molecular.driver.GeneRole
import com.hartwig.actin.datamodel.molecular.driver.ProteinEffect
import com.hartwig.actin.molecular.evidence.known.TestServeKnownFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import com.hartwig.serve.datamodel.molecular.common.GeneRole as ServeGeneRole
import com.hartwig.serve.datamodel.molecular.common.ProteinEffect as ServeProteinEffect

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
        for (geneRole in ServeGeneRole.values()) {
            for (proteinEffect in ServeProteinEffect.values()) {
                val alteration = GeneAlterationFactory.convertAlteration(
                    "", TestServeKnownFactory.createGeneAlteration(geneRole, proteinEffect)
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
            TestServeKnownFactory.createGeneAlteration(ServeGeneRole.UNKNOWN, ServeProteinEffect.UNKNOWN, true)
        )
        assertThat(withDrugAssociation.isAssociatedWithDrugResistance).isTrue()

        val withNoDrugAssociation = GeneAlterationFactory.convertAlteration(
            "",
            TestServeKnownFactory.createGeneAlteration(ServeGeneRole.UNKNOWN, ServeProteinEffect.UNKNOWN, false)
        )
        assertThat(withNoDrugAssociation.isAssociatedWithDrugResistance == false).isTrue()
    }
}