package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.datamodel.molecular.MolecularTest
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

class CombinedEvidenceMatcher(private val evidences: List<EfficacyEvidence>, private val trials: List<ActionableTrial>) {
    val logger: Logger = LogManager.getLogger(CombinedEvidenceMatcher::class.java)

    fun match(molecularTest: MolecularTest): MatchesForActionable {
        val evidences = match(molecularTest, evidences) { listOf(it.molecularCriterium()) }
        val trials = match(molecularTest, trials) { it.anyMolecularCriteria().toList() }

        return evidences.entries.associate { entry ->
            val trialMatches = trials[entry.key] ?: emptySet()
            entry.key to ActionabilityMatch(
                entry.value.map { it.first }.toList(),
                trialMatches.groupBy { it.first }.mapValues { it.value.map { p -> p.second }.toSet() }
            )
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
        val matches = molecularTest.drivers.variants
            .filter { variant ->
                VariantEvidence.isVariantEligible(variant) && HotspotMatching.isMatch(hotspot, variant)
            }

        return successWhenNotEmpty(matches)
    }

    private fun matchCodons(molecularTest: MolecularTest, criterium: MolecularCriterium): ActionabilityMatchResult {
        val codonMatches = criterium.codons()
            .map { codon -> matchCodon(molecularTest, codon) }

        return ActionabilityMatchResult.combine(codonMatches)
    }

    private fun matchCodon(molecularTest: MolecularTest, codon: RangeAnnotation): ActionabilityMatchResult {
        val matches = molecularTest.drivers.variants
            .filter { variant ->
                VariantEvidence.isVariantEligible(variant) && RangeMatching.isMatch(codon, variant)
            }

        return successWhenNotEmpty(matches)
    }

    private fun matchExons(molecularTest: MolecularTest, criterium: MolecularCriterium): ActionabilityMatchResult {
        val exonMatches = criterium.exons()
            .map { exon -> matchExon(molecularTest, exon) }

        return ActionabilityMatchResult.combine(exonMatches)
    }

    private fun matchExon(molecularTest: MolecularTest, exon: RangeAnnotation): ActionabilityMatchResult {
        val matches = molecularTest.drivers.variants
            .filter { variant ->
                VariantEvidence.isVariantEligible(variant) && RangeMatching.isMatch(exon, variant)
            }

        return successWhenNotEmpty(matches)
    }

    private fun matchGenes(molecularTest: MolecularTest, criterium: MolecularCriterium): ActionabilityMatchResult {
        val geneMatches = criterium.genes()
            .map { gene -> matchGene(molecularTest, gene) }

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
            molecularTest.drivers.fusions
                .filter { fusion ->
                    FusionEvidence.isPromiscuousMatch(gene, fusion)
                }
        } else {
            emptyList()
        }

        val disruptionMatches = if (DisruptionEvidence.isDisruptionEvent(gene.event())) {
            molecularTest.drivers.disruptions
                .filter { disruption ->
                    DisruptionEvidence.isDisruptionMatch(gene, disruption)
                }
        } else {
            emptyList()
        }

        val homozygousDisruptionMatches = if (HomozygousDisruptionEvidence.isHomozygousDisruptionEvent(gene.event())) {
            molecularTest.drivers.homozygousDisruptions
                .filter { homozygousDisruption ->
                    HomozygousDisruptionEvidence.isHomozygousDisruptionMatch(gene, homozygousDisruption)
                }
        } else {
            emptyList()
        }

        val copyNumberAmplificationMatches = if (CopyNumberEvidence.isAmplificationEvent(gene.event())) {
            molecularTest.drivers.copyNumbers
                .filter { copyNumber ->
                    CopyNumberEvidence.isAmplificationMatch(gene, copyNumber)
                }
        } else {
            emptyList()
        }

        val copyNumberDeletionmatches = if (CopyNumberEvidence.isDeletionEvent(gene.event())) {
            molecularTest.drivers.copyNumbers
                .filter { copyNumber ->
                    CopyNumberEvidence.isDeletionMatch(gene, copyNumber)
                }
        } else {
            emptyList()
        }

        return successWhenNotEmpty(
            variantMatches + promiscuousFusionMatches + disruptionMatches +
                    homozygousDisruptionMatches + copyNumberAmplificationMatches + copyNumberDeletionmatches
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
            TumorCharacteristicType.MICROSATELLITE_STABLE -> {
                molecularTest.characteristics.microsatelliteStability?.let { msi ->
                    if (msi.isUnstable) {
                        ActionabilityMatchResult.Failure
                    } else {
                        ActionabilityMatchResult.Success(listOf(msi))
                    }
                } ?: ActionabilityMatchResult.Failure
            }

            TumorCharacteristicType.MICROSATELLITE_UNSTABLE -> {
                molecularTest.characteristics.microsatelliteStability?.let { msi ->
                    if (msi.isUnstable) {
                        ActionabilityMatchResult.Success(listOf(msi))
                    } else {
                        ActionabilityMatchResult.Failure
                    }
                } ?: ActionabilityMatchResult.Failure
            }

            TumorCharacteristicType.HIGH_TUMOR_MUTATIONAL_LOAD -> {
                molecularTest.characteristics.tumorMutationalLoad?.let { tml ->
                    if (tml.isHigh) {
                        ActionabilityMatchResult.Success(listOf(tml))
                    } else {
                        ActionabilityMatchResult.Failure
                    }
                } ?: ActionabilityMatchResult.Failure
            }

            TumorCharacteristicType.LOW_TUMOR_MUTATIONAL_LOAD -> {
                molecularTest.characteristics.tumorMutationalLoad?.let { tml ->
                    if (tml.isHigh) {
                        ActionabilityMatchResult.Failure
                    } else {
                        ActionabilityMatchResult.Success(listOf(tml))
                    }
                } ?: ActionabilityMatchResult.Failure
            }

            TumorCharacteristicType.HIGH_TUMOR_MUTATIONAL_BURDEN -> {
                molecularTest.characteristics.tumorMutationalLoad?.let { tml ->
                    if (tml.isHigh) {
                        ActionabilityMatchResult.Success(listOf(tml))

                    } else {
                        ActionabilityMatchResult.Failure
                    }
                } ?: ActionabilityMatchResult.Failure
            }

            TumorCharacteristicType.LOW_TUMOR_MUTATIONAL_BURDEN -> {
                molecularTest.characteristics.tumorMutationalLoad?.let { tml ->
                    if (tml.isHigh) {
                        ActionabilityMatchResult.Failure
                    } else {
                        ActionabilityMatchResult.Success(listOf(tml))
                    }
                } ?: ActionabilityMatchResult.Failure
            }

            TumorCharacteristicType.HOMOLOGOUS_RECOMBINATION_DEFICIENT -> {
                molecularTest.characteristics.homologousRecombination?.let { hr ->
                    if (hr.isDeficient == true) {
                        ActionabilityMatchResult.Success(listOf(hr))
                    } else {
                        ActionabilityMatchResult.Failure
                    }
                } ?: ActionabilityMatchResult.Failure
            }

            // note that VirusEvidence has a HPV_POSITIVE_TYPES that contains just TumorCharacteristicType.HPV_POSITIVE (serve data model)
            // can we reuse that here?
            TumorCharacteristicType.HPV_POSITIVE -> {
                val hits = molecularTest.drivers.viruses.filter { virus -> virus.type == VirusType.HUMAN_PAPILLOMA_VIRUS }
                if (hits.isNotEmpty()) {
                    ActionabilityMatchResult.Success(hits)
                } else {
                    ActionabilityMatchResult.Failure
                }
            }

            TumorCharacteristicType.EBV_POSITIVE -> {
                val hits = molecularTest.drivers.viruses.filter { virus -> virus.type == VirusType.EPSTEIN_BARR_VIRUS }
                if (hits.isNotEmpty()) {
                    ActionabilityMatchResult.Success(hits)
                } else {
                    ActionabilityMatchResult.Failure
                }
            }
        }
    }

    private fun matchHlas(molecularTest: MolecularTest, criterium: MolecularCriterium): ActionabilityMatchResult {
        if (criterium.hla().isEmpty()) {
            return ActionabilityMatchResult.Success()
        } else {
            // TODO don't notice previous support for hla matching, and SERVE has no criteria with
            // populated hla entries ... is this a problem? Should check in serve verifier? warn for now
            logger.warn("evidence contains HLA but matching supported")
            return ActionabilityMatchResult.Failure
        }
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