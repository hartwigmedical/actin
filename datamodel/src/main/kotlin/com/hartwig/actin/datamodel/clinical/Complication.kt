package com.hartwig.actin.datamodel.clinical

data class Complication(
    override val name: String,
    override val year: Int? = null,
    override val month: Int? = null,
    override val icdCodes: Set<IcdCode>,
): Comorbidity