package com.hartwig.actin.soc.evaluation

import com.hartwig.actin.doid.DoidModel
import com.hartwig.actin.soc.calendar.ReferenceDateProvider
import com.hartwig.actin.treatment.input.FunctionInputResolver

data class RuleMappingResources(
    val referenceDateProvider: ReferenceDateProvider, val doidModel: DoidModel,
    val functionInputResolver: FunctionInputResolver
)