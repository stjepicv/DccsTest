package com.example.dccs.models;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Entity
public class Form {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private long id;
    @NotNull
    private String name;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "form")
    private List<FormField> fields = new ArrayList<>();

    public Form() {
    }

    public Form(String name) {
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<FormField> getFields() {
        return fields;
    }

    public void setFields(List<? extends FormField> fields) {
        this.fields = (List<FormField>) fields;
    }

    public Optional<FormField> getFieldById(long fieldId) {
        return this.getFields().stream()
                .filter(f -> f.getId() == fieldId)
                .findAny();
    }
}
