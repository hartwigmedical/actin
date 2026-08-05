package com.hartwig.actin.algo.evaluation

import com.hartwig.actin.algo.calendar.ReferenceDateProvider
import com.hartwig.actin.doid.CuppaToDoidMapping
import com.hartwig.actin.trial.input.EligibilityRule
import com.hartwig.actin.doid.DoidModel
import com.hartwig.actin.icd.IcdModel
import com.hartwig.actin.medication.AtcTree

abstract class RuleMapper(val resources: RuleMappingResources) {

    protected val referenceDateProvider: ReferenceDateProvider = resources.referenceDateProvider

    protected val doidModel: DoidModel = resources.doidModel

    protected val cuppaToDoidMapping: CuppaToDoidMapping = resources.cuppaToDoidMapping

    protected val icdModel: IcdModel = resources.icdModel

    protected val atcTree: AtcTree = resources.atcTree

    protected val evaluationLabels: EvaluationLabels = resources.evaluationLabels

    abstract fun createMappings(): Map<EligibilityRule, FunctionCreator>
}