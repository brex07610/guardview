package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class GuardViewRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun createCheckin_validData_succeeds() {
        runBlocking {
            signInTestUser(TEST_GUARD_EMAIL)
            val repository = GuardViewRepository(firestore)

            val siteId = "site_${UUID.randomUUID().toString().take(8)}"
            val metroSite = mapOf(
                "name" to "Metro Center",
                "address" to "123 Main St",
                "clientId" to "client_1",
                "currentStatus" to "on_site",
                "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
            )
            firestore.collection("sites").document(siteId).set(metroSite)

            val checkinId = withTimeout(5000L) {
                repository.clockIn(
                    guardId = "guard_alice",
                    guardName = "Alice Guard",
                    siteId = siteId,
                    siteName = "Metro Center",
                    lat = 37.7749,
                    lng = -122.4194,
                    isWithinGeofence = true,
                    selfieUrl = "https://example.com/selfie.jpg",
                    lateMinutes = 0L
                )
            }
            assertNotNull(checkinId)
            assertTrue(checkinId.isNotEmpty())
        }
    }

    @Test
    fun observeSites_authenticatedUser_returnsSites() {
        runBlocking {
            signInTestUser(TEST_CLIENT_EMAIL)
            val repository = GuardViewRepository(firestore)

            val siteId = "site_${UUID.randomUUID().toString().take(8)}"
            firestore.collection("sites").document(siteId).set(
                mapOf(
                    "name" to "Logistics Bay",
                    "address" to "88 Harbor Rd",
                    "clientId" to "client_logistics",
                    "currentStatus" to "on_site",
                    "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                )
            )

            val sites = withTimeout(5000L) {
                repository.observeSites().first { list -> list.any { it.id == siteId } }
            }
            assertTrue(sites.any { it.id == siteId })
        }
    }

    @Test
    fun createTicket_validComplaint_succeeds() {
        runBlocking {
            signInTestUser(TEST_CLIENT_EMAIL)
            val repository = GuardViewRepository(firestore)

            val ticketId = withTimeout(5000L) {
                repository.createTicket(
                    clientId = "client_metro",
                    siteId = "site_metro",
                    siteName = "Metro Plaza",
                    category = "guard_absent",
                    description = "Guard missing at morning gate opening",
                    supervisorName = "Capt. Ramos",
                    autoEvidenceText = "Shift #101 scheduled 06:00, no checkin recorded."
                )
            }
            assertNotNull(ticketId)
            assertTrue(ticketId.isNotEmpty())
        }
    }

    private companion object {
        const val TEST_GUARD_EMAIL = "guard@guardview.test"
        const val TEST_CLIENT_EMAIL = "client@guardview.test"
    }
}
