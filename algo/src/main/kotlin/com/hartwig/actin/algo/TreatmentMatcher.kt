package com.hartwig.actin.algo

import com.hartwig.actin.algo.calendar.ReferenceDateProvider
import com.hartwig.actin.algo.evaluation.RuleMappingResources
import com.hartwig.actin.algo.soc.EvaluatedTreatmentAnnotator
import com.hartwig.actin.algo.soc.ResistanceEvidenceMatcher
import com.hartwig.actin.algo.soc.StandardOfCareEvaluator
import com.hartwig.actin.algo.soc.StandardOfCareEvaluatorFactory
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.TreatmentMatch
import com.hartwig.actin.datamodel.efficacy.EfficacyEntry
import com.hartwig.actin.datamodel.trial.Trial
import com.hartwig.actin.personalization.serialization.PersonalizedTreatmentSummaryJson
import java.time.LocalDate

class TreatmentMatcher(
    private val trialMatcher: TrialMatcher,
    private val standardOfCareEvaluator: StandardOfCareEvaluator,
    private val trials: List<Trial>,
    private val referenceDateProvider: ReferenceDateProvider,
    private val evaluatedTreatmentAnnotator: EvaluatedTreatmentAnnotator,
    private val treatmentEfficacyPredictionPath: String? = null,
    private val maxMolecularTestAge: LocalDate?
) {

    fun run(patient: PatientRecord): TreatmentMatch {
        val trialMatches = trialMatcher.determineEligibility(patient, trials)

        val (standardOfCareMatches, personalizedTreatmentSummary) =
            if (standardOfCareEvaluator.standardOfCareCanBeEvaluatedForPatient(patient)) {
                val evaluatedTreatments = standardOfCareEvaluator.standardOfCareEvaluatedTreatments(patient).evaluatedTreatments
                val personalizedTreatmentSummary = treatmentEfficacyPredictionPath?.let(PersonalizedTreatmentSummaryJson::read)
                Pair(
                    evaluatedTreatmentAnnotator.annotate(evaluatedTreatments),
                    personalizedTreatmentSummary
                )
            } else {
                Pair(null, null)
            }

        return TreatmentMatch(
            patientId = patient.patientId,
            referenceDate = referenceDateProvider.date(),
            referenceDateIsLive = referenceDateProvider.isLive,
            trialMatches = trialMatches,
            standardOfCareMatches = standardOfCareMatches,
            personalizedTreatmentSummary = personalizedTreatmentSummary,
            maxMolecularTestAge = maxMolecularTestAge
        )
    }

    companion object {
        fun create(
            resources: RuleMappingResources,
            trials: List<Trial>,
            efficacyEvidence: List<EfficacyEntry>,
            resistanceEvidenceMatcher: ResistanceEvidenceMatcher,
            maxMolecularTestAge: LocalDate?
        ): TreatmentMatcher {
            return TreatmentMatcher(
                trialMatcher = TrialMatcher.create(resources),
                standardOfCareEvaluator = StandardOfCareEvaluatorFactory(resources).create(),
                trials = trials,
                referenceDateProvider = resources.referenceDateProvider,
                evaluatedTreatmentAnnotator = EvaluatedTreatmentAnnotator.create(efficacyEvidence, resistanceEvidenceMatcher),
                treatmentEfficacyPredictionPath = resources.treatmentEfficacyPredictionJson,
                maxMolecularTestAge = maxMolecularTestAge
            )
        }
    }
}