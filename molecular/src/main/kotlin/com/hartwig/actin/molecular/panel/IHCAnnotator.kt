package com.hartwig.actin.molecular.panel

import com.hartwig.actin.datamodel.clinical.SequencedFusion
import com.hartwig.actin.datamodel.molecular.Drivers
import com.hartwig.actin.datamodel.molecular.ExperimentType
import com.hartwig.actin.datamodel.molecular.MolecularCharacteristics
import com.hartwig.actin.datamodel.molecular.PanelRecord
import com.hartwig.actin.molecular.MolecularAnnotator
import com.hartwig.actin.molecular.evidence.actionability.ActionabilityConstants

class IHCAnnotator(
    private val panelFusionAnnotator: PanelFusionAnnotator
) :
    MolecularAnnotator<IHCExtraction, PanelRecord> {
    override fun annotate(input: IHCExtraction): PanelRecord {
        return PanelRecord(
            date = input.date,
            testedGenes = input.fusionPositiveGenes + input.fusionNegativeGenes,
            experimentType = ExperimentType.IHC,
            testTypeDisplay = ExperimentType.IHC.display(),
            drivers = Drivers(
                fusions = panelFusionAnnotator.annotate(
                    input.fusionPositiveGenes.map { SequencedFusion(geneUp = it) }.toSet(),
                    emptySet()
                )
            ),
            characteristics = MolecularCharacteristics(),
            evidenceSource = ActionabilityConstants.EVIDENCE_SOURCE.display(),
            hasSufficientPurity = true,
            hasSufficientQuality = true
        )
    }
}
