package com.enviroclean.base


import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView

//Created by Paresh Devatval
/*Base class for Recyclerview ViewHolder*/
class FooterProgressViewHolder : RecyclerView.ViewHolder {
    /**
     * Called when mInputCharacter view has been clicked.
     *
     * @param v The view that was clicked.
     */

    var binding: ViewDataBinding

    constructor(binding: ViewDataBinding) : super(binding.root) {
        this.binding = binding
    }




}