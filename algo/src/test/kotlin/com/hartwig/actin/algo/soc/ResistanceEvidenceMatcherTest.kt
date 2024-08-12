package com.hartwig.actin.algo.soc

import com.hartwig.actin.TestTreatmentDatabaseFactory
import com.hartwig.actin.algo.datamodel.ResistanceEvidence
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory
import com.hartwig.actin.clinical.datamodel.treatment.DrugType
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.doid.datamodel.TestDoidEntryFactory
import com.hartwig.actin.molecular.datamodel.Drivers
import com.hartwig.actin.molecular.datamodel.driver.TestCopyNumberFactory
import com.hartwig.actin.molecular.datamodel.orange.driver.CopyNumberType
import com.hartwig.actin.serve.TestServeActionabilityFactory
import com.hartwig.serve.datamodel.ActionableEvents
import com.hartwig.serve.datamodel.EvidenceDirection
import com.hartwig.serve.datamodel.EvidenceLevel
import com.hartwig.serve.datamodel.ImmutableActionableEvents
import com.hartwig.serve.datamodel.gene.GeneEvent
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ResistanceEvidenceMatcherTest {

    private val actionableEvents: ActionableEvents = ImmutableActionableEvents.builder().addGenes(
        TestServeActionabilityFactory.geneBuilder().direction(EvidenceDirection.RESISTANT)
            .intervention(TestServeActionabilityFactory.treatmentBuilder().name("pembrolizumab").build())
            .applicableCancerType(TestServeActionabilityFactory.cancerTypeBuilder().doid("1520").build())
            .sourceEvent("BRAF amp")
            .event(GeneEvent.AMPLIFICATION)
            .gene("BRAF")
            .level(EvidenceLevel.A).build()
    ).build()
    private val doidEntry = TestDoidEntryFactory.createMinimalTestDoidEntry()
    private val tumorDoids = setOf("1520")
    private val treatmentDatabase = TestTreatmentDatabaseFactory.createProper()
    val drivers = Drivers(
        emptySet(), setOf(
            TestCopyNumberFactory.createMinimal().copy(
                gene = "BRAF", type = CopyNumberType.FULL_GAIN, isReportable = true
            )
        ), emptySet(), emptySet(), emptySet(), emptySet()
    )
    private val resistanceEvidenceMatcher =
        ResistanceEvidenceMatcher.create(doidEntry, tumorDoids, actionableEvents, treatmentDatabase, drivers)

    @Test
    fun `Should match resistance evidence to SOC treatments`() {
        val socTreatment =
            TreatmentTestFactory.drugTreatment("PEMBROLIZUMAB", TreatmentCategory.IMMUNOTHERAPY, setOf(DrugType.TOPO1_INHIBITOR))

        val actualResistanceEvidence = resistanceEvidenceMatcher.match(socTreatment)
        val expectedResistanceEvidence = listOf(
            ResistanceEvidence(
                event = "BRAF amp",
                isTested = null,
                isFound = true,
                resistanceLevel = "A",
                evidenceUrls = emptySet(),
                treatmentName = "PEMBROLIZUMAB"
            )
        )

        assertThat(actualResistanceEvidence).isEqualTo(expectedResistanceEvidence)
    }

    @Test
    fun `Should return empty resistance evidence list for SOC treatment without resistance evidence`() {
        val socTreatment = TreatmentTestFactory.drugTreatment("capecitabine+oxaliplatin", TreatmentCategory.CHEMOTHERAPY)
        val actualResistanceEvidence = resistanceEvidenceMatcher.match(socTreatment)
        val expectedResistanceEvidence = emptyList<ResistanceEvidence>()

        assertThat(actualResistanceEvidence).isEqualTo(expectedResistanceEvidence)
    }
}