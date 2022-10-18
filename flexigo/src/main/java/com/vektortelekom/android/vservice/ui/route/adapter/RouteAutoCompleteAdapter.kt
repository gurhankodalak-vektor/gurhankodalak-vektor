package com.vektortelekom.android.vservice.ui.route.adapter

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.model.LocationModel

class RouteAutoCompleteAdapter (private val mContext: Context,
                                private val mLayoutResourceId: Int,
                                list: List<AutocompletePrediction>):
    ArrayAdapter<AutocompletePrediction>(mContext, mLayoutResourceId, list),
    Filterable {

    private val list: MutableList<AutocompletePrediction> = ArrayList(list)

    override fun getCount(): Int {
        return list.size
    }
    override fun getItem(position: Int): AutocompletePrediction {
        return list[position]
    }
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        if (convertView == null) {
            val inflater = (mContext as Activity).layoutInflater
            convertView = inflater.inflate(mLayoutResourceId, parent, false)
        }
        try {
            val item: AutocompletePrediction = getItem(position)

            val addressTextview = convertView!!.findViewById<View>(R.id.text_view_address) as TextView
            val addressImageview = convertView.findViewById<View>(R.id.image_view_address) as ImageView

            addressTextview.text = item.getFullText(null)

            addressImageview.setImageResource(R.drawable.ic_marker)
            addressImageview.background = null

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return convertView!!
    }

    interface SearchItemClickListener {
        fun onItemClicked(model: LocationModel)
    }


}