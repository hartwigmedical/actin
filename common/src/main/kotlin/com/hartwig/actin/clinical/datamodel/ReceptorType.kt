package com.hartwig.actin.clinical.datamodel

import com.hartwig.actin.Displayable

enum class ReceptorType : Displayable {
    ER,
    PR,
    HER2;

    override fun display(): String {
        return this.toString()
    }
}