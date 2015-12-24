package edu.usc.imsc.sbus;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.List;

/**
 * Created by romil93 on 18/12/15.
 */

public class HubsAdapter extends ArrayAdapter<Hub> {

    private class ViewHolder {
        TextView stopName;
        TextView stopTime;
    }

    private Context context;

    public HubsAdapter(Context context, int resource, List<Hub> hubs) {
        super(context, resource, hubs);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder = null;
        Hub hub = getItem(position);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.stop_info, null);
            holder = new ViewHolder();
            holder.stopName = (TextView) convertView.findViewById(R.id.selected_stop_name);
            holder.stopTime = (TextView) convertView.findViewById(R.id.selected_stop_time);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.stopName.setText(hub.id);
        holder.stopTime.setText(hub.arrivalTime);

        return convertView;
    }
}
