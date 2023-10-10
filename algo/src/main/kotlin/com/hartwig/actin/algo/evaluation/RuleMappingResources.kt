package com.hartwig.actin.algo.evaluation

import com.hartwig.actin.algo.calendar.ReferenceDateProvider
import com.hartwig.actin.algo.evaluation.medication.AtcTree
import com.hartwig.actin.doid.DoidModel
import com.hartwig.actin.treatment.input.FunctionInputResolver

data class RuleMappingResources(
    val referenceDateProvider: ReferenceDateProvider, val doidModel: DoidModel,
    val functionInputResolver: FunctionInputResolver, val atcTree: AtcTree
)