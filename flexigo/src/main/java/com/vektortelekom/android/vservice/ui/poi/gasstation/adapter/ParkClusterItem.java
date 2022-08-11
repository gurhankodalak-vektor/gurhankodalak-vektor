package com.vektortelekom.android.vservice.ui.poi.gasstation.adapter;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.vektor.ktx.utils.map.VClusterItem;

public class ParkClusterItem extends VClusterItem implements Cloneable {

    private BitmapDescriptor selectedIcon;
    //    private Bitmap selectedIconBitmap;
    //private Bitmap iconBitmap;
    private boolean selected;

    public ParkClusterItem(double latitude, double longitude, String title, String subtitle, String image) {
        super(latitude, longitude, title, subtitle, image);
    }

    public ParkClusterItem(double latitude, double longitude, String title, String subtitle, String image, BitmapDescriptor icon) {
        super(latitude, longitude, title, subtitle, image, icon);
    }

    public ParkClusterItem(LatLng latLng, String assetIdentity, String address, BitmapDescriptor icon) {
        super(latLng, assetIdentity, address, icon);
    }

    public ParkClusterItem(double lat, double lon, String title, String subtitle) {
        super(lat, lon, title, subtitle);
    }

    public BitmapDescriptor getSelectedIcon() {
        return selectedIcon;
    }

    public void setSelectedIcon(BitmapDescriptor selectedIcon) {
        this.selectedIcon = selectedIcon;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }


//    public Bitmap getSelectedIconBitmap() {
//        return selectedIconBitmap;
//    }
//
//    public void setSelectedIconBitmap(Bitmap selectedIconBitmap) {
//        this.selectedIconBitmap = selectedIconBitmap;
//    }
//
//    public Bitmap getIconBitmap() {
//        return iconBitmap;
//    }
//
//    public void setIconBitmap(Bitmap iconBitmap) {
//        this.iconBitmap = iconBitmap;
//    }

    public Object clone() {
        return super.clone();
    }
}
