package com.hartwig.actin.molecular.sort.driver

import com.google.common.collect.Lists
import com.hartwig.actin.molecular.datamodel.driver.GeneAlteration
import com.hartwig.actin.molecular.datamodel.driver.GeneRole
import com.hartwig.actin.molecular.datamodel.driver.ProteinEffect
import org.junit.Assert
import org.junit.Test

class GeneAlterationComparatorTest {
    @Test
    fun canSortGeneAlterations() {
        val alteration1 = create("gene A", GeneRole.ONCO, ProteinEffect.GAIN_OF_FUNCTION)
        val alteration2 = create("gene A", GeneRole.TSG, ProteinEffect.GAIN_OF_FUNCTION)
        val alteration3 = create("gene A", GeneRole.TSG, ProteinEffect.NO_EFFECT)
        val alteration4 = create("gene B", GeneRole.ONCO, ProteinEffect.GAIN_OF_FUNCTION)
        val alterations: List<GeneAlteration> = Lists.newArrayList(alteration2, alteration1, alteration4, alteration3)
        alterations.sort(GeneAlterationComparator())
        Assert.assertEquals(alteration1, alterations[0])
        Assert.assertEquals(alteration2, alterations[1])
        Assert.assertEquals(alteration3, alterations[2])
        Assert.assertEquals(alteration4, alterations[3])
    }

    companion object {
        private fun create(gene: String, geneRole: GeneRole, proteinEffect: ProteinEffect): GeneAlteration {
            return object : GeneAlteration {
                override fun gene(): String {
                    return gene
                }

                override fun geneRole(): GeneRole {
                    return geneRole
                }

                override fun proteinEffect(): ProteinEffect {
                    return proteinEffect
                }

                override val isAssociatedWithDrugResistance: Boolean?
                    get() = null

                override fun toString(): String {
                    return "$gene $geneRole $proteinEffect"
                }
            }
        }
    }
}