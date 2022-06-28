package com.hartwig.actin.molecular.orange.interpretation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.hartwig.actin.molecular.datamodel.immunology.HlaAllele;
import com.hartwig.actin.molecular.datamodel.immunology.MolecularImmunology;
import com.hartwig.actin.molecular.orange.datamodel.TestOrangeFactory;

import org.junit.Test;

public class ImmunologyExtractionTest {

    private static final double EPSILON = 1.0E-10;

    @Test
    public void canExtractFromMinimalData() {
        assertNotNull(ImmunologyExtraction.extract(TestOrangeFactory.createMinimalTestOrangeRecord()));
    }

    @Test
    public void canExtractFromProperTestData() {
        MolecularImmunology immunology = ImmunologyExtraction.extract(TestOrangeFactory.createProperTestOrangeRecord());

        assertTrue(immunology.isReliable());
        assertEquals(1, immunology.hlaAlleles().size());
        HlaAllele hlaAllele = immunology.hlaAlleles().iterator().next();

        assertEquals("A*01:01", hlaAllele.name());
        assertEquals(1.2, hlaAllele.tumorCopyNumber(), EPSILON);
        assertFalse(hlaAllele.hasSomaticMutations());
    }
}