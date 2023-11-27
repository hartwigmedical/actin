package com.hartwig.actin.molecular.orange.evidence.actionability

import com.hartwig.actin.molecular.orange.datamodel.virus.TestVirusInterpreterFactory
import com.hartwig.hmftools.datamodel.virus.VirusInterpretation
import com.hartwig.hmftools.datamodel.virus.VirusInterpreterEntry
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

        val virusMatch: VirusInterpreterEntry = TestVirusInterpreterFactory.builder().interpretation(VirusInterpretation.HPV).reported(true).build()
        val matches = virusEvidence.findMatches(virusMatch)
        assertEquals(1, matches.size.toLong())
        assertTrue(matches.contains(hpv))

        val noInterpretation: VirusInterpreterEntry = TestVirusInterpreterFactory.builder().interpretation(null).reported(true).build()
        assertTrue(virusEvidence.findMatches(noInterpretation).isEmpty())

        val otherInterpretation: VirusInterpreterEntry = TestVirusInterpreterFactory.builder().interpretation(VirusInterpretation.MCV).reported(true).build()
        assertTrue(virusEvidence.findMatches(otherInterpretation).isEmpty())

        val notReported: VirusInterpreterEntry = TestVirusInterpreterFactory.builder().interpretation(VirusInterpretation.HPV).reported(false).build()
        assertTrue(virusEvidence.findMatches(notReported).isEmpty())
    }

    @Test
    fun canDetermineEvidenceForEBV() {
        val ebv: ActionableCharacteristic = TestServeActionabilityFactory.characteristicBuilder().type(TumorCharacteristicType.EBV_POSITIVE).build()
        val actionable: ActionableEvents = ImmutableActionableEvents.builder().addCharacteristics(ebv).build()
        val virusEvidence: VirusEvidence = VirusEvidence.create(actionable)

        val virusMatch: VirusInterpreterEntry = TestVirusInterpreterFactory.builder().interpretation(VirusInterpretation.EBV).reported(true).build()
        val matches = virusEvidence.findMatches(virusMatch)
        assertEquals(1, matches.size.toLong())
        assertTrue(matches.contains(ebv))

        val noInterpretation: VirusInterpreterEntry = TestVirusInterpreterFactory.builder().interpretation(null).reported(true).build()
        assertTrue(virusEvidence.findMatches(noInterpretation).isEmpty())

        val otherInterpretation: VirusInterpreterEntry = TestVirusInterpreterFactory.builder().interpretation(VirusInterpretation.MCV).reported(true).build()
        assertTrue(virusEvidence.findMatches(otherInterpretation).isEmpty())

        val notReported: VirusInterpreterEntry = TestVirusInterpreterFactory.builder().interpretation(VirusInterpretation.EBV).reported(false).build()
        assertTrue(virusEvidence.findMatches(notReported).isEmpty())
    }
}