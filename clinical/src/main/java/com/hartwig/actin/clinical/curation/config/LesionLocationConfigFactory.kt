package com.hartwig.actin.clinical.curation.config

import com.google.common.annotations.VisibleForTesting
import com.hartwig.actin.clinical.curation.datamodel.LesionLocationCategory
import java.util.*

class LesionLocationConfigFactory : CurationConfigFactory<LesionLocationConfig> {
    override fun create(fields: Map<String?, Int?>, parts: Array<String>): LesionLocationConfig {
        return ImmutableLesionLocationConfig.builder()
            .input(parts[fields["input"]!!])
            .location(parts[fields["location"]!!])
            .category(toCategory(parts[fields["category"]!!]))
            .build()
    }

    companion object {
        @JvmStatic
        @VisibleForTesting
        fun toCategory(category: String): LesionLocationCategory? {
            return if (category.isEmpty()) {
                null
            } else LesionLocationCategory.valueOf(
                category.replace(" ".toRegex(), "_").uppercase(Locale.getDefault())
            )
        }
    }
}