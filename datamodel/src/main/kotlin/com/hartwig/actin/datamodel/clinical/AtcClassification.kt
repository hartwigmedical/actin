package com.hartwig.actin.datamodel.clinical

data class AtcClassification(
    val anatomicalMainGroup: AtcLevel,
    val therapeuticSubGroup: AtcLevel,
    val pharmacologicalSubGroup: AtcLevel,
    val chemicalSubGroup: AtcLevel?,
    val chemicalSubstance: AtcLevel?
) {
    
    fun allLevels(): Set<AtcLevel> {
        return setOfNotNull(anatomicalMainGroup, therapeuticSubGroup, pharmacologicalSubGroup, chemicalSubGroup, chemicalSubstance)
    }
}
