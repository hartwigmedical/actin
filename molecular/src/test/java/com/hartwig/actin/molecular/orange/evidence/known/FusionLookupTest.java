package com.hartwig.actin.molecular.orange.evidence.known;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxFusion;
import com.hartwig.actin.molecular.orange.datamodel.linx.TestLinxFactory;
import com.hartwig.serve.datamodel.fusion.KnownFusion;

import org.junit.Test;

public class FusionLookupTest {

    @Test
    public void canLookupFusions() {
        KnownFusion fusion1 = TestServeKnownFactory.fusionBuilder().geneUp("up").geneDown("down").build();
        KnownFusion fusion2 = TestServeKnownFactory.fusionBuilder().geneUp("up").geneDown("down").minExonUp(3).maxExonUp(3).build();
        KnownFusion fusion3 = TestServeKnownFactory.fusionBuilder().geneUp("up").geneDown("down").minExonDown(4).maxExonDown(4).build();
        KnownFusion fusion4 = TestServeKnownFactory.fusionBuilder()
                .geneUp("up")
                .geneDown("down")
                .minExonUp(3)
                .maxExonUp(3)
                .minExonDown(4)
                .maxExonDown(4)
                .build();

        List<KnownFusion> knownFusions = Lists.newArrayList(fusion1, fusion2, fusion3, fusion4);

        LinxFusion broadMatch = TestLinxFactory.fusionBuilder().geneStart("up").geneEnd("down").fusedExonUp(2).fusedExonDown(5).build();
        assertEquals(fusion1, FusionLookup.find(knownFusions, broadMatch));

        LinxFusion specificUpMatch = TestLinxFactory.fusionBuilder().geneStart("up").geneEnd("down").fusedExonUp(3).fusedExonDown(5).build();
        assertEquals(fusion2, FusionLookup.find(knownFusions, specificUpMatch));

        LinxFusion specificDownMatch = TestLinxFactory.fusionBuilder().geneStart("up").geneEnd("down").fusedExonUp(2).fusedExonDown(4).build();
        assertEquals(fusion3, FusionLookup.find(knownFusions, specificDownMatch));

        LinxFusion specificMatch = TestLinxFactory.fusionBuilder().geneStart("up").geneEnd("down").fusedExonUp(3).fusedExonDown(4).build();
        assertEquals(fusion4, FusionLookup.find(knownFusions, specificMatch));

        LinxFusion noMatch = TestLinxFactory.fusionBuilder().geneStart("down").geneEnd("up").build();
        assertNull(FusionLookup.find(knownFusions, noMatch));
    }
}