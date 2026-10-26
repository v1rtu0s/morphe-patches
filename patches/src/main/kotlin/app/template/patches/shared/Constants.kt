package app.blitzermorph.patches.shared

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility
import app.morphe.patcher.patch.SupportedAbi

object Constants {
    /**
     * Google Maps compatibility for the BlitzerMorph patch.
     * Injects live mobile speed cameras from Blitzer.de (Atudo API) into Google Maps.
     */
    val COMPATIBILITY_GOOGLE_MAPS = Compatibility(
        name = "Google Maps",
        packageName = "com.google.android.apps.maps",
        // Google Maps distributes as APKM/XAPK on APKMirror/UpToDown
        apkFileType = ApkFileType.APKM,
        // Google Maps primary icon color (blue)
                appIconColor = 0x1A73E8,
        targets = listOf(
            // Latest confirmed working version
            AppTarget(
                version = "v15.2.0124"
            ),
            // Experimental support for future versions
            AppTarget(
                version = null,
                isExperimental = true
            )
        )
    )
}