package com.hartwig.actin.algo.evaluation

import com.hartwig.actin.algo.calendar.ReferenceDateProvider
import com.hartwig.actin.doid.CuppaToDoidMapping
import com.hartwig.actin.trial.input.EligibilityRule
import com.hartwig.actin.doid.DoidModel
import com.hartwig.actin.icd.IcdModel
import com.hartwig.actin.medication.AtcTree

abstract class RuleMapper(val resources: RuleMappingResources) {

    protected fun referenceDateProvider(): ReferenceDateProvider = resources.referenceDateProvider

    protected fun doidModel(): DoidModel = resources.doidModel

    protected fun cuppaToDoidMapping(): CuppaToDoidMapping = resources.cuppaToDoidMapping

    protected fun icdModel(): IcdModel = resources.icdModel

    protected fun atcTree(): AtcTree = resources.atcTree

    abstract fun createMappings(): Map<EligibilityRule, FunctionCreator>
}