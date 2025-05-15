package com.hartwig.actin.molecular.panel

import com.hartwig.actin.datamodel.clinical.SequencedFusion
import com.hartwig.actin.datamodel.molecular.ExperimentType
import com.hartwig.actin.datamodel.molecular.MolecularTestTarget
import com.hartwig.actin.datamodel.molecular.PanelRecord
import com.hartwig.actin.datamodel.molecular.KnownPanelSpecification
import com.hartwig.actin.datamodel.molecular.characteristics.MolecularCharacteristics
import com.hartwig.actin.datamodel.molecular.driver.Drivers
import com.hartwig.actin.molecular.MolecularAnnotator
import com.hartwig.actin.molecular.evidence.actionability.ActionabilityConstants

class IhcAnnotator(private val panelFusionAnnotator: PanelFusionAnnotator) : MolecularAnnotator<IhcExtraction, PanelRecord> {

    override fun annotate(input: IhcExtraction): PanelRecord {
        return PanelRecord(
            date = input.date,
            specification = KnownPanelSpecification((input.fusionPositiveGenes + input.fusionNegativeGenes).associateWith {
                listOf(
                    MolecularTestTarget.FUSION
                )
            }),
            experimentType = ExperimentType.IHC,
            testTypeDisplay = ExperimentType.IHC.display(),
            drivers = Drivers(
                variants = emptyList(),
                copyNumbers = emptyList(),
                homozygousDisruptions = emptyList(),
                disruptions = emptyList(),
                fusions = panelFusionAnnotator.annotate(
                    input.fusionPositiveGenes.map { SequencedFusion(geneUp = it) }.toSet(),
                    emptySet()
                ),
                viruses = emptyList()
            ),
            characteristics = emptyCharacteristics(),
            evidenceSource = ActionabilityConstants.EVIDENCE_SOURCE.display(),
            hasSufficientPurity = true,
            hasSufficientQuality = true
        )
    }

    private fun emptyCharacteristics(): MolecularCharacteristics {
        return MolecularCharacteristics(
            purity = null,
            ploidy = null,
            predictedTumorOrigin = null,
            microsatelliteStability = null,
            homologousRecombination = null,
            tumorMutationalBurden = null,
            tumorMutationalLoad = null
        )
    }
}
