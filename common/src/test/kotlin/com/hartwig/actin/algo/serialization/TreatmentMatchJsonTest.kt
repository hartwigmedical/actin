package com.hartwig.actin.algo.serialization

import com.google.common.io.Resources
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.datamodel.TestTreatmentMatchFactory
import com.hartwig.actin.algo.datamodel.TreatmentMatch
import com.hartwig.actin.algo.datamodel.TrialMatch
import com.hartwig.actin.algo.serialization.TreatmentMatchJson.fromJson
import com.hartwig.actin.algo.serialization.TreatmentMatchJson.read
import com.hartwig.actin.algo.serialization.TreatmentMatchJson.toJson
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.io.File
import java.io.IOException

class TreatmentMatchJsonTest {
    @Test
    fun canConvertBackAndForthJson() {
        val minimal = TestTreatmentMatchFactory.createMinimalTreatmentMatch()
        val convertedMinimal = fromJson(toJson(minimal))
        assertThat(convertedMinimal).isEqualTo(minimal)
        val proper = TestTreatmentMatchFactory.createProperTreatmentMatch()
        val convertedProper = fromJson(toJson(proper))
        assertThat(convertedProper).isEqualTo(proper)
    }

    @Test
    fun shouldSortMessageSetsBeforeSerialization() {
        val proper = TestTreatmentMatchFactory.createProperTreatmentMatch()
        val trialMatch: TrialMatch = proper.trialMatches[0]
        val key = trialMatch.evaluations.keys.first()
        val match: TreatmentMatch = proper.copy(
            trialMatches = listOf(
                trialMatch.copy(
                    evaluations = mapOf(
                        key to Evaluation(
                            result = EvaluationResult.PASS,
                            recoverable = false,
                            passSpecificMessages = setOf("msg 2", "msg 1", "msg 3"),
                        )
                    ),
                    cohorts = emptyList()
                )
            )
        )
        val expectedJson = ("{\"patientId\":\"ACTN01029999\",\"sampleId\":\"ACTN01029999T\","
                + "\"referenceDate\":{\"year\":2021,\"month\":8,\"day\":2},\"referenceDateIsLive\":true,\"trialMatches\":["
                + "{\"identification\":{\"trialId\":\"Test Trial 1\",\"open\":true,\"acronym\":\"TEST-1\","
                + "\"title\":\"Example test trial 1\"},\"isPotentiallyEligible\":true,\"evaluations\":[["
                + "{\"references\":[{\"id\":\"I-01\",\"text\":\"Patient must be an adult\"}],"
                + "\"function\":{\"rule\":\"IS_AT_LEAST_X_YEARS_OLD\",\"parameters\":[]}},"
                + "{\"result\":\"PASS\",\"recoverable\":false,\"inclusionMolecularEvents\":[],\"exclusionMolecularEvents\":[],"
                + "\"passSpecificMessages\":[\"msg 1\",\"msg 2\",\"msg 3\"],\"passGeneralMessages\":[],"
                + "\"warnSpecificMessages\":[],\"warnGeneralMessages\":[],\"undeterminedSpecificMessages\":[],\"undeterminedGeneralMessages\":[],"
                + "\"failSpecificMessages\":[],\"failGeneralMessages\":[]}]],\"cohorts\":[]}],"
                + "\"standardOfCareMatches\":[{\"treatmentCandidate\":{\"treatment\":{\"name\":\"Vemurafenib\",\"isSystemic\":true,"
                + "\"synonyms\":[],\"displayOverride\":null,\"categories\":[],\"types\":[],\"treatmentClass\":\"OTHER_TREATMENT\"},"
                + "\"optional\":true,\"eligibilityFunctions\":[{\"rule\":\"HAS_KNOWN_ACTIVE_CNS_METASTASES\",\"parameters\":[]}]},"
                + "\"evaluations\":[{\"result\":\"PASS\",\"recoverable\":false,\"inclusionMolecularEvents\":[],"
                + "\"exclusionMolecularEvents\":[],\"passSpecificMessages\":[\"Patient has active CNS metastases\"],"
                + "\"passGeneralMessages\":[\"Active CNS metastases\"],\"warnSpecificMessages\":[],\"warnGeneralMessages\":[],"
                + "\"undeterminedSpecificMessages\":[],\"undeterminedGeneralMessages\":[],\"failSpecificMessages\":[],"
                + "\"failGeneralMessages\":[]}],\"annotations\":null}]}")
        assertThat(toJson(match)).isEqualTo(expectedJson)
    }

    @Test
    @Throws(IOException::class)
    fun canReadTreatmentMatchJson() {
        val match = read(TREATMENT_MATCH_JSON)
        assertThat(match.patientId).isEqualTo("ACTN01029999")
        assertThat(match.trialMatches).hasSize(1)
        val trialMatch = match.trialMatches[0]
        assertThat(trialMatch.evaluations).hasSize(1)
        assertThat(trialMatch.cohorts).hasSize(3)
    }

    companion object {
        private val ALGO_DIRECTORY = Resources.getResource("algo").path
        private val TREATMENT_MATCH_JSON = ALGO_DIRECTORY + File.separator + "patient.treatment_match.json"
    }
}