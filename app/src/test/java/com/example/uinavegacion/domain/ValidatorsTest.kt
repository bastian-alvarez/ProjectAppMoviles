package com.example.uinavegacion.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
class ValidatorsTest {

    @Test
    fun `validateLettersOnly accepts names with spaces and accents`() {
        assertNull(validateLettersOnly("José María"))
    }

    @Test
    fun `validateLettersOnly rejects names with digits`() {
        assertEquals("Solo letras y espacios", validateLettersOnly("John3"))
    }

    @Test
    fun `validateNickname accepts values between 3 and 20 characters`() {
        assertNull(validateNickname("user_name123"))
    }

    @Test
    fun `validateNickname rejects invalid characters`() {
        assertEquals("Solo letras, números y guion bajo (_)", validateNickname("bad-nick"))
    }

    @Test
    fun `validatePhoneDigitsOnly accepts formatted chilean number with spaces`() {
        assertNull(validatePhoneDigitsOnly("+56 9 1234 5678"))
    }

    @Test
    fun `validatePhoneDigitsOnly rejects numbers without +56`() {
        assertEquals("Debe comenzar con +56", validatePhoneDigitsOnly("9 1234 5678"))
    }

    @Test
    fun `validateStrongPassword returns null for strong password`() {
        assertNull(validateStrongPassword("Str0ng!Pass"))
    }

    @Test
    fun `validateStrongPassword requires uppercase letter`() {
        assertEquals("Debe incluir una Mayúscula", validateStrongPassword("weakpass1!"))
    }

    @Test
    fun `validateConfirm returns null when passwords match`() {
        assertNull(validateConfirm("Secret123!", "Secret123!"))
    }

    @Test
    fun `validateConfirm returns error when passwords do not match`() {
        assertEquals("Las contraseñas no coinciden", validateConfirm("Secret123!", "Secret321!"))
    }
}

