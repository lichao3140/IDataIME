package com.idata.bluetoothime;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class IDataSettingActivity extends Activity implements OnClickListener {
	static final String TAG = IDataSettingActivity.class.getSimpleName();
	
	private TextView tv_Title, tv_ConnectStatus;
	private Button bt_Activation, bt_Open, bt_Select, bt_Change, bt_Test, bt_Back;
	// ϵͳ��������
	private InputMethodManager mImm;
	private boolean mNeedsToAdjustStepNumberToSystemState;
	// ��������������
	private BluetoothAdapter mBluetoothAdapter = null;
	// ��Ա�����������
	private static BluetoothService mChatService = null;
	// �����豸������
	private String mConnectedDeviceName = null;
	//private ArrayAdapter<String> mConversationArrayAdapter;
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";
	
	private static final int REQUEST_CONNECT_DEVICE = 1;
	private static final int REQUEST_ENABLE_BT = 2;
	
	// ��BluetoothChatService���ʹ���������Ϣ����
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting_idata);
		
		mImm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
		init();
		// ��ȡ��������������
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		// �ж������Ƿ����
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "�����ǲ����õ�", Toast.LENGTH_LONG).show();
			finish();
			return;
		}
	}

	private void init() {
		tv_Title = (TextView) findViewById(R.id.tvTitle);
		tv_ConnectStatus = (TextView) findViewById(R.id.tv_connect_status);
		tv_Title.setText(R.string.setting);
		tv_ConnectStatus.setText(R.string.StopBluetooth);
		bt_Activation = (Button) findViewById(R.id.bt_activation);
		bt_Open = (Button) findViewById(R.id.bt_open);
		bt_Select = (Button) findViewById(R.id.bt_select);
		bt_Change = (Button) findViewById(R.id.bt_change);
		bt_Test = (Button) findViewById(R.id.bt_test);
		bt_Back = (Button) findViewById(R.id.bt_back);
		bt_Back.setText(R.string.ui_title_complete);
		
		bt_Activation.setOnClickListener(this);
		bt_Open.setOnClickListener(this);
		bt_Select.setOnClickListener(this);
		bt_Change.setOnClickListener(this);
		bt_Test.setOnClickListener(this);
		bt_Back.setOnClickListener(this);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		// �ж������Ƿ�򿪣���û���򵯳�������ʾ�������Ի���
		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
		} else {
			if (mChatService == null) {
				Log.e(TAG, "----���������������---");
				// ��ʼ��BluetoothChatService������������
				mChatService = new BluetoothService(this, mHandler);
			}
		}
	}
	
	/**
	 * Handler����BluetoothChatService��������Ϣ
	 */
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_STATE_CHANGE:
				Log.e(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
				switch (msg.arg1) {
				case BluetoothService.STATE_CONNECTED:
					tv_ConnectStatus.setText(R.string.StartBluetooth);
					tv_ConnectStatus.append(mConnectedDeviceName);
					//mConversationArrayAdapter.clear();
					break;
				case BluetoothService.STATE_CONNECTING:
					tv_ConnectStatus.setText(R.string.devoice_connecting);
					break;
				case BluetoothService.STATE_LISTEN:
				case BluetoothService.STATE_NONE:
					tv_ConnectStatus.setText(R.string.devoice_not_connected);
					break;
				}
				break;
			case MESSAGE_WRITE:
				byte[] writeBuf = (byte[]) msg.obj;
				String writeMessage = new String(writeBuf);
				//mConversationArrayAdapter.add("Me:  " + writeMessage);
				break;
			case MESSAGE_READ:
				byte[] readBuf = (byte[]) msg.obj;
				// ��ȡ��������
				String readMessage = new String(readBuf, 0, msg.arg1);
				//mConversationArrayAdapter.add(mConnectedDeviceName + ":  " + readMessage);
				Log.e("lichao", "�յ���Ϣ" + readMessage);
				break;
			case MESSAGE_DEVICE_NAME:
				// ���������豸������
				mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
				Toast.makeText(getApplicationContext(),
						"���ӵ�" + mConnectedDeviceName, Toast.LENGTH_SHORT)
						.show();
				break;
			case MESSAGE_TOAST:
				Toast.makeText(getApplicationContext(),
						msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
						.show();
				break;
			}
		}
	};

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.bt_activation:
			Toast.makeText(IDataSettingActivity.this, R.string.select_bluetoothIME, Toast.LENGTH_LONG).show();
			Intent input = new Intent(android.provider.Settings.ACTION_INPUT_METHOD_SETTINGS);	
			startActivity(input);
			break;
		case R.id.bt_open:
			Intent bluetooth = new Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
			startActivity(bluetooth);
			break;
		case R.id.bt_select:
			Intent select = new Intent(IDataSettingActivity.this, DeviceListActivity.class);
			startActivityForResult(select, REQUEST_CONNECT_DEVICE);
			break;
		case R.id.bt_change:
			mImm.showInputMethodPicker();
			mNeedsToAdjustStepNumberToSystemState = true;
			break;
		case R.id.bt_test:
			Intent test =new Intent();
			test.setClass(IDataSettingActivity.this, TestActivity.class);
			test.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(test);
			break;
		case R.id.bt_back:
			finish();
			break;
		default:
			break;
		}
	}
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus && mNeedsToAdjustStepNumberToSystemState) {
            mNeedsToAdjustStepNumberToSystemState = false;
        }
	}
	
	@Override
	protected synchronized void onResume() {
		super.onResume();
		Log.e(TAG, "----onResume()");
		if (mChatService != null) {
			if (mChatService.getState() == BluetoothService.STATE_NONE) {
				mChatService.startChat();
			}
		}
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.e(TAG, "onActivityResult " + resultCode);
		switch (requestCode) {
		case REQUEST_CONNECT_DEVICE:
			// ��DeviceListActivity�������豸���ӵ���Ϣ
			if (resultCode == Activity.RESULT_OK) {
				// �����豸��MAC��ַ
				String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
				// �õ���������
				BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
				// ��ʼ�����豸
				mChatService.connect(device);
			}
			break;
		case REQUEST_ENABLE_BT:
			// �ж������Ƿ�����
			if (resultCode == Activity.RESULT_OK) {
				// ��������
				mChatService = new BluetoothService(this, mHandler);
			} else {
				Log.e(TAG, "����δ����");
				Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
				finish();
			}
		}
	}
	
	/**
	 * ������Ϣ
	 * @param message ��Ϣ����
	 */
	public static void sendMessage(String message) {
		if (mChatService.getState() != BluetoothService.STATE_CONNECTED) {
			//Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
			return;
		}
		if (message.length() > 0) {
			Log.e("lichao", "������Ϣ" + message);
			byte[] send = message.getBytes();
			mChatService.write(send);
		}
	}
	
	@Override
	protected synchronized void onRestart() {
		super.onRestart();
		Log.e(TAG, "----onRestart()");
	}
	
	@Override
	protected synchronized void onPause() {
		super.onPause();
		Log.e(TAG, "----onPause()");
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		Log.e(TAG, "----onStop()");
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.e(TAG, "----onDestroy()");
	}
}
