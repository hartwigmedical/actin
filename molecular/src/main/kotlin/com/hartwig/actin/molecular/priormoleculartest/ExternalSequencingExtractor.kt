package com.hartwig.actin.molecular.priormoleculartest

import com.hartwig.actin.clinical.datamodel.PriorSequencingTest
import com.hartwig.actin.molecular.MolecularExtractor
import com.hartwig.actin.molecular.datamodel.panel.PanelExtraction

class ExternalSequencingExtractor: MolecularExtractor<PriorSequencingTest, PanelExtraction> {
    override fun extract(input: List<PriorSequencingTest>): List<PanelExtraction> {
        return emptyList()
    }
}