package com.hartwig.actin.algo

import com.hartwig.actin.TreatmentDatabase
import com.hartwig.actin.datamodel.clinical.treatment.Drug
import com.hartwig.actin.datamodel.clinical.treatment.Treatment
import com.hartwig.actin.datamodel.trial.DrugParameter
import com.hartwig.actin.datamodel.trial.EligibilityFunction
import com.hartwig.actin.datamodel.trial.FunctionParameter
import com.hartwig.actin.datamodel.trial.ManyDrugsParameter
import com.hartwig.actin.datamodel.trial.ManyTreatmentsParameter
import com.hartwig.actin.datamodel.trial.Parameter
import com.hartwig.actin.datamodel.trial.TreatmentParameter
import com.hartwig.actin.trial.input.EligibilityRule

private const val COMPOSITE_START = '('
private const val COMPOSITE_END = ')'
private const val PARAM_START = '['
private const val PARAM_END = ']'

class EligibilityFactory(val treatmentDB: TreatmentDatabase) {

    fun generateEligibilityFunction(criterion: String): EligibilityFunction {
        val trimmed = criterion.trim { it <= ' ' }
        val (rule: String, parameters) = when {
            isCompositeCriterion(trimmed) -> {
                Pair(
                    extractCompositeRule(trimmed),
                    extractCompositeInputs(trimmed).map { FunctionParameter(generateEligibilityFunction(it)) })
            }

            isParameterizedCriterion(trimmed) -> {
                val rule = extractParameterizedRule(trimmed)
                Pair(rule, extractParameterizedInputs(EligibilityRule.valueOf(rule), trimmed))
            }

            else -> {
                Pair(trimmed, emptyList())
            }
        }
        return EligibilityFunction(rule = rule, parameters = parameters)
    }

    private fun extractCompositeRule(criterion: String) = criterion.substring(0, criterion.indexOf(COMPOSITE_START))

    private fun extractParameterizedRule(criterion: String): String {
        return criterion.substring(0, criterion.indexOf(PARAM_START)).trim { it <= ' ' }
    }

    private fun extractCompositeInputs(criterion: String): List<String> {
        val params = criterion.substring(criterion.indexOf(COMPOSITE_START) + 1, criterion.lastIndexOf(COMPOSITE_END))
        val relevantCommaPositions = findSeparatingCommaPositions(params)
        if (relevantCommaPositions.isEmpty()) {
            return listOf(params.trim { it <= ' ' })
        }
        val result: MutableList<String> = mutableListOf()
        var index = 0
        while (index < relevantCommaPositions.size) {
            val start = if (index == 0) -1 else relevantCommaPositions[index - 1]
            result.add(params.substring(start + 1, relevantCommaPositions[index]).trim { it <= ' ' })
            index++
        }
        result.add(params.substring(relevantCommaPositions[relevantCommaPositions.size - 1] + 1).trim { it <= ' ' })
        return result
    }

    private fun findSeparatingCommaPositions(params: String): List<Int> {
        var nestedCompositeLevel = 0
        var nestedParameterSection = 0
        val commaPositions: MutableList<Int> = mutableListOf()
        for (i in params.indices) {
            val character = params[i]
            if (character == COMPOSITE_START) {
                nestedCompositeLevel++
            } else if (character == COMPOSITE_END) {
                nestedCompositeLevel--
            } else if (character == PARAM_START) {
                nestedParameterSection++
            } else if (character == PARAM_END) {
                nestedParameterSection--
            } else if (character == ',' && nestedCompositeLevel == 0 && nestedParameterSection == 0) {
                commaPositions.add(i)
            }
        }
        return commaPositions
    }

    private fun extractParameterizedInputs(rule: EligibilityRule, criterion: String): List<Parameter<*>> {

        val expectedParameters = rule.input

        val parameterString = criterion.substring(criterion.indexOf(PARAM_START) + 1, criterion.lastIndexOf(PARAM_END))
        val params = parameterString.split(",").map { it.trim() }.dropLastWhile { it.isEmpty() }
        return params.mapIndexed { index, string ->
            when (val type = expectedParameters[index]) {
                Parameter.Type.TREATMENT -> TreatmentParameter(findTreatment(string))
                Parameter.Type.SYSTEMIC_TREATMENT -> TreatmentParameter(findTreatment(string))
                Parameter.Type.MANY_TREATMENTS -> ManyTreatmentsParameter(string.split(";").map { findTreatment(it) })
                Parameter.Type.DRUG -> DrugParameter(findDrug(string))
                Parameter.Type.MANY_DRUGS -> ManyDrugsParameter(string.split(";").map { findDrug(it) }.toSet())

                else -> type.create(string)
            }
        }
    }

    private fun findTreatment(string: String): Treatment =
        treatmentDB.findTreatmentByName(string) ?: error("$string not found in treatmentDB")

    private fun findDrug(string: String): Drug = treatmentDB.findDrugByName(string) ?: error("$string not found in treatmentDB")

    private fun isCompositeCriterion(criterion: String): Boolean {
        return criterion.contains(COMPOSITE_START.toString()) && criterion.endsWith(COMPOSITE_END.toString())
    }

    private fun isParameterizedCriterion(criterion: String): Boolean {
        return criterion.contains(PARAM_START.toString()) && criterion.endsWith(PARAM_END.toString())
    }
}