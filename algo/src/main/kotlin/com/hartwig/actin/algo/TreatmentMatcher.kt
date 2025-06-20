package com.hartwig.actin.algo

import com.hartwig.actin.algo.calendar.ReferenceDateProvider
import com.hartwig.actin.algo.evaluation.RuleMappingResources
import com.hartwig.actin.algo.soc.EvaluatedTreatmentAnnotator
import com.hartwig.actin.algo.soc.PersonalizedDataInterpreter
import com.hartwig.actin.algo.soc.ResistanceEvidenceMatcher
import com.hartwig.actin.algo.soc.StandardOfCareEvaluator
import com.hartwig.actin.algo.soc.StandardOfCareEvaluatorFactory
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.TreatmentMatch
import com.hartwig.actin.datamodel.efficacy.EfficacyEntry
import com.hartwig.actin.datamodel.trial.Trial
import com.hartwig.actin.personalization.serialization.TreatmentEfficacyPredictionJson
import java.time.LocalDate

class TreatmentMatcher(
    private val trialMatcher: TrialMatcher,
    private val standardOfCareEvaluator: StandardOfCareEvaluator,
    private val trials: List<Trial>,
    private val referenceDateProvider: ReferenceDateProvider,
    private val evaluatedTreatmentAnnotator: EvaluatedTreatmentAnnotator,
    private val personalizationDataPath: String? = null,
    private val treatmentEfficacyPredictionPath: String? = null,
    private val maxMolecularTestAge: LocalDate?
) {

    fun run(patient: PatientRecord): TreatmentMatch {
        val trialMatches = trialMatcher.determineEligibility(patient, trials)

        val (standardOfCareMatches, personalizedDataAnalysis, survivalPredictionsPerTreatment) =
            if (standardOfCareEvaluator.standardOfCareCanBeEvaluatedForPatient(patient)) {
                val evaluatedTreatments = standardOfCareEvaluator.standardOfCareEvaluatedTreatments(patient).evaluatedTreatments
                val personalizedDataAnalysis = personalizationDataPath?.let { PersonalizedDataInterpreter.create(it).interpret(patient) }
                val survivalPredictionsPerTreatment =
                    treatmentEfficacyPredictionPath?.let { TreatmentEfficacyPredictionJson.read(treatmentEfficacyPredictionPath) }
                Triple(
                    evaluatedTreatmentAnnotator.annotate(evaluatedTreatments, personalizedDataAnalysis?.treatmentAnalyses),
                    personalizedDataAnalysis,
                    survivalPredictionsPerTreatment
                )
            } else {
                Triple(null, null, null)
            }

        return TreatmentMatch(
            patientId = patient.patientId,
            sampleId = patient.molecularHistory.latestOrangeMolecularRecord()?.sampleId ?: "N/A",
            referenceDate = referenceDateProvider.date(),
            referenceDateIsLive = referenceDateProvider.isLive,
            trialMatches = trialMatches,
            standardOfCareMatches = standardOfCareMatches,
            personalizedDataAnalysis = personalizedDataAnalysis,
            survivalPredictionsPerTreatment = survivalPredictionsPerTreatment,
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
                personalizationDataPath = resources.personalizationDataPath,
                treatmentEfficacyPredictionPath = resources.treatmentEfficacyPredictionJson,
                maxMolecularTestAge = maxMolecularTestAge
            )
        }
    }
}