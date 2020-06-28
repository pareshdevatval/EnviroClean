package com.enviroclean.utils.validator

import android.content.Context
import android.widget.EditText
import com.enviroclean.R

/**
 * Created by Darshna Desai on 30/3/18.
 */

/**
 * A utility class which contains methods with all the validation logic of whole app.
 */
object Validator {

    private const val EMAIL_PATTERN: String = "[mInputCharacter-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
            "\\@" +
            "[mInputCharacter-zA-Z0-9][mInputCharacter-zA-Z0-9\\-]{0,64}" +
            "(" +
            "\\." +
            "[mInputCharacter-zA-Z]{2,8}" +
            ")+"

    private const val PASSWORD_PATTERN: String = "^.*(?=.{6,20})(?=.*\\d)(?=.*[mInputCharacter-zA-Z])(^[mInputCharacter-zA-Z0-9._@!&$*%+-:/><#]+$)"



    fun validateEmail(email: String?): ValidationErrorModel? {
        return if (isBlank(email))
            ValidationErrorModel(R.string.blank_email_or_cellphone, ValidationError.EMAIL)
        else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches())
            ValidationErrorModel(R.string.invalid_email, ValidationError.EMAIL)
        else
            null
    }

    fun validateEmailAddress(email: String?): ValidationErrorModel? {
        return if (isBlank(email))
            ValidationErrorModel(R.string.blank_email, ValidationError.EMAIL)
        else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches())
            ValidationErrorModel(R.string.invalid_email, ValidationError.EMAIL)
        else
            null
    }

    fun validateEmailAddressSecond(email: String?): ValidationErrorModel? {
        return if (isBlank(email))
            ValidationErrorModel(R.string.blank_email, ValidationError.EMAIL)
        else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches())
            ValidationErrorModel(R.string.invalid_email_, ValidationError.EMAIL)
        else
            null
    }

    fun validateFirstName(fname: String?): ValidationErrorModel? {
        return if (isBlank(fname))
            ValidationErrorModel(R.string.blank_first_name, ValidationError.FIRST_NAME)
        else
            null
    }

    fun validateName(name: String?): ValidationErrorModel? {
        return if (isBlank(name))
            ValidationErrorModel(R.string.blank_name, ValidationError.NAME)
        else
            null
    }

    fun validateBillingAddress(billingAddress: String?): ValidationErrorModel? {
        return if (isBlank(billingAddress))
            ValidationErrorModel(R.string.blank_billing_add, ValidationError.BILLING_ADDRESS)
        else
            null
    }

    fun validateBillingCity(billingCity: String?): ValidationErrorModel? {
        return if (isBlank(billingCity))
            ValidationErrorModel(R.string.blank_billing_city, ValidationError.BILLING_CITY)
        else
            null
    }

    fun validateBillingState(billingState: String?): ValidationErrorModel? {
        return if (isBlank(billingState))
            ValidationErrorModel(R.string.blank_billing_state, ValidationError.BILLING_STATE)
        else
            null
    }

    fun validateBillingZipCode(billingZipCode: String?): ValidationErrorModel? {
        return if (isBlank(billingZipCode))
            ValidationErrorModel(R.string.blank_billing_zipcode, ValidationError.BILLING_ZIPCODE)
        else
            null
    }

    fun validateShippingAddress(shippingAddress: String?): ValidationErrorModel? {
        return if (isBlank(shippingAddress))
            ValidationErrorModel(R.string.blank_billing_add, ValidationError.SHIPING_ADDRESS)
        else
            null
    }

    fun validateShippingCity(shippingCity: String?): ValidationErrorModel? {
        return if (isBlank(shippingCity))
            ValidationErrorModel(R.string.blank_billing_city, ValidationError.SHIPING_CITY)
        else
            null
    }

    fun validateShippingState(shippingState: String?): ValidationErrorModel? {
        return if (isBlank(shippingState))
            ValidationErrorModel(R.string.blank_billing_state, ValidationError.SHIPING_STATE)
        else
            null
    }

    fun validateShippingZipCode(shippingZipCode: String?): ValidationErrorModel? {
        return if (isBlank(shippingZipCode))
            ValidationErrorModel(R.string.blank_billing_zipcode, ValidationError.SHIPING_ZIPCODE)
        else
            null
    }

    fun validateCardHolderName(cardHolderName: String?): ValidationErrorModel? {
        return if (isBlank(cardHolderName))
            ValidationErrorModel(R.string.blank_card_holder_name, ValidationError.CARD_HOLDER_NAME)
        else
            null
    }

    fun validateCardNumber(cardNumber: String?): ValidationErrorModel? {
        return if (isBlank(cardNumber))
            ValidationErrorModel(R.string.blank_card_number, ValidationError.CARD_NUMBER)
        else
            null
    }

    fun validateCardExpirationDate(expirationDate: String?): ValidationErrorModel? {
        return if (isBlank(expirationDate))
            ValidationErrorModel(R.string.blank_expiration_date,
                ValidationError.CARD_EXPIRATION_DATE
            )
        else
            null
    }

    fun validateCardCvv(cvv: String?): ValidationErrorModel? {
        return if (isBlank(cvv))
            ValidationErrorModel(R.string.blank_cvv, ValidationError.CVV)
        else
            null
    }

    fun validateSubject(subject: String?): ValidationErrorModel? {
        return if (isBlank(subject))
            ValidationErrorModel(R.string.blank_subject, ValidationError.SUBJECT)
        else
            null
    }

    fun validateMessages(messages: String?): ValidationErrorModel? {
        return if (isBlank(messages))
            ValidationErrorModel(R.string.blank_messages, ValidationError.MESSAGE)
        else
            null
    }


    fun validateEmailOrCellPhone(emailOrCellPhone: String?): ValidationErrorModel? {
        return when {
            isBlank(emailOrCellPhone) -> ValidationErrorModel(
                R.string.blank_email_or_cellphone,
                ValidationError.EMAIL_OR_CELLPHONE
            )
            emailOrCellPhone!!.length !in 10..15 -> ValidationErrorModel(
                R.string.invalid_phone,
                ValidationError.PHONE
            )

            else -> null
        }
    }


    fun validateLastName(lname: String?): ValidationErrorModel? {
        return if (isBlank(lname))
            ValidationErrorModel(R.string.blank_last_name, ValidationError.LAST_NAME)
        else
            null
    }

    fun validatePassword(password: String?, msg: Int = R.string.blank_password): ValidationErrorModel? {
        return when {
            isBlank(password) -> ValidationErrorModel(msg, ValidationError.PASSWORD)
            password!!.length < 6 -> ValidationErrorModel(R.string.invalid_password,
                ValidationError.PASSWORD
            )
            /* else if (!Pattern.compile(PASSWORD_PATTERN).matcher(password).matches())
                 ValidationErrorModel(R.string.invalid_password, ValidationError.PASSWORD)*/
            else -> null
        }
    }

    fun validateCurrentPassword(password: String?, msg: Int = R.string.blank_current_password): ValidationErrorModel? {
        return when {
            isBlank(password) -> ValidationErrorModel(msg, ValidationError.PASSWORD)
            password!!.length < 6 -> ValidationErrorModel(R.string.invalid_current_password,
                ValidationError.PASSWORD
            )
            /* else if (!Pattern.compile(PASSWORD_PATTERN).matcher(password).matches())
                 ValidationErrorModel(R.string.invalid_password, ValidationError.PASSWORD)*/
            else -> null
        }
    }

    fun validateNewPassword(password: String?, msg: Int = R.string.blank_new_password): ValidationErrorModel? {
        return when {
            isBlank(password) -> ValidationErrorModel(msg, ValidationError.PASSWORD)
            password!!.length < 6 -> ValidationErrorModel(R.string.invalid_new_password,
                ValidationError.PASSWORD
            )
            /* else if (!Pattern.compile(PASSWORD_PATTERN).matcher(password).matches())
                 ValidationErrorModel(R.string.invalid_password, ValidationError.PASSWORD)*/
            else -> null
        }
    }

    fun validateConfirmPassword(password: String?, confirmPassword: String?): ValidationErrorModel? {
        return when {
            isBlank(confirmPassword) -> ValidationErrorModel(R.string.blank_confirm_password,
                ValidationError.CONFIRM_PASSWORD
            )
            password != confirmPassword -> ValidationErrorModel(R.string.invalid_confirm_password,
                ValidationError.CONFIRM_PASSWORD
            )
            else -> null
        }
    }

    fun validateTitle(title: String?): ValidationErrorModel? {
        return if (isBlank(title))
            ValidationErrorModel(R.string.blank_title, ValidationError.FIRST_NAME)
        else
            null
    }

    fun validateDescription(desc: String?): ValidationErrorModel? {
        return if (isBlank(desc))
            ValidationErrorModel(R.string.blank_desc, ValidationError.DESC)
        else
            null
    }

    fun validateLocation(address: String?, lat: String, long: String): ValidationErrorModel? {
        return if (isBlank(address) || isBlank(lat) || isBlank(long) || lat == "0.0" || long == "0.0")
            ValidationErrorModel(R.string.blank_address, ValidationError.DATA)
        else
            null
    }

    fun validateAssignTo(assignId: Int): ValidationErrorModel? {
        return if (assignId == 0)
            ValidationErrorModel(R.string.blank_assign_to, ValidationError.DATA)
        else
            null
    }

     fun isBlank(text: String?): Boolean {
        return text == null || text.trim().isEmpty()
    }

    fun validateData(data: String): ValidationErrorModel? {
        return if (isBlank(data)) ValidationErrorModel(R.string.blank_data, ValidationError.DATA) else null
    }

    fun validateTelephone(phone: String): ValidationErrorModel? {
        return when {
            isBlank(phone) -> ValidationErrorModel(R.string.blank_phone, ValidationError.PHONE)
            phone.length !in 10..15 -> ValidationErrorModel(R.string.invalid_phone,
                ValidationError.PHONE
            )
            else -> null
        }
    }

    /*fun validateUserName(userName: String?): ValidationErrorModel? {
        return if (isBlank(userName))
            ValidationErrorModel(R.string.blank_username, ValidationError.USERNAME)
        // else if (!Pattern.compile(EMAIL_PATTERN).matcher(userName).matches())
        //    ValidationErrorModel(R.string.invalid_email, ValidationError.USERNAME)
        else
            null
    }*/

    fun isBlank(editText: EditText): Boolean {
        return editText.text == null || editText.text.trim().isEmpty()
    }

    fun validateData(context: Context, str: String, strKey: String): String {
        return if (isBlank(str)) {
            context.resources.getString(R.string.invalid) + strKey
        } else ""
    }

    fun validateNumber(strNumber: String, min: Int, max: Int): Boolean {
        return strNumber.length in min..max
    }

    fun validateAllFields(data: ArrayList<String>): ValidationErrorModel? {
        for (str in data) {
            if (isBlank(str)) {
                return@validateAllFields ValidationErrorModel(R.string.msg_all_field_required,
                    ValidationError.DATA
                )
            }
        }
        return null
    }
}