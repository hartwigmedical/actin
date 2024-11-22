package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.datamodel.molecular.orange.driver.Virus
import com.hartwig.actin.datamodel.molecular.orange.driver.VirusType
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsExtraction.characteristicsFilter
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsExtraction.filterAndExpandTrials
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsExtraction.filterEfficacyEvidence
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsExtraction.extractCharacteristic
import com.hartwig.actin.molecular.evidence.matching.EvidenceMatcher
import com.hartwig.serve.datamodel.molecular.characteristic.ActionableCharacteristic
import com.hartwig.serve.datamodel.molecular.characteristic.TumorCharacteristicType

internal class VirusEvidence private constructor(
    private val hpvCharacteristics: ActionableEvents,
    private val ebvCharacteristics: ActionableEvents
) : EvidenceMatcher<Virus> {

    override fun findMatches(event: Virus): ActionableEvents {
        return if (!event.isReportable) {
            ActionableEvents()
        } else when (event.type) {
            VirusType.HUMAN_PAPILLOMA_VIRUS -> {
                hpvCharacteristics
            }

            VirusType.EPSTEIN_BARR_VIRUS -> {
                ebvCharacteristics
            }

            else -> {
                ActionableEvents()
            }
        }
    }

    companion object {
        fun create(actionableEvents: ActionableEvents): VirusEvidence {
            val evidences = filterEfficacyEvidence(actionableEvents.evidences, characteristicsFilter())
            val trials = filterAndExpandTrials(actionableEvents.trials, characteristicsFilter())
            val (hpvCharacteristicsEvidence, ebvCharacteristicsEvidence) = extractHPVAndEBV(evidences, ::extractCharacteristic)
            val (hpvCharacteristicsTrials, ebvCharacteristicsTrials) = extractHPVAndEBV(trials, ::extractCharacteristic)
            return VirusEvidence(
                ActionableEvents(hpvCharacteristicsEvidence, hpvCharacteristicsTrials),
                ActionableEvents(ebvCharacteristicsEvidence, ebvCharacteristicsTrials)
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
