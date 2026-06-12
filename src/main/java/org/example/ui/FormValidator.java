package org.example.ui;

public final class FormValidator {
    private FormValidator() {}

    public static String validateName(String value, String label) {
        if (value == null || value.trim().isEmpty()) {
            String article = "usuário".equals(label) ? "do" : "da";
            return "Nome " + article + " " + label + " não pode estar vazio.";
        }
        return null;
    }

    public static String validateTask(String title, Object user, Object category) {
        if (title == null || title.trim().isEmpty()) {
            return "Título da tarefa não pode estar vazio.";
        }
        if (user == null) {
            return "Selecione um usuário para a tarefa.";
        }
        if (category == null) {
            return "Selecione uma categoria para a tarefa.";
        }
        return null;
    }
}
