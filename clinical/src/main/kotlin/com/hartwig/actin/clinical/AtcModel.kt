package com.hartwig.actin.clinical

import com.hartwig.actin.clinical.datamodel.AtcClassification
import com.hartwig.actin.clinical.datamodel.ImmutableAtcClassification
import com.hartwig.actin.clinical.datamodel.ImmutableAtcLevel

class AtcModel(private val atcMap: Map<String, String>) {

    fun resolve(rawAtcCode: String): AtcClassification {
        return ImmutableAtcClassification.builder()
            .anatomicalMainGroup(atcLevel(rawAtcCode.substring(0, 1)))
            .therapeuticSubGroup(atcLevel(rawAtcCode.substring(0, 3)))
            .pharmacologicalSubGroup(atcLevel(rawAtcCode.substring(0, 4)))
            .chemicalSubGroup(atcLevel(rawAtcCode.substring(0, 5)))
            .chemicalSubstance(atcLevel(rawAtcCode.substring(0, 7))).build();
    }

    private fun atcLevel(levelCode: String): ImmutableAtcLevel = ImmutableAtcLevel.builder().code(levelCode).name(lookup(levelCode)).build()

    private fun lookup(level: String) = atcMap[level] ?: throw IllegalArgumentException("ATC code [$level] not found in tree")
}