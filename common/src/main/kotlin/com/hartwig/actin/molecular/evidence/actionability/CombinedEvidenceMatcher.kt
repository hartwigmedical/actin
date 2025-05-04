package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.driver.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.evidence.Actionable
import com.hartwig.actin.molecular.evidence.matching.FusionMatching
import com.hartwig.actin.molecular.evidence.matching.GeneMatching
import com.hartwig.actin.molecular.evidence.matching.HotspotMatching
import com.hartwig.actin.molecular.evidence.matching.MatchingCriteriaFunctions
import com.hartwig.actin.molecular.evidence.matching.RangeMatching
import com.hartwig.serve.datamodel.efficacy.EfficacyEvidence
import com.hartwig.serve.datamodel.molecular.characteristic.ActionableCharacteristic
import com.hartwig.serve.datamodel.molecular.characteristic.TumorCharacteristicType
import com.hartwig.serve.datamodel.molecular.fusion.ActionableFusion
import com.hartwig.serve.datamodel.molecular.gene.ActionableGene
import com.hartwig.serve.datamodel.molecular.hotspot.ActionableHotspot
import com.hartwig.serve.datamodel.molecular.range.RangeAnnotation

typealias ActionableToEvidences = Map<Actionable, Set<EfficacyEvidence>>

sealed class MatchResult {
    object Failure : MatchResult()

    // events is not the best name? eventsOrCharacterstics? Actionables?
    data class Success(val events: List<Actionable> = emptyList()) : MatchResult()

    companion object {
        fun combine(results: List<MatchResult>): MatchResult {
            return if (results.any { it is Failure }) {
                Failure
            } else {
                Success(results.filterIsInstance<Success>().flatMap { it.events })
            }
        }

        // TODO could this help instead of the above?
        fun lazyCombine(results: Sequence<MatchResult>): MatchResult {
            results.forEach { result ->
                if (result is Failure) {
                    return Failure
                }
            }
            return Success(results.filterIsInstance<Success>().flatMap { it.events }.toList())
        }

        fun successWhenNotEmpty(matches: List<Actionable>): MatchResult {
            return if (matches.isEmpty()) {
                Failure
            } else {
                Success(matches)
            }
        }
    }
}

class CombinedEvidenceMatcher(private val evidences: List<EfficacyEvidence>) {


    fun match(molecularTest: MolecularTest): ActionableToEvidences {
        return evidences.asSequence()
            .mapNotNull { evidence ->
                when (val result = matchEvidence(molecularTest, evidence)) {
                    is MatchResult.Success -> result.events.associateWith { setOf(evidence) }
                    is MatchResult.Failure -> null
                }
            }
            .flatMap { it.entries }
            .groupBy({ it.key }, { it.value })
            .mapValues { it.value.flatten().toSet() }
    }

    private fun matchEvidence(molecularTest: MolecularTest, efficacyEvidence: EfficacyEvidence): MatchResult {
        val matchFunctions = listOf(
            ::matchHotspots,
            ::matchCodons,
            ::matchExons,
            ::matchGenes,
            ::matchFusions,
            ::matchCharacteristics,
            ::matchHlas
        )

        val results = matchFunctions.asSequence()
            .map { matchFunction -> matchFunction(molecularTest, efficacyEvidence) }

        return MatchResult.lazyCombine(results)
    }

    private fun matchHotspots(molecularTest: MolecularTest, efficacyEvidence: EfficacyEvidence): MatchResult {
        val variantMatches = efficacyEvidence.molecularCriterium().hotspots()
            .map { hotspot -> matchHotspot(molecularTest, hotspot) }

        return MatchResult.combine(variantMatches)
    }

    private fun matchHotspot(molecularTest: MolecularTest, hotspot: ActionableHotspot): MatchResult {
        val matches = molecularTest.drivers.variants
            .filter { variant ->
                // TODO important, should we match on reportable/driverlikelihoood or push these to the later filtering?
                variant.isReportable && variant.driverLikelihood == DriverLikelihood.HIGH
                        && HotspotMatching.isMatch(hotspot, MatchingCriteriaFunctions.createVariantCriteria(variant))
            }

        return MatchResult.successWhenNotEmpty(matches)
    }

    fun matchCodons(molecularTest: MolecularTest, efficacyEvidence: EfficacyEvidence): MatchResult {
        val codonMatches = efficacyEvidence.molecularCriterium().codons()
            .map { codon -> matchCodon(molecularTest, codon) }

        return MatchResult.combine(codonMatches)
    }

    fun matchCodon(molecularTest: MolecularTest, codon: RangeAnnotation): MatchResult {
        val matches = molecularTest.drivers.variants
            .filter { variant ->
                RangeMatching.isMatch(codon, MatchingCriteriaFunctions.createVariantCriteria(variant))
            }

        return MatchResult.successWhenNotEmpty(matches)
    }

    fun matchExons(molecularTest: MolecularTest, efficacyEvidence: EfficacyEvidence): MatchResult {
        val exonMatches = efficacyEvidence.molecularCriterium().exons()
            .map { exon -> matchExon(molecularTest, exon) }

        return MatchResult.combine(exonMatches)
    }

    fun matchExon(molecularTest: MolecularTest, exon: RangeAnnotation): MatchResult {
        val matches = molecularTest.drivers.variants
            .filter { variant -> RangeMatching.isMatch(exon, MatchingCriteriaFunctions.createVariantCriteria(variant)) }

        return MatchResult.successWhenNotEmpty(matches)
    }

    fun matchGenes(molecularTest: MolecularTest, efficacyEvidence: EfficacyEvidence): MatchResult {
        val geneMatches = efficacyEvidence.molecularCriterium().genes()
            .map { gene -> matchGene(molecularTest, gene) }

        return MatchResult.combine(geneMatches)
    }

    fun matchGene(molecularTest: MolecularTest, gene: ActionableGene): MatchResult {
        val matches = molecularTest.drivers.variants
            .filter { variant ->
                GeneMatching.isMatch(gene, MatchingCriteriaFunctions.createVariantCriteria(variant))
            }
        return MatchResult.successWhenNotEmpty(matches)
    }

    fun matchFusions(molecularTest: MolecularTest, efficacyEvidence: EfficacyEvidence): MatchResult {
        val fusionMatches = efficacyEvidence.molecularCriterium().fusions()
            .map { fusion -> matchFusion(molecularTest, fusion) }

        return MatchResult.combine(fusionMatches)
    }

    fun matchFusion(molecularTest: MolecularTest, fusion: ActionableFusion): MatchResult {
        val matches = molecularTest.drivers.fusions
            .filter { driverFusion ->
                val fusionMatchCriteria = MatchingCriteriaFunctions.createFusionCriteria(driverFusion)
                driverFusion.isReportable && FusionMatching.isGeneMatch(fusion, fusionMatchCriteria)
                        && FusionMatching.isExonMatch(fusion, fusionMatchCriteria)
            }

        return MatchResult.successWhenNotEmpty(matches)
    }

    fun matchCharacteristics(molecularTest: MolecularTest, efficacyEvidence: EfficacyEvidence): MatchResult {
        val characteristicMatches = efficacyEvidence.molecularCriterium().characteristics()
            .map { characteristic -> matchCharacteristic(molecularTest, characteristic) }

        return MatchResult.combine(characteristicMatches)
    }

    fun matchCharacteristic(molecularTest: MolecularTest, characteristic: ActionableCharacteristic): MatchResult {

        return when (characteristic.type()) {
            TumorCharacteristicType.MICROSATELLITE_STABLE -> {
                molecularTest.characteristics.microsatelliteStability?.let { msi ->
                    if (msi.isUnstable) {
                        MatchResult.Failure
                    } else {
                        MatchResult.Success(listOf(msi))
                    }
                } ?: MatchResult.Success()
            }

            TumorCharacteristicType.MICROSATELLITE_UNSTABLE -> {
                molecularTest.characteristics.microsatelliteStability?.let { msi ->
                    if (msi.isUnstable) {
                        MatchResult.Success(listOf(msi))
                    } else {
                        MatchResult.Failure
                    }
                } ?: MatchResult.Success()
            }

            TumorCharacteristicType.HIGH_TUMOR_MUTATIONAL_LOAD -> {
                molecularTest.characteristics.tumorMutationalLoad?.let { tml ->
                    if (tml.isHigh) {
                        MatchResult.Success(listOf(tml))
                    } else {
                        MatchResult.Failure
                    }
                } ?: MatchResult.Success()
            }

            TumorCharacteristicType.LOW_TUMOR_MUTATIONAL_LOAD -> {
                molecularTest.characteristics.tumorMutationalLoad?.let { tml ->
                    if (tml.isHigh) {
                        MatchResult.Failure
                    } else {
                        MatchResult.Success(listOf(tml))
                    }
                } ?: MatchResult.Success()
            }

            TumorCharacteristicType.HIGH_TUMOR_MUTATIONAL_BURDEN -> {
                molecularTest.characteristics.tumorMutationalLoad?.let { tml ->
                    if (tml.isHigh) {
                        MatchResult.Success(listOf(tml))

                    } else {
                        MatchResult.Failure
                    }
                } ?: MatchResult.Success()
            }

            TumorCharacteristicType.LOW_TUMOR_MUTATIONAL_BURDEN -> {
                molecularTest.characteristics.tumorMutationalLoad?.let { tml ->
                    if (tml.isHigh) {
                        MatchResult.Failure
                    } else {
                        MatchResult.Success(listOf(tml))
                    }
                } ?: MatchResult.Success()
            }

            TumorCharacteristicType.HOMOLOGOUS_RECOMBINATION_DEFICIENT -> {
                molecularTest.characteristics.homologousRecombination?.let { hr ->
                    if (hr.isDeficient == true) {
                        MatchResult.Success(listOf(hr))
                    } else {
                        MatchResult.Failure
                    }
                } ?: MatchResult.Success()
            }

            TumorCharacteristicType.HPV_POSITIVE -> TODO()

            TumorCharacteristicType.EBV_POSITIVE -> TODO()

        }
    }

    fun matchHlas(molecularTest: MolecularTest, efficacyEvidence: EfficacyEvidence): MatchResult {
        if (efficacyEvidence.molecularCriterium().hla().isEmpty()) {
            return MatchResult.Success()
        }
        // MolecularRecord (from Orange) has MolecularImmunology, that should be matchable against evidence hla?
        // Not in MolecularTest though (and not in PanelRecord)
        // Could maybe warn if the evidence has hla(), and either always fail or match as empty
        TODO("Implement HLA matching")
    }
}