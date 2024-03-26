package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory

data class IntoleranceConfig(
    override val input: String, override val ignore: Boolean = false, val name: String, val doids: Set<String>, val treatmentCategories: Set<TreatmentCategory>?
) : CurationConfig