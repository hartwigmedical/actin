package com.hartwig.actin.molecular.evidence

import com.hartwig.serve.datamodel.ImmutableServeDatabase
import com.hartwig.serve.datamodel.ImmutableServeRecord
import com.hartwig.serve.datamodel.RefGenome
import com.hartwig.serve.datamodel.ServeDatabase
import com.hartwig.serve.datamodel.ServeRecord
import com.hartwig.serve.datamodel.efficacy.EfficacyEvidence
import com.hartwig.serve.datamodel.molecular.ImmutableKnownEvents
import com.hartwig.serve.datamodel.trial.ActionableTrial
import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.Assertions.assertThatIllegalStateException
import org.junit.Test

val SINGLE_PROFILE_1 = TestServeMolecularFactory.createHotspotCriterium()
val SINGLE_PROFILE_2 = TestServeMolecularFactory.createGeneCriterium()
val COMBINED_PROFILE = TestServeMolecularFactory.createCombinedCriterium()

class ServeVerifierTest {

    @Test
    fun `Should pass on single profile evidence and trial`() {
        val evidence = TestServeEvidenceFactory.create(molecularCriterium = SINGLE_PROFILE_1)
        val trial = TestServeTrialFactory.create(anyMolecularCriteria = setOf(SINGLE_PROFILE_1, SINGLE_PROFILE_2))

        val database = toServeDatabase(evidence, trial)
        assertThatCode { ServeVerifier.verifyServeDatabase(database) }.doesNotThrowAnyException()
    }

    @Test
    fun `Should throw exception on trial with at least one combined profile`() {
        val evidence = TestServeEvidenceFactory.create(molecularCriterium = SINGLE_PROFILE_1)
        val trial = TestServeTrialFactory.create(anyMolecularCriteria = setOf(SINGLE_PROFILE_1, COMBINED_PROFILE))

        val database = toServeDatabase(evidence, trial)

        assertThatIllegalStateException().isThrownBy { ServeVerifier.verifyServeDatabase(database) }
    }

    @Test
    fun `Should throw exception on evidence based on a combined profile`() {
        val evidence = TestServeEvidenceFactory.create(molecularCriterium = COMBINED_PROFILE)
        val trial = TestServeTrialFactory.create(anyMolecularCriteria = setOf(SINGLE_PROFILE_1, SINGLE_PROFILE_2))

        val database = toServeDatabase(evidence, trial)

        assertThatIllegalStateException().isThrownBy { ServeVerifier.verifyServeDatabase(database) }
    }

    @Test
    fun `Should throw exception for hotspot with inconsistent genes in efficacy evidence`() {
        val evidence = TestServeEvidenceFactory.create(
            molecularCriterium = TestServeMolecularFactory.createHotspotCriterium(
                variants = setOf(
                    TestServeMolecularFactory.createVariantAnnotation(gene = "gene1"),
                    TestServeMolecularFactory.createVariantAnnotation(gene = "gene2")
                )
            )
        )

        val database = toServeDatabase(evidence, TestServeTrialFactory.create())
        assertThatIllegalStateException().isThrownBy { ServeVerifier.verifyServeDatabase(database) }
    }


    @Test
    fun `Should throw exception for hotspot with inconsistent genes in actionable trial`() {
        val trial = TestServeTrialFactory.create(
            anyMolecularCriteria = setOf(
                TestServeMolecularFactory.createHotspotCriterium(
                    variants = setOf(
                        TestServeMolecularFactory.createVariantAnnotation(gene = "gene1"),
                        TestServeMolecularFactory.createVariantAnnotation(gene = "gene2")
                    )
                )
            )
        )

        val database = toServeDatabase(TestServeEvidenceFactory.create(), trial)
        assertThatIllegalStateException().isThrownBy { ServeVerifier.verifyServeDatabase(database) }
    }

    private fun toServeDatabase(evidence: EfficacyEvidence, trial: ActionableTrial): ServeDatabase {
        return ImmutableServeDatabase.builder()
            .version("test")
            .putRecords(RefGenome.V37, createServeRecord(evidence, trial))
            .putRecords(RefGenome.V38, createServeRecord(evidence, trial))
            .build()
    }

    private fun createServeRecord(evidence: EfficacyEvidence, trial: ActionableTrial): ServeRecord {
        return ImmutableServeRecord.builder()
            .knownEvents(ImmutableKnownEvents.builder().build())
            .addEvidences(evidence)
            .addTrials(trial)
            .build()
    }
}