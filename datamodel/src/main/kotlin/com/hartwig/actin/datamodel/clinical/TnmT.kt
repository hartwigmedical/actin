package com.hartwig.actin.datamodel.clinical

import com.hartwig.actin.datamodel.Displayable

enum class TnmT : Displayable {
    T0,
    T1,
    T1A,
    T1B,
    T1C,
    T2,
    T2A,
    T2B,
    T3,
    T4;

    override fun display(): String {
        return this.toString()
    }
}