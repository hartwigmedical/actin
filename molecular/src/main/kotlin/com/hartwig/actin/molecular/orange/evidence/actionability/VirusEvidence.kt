package com.hartwig.actin.molecular.orange.evidence.actionability

import com.hartwig.hmftools.datamodel.virus.VirusInterpretation
import com.hartwig.hmftools.datamodel.virus.VirusInterpreterEntry
import com.hartwig.serve.datamodel.ActionableEvent
import com.hartwig.serve.datamodel.ActionableEvents
import com.hartwig.serve.datamodel.characteristic.TumorCharacteristicType

internal class VirusEvidence private constructor(
    private val hpvCharacteristics: List<ActionableEvent>,
    private val ebvCharacteristics: List<ActionableEvent>
) : EvidenceMatcher<VirusInterpreterEntry> {

    override fun findMatches(event: VirusInterpreterEntry): List<ActionableEvent> {
        return if (!event.reported()) {
            emptyList()
        } else when (event.interpretation()) {
            VirusInterpretation.HPV -> {
                hpvCharacteristics
            }
            VirusInterpretation.EBV -> {
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
