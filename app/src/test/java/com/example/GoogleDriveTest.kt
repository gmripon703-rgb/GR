package com.example

import com.example.modelmanager.GitHubReleaseConfig
import com.example.storage.gdrive.DriveQuotaInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GoogleDriveTest {

    @Test
    fun testConvertGoogleDriveSharingUrl() {
        val standardUrl = "https://drive.google.com/file/d/1B2C3D4E5F6G7H8I9J/view?usp=sharing"
        val directStream = GitHubReleaseConfig.convertGoogleDriveUrl(standardUrl)
        assertEquals("https://drive.google.com/uc?export=download&id=1B2C3D4E5F6G7H8I9J", directStream)

        val idUrl = "https://drive.google.com/open?id=SAMPLE_FILE_ID&authuser=0"
        val directStreamFromId = GitHubReleaseConfig.convertGoogleDriveUrl(idUrl)
        assertEquals("https://drive.google.com/uc?export=download&id=SAMPLE_FILE_ID", directStreamFromId)
    }

    @Test
    fun testDriveQuotaCalculations() {
        val totalBytes = 15L * 1024 * 1024 * 1024
        val usedBytes = 3L * 1024 * 1024 * 1024
        val quota = DriveQuotaInfo(
            limitBytes = totalBytes,
            usageBytes = usedBytes,
            usageInDriveBytes = 1L * 1024 * 1024 * 1024,
            userEmail = "gmripon703@gmail.com",
            displayName = "GM Ripon"
        )

        assertEquals(20, quota.usagePercent)
        assertTrue(quota.freeBytes > 0)
        assertEquals("15.00 GB", quota.formattedLimit)
        assertEquals("3.00 GB", quota.formattedUsed)
        assertEquals("12.00 GB", quota.formattedFree)
    }
}
