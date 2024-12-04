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

val SINGLE_PROFILE_1 = TestServeMolecularFactory.createHotspot()
val SINGLE_PROFILE_2 = TestServeMolecularFactory.createGene()
val COMBINED_PROFILE = TestServeMolecularFactory.createCombined()

class ServeLoaderTest {

    @Test
    fun `Should pass on single profile evidence and trial`() {
        val evidence = TestServeEvidenceFactory.create(molecularCriterium = SINGLE_PROFILE_1)
        val trial = TestServeTrialFactory.create(molecularCriteria = setOf(SINGLE_PROFILE_1, SINGLE_PROFILE_2))

        val database = toServeDatabase(evidence, trial)
        assertThatCode { ServeVerifier.verifyNoCombinedMolecularProfiles(database) }.doesNotThrowAnyException()
    }

    @Test
    fun `Should throw exception on combined trial`() {
        val evidence = TestServeEvidenceFactory.create(molecularCriterium = SINGLE_PROFILE_1)
        val trial = TestServeTrialFactory.create(molecularCriteria = setOf(SINGLE_PROFILE_1, COMBINED_PROFILE))

        val database = toServeDatabase(evidence, trial)

        assertThatIllegalStateException().isThrownBy { ServeVerifier.verifyNoCombinedMolecularProfiles(database) }
    }

    @Test
    fun `Should throw exception on combined profile evidence`() {
        val evidence = TestServeEvidenceFactory.create(molecularCriterium = COMBINED_PROFILE)
        val trial = TestServeTrialFactory.create(molecularCriteria = setOf(SINGLE_PROFILE_1, SINGLE_PROFILE_2))

        val database = toServeDatabase(evidence, trial)

        assertThatIllegalStateException().isThrownBy { ServeVerifier.verifyNoCombinedMolecularProfiles(database) }
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