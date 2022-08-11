package com.vektortelekom.android.vservice.ui.poi.gasstation.adapter;

import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.vektor.ktx.utils.map.VClusterItem;

public class ParkClusterRenderer extends DefaultClusterRenderer<VClusterItem> {

    private int icon;

    public ParkClusterRenderer(Context context, GoogleMap map, ClusterManager<VClusterItem> clusterManager) {
        super(context, map, clusterManager);
        // show clustered item count if size is greater than x
        this.setMinClusterSize(300);
        icon = com.vektor.ktx.R.drawable.carpin;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public void showInfoWindow(VClusterItem item) {
        Marker marker = getMarker(item);
        if (marker != null) {
            //marker.showInfoWindow();
        }
    }

    @Override
    protected void onBeforeClusterItemRendered(VClusterItem item, MarkerOptions markerOptions) {
        if (item != null && item.getIcon() != null) {
            markerOptions.icon(item.getIcon());
        } else {
            markerOptions.icon(BitmapDescriptorFactory.fromResource(icon));
        }

        if (item instanceof ParkClusterItem) {
            ParkClusterItem item2 = (ParkClusterItem) item;
            if (item2.isSelected()) {
                markerOptions.icon(item2.getSelectedIcon());
                //markerOptions.zIndex(100.0f);
            } else {
                //markerOptions.zIndex(0.0f);
            }
        }

        if (item != null && item.getSubtitle() != null) {
            markerOptions.snippet(item.getSubtitle());
        }

        markerOptions.anchor(0.5f, 0.7f);

        super.onBeforeClusterItemRendered(item, markerOptions);
    }

    @Override
    protected void onClusterItemRendered(VClusterItem clusterItem, final Marker marker) {
        super.onClusterItemRendered(clusterItem, marker);

        // copy tag from cluster item to marker
        marker.setTag(clusterItem.getTag());

        if (clusterItem instanceof ParkClusterItem) {
            ParkClusterItem item2 = (ParkClusterItem) clusterItem;
            if (item2.isSelected()) {
                //marker.showInfoWindow();
//                final Bitmap bitmap = BitmapFactory. item2.getSelectedIcon();
//                final Bitmap target = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
//                final Canvas canvas = new Canvas(target);
//                ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
//                animator.setDuration(500);
//                animator.setStartDelay(1000);
//                final Rect originalRect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
//                final RectF scaledRect = new RectF();
//                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//                    @Override
//                    public void onAnimationUpdate(ValueAnimator animation) {
//                        float scale = (float) animation.getAnimatedValue();
//                        scaledRect.set(0, 0, originalRect.right * scale, originalRect.bottom * scale);
//                        canvas.drawBitmap(bitmap, originalRect, scaledRect, null);
//                        marker.setIcon(BitmapDescriptorFactory.fromBitmap(target));
//                    }
//                });
//                animator.start();

            }
        }
    }
}

