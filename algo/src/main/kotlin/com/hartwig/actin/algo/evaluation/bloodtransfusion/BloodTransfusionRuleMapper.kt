package com.hartwig.actin.algo.evaluation.bloodtransfusion

import com.hartwig.actin.algo.evaluation.FunctionCreator
import com.hartwig.actin.algo.evaluation.RuleMapper
import com.hartwig.actin.algo.evaluation.RuleMappingResources
import com.hartwig.actin.medication.AtcTree
import com.hartwig.actin.trial.datamodel.EligibilityFunction
import com.hartwig.actin.trial.datamodel.EligibilityRule

class BloodTransfusionRuleMapper(resources: RuleMappingResources) : RuleMapper(resources) {
    override fun createMappings(): Map<EligibilityRule, FunctionCreator> {
        return mapOf(
            EligibilityRule.REQUIRES_REGULAR_HEMATOPOIETIC_SUPPORT to requiresRegularHematopoieticSupportCreator(atcTree()),
            EligibilityRule.HAS_HAD_ERYTHROCYTE_TRANSFUSION_WITHIN_LAST_X_WEEKS to hasHadRecentBloodTransfusion(TransfusionProduct.ERYTHROCYTE),
            EligibilityRule.HAS_HAD_THROMBOCYTE_TRANSFUSION_WITHIN_LAST_X_WEEKS to hasHadRecentBloodTransfusion(TransfusionProduct.THROMBOCYTE),
        )
    }

    private fun requiresRegularHematopoieticSupportCreator(atcTree: AtcTree): FunctionCreator {
        return FunctionCreator {
            val minDate = referenceDateProvider().date().minusMonths(2)
            val maxDate = referenceDateProvider().date().plusMonths(2)
            RequiresRegularHematopoieticSupport(atcTree, minDate, maxDate)
        }
    }

    private fun hasHadRecentBloodTransfusion(product: TransfusionProduct): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val maxAgeWeeks = functionInputResolver().createOneIntegerInput(function)
            val minDate = referenceDateProvider().date().minusWeeks(maxAgeWeeks.toLong())
            HasHadRecentBloodTransfusion(product, minDate)
        }
    }
}