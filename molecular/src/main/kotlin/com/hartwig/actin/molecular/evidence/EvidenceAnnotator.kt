package com.hartwig.actin.molecular.evidence

import com.hartwig.actin.datamodel.molecular.MolecularRecord
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.PanelRecord
import com.hartwig.actin.datamodel.molecular.characteristics.HomologousRecombination
import com.hartwig.actin.datamodel.molecular.characteristics.MicrosatelliteStability
import com.hartwig.actin.datamodel.molecular.characteristics.MolecularCharacteristics
import com.hartwig.actin.datamodel.molecular.characteristics.TumorMutationalBurden
import com.hartwig.actin.datamodel.molecular.characteristics.TumorMutationalLoad
import com.hartwig.actin.datamodel.molecular.driver.CopyNumber
import com.hartwig.actin.datamodel.molecular.driver.Disruption
import com.hartwig.actin.datamodel.molecular.driver.Drivers
import com.hartwig.actin.datamodel.molecular.driver.Fusion
import com.hartwig.actin.datamodel.molecular.driver.HomozygousDisruption
import com.hartwig.actin.datamodel.molecular.driver.Variant
import com.hartwig.actin.datamodel.molecular.driver.Virus
import com.hartwig.actin.molecular.MolecularAnnotator
import com.hartwig.actin.molecular.evidence.actionability.ActionabilityMatch
import com.hartwig.actin.molecular.evidence.actionability.ClinicalEvidenceFactory
import com.hartwig.actin.molecular.evidence.actionability.CombinedEvidenceMatcher
import com.hartwig.actin.molecular.evidence.actionability.EvidencesForActionable

class EvidenceAnnotator(
    private val clinicalEvidenceFactory: ClinicalEvidenceFactory,
    val combinedEvidenceMatcher: CombinedEvidenceMatcher,
) : MolecularAnnotator<MolecularTest, MolecularTest> {
    override fun annotate(input: MolecularTest): MolecularTest {

        val evidencesForActionable = combinedEvidenceMatcher.match(input)

        when (input) {
            is MolecularRecord -> {
                return input.copy(
                    drivers = annotateDriversWithEvidence(input.drivers, evidencesForActionable),
                    characteristics = annotateCharacteristicsWithEvidence(input.characteristics, evidencesForActionable)
                )
            }

            is PanelRecord -> {
                return input.copy(
                    drivers = annotateDriversWithEvidence(input.drivers, evidencesForActionable),
                    characteristics = annotateCharacteristicsWithEvidence(input.characteristics, evidencesForActionable)
                )
            }

            else -> {
                throw IllegalArgumentException("Unsupported MolecularTest type: ${input::class.java}")
            }
        }
    }

    private fun annotateDriversWithEvidence(
        drivers: Drivers,
        evidencesForActionable: EvidencesForActionable
    ): Drivers {
        return drivers.copy(
            variants = drivers.variants.map { annotateVariantsWithEvidence(it, evidencesForActionable) },
            copyNumbers = drivers.copyNumbers.map { annotateCopyWithEvidence(it, evidencesForActionable) },
            homozygousDisruptions = drivers.homozygousDisruptions.map { annotateHomozygousDisruptionWithEvidence(it, evidencesForActionable) },
            disruptions = drivers.disruptions.map { annotateDisruptionWithEvidence(it, evidencesForActionable) },
            fusions = drivers.fusions.map { annotateFusionsWithEvidence(it, evidencesForActionable) },
            viruses = drivers.viruses.map { annotateVirusWithEvidence(it, evidencesForActionable) }
        )
    }

    private fun annotateCharacteristicsWithEvidence(
        characteristics: MolecularCharacteristics,
        evidencesForActionable: EvidencesForActionable
    ): MolecularCharacteristics {
        return characteristics.copy(
            microsatelliteStability = characteristics.microsatelliteStability?.let { msi -> annotateMsiWithEvidence(msi, evidencesForActionable) },
            homologousRecombination = characteristics.homologousRecombination?.let { hr -> annotateHrWithEvidence(hr, evidencesForActionable) },
            tumorMutationalBurden = characteristics.tumorMutationalBurden?.let { tmb -> annotateTmbWithEvidence(tmb, evidencesForActionable) },
            tumorMutationalLoad = characteristics.tumorMutationalLoad?.let { tml -> annotateTmlWithEvidence(tml, evidencesForActionable) }
        )
    }

    private fun annotateVariantsWithEvidence(variant: Variant, evidencesForActionable: EvidencesForActionable): Variant {
        val evidences = evidencesForActionable[variant] ?: throw IllegalStateException("No evidences found for variant: $variant")
        val actionabilityMatch = ActionabilityMatch(evidenceMatches = evidences.toList(), matchingCriteriaPerTrialMatch = emptyMap())
        val clinicalEvidence = clinicalEvidenceFactory.create(actionabilityMatch).copy(eligibleTrials = variant.evidence.eligibleTrials)
        return variant.copy(evidence = clinicalEvidence)
    }

    private fun annotateCopyWithEvidence(copyNumber: CopyNumber, evidencesForActionable: EvidencesForActionable): CopyNumber {
        val evidences = evidencesForActionable[copyNumber] ?: throw IllegalStateException("No evidences found for copy number: $copyNumber")
        val actionabilityMatch = ActionabilityMatch(evidenceMatches = evidences.toList(), matchingCriteriaPerTrialMatch = emptyMap())
        val clinicalEvidence = clinicalEvidenceFactory.create(actionabilityMatch).copy(eligibleTrials = copyNumber.evidence.eligibleTrials)
        return copyNumber.copy(evidence = clinicalEvidence)
    }

    private fun annotateHomozygousDisruptionWithEvidence(
        homozygousDisruption: HomozygousDisruption,
        evidencesForActionable: EvidencesForActionable
    ): HomozygousDisruption {
        val evidences = evidencesForActionable[homozygousDisruption]
            ?: throw IllegalStateException("No evidences found for homozygous disruption: $homozygousDisruption")
        val actionabilityMatch = ActionabilityMatch(evidenceMatches = evidences.toList(), matchingCriteriaPerTrialMatch = emptyMap())
        val clinicalEvidence = clinicalEvidenceFactory.create(actionabilityMatch).copy(eligibleTrials = homozygousDisruption.evidence.eligibleTrials)
        return homozygousDisruption.copy(evidence = clinicalEvidence)
    }

    private fun annotateDisruptionWithEvidence(
        disruption: Disruption,
        evidencesForActionable: EvidencesForActionable
    ): Disruption {
        val evidences = evidencesForActionable[disruption] ?: throw IllegalStateException("No evidences found for disruption: $disruption")
        val actionabilityMatch = ActionabilityMatch(evidenceMatches = evidences.toList(), matchingCriteriaPerTrialMatch = emptyMap())
        val clinicalEvidence = clinicalEvidenceFactory.create(actionabilityMatch).copy(eligibleTrials = disruption.evidence.eligibleTrials)
        return disruption.copy(evidence = clinicalEvidence)
    }

    private fun annotateFusionsWithEvidence(fusion: Fusion, evidencesForActionable: EvidencesForActionable): Fusion {
        val evidences = evidencesForActionable[fusion] ?: throw IllegalStateException("No evidences found for fusion: $fusion")
        val actionabilityMatch = ActionabilityMatch(evidenceMatches = evidences.toList(), matchingCriteriaPerTrialMatch = emptyMap())
        val clinicalEvidence = clinicalEvidenceFactory.create(actionabilityMatch).copy(eligibleTrials = fusion.evidence.eligibleTrials)
        return fusion.copy(evidence = clinicalEvidence)
    }

    private fun annotateVirusWithEvidence(virus: Virus, evidencesForActionable: EvidencesForActionable): Virus {
        val evidences = evidencesForActionable[virus] ?: throw IllegalStateException("No evidences found for virus: $virus")
        val actionabilityMatch = ActionabilityMatch(evidenceMatches = evidences.toList(), matchingCriteriaPerTrialMatch = emptyMap())
        val clinicalEvidence = clinicalEvidenceFactory.create(actionabilityMatch).copy(eligibleTrials = virus.evidence.eligibleTrials)
        return virus.copy(evidence = clinicalEvidence)
    }

    private fun annotateMsiWithEvidence(
        msi: MicrosatelliteStability,
        evidencesForActionable: EvidencesForActionable
    ): MicrosatelliteStability {
        val evidences = evidencesForActionable[msi] ?: throw IllegalStateException("No evidences found for microsatellite stability: $msi")
        val actionabilityMatch = ActionabilityMatch(evidenceMatches = evidences.toList(), matchingCriteriaPerTrialMatch = emptyMap())
        val clinicalEvidence = clinicalEvidenceFactory.create(actionabilityMatch).copy(eligibleTrials = msi.evidence.eligibleTrials)
        return msi.copy(evidence = clinicalEvidence)
    }

    private fun annotateHrWithEvidence(
        hr: HomologousRecombination,
        evidencesForActionable: EvidencesForActionable
    ): HomologousRecombination {
        val evidences = evidencesForActionable[hr] ?: throw IllegalStateException("No evidences found for homologous recombination: $hr")
        val actionabilityMatch = ActionabilityMatch(evidenceMatches = evidences.toList(), matchingCriteriaPerTrialMatch = emptyMap())
        val clinicalEvidence = clinicalEvidenceFactory.create(actionabilityMatch).copy(eligibleTrials = hr.evidence.eligibleTrials)
        return hr.copy(evidence = clinicalEvidence)
    }

    private fun annotateTmbWithEvidence(
        tmb: TumorMutationalBurden,
        evidencesForActionable: EvidencesForActionable
    ): TumorMutationalBurden {
        val evidences = evidencesForActionable[tmb] ?: throw IllegalStateException("No evidences found for tumor mutational burden: $tmb")
        val actionabilityMatch = ActionabilityMatch(evidenceMatches = evidences.toList(), matchingCriteriaPerTrialMatch = emptyMap())
        val clinicalEvidence = clinicalEvidenceFactory.create(actionabilityMatch).copy(eligibleTrials = tmb.evidence.eligibleTrials)
        return tmb.copy(evidence = clinicalEvidence)
    }

    private fun annotateTmlWithEvidence(
        tml: TumorMutationalLoad,
        evidencesForActionable: EvidencesForActionable
    ): TumorMutationalLoad {
        val evidences = evidencesForActionable[tml] ?: throw IllegalStateException("No evidences found for tumor mutational load: $tml")
        val actionabilityMatch = ActionabilityMatch(evidenceMatches = evidences.toList(), matchingCriteriaPerTrialMatch = emptyMap())
        val clinicalEvidence = clinicalEvidenceFactory.create(actionabilityMatch).copy(eligibleTrials = tml.evidence.eligibleTrials)
        return tml.copy(evidence = clinicalEvidence)
    }


// TODO (maybe): if we want super generalized code, can use a helper like the following:
//
//    private inline fun <T : Actionable> annotateWithEvidence(
//        actionable: T,
//        evidencesForActionable: EvidencesForActionable,
//        crossinline copyWithEvidence: (T, ClinicalEvidence) -> T
//    ): T {
//        val evidences = evidencesForActionable[actionable]
//            ?: throw IllegalStateException("No evidences found for actionable: $actionable")
//        val actionabilityMatch = ActionabilityMatch(evidenceMatches = evidences.toList(), matchingCriteriaPerTrialMatch = emptyMap())
//        val clinicalEvidence = clinicalEvidenceFactory.create(actionabilityMatch).copy(eligibleTrials = actionable.evidence.eligibleTrials)
//        return copyWithEvidence(actionable, clinicalEvidence)
//    }
//
// And here's a usage example:
//
//    private fun annotateHrWithEvidence(
//        hr: HomologousRecombination,
//        evidencesForActionable: EvidencesForActionable
//    ): HomologousRecombination = annotateWithEvidence(hr, evidencesForActionable) { obj, evidence -> obj.copy(evidence = evidence) }
//
// But it may be overly complex for the value its providing
}