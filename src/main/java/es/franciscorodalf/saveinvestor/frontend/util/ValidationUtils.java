package es.franciscorodalf.saveinvestor.frontend.util;

import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;

public class ValidationUtils {
    
    public static boolean isEmptyField(TextField field) {
        return field == null || field.getText().trim().isEmpty();
    }

    public static boolean isEmptyField(PasswordField field) {
        return field == null || field.getText().trim().isEmpty();
    }

    public static boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return email != null && email.matches(emailRegex);
    }

    public static boolean doPasswordsMatch(PasswordField pass1, PasswordField pass2) {
        return pass1.getText().equals(pass2.getText());
    }

    public static boolean doFieldsMatch(TextField field1, TextField field2) {
        return field1.getText().equals(field2.getText());
    }

}
