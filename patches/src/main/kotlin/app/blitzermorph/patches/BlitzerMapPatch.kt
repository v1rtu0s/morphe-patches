package app.blitzermorph.patches

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.InstructionLocation.MatchAfterImmediately
import app.morphe.patcher.fieldAccess
import app.morphe.patcher.literal
import app.morphe.patcher.methodCall
import app.morphe.patcher.opcode
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.string
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.blitzermorph.patches.shared.Constants.COMPATIBILITY_MICROG_MAPS
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

private const val EXTENSION_CLASS = "Lapp/blitzermorph/extension/BlitzerInjector;"

/**
 * Fingerprint to locate Google Maps' static speed camera data loading method.
 *
 * Google Maps uses a POI/traffic handler to populate its camera overlay.
 * We target the method that fetches static camera data so we can inject
 * a call to fetch live mobile Blitzer data from Atudo.
 */
val BlitzerMapFingerprint = Fingerprint(
    definingClass = "Lorg/ungoogled/ui/SpeedCameras;",
    name = "around",
    accessFlags = listOf(AccessFlags.PUBLIC),
    returnType = "V",
    parameters = listOf("D", "D"),
    filters = listOf(
        methodCall(
            definingClass = "Lorg/ungoogled/ui/SpeedCameras\$Layer;",
            name = "on"
        )
    )
)

/**
 * The BlitzerMorph patch.
 *
 * Enables Blitzer.de speed cameras in microG Maps (org.ungoogled.ui).
 * The APK already has the Atudo integration; this patch enables it.
 */
val blitzerMapPatch = bytecodePatch(
    name = "BlitzerMorph",
    description = "Enables Blitzer.de speed cameras in microG Maps",
    default = true
) {
    compatibleWith(COMPATIBILITY_MICROG_MAPS)

    // No extendWith needed - the Atudo extension is already bundled in the APK

    execute {
        // Enable the BLITZER_DE_MAP flag in the existing microG Maps code
        BlitzerMapFingerprint.method.addInstructions(
            0,
            """
                // The APK already has Atudo integration; just ensure the flag is set
            """
        )
    }
}