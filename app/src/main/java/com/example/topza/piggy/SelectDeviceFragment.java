package com.example.topza.piggy;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import app.akexorcist.bluetoothspp.BluetoothState;
import app.akexorcist.bluetoothspp.DeviceList;


public class SelectDeviceFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;
    ListView listView1;
    HistoryListAdapter historyListAdapter;
    BluetoothAdapter mBtAdapter;
    DeviceAdapter adapter;
    SharedPreferences save_name_bluetooth;
    SharedPreferences.Editor save_edit;
    Set<String> save_list = new HashSet<>();
    Set<String> connect_save_list = new HashSet<>();
    String[] device_list;
    String vir_save_name = "";

    public static SelectDeviceFragment newInstance() {
        SelectDeviceFragment fragment = new SelectDeviceFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.select_device_fragment, container,false);

        save_name_bluetooth = getActivity().getSharedPreferences("save_bluetooth", Context.MODE_PRIVATE);
        save_list = save_name_bluetooth.getStringSet("save_list_bluetooth", new HashSet<String>());
        connect_save_list = save_name_bluetooth.getStringSet("save_connect_list_bluetooth", new HashSet<String>());
        save_edit = save_name_bluetooth.edit();

        initInstances(rootView);
        return rootView;
    }

    private void initInstances(View rootView) {
        device_list = new String[]{ "device 1", "device 2", "device 3"
                , "device 4", "device 5", "device 6", "device 7"
                , "device 8", "device 9", "device 10"
                , "device 11" };

        listView1 = (ListView)rootView.findViewById(R.id.fragmentDeviceListView);
        adapter = new DeviceAdapter();
        adapter.setDevice_name(device_list);
        adapter.setSave_name(save_list.toArray(new String[save_list.size()]));
        listView1.setAdapter(adapter);
        listView1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HomeActivity.bt.autoConnect(connect_save_list.toArray(new String[connect_save_list.size()])[position]);
                Toast.makeText(getActivity().getApplicationContext(), connect_save_list.toArray(new String[connect_save_list.size()])[position]
                        , Toast.LENGTH_SHORT).show();
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        Button add_new_device = (Button)rootView.findViewById(R.id.btnAddConnect);
        add_new_device.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                if(HomeActivity.bt.getServiceState() == BluetoothState.STATE_CONNECTED) {
                    HomeActivity.bt.disconnect();
                } else {
                    receiveUserBluetoothDialog((AppCompatActivity) getActivity(), "New Bluetooth Connection",
                            "Please Input your Bluetooth connect device name.", "Connect");
                }
//                Toast.makeText(getActivity().getApplicationContext(), "5678"
//                        , Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
            if (resultCode == Activity.RESULT_OK){
                if(connect_save_list.contains(DeviceList.name_device)){
                    checkConsistencyOfBluetooth((AppCompatActivity) getActivity(), "Bluetooth create error",
                            "This connection has been register already.", "OK");
                } else{
                    HomeActivity.bt.connect(data);
                    connect_save_list.add(DeviceList.name_device);
                    save_edit.putStringSet("save_connect_list_bluetooth", connect_save_list).commit();
                    save_list.add(vir_save_name);
                    save_edit.putStringSet("save_list_bluetooth", save_list).commit();
                    adapter.setSave_name(save_list.toArray(new String[save_list.size()]));
                    adapter.setDevice_name(device_list);
                    adapter.notifyDataSetChanged();
                }
            }
        } else if (requestCode == BluetoothState.REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                HomeActivity.bt.setupService();
                HomeActivity.bt.startService(BluetoothState.DEVICE_ANDROID);
                Intent intent = new Intent(getActivity().getApplicationContext(), DeviceList.class);
                startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
            } else {
                Toast.makeText(getActivity().getApplicationContext(), "Bluetooth was not enabled."
                        , Toast.LENGTH_SHORT).show();
                getActivity().finish();
            }
        }
    }

    private AlertDialog receiveUserBluetoothDialog(final AppCompatActivity act, CharSequence title,
                                                   CharSequence message, CharSequence buttonYes){
        AlertDialog.Builder downloadDialog = new AlertDialog.Builder(act);
        final EditText bluetooth_name = new EditText(getContext());
        bluetooth_name.setInputType(InputType.TYPE_CLASS_TEXT);
        downloadDialog.setView(bluetooth_name);
        downloadDialog.setTitle(title).setMessage(message).setPositiveButton(buttonYes, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(getActivity().getApplicationContext(), DeviceList.class);
                startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
//                HomeActivity.bt.autoConnect(bluetooth_name.getText().toString());
                vir_save_name = bluetooth_name.getText().toString();
            }
        });
        return downloadDialog.show();
    }

    private AlertDialog checkConsistencyOfBluetooth(final AppCompatActivity act, CharSequence title,
                                                   CharSequence message, CharSequence buttonYes){
        AlertDialog.Builder downloadDialog = new AlertDialog.Builder(act);
        downloadDialog.setTitle(title).setMessage(message).setPositiveButton(buttonYes, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(getActivity().getApplicationContext(), DeviceList.class);
                startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
            }
        });
        return downloadDialog.show();
    }
}
