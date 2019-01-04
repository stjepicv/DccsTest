const FormEditor = {
    formName: '',

    show: () => {
        $('#form-editor').show()
    },

    hide: () => {
        $('#form-editor').hide()
    },

    newForm: (formName) => {
        FormEditor.clear()
        FormEditor.addField()
        FormEditor.formName = formName
    },

    clear: () => {
        $('#fields-container').empty()
    },

    addField: (label, type, validation, possibleValues) => {
        const templateHtml = $('#template-field').html()
        const fieldElement = $(templateHtml)

        $('#fields-container').append(fieldElement)

        // set event listeners
        fieldElement.find('.btn-add-field').click(() => FormEditor.addField())
        fieldElement.find('.btn-remove-field').click(() => fieldElement.remove())

        fieldElement.find('.field-type').change(() => {
            const newValue = fieldElement.find('.field-type').val()
            if (newValue == 'RadioGroup') {
                fieldElement.find('.field-radio-option-count').show()
                fieldElement.find('.field-radio-options').show()
            } else {
                fieldElement.find('.field-radio-option-count').hide()
                fieldElement.find('.field-radio-options').hide()
            }
        })

        fieldElement.find('.field-radio-option-count').change(() => {
            const optionsContainer = fieldElement.find('.field-radio-options')
            const optionsCount = fieldElement.find('.field-radio-option-count').val()
            const optionHtml = $('#template-radio-option').html()

            optionsContainer.empty()
            for (var i = 0; i < optionsCount; i++) {
                optionsContainer.append(optionHtml)
            }
        })

        //populate data if provided
        fieldElement.find('.field-label').val(label)
        fieldElement.find('.field-type').val(type)
        fieldElement.find('.field-validation').val(validation)
        if (possibleValues && possibleValues.length > 0) {

            fieldElement.find('.field-radio-option-count').val(possibleValues.length)
            const optionsContainer = fieldElement.find('.field-radio-options')
            const optionHtml = $('#template-radio-option').html()

            optionsContainer.empty()
            for (var i = 0; i < possibleValues.length; i++) {
                const optionElement = $(optionHtml)
                optionElement.find('.field-radio-option').val(possibleValues[i])
                optionsContainer.append(optionElement)
            }
        }

        // this will show/hide additional options accordingly
        fieldElement.find('.field-type').trigger('change')
    },


    getData: () => {
        return { 
            name: FormEditor.formName, 
            fields: $('#fields-container .field').map((_i, field) => {
                var fieldElement = $(field)
                return {
                    label: fieldElement.find('.field-label').val(),
                    type: fieldElement.find('.field-type').val(),
                    validation: fieldElement.find('.field-validation').val(),
                    possibleValues: fieldElement.find('.field-radio-option')
                        .map((_j, option) => $(option).val())
                        .toArray()
                }
            }).toArray()
        }
    },


    createFromData: (data) => {
        FormEditor.clear()

        FormEditor.formName = data.name
        $.each(data.fields, (_i, field) => {
            FormEditor.addField(field.label, field.type, field.validation, field.possibleValues)
        })
    }
}



$(document).ready(() => {

    $('#btn-search').click(() => {
        FormEditor.hide()
        var formName = $('#search-bar').val()
        
        if (formName) {
            $.get('./form/get', { name: formName }, (response) => {
                FormEditor.show()
                if (response) {
                    FormEditor.createFromData(response)
                } else {
                    FormEditor.newForm(formName)
                }
            })
            .fail(() => {
                alert('There was an error')
            })
        } else {
            alert('You must enter form name')
        }
    }),

    $('#btn-save').click(() => {
        $.ajax({
            type: 'POST',
            url: './form/create',
            contentType: "application/json",
            data: JSON.stringify(FormEditor.getData()),
            success: (response) => {
                FormEditor.hide()
                alert('Successfully saved')
            },
            error: () => {
                alert('There was an error')
            }
        })
    })
})