package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.serve.datamodel.efficacy.EfficacyEvidence
import com.hartwig.serve.datamodel.molecular.characteristic.TumorCharacteristicType
import com.hartwig.serve.datamodel.trial.ActionableTrial

class SignatureEvidence(
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
                ActionableEventsExtraction.extractCharacteristics(it).any { it.type() == typeToFind }
            }
            ActionabilityMatch(evidenceMatches, trialMatches)
        }
    }

    companion object {
        private val SIGNATURE_CHARACTERISTICS_TYPES = setOf(
            TumorCharacteristicType.MICROSATELLITE_UNSTABLE,
            TumorCharacteristicType.HOMOLOGOUS_RECOMBINATION_DEFICIENT,
            TumorCharacteristicType.HIGH_TUMOR_MUTATIONAL_BURDEN,
            TumorCharacteristicType.HIGH_TUMOR_MUTATIONAL_LOAD
        )

        fun create(evidences: List<EfficacyEvidence>, trials: List<ActionableTrial>): SignatureEvidence {
            val characteristicsEvidences =
                ActionableEventsExtraction.extractCharacteristicEvidence(evidences, SIGNATURE_CHARACTERISTICS_TYPES)
            val characteristicsTrials = ActionableEventsExtraction.extractCharacteristicsTrials(trials, SIGNATURE_CHARACTERISTICS_TYPES)
            return SignatureEvidence(characteristicsEvidences, characteristicsTrials)
        }
    }
}
