package com.hartwig.actin.molecular.priormoleculartest

import com.hartwig.actin.clinical.datamodel.PriorSequencingTest
import com.hartwig.actin.molecular.MolecularExtractor
import com.hartwig.actin.molecular.datamodel.panel.PanelExtraction
import com.hartwig.actin.molecular.datamodel.panel.PanelExtractionAdapter

class PriorSequencingExtractor : MolecularExtractor<PriorSequencingTest, PanelExtraction> {
    override fun extract(input: List<PriorSequencingTest>): List<PanelExtraction> {
        return input.map { PanelExtractionAdapter(it) }
    }
}

