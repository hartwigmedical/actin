package com.hartwig.actin.molecular.orange.interpretation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.hartwig.actin.molecular.datamodel.driver.GeneAlteration;
import com.hartwig.actin.molecular.orange.evidence.known.TestServeKnownFactory;
import com.hartwig.serve.datamodel.common.GeneRole;
import com.hartwig.serve.datamodel.common.ProteinEffect;

import org.apache.logging.log4j.util.Strings;
import org.junit.Test;

public class GeneAlterationFactoryTest {

    @Test
    public void canConvertNullAlterations() {
        GeneAlteration nullAlteration = GeneAlterationFactory.convertAlteration("gene", null);

        assertEquals("gene", nullAlteration.gene());
        assertEquals(com.hartwig.actin.molecular.datamodel.driver.GeneRole.UNKNOWN, nullAlteration.geneRole());
        assertEquals(com.hartwig.actin.molecular.datamodel.driver.ProteinEffect.UNKNOWN, nullAlteration.proteinEffect());
        assertNull(nullAlteration.isAssociatedWithDrugResistance());
    }

    @Test
    public void canConvertAllRolesAndEffects() {
        for (GeneRole geneRole : GeneRole.values()) {
            for (ProteinEffect proteinEffect : ProteinEffect.values()) {
                GeneAlteration alteration = GeneAlterationFactory.convertAlteration(Strings.EMPTY,
                        TestServeKnownFactory.createGeneAlteration(geneRole, proteinEffect));
                assertNotNull(alteration.gene());
                assertNotNull(alteration.geneRole());
                assertNotNull(alteration.proteinEffect());
            }
        }
    }

    @Test
    public void canHandleDrugAssociations() {
        GeneAlteration withDrugAssociation = GeneAlterationFactory.convertAlteration(Strings.EMPTY,
                TestServeKnownFactory.createGeneAlteration(GeneRole.UNKNOWN, ProteinEffect.UNKNOWN, true));
        assertTrue(withDrugAssociation.isAssociatedWithDrugResistance());

        GeneAlteration withNoDrugAssociation = GeneAlterationFactory.convertAlteration(Strings.EMPTY,
                TestServeKnownFactory.createGeneAlteration(GeneRole.UNKNOWN, ProteinEffect.UNKNOWN, false));
        assertFalse(withNoDrugAssociation.isAssociatedWithDrugResistance());
    }
}