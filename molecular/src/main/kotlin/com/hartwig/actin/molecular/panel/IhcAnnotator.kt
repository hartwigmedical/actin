package com.hartwig.actin.molecular.panel

import com.hartwig.actin.datamodel.clinical.SequencedFusion
import com.hartwig.actin.datamodel.molecular.ExperimentType
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.MolecularTestTarget
import com.hartwig.actin.datamodel.molecular.RefGenomeVersion
import com.hartwig.actin.datamodel.molecular.characteristics.MolecularCharacteristics
import com.hartwig.actin.datamodel.molecular.driver.Drivers
import com.hartwig.actin.datamodel.molecular.immunology.MolecularImmunology
import com.hartwig.actin.datamodel.molecular.panel.PanelTargetSpecification
import com.hartwig.actin.molecular.MolecularAnnotator
import com.hartwig.actin.molecular.evidence.actionability.ActionabilityConstants

class IhcAnnotator(private val panelFusionAnnotator: PanelFusionAnnotator) : MolecularAnnotator<IhcExtraction> {

    override fun annotate(input: IhcExtraction): MolecularTest {
        return MolecularTest(
            date = input.date,
            sampleId = null,
            reportHash = null,
            experimentType = ExperimentType.IHC,
            testTypeDisplay = ExperimentType.IHC.display(),
            targetSpecification = PanelTargetSpecification((input.fusionPositiveGenes + input.fusionNegativeGenes).associateWith {
                listOf(
                    MolecularTestTarget.FUSION
                )
            }),
            refGenomeVersion = RefGenomeVersion.V37,
            containsTumorCells = true,
            hasSufficientPurity = true,
            hasSufficientQuality = true,
            isContaminated = false,
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
            immunology = emptyImmunology(),
            pharmaco = emptySet(),
            evidenceSource = ActionabilityConstants.EVIDENCE_SOURCE.display(),
            externalTrialSource = ActionabilityConstants.EXTERNAL_TRIAL_SOURCE.display()
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

    private fun emptyImmunology(): MolecularImmunology {
        return MolecularImmunology(isReliable = false, hlaAlleles = emptySet())
    }
}
