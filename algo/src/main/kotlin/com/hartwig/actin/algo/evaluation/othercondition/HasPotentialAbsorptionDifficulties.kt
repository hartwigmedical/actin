package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format.concat
import com.hartwig.actin.algo.evaluation.util.ValueComparison.stringCaseInsensitivelyMatchesQueryCollection
import com.hartwig.actin.algo.othercondition.OtherConditionSelector
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.Complication
import com.hartwig.actin.datamodel.clinical.ToxicitySource
import com.hartwig.actin.doid.DoidModel

class HasPotentialAbsorptionDifficulties(private val doidModel: DoidModel) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val conditions = OtherConditionSelector.selectClinicallyRelevant(record.priorOtherConditions).flatMap { it.doids }
            .filter { doidModel.doidWithParents(it).any { doid -> doid in DoidConstants.ABSORPTION_DIFFICULTIES_DOID_SET } }
            .map { doidModel.resolveTermForDoid(it) }

        if (conditions.isNotEmpty()) {
            return EvaluationFactory.pass("Potential absorption difficulties: " + concat(conditions.filterNotNull()))
        }
        val complications = record.complications?.filter { isOfCategory(it, GASTROINTESTINAL_DISORDER_CATEGORY) }
            ?.map { it.name } ?: emptyList()

        if (complications.isNotEmpty()) {
            return EvaluationFactory.pass("Potential absorption difficulties: " + concat(complications))
        }
        val toxicities = record.toxicities
            .filter { it.source == ToxicitySource.QUESTIONNAIRE || (it.grade ?: 0) >= 2 }
            .map { it.name }
            .filter { stringCaseInsensitivelyMatchesQueryCollection(it, TOXICITIES_CAUSING_ABSORPTION_DIFFICULTY) }

        return if (toxicities.isNotEmpty()) {
            EvaluationFactory.pass("Potential absorption difficulties: " + concat(toxicities))
        } else
            EvaluationFactory.fail("No potential reasons for absorption problems identified")
    }

    companion object {
        const val GASTROINTESTINAL_DISORDER_CATEGORY: String = "gastrointestinal disorder"
        val TOXICITIES_CAUSING_ABSORPTION_DIFFICULTY = setOf("diarrhea", "nausea", "vomit")

        private fun isOfCategory(complication: Complication, categoryToFind: String): Boolean {
            return complication.categories.any { it.lowercase().contains(categoryToFind.lowercase()) }
        }
    }
}