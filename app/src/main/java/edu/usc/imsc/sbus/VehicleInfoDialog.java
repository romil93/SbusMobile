package edu.usc.imsc.sbus;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

/**
 * Created by danielCantwell on 4/8/15.
 */
public class VehicleInfoDialog extends DialogFragment {

    private Vehicle mVehicle;

    public VehicleInfoDialog() {

    }

    public void setVehicle(Vehicle v) {
        mVehicle = v;
    }

    private TextView vehicleName;
    private TextView stopName;
    private TextView stopTime;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.vehicle_info, null);

        vehicleName = (TextView) view.findViewById(R.id.vehicle_name);
        stopName = (TextView) view.findViewById(R.id.stop_name);
        stopTime = (TextView) view.findViewById(R.id.stop_time);

        vehicleName.setText(mVehicle.stopHeadsign);
        stopName.setText(mVehicle.stops.get(mVehicle.nextStop).name);
        stopTime.setText(mVehicle.stops.get(mVehicle.nextStop).arrivalTime);

        builder.setView(view);
        return builder.create();
    }
}