package org.example.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FormValidatorTest {

    @Test
    void shouldRejectBlankUserName() {
        assertEquals("Nome do usuário não pode estar vazio.", FormValidator.validateName("", "usuário"));
    }

    @Test
    void shouldRejectBlankCategoryName() {
        assertEquals("Nome da categoria não pode estar vazio.", FormValidator.validateName("   ", "categoria"));
    }

    @Test
    void shouldAcceptNonBlankName() {
        assertNull(FormValidator.validateName("Minha Tarefa", "tarefa"));
    }

    @Test
    void shouldRejectTaskWithoutTitle() {
        assertEquals("Título da tarefa não pode estar vazio.", FormValidator.validateTask("", null, null));
    }

    @Test
    void shouldRejectTaskWithoutUser() {
        assertEquals("Selecione um usuário para a tarefa.", FormValidator.validateTask("Tarefa", null, null));
    }

    @Test
    void shouldRejectTaskWithoutCategory() {
        assertEquals("Selecione uma categoria para a tarefa.", FormValidator.validateTask("Tarefa", new Object(), null));
    }

    @Test
    void shouldAcceptCompleteTaskData() {
        assertNull(FormValidator.validateTask("Tarefa", new Object(), new Object()));
    }
}
