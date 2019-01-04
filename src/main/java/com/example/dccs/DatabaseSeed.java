package com.example.dccs;

import com.example.dccs.models.FormField;
import com.example.dccs.repositories.FormValueRepository;
import org.springframework.stereotype.Component;

import com.example.dccs.models.Form;
import com.example.dccs.repositories.FormRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;

@Component
public class DatabaseSeed implements CommandLineRunner {
    private FormRepository formRepository;
    private FormValueRepository formValueRepository;

    @Autowired
    public DatabaseSeed(FormRepository formRepository, FormValueRepository formValueRepository) {
        this.formRepository = formRepository;
        this.formValueRepository = formValueRepository;
    }

    @Override
    public void run(String... commands) throws Exception {
        Form form = new Form("Test formular");

        FormField[] fields = new FormField[] {
                new FormField("Ime", FormField.Type.TextBox, FormField.Validation.Mandatory, null),
                new FormField("Prezime", FormField.Type.TextBox, FormField.Validation.None, null),
                new FormField("Godiste", FormField.Type.TextBox, FormField.Validation.Numeric, null),
                new FormField("Spol", FormField.Type.RadioGroup, FormField.Validation.None, new String[] { "Musko", "Zensko" }),
                new FormField("Prihvatam uvjete", FormField.Type.CheckBox, FormField.Validation.Mandatory, null)
        };

        for(FormField field : fields) {
            field.setForm(form);
            form.getFields().add(field);
        }

        formRepository.save(form);
    }
}