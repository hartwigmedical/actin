package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsExtraction.characteristicsFilter
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsExtraction.filterEfficacyEvidence
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsExtraction.filterTrials
import com.hartwig.serve.datamodel.efficacy.EfficacyEvidence
import com.hartwig.serve.datamodel.molecular.characteristic.TumorCharacteristicType
import com.hartwig.serve.datamodel.trial.ActionableTrial

internal class SignatureEvidence private constructor(
    private val signatureEvidences: List<EfficacyEvidence>,
    private val signatureTrials: List<ActionableTrial>
) {

    fun findMicrosatelliteMatches(isMicrosatelliteUnstable: Boolean): ActionabilityMatch {
        return findMatches(isMicrosatelliteUnstable, TumorCharacteristicType.MICROSATELLITE_UNSTABLE)
    }

    fun findHomologousRepairMatches(isHomologousRepairDeficient: Boolean): ActionabilityMatch {
        return findMatches(isHomologousRepairDeficient, TumorCharacteristicType.HOMOLOGOUS_RECOMBINATION_DEFICIENT)
    }

    fun findTumorBurdenMatches(hasHighTumorMutationalBurden: Boolean): ActionabilityMatch {
        return findMatches(hasHighTumorMutationalBurden, TumorCharacteristicType.HIGH_TUMOR_MUTATIONAL_BURDEN)
    }

    fun findTumorLoadMatches(hasHighTumorMutationalLoad: Boolean): ActionabilityMatch {
        return findMatches(hasHighTumorMutationalLoad, TumorCharacteristicType.HIGH_TUMOR_MUTATIONAL_LOAD)
    }

    private fun findMatches(
        hasCharacteristic: Boolean,
        typeToFind: TumorCharacteristicType
    ): ActionabilityMatch {
        return if (!hasCharacteristic) ActionabilityMatch(emptyList(), emptyList()) else {
            val evidenceMatches = signatureEvidences.filter {
                ActionableEventsExtraction.extractCharacteristic(it).type() == typeToFind
            }
            val trialMatches = signatureTrials.filter {
                ActionableEventsExtraction.extractCharacteristic(it).type() == typeToFind
            }
            ActionabilityMatch(evidenceMatches, trialMatches)
        }
    }

    companion object {
        private val signatureCharacteristicTypes = setOf(
            TumorCharacteristicType.MICROSATELLITE_UNSTABLE,
            TumorCharacteristicType.HOMOLOGOUS_RECOMBINATION_DEFICIENT,
            TumorCharacteristicType.HIGH_TUMOR_MUTATIONAL_BURDEN,
            TumorCharacteristicType.HIGH_TUMOR_MUTATIONAL_LOAD
        )

        fun create(evidences: List<EfficacyEvidence>, trials: List<ActionableTrial>): SignatureEvidence {
            val characteristicsEvidences = filterEfficacyEvidence(evidences, characteristicsFilter()).filter {
                ActionableEventsExtraction.extractCharacteristic(it).type() in signatureCharacteristicTypes
            }
            val characteristicsTrials = filterTrials(trials, characteristicsFilter()).filter {
                ActionableEventsExtraction.extractCharacteristic(it).type() in signatureCharacteristicTypes
            }
            return SignatureEvidence(characteristicsEvidences, characteristicsTrials)
        }
    }
}
