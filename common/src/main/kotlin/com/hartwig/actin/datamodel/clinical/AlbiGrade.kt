package com.hartwig.actin.datamodel.clinical

import com.hartwig.actin.datamodel.Displayable

enum class AlbiGrade : Displayable {
    GRADE_1,
    GRADE_2,
    GRADE_3;

    override fun display(): String {
        return this.toString().replace("_", " ").lowercase()
    }
}