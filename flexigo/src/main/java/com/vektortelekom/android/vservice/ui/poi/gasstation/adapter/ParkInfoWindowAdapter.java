package com.vektortelekom.android.vservice.ui.poi.gasstation.adapter;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.vektortelekom.android.vservice.R;

public class ParkInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private View myContentsView;

    public ParkInfoWindowAdapter(Activity activity) {
        myContentsView = activity.getLayoutInflater().inflate(R.layout.map_info_window_park, null);
    }

    @Override
    public View getInfoWindow(Marker marker) {
        TextView tvTitle = ((TextView) myContentsView.findViewById(R.id.textView1));
        //TextView tvSnippet = ((TextView) myContentsView.findViewById(com.vektortelekom.common.R.id.textView2));

        Object tag = marker.getTag();
        if (tag != null) {
//            CarDetailModel model = (CarDetailModel) tag;
//            Integer distance = model.distance.distanceInMeters;
//            String value = "";
//            if (distance != null) {
//                if (distance >= 1000) {
//                    BigDecimal km = new BigDecimal(distance).divide(new BigDecimal(1000), 1, RoundingMode.UP);
//                    value = km.toPlainString();
//                    if (value.endsWith(".0")) {
//                        value = value.substring(0, value.length() - 2);
//                    }
//                    value = value + " km";
//                } else {
//                    value = distance + " m";
//                }
            //}

            //tvTitle.setText(value);
        }
//        if (selectedItemId != null) {
//            tvTitle.setText(selectedItemId.getTitle());
//            tvSnippet.setText(selectedItemId.getSubtitle());
//            if (selectedItemId.getImage() != null) {
//                setUrl(selectedItemId.getImage());
//            }
//        }

        return null;
    }

    public View getInfoContents(Marker marker) {
        return null;
    }

}