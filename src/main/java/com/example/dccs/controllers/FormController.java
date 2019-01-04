package com.example.dccs.controllers;

import com.example.dccs.models.*;
import com.example.dccs.repositories.FormRepository;
import com.example.dccs.repositories.FormValueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping(value = "/form")
public class FormController {

    private FormRepository formRepository;
    private FormValueRepository formValueRepository;

    @Autowired
    public FormController(FormRepository formRepository, FormValueRepository formValueRepository) {
        this.formRepository = formRepository;
        this.formValueRepository = formValueRepository;
    }

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public Form create(@RequestBody Form form) {
        // delete old version if it exists
        formRepository.findByName(form.getName())
                .ifPresent(oldForm -> formRepository.delete(oldForm));

        for (FormField field : form.getFields()) {
            field.setForm(form);
        }
        formRepository.save(form);

        return form;
    }

    @RequestMapping(value = "/get", method = RequestMethod.GET, params = "name")
    public Form getByName(@RequestParam("name") String name) {
        return formRepository.findByName(name).orElse(null);
    }


    @RequestMapping(value = "/get", method = RequestMethod.GET, params = { "name", "version" })
    public Form getByName(@RequestParam("name") String name, @RequestParam("version") int version) {
        Form form = formRepository.findByName(name).orElse(null);

        // get values for requested version
        if (form != null) {
            List<FormFieldWithValue> fieldsWithValues = new ArrayList<>();
            for (FormField field : form.getFields()) {
                String fieldValue = formValueRepository.findById(new FormValue.Id(field, version))
                        .map(FormValue::getValue)
                        .orElse(null);

                fieldsWithValues.add(new FormFieldWithValue(field, fieldValue));
            }
            form.setFields(fieldsWithValues);
        }

        return form;
    }

    /*
        The parameters for this request should countain name and version of the form,
        as well as id and value pairs for the form fields.
        Example: {
            name: "Test formular",
            version: "1",
            2: "Valentin",
            3: "Stjepic"
            ...
        }
     */
    @RequestMapping(value = "/submit", method = RequestMethod.POST)
    public ResponseEntity submit(@RequestBody Map<String, String> params) {

        // check if form name and version are provided

        String formName = params.get("name");
        int version;

        if (formName == null) {
            ErrorResponse error = new ErrorResponse("Form name not provided");
            return ResponseEntity.badRequest().body(error);
        }

        try {
            version = Integer.parseInt(params.get("version"));
        } catch (NumberFormatException e) {
            ErrorResponse error = new ErrorResponse("Version not provided or not an integer");
            return ResponseEntity.badRequest().body(error);
        }

        Form form = formRepository.findByName(formName).orElse(null);
        if (form == null) {
            ErrorResponse error = new ErrorResponse("No form with this name exists");
            return ResponseEntity.badRequest().body(error);
        }




        // for each field id and value pair provided check if it passes validation and create matching FormValue object

        List<FormValue> formValues = new ArrayList<>();

        for (Map.Entry<String, String> pair : params.entrySet()) {
            try {
                long fieldId = Long.parseLong(pair.getKey());

                FormField field = form.getFieldById(fieldId).orElse(null);
                if (field == null) {
                    String message = String.format("Field with id %d is not part of this form", fieldId);
                    return ResponseEntity.badRequest().body(new ErrorResponse(message));
                } else if(!field.isValidValue(pair.getValue())) {
                    String message;
                    if (field.getValidation() == FormField.Validation.Mandatory) {
                        message = String.format("Field \"%s\" is mandatory", field.getLabel());
                    } else if (field.getValidation() == FormField.Validation.Numeric) {
                        message = String.format("Field \"%s\" has to be a number", field.getLabel());
                    } else {
                        message = "Field does not pass validation";
                    }
                    return ResponseEntity.badRequest().body(new ErrorResponse(message));
                } else {
                    FormValue.Id valueId = new FormValue.Id(field, version);
                    formValues.add(new FormValue(valueId, pair.getValue()));
                }
            } catch (NumberFormatException e) {

            }
        }

        // check if the form contains any fields which are mandatory but weren't provided
        Optional<FormField> missingField = form.getFields().stream()
                                .filter(field -> formValues.stream()
                                        .noneMatch(formValue -> formValue.getFormFieldId() == field.getId()))
                                .filter(field -> !field.isValidValue(null))
                                .findFirst();

        if (missingField.isPresent()) {
            String message = String.format("Field \"%s\" is mandatory", missingField.get().getLabel());
            return ResponseEntity.badRequest().body(new ErrorResponse(message));
        }

        
        formValueRepository.saveAll(formValues);
        return ResponseEntity.ok(null);
    }
}
