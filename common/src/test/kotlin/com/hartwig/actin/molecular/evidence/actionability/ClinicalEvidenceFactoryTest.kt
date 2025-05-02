package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.datamodel.molecular.evidence.CancerType
import com.hartwig.actin.datamodel.molecular.evidence.CancerTypeMatchApplicability
import com.hartwig.actin.datamodel.molecular.evidence.Country
import com.hartwig.actin.datamodel.molecular.evidence.CountryDetails
import com.hartwig.actin.datamodel.molecular.evidence.EvidenceLevel
import com.hartwig.actin.datamodel.molecular.evidence.EvidenceLevelDetails
import com.hartwig.actin.datamodel.molecular.evidence.EvidenceType
import com.hartwig.actin.datamodel.molecular.evidence.Hospital
import com.hartwig.actin.datamodel.molecular.evidence.TestClinicalEvidenceFactory
import com.hartwig.actin.datamodel.molecular.evidence.TestEvidenceDirectionFactory
import com.hartwig.actin.datamodel.molecular.evidence.TestExternalTrialFactory
import com.hartwig.actin.datamodel.molecular.evidence.TestMolecularMatchDetailsFactory
import com.hartwig.actin.datamodel.molecular.evidence.TestTreatmentEvidenceFactory
import com.hartwig.actin.molecular.evidence.TestServeEvidenceFactory
import com.hartwig.actin.molecular.evidence.TestServeFactory
import com.hartwig.actin.molecular.evidence.TestServeMolecularFactory
import com.hartwig.actin.molecular.evidence.TestServeTrialFactory
import com.hartwig.serve.datamodel.efficacy.EvidenceDirection
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import com.hartwig.serve.datamodel.efficacy.EvidenceLevel as ServeEvidenceLevel
import com.hartwig.serve.datamodel.efficacy.EvidenceLevelDetails as ServeEvidenceLevelDetails

private val BASE_ACTIONABLE_EVENT = TestServeMolecularFactory.createActionableEvent()

class ClinicalEvidenceFactoryTest {

    private val cancerTypeResolver = mockk<EvidenceCancerTypeResolver>()
    val factory = ClinicalEvidenceFactory(cancerTypeResolver)

    @Test
    fun `Should convert SERVE specifically applicable cancer type hotspot evidence to treatment evidence`() {
        val evidence = TestServeEvidenceFactory.create(
            treatment = "on-label",
            indication = TestServeFactory.createIndicationWithTypeAndExcludedTypes(
                type = "on-label type",
                excludedTypes = setOf("excluded 1", "excluded 2")
            ),
            molecularCriterium = TestServeMolecularFactory.createHotspotCriterium(BASE_ACTIONABLE_EVENT),
            evidenceLevel = ServeEvidenceLevel.D,
            evidenceLevelDetails = ServeEvidenceLevelDetails.CASE_REPORTS_SERIES,
            evidenceDirection = EvidenceDirection.NO_BENEFIT
        )
        every { cancerTypeResolver.resolve(evidence.indication()) } returns CancerTypeMatchApplicability.SPECIFIC_TYPE
        val result = factory.create(
            actionabilityMatch = ActionabilityMatch(
                listOf(
                    evidence
                ), emptyMap()
            )
        )

        val expectedClinicalEvidence = TestClinicalEvidenceFactory.withEvidence(
            TestTreatmentEvidenceFactory.create(
                treatment = "on-label",
                cancerTypeMatchApplicability = CancerTypeMatchApplicability.SPECIFIC_TYPE,
                sourceDate = BASE_ACTIONABLE_EVENT.sourceDate(),
                sourceEvent = BASE_ACTIONABLE_EVENT.sourceEvent(),
                evidenceType = EvidenceType.HOTSPOT_MUTATION,
                matchedCancerType = "on-label type",
                excludedCancerSubTypes = setOf("excluded 1", "excluded 2"),
                evidenceLevel = EvidenceLevel.D,
                evidenceLevelDetails = EvidenceLevelDetails.CASE_REPORTS_SERIES,
                evidenceDirection = TestEvidenceDirectionFactory.noBenefit()
            )
        )

        assertThat(result).isEqualTo(expectedClinicalEvidence)
    }

    @Test
    fun `Should convert SERVE other applicable cancer type range evidence to treatment evidence`() {
        val indication = TestServeFactory.createIndicationWithTypeAndExcludedTypes(
            type = "off-label type",
            excludedTypes = emptySet()
        )
        every { cancerTypeResolver.resolve(indication) }.returns(CancerTypeMatchApplicability.OTHER_TYPE)
        val result =
            factory.create(
                ActionabilityMatch(
                    listOf(
                        TestServeEvidenceFactory.create(
                            treatment = "off-label",
                            indication = indication,
                            molecularCriterium = TestServeMolecularFactory.createCodonCriterium(baseActionableEvent = BASE_ACTIONABLE_EVENT),
                            evidenceLevel = ServeEvidenceLevel.B,
                            evidenceLevelDetails = ServeEvidenceLevelDetails.CLINICAL_STUDY,
                            evidenceDirection = EvidenceDirection.RESPONSIVE
                        )
                    ), emptyMap()
                )
            )

        val expectedClinicalEvidence = TestClinicalEvidenceFactory.withEvidence(
            TestTreatmentEvidenceFactory.create(
                treatment = "off-label",
                cancerTypeMatchApplicability = CancerTypeMatchApplicability.OTHER_TYPE,
                sourceDate = BASE_ACTIONABLE_EVENT.sourceDate(),
                sourceEvent = BASE_ACTIONABLE_EVENT.sourceEvent(),
                evidenceType = EvidenceType.CODON_MUTATION,
                matchedCancerType = "off-label type",
                excludedCancerSubTypes = emptySet(),
                evidenceLevel = EvidenceLevel.B,
                evidenceLevelDetails = EvidenceLevelDetails.CLINICAL_STUDY,
                evidenceDirection = TestEvidenceDirectionFactory.certainPositiveResponse()
            )
        )

        assertThat(result).isEqualTo(expectedClinicalEvidence)
    }

    @Test
    fun `Should convert SERVE all applicable cancer type range evidence to treatment evidence`() {
        val indication = TestServeFactory.createIndicationWithTypeAndExcludedTypes(
            type = "off-label type",
            excludedTypes = emptySet()
        )
        every { cancerTypeResolver.resolve(indication) }.returns(CancerTypeMatchApplicability.ALL_TYPES)
        val result =
            factory.create(
                ActionabilityMatch(
                    listOf(
                        TestServeEvidenceFactory.create(
                            treatment = "off-label",
                            indication = indication,
                            molecularCriterium = TestServeMolecularFactory.createCodonCriterium(baseActionableEvent = BASE_ACTIONABLE_EVENT),
                            evidenceLevel = ServeEvidenceLevel.B,
                            evidenceLevelDetails = ServeEvidenceLevelDetails.CLINICAL_STUDY,
                            evidenceDirection = EvidenceDirection.RESPONSIVE
                        )
                    ), emptyMap()
                )
            )

        val expectedClinicalEvidence = TestClinicalEvidenceFactory.withEvidence(
            TestTreatmentEvidenceFactory.create(
                treatment = "off-label",
                cancerTypeMatchApplicability = CancerTypeMatchApplicability.ALL_TYPES,
                sourceDate = BASE_ACTIONABLE_EVENT.sourceDate(),
                sourceEvent = BASE_ACTIONABLE_EVENT.sourceEvent(),
                evidenceType = EvidenceType.CODON_MUTATION,
                matchedCancerType = "off-label type",
                excludedCancerSubTypes = emptySet(),
                evidenceLevel = EvidenceLevel.B,
                evidenceLevelDetails = EvidenceLevelDetails.CLINICAL_STUDY,
                evidenceDirection = TestEvidenceDirectionFactory.certainPositiveResponse()
            )
        )

        assertThat(result).isEqualTo(expectedClinicalEvidence)
    }

    @Test
    fun `Should convert single SERVE actionable trial to external trial`() {
        val matchingEvent1 = TestServeMolecularFactory.createActionableEvent(sourceDate = LocalDate.of(2022, 1, 1), sourceEvent = "event 1")
        val matchingEvent2 = TestServeMolecularFactory.createActionableEvent(sourceDate = LocalDate.of(2023, 1, 1), sourceEvent = "event 2")


       /* val matchingIndication1 = TestServeFactory.createIndicationWithTypeAndExcludedTypes(type = "matched 1", excludedTypes = emptySet())
        val matchingIndication2 =
            TestServeFactory.createIndicationWithTypeAndExcludedTypes(type = "matched 2", excludedTypes = setOf("excluded"))*/
        val country = TestServeFactory.createCountry(
            name = "Netherlands",
            hospitalsPerCity = mapOf(
                "city 1" to
                        setOf(
                            TestServeFactory.createHospital("hospital 1", true),
                            TestServeFactory.createHospital("hospital 2", false)
                        )
            )
        )

        val trial = TestServeTrialFactory.create(
            nctId = "NCT00000001",
            title = "test trial",
            acronym = "test trial acronym",
            countries = setOf(country),
            urls = setOf("invalid url", "https://clinicaltrials.gov/study/NCT00000001")
        )
        val result =
            factory.create(
                ActionabilityMatch(
                    evidenceMatches = emptyList(),
                    matchingCriteriaPerTrialMatch = mapOf(
                        trial to setOf(
                            TestServeMolecularFactory.createHotspotCriterium(matchingEvent1),
                            TestServeMolecularFactory.createHotspotCriterium(matchingEvent2)
                        )
                    )
                )
            )

        val expectedClinicalEvidence = TestClinicalEvidenceFactory.withEligibleTrial(
            TestExternalTrialFactory.create(
                nctId = "NCT00000001",
                title = "test trial",
                acronym = "test trial acronym",
                countries = setOf(
                    CountryDetails(
                        country = Country.NETHERLANDS,
                        hospitalsPerCity = mapOf(
                            "city 1" to
                                    setOf(
                                        Hospital(name = "hospital 1", isChildrensHospital = true),
                                        Hospital(name = "hospital 2", isChildrensHospital = false)
                                    )
                        )
                    )
                ),
                molecularMatches = setOf(
                    TestMolecularMatchDetailsFactory.create(
                        sourceDate = LocalDate.of(2022, 1, 1),
                        sourceEvent = "event 1",
                        sourceEvidenceType = EvidenceType.HOTSPOT_MUTATION
                    ),
                    TestMolecularMatchDetailsFactory.create(
                        sourceDate = LocalDate.of(2023, 1, 1),
                        sourceEvent = "event 2",
                        sourceEvidenceType = EvidenceType.HOTSPOT_MUTATION
                    ),
                ),
                applicableCancerTypes = setOf(
                    CancerType(matchedCancerType = "matched 1", excludedCancerSubTypes = emptySet()),
                    CancerType(matchedCancerType = "matched 2", excludedCancerSubTypes = setOf("excluded"))
                ),
                url = "https://clinicaltrials.gov/study/NCT00000001"
            )
        )

        assertThat(result).isEqualTo(expectedClinicalEvidence)
    }

    @Test
    fun `Should create multiple external trials for multiple SERVE actionable trial`() {
        val expectedSourceDate = LocalDate.of(2024, 1, 1)
        val event1 = TestServeMolecularFactory.createActionableEvent(sourceDate = expectedSourceDate, sourceEvent = "event 1")
        val event2 = TestServeMolecularFactory.createActionableEvent(sourceDate = expectedSourceDate, sourceEvent = "event 2")

        val indication1 = TestServeFactory.createIndicationWithTypeAndExcludedTypes(type = "type 1")
        val indication2 = TestServeFactory.createIndicationWithTypeAndExcludedTypes(type = "type 2")

        val matchesPerTrial = mapOf(
            TestServeTrialFactory.create(nctId = "NCT00000001", urls = setOf("https://clinicaltrials.gov/study/NCT00000001")) to
                    Pair(
                        setOf(TestServeMolecularFactory.createHotspotCriterium(event1)),
                        setOf(indication1)
                    ),
            TestServeTrialFactory.create(nctId = "NCT00000002", urls = setOf("https://clinicaltrials.gov/study/NCT00000002")) to
                    Pair(
                        setOf(TestServeMolecularFactory.createHotspotCriterium(event2)),
                        setOf(indication2)
                    )
        )

        val result = ClinicalEvidenceFactory.create(
            specificCancerTypeEvidences = emptyList(),
            offTumorEvidences = emptyList(),
            tumorAgnosticEvidence = emptyList(),
            matchingCriteriaAndIndicationsPerEligibleTrial = matchesPerTrial
        )

        val expectedClinicalEvidence = TestClinicalEvidenceFactory.withEligibleTrials(
            setOf(
                TestExternalTrialFactory.create(
                    nctId = "NCT00000001",
                    molecularMatches = setOf(
                        TestMolecularMatchDetailsFactory.create(
                            sourceDate = expectedSourceDate,
                            sourceEvent = "event 1",
                            sourceEvidenceType = EvidenceType.HOTSPOT_MUTATION
                        )
                    ),
                    applicableCancerTypes = setOf(
                        CancerType(matchedCancerType = "type 1", excludedCancerSubTypes = emptySet()),
                    ),
                    url = "https://clinicaltrials.gov/study/NCT00000001"
                ),
                TestExternalTrialFactory.create(
                    nctId = "NCT00000002",
                    molecularMatches = setOf(
                        TestMolecularMatchDetailsFactory.create(
                            sourceDate = expectedSourceDate,
                            sourceEvent = "event 2",
                            sourceEvidenceType = EvidenceType.HOTSPOT_MUTATION
                        )
                    ),
                    applicableCancerTypes = setOf(
                        CancerType(matchedCancerType = "type 2", excludedCancerSubTypes = emptySet())
                    ),
                    url = "https://clinicaltrials.gov/study/NCT00000002"
                )
            )
        )

        assertThat(result).isEqualTo(expectedClinicalEvidence)
    }

    @Test
    fun `Should throw exception on invalid or no URL for external trial`() {
        val invalidUrlTrial = TestServeTrialFactory.create(nctId = "NCT00000001", urls = setOf("this is not a valid url"))

        assertThatIllegalStateException().isThrownBy {
            ClinicalEvidenceFactory.create(
                specificCancerTypeEvidences = emptyList(),
                offTumorEvidences = emptyList(),
                tumorAgnosticEvidence = emptyList(),
                matchingCriteriaAndIndicationsPerEligibleTrial = createTestMatchingCriteriaAndIndicationMap(invalidUrlTrial)
            )
        }

        val emptyUrlTrial = TestServeTrialFactory.create(nctId = "NCT00000001", urls = emptySet())

        assertThatIllegalStateException().isThrownBy {
            ClinicalEvidenceFactory.create(
                specificCancerTypeEvidences = emptyList(),
                offTumorEvidences = emptyList(),
                tumorAgnosticEvidence = emptyList(),
                matchingCriteriaAndIndicationsPerEligibleTrial = createTestMatchingCriteriaAndIndicationMap(emptyUrlTrial)
            )
        }
    }

    private fun createTestMatchingCriteriaAndIndicationMap(trial: ActionableTrial):
            Map<ActionableTrial, Pair<Set<MolecularCriterium>, Set<Indication>>> {
        return mapOf(
            trial to
                    Pair(
                        setOf(TestServeMolecularFactory.createHotspotCriterium()),
                        setOf(TestServeFactory.createEmptyIndication())
                    )
        )
    }*/
}