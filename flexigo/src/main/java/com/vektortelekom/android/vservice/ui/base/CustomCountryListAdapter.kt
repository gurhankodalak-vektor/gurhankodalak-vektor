package com.vektortelekom.android.vservice.ui.base

import com.vektortelekom.android.vservice.data.model.CountryCodeResponseListModel
import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filterable
import android.widget.TextView
import com.vektortelekom.android.vservice.R

class CustomCountryListAdapter (private val mContext: Context,
                                private val mLayoutResourceId: Int,
                                list: List<CountryCodeResponseListModel>):
    ArrayAdapter<CountryCodeResponseListModel>(mContext, mLayoutResourceId, list),
    Filterable {

    private val list: MutableList<CountryCodeResponseListModel> = ArrayList(list)

    override fun getCount(): Int {
        return list.size
    }
    override fun getItem(position: Int): CountryCodeResponseListModel {
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
            val item: CountryCodeResponseListModel = getItem(position)

            val textView = convertView!!.findViewById<View>(R.id.textView) as TextView
//            val addressImageview = convertView.findViewById<View>(R.id.image_view_address) as ImageView

            val shortCode = item.shortCode
            textView.text = shortCode.plus("  + ".plus(item.areaCode))

//            addressImageview.setImageResource(R.drawable.ic_marker)
//            addressImageview.background = null

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return convertView!!
    }

    interface SearchItemClickListener {
        fun onItemClicked(model: CountryCodeResponseListModel)
    }


}

