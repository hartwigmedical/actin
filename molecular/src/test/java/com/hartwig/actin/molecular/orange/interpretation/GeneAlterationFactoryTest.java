package com.hartwig.actin.molecular.orange.interpretation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.hartwig.actin.molecular.datamodel.driver.GeneAlteration;
import com.hartwig.actin.molecular.orange.evidence.TestEvidenceFactory;
import com.hartwig.serve.datamodel.common.GeneRole;
import com.hartwig.serve.datamodel.common.ProteinEffect;

import org.apache.logging.log4j.util.Strings;
import org.junit.Test;

public class GeneAlterationFactoryTest {

    @Test
    public void canConvertAllGeneAlterations() {
        GeneAlteration nullAlteration = GeneAlterationFactory.convertAlteration("gene", null);

        assertEquals("gene", nullAlteration.gene());
        assertEquals(com.hartwig.actin.molecular.datamodel.driver.GeneRole.UNKNOWN, nullAlteration.geneRole());
        assertEquals(com.hartwig.actin.molecular.datamodel.driver.ProteinEffect.UNKNOWN, nullAlteration.proteinEffect());
        assertNull(nullAlteration.isAssociatedWithDrugResistance());

        for (GeneRole geneRole : GeneRole.values()) {
            for (ProteinEffect proteinEffect : ProteinEffect.values()) {
                assertNotNull(GeneAlterationFactory.convertAlteration(Strings.EMPTY,
                        TestEvidenceFactory.createGeneAlteration(geneRole, proteinEffect)));
            }
        }

        GeneAlteration withDrugAssociation = GeneAlterationFactory.convertAlteration(Strings.EMPTY,
                TestEvidenceFactory.createGeneAlteration(GeneRole.UNKNOWN, ProteinEffect.UNKNOWN, true));
        assertTrue(withDrugAssociation.isAssociatedWithDrugResistance());

        GeneAlteration withNoDrugAssociation = GeneAlterationFactory.convertAlteration(Strings.EMPTY,
                TestEvidenceFactory.createGeneAlteration(GeneRole.UNKNOWN, ProteinEffect.UNKNOWN, false));
        assertFalse(withNoDrugAssociation.isAssociatedWithDrugResistance());
    }
}