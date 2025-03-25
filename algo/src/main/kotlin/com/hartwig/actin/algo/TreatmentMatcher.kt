package com.hartwig.actin.algo

import com.hartwig.actin.algo.calendar.ReferenceDateProvider
import com.hartwig.actin.algo.evaluation.RuleMappingResources
import com.hartwig.actin.algo.evidence.TreatmentRanker
import com.hartwig.actin.algo.soc.EvaluatedTreatmentAnnotator
import com.hartwig.actin.algo.soc.PersonalizedDataInterpreter
import com.hartwig.actin.algo.soc.ResistanceEvidenceMatcher
import com.hartwig.actin.algo.soc.StandardOfCareEvaluator
import com.hartwig.actin.algo.soc.StandardOfCareEvaluatorFactory
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.RankedTreatment
import com.hartwig.actin.datamodel.algo.TreatmentEvidenceRanking
import com.hartwig.actin.datamodel.algo.TreatmentMatch
import com.hartwig.actin.datamodel.efficacy.EfficacyEntry
import com.hartwig.actin.datamodel.trial.Trial
import java.time.LocalDate

class TreatmentMatcher(
    private val trialMatcher: TrialMatcher,
    private val standardOfCareEvaluator: StandardOfCareEvaluator,
    private val trials: List<Trial>,
    private val referenceDateProvider: ReferenceDateProvider,
    private val evaluatedTreatmentAnnotator: EvaluatedTreatmentAnnotator,
    private val personalizationDataPath: String? = null,
    private val maxMolecularTestAge: LocalDate?,
    private val treatmentRanker: TreatmentRanker
) {

    fun evaluateAndAnnotateMatchesForPatient(patient: PatientRecord): TreatmentMatch {
        val trialMatches = trialMatcher.determineEligibility(patient, trials)

        val (standardOfCareMatches, personalizedDataAnalysis) = if (standardOfCareEvaluator.standardOfCareCanBeEvaluatedForPatient(patient)) {
            val evaluatedTreatments = standardOfCareEvaluator.standardOfCareEvaluatedTreatments(patient).evaluatedTreatments
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
            personalizedDataAnalysis = personalizedDataAnalysis,
            maxMolecularTestAge = maxMolecularTestAge,
            treatmentEvidenceRanking = TreatmentEvidenceRanking(
                treatmentRanker.rank(patient).map { RankedTreatment(it.treatment, it.scores.map { s -> s.event }, it.score) }
            )
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
                TrialMatcher.create(resources),
                StandardOfCareEvaluatorFactory(resources).create(),
                trials,
                resources.referenceDateProvider,
                EvaluatedTreatmentAnnotator.create(efficacyEvidence, resistanceEvidenceMatcher),
                resources.personalizationDataPath,
                maxMolecularTestAge,
                TreatmentRanker()
            )
        }
    }
}