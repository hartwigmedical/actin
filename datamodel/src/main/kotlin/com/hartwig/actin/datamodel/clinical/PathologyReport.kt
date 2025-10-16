package com.hartwig.actin.datamodel.clinical

import java.time.LocalDate

data class PathologyReport(
    val tissueId: String? = null,
    val lab: String? = null,
    val diagnosis: String? = null,
    val tissueDate: LocalDate? = null,
    val authorisationDate: LocalDate? = null,
    val reportDate: LocalDate? = null,
    val extractionDate: LocalDate? = null,
    val report: String,
    val reportHash: String? = null
)
