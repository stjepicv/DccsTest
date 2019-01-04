function FormManager(rootElement) {
    this.show = () => rootElement.show()

    this.hide = () => rootElement.hide()

    this.clear = () => rootElement.find('#form-fields').empty()

    this.loadForm = (form, version) => {
        rootElement.find('input[name="name"]').val(form.name)
        rootElement.find('input[name="version"]').val(version)

        $.each(form.fields, (_i, field) => {
            addField(field.id, field.label, field.value, field.type, field.validation, field.possibleValues)
        })
    }

    function addField(id, label, value, type, validation, possibleValues) {
        if (type == 'TextBox') {
            const fieldElement = $($('#template-textbox').html())

            fieldElement.find('.field-label').html(label)
            fieldElement.find('input').attr('name', id)
            if (validation == 'Numeric') {
                fieldElement.find('input').attr('type', 'number')
            }
            fieldElement.find('input').val(value)

            rootElement.find('#form-fields').append(fieldElement)

        } else if (type == 'CheckBox') {
            const fieldElement = $($('#template-checkbox').html())
            fieldElement.find('.field-label').html(label)
            fieldElement.find('input').attr('name', id)
            if (value && value.toLowerCase() == 'true') {
                fieldElement.find('input').attr('checked', 'checked')
            }

            rootElement.find('#form-fields').append(fieldElement)

        } else if (type == 'RadioGroup') {
            const fieldElement = $($('#template-radiogroup').html()) 
            
            fieldElement.find('.field-label').html(label)

            const optionsContainer = fieldElement.find('.field-radio-container')
            const optionHtml = $('#template-radio').html()
            $.each(possibleValues, (_i, possibleValue) => {
                const optionElement = $(optionHtml)
                const inputElement = optionElement.find('input')
                inputElement.attr('name', id)
                inputElement.attr('value', possibleValue)
                optionElement.find('.radio-label').html(possibleValue)
                optionsContainer.append(optionElement)
            })
            
            optionsContainer.find('input[value="' + value + '"]').attr('checked', 'checked')
            
            rootElement.find('#form-fields').append(fieldElement)
        }
    }

    this.getData = () => {
        const arr = rootElement.find('form').serializeArray()
        const map = {}
        $.each(arr, (_i, element) => {
            map[element.name] = element.value
        })
        return map
    }
}


$(document).ready(() => {
    const formManager = new FormManager($('#form-container'))

    $('#btn-load').click(() => {
        formManager.hide()
        formManager.clear()

        const formName = $('#search-bar').val()
        const formVersion = $('#input-version').val()

        if (formName && formVersion) {
            $.get('./form/get', { name: formName, version: formVersion }, (response) => {
                formManager.loadForm(response, formVersion)
                formManager.show()
            })
            .fail(() => alert('There was an error'))
        } else {
            alert('You must enter form name and version')
        }
    })

    $('#btn-save').click(() => {
        formManager.hide()
        $.ajax({
            type: 'POST',
            url: './form/submit',
            contentType: 'application/json',
            data: JSON.stringify(formManager.getData()),
            success: () => {
                alert('Successfully saved')
            },
            error: (http) => {
                const response = JSON.parse(http.responseText)
                if (response.errorMessage) {
                    alert(response.errorMessage)
                } else {
                    alert('There was an error')
                }
                formManager.show()
            }
        })
    })
})