package com.example.centus;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

public class GroupsExpandableListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<String> userNames; // Lista użytkowników (Rodzic)
    private Map<String, List<Map<String, Object>>> userDebts; // Długi użytkowników (Dziecko)

    public GroupsExpandableListAdapter(Context context, List<String> userNames, Map<String, List<Map<String, Object>>> userDebts) {
        this.context = context;
        this.userNames = userNames;
        this.userDebts = userDebts;
    }

    @Override
    public int getGroupCount() {
        return userNames.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        String userId = userNames.get(groupPosition);
        return userDebts.get(userId).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return userNames.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        String userId = userNames.get(groupPosition);
        return userDebts.get(userId).get(childPosition);
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
        String userName = (String) getGroup(groupPosition);
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(android.R.layout.simple_expandable_list_item_1, parent, false);
        }
        TextView textView = convertView.findViewById(android.R.id.text1);
        textView.setText(userName);
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        Map<String, Object> debt = (Map<String, Object>) getChild(groupPosition, childPosition);
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_2, parent, false);
        }
        TextView text1 = convertView.findViewById(android.R.id.text1);
        TextView text2 = convertView.findViewById(android.R.id.text2);

        text1.setText(debt.get("name").toString());
        text2.setText(debt.get("amount").toString() + " zł");
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}

