package com.hartwig.actin.clinical.sort

import com.hartwig.actin.datamodel.clinical.Medication

class MedicationByNameComparator : Comparator<Medication> {

    override fun compare(medication1: Medication, medication2: Medication): Int {
        return medication1.name.compareTo(medication2.name)
    }
}
