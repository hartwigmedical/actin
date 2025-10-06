package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.driver.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.driver.Variant
import com.hartwig.actin.datamodel.molecular.driver.VirusType
import com.hartwig.actin.datamodel.molecular.evidence.Actionable
import com.hartwig.actin.molecular.evidence.matching.GeneMatching
import com.hartwig.actin.molecular.evidence.matching.HotspotMatching
import com.hartwig.actin.molecular.evidence.matching.RangeMatching
import com.hartwig.serve.datamodel.efficacy.EfficacyEvidence
import com.hartwig.serve.datamodel.molecular.MolecularCriterium
import com.hartwig.serve.datamodel.molecular.characteristic.ActionableCharacteristic
import com.hartwig.serve.datamodel.molecular.characteristic.TumorCharacteristicType
import com.hartwig.serve.datamodel.molecular.fusion.ActionableFusion
import com.hartwig.serve.datamodel.molecular.gene.ActionableGene
import com.hartwig.serve.datamodel.molecular.hotspot.ActionableHotspot
import com.hartwig.serve.datamodel.molecular.range.RangeAnnotation
import com.hartwig.serve.datamodel.trial.ActionableTrial
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

typealias MatchesForActionable = Map<Actionable, ActionabilityMatch>

class ActionabilityMatcher(
    private val evidences: List<EfficacyEvidence>,
    private val trials: List<ActionableTrial>,
    private val indirectEvidenceMatcher: IndirectEvidenceMatcher
) {

    val logger: Logger = LogManager.getLogger(ActionabilityMatcher::class.java)

    fun match(molecularTest: MolecularTest): MatchesForActionable {
        val evidenceMatches = match(molecularTest, evidences) { listOf(it.molecularCriterium()) }
        val trialMatches = match(molecularTest, trials) { it.anyMolecularCriteria().toList() }
        val indirectMatches = findIndirectEvidenceMatchesForVariants(molecularTest, evidenceMatches)
        val allActionables = evidenceMatches.keys + trialMatches.keys + indirectMatches.keys

        return allActionables.associateWith { actionable ->
            val evidenceMatch = evidenceMatches[actionable] ?: emptySet()
            val trialMatch = trialMatches[actionable] ?: emptySet()
            ActionabilityMatch(
                evidenceMatch.map { it.first },
                indirectMatches[actionable] ?: emptyList(),
                trialMatch.groupBy { it.first }.mapValues { it.value.map { pair -> pair.second }.toSet() }
            )
        }
    }

    private fun findIndirectEvidenceMatchesForVariants(
        molecularTest: MolecularTest,
        evidenceMatches: Map<Actionable, Set<Pair<EfficacyEvidence, MolecularCriterium>>>
    ): Map<Actionable, List<EfficacyEvidence>> {
        return molecularTest.drivers.variants
            .mapNotNull { variant -> findIndirectEvidenceMatchesForVariant(variant, evidenceMatches) }
            .toMap()
    }

    private fun findIndirectEvidenceMatchesForVariant(
        variant: Variant,
        evidenceMatches: Map<Actionable, Set<Pair<EfficacyEvidence, MolecularCriterium>>>
    ): Pair<Variant, List<EfficacyEvidence>>? {
        val indirectEvidences = indirectEvidenceMatcher.findIndirectEvidence(variant)
            .filterNot { evidence ->
                evidenceMatches[variant]?.any { (directEvidence, _) -> directEvidence == evidence } == true
            }
            .distinct()
        logIndirectEvidence(variant, indirectEvidences, evidenceMatches[variant])

        return if (indirectEvidences.isEmpty()) {
            null
        } else {
            variant to indirectEvidences
        }
    }

    private fun logIndirectEvidence(
        variant: Variant,
        indirectEvidences: List<EfficacyEvidence>,
        directMatches: Set<Pair<EfficacyEvidence, MolecularCriterium>>?
    ) {
        val uniqueTreatmentCount = indirectEvidences.map { it.treatment().name() }.toSet().size
        val directTreatmentNames = directMatches
            ?.map { (directEvidence, _) -> directEvidence.treatment().name() }
            ?.toSet()
            .orEmpty()
        val uniqueNovelTreatments = indirectEvidences
            .map { it.treatment().name() }
            .filterNot { directTreatmentNames.contains(it) }
            .toSet()
        logger.info(
            "Retaining {} indirect evidence matches ({} unique treatments, {} novel treatments: {}) for variant {}",
            indirectEvidences.size,
            uniqueTreatmentCount,
            uniqueNovelTreatments.size,
            uniqueNovelTreatments.sorted().joinToString(", "),
            variant.event
        )

        if (indirectEvidences.isNotEmpty()) {
            logger.info("Retained indirect evidence details for variant {}:", variant.event)
            indirectEvidences
                .sortedWith(compareBy({ it.evidenceLevel() }, { it.treatment().name() }))
                .forEach { evidence ->
                    logger.info(
                        " - Treatment: {} | Level: {} | Direction: {} | Indication: {} | Class: {} | Source event: {}",
                        evidence.treatment().name(),
                        evidence.evidenceLevel(),
                        evidence.evidenceDirection(),
                        evidence.indication().applicableType().name(),
                        evidence.treatment().treatmentApproachesDrugClass(),
                        evidence.molecularCriterium().hotspots().firstOrNull()?.sourceEvent()
                    )
                }
        }
    }

    private fun <T> match(molecularTest: MolecularTest, toMatch: List<T>, criteriumExtraction: (T) -> List<MolecularCriterium>) =
        toMatch.flatMap {
            criteriumExtraction.invoke(it).map { criterium -> it to criterium }
        }.flatMap { (instanceToMatch, criterium) ->
            when (val result = match(molecularTest, criterium)) {
                is ActionabilityMatchResult.Success -> result.actionables.map { actionable -> actionable to (instanceToMatch to criterium) }
                is ActionabilityMatchResult.Failure -> emptyList()
            }
        }.groupBy(keySelector = { it.first }, valueTransform = { it.second }).mapValues { it.value.toSet() }

    private fun match(molecularTest: MolecularTest, criterium: MolecularCriterium): ActionabilityMatchResult =
        ActionabilityMatchResult.combine(
            sequenceOf(
                ::matchHotspots,
                ::matchCodons,
                ::matchExons,
                ::matchGenes,
                ::matchFusions,
                ::matchCharacteristics,
                ::matchHlas
            ).map { matchFunc -> matchFunc(molecularTest, criterium) }
        )

    private fun matchHotspots(molecularTest: MolecularTest, criterium: MolecularCriterium): ActionabilityMatchResult {
        val variantMatches = criterium.hotspots()
            .map { hotspot -> matchHotspot(molecularTest, hotspot) }

        return ActionabilityMatchResult.combine(variantMatches)
    }

    private fun matchHotspot(molecularTest: MolecularTest, hotspot: ActionableHotspot): ActionabilityMatchResult {
        val matches = molecularTest.drivers.variants.filter { variant -> HotspotMatching.isMatch(hotspot, variant) }
        return successWhenNotEmpty(matches)
    }

    private fun matchCodons(molecularTest: MolecularTest, criterium: MolecularCriterium): ActionabilityMatchResult {
        val codonMatches = criterium.codons().map { codon -> matchCodon(molecularTest, codon) }

        return ActionabilityMatchResult.combine(codonMatches)
    }

    private fun matchCodon(molecularTest: MolecularTest, codon: RangeAnnotation): ActionabilityMatchResult {
        val matches = molecularTest.drivers.variants.filter { variant -> RangeMatching.isMatch(codon, variant) }

        return successWhenNotEmpty(matches)
    }

    private fun matchExons(molecularTest: MolecularTest, criterium: MolecularCriterium): ActionabilityMatchResult {
        val exonMatches = criterium.exons().map { exon -> matchExon(molecularTest, exon) }

        return ActionabilityMatchResult.combine(exonMatches)
    }

    private fun matchExon(molecularTest: MolecularTest, exon: RangeAnnotation): ActionabilityMatchResult {
        val matches = molecularTest.drivers.variants.filter { variant ->
            VariantEvidence.isVariantEligible(variant) && RangeMatching.isMatch(exon, variant)
        }

        return successWhenNotEmpty(matches)
    }

    private fun matchGenes(molecularTest: MolecularTest, criterium: MolecularCriterium): ActionabilityMatchResult {
        val geneMatches = criterium.genes().map { gene -> matchGene(molecularTest, gene) }

        return ActionabilityMatchResult.combine(geneMatches)
    }

    private fun matchGene(molecularTest: MolecularTest, gene: ActionableGene): ActionabilityMatchResult {
        val variantMatches = molecularTest.drivers.variants
            .filter { variant ->
                VariantEvidence.isVariantEligible(variant) && VariantEvidence.isGeneEventEligible(gene) && GeneMatching.isMatch(
                    gene,
                    variant
                )
            }

        val promiscuousFusionMatches = if (FusionEvidence.isPromiscuousFusionEvent(gene.event())) {
            molecularTest.drivers.fusions.filter { fusion -> FusionEvidence.isPromiscuousMatch(gene, fusion) }
        } else {
            emptyList()
        }

        val homozygousDisruptionMatches =
            if (HomozygousDisruptionEvidence.isHomozygousDisruptionEvent(gene)) {
                molecularTest.drivers.homozygousDisruptions.filter { homozygousDisruption ->
                    HomozygousDisruptionEvidence.isHomozygousDisruptionMatch(
                        gene,
                        homozygousDisruption
                    )
                }
            } else {
                emptyList()
            }

        val copyNumberAmplificationMatches = if (CopyNumberEvidence.isAmplificationEvent(gene.event())) {
            molecularTest.drivers.copyNumbers.filter { copyNumber -> CopyNumberEvidence.isAmplificationMatch(gene, copyNumber) }
        } else {
            emptyList()
        }

        val copyNumberDeletionMatches =
            if (CopyNumberEvidence.isDeletionEvent(gene)) {
                molecularTest.drivers.copyNumbers.filter { copyNumber -> CopyNumberEvidence.isDeletionMatch(gene, copyNumber) }
            } else {
                emptyList()
            }

        return successWhenNotEmpty(
            variantMatches + promiscuousFusionMatches + homozygousDisruptionMatches
                    + copyNumberAmplificationMatches + copyNumberDeletionMatches
        )
    }

    private fun matchFusions(molecularTest: MolecularTest, criterium: MolecularCriterium): ActionabilityMatchResult {
        val fusionMatches = criterium.fusions()
            .map { fusion -> matchFusion(molecularTest, fusion) }

        return ActionabilityMatchResult.combine(fusionMatches)
    }

    private fun matchFusion(molecularTest: MolecularTest, fusion: ActionableFusion): ActionabilityMatchResult {
        val matches = molecularTest.drivers.fusions
            .filter { driverFusion ->
                FusionEvidence.isFusionMatch(fusion, driverFusion)
            }

        return successWhenNotEmpty(matches)
    }

    private fun matchCharacteristics(molecularTest: MolecularTest, criterium: MolecularCriterium): ActionabilityMatchResult {
        val characteristicMatches = criterium.characteristics()
            .map { characteristic -> matchCharacteristic(molecularTest, characteristic) }

        return ActionabilityMatchResult.combine(characteristicMatches)
    }

    private fun matchCharacteristic(molecularTest: MolecularTest, characteristic: ActionableCharacteristic): ActionabilityMatchResult {
        return when (characteristic.type()) {
            TumorCharacteristicType.MICROSATELLITE_UNSTABLE -> {
                matchMicrosatelliteStability(
                    molecularTest,
                    requireMicrosatelliteUnstable = true
                )
            }

            TumorCharacteristicType.MICROSATELLITE_STABLE -> {
                matchMicrosatelliteStability(
                    molecularTest,
                    requireMicrosatelliteUnstable = false
                )
            }

            TumorCharacteristicType.HIGH_TUMOR_MUTATIONAL_LOAD -> matchTumorMutationalLoad(molecularTest, requireTmlHigh = true)

            TumorCharacteristicType.LOW_TUMOR_MUTATIONAL_LOAD -> matchTumorMutationalLoad(molecularTest, requireTmlHigh = false)

            TumorCharacteristicType.HIGH_TUMOR_MUTATIONAL_BURDEN -> matchTumorMutationalBurden(molecularTest, requireTmbHigh = true)

            TumorCharacteristicType.LOW_TUMOR_MUTATIONAL_BURDEN -> matchTumorMutationalBurden(molecularTest, requireTmbHigh = false)

            TumorCharacteristicType.HOMOLOGOUS_RECOMBINATION_DEFICIENT -> {
                molecularTest.characteristics.homologousRecombination?.let { hr ->
                    if (hr.isDeficient) {
                        ActionabilityMatchResult.Success(listOf(hr))
                    } else {
                        ActionabilityMatchResult.Failure
                    }
                } ?: ActionabilityMatchResult.Failure
            }

            TumorCharacteristicType.HPV_POSITIVE -> {
                val hits =
                    molecularTest.drivers.viruses.filter { virus ->
                        virus.type == VirusType.HPV && virus.driverLikelihood == DriverLikelihood.HIGH
                    }
                if (hits.isNotEmpty()) {
                    ActionabilityMatchResult.Success(hits)
                } else {
                    ActionabilityMatchResult.Failure
                }
            }

            TumorCharacteristicType.EBV_POSITIVE -> {
                val hits =
                    molecularTest.drivers.viruses.filter { virus ->
                        virus.type == VirusType.EBV && virus.driverLikelihood == DriverLikelihood.HIGH
                    }
                if (hits.isNotEmpty()) {
                    ActionabilityMatchResult.Success(hits)
                } else {
                    ActionabilityMatchResult.Failure
                }
            }
        }
    }

    private fun matchHlas(molecularTest: MolecularTest, criterium: MolecularCriterium): ActionabilityMatchResult {
        return if (criterium.hla().isEmpty()) {
            ActionabilityMatchResult.Success()
        } else {
            logger.warn("Evidence contains HLA but matching in ACTIN not supported")
            ActionabilityMatchResult.Failure
        }
    }

    private fun matchMicrosatelliteStability(
        molecularTest: MolecularTest,
        requireMicrosatelliteUnstable: Boolean
    ): ActionabilityMatchResult {
        return molecularTest.characteristics.microsatelliteStability?.let { msi ->
            if (requireMicrosatelliteUnstable && msi.isUnstable || !requireMicrosatelliteUnstable && !msi.isUnstable) {
                ActionabilityMatchResult.Success(listOf(msi))
            } else {
                ActionabilityMatchResult.Failure
            }
        } ?: ActionabilityMatchResult.Failure
    }

    private fun matchTumorMutationalBurden(molecularTest: MolecularTest, requireTmbHigh: Boolean): ActionabilityMatchResult {
        return molecularTest.characteristics.tumorMutationalBurden?.let { tmb ->
            if (requireTmbHigh && tmb.isHigh || !requireTmbHigh && !tmb.isHigh) {
                ActionabilityMatchResult.Success(listOf(tmb))
            } else {
                ActionabilityMatchResult.Failure
            }
        } ?: ActionabilityMatchResult.Failure
    }

    private fun matchTumorMutationalLoad(molecularTest: MolecularTest, requireTmlHigh: Boolean): ActionabilityMatchResult {
        return molecularTest.characteristics.tumorMutationalLoad?.let { tml ->
            if (requireTmlHigh && tml.isHigh || !requireTmlHigh && !tml.isHigh) {
                ActionabilityMatchResult.Success(listOf(tml))
            } else {
                ActionabilityMatchResult.Failure
            }
        } ?: ActionabilityMatchResult.Failure
    }

    companion object {
        fun successWhenNotEmpty(matches: List<Actionable>): ActionabilityMatchResult {
            return if (matches.isEmpty()) {
                ActionabilityMatchResult.Failure
            } else {
                ActionabilityMatchResult.Success(matches)
            }
        }
    }
}
