package com.hartwig.actin.report.pdf.tables.trial

import com.hartwig.actin.datamodel.algo.TrialMatch
import com.hartwig.actin.datamodel.molecular.evidence.CountryName
import com.hartwig.actin.datamodel.molecular.evidence.TestClinicalEvidenceFactory
import com.hartwig.actin.datamodel.trial.TrialIdentification

private const val TMB_TARGET = "TMB"
private const val EGFR_TARGET = "EGFR"
private val TRIAL_1 = TestClinicalEvidenceFactory.createExternalTrial(
    "1", setOf(TestClinicalEvidenceFactory.createCountry(CountryName.NETHERLANDS)), "url", "NCT001"
)
private val TRIAL_2 = TestClinicalEvidenceFactory.createExternalTrial(
    "2", setOf(TestClinicalEvidenceFactory.createCountry(CountryName.BELGIUM)), "url", "NCT002"
)
private val TRIAL_3 = TestClinicalEvidenceFactory.createExternalTrial(
    "3", setOf(TestClinicalEvidenceFactory.createCountry(CountryName.NETHERLANDS)), "url", "NCT003"
)

private val trialMatches = listOf(
    TrialMatch(
        identification = TrialIdentification("TRIAL-1", true, "TR-1", "Different title of same trial 1", "NCT00000001"),
        isPotentiallyEligible = true,
        evaluations = emptyMap(),
        cohorts = emptyList(),
        nonEvaluableCohorts = emptyList()
    ),
    TrialMatch(
        identification = TrialIdentification("TRIAL-2", true, "TR-2", "Different trial 2", "NCT00000003"),
        isPotentiallyEligible = true,
        evaluations = emptyMap(),
        cohorts = emptyList(),
        nonEvaluableCohorts = emptyList()
    )
)

class ExternalTrialSummarizerTest {
    
}