package com.foxelbox.app.gui;

import android.content.Context;
import android.text.Spannable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.foxelbox.app.R;

public class CategoricListArrayAdapter extends ArrayAdapter<CategoricListArrayAdapter.ICategoricListItem> {
    private final LayoutInflater mInflater;

    public enum RowType {
        LIST_ITEM, HEADER_ITEM
    }

    public CategoricListArrayAdapter(Context context) {
        super(context, 0);
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getViewTypeCount() {
        return RowType.values().length;
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).getViewType();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getItem(position).getView(mInflater, convertView);
    }

    public interface ICategoricListItem {
        int getViewType();
        View getView(LayoutInflater inflater, View convertView);
    }

    public static class CategoricListHeader implements ICategoricListItem {
        private final String name;

        public CategoricListHeader(String name) {
            this.name = name;
        }

        @Override
        public int getViewType() {
            return RowType.HEADER_ITEM.ordinal();
        }

        @Override
        public View getView(LayoutInflater inflater, View convertView) {
            View view;
            if (convertView == null) {
                view = inflater.inflate(R.layout.list_categoric_header, null);
                // Do some initialization
            } else {
                view = convertView;
            }

            TextView text = (TextView) view.findViewById(R.id.list_categoric_header);
            text.setText(name);

            return view;
        }
    }

    public static class CategoricListItem implements ICategoricListItem {
        private final Spannable str1;

        public CategoricListItem(Spannable text1) {
            this.str1 = text1;
        }

        @Override
        public int getViewType() {
            return RowType.LIST_ITEM.ordinal();
        }

        @Override
        public View getView(LayoutInflater inflater, View convertView) {
            View view;
            if (convertView == null) {
                view = inflater.inflate(R.layout.list_item_normal, null);
                // Do some initialization
            } else {
                view = convertView;
            }

            TextView text = (TextView) view.findViewById(R.id.list_item_contents);
            text.setText(str1);

            return view;
        }

    }
}
