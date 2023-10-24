package com.hartwig.actin.molecular.orange.evidence.actionability

import com.hartwig.actin.molecular.orange.datamodel.virus.TestVirusInterpreterFactory
import com.hartwig.hmftools.datamodel.virus.AnnotatedVirus
import com.hartwig.hmftools.datamodel.virus.VirusInterpretation
import com.hartwig.serve.datamodel.ActionableEvents
import com.hartwig.serve.datamodel.ImmutableActionableEvents
import com.hartwig.serve.datamodel.characteristic.ActionableCharacteristic
import com.hartwig.serve.datamodel.characteristic.TumorCharacteristicType
import org.junit.Assert
import org.junit.Test

class VirusEvidenceTest {
    @Test
    fun canDetermineEvidenceForHPV() {
        val hpv: ActionableCharacteristic? = TestServeActionabilityFactory.characteristicBuilder().type(TumorCharacteristicType.HPV_POSITIVE).build()
        val actionable: ActionableEvents? = ImmutableActionableEvents.builder().addCharacteristics(hpv).build()
        val virusEvidence: VirusEvidence = VirusEvidence.Companion.create(actionable)
        val virusMatch: AnnotatedVirus? = TestVirusInterpreterFactory.builder().interpretation(VirusInterpretation.HPV).reported(true).build()
        val matches = virusEvidence.findMatches(virusMatch)
        Assert.assertEquals(1, matches.size.toLong())
        Assert.assertTrue(matches.contains(hpv))
        val noInterpretation: AnnotatedVirus? = TestVirusInterpreterFactory.builder().interpretation(null).reported(true).build()
        Assert.assertTrue(virusEvidence.findMatches(noInterpretation).isEmpty())
        val otherInterpretation: AnnotatedVirus? = TestVirusInterpreterFactory.builder().interpretation(VirusInterpretation.MCV).reported(true).build()
        Assert.assertTrue(virusEvidence.findMatches(otherInterpretation).isEmpty())
        val notReported: AnnotatedVirus? = TestVirusInterpreterFactory.builder().interpretation(VirusInterpretation.HPV).reported(false).build()
        Assert.assertTrue(virusEvidence.findMatches(notReported).isEmpty())
    }

    @Test
    fun canDetermineEvidenceForEBV() {
        val ebv: ActionableCharacteristic? = TestServeActionabilityFactory.characteristicBuilder().type(TumorCharacteristicType.EBV_POSITIVE).build()
        val actionable: ActionableEvents? = ImmutableActionableEvents.builder().addCharacteristics(ebv).build()
        val virusEvidence: VirusEvidence = VirusEvidence.Companion.create(actionable)
        val virusMatch: AnnotatedVirus? = TestVirusInterpreterFactory.builder().interpretation(VirusInterpretation.EBV).reported(true).build()
        val matches = virusEvidence.findMatches(virusMatch)
        Assert.assertEquals(1, matches.size.toLong())
        Assert.assertTrue(matches.contains(ebv))
        val noInterpretation: AnnotatedVirus? = TestVirusInterpreterFactory.builder().interpretation(null).reported(true).build()
        Assert.assertTrue(virusEvidence.findMatches(noInterpretation).isEmpty())
        val otherInterpretation: AnnotatedVirus? = TestVirusInterpreterFactory.builder().interpretation(VirusInterpretation.MCV).reported(true).build()
        Assert.assertTrue(virusEvidence.findMatches(otherInterpretation).isEmpty())
        val notReported: AnnotatedVirus? = TestVirusInterpreterFactory.builder().interpretation(VirusInterpretation.EBV).reported(false).build()
        Assert.assertTrue(virusEvidence.findMatches(notReported).isEmpty())
    }
}