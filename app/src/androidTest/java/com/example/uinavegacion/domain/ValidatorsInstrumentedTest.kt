package com.example.uinavegacion.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class ValidatorsInstrumentedTest {

    @Test
    fun validateEmail_instrumented_returnsNullForValidEmail() {
        assertNull(validateEmail("mobile@test.com"))
    }

    @Test
    fun validateEmail_instrumented_returnsErrorForInvalidEmail() {
        assertEquals("Formato de correo inválido", validateEmail("mobile-test"))
    }

    @Test
    fun validatePhoneDigitsOnly_instrumented_acceptsCompactFormat() {
        assertNull(validatePhoneDigitsOnly("+56987654321"))
    }

    @Test
    fun validateStrongPassword_instrumented_rejectsSpaces() {
        assertEquals("No debe contener espacios", validateStrongPassword("Strong Pass1!"))
    }
}

