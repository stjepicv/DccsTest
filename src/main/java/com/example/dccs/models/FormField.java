package com.example.dccs.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Optional;

@Entity
public class FormField {
    public enum Type { TextBox, CheckBox, RadioGroup }
    public enum Validation { None, Mandatory, Numeric}

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private long id;

    @NotNull
    private String label;

    @Enumerated(value = EnumType.STRING)
    @NotNull
    private Type type;

    @Enumerated(value = EnumType.STRING)
    @NotNull
    private Validation validation = Validation.None;

    private String[] possibleValues = null;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "form_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    private Form form;

    public FormField() {
    }

    public FormField(String label, Type type, Validation validation, String[] possibleValues) {
        this.label = label;
        this.type = type;
        this.validation = validation;
        this.possibleValues = Optional.ofNullable(possibleValues).orElse(new String[0]);
    }

    public FormField(FormField other) {
        this(other.label, other.type, other.validation, other.possibleValues);
        this.id = other.id;

    }

    public long getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public Type getType() {
        return type;
    }

    public Validation getValidation() {
        return validation;
    }

    public String[] getPossibleValues() {
        return possibleValues;
    }

    public Form getForm() {
        return form;
    }

    public void setForm(Form form) {
        this.form = form;
    }

    public boolean isValidValue(String value) {
        if (this.getType() == Type.RadioGroup) {
            for (String possibleValue : this.getPossibleValues()) {
                if (possibleValue.equals(value)) {
                    return true;
                }
            }
            return false;

        } else if (this.getType() == Type.CheckBox) {
            if (getValidation() == Validation.Mandatory) {
                return "true".equalsIgnoreCase(value);
            } else {
                return "true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value);
            }

        } else if (this.getValidation() == Validation.Mandatory) {
            return value != null && !value.trim().isEmpty();

        } else if (this.getValidation() == Validation.Numeric) {
            try {
                Double.parseDouble(value);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }

        return true;

    }
}
