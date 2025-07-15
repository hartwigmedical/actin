package com.hartwig.actin.molecular.panel

import com.hartwig.actin.datamodel.clinical.SequencingTest
import com.hartwig.actin.datamodel.molecular.ExperimentType
import com.hartwig.actin.datamodel.molecular.PanelRecord
import com.hartwig.actin.datamodel.molecular.PanelSpecification
import com.hartwig.actin.datamodel.molecular.PanelSpecificationFunctions
import com.hartwig.actin.datamodel.molecular.PanelSpecifications
import com.hartwig.actin.datamodel.molecular.PanelTestSpecification
import com.hartwig.actin.datamodel.molecular.characteristics.HomologousRecombination
import com.hartwig.actin.datamodel.molecular.characteristics.HomologousRecombinationType
import com.hartwig.actin.datamodel.molecular.characteristics.MicrosatelliteStability
import com.hartwig.actin.datamodel.molecular.characteristics.MolecularCharacteristics
import com.hartwig.actin.datamodel.molecular.characteristics.TumorMutationalBurden
import com.hartwig.actin.datamodel.molecular.derivedGeneTargetMap
import com.hartwig.actin.datamodel.molecular.driver.Drivers
import com.hartwig.actin.molecular.MolecularAnnotator
import com.hartwig.actin.molecular.evidence.actionability.ActionabilityConstants
import com.hartwig.actin.molecular.util.ExtractionUtil
import java.time.LocalDate

private const val TMB_HIGH_CUTOFF = 10.0

class PanelAnnotator(
    private val registrationDate: LocalDate,
    private val panelVariantAnnotator: PanelVariantAnnotator,
    private val panelFusionAnnotator: PanelFusionAnnotator,
    private val panelCopyNumberAnnotator: PanelCopyNumberAnnotator,
    private val panelVirusAnnotator: PanelVirusAnnotator,
    private val panelDriverAttributeAnnotator: PanelDriverAttributeAnnotator,
    private val panelSpecifications: PanelSpecifications
) : MolecularAnnotator<SequencingTest, PanelRecord> {

    override fun annotate(input: SequencingTest): PanelRecord {
        return input
            .let(::interpret)
            .let(panelDriverAttributeAnnotator::annotate)
    }

    private fun interpret(input: SequencingTest): PanelRecord {
        val testVersion =
            PanelSpecificationFunctions.determineTestVersion(input, panelSpecifications.panelTestSpecifications, registrationDate)

        val specification = if (input.knownSpecifications) {
            panelSpecifications.panelSpecification(PanelTestSpecification(input.test, testVersion))
        } else PanelSpecification(
            derivedGeneTargetMap(input)
        )
        
        val annotatedVariants = panelVariantAnnotator.annotate(input.variants)
        val annotatedAmplifications = panelCopyNumberAnnotator.annotate(input.amplifications)
        val annotatedDeletions = panelCopyNumberAnnotator.annotate(input.deletions)
        val annotatedFusions = panelFusionAnnotator.annotate(input.fusions, input.skippedExons)
        val annotatedViruses = panelVirusAnnotator.annotate(input.viruses)
        
        return PanelRecord(
            specification = specification,
            experimentType = ExperimentType.PANEL,
            testTypeDisplay = input.test,
            date = input.date,
            drivers = Drivers(
                variants = annotatedVariants,
                copyNumbers = annotatedAmplifications + annotatedDeletions,
                homozygousDisruptions = emptyList(),
                disruptions = emptyList(),
                fusions = annotatedFusions,
                viruses = annotatedViruses
            ),
            characteristics = MolecularCharacteristics(
                purity = null,
                ploidy = null,
                predictedTumorOrigin = null,
                microsatelliteStability = input.isMicrosatelliteUnstable?.let {
                    MicrosatelliteStability(
                        microsatelliteIndelsPerMb = null,
                        isUnstable = it,
                        evidence = ExtractionUtil.noEvidence()
                    )
                },
                homologousRecombination = input.isHomologousRecombinationDeficient?.let {
                    HomologousRecombination(
                        score = null,
                        isDeficient = it,
                        type = if (!it) HomologousRecombinationType.NONE else null,
                        brca1Value = null,
                        brca2Value = null,
                        evidence = ExtractionUtil.noEvidence()
                    )
                },
                tumorMutationalBurden = input.tumorMutationalBurden?.let {
                    val isHigh = it > TMB_HIGH_CUTOFF
                    TumorMutationalBurden(
                        score = it,
                        isHigh = isHigh,
                        evidence = ExtractionUtil.noEvidence()
                    )
                },
                tumorMutationalLoad = null
            ),
            evidenceSource = ActionabilityConstants.EVIDENCE_SOURCE.display(),
            hasSufficientPurity = true,
            hasSufficientQuality = true,
            reportHash = input.reportHash
        )
    }
}