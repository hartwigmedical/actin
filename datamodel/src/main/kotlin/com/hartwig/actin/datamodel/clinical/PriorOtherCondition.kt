package com.hartwig.actin.datamodel.clinical

data class PriorOtherCondition(
    override val name: String,
    override val year: Int? = null,
    override val month: Int? = null,
    override val icdCodes: Set<IcdCode>,
    val isContraindicationForTherapy: Boolean
): Comorbidity