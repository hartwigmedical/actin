package com.hartwig.actin.clinical.curation

import com.hartwig.actin.clinical.curation.config.CurationConfig
import com.hartwig.actin.clinical.curation.config.ValidatedCurationConfig
import com.hartwig.actin.clinical.curation.config.CurationConfigValidationError

typealias InputText = String

class CurationDatabase<T : CurationConfig>(
    val configs: Map<InputText, Set<ValidatedCurationConfig<T>>>
) {
    fun curate(input: InputText) = configs[input] ?: emptySet()
}