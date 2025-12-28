package ee.taltech.icd0011.dto;

import java.util.List;

public class Result<T> {
    private boolean success;
    private T value;
    private List<String> errors;

    public Result() {
    }

    public Result(boolean success, T value, List<String> errors) {
        this.success = success;
        this.value = value;
        this.errors = errors;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }
}