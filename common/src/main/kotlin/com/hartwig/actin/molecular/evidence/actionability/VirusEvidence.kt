package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.datamodel.molecular.orange.driver.Virus
import com.hartwig.actin.datamodel.molecular.orange.driver.VirusType
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsExtraction.characteristicsFilter
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsExtraction.extractCharacteristic
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsExtraction.filterEfficacyEvidence
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsExtraction.filterTrials
import com.hartwig.serve.datamodel.efficacy.EfficacyEvidence
import com.hartwig.serve.datamodel.molecular.characteristic.ActionableCharacteristic
import com.hartwig.serve.datamodel.molecular.characteristic.TumorCharacteristicType
import com.hartwig.serve.datamodel.trial.ActionableTrial

internal class VirusEvidence private constructor(
    private val hpvEvidences: List<EfficacyEvidence>,
    private val hpvTrials: List<ActionableTrial>,
    private val ebvEvidences: List<EfficacyEvidence>,
    private val ebvTrials: List<ActionableTrial>
) : ActionabilityMatcher<Virus> {

    override fun findMatches(event: Virus): ActionabilityMatch {
        return if (!event.isReportable) {
            ActionabilityMatch(evidenceMatches = emptyList(), trialMatches = emptyList())
        } else when (event.type) {
            VirusType.HUMAN_PAPILLOMA_VIRUS -> {
                ActionabilityMatch(evidenceMatches = hpvEvidences, trialMatches = hpvTrials)
            }

            VirusType.EPSTEIN_BARR_VIRUS -> {
                ActionabilityMatch(evidenceMatches = ebvEvidences, trialMatches = ebvTrials)
            }

            else -> {
                ActionabilityMatch(evidenceMatches = emptyList(), trialMatches = emptyList())
            }
        }
    }

    companion object {
        fun create(evidences: List<EfficacyEvidence>, trials: List<ActionableTrial>): VirusEvidence {
            val applicableEvidences = filterEfficacyEvidence(evidences, characteristicsFilter())
            val applicableTrials = filterTrials(trials, characteristicsFilter())
            val (hpvCharacteristicsEvidence, ebvCharacteristicsEvidence) = extractHPVAndEBV(applicableEvidences, ::extractCharacteristic)
            val (hpvCharacteristicsTrials, ebvCharacteristicsTrials) = extractHPVAndEBV(applicableTrials, ::extractCharacteristic)

            return VirusEvidence(
                hpvCharacteristicsEvidence,
                hpvCharacteristicsTrials,
                ebvCharacteristicsEvidence,
                ebvCharacteristicsTrials
            )
        }

        private fun <T> extractHPVAndEBV(items: List<T>, getCharacteristic: (T) -> ActionableCharacteristic): Pair<List<T>, List<T>> {
            return items.fold(Pair(emptyList(), emptyList())) { acc, actionableCharacteristic ->
                when (getCharacteristic(actionableCharacteristic).type()) {
                    TumorCharacteristicType.HPV_POSITIVE -> {
                        Pair(acc.first + actionableCharacteristic, acc.second)
                    }

                    TumorCharacteristicType.EBV_POSITIVE -> {
                        Pair(acc.first, acc.second + actionableCharacteristic)
                    }

                    else -> {
                        acc
                    }
                }
            }
        }
    }
}
