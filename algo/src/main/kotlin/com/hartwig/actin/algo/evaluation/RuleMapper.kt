package com.hartwig.actin.algo.evaluation

import com.hartwig.actin.algo.calendar.ReferenceDateProvider
import com.hartwig.actin.algo.evaluation.tumor.CuppaToDoidMapping
import com.hartwig.actin.trial.input.EligibilityRule
import com.hartwig.actin.doid.DoidModel
import com.hartwig.actin.icd.IcdModel
import com.hartwig.actin.medication.AtcTree

abstract class RuleMapper(val resources: RuleMappingResources) {

    protected fun referenceDateProvider(): ReferenceDateProvider {
        return resources.referenceDateProvider
    }

    protected fun doidModel(): DoidModel {
        return resources.doidModel
    }

    protected fun cuppaToDoidMapping(): CuppaToDoidMapping? {
        return resources.cuppaToDoidMapping
    }

    protected fun icdModel(): IcdModel {
        return resources.icdModel
    }

    protected fun atcTree(): AtcTree {
        return resources.atcTree
    }

    abstract fun createMappings(): Map<EligibilityRule, FunctionCreator>
}