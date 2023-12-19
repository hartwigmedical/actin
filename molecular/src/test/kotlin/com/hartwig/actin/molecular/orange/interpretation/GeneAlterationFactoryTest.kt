package com.hartwig.actin.molecular.orange.interpretation

import com.hartwig.actin.molecular.datamodel.driver.GeneRole
import com.hartwig.actin.molecular.datamodel.driver.ProteinEffect
import com.hartwig.actin.molecular.orange.evidence.known.TestServeKnownFactory
import org.apache.logging.log4j.util.Strings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GeneAlterationFactoryTest {

    @Test
    fun canConvertNullAlterations() {
        val nullAlteration = GeneAlterationFactory.convertAlteration("gene", null)
        assertEquals("gene", nullAlteration.gene())
        assertEquals(GeneRole.UNKNOWN, nullAlteration.geneRole())
        assertEquals(ProteinEffect.UNKNOWN, nullAlteration.proteinEffect())
        assertNull(nullAlteration.isAssociatedWithDrugResistance)
    }

    @Test
    fun canConvertAllRolesAndEffects() {
        for (geneRole in com.hartwig.serve.datamodel.common.GeneRole.values()) {
            for (proteinEffect in com.hartwig.serve.datamodel.common.ProteinEffect.values()) {
                val alteration = GeneAlterationFactory.convertAlteration(
                    Strings.EMPTY,
                    TestServeKnownFactory.createGeneAlteration(geneRole, proteinEffect)
                )
                assertNotNull(alteration.gene())
                assertNotNull(alteration.geneRole())
                assertNotNull(alteration.proteinEffect())
            }
        }
    }

    @Test
    fun canHandleDrugAssociations() {
        val withDrugAssociation = GeneAlterationFactory.convertAlteration(
            Strings.EMPTY,
            TestServeKnownFactory.createGeneAlteration(
                com.hartwig.serve.datamodel.common.GeneRole.UNKNOWN,
                com.hartwig.serve.datamodel.common.ProteinEffect.UNKNOWN,
                true
            )
        )
        assertTrue(withDrugAssociation.isAssociatedWithDrugResistance == true)

        val withNoDrugAssociation = GeneAlterationFactory.convertAlteration(
            Strings.EMPTY,
            TestServeKnownFactory.createGeneAlteration(
                com.hartwig.serve.datamodel.common.GeneRole.UNKNOWN,
                com.hartwig.serve.datamodel.common.ProteinEffect.UNKNOWN,
                false
            )
        )
        assertFalse(withNoDrugAssociation.isAssociatedWithDrugResistance == true)
    }
}