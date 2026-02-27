package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.molecular.evidence.TestServeMolecularFactory
import com.hartwig.actin.molecular.evidence.TestServeTrialFactory
import com.hartwig.actin.molecular.evidence.curation.ApplicabilityFiltering
import com.hartwig.serve.datamodel.ImmutableServeRecord
import com.hartwig.serve.datamodel.ServeRecord
import com.hartwig.serve.datamodel.efficacy.EfficacyEvidence
import com.hartwig.serve.datamodel.molecular.ImmutableKnownEvents
import com.hartwig.serve.datamodel.trial.ActionableTrial
import org.junit.Test

class ActionabilityMatcherFactoryTest {

    @Test
    fun `Should filter hotspots`() {
        val applicableMolecularCriterium = TestServeMolecularFactory.createGeneCriterium(gene = "GENE1")
        val nonApplicableMolecularCriterium =
            TestServeMolecularFactory.createGeneCriterium(gene = ApplicabilityFiltering.NON_APPLICABLE_GENES.first())

        val trial =
            TestServeTrialFactory.create(anyMolecularCriteria = setOf(applicableMolecularCriterium, nonApplicableMolecularCriterium))
        val serveRecord = serveRecord(evidences = emptyList(), trials = listOf(trial))
        
        val matcher = ActionabilityMatcherFactory.create(serveRecord)
        println("Matcher: $matcher")
    }

    private fun serveRecord(evidences: List<EfficacyEvidence>, trials: List<ActionableTrial> = emptyList()): ServeRecord {
        return ImmutableServeRecord.builder().knownEvents(ImmutableKnownEvents.builder().build()).addAllEvidences(evidences)
            .addAllTrials(trials).build()
    }
}