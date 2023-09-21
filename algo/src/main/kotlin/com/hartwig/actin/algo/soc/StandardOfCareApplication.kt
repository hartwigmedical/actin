package com.hartwig.actin.algo.soc

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.PatientRecordFactory
import com.hartwig.actin.TreatmentDatabaseFactory
import com.hartwig.actin.algo.calendar.ReferenceDateProviderFactory
import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.clinical.datamodel.ClinicalRecord
import com.hartwig.actin.clinical.serialization.ClinicalRecordJson
import com.hartwig.actin.clinical.util.ClinicalPrinter
import com.hartwig.actin.doid.DoidModel
import com.hartwig.actin.doid.DoidModelFactory
import com.hartwig.actin.doid.datamodel.DoidEntry
import com.hartwig.actin.doid.serialization.DoidJson
import com.hartwig.actin.molecular.datamodel.MolecularRecord
import com.hartwig.actin.molecular.serialization.MolecularRecordJson
import com.hartwig.actin.molecular.util.MolecularPrinter
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.IOException
import kotlin.system.exitProcess

class StandardOfCareApplication(config: StandardOfCareConfig) {
    private val config: StandardOfCareConfig

    init {
        this.config = config
    }

    fun run() {
        LOGGER.info("Running {} v{}", APPLICATION, VERSION)

        LOGGER.info("Loading clinical record from {}", config.clinicalJson)
        val clinical: ClinicalRecord = ClinicalRecordJson.read(config.clinicalJson)
        ClinicalPrinter.printRecord(clinical)

        LOGGER.info("Loading molecular record from {}", config.molecularJson)
        val molecular: MolecularRecord = MolecularRecordJson.read(config.molecularJson)
        MolecularPrinter.printRecord(molecular)

        val patient: PatientRecord = PatientRecordFactory.fromInputs(clinical, molecular)

        LOGGER.info("Loading DOID tree from {}", config.doidJson)
        val doidEntry: DoidEntry = DoidJson.readDoidOwlEntry(config.doidJson)
        LOGGER.info(" Loaded {} nodes", doidEntry.nodes().size)
        val doidModel: DoidModel = DoidModelFactory.createFromDoidEntry(doidEntry)

        LOGGER.info("Loading treatment data from {}", config.treatmentDirectory)
        val recommendationDatabase = RecommendationDatabase(TreatmentDatabaseFactory.createFromPath(config.treatmentDirectory))
        LOGGER.info("Loaded recommendation database for colorectal cancer with treatment candidates:")
        recommendationDatabase.logRulesForDoidSet(setOf(DoidConstants.COLORECTAL_CANCER_DOID))

        val referenceDateProvider = ReferenceDateProviderFactory.create(clinical, config.runHistorically)
        val recommendationEngine = RecommendationEngine.create(doidModel, recommendationDatabase, referenceDateProvider)

        val recommendations = recommendationEngine.provideRecommendations(patient)
        LOGGER.info(recommendations.summarize())
    }

    companion object {
        val LOGGER: Logger = LogManager.getLogger(StandardOfCareApplication::class.java)
        const val APPLICATION = "ACTIN Standard of Care"
        private val VERSION = StandardOfCareApplication::class.java.getPackage().implementationVersion
    }
}

@Throws(IOException::class)
fun main(args: Array<String>) {
    val options: Options = StandardOfCareConfig.createOptions()
    
    try {
        val config = StandardOfCareConfig.createConfig(DefaultParser().parse(options, args))
        StandardOfCareApplication(config).run()
    } catch (exception: ParseException) {
        StandardOfCareApplication.LOGGER.warn(exception)
        HelpFormatter().printHelp(StandardOfCareApplication.APPLICATION, options)
        exitProcess(1)
    }
}
