package com.example.app1;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.ThumbnailUtils;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CompoundButton;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
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
    public static List<DatabaseRow> deleteItems = new ArrayList<>();
    public static List<DatabaseRow> data;
    ActionMode mActionMode = null;
    public ExpandableListView expandableListView;

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
        int count = getChildrenCount(groupPosition);
        headerTitle = headerTitle.concat(" (" + count + ")");
        if (convertView == null){
            LayoutInflater inflater = (LayoutInflater) this._context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.activitylog_group, null);
        }

        TextView jTVHeader = (TextView) convertView.findViewById(R.id.xGroupHeader);
        jTVHeader.setTypeface(null, Typeface.BOLD);
        jTVHeader.setText(headerTitle);
        System.out.println("HEADER TITLE : " + headerTitle);
        return convertView;
    }

    @Override
    public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final DatabaseRow childDataItem = (DatabaseRow) getChild(groupPosition, childPosition);

        if (convertView == null){
            LayoutInflater inflater = (LayoutInflater) this._context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.activitylog_item, null);
        }


        TextView jTVActivityName = (TextView) convertView.findViewById(R.id.xLogName);
        jTVActivityName.setText(childDataItem.getName());

        TextView jTVTime = (TextView) convertView.findViewById(R.id.xLogTime);
        String dateTime = childDataItem.getDateTime();
        //System.out.println("LOG CUSTOM ADAPTER STRING DATE TIME :" + dateTime + " " + dateTime.length());
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
        //System.out.println("Value from db:" + String.valueOf(childDataItem.isBookmarked()));
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
        //convertView.setOnClickListener(onClickListener);
        //convertView.setOnLongClickListener(onLongClickListener);

        convertView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                System.out.println("LONG CLICK LISTENER");
                if (mActionMode == null)
                    mActionMode = view.startActionMode(mActionModeCallback);
                multi_select(childPosition, groupPosition);
                notifyDataSetChanged();
                return true;
            }
        });

        convertView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                System.out.println("ON CLICK LISTENER");
                if(mActionMode != null) {
                    multi_select(childPosition, groupPosition);
                    notifyDataSetChanged();
                }else
                    mActionMode = view.startActionMode(mActionModeCallback);
            }
        });

        // Highlight the selected child rows
        if(childDataItem.getStatus()){
            convertView.setBackgroundColor(Color.GRAY);
        }else
            convertView.setBackgroundColor(Color.WHITE);
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

    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MainActivity.toolbar.setVisibility(View.GONE);
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.contextual_menu, menu);
            System.out.println("..................on create madhe ghusla.....................");
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            System.out.println("..................on prepare madhe ghusla.....................");
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            System.out.println("..................on action item clicked......................");
            switch (item.getItemId()) {
                case R.id.item_delete:
                    AlertDialog diaBox = AskOption();
                    diaBox.show();
                    return true;
                default:
                    break;
            }

            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            if(mode != null) {
                mode.finish();
                System.out.println(".......hehe......");
            }
            mActionMode = null;
            int position;
            for(position=0; position<deleteItems.size(); position++) {
                deleteItems.get(position).setStatus(false);
            }
            deleteItems.clear();
            System.out.println("..................on destroy madhe ghusla.....................");
            MainActivity.toolbar.setVisibility(View.VISIBLE);
            ActivityLogCustomAdapter logCustomAdapter = new ActivityLogCustomAdapter(_context,_listDataHeader, _listDataChild);
            //logCustomAdapter.expandableListView = expandableListView;
            expandableListView.setAdapter(logCustomAdapter);
        }
    };

    public void multi_select(int childPosition , int groupPosition ) {
        System.out.println("..........multiselect madhe ghusla.............");
        final String date =(String) getGroup(groupPosition);
        System.out.println("....multiselect group Position = "+groupPosition);
        data = _listDataChild.get(date);
        if (mActionMode != null) {
            if(deleteItems.contains(data.get(childPosition))){
                deleteItems.remove(data.get(childPosition));
                data.get(childPosition).setStatus(false);
            }else{
                deleteItems.add(data.get(childPosition));
                data.get(childPosition).setStatus(true);
            }

            if (deleteItems.size() > 0)
                mActionMode.setTitle("" + deleteItems.size() + " items selected");
            else {
                mActionMode.setTitle("");
                mActionModeCallback.onDestroyActionMode(mActionMode);
                deleteItems.clear();
                MainActivity.toolbar.setVisibility(View.VISIBLE);
            }
            notifyDataSetChanged();
        }
    }

    private AlertDialog AskOption()
    {
        final AlertDialog myQuittingDialogBox =new AlertDialog.Builder(_context)
                //set message, title, and icon
                .setTitle("Delete")
                .setMessage("Delete "+ deleteItems.size()+ " items ?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        //your deleting code
                        int position;
                        System.out.println(deleteItems.size()+" items present");
                        for(position=0; position<deleteItems.size(); position++) {
                            DatabaseRow row = deleteItems.get(position);
                            NotifyService.db.deleteRow(row.getID());
                            System.out.println("...........file deleted...........");
                        }
                        mActionModeCallback.onDestroyActionMode(mActionMode);
                        notifyDataSetChanged();
                        deleteItems.clear();
                        dialog.dismiss();
                    }

                })
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mActionModeCallback.onDestroyActionMode(mActionMode);
                        deleteItems.clear();
                        dialog.dismiss();
                    }
                })
                .create();
        return myQuittingDialogBox;

    }
}
