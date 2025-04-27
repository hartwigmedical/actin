package com.hartwig.actin.datamodel.clinical

import java.time.LocalDate

data class PathologyReport(
    val tissueId: String? = null,
    val reportRequested: Boolean,
    val source: String,
    val lab: String? = null,
    val diagnosis: String,
    val tissueDate: LocalDate,
    val authorisationDate: LocalDate,
    val report: String
)
