package com.hartwig.actin.molecular.orange.interpretation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.hartwig.actin.molecular.datamodel.driver.GeneAlteration;
import com.hartwig.actin.molecular.orange.evidence.datamodel.TestEvidenceFactory;
import com.hartwig.serve.datamodel.common.GeneRole;
import com.hartwig.serve.datamodel.common.ProteinEffect;

import org.apache.logging.log4j.util.Strings;
import org.junit.Test;

public class ExtractionUtilTest {

    private static final double EPSILON = 1.0E-10;

    @Test
    public void canKeep3Digits() {
        assertEquals(3, ExtractionUtil.keep3Digits(3D), EPSILON);
        assertEquals(3.123, ExtractionUtil.keep3Digits(3.123), EPSILON);
        assertEquals(3.123, ExtractionUtil.keep3Digits(3.123456789), EPSILON);
    }

    @Test
    public void canConvertAllGeneAlterations() {
        GeneAlteration nullAlteration = ExtractionUtil.convertAlteration("gene", null);

        assertEquals("gene", nullAlteration.gene());
        assertEquals(com.hartwig.actin.molecular.datamodel.driver.GeneRole.UNKNOWN, nullAlteration.geneRole());
        assertEquals(com.hartwig.actin.molecular.datamodel.driver.ProteinEffect.UNKNOWN, nullAlteration.proteinEffect());
        assertNull(nullAlteration.isAssociatedWithDrugResistance());

        for (GeneRole geneRole : GeneRole.values()) {
            for (ProteinEffect proteinEffect : ProteinEffect.values()) {
                assertNotNull(ExtractionUtil.convertAlteration(Strings.EMPTY,
                        TestEvidenceFactory.createGeneAlteration(geneRole, proteinEffect)));
            }
        }

        GeneAlteration withDrugAssociation = ExtractionUtil.convertAlteration(Strings.EMPTY,
                TestEvidenceFactory.createGeneAlteration(GeneRole.UNKNOWN, ProteinEffect.UNKNOWN, true));
        assertTrue(withDrugAssociation.isAssociatedWithDrugResistance());

        GeneAlteration withNoDrugAssociation = ExtractionUtil.convertAlteration(Strings.EMPTY,
                TestEvidenceFactory.createGeneAlteration(GeneRole.UNKNOWN, ProteinEffect.UNKNOWN, false));
        assertFalse(withNoDrugAssociation.isAssociatedWithDrugResistance());
    }
}