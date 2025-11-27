package com.hartwig.actin.molecular.panel

import com.hartwig.actin.datamodel.molecular.ExperimentType
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.MolecularTestTarget
import com.hartwig.actin.datamodel.molecular.RefGenomeVersion
import com.hartwig.actin.datamodel.molecular.characteristics.MolecularCharacteristics
import com.hartwig.actin.datamodel.molecular.driver.Drivers
import com.hartwig.actin.datamodel.molecular.panel.PanelTargetSpecification
import com.hartwig.actin.molecular.MolecularAnnotator
import com.hartwig.actin.molecular.evidence.actionability.ActionabilityConstants

class IhcAnnotator() : MolecularAnnotator<IhcExtraction> {

    override fun annotate(input: IhcExtraction): MolecularTest {
        val geneTargetMap = (input.fusionTestedGenes).associateWith { listOf(MolecularTestTarget.FUSION) } +
                input.mutationAndDeletionTestedGenes.associateWith {
                    listOf(
                        MolecularTestTarget.MUTATION,
                        MolecularTestTarget.DELETION
                    )
                }

        return MolecularTest(
            date = input.date,
            sampleId = null,
            reportHash = null,
            experimentType = ExperimentType.IHC,
            testTypeDisplay = ExperimentType.IHC.display(),
            targetSpecification = PanelTargetSpecification(geneTargetMap),
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
                fusions = emptyList(),
                viruses = emptyList()
            ),
            characteristics = emptyCharacteristics(),
            immunology = null,
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
}
