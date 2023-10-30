package com.hartwig.actin.molecular.orange.interpretation

import com.hartwig.actin.molecular.datamodel.driver.GeneRole
import com.hartwig.actin.molecular.datamodel.driver.ProteinEffect
import com.hartwig.actin.molecular.orange.evidence.known.TestServeKnownFactory
import org.apache.logging.log4j.util.Strings
import org.junit.Assert
import org.junit.Test

class GeneAlterationFactoryTest {
    @Test
    fun canConvertNullAlterations() {
        val nullAlteration = GeneAlterationFactory.convertAlteration("gene", null)
        Assert.assertEquals("gene", nullAlteration.gene())
        Assert.assertEquals(GeneRole.UNKNOWN, nullAlteration.geneRole())
        Assert.assertEquals(ProteinEffect.UNKNOWN, nullAlteration.proteinEffect())
        Assert.assertNull(nullAlteration.isAssociatedWithDrugResistance())
    }

    @Test
    fun canConvertAllRolesAndEffects() {
        for (geneRole in com.hartwig.serve.datamodel.common.GeneRole.values()) {
            for (proteinEffect in com.hartwig.serve.datamodel.common.ProteinEffect.values()) {
                val alteration = GeneAlterationFactory.convertAlteration(Strings.EMPTY,
                    TestServeKnownFactory.createGeneAlteration(geneRole, proteinEffect))
                Assert.assertNotNull(alteration.gene())
                Assert.assertNotNull(alteration.geneRole())
                Assert.assertNotNull(alteration.proteinEffect())
            }
        }
    }

    @Test
    fun canHandleDrugAssociations() {
        val withDrugAssociation = GeneAlterationFactory.convertAlteration(Strings.EMPTY,
            TestServeKnownFactory.createGeneAlteration(com.hartwig.serve.datamodel.common.GeneRole.UNKNOWN, com.hartwig.serve.datamodel.common.ProteinEffect.UNKNOWN, true))
        Assert.assertTrue(withDrugAssociation.isAssociatedWithDrugResistance() == true)
        val withNoDrugAssociation = GeneAlterationFactory.convertAlteration(Strings.EMPTY,
            TestServeKnownFactory.createGeneAlteration(com.hartwig.serve.datamodel.common.GeneRole.UNKNOWN, com.hartwig.serve.datamodel.common.ProteinEffect.UNKNOWN, false))
        Assert.assertFalse(withNoDrugAssociation.isAssociatedWithDrugResistance() == true)
    }
}