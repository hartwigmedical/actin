package com.hartwig.actin.algo.evaluation

import com.hartwig.actin.algo.calendar.ReferenceDateProvider
import com.hartwig.actin.datamodel.trial.EligibilityRule
import com.hartwig.actin.doid.DoidModel
import com.hartwig.actin.medication.AtcTree
import com.hartwig.actin.trial.input.FunctionInputResolver

abstract class RuleMapper(val resources: RuleMappingResources) {

    protected fun referenceDateProvider(): ReferenceDateProvider {
        return resources.referenceDateProvider
    }

    protected fun doidModel(): DoidModel {
        return resources.doidModel
    }

    protected fun functionInputResolver(): FunctionInputResolver {
        return resources.functionInputResolver
    }

    protected fun atcTree(): AtcTree {
        return resources.atcTree
    }

    protected fun maxMolecularTestAge() =
        resources.maxMolecularTestAge

    abstract fun createMappings(): Map<EligibilityRule, FunctionCreator>
}