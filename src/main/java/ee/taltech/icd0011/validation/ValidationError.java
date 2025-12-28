package ee.taltech.icd0011.validation;

public class ValidationError {
    private String code;

    public ValidationError() {
    }

    public ValidationError(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}