package com.hartwig.actin.molecular.priormoleculartest

import com.hartwig.actin.molecular.MolecularAnnotator
import com.hartwig.actin.molecular.datamodel.ExperimentType
import com.hartwig.actin.molecular.datamodel.MolecularCharacteristics
import com.hartwig.actin.molecular.datamodel.panel.PanelDrivers
import com.hartwig.actin.molecular.datamodel.panel.PanelRecord
import com.hartwig.actin.molecular.datamodel.panel.generic.GenericPanelExtraction
import com.hartwig.actin.molecular.evidence.actionability.ActionabilityConstants

class GenericPanelAnnotator : MolecularAnnotator<GenericPanelExtraction, PanelRecord> {
    override fun annotate(input: GenericPanelExtraction): PanelRecord {
        return PanelRecord(
            testedGenes = input.testedGenes(),
            genericPanelExtraction = input,
            type = ExperimentType.GENERIC_PANEL,
            date = input.date,
            drivers = PanelDrivers(),
            characteristics = MolecularCharacteristics(),
            evidenceSource = ActionabilityConstants.EVIDENCE_SOURCE.display()
        )
    }
}