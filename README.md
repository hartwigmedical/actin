# ACTIN

(in development)

ACTIN is a system that determines and ranks treatment options for cancer patients based on the following inputs:
 - Comprehensive clinical record of the patient
 - Comprehensive molecular analysis of the tumor
 - Data from all patients previously evaluated and collected by ACTIN
 
More details on the following sub-modules are available from the links below:
 - [Clinical](actin-clinical): A datamodel along with loading procedures describing the clinical data collected for each patient.
 - [Treatment](actin-treatment): An algo to generate a database of all standard-of-care and experimental treatments 
 available to patients at any point in time.
 - [Algo](actin-algo): The algo matching all data from a patient to all available treatment options and generating a report.
  