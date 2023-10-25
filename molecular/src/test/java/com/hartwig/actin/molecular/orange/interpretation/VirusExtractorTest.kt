package com.hartwig.actin.molecular.orange.interpretation

import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood
import com.hartwig.actin.molecular.datamodel.driver.Virus
import com.hartwig.actin.molecular.datamodel.driver.VirusType
import com.hartwig.actin.molecular.orange.datamodel.virus.TestVirusInterpreterFactory
import com.hartwig.actin.molecular.orange.evidence.TestEvidenceDatabaseFactory
import com.hartwig.hmftools.datamodel.virus.AnnotatedVirus
import com.hartwig.hmftools.datamodel.virus.ImmutableVirusInterpreterData
import com.hartwig.hmftools.datamodel.virus.VirusBreakendQCStatus
import com.hartwig.hmftools.datamodel.virus.VirusInterpretation
import com.hartwig.hmftools.datamodel.virus.VirusInterpreterData
import com.hartwig.hmftools.datamodel.virus.VirusLikelihoodType
import org.junit.Assert
import org.junit.Test

class VirusExtractorTest {
    @Test
    fun canExtractViruses() {
        val virusEntry1: AnnotatedVirus = TestVirusInterpreterFactory.builder()
            .reported(true)
            .name("virus 1")
            .qcStatus(VirusExtractor.Companion.QC_PASS_STATUS)
            .interpretation(VirusInterpretation.HPV)
            .integrations(2)
            .virusDriverLikelihoodType(VirusLikelihoodType.HIGH)
            .build()
        val virusEntry2: AnnotatedVirus = TestVirusInterpreterFactory.builder()
            .reported(false)
            .name("virus 2")
            .qcStatus(VirusBreakendQCStatus.LOW_VIRAL_COVERAGE)
            .interpretation(null)
            .integrations(0)
            .virusDriverLikelihoodType(VirusLikelihoodType.LOW)
            .build()
        val virusInterpreter: VirusInterpreterData = ImmutableVirusInterpreterData.builder().addAllViruses(virusEntry1, virusEntry2).build()
        val virusExtractor = VirusExtractor(TestEvidenceDatabaseFactory.createEmptyDatabase())
        val viruses = virusExtractor.extract(virusInterpreter)
        Assert.assertEquals(2, viruses.size.toLong())
        val virus1 = findByName(viruses, "virus 1")
        Assert.assertTrue(virus1.isReportable())
        Assert.assertEquals(DriverLikelihood.HIGH, virus1.driverLikelihood())
        Assert.assertEquals(VirusType.HUMAN_PAPILLOMA_VIRUS, virus1.type())
        Assert.assertTrue(virus1.isReliable())
        Assert.assertEquals(2, virus1.integrations().toLong())
        val virus2 = findByName(viruses, "virus 2")
        Assert.assertFalse(virus2.isReportable())
        Assert.assertEquals(DriverLikelihood.LOW, virus2.driverLikelihood())
        Assert.assertEquals(VirusType.OTHER, virus2.type())
        Assert.assertFalse(virus2.isReliable())
        Assert.assertEquals(0, virus2.integrations().toLong())
    }

    @Test
    fun canDetermineDriverLikelihoodForAllVirusDriverLikelihoods() {
        val expectedDriverLikelihoodLookup: MutableMap<VirusLikelihoodType?, DriverLikelihood?> = HashMap()
        expectedDriverLikelihoodLookup[VirusLikelihoodType.LOW] = DriverLikelihood.LOW
        expectedDriverLikelihoodLookup[VirusLikelihoodType.HIGH] = DriverLikelihood.HIGH
        expectedDriverLikelihoodLookup[VirusLikelihoodType.UNKNOWN] = null
        for (virusDriverLikelihood in VirusLikelihoodType.values()) {
            Assert.assertEquals(expectedDriverLikelihoodLookup[virusDriverLikelihood],
                VirusExtractor.Companion.determineDriverLikelihood(virusDriverLikelihood))
        }
    }

    @Test
    fun canDetermineTypeForAllInterpretations() {
        Assert.assertEquals(VirusType.OTHER, VirusExtractor.Companion.determineType(null))
        for (interpretation in VirusInterpretation.values()) {
            Assert.assertNotNull(VirusExtractor.Companion.determineType(interpretation))
        }
    }

    companion object {
        private fun findByName(viruses: MutableSet<Virus>, nameToFind: String): Virus {
            for (virus in viruses) {
                if (virus.name() == nameToFind) {
                    return virus
                }
            }
            throw IllegalStateException("Could not find virus with name: $nameToFind")
        }
    }
}