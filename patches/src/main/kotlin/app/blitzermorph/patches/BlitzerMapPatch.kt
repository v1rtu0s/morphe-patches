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
import app.blitzermorph.patches.shared.Constants.COMPATIBILITY_GOOGLE_MAPS
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
    definingClass = "Lcom/google/android/apps/maps/traffic/;",
    name = "loadStaticCameras",
    accessFlags = listOf(AccessFlags.PUBLIC),
    returnType = "V",
    parameters = listOf(),
    filters = listOf(
        string("static_speed_cameras"),
        methodCall(
            definingClass = "Lcom/google/android/apps/maps/traffic/TrafficState;",
            name = "getCameras"
        )
    )
)

/**
 * The BlitzerMorph patch.
 *
 * Injects live mobile speed cameras from Blitzer.de (Atudo API) into Google Maps.
 */
val blitzerMapPatch = bytecodePatch(
    name = "BlitzerMorph",
    description = "Injects live mobile speed cameras from Blitzer.de into Google Maps.",
    default = true
) {
    compatibleWith(COMPATIBILITY_GOOGLE_MAPS)

    // Bundle the compiled extension .mpe package
    extendWith("extensions/blitzermorph.mpe")

    execute {
        // Inject the hook to fetch live Blitzer data
        BlitzerMapFingerprint.method.addInstructions(
            0,
            """
                invoke-static {}, $EXTENSION_CLASS;->injectDynamicPOIs()V
            """
        )
    }
}