package com.hartwig.actin.datamodel.clinical

import java.time.LocalDate

data class PathologyReport(
    val tissueId: String? = null,
    val lab: String,
    val diagnosis: String,
    val tissueDate: LocalDate? = null,
    val authorisationDate: LocalDate? = null,
    val reportDate: LocalDate? = null,
    val report: String
)
