package com.hartwig.actin.algo

import com.hartwig.actin.algo.calendar.ReferenceDateProvider
import com.hartwig.actin.algo.evaluation.RuleMappingResources
import com.hartwig.actin.algo.soc.EvaluatedTreatmentAnnotator
import com.hartwig.actin.algo.soc.PersonalizedDataInterpreter
import com.hartwig.actin.algo.soc.RecommendationEngine
import com.hartwig.actin.algo.soc.RecommendationEngineFactory
import com.hartwig.actin.algo.soc.ResistanceEvidenceMatcher
import com.hartwig.actin.datamodel.PatientRecord

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.datamodel.algo.EvaluatedTreatment
import com.hartwig.actin.datamodel.algo.TreatmentMatch
import com.hartwig.actin.datamodel.algo.TreatmentCandidate

import com.hartwig.actin.datamodel.efficacy.EfficacyEntry
import com.hartwig.actin.datamodel.trial.Trial

import com.hartwig.actin.datamodel.clinical.treatment.OtherTreatment


class TreatmentMatcher(
    private val trialMatcher: TrialMatcher,
    private val recommendationEngine: RecommendationEngine,
    private val trials: List<Trial>,
    private val referenceDateProvider: ReferenceDateProvider,
    private val evaluatedTreatmentAnnotator: EvaluatedTreatmentAnnotator,
    private val trialSource: String?,
    private val personalizationDataPath: String? = null
) {

    fun evaluateAndAnnotateMatchesForPatient(patient: PatientRecord): TreatmentMatch {
        val trialMatches = trialMatcher.determineEligibility(patient, trials)

        val (standardOfCareMatches, personalizedDataAnalysis) = if (recommendationEngine.standardOfCareCanBeEvaluatedForPatient(patient)) {
            val evaluatedTreatments = recommendationEngine.standardOfCareEvaluatedTreatments(patient) + createNoneEvaluatedTreatment()
            val personalizedDataAnalysis = personalizationDataPath?.let { PersonalizedDataInterpreter.create(it).interpret(patient) }
            Pair(
                evaluatedTreatmentAnnotator.annotate(evaluatedTreatments, personalizedDataAnalysis?.treatmentAnalyses),
                personalizedDataAnalysis
            )
        } else {
            Pair(null, null)
        }

        return TreatmentMatch(
            patientId = patient.patientId,
            sampleId = patient.molecularHistory.latestOrangeMolecularRecord()?.sampleId ?: "N/A",
            referenceDate = referenceDateProvider.date(),
            referenceDateIsLive = referenceDateProvider.isLive,
            trialMatches = trialMatches,
            standardOfCareMatches = standardOfCareMatches,
            trialSource = trialSource,
            personalizedDataAnalysis = personalizedDataAnalysis
        )
    }

    private fun createNoneEvaluatedTreatment(): EvaluatedTreatment {
        val treatmentCandidate = TreatmentCandidate(
            treatment = OtherTreatment.NONE,
            optional = true,
            eligibilityFunctions = emptySet()
        )

        return EvaluatedTreatment(
            treatmentCandidate = treatmentCandidate,
            evaluations = listOf( EvaluationFactory.pass("No suitable treatments matched."))
        )
    }

    companion object {
        fun create(
            resources: RuleMappingResources,
            trials: List<Trial>,
            efficacyEvidence: List<EfficacyEntry>,
            resistanceEvidenceMatcher: ResistanceEvidenceMatcher
        ): TreatmentMatcher {
            return TreatmentMatcher(
                TrialMatcher.create(resources),
                RecommendationEngineFactory(resources).create(),
                trials,
                resources.referenceDateProvider,
                EvaluatedTreatmentAnnotator.create(efficacyEvidence, resistanceEvidenceMatcher),
                resources.algoConfiguration.trialSource,
                resources.personalizationDataPath
            )
        }
    }
}