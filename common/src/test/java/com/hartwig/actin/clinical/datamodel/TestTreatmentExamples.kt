package com.hartwig.actin.clinical.datamodel

import com.hartwig.actin.clinical.datamodel.treatment.Drug
import com.hartwig.actin.clinical.datamodel.treatment.DrugTreatment
import java.io.IOException
import java.util.*
import java.util.stream.Collectors

object TestTreatmentExamples {
    @Throws(IOException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        for (entry in TestClinicalFactory.createProperTestClinicalRecord().oncologicalHistory()) {
            System.out.printf("In %s, administered the following treatment(s) with %s intent:\n", entry!!.startYear(), entry.intents())
            for (treatment in entry.treatments()) {
                val categoryString = setToString(treatment.categories())
                System.out.printf(" - %s: %s, %ssystemic", treatment.name(), categoryString, if (treatment.isSystemic) "" else "non")
                if (treatment is DrugTreatment && !treatment.drugs().isEmpty()) {
                    System.out.printf(", with drugs %s",
                        treatment.drugs()
                            .stream()
                            .map { drug: Drug -> String.format("%s (%s)", drug.name(), setToString(drug.drugTypes())) }
                            .collect(Collectors.joining(", ")))
                }
                println()
            }
        }
    }

    private fun <T> setToString(categories: Set<T>): String {
        return categories.stream().map { o: T -> Objects.toString(o) }.collect(Collectors.joining("/"))
    }
}
