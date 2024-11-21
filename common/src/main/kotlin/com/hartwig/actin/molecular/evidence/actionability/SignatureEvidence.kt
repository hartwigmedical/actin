package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsFiltering.characteristicsFilter
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsFiltering.filterAndExpandTrials
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsFiltering.filterEfficacyEvidence
import com.hartwig.serve.datamodel.molecular.characteristic.TumorCharacteristicType

internal class SignatureEvidence private constructor(private val signatureCharacteristics: ActionableEvents) {

    fun findMicrosatelliteMatches(isMicrosatelliteUnstable: Boolean): ActionableEvents {
        return findMatches(isMicrosatelliteUnstable, TumorCharacteristicType.MICROSATELLITE_UNSTABLE)
    }

    fun findHomologousRepairMatches(isHomologousRepairDeficient: Boolean): ActionableEvents {
        return findMatches(isHomologousRepairDeficient, TumorCharacteristicType.HOMOLOGOUS_RECOMBINATION_DEFICIENT)
    }

    fun findTumorBurdenMatches(hasHighTumorMutationalBurden: Boolean): ActionableEvents {
        return findMatches(hasHighTumorMutationalBurden, TumorCharacteristicType.HIGH_TUMOR_MUTATIONAL_BURDEN)
    }

    fun findTumorLoadMatches(hasHighTumorMutationalLoad: Boolean): ActionableEvents {
        return findMatches(hasHighTumorMutationalLoad, TumorCharacteristicType.HIGH_TUMOR_MUTATIONAL_LOAD)
    }

    private fun findMatches(hasCharacteristic: Boolean, typeToFind: TumorCharacteristicType): ActionableEvents {
        return if (!hasCharacteristic) ActionableEvents() else {
            val evidences = signatureCharacteristics.evidences.filter {
                ActionableEventsFiltering.getCharacteristic(it)
                    .type() == typeToFind
            }
            val trials = signatureCharacteristics.trials.filter {
                ActionableEventsFiltering.getCharacteristic(it)
                    .type() == typeToFind
            }
            ActionableEvents(evidences, trials)
        }
    }

    companion object {
        private val signatureCharacteristicTypes = setOf(
            TumorCharacteristicType.MICROSATELLITE_UNSTABLE,
            TumorCharacteristicType.HOMOLOGOUS_RECOMBINATION_DEFICIENT,
            TumorCharacteristicType.HIGH_TUMOR_MUTATIONAL_BURDEN,
            TumorCharacteristicType.HIGH_TUMOR_MUTATIONAL_LOAD
        )

        fun create(actionableEvents: ActionableEvents): SignatureEvidence {
            val characteristicsEvidences = filterEfficacyEvidence(actionableEvents.evidences, characteristicsFilter()).filter {
                ActionableEventsFiltering.getCharacteristic(it)
                    .type() in signatureCharacteristicTypes
            }
            val characteristicsTrials = filterAndExpandTrials(actionableEvents.trials, characteristicsFilter()).filter {
                ActionableEventsFiltering.getCharacteristic(it)
                    .type() in signatureCharacteristicTypes
            }
            return SignatureEvidence(ActionableEvents(characteristicsEvidences, characteristicsTrials))
        }
    }
}
