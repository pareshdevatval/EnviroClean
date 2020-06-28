package com.enviroclean.customeview

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import androidx.appcompat.widget.AppCompatImageView


/*An imageView with Ripple effect when click on it */
class RippleEffectImageView: AppCompatImageView {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        /*Adding ripple effect as background*/
        val outValue = TypedValue()
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            context.theme.resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, outValue, true)
        } else {
            context.theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
        }
        setBackgroundResource(outValue.resourceId)
        isClickable = true
        isFocusable = true
    }
    constructor(context: Context, attrs: AttributeSet, styleId: Int) : super(context, attrs, styleId)
}