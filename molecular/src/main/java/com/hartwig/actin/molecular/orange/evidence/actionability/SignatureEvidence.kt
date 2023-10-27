package com.hartwig.actin.molecular.orange.evidence.actionability

import com.google.common.collect.Lists
import com.hartwig.serve.datamodel.ActionableEvent
import com.hartwig.serve.datamodel.ActionableEvents
import com.hartwig.serve.datamodel.characteristic.ActionableCharacteristic
import com.hartwig.serve.datamodel.characteristic.TumorCharacteristicType

internal class SignatureEvidence private constructor(private val signatureCharacteristics: List<ActionableCharacteristic>) {
    fun findMicrosatelliteMatches(isMicrosatelliteUnstable: Boolean): List<ActionableEvent> {
        return findMatches(isMicrosatelliteUnstable, MICROSATELLITE_UNSTABLE_TYPE)
    }

    fun findHomologousRepairMatches(isHomologousRepairDeficient: Boolean): List<ActionableEvent> {
        return findMatches(isHomologousRepairDeficient, HOMOLOGOUS_REPAIR_DEFICIENT_TYPE)
    }

    fun findTumorBurdenMatches(hasHighTumorMutationalBurden: Boolean): List<ActionableEvent> {
        return findMatches(hasHighTumorMutationalBurden, HIGH_TUMOR_MUTATIONAL_BURDEN_TYPE)
    }

    fun findTumorLoadMatches(hasHighTumorMutationalLoad: Boolean): List<ActionableEvent> {
        return findMatches(hasHighTumorMutationalLoad, HIGH_TUMOR_MUTATIONAL_LOAD_TYPE)
    }

    private fun findMatches(hasCharacteristic: Boolean, typeToFind: TumorCharacteristicType): List<ActionableEvent> {
        if (!hasCharacteristic) {
            return Lists.newArrayList()
        }
        val matches: MutableList<ActionableEvent> = Lists.newArrayList()
        for (actionableCharacteristic in signatureCharacteristics) {
            if (actionableCharacteristic.type() == typeToFind) {
                matches.add(actionableCharacteristic)
            }
        }
        return matches
    }

    companion object {
        private val MICROSATELLITE_UNSTABLE_TYPE: TumorCharacteristicType = TumorCharacteristicType.MICROSATELLITE_UNSTABLE
        private val HOMOLOGOUS_REPAIR_DEFICIENT_TYPE: TumorCharacteristicType = TumorCharacteristicType.HOMOLOGOUS_RECOMBINATION_DEFICIENT
        private val HIGH_TUMOR_MUTATIONAL_BURDEN_TYPE: TumorCharacteristicType = TumorCharacteristicType.HIGH_TUMOR_MUTATIONAL_BURDEN
        private val HIGH_TUMOR_MUTATIONAL_LOAD_TYPE: TumorCharacteristicType = TumorCharacteristicType.HIGH_TUMOR_MUTATIONAL_LOAD
        fun create(actionableEvents: ActionableEvents): SignatureEvidence {
            val signatureCharacteristics: MutableList<ActionableCharacteristic> = Lists.newArrayList()
            for (actionableCharacteristic in actionableEvents.characteristics()) {
                if (actionableCharacteristic.type() == MICROSATELLITE_UNSTABLE_TYPE || actionableCharacteristic.type() == HOMOLOGOUS_REPAIR_DEFICIENT_TYPE || actionableCharacteristic.type() == HIGH_TUMOR_MUTATIONAL_BURDEN_TYPE || actionableCharacteristic.type() == HIGH_TUMOR_MUTATIONAL_LOAD_TYPE) {
                    signatureCharacteristics.add(actionableCharacteristic)
                }
            }
            return SignatureEvidence(signatureCharacteristics)
        }
    }
}
