package com.hartwig.actin.clinical

import org.apache.logging.log4j.LogManager

class PatientId {

    companion object {
        private val LOGGER = LogManager.getLogger(PatientId::class.java)
        fun from(subject: String): String {
            var adjusted = subject
            // Subjects have been passed with unexpected subject IDs in the past (e.g. without ACTN prefix)
            if (subject.length == 10 && !subject.startsWith("ACTN")) {
                LOGGER.warn("Suspicious subject detected. Pre-fixing with 'ACTN': {}", subject)
                adjusted = "ACTN$subject"
            }
            return adjusted.replace("-".toRegex(), "")
        }
    }
}