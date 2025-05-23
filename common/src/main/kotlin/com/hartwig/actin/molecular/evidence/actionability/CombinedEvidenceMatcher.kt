package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.driver.VirusType
import com.hartwig.actin.datamodel.molecular.evidence.Actionable
import com.hartwig.actin.molecular.evidence.actionability.ActionabilityMatchResult.Failure
import com.hartwig.actin.molecular.evidence.actionability.ActionabilityMatchResult.Success
import com.hartwig.actin.molecular.evidence.matching.GeneMatching
import com.hartwig.actin.molecular.evidence.matching.HotspotMatching
import com.hartwig.actin.molecular.evidence.matching.RangeMatching
import com.hartwig.serve.datamodel.efficacy.EfficacyEvidence
import com.hartwig.serve.datamodel.molecular.characteristic.ActionableCharacteristic
import com.hartwig.serve.datamodel.molecular.characteristic.TumorCharacteristicType
import com.hartwig.serve.datamodel.molecular.fusion.ActionableFusion
import com.hartwig.serve.datamodel.molecular.gene.ActionableGene
import com.hartwig.serve.datamodel.molecular.hotspot.ActionableHotspot
import com.hartwig.serve.datamodel.molecular.range.RangeAnnotation
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

typealias EvidencesForActionable = Map<Actionable, Set<EfficacyEvidence>>

class CombinedEvidenceMatcher(private val evidences: List<EfficacyEvidence>) {
    val logger: Logger = LogManager.getLogger(CombinedEvidenceMatcher::class.java)

    fun match(molecularTest: MolecularTest): EvidencesForActionable {
        return evidences.asSequence()
            .flatMap { evidence ->
                when (val result = matchEvidence(molecularTest, evidence)) {
                    is Success -> result.actionables.map { actionable -> actionable to evidence }
                    is Failure -> emptyList()
                }
            }
            .groupBy(keySelector = { it.first }, valueTransform = { it.second })
            .mapValues { it.value.toSet() }
    }

    private fun matchEvidence(molecularTest: MolecularTest, efficacyEvidence: EfficacyEvidence): ActionabilityMatchResult =
        ActionabilityMatchResult.combine(
            sequenceOf(
                ::matchHotspots,
                ::matchCodons,
                ::matchExons,
                ::matchGenes,
                ::matchFusions,
                ::matchCharacteristics,
                ::matchHlas
            ).map { matchFunc -> matchFunc(molecularTest, efficacyEvidence) }
        )

    private fun matchHotspots(molecularTest: MolecularTest, efficacyEvidence: EfficacyEvidence): ActionabilityMatchResult {
        val variantMatches = efficacyEvidence.molecularCriterium().hotspots()
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

    private fun matchCodons(molecularTest: MolecularTest, efficacyEvidence: EfficacyEvidence): ActionabilityMatchResult {
        val codonMatches = efficacyEvidence.molecularCriterium().codons()
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

    private fun matchExons(molecularTest: MolecularTest, efficacyEvidence: EfficacyEvidence): ActionabilityMatchResult {
        val exonMatches = efficacyEvidence.molecularCriterium().exons()
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

    private fun matchGenes(molecularTest: MolecularTest, efficacyEvidence: EfficacyEvidence): ActionabilityMatchResult {
        val geneMatches = efficacyEvidence.molecularCriterium().genes()
            .map { gene -> matchGene(molecularTest, gene) }

        return ActionabilityMatchResult.combine(geneMatches)
    }

    private fun matchGene(molecularTest: MolecularTest, gene: ActionableGene): ActionabilityMatchResult {
        val variantMatches = molecularTest.drivers.variants
            .filter { variant ->
                VariantEvidence.isVariantEligible(variant) && GeneMatching.isMatch(gene, variant)
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

        return successWhenNotEmpty(variantMatches + promiscuousFusionMatches + disruptionMatches +
                homozygousDisruptionMatches + copyNumberAmplificationMatches + copyNumberDeletionmatches)
    }

    private fun matchFusions(molecularTest: MolecularTest, efficacyEvidence: EfficacyEvidence): ActionabilityMatchResult {
        val fusionMatches = efficacyEvidence.molecularCriterium().fusions()
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


    private fun matchCharacteristics(molecularTest: MolecularTest, efficacyEvidence: EfficacyEvidence): ActionabilityMatchResult {
        val characteristicMatches = efficacyEvidence.molecularCriterium().characteristics()
            .map { characteristic -> matchCharacteristic(molecularTest, characteristic) }

        return ActionabilityMatchResult.combine(characteristicMatches)
    }

    private fun matchCharacteristic(molecularTest: MolecularTest, characteristic: ActionableCharacteristic): ActionabilityMatchResult {

        return when (characteristic.type()) {
            TumorCharacteristicType.MICROSATELLITE_STABLE -> {
                molecularTest.characteristics.microsatelliteStability?.let { msi ->
                    if (msi.isUnstable) {
                        Failure
                    } else {
                        Success(listOf(msi))
                    }
                } ?: Failure
            }

            TumorCharacteristicType.MICROSATELLITE_UNSTABLE -> {
                molecularTest.characteristics.microsatelliteStability?.let { msi ->
                    if (msi.isUnstable) {
                        Success(listOf(msi))
                    } else {
                        Failure
                    }
                } ?: Failure
            }

            TumorCharacteristicType.HIGH_TUMOR_MUTATIONAL_LOAD -> {
                molecularTest.characteristics.tumorMutationalLoad?.let { tml ->
                    if (tml.isHigh) {
                        Success(listOf(tml))
                    } else {
                        Failure
                    }
                } ?: Failure
            }

            TumorCharacteristicType.LOW_TUMOR_MUTATIONAL_LOAD -> {
                molecularTest.characteristics.tumorMutationalLoad?.let { tml ->
                    if (tml.isHigh) {
                        Failure
                    } else {
                        Success(listOf(tml))
                    }
                } ?: Failure
            }

            TumorCharacteristicType.HIGH_TUMOR_MUTATIONAL_BURDEN -> {
                molecularTest.characteristics.tumorMutationalLoad?.let { tml ->
                    if (tml.isHigh) {
                        Success(listOf(tml))

                    } else {
                        Failure
                    }
                } ?: Failure
            }

            TumorCharacteristicType.LOW_TUMOR_MUTATIONAL_BURDEN -> {
                molecularTest.characteristics.tumorMutationalLoad?.let { tml ->
                    if (tml.isHigh) {
                        Failure
                    } else {
                        Success(listOf(tml))
                    }
                } ?: Failure
            }

            TumorCharacteristicType.HOMOLOGOUS_RECOMBINATION_DEFICIENT -> {
                molecularTest.characteristics.homologousRecombination?.let { hr ->
                    if (hr.isDeficient == true) {
                        Success(listOf(hr))
                    } else {
                        Failure
                    }
                } ?: Failure
            }

            // note that VirusEvidence has a HPV_POSITIVE_TYPES that contains just TumorCharacteristicType.HPV_POSITIVE (serve data model)
            // can we reuse that here?
            TumorCharacteristicType.HPV_POSITIVE -> {
                val hits = molecularTest.drivers.viruses.filter { virus -> virus.type == VirusType.HUMAN_PAPILLOMA_VIRUS }
                if (hits.isNotEmpty()) {
                    Success(hits)
                } else {
                    Failure
                }
            }

            TumorCharacteristicType.EBV_POSITIVE -> {
                val hits = molecularTest.drivers.viruses.filter { virus -> virus.type == VirusType.EPSTEIN_BARR_VIRUS }
                if (hits.isNotEmpty()) {
                    Success(hits)
                } else {
                    Failure
                }
            }
        }
    }

    private fun matchHlas(molecularTest: MolecularTest, efficacyEvidence: EfficacyEvidence): ActionabilityMatchResult {
        if (efficacyEvidence.molecularCriterium().hla().isEmpty()) {
            return Success()
        } else {
            // TODO don't notice previous support for hla matching, and SERVE has no criteria with
            // populated hla entries ... is this a problem? Should check in serve verifier? warn for now
            logger.warn("evidence contains HLA but matching supported")
            return Failure
        }
    }

    companion object {

        fun successWhenNotEmpty(matches: List<Actionable>): ActionabilityMatchResult {
            return if (matches.isEmpty()) {
                Failure
            } else {
                Success(matches)
            }
        }
    }
}