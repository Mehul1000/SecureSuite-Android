package com.nuvolect.securesuite.main;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.nuvolect.securesuite.R;
import com.nuvolect.securesuite.data.MyGroups;
import com.nuvolect.securesuite.data.SqlCipher.GTTab;
import com.nuvolect.securesuite.util.AppTheme;

public class GroupListCursorAdapter extends CursorAdapter {

//    private static final boolean DEBUG = false;
    private final LayoutInflater m_inflater;
    private final int m_layout;
    protected ListView mListView;
    private static int m_h1_color;
    private static int m_h2_color;
    private static int m_title_index;
    private static int m_group_id_index;

    public GroupListCursorAdapter(Context context, Cursor c, int flags, int layout) {
        super(context, c, flags);

        this.m_layout = layout;
        this.m_inflater=LayoutInflater.from(context);

        // Save cursor index for bindView reuse
        m_title_index = c.getColumnIndex( GTTab.title.toString());
        m_group_id_index = c.getColumnIndex( GTTab.group_id.toString());

        m_h1_color = AppTheme.getThemeColor(context, R.attr.h1_color);
        m_h2_color = AppTheme.getThemeColor(context, R.attr.h2_color);

//        if(DEBUG) {
//            Util.dumpCursorDescription( c, "getTitleCursor");
//            c.moveToPosition(-1);
//
//            while( c.moveToNext()){
//
//                String groupName = c.getString( c.getColumnIndex(GTTab.title.toString()) );
//                String account = c.getString( c.getColumnIndex(GTTab.account_name.toString()) );
//                int group_id = c.getInt( c.getColumnIndex(GTTab.group_id.toString()) );
//                LogUtil.log(account+", id: "+group_id+", title: "+groupName);
//            }
//        }
    }

    @Override
    public void bindView(View view, Context m_act, Cursor cursor) {

        TextView nameTv = (TextView) view.findViewById(R.id.group_name);
        nameTv.setText(cursor.getString( m_title_index ));
        nameTv.setTextColor( m_h1_color);

        int group_id = cursor.getInt( m_group_id_index);
        TextView countTv = (TextView) view.findViewById(R.id.count_in_group);

        int count = MyGroups.mGroupCount.get(group_id);
        if( count == 1)
            countTv.setText("1 person");
        else
            countTv.setText(MyGroups.mGroupCount.get(group_id)+" people");
        countTv.setTextColor( m_h2_color);
    }

    @Override
    public View newView(Context m_act, Cursor cursor, ViewGroup parent) {

        return m_inflater.inflate( m_layout, null);
    }
}
