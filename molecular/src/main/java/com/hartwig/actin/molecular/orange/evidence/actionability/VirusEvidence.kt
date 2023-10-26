package com.hartwig.actin.molecular.orange.evidence.actionability

import com.google.common.collect.Lists
import com.hartwig.hmftools.datamodel.virus.AnnotatedVirus
import com.hartwig.hmftools.datamodel.virus.VirusInterpretation
import com.hartwig.serve.datamodel.ActionableEvent
import com.hartwig.serve.datamodel.ActionableEvents
import com.hartwig.serve.datamodel.characteristic.TumorCharacteristicType

internal class VirusEvidence private constructor(private val hpvCharacteristics: List<ActionableEvent>,
                                                 private val ebvCharacteristics: List<ActionableEvent>) : EvidenceMatcher<AnnotatedVirus> {
    override fun findMatches(virus: AnnotatedVirus): List<ActionableEvent> {
        val interpretation = virus.interpretation()
        return if (interpretation == null || !virus.reported()) {
            Lists.newArrayList()
        } else when (interpretation) {
            VirusInterpretation.HPV -> {
                hpvCharacteristics
            }

            VirusInterpretation.EBV -> {
                ebvCharacteristics
            }

            else -> {
                Lists.newArrayList()
            }
        }
    }

    companion object {
        fun create(actionableEvents: ActionableEvents): VirusEvidence {
            val hpvCharacteristics: MutableList<ActionableEvent> = Lists.newArrayList()
            val ebvCharacteristics: MutableList<ActionableEvent> = Lists.newArrayList()
            for (actionableCharacteristic in actionableEvents.characteristics()) {
                if (actionableCharacteristic.type() == TumorCharacteristicType.HPV_POSITIVE) {
                    hpvCharacteristics.add(actionableCharacteristic)
                } else if (actionableCharacteristic.type() == TumorCharacteristicType.EBV_POSITIVE) {
                    ebvCharacteristics.add(actionableCharacteristic)
                }
            }
            return VirusEvidence(hpvCharacteristics, ebvCharacteristics)
        }
    }
}
