package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.serve.datamodel.ActionableEvent
import com.hartwig.serve.datamodel.ActionableEvents
import com.hartwig.serve.datamodel.characteristic.ActionableCharacteristic
import com.hartwig.serve.datamodel.characteristic.TumorCharacteristicType

internal class SignatureEvidence private constructor(private val signatureCharacteristics: List<ActionableCharacteristic>) {

    fun findMicrosatelliteMatches(isMicrosatelliteUnstable: Boolean): List<ActionableEvent> {
        return findMatches(isMicrosatelliteUnstable, TumorCharacteristicType.MICROSATELLITE_UNSTABLE)
    }

    fun findHomologousRepairMatches(isHomologousRepairDeficient: Boolean): List<ActionableEvent> {
        return findMatches(isHomologousRepairDeficient, TumorCharacteristicType.HOMOLOGOUS_RECOMBINATION_DEFICIENT)
    }

    fun findTumorBurdenMatches(hasHighTumorMutationalBurden: Boolean): List<ActionableEvent> {
        return findMatches(hasHighTumorMutationalBurden, TumorCharacteristicType.HIGH_TUMOR_MUTATIONAL_BURDEN)
    }

    fun findTumorLoadMatches(hasHighTumorMutationalLoad: Boolean): List<ActionableEvent> {
        return findMatches(hasHighTumorMutationalLoad, TumorCharacteristicType.HIGH_TUMOR_MUTATIONAL_LOAD)
    }

    private fun findMatches(hasCharacteristic: Boolean, typeToFind: TumorCharacteristicType): List<ActionableEvent> {
        return if (!hasCharacteristic) emptyList() else signatureCharacteristics.filter { it.type() == typeToFind }
    }

    companion object {
        private val signatureCharacteristicTypes = setOf(
            TumorCharacteristicType.MICROSATELLITE_UNSTABLE,
            TumorCharacteristicType.HOMOLOGOUS_RECOMBINATION_DEFICIENT,
            TumorCharacteristicType.HIGH_TUMOR_MUTATIONAL_BURDEN,
            TumorCharacteristicType.HIGH_TUMOR_MUTATIONAL_LOAD
        )

        fun create(actionableEvents: ActionableEvents): SignatureEvidence {
            return SignatureEvidence(actionableEvents.characteristics().filter { it.type() in signatureCharacteristicTypes })
        }
    }
}
