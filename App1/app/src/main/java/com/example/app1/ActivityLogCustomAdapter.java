package com.example.app1;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.media.ThumbnailUtils;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.List;

/**
 * Created by mithileshhinge on 14/10/17.
 */
public class ActivityLogCustomAdapter extends BaseExpandableListAdapter{
    private Context _context;
    private List<String> _listDataHeader; // header titles
    // child data in format of header title, child title
    private HashMap<String, List<DatabaseRow>> _listDataChild;

    public ActivityLogCustomAdapter(Context context, List<String> listDataHeader, HashMap<String, List<DatabaseRow>> listDataChild) {
        this._context = context;
        this._listDataHeader = listDataHeader;
        this._listDataChild = listDataChild;
    }

    @Override
    public int getGroupCount() {
        return this._listDataHeader.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this._listDataChild.get(this._listDataHeader.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this._listDataHeader.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return this._listDataChild.get(this._listDataHeader.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String headerTitle = (String) getGroup(groupPosition);
        if (convertView == null){
            LayoutInflater inflater = (LayoutInflater) this._context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.activitylog_group, null);
        }

        TextView jTVHeader = (TextView) convertView.findViewById(R.id.xGroupHeader);
        jTVHeader.setTypeface(null, Typeface.BOLD);
        jTVHeader.setText(headerTitle);

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final DatabaseRow childDataItem = (DatabaseRow) getChild(groupPosition, childPosition);

        if (convertView == null){
            LayoutInflater inflater = (LayoutInflater) this._context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.activitylog_item, null);
        }

        TextView jTVActivityName = (TextView) convertView.findViewById(R.id.xLogName);
        jTVActivityName.setText(childDataItem.getName());

        TextView jTVTime = (TextView) convertView.findViewById(R.id.xLogTime);
        String dateTime = childDataItem.getDateTime();
        System.out.println("LOG CUSTOM ADAPTER STRING DATE TIME :" + dateTime + " " + dateTime.length());
        jTVTime.setText(dateTime.substring(12,14) + ":" + dateTime.substring(15,17) + ":" + dateTime.substring(18,20) + " " + dateTime.substring(21,23));

        ImageView jIVThumb = (ImageView) convertView.findViewById(R.id.xIVLogImg);
        Bitmap getImg = getImageBitmap(_context, childDataItem.getThumbpath());
        Bitmap thumbnailImg = ThumbnailUtils.extractThumbnail(getImg, 60, 60);
        jIVThumb.setImageBitmap(thumbnailImg);
        /*getImg.recycle();
        getImg = null;
        thumbnailImg.recycle();
        thumbnailImg = null;*/

        ToggleButton jTBActivityLogBkmrk = (ToggleButton) convertView.findViewById(R.id.xActivityLogBkrmrk);
        System.out.println("Value from db:" + String.valueOf(childDataItem.isBookmarked()));
        jTBActivityLogBkmrk.setChecked(childDataItem.isBookmarked());

        jTBActivityLogBkmrk.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isPressed()) {
                    childDataItem.setBookmarked(isChecked);
                    NotifyService.db.updateRow(childDataItem);
                    System.out.println("CheckedChanged: " + String.valueOf(isChecked));
                }
            }
        });
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public Bitmap getImageBitmap(Context context, String name){
        name=name+".jpg";
        try{

            FileInputStream fis = new FileInputStream(new File(new File(Environment.getExternalStoragePublicDirectory("MagicEye"), "MagicEyePictures"), name));
            Bitmap b = BitmapFactory.decodeStream(fis);
            if(b!=null)
                System.out.println("....frame attached!....");
            fis.close();
            return b;
        }
        catch(Exception e){
        }
        return null;
    }




}
