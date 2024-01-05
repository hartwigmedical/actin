package com.hartwig.actin.molecular.orange.interpretation

import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood
import com.hartwig.actin.molecular.datamodel.driver.Virus
import com.hartwig.actin.molecular.datamodel.driver.VirusType
import com.hartwig.actin.molecular.orange.datamodel.virus.TestVirusInterpreterFactory
import com.hartwig.actin.molecular.orange.evidence.TestEvidenceDatabaseFactory
import com.hartwig.hmftools.datamodel.virus.ImmutableVirusInterpreterData
import com.hartwig.hmftools.datamodel.virus.VirusBreakendQCStatus
import com.hartwig.hmftools.datamodel.virus.VirusInterpretation
import com.hartwig.hmftools.datamodel.virus.VirusLikelihoodType
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class VirusExtractorTest {

    @Test
    fun `Should extract viruses`() {
        val virusEntry1 = TestVirusInterpreterFactory.builder()
            .reported(true)
            .name("virus 1")
            .qcStatus(VirusExtractor.QC_PASS_STATUS)
            .interpretation(VirusInterpretation.HPV)
            .integrations(2)
            .driverLikelihood(VirusLikelihoodType.HIGH)
            .build()

        val virusEntry2 = TestVirusInterpreterFactory.builder()
            .reported(false)
            .name("virus 2")
            .qcStatus(VirusBreakendQCStatus.LOW_VIRAL_COVERAGE)
            .interpretation(null)
            .integrations(0)
            .driverLikelihood(VirusLikelihoodType.LOW)
            .build()
        val virusInterpreter = ImmutableVirusInterpreterData.builder().addAllViruses(virusEntry1, virusEntry2).build()
        val virusExtractor = VirusExtractor(TestEvidenceDatabaseFactory.createEmptyDatabase())

        val viruses = virusExtractor.extract(virusInterpreter)
        assertThat(viruses.size.toLong()).isEqualTo(2)

        val virus1 = findByName(viruses, "virus 1")
        assertThat(virus1.isReportable).isTrue
        assertThat(virus1.driverLikelihood).isEqualTo(DriverLikelihood.HIGH)
        assertThat(virus1.type).isEqualTo(VirusType.HUMAN_PAPILLOMA_VIRUS)
        assertThat(virus1.isReliable).isTrue
        assertThat(virus1.integrations.toLong()).isEqualTo(2)

        val virus2 = findByName(viruses, "virus 2")
        assertThat(virus2.isReportable).isFalse
        assertThat(virus2.driverLikelihood).isEqualTo(DriverLikelihood.LOW)
        assertThat(virus2.type).isEqualTo(VirusType.OTHER)
        assertThat(virus2.isReliable).isFalse
        assertThat(virus2.integrations.toLong()).isEqualTo(0)
    }

    @Test
    fun `Should determine driver likelihood for all virus driver likelihoods`() {
        val expectedDriverLikelihoodLookup = mapOf(
            VirusLikelihoodType.LOW to DriverLikelihood.LOW,
            VirusLikelihoodType.HIGH to DriverLikelihood.HIGH,
            VirusLikelihoodType.UNKNOWN to null
        )
        for (virusDriverLikelihood in VirusLikelihoodType.values()) {
            assertThat(VirusExtractor.determineDriverLikelihood(virusDriverLikelihood))
                .isEqualTo(expectedDriverLikelihoodLookup[virusDriverLikelihood])
        }
    }

    @Test
    fun `Should determine type for all interpretations`() {
        assertThat(VirusExtractor.determineType(null)).isEqualTo(VirusType.OTHER)
        for (interpretation in VirusInterpretation.values()) {
            assertThat(VirusExtractor.determineType(interpretation)).isNotNull()
        }
    }

    companion object {
        private fun findByName(viruses: Set<Virus>, nameToFind: String): Virus {
            return viruses.find { it.name == nameToFind }
                ?: throw IllegalStateException("Could not find virus with name: $nameToFind")
        }
    }
}