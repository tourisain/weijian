package com.tourisain.weijian.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MembershipStateProtectorPolicyTest {
    @Test
    fun lifetimeMembershipTicketRequiresLifetimeShape() {
        val proof = MembershipActivationProof(
            source = "activation-v5",
            activationHash = "a".repeat(32),
            identityHash = "b".repeat(32),
            deviceCodeHash = "c".repeat(32)
        )

        assertTrue(isMembershipTicketShapeAllowed(isPro = true, level = 2, expiresAt = null, proof = proof))
        assertFalse(isMembershipTicketShapeAllowed(isPro = true, level = 1, expiresAt = null, proof = proof))
        assertFalse(isMembershipTicketShapeAllowed(isPro = true, level = 2, expiresAt = 123L, proof = proof))
        assertFalse(isMembershipTicketShapeAllowed(isPro = true, level = 2, expiresAt = null, proof = proof.copy(source = "free")))
        assertTrue(isMembershipTicketShapeAllowed(isPro = false, level = 0, expiresAt = null, proof = MembershipActivationProof(source = "free")))
    }

    @Test
    fun membershipTicketNonceMustBeStrongWhenPresent() {
        assertTrue(isMembershipTicketNonceStrong("a".repeat(32)))
        assertTrue(isMembershipTicketNonceStrong("A1".repeat(16)))
        assertFalse(isMembershipTicketNonceStrong(""))
        assertFalse(isMembershipTicketNonceStrong("abc"))
        assertFalse(isMembershipTicketNonceStrong("g".repeat(32)))
    }
}
