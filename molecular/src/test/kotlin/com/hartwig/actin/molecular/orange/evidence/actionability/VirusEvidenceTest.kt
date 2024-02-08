package com.hartwig.actin.molecular.orange.evidence.actionability

import com.hartwig.actin.molecular.datamodel.driver.VirusType
import com.hartwig.actin.molecular.orange.evidence.TestMolecularFactory
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

//        val virusMatch: VirusInterpreterEntry = TestVirusInterpreterFactory.builder().interpretation(VirusInterpretation.HPV).reported(true).build()
        val virusMatch = TestMolecularFactory.minimalTestVirus().copy(type = VirusType.HUMAN_PAPILLOMA_VIRUS, isReportable = true)
        val matches = virusEvidence.findMatches(virusMatch)
        assertEquals(1, matches.size.toLong())
        assertTrue(matches.contains(hpv))

//        val noInterpretation: VirusInterpreterEntry = TestVirusInterpreterFactory.builder().interpretation(null).reported(true).build()
        val noInterpretation = TestMolecularFactory.minimalTestVirus().copy(type = VirusType.OTHER, isReportable = true)
        assertTrue(virusEvidence.findMatches(noInterpretation).isEmpty())

//        val otherInterpretation: VirusInterpreterEntry = TestVirusInterpreterFactory.builder().interpretation(VirusInterpretation.MCV).reported(true).build()
        val otherInterpretation = TestMolecularFactory.minimalTestVirus().copy(type = VirusType.MERKEL_CELL_VIRUS, isReportable = true)
        assertTrue(virusEvidence.findMatches(otherInterpretation).isEmpty())

//        val notReported: VirusInterpreterEntry = TestVirusInterpreterFactory.builder().interpretation(VirusInterpretation.HPV).reported(false).build()
        val notReported = TestMolecularFactory.minimalTestVirus().copy(type = VirusType.HUMAN_PAPILLOMA_VIRUS, isReportable = false)
        assertTrue(virusEvidence.findMatches(notReported).isEmpty())
    }

    @Test
    fun canDetermineEvidenceForEBV() {
        val ebv: ActionableCharacteristic = TestServeActionabilityFactory.characteristicBuilder().type(TumorCharacteristicType.EBV_POSITIVE).build()
        val actionable: ActionableEvents = ImmutableActionableEvents.builder().addCharacteristics(ebv).build()
        val virusEvidence: VirusEvidence = VirusEvidence.create(actionable)

//        val virusMatch: VirusInterpreterEntry = TestVirusInterpreterFactory.builder().interpretation(VirusInterpretation.EBV).reported(true).build()
        val virusMatch = TestMolecularFactory.minimalTestVirus().copy(type = VirusType.EPSTEIN_BARR_VIRUS, isReportable = true)
        val matches = virusEvidence.findMatches(virusMatch)
        assertEquals(1, matches.size.toLong())
        assertTrue(matches.contains(ebv))

//        val noInterpretation: VirusInterpreterEntry = TestVirusInterpreterFactory.builder().interpretation(null).reported(true).build()
        val noInterpretation = TestMolecularFactory.minimalTestVirus().copy(type = VirusType.OTHER, isReportable = true)
        assertTrue(virusEvidence.findMatches(noInterpretation).isEmpty())

//        val otherInterpretation: VirusInterpreterEntry = TestVirusInterpreterFactory.builder().interpretation(VirusInterpretation.MCV).reported(true).build()
        val otherInterpretation = TestMolecularFactory.minimalTestVirus().copy(type = VirusType.MERKEL_CELL_VIRUS, isReportable = true)
        assertTrue(virusEvidence.findMatches(otherInterpretation).isEmpty())

//        val notReported: VirusInterpreterEntry = TestVirusInterpreterFactory.builder().interpretation(VirusInterpretation.EBV).reported(false).build()
        val notReported = TestMolecularFactory.minimalTestVirus().copy(type = VirusType.EPSTEIN_BARR_VIRUS, isReportable = false)
        assertTrue(virusEvidence.findMatches(notReported).isEmpty())
    }
}