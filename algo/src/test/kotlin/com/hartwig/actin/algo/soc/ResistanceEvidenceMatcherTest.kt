package com.hartwig.actin.algo.soc

import com.hartwig.actin.TestTreatmentDatabaseFactory
import com.hartwig.actin.algo.datamodel.ResistanceEvidence
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.doid.datamodel.TestDoidEntryFactory
import com.hartwig.actin.serve.TestServeActionabilityFactory
import com.hartwig.serve.datamodel.ActionableEvents
import com.hartwig.serve.datamodel.EvidenceDirection
import com.hartwig.serve.datamodel.EvidenceLevel
import com.hartwig.serve.datamodel.ImmutableActionableEvents
import org.assertj.core.api.Assertions
import org.junit.Test

class ResistanceEvidenceMatcherTest {

    private val actionableEvents: ActionableEvents = ImmutableActionableEvents.builder().addHotspots(
        TestServeActionabilityFactory.hotspotBuilder().direction(EvidenceDirection.RESISTANT)
            .intervention(TestServeActionabilityFactory.treatmentBuilder().name("pembrolizumab").build())
            .applicableCancerType(TestServeActionabilityFactory.cancerTypeBuilder().doid("1520").build()).sourceEvent("BRAF amp")
            .level(EvidenceLevel.A).build()
    ).build()
    private val doidEntry = TestDoidEntryFactory.createMinimalTestDoidEntry()
    private val tumorDoids = setOf("1520")
    private val treatmentDatabase = TestTreatmentDatabaseFactory.createProper()
    private val resistanceEvidenceMatcher = ResistanceEvidenceMatcher.create(doidEntry, tumorDoids, actionableEvents, treatmentDatabase)

    @Test
    fun `Should match resistance evidence to SOC treatments`() {
        val socTreatment = TreatmentTestFactory.drugTreatment("PEMBROLIZUMAB", TreatmentCategory.IMMUNOTHERAPY)

        val actualResistanceEvidence = resistanceEvidenceMatcher.match(socTreatment)
        val expectedResistanceEvidence = listOf(
            ResistanceEvidence(
                event = "BRAF amp",
                isTested = null,
                isFound = null,
                resistanceLevel = "A",
                evidenceUrls = emptySet()
            )
        )

        Assertions.assertThat(actualResistanceEvidence).isEqualTo(expectedResistanceEvidence)
    }

    @Test
    fun `Should return empty resistance evidence list for SOC treatment without resistance evidence`() {
        val socTreatment = TreatmentTestFactory.drugTreatment("capecitabine+oxaliplatin", TreatmentCategory.CHEMOTHERAPY)
        val actualResistanceEvidence = resistanceEvidenceMatcher.match(socTreatment)
        val expectedResistanceEvidence = emptyList<ResistanceEvidence>()

        Assertions.assertThat(actualResistanceEvidence).isEqualTo(expectedResistanceEvidence)
    }
}