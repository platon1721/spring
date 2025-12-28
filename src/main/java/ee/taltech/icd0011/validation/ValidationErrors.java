package ee.taltech.icd0011.validation;

import java.util.ArrayList;
import java.util.List;

public class ValidationErrors {
    private List<ValidationError> errors;

    public ValidationErrors() {
        this.errors = new ArrayList<>();
    }

    public void addError(String code) {
        errors.add(new ValidationError(code));
    }

    public List<ValidationError> getErrors() {
        return errors;
    }

    public void setErrors(List<ValidationError> errors) {
        this.errors = errors;
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }
}