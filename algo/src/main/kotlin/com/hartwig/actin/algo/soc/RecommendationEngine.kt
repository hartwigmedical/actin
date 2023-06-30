package com.hartwig.actin.algo.soc

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.calendar.ReferenceDateProvider
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFunctionFactory
import com.hartwig.actin.algo.soc.datamodel.EvaluatedTreatment
import com.hartwig.actin.algo.soc.datamodel.TreatmentCandidate
import com.hartwig.actin.clinical.datamodel.treatment.PriorTumorTreatment
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.doid.DoidModel

internal class RecommendationEngine private constructor(doidModel: DoidModel, evaluationFunctionFactory: EvaluationFunctionFactory) {
    private val doidModel: DoidModel
    private val evaluationFunctionFactory: EvaluationFunctionFactory

    init {
        this.doidModel = doidModel
        this.evaluationFunctionFactory = evaluationFunctionFactory
    }

    fun determineAvailableTreatments(
        patientRecord: PatientRecord,
        treatmentCandidates: List<TreatmentCandidate>
    ): List<EvaluatedTreatment> {
        val expandedTumorDoids = patientRecord.clinical().tumor().doids()?.flatMap { doidModel.doidWithParents(it) } ?: emptySet<String>()
        require(DoidConstants.COLORECTAL_CANCER_DOID in expandedTumorDoids) { "No colorectal cancer reported in patient clinical record. SOC recommendation not supported." }
        require((EXCLUDED_TUMOR_DOIDS intersect expandedTumorDoids.toSet()).isEmpty()) { "SOC recommendation only supported for colorectal carcinoma" }

        return treatmentCandidates.asSequence().map { evaluateTreatmentForPatient(it, patientRecord) }
            .filter { treatmentHasNoFailedEvaluations(it) }
            .filter { it.score >= 0 }
            .sortedByDescending { it.score }.toList()
    }

    fun provideRecommendations(patientRecord: PatientRecord, treatmentCandidates: List<TreatmentCandidate>): EvaluatedTreatmentInterpreter {
        return EvaluatedTreatmentInterpreter(determineAvailableTreatments(patientRecord, treatmentCandidates))
    }

    fun patientHasExhaustedStandardOfCare(patientRecord: PatientRecord, treatmentCandidates: List<TreatmentCandidate>): Boolean {
        return determineAvailableTreatments(patientRecord, treatmentCandidates).all { evaluatedTreatment: EvaluatedTreatment ->
            evaluatedTreatment.treatmentCandidate.isOptional
        }
    }

    private fun evaluateTreatmentForPatient(treatmentCandidate: TreatmentCandidate, patientRecord: PatientRecord): EvaluatedTreatment {
        val evaluations: List<Evaluation> = treatmentCandidate.eligibilityFunctions.map { eligibilityFunction ->
            evaluationFunctionFactory.create(eligibilityFunction).evaluate(patientRecord)
        }
        return EvaluatedTreatment(treatmentCandidate, evaluations, treatmentCandidate.expectedBenefitScore)
    }

    private fun determineTreatmentLineForPatient(patientRecord: PatientRecord): Int {
        val priorTumorTreatments: List<PriorTumorTreatment> = patientRecord.clinical().priorTumorTreatments()
        return if (priorTumorTreatments.none {
                it.categories().contains(TreatmentCategory.CHEMOTHERAPY) || it.categories().contains(
                    TreatmentCategory.IMMUNOTHERAPY
                )
            }) 1 else if (priorTumorTreatments.any { it.categories().contains(TreatmentCategory.TARGETED_THERAPY) }) 3 else 2
    }

    companion object {
        private val EXCLUDED_TUMOR_DOIDS = setOf("5777", "169", "1800")

        fun create(doidModel: DoidModel, referenceDateProvider: ReferenceDateProvider): RecommendationEngine {
            return RecommendationEngine(doidModel, EvaluationFunctionFactory.create(doidModel, referenceDateProvider))
        }

        private fun treatmentHasNoFailedEvaluations(evaluatedTreatment: EvaluatedTreatment): Boolean {
            return evaluatedTreatment.evaluations.none { it.result() == EvaluationResult.FAIL }
        }
    }
}