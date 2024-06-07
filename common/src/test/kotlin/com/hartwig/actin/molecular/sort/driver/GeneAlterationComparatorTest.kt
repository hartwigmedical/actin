package com.hartwig.actin.molecular.sort.driver

import com.hartwig.actin.molecular.datamodel.GeneAlteration
import com.hartwig.actin.molecular.datamodel.GeneRole
import com.hartwig.actin.molecular.datamodel.ProteinEffect
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class GeneAlterationComparatorTest {

    @Test
    fun `Should sort gene alterations`() {
        val alteration1 = create("gene A", GeneRole.ONCO, ProteinEffect.GAIN_OF_FUNCTION)
        val alteration2 = create("gene A", GeneRole.TSG, ProteinEffect.GAIN_OF_FUNCTION)
        val alteration3 = create("gene A", GeneRole.TSG, ProteinEffect.NO_EFFECT)
        val alteration4 = create("gene B", GeneRole.ONCO, ProteinEffect.GAIN_OF_FUNCTION)
        val alterations = listOf(alteration2, alteration1, alteration4, alteration3).sortedWith(GeneAlterationComparator())

        assertThat(alterations[0]).isEqualTo(alteration1)
        assertThat(alterations[1]).isEqualTo(alteration2)
        assertThat(alterations[2]).isEqualTo(alteration3)
        assertThat(alterations[3]).isEqualTo(alteration4)
    }

    private fun create(gene: String, geneRole: GeneRole, proteinEffect: ProteinEffect): GeneAlteration {
        return object : GeneAlteration {
            override val gene: String
                get() = gene

            override val geneRole: GeneRole
                get() = geneRole

            override val proteinEffect: ProteinEffect
                get() = proteinEffect

            override val isAssociatedWithDrugResistance: Boolean?
                get() = null

            override fun toString(): String {
                return "$gene $geneRole $proteinEffect"
            }
        }
    }
}