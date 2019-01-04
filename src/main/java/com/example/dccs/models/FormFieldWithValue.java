package com.example.dccs.models;

public class FormFieldWithValue extends FormField {
    private String value;

    public FormFieldWithValue(FormField formField, String value) {
        super(formField);
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
