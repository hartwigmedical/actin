package com.hartwig.actin.database.dao

import org.jooq.InsertValuesStep3
import org.jooq.Record

class EvidenceInserter<T : Record?>(private val inserter: InsertValuesStep3<T, Int?, String?, String?>) {

    fun write(topicId: Int, treatment: String, type: String) {
        inserter.values(topicId, treatment, type)
    }

    fun execute() {
        inserter.execute()
    }
}