package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.modelmanager.GitHubReleaseConfig
import com.example.security.SecureStorage
import com.example.storage.gdrive.DriveQuotaInfo
import com.example.storage.gdrive.GoogleDriveManager
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
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

    @Test
    fun testUnauthenticatedDriveOperationsFailWithoutFabrication() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val storage = SecureStorage(context)
        storage.googleDriveConnected = false
        storage.googleDriveAccessToken = ""

        val manager = GoogleDriveManager(context, storage)

        // Must start disconnected when no verified token exists
        assertFalse(manager.isConnected.value)
        assertEquals(0, manager.driveFiles.value.size)
        assertEquals(null, manager.driveQuota.value)

        // Blank token authentication fails
        val blankAuth = manager.authenticateWithToken("   ")
        assertTrue(blankAuth.isFailure)
        assertFalse(manager.isConnected.value)

        // Unauthenticated upload must fail with clear exception, NOT report simulated success
        val teamRulesSync = manager.syncTeamRulesToDrive("{\"rules\": []}")
        assertTrue("Sync should fail when unauthenticated", teamRulesSync.isFailure)
        assertTrue(teamRulesSync.exceptionOrNull() is IllegalStateException)
        assertEquals(0, manager.driveFiles.value.size)

        val ragSync = manager.syncRagArchiveToDrive("[]")
        assertTrue("RAG sync should fail when unauthenticated", ragSync.isFailure)

        val chatSync = manager.syncChatHistoryToDrive("[]")
        assertTrue("Chat sync should fail when unauthenticated", chatSync.isFailure)
    }
}
