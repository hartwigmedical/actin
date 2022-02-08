package com.hartwig.actin.molecular.orange.util;

import static org.junit.Assert.assertEquals;

import com.hartwig.actin.molecular.datamodel.FusionGene;

import org.junit.Test;

public class FusionParserTest {

    @Test
    public void canParseFusionEvent() {
        FusionGene fusion = FusionParser.fromEvidenceEvent("EML4 - ALK fusion");

        assertEquals("EML4", fusion.fiveGene());
        assertEquals("ALK", fusion.threeGene());
    }
}