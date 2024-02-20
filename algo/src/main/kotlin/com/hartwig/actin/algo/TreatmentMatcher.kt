package com.hartwig.actin.algo

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.calendar.ReferenceDateProvider
import com.hartwig.actin.algo.datamodel.TreatmentMatch
import com.hartwig.actin.algo.evaluation.RuleMappingResources
import com.hartwig.actin.algo.soc.RecommendationEngine
import com.hartwig.actin.algo.soc.RecommendationEngineFactory
import com.hartwig.actin.trial.datamodel.Trial

class TreatmentMatcher(
    private val trialMatcher: TrialMatcher,
    private val recommendationEngine: RecommendationEngine,
    private val trials: List<Trial>,
    private val referenceDateProvider: ReferenceDateProvider
) {

    fun evaluateMatchesForPatient(patient: PatientRecord): TreatmentMatch {
        val trialMatches = trialMatcher.determineEligibility(patient, trials)
        val standardOfCareMatches = if (!recommendationEngine.standardOfCareCanBeEvaluatedForPatient(patient)) null else {
            recommendationEngine.standardOfCareEvaluatedTreatments(patient)
        }

        return TreatmentMatch(
            patientId = patient.patientId,
            sampleId = patient.molecular?.sampleId ?: "N/A",
            referenceDate = referenceDateProvider.date(),
            referenceDateIsLive = referenceDateProvider.isLive,
            trialMatches = trialMatches,
            standardOfCareMatches = standardOfCareMatches
        )
    }

    companion object {
        fun create(resources: RuleMappingResources, trials: List<Trial>): TreatmentMatcher {
            return TreatmentMatcher(
                TrialMatcher.create(resources), RecommendationEngineFactory(resources).create(), trials, resources.referenceDateProvider
            )
        }
    }
}