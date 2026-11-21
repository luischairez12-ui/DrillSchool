package com.forgecrew.drillschool

/**
 * Published *maximum machine ratings*, NOT live gauges, target operating pressures,
 * operating advice or reliable downhole predictions.
 *
 * Sources:
 * Vermeer D24x40 S3 (2026) https://www.vermeer.com/getmedia/e6628b54-49cc-42cf-8456-f215c7a54485/vermeer-d24x40-s3-spec-sheet-may-2026.pdf?ext=.pdf
 * Vermeer D23x30 S3 (2022) https://www.vermeer.com/getmedia/475562df-ec1f-41c5-ab91-9d3e7c67d944/d23x30-s3-utility-drill-spec-sheet-updated-2022.pdf?ext=.pdf
 * Ditch Witch JT20 https://www.ditchwitchwest.com/new-equipment/all-directional-drills/directional-drills/jt20-directional-drill/
 * Ditch Witch JT21 (2024) https://www.ditchwitch.com/wp-content/uploads/2024/11/JT21-Brochure.pdf
 * JT20 combined joystick reference:
 * https://www.ditchwitchmidwest.com/fckimages/pdfs/manuals/Directional%20Drills/JT20%20operator_s%20manual%20%28ID0318836_09_SVC%29.pdf
 */
data class RigProfile(
    val id: String,
    val displayName: String,
    val modelYearNote: String,
    val rodFeet: Double,
    val thrustLb: Int,
    val pullbackLb: Int,
    val maxTorqueFtLb: Int,
    val maxSpindleRpm: Int,
    val maxFluidPsi: Int,
    val maxFluidGpm: Int,
    val controlMode: RigControlMode,
    val sourceUrl: String,
    val torqueNote: String = ""
)

enum class RigControlMode { TRAINING_DUAL, JT20_COMBINED }

val rigProfiles = listOf(
    RigProfile(
        id = "d24x40s3", displayName = "Vermeer D24x40 S3", modelYearNote = "2026 Vermeer specification",
        rodFeet = 10.0, thrustLb = 28000, pullbackLb = 28000,
        maxTorqueFtLb = 4200, maxSpindleRpm = 270,
        maxFluidPsi = 1050, maxFluidGpm = 50,
        controlMode = RigControlMode.TRAINING_DUAL,
        sourceUrl = "https://www.vermeer.com/na/directional-drills/d24x40-s3"
    ),
    RigProfile(
        id = "d23x30s3", displayName = "Vermeer D23x30 S3", modelYearNote = "2022 Vermeer specification; 2.375-inch rod option",
        rodFeet = 10.0, thrustLb = 24000, pullbackLb = 24000,
        maxTorqueFtLb = 3000, maxSpindleRpm = 219,
        maxFluidPsi = 1300, maxFluidGpm = 35,
        controlMode = RigControlMode.TRAINING_DUAL,
        sourceUrl = "https://www.vermeer.com/getmedia/475562df-ec1f-41c5-ab91-9d3e7c67d944/d23x30-s3-utility-drill-spec-sheet-updated-2022.pdf?ext=.pdf",
        torqueNote = "2.06-inch rod option is rated 2,600 ft-lb."
    ),
    RigProfile(
        id = "jt20", displayName = "Ditch Witch JT20", modelYearNote = "JT20 published dealer specifications; configuration dependent",
        rodFeet = 10.0, thrustLb = 17000, pullbackLb = 20000,
        maxTorqueFtLb = 2200, maxSpindleRpm = 210,
        maxFluidPsi = 1000, maxFluidGpm = 25,
        controlMode = RigControlMode.JT20_COMBINED,
        sourceUrl = "https://www.ditchwitchwest.com/new-equipment/all-directional-drills/directional-drills/jt20-directional-drill/"
    ),
    RigProfile(
        id = "jt21", displayName = "Ditch Witch JT21", modelYearNote = "2024 manufacturer brochure",
        rodFeet = 10.0, thrustLb = 20000, pullbackLb = 21000,
        maxTorqueFtLb = 2250, maxSpindleRpm = 270,
        maxFluidPsi = 1000, maxFluidGpm = 35,
        controlMode = RigControlMode.TRAINING_DUAL,
        sourceUrl = "https://www.ditchwitch.com/wp-content/uploads/2024/11/JT21-Brochure.pdf"
    )
)

fun getRigProfile(id: String): RigProfile = rigProfiles.firstOrNull { it.id == id } ?: rigProfiles.first()
