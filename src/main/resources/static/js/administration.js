function FormEditor(rootElement) {
    this.formName = ''

    this.show = () => rootElement.show()

    this.hide = () => rootElement.hide()

    this.clear = () => rootElement.find('#fields-container').empty()

    this.newForm = (formName) => {
        this.clear()
        this.addField()
        this.formName = formName
    }

    function addField(label, type, validation, possibleValues) {
        const templateHtml = $('#template-field').html()
        const fieldElement = $(templateHtml)

        rootElement.find('#fields-container').append(fieldElement)

        fieldElement.find('.btn-add-field').click(() => addField())
        fieldElement.find('.btn-remove-field').click(() => {
            fieldElement.remove()
            updateIndexes()
        })

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

        // this will show/hide additional options accordingly, 
        // change event listener was set a few lines ago inside this function
        fieldElement.find('.field-type').trigger('change')
        updateIndexes()
    }

    function updateIndexes() {
        $.each(rootElement.find('.field-index'), (index, element) => $(element).html(index + 1))
    }

    this.createFromData = (data) => {
        this.clear()

        this.formName = data.name
        $.each(data.fields, (_i, field) => {
            addField(field.label, field.type, field.validation, field.possibleValues)
        })
    }

    this.getData = () => {
        return { 
            name: this.formName, 
            fields: rootElement.find('.field').map((_i, field) => {
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
    }
}





$(document).ready(() => {
    const formEditor = new FormEditor($('#form-editor'))

    $('#btn-search').click(() => {
        formEditor.hide()
        var formName = $('#search-bar').val()
        
        if (formName) {
            $.get('./form/get', { name: formName }, (response) => {
                formEditor.show()
                if (response) {
                    formEditor.createFromData(response)
                } else {
                    formEditor.newForm(formName)
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
            data: JSON.stringify(formEditor.getData()),
            success: (response) => {
                formEditor.hide()
                alert('Successfully saved')
            },
            error: () => {
                alert('There was an error')
            }
        })
    })
})