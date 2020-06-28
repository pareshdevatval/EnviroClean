package com.enviroclean.utils.validator


/**
 * Created by Darshna Desai on 28/3/18.
 */


/**
 * A data class which needs to be passed with error msg and error constant
 * from the presenter to the activity, whenever an error occurs.
 */
data class ValidationErrorModel(val msg: Int, val error: ValidationError)