package com.example.dccs.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Entity
public class FormValue {

    @EmbeddedId
    private Id id;

    @NotNull
    private String value;


    public FormValue() {
    }

    public FormValue(Id id, String value) {
        this.id = id;
        this.value = value;
    }

    public Id getId() {
        return id;
    }

    public long getFormFieldId() {
        return id.formField.getId();
    }

    public String getValue() {
        return value;
    }

    @Embeddable
    public static class Id implements Serializable {
        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "formfield_id", nullable = false)
        @OnDelete(action = OnDeleteAction.CASCADE)
        private FormField formField;
        private int version;

        public Id() {
        }

        public Id(FormField formField, int version) {
            this.formField = formField;
            this.version = version;
        }

        public FormField getFormField() {
            return formField;
        }

        public int getVersion() {
            return version;
        }
    }
}
