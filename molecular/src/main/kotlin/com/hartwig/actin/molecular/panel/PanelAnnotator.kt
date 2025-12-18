package com.hartwig.actin.molecular.panel

import com.hartwig.actin.datamodel.clinical.SequencingTest
import com.hartwig.actin.datamodel.molecular.ExperimentType
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.RefGenomeVersion
import com.hartwig.actin.datamodel.molecular.characteristics.HomologousRecombination
import com.hartwig.actin.datamodel.molecular.characteristics.HomologousRecombinationType
import com.hartwig.actin.datamodel.molecular.characteristics.MicrosatelliteStability
import com.hartwig.actin.datamodel.molecular.characteristics.MolecularCharacteristics
import com.hartwig.actin.datamodel.molecular.characteristics.TumorMutationalBurden
import com.hartwig.actin.datamodel.molecular.driver.Drivers
import com.hartwig.actin.datamodel.molecular.immunology.MolecularImmunology
import com.hartwig.actin.datamodel.molecular.immunology.HlaAllele
import com.hartwig.actin.datamodel.molecular.panel.PanelSpecificationFunctions
import com.hartwig.actin.datamodel.molecular.panel.PanelTargetSpecification
import com.hartwig.actin.molecular.MolecularAnnotator
import com.hartwig.actin.molecular.evidence.actionability.ActionabilityConstants
import com.hartwig.actin.molecular.util.ExtractionUtil
import com.hartwig.actin.datamodel.clinical.SequencedHlaAllele
import java.time.LocalDate

private const val TMB_HIGH_CUTOFF = 10.0
private val HLA_REGEX = Regex(pattern = """^(?:HLA-)?(?<gene>[A-Z0-9]+)\*(?<alleleGroup>\d{2,}):(?<hlaProtein>\d{2,})$""")

class PanelAnnotator(
    private val registrationDate: LocalDate,
    private val panelVariantAnnotator: PanelVariantAnnotator,
    private val panelFusionAnnotator: PanelFusionAnnotator,
    private val panelCopyNumberAnnotator: PanelCopyNumberAnnotator,
    private val panelVirusAnnotator: PanelVirusAnnotator,
    private val panelDriverAttributeAnnotator: PanelDriverAttributeAnnotator,
    private val panelSpecifications: PanelSpecifications
) : MolecularAnnotator<SequencingTest> {

    override fun annotate(input: SequencingTest): MolecularTest {
        return input
            .let(::interpret)
            .let(panelDriverAttributeAnnotator::annotate)
    }

    private fun interpret(input: SequencingTest): MolecularTest {
        val testVersion =
            PanelSpecificationFunctions.determineTestVersion(input, panelSpecifications.panelTestSpecifications, registrationDate)

        val specification = if (input.knownSpecifications) {
            panelSpecifications.panelTargetSpecification(input, testVersion)
        } else PanelTargetSpecification(PanelSpecificationFunctions.derivedGeneTargetMap(input))

        val annotatedVariants = panelVariantAnnotator.annotate(input.variants)
        val annotatedAmplifications = panelCopyNumberAnnotator.annotate(input.amplifications)
        val annotatedDeletions = panelCopyNumberAnnotator.annotate(input.deletions)
        val annotatedFusions = panelFusionAnnotator.annotate(input.fusions, input.skippedExons)
        val annotatedViruses = panelVirusAnnotator.annotate(input.viruses)

        val immunology = panelImmunology(input.hlaAlleles)

        return MolecularTest(
            date = input.date,
            sampleId = null,
            reportHash = input.reportHash,
            experimentType = ExperimentType.PANEL,
            testTypeDisplay = input.test,
            targetSpecification = specification,
            refGenomeVersion = RefGenomeVersion.V37,
            containsTumorCells = true,
            hasSufficientPurity = true,
            hasSufficientQuality = true,
            isContaminated = false,
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
            immunology = immunology,
            pharmaco = emptySet(),
            evidenceSource = ActionabilityConstants.EVIDENCE_SOURCE.display(),
            externalTrialSource = ActionabilityConstants.EXTERNAL_TRIAL_SOURCE.display()
        )
    }
}

internal fun panelImmunology(hlaAlleles: Set<SequencedHlaAllele>): MolecularImmunology? {
    val extractedAlleles = hlaAlleles.map { allele -> toMolecularHlaAllele(allele) }.toSet()
    return extractedAlleles.takeIf { it.isNotEmpty() }?.let { MolecularImmunology(isReliable = true, hlaAlleles = it) }
}

private fun toMolecularHlaAllele(hlaAllele: SequencedHlaAllele): HlaAllele {
    val match = HLA_REGEX.matchEntire(hlaAllele.name)
        ?: throw IllegalArgumentException("Can't extract HLA gene, alleleGroup and hlaProtein from '${hlaAllele.name}' (example: A*02:01)")
    val gene = match.groups["gene"]!!.value
    val alleleGroup = match.groups["alleleGroup"]!!.value
    val hlaProtein = match.groups["hlaProtein"]!!.value
    val normalizedAllele = hlaAllele.name.removePrefix("HLA-")

    return HlaAllele(
        gene = "HLA-$gene",
        alleleGroup = alleleGroup,
        hlaProtein = hlaProtein,
        tumorCopyNumber = hlaAllele.tumorCopyNumber,
        hasSomaticMutations = hlaAllele.hasSomaticMutations,
        evidence = ExtractionUtil.noEvidence(),
        event = "HLA-$normalizedAllele"
    )
}
