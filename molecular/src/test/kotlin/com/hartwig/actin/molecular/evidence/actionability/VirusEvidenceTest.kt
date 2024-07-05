package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.molecular.TestMolecularFactory.minimalVirus
import com.hartwig.actin.molecular.datamodel.orange.driver.VirusType
import com.hartwig.serve.datamodel.ActionableEvents
import com.hartwig.serve.datamodel.ImmutableActionableEvents
import com.hartwig.serve.datamodel.characteristic.ActionableCharacteristic
import com.hartwig.serve.datamodel.characteristic.TumorCharacteristicType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VirusEvidenceTest {

    @Test
    fun canDetermineEvidenceForHPV() {
        val hpv: ActionableCharacteristic = TestServeActionabilityFactory.characteristicBuilder().type(TumorCharacteristicType.HPV_POSITIVE).build()
        val actionable: ActionableEvents = ImmutableActionableEvents.builder().addCharacteristics(hpv).build()
        val virusEvidence: VirusEvidence = VirusEvidence.create(actionable)

        val virusMatch = minimalVirus().copy(type = VirusType.HUMAN_PAPILLOMA_VIRUS, isReportable = true)
        val matches = virusEvidence.findMatches(virusMatch)
        assertEquals(1, matches.size.toLong())
        assertTrue(matches.contains(hpv))

        val noInterpretation = minimalVirus().copy(type = VirusType.OTHER, isReportable = true)
        assertTrue(virusEvidence.findMatches(noInterpretation).isEmpty())

        val otherInterpretation = minimalVirus().copy(type = VirusType.MERKEL_CELL_VIRUS, isReportable = true)
        assertTrue(virusEvidence.findMatches(otherInterpretation).isEmpty())

        val notReported = minimalVirus().copy(type = VirusType.HUMAN_PAPILLOMA_VIRUS, isReportable = false)
        assertTrue(virusEvidence.findMatches(notReported).isEmpty())
    }

    @Test
    fun canDetermineEvidenceForEBV() {
        val ebv: ActionableCharacteristic = TestServeActionabilityFactory.characteristicBuilder().type(TumorCharacteristicType.EBV_POSITIVE).build()
        val actionable: ActionableEvents = ImmutableActionableEvents.builder().addCharacteristics(ebv).build()
        val virusEvidence: VirusEvidence = VirusEvidence.create(actionable)

        val virusMatch = minimalVirus().copy(type = VirusType.EPSTEIN_BARR_VIRUS, isReportable = true)
        val matches = virusEvidence.findMatches(virusMatch)
        assertEquals(1, matches.size.toLong())
        assertTrue(matches.contains(ebv))

        val noInterpretation = minimalVirus().copy(type = VirusType.OTHER, isReportable = true)
        assertTrue(virusEvidence.findMatches(noInterpretation).isEmpty())

        val otherInterpretation = minimalVirus().copy(type = VirusType.MERKEL_CELL_VIRUS, isReportable = true)
        assertTrue(virusEvidence.findMatches(otherInterpretation).isEmpty())

        val notReported = minimalVirus().copy(type = VirusType.EPSTEIN_BARR_VIRUS, isReportable = false)
        assertTrue(virusEvidence.findMatches(notReported).isEmpty())
    }
}