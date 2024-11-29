package com.hartwig.actin.datamodel.trial

data class TrialLocation(
    val id: Int,
    val name: String
) {
    companion object {
        fun fromString(input: String?): List<TrialLocation> {
            try {
                return input.takeIf { !it.isNullOrEmpty() }?.let {
                    it.split(":")
                        .map { loc -> loc.split(",") }
                        .map { (id, name) ->
                            require(id.isNotEmpty())
                            require(name.isNotEmpty())
                            TrialLocation(id.toInt(), name)
                        }
                } ?: emptyList()
            } catch (e: Exception) {
                throw IllegalArgumentException("Cannot parse locations from input $input", e)
            }
        }
    }
}

