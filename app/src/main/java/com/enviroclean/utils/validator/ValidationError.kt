package com.enviroclean.utils.validator

/**
 * Created by Darshna Desai on 28/3/18.
 */


/**
 * An ENUM class which declares different types of validation error constants
 * , which needs to be passed in ValidationError model from the presenter to the activity.
 */
enum class ValidationError {
    EMAIL,
    FIRST_NAME,
    LAST_NAME,
    DOB,
    GENDER,
    PASSWORD,
    CONFIRM_PASSWORD,
    PHONE,
    DATA,
    DESC,
    EMAIL_OR_CELLPHONE,

    NAME,
    BILLING_ADDRESS,
    BILLING_CITY,
    BILLING_STATE,
    BILLING_ZIPCODE,
    SHIPING_ADDRESS,
    SHIPING_CITY,
    SHIPING_STATE,
    SHIPING_ZIPCODE,
    CARD_HOLDER_NAME,
    CARD_NUMBER,
    CARD_EXPIRATION_DATE,
    CVV,
    SUBJECT,
    MESSAGE
}