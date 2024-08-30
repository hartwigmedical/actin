package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.datamodel.molecular.orange.driver.Virus
import com.hartwig.actin.datamodel.molecular.orange.driver.VirusType
import com.hartwig.actin.molecular.evidence.matching.EvidenceMatcher
import com.hartwig.serve.datamodel.ActionableEvent
import com.hartwig.serve.datamodel.ActionableEvents
import com.hartwig.serve.datamodel.characteristic.TumorCharacteristicType

internal class VirusEvidence private constructor(
    private val hpvCharacteristics: List<ActionableEvent>,
    private val ebvCharacteristics: List<ActionableEvent>
) : EvidenceMatcher<Virus> {

    override fun findMatches(event: Virus): List<ActionableEvent> {
        return if (!event.isReportable) {
            emptyList()
        } else when (event.type) {
            VirusType.HUMAN_PAPILLOMA_VIRUS -> {
                hpvCharacteristics
            }

            VirusType.EPSTEIN_BARR_VIRUS -> {
                ebvCharacteristics
            }

            else -> {
                emptyList()
            }
        }
    }

    companion object {
        fun create(actionableEvents: ActionableEvents): VirusEvidence {
            val (hpvCharacteristics, ebvCharacteristics) = actionableEvents.characteristics()
                .fold(Pair(emptyList<ActionableEvent>(), emptyList<ActionableEvent>())) { acc, actionableCharacteristic ->
                    when (actionableCharacteristic.type()) {
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
            return VirusEvidence(hpvCharacteristics, ebvCharacteristics)
        }
    }
}
