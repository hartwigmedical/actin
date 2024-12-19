package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.doid.TestDoidModelFactory
import com.hartwig.actin.molecular.evidence.TestServeEvidenceFactory
import com.hartwig.actin.molecular.evidence.TestServeFactory
import com.hartwig.actin.molecular.evidence.TestServeMolecularFactory
import com.hartwig.actin.molecular.evidence.TestServeTrialFactory
import com.hartwig.actin.molecular.evidence.curation.ApplicabilityFiltering
import com.hartwig.serve.datamodel.Knowledgebase
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val MATCH_DOID = "match doid"
private const val APPLICABLE_GENE = "applicable"

private val VALID_EVIDENCE = TestServeEvidenceFactory.create(
    source = ActionabilityConstants.EVIDENCE_SOURCE,
    treatment = "treatment 1",
    molecularCriterium = TestServeMolecularFactory.createGeneCriterium(gene = APPLICABLE_GENE)
)

private val VALID_TRIAL = TestServeTrialFactory.create(
    source = ActionabilityConstants.EVIDENCE_SOURCE,
    nctId = "NCT00000001",
    indications = setOf(TestServeFactory.createIndicationWithDoid(MATCH_DOID)),
    anyMolecularCriteria = setOf(TestServeMolecularFactory.createGeneCriterium(gene = APPLICABLE_GENE))
)

private val MATCHING_DISRUPTION = TestMolecularFactory.minimalDisruption().copy(isReportable = true, gene = APPLICABLE_GENE)

class ClinicalEvidenceMatcherFactoryTest {

    val doidModel = TestDoidModelFactory.createMinimalTestDoidModel()
    val factory = ClinicalEvidenceMatcherFactory(doidModel, setOf(MATCH_DOID))

    @Test
    fun `Should create clinical evidence matcher on empty inputs`() {
        val matcher = factory.create(evidences = emptyList(), trials = emptyList())

        assertThat(matcher).isNotNull()
    }

    @Test
    fun `Should remove evidence on non-evidence source`() {
        val unknownSourceEvidence = TestServeEvidenceFactory.create(
            source = Knowledgebase.UNKNOWN,
            treatment = "treatment 2",
            molecularCriterium = TestServeMolecularFactory.createGeneCriterium(gene = APPLICABLE_GENE)
        )

        val matcher = factory.create(evidences = listOf(VALID_EVIDENCE, unknownSourceEvidence), trials = emptyList())

        val matches = matcher.matchForDisruption(MATCHING_DISRUPTION)
        assertThat(matches.treatmentEvidence).hasSize(1)
    }

    @Test
    fun `Should remove evidence on non-applicable gene`() {
        val nonApplicableEvidence = TestServeEvidenceFactory.create(
            source = ActionabilityConstants.EVIDENCE_SOURCE,
            treatment = "treatment 2",
            molecularCriterium = TestServeMolecularFactory.createGeneCriterium(gene = ApplicabilityFiltering.NON_APPLICABLE_GENES.first())
        )

        val matcher = factory.create(evidences = listOf(VALID_EVIDENCE, nonApplicableEvidence), trials = emptyList())

        val matches = matcher.matchForDisruption(MATCHING_DISRUPTION)
        assertThat(matches.treatmentEvidence).hasSize(1)
    }

    @Test
    fun `Should remove trials on non-trial source`() {
        val unknownSourceTrial = TestServeTrialFactory.create(
            source = Knowledgebase.UNKNOWN,
            nctId = "NCT00000002",
            indications = setOf(TestServeFactory.createIndicationWithDoid(MATCH_DOID)),
            anyMolecularCriteria = setOf(TestServeMolecularFactory.createGeneCriterium(gene = APPLICABLE_GENE))
        )

        val matcher = factory.create(evidences = emptyList(), trials = listOf(VALID_TRIAL, unknownSourceTrial))

        val matches = matcher.matchForDisruption(MATCHING_DISRUPTION)
        assertThat(matches.eligibleTrials).hasSize(1)
    }

    @Test
    fun `Should remove non-applicable molecular criteria from trials`() {
        val applicableAndNonApplicableCriteriaTrial = TestServeTrialFactory.create(
            source = ActionabilityConstants.EXTERNAL_TRIAL_SOURCE,
            nctId = "NCT00000002",
            indications = setOf(TestServeFactory.createIndicationWithDoid(MATCH_DOID)),
            anyMolecularCriteria = setOf(
                TestServeMolecularFactory.createGeneCriterium(gene = APPLICABLE_GENE),
                TestServeMolecularFactory.createGeneCriterium(gene = ApplicabilityFiltering.NON_APPLICABLE_GENES.first())
            )
        )

        val nonApplicableCriteriaTrial = TestServeTrialFactory.create(
            source = ActionabilityConstants.EXTERNAL_TRIAL_SOURCE,
            nctId = "NCT00000002",
            indications = setOf(TestServeFactory.createIndicationWithDoid(MATCH_DOID)),
            anyMolecularCriteria = setOf(
                TestServeMolecularFactory.createGeneCriterium(gene = ApplicabilityFiltering.NON_APPLICABLE_GENES.first())
            )
        )

        val matcher = factory.create(
            evidences = emptyList(),
            trials = listOf(VALID_TRIAL, applicableAndNonApplicableCriteriaTrial, nonApplicableCriteriaTrial)
        )

        val matches = matcher.matchForDisruption(MATCHING_DISRUPTION)
        assertThat(matches.eligibleTrials).hasSize(2)
    }

}