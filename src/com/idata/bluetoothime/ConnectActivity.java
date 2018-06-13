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
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @description:����Ϊ������������������ ���շ���������Ϣ����������ͨ�����ӡ���ӡ������Ϣ
 */
public class ConnectActivity extends Activity implements OnClickListener {
	private final String TAG = "ConnectActivity";
	// ��BluetoothChatService���ʹ���������Ϣ����
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;

	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";

	private static final int REQUEST_CONNECT_DEVICE = 1;
	private static final int REQUEST_ENABLE_BT = 2;

	private TextView mTitle;
	private ListView mConversationView;
	private Button mSendButton;
	private Button mClearButton;

	// �����豸������
	private String mConnectedDeviceName = null;
	private ArrayAdapter<String> mConversationArrayAdapter;

	// ��������������
	private BluetoothAdapter mBluetoothAdapter = null;
	// ��Ա�����������
	private static BluetoothService mChatService = null;
	private Button btn_connect, btn_discover;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_connect);
		mTitle = (TextView) findViewById(R.id.title_left_text);
		mTitle.setText(R.string.app_name);
		mTitle = (TextView) findViewById(R.id.title_right_text);
		btn_connect = (Button) findViewById(R.id.btn_connect);
		btn_discover = (Button) findViewById(R.id.btn_discover);
		btn_connect.setOnClickListener(this);
		btn_discover.setOnClickListener(this);
		// ��ȡ��������������
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		// �ж������Ƿ����
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "�����ǲ����õ�", Toast.LENGTH_LONG).show();
			finish();
			return;
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		// �ж������Ƿ�򿪣���û���򵯳�������ʾ�������Ի���
		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
		} else {
			if (mChatService == null) {
				Log.e(TAG, "----���������������---");
				setupChat();
			}
		}
	}

	@Override
	public synchronized void onResume() {
		super.onResume();
		Log.e(TAG, "----onResume()");
		if (mChatService != null) {
			if (mChatService.getState() == BluetoothService.STATE_NONE) {
				mChatService.startChat();
			}
		}
	}

	private EditText edt;

	/**
	 * ������Ҫ��һЩ����
	 */
	private void setupChat() {
		mConversationArrayAdapter = new ArrayAdapter<String>(this,
				R.layout.message);
		mConversationView = (ListView) findViewById(R.id.in);
		mConversationView.setAdapter(mConversationArrayAdapter);
		mSendButton = (Button) findViewById(R.id.button_send);
		mClearButton = (Button) findViewById(R.id.button_clear);
		edt = (EditText) findViewById(R.id.edit_text_out);
		mSendButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				String message = edt.getText().toString();
				sendMessage(message);
				edt.setText("");
				//sendMessage(BluetoothConstant.BLUETOOTH_END_NONE);
			}
		});
		mClearButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mConversationArrayAdapter.clear();
			}
		});
		// ��ʼ��BluetoothChatService������������
		mChatService = new BluetoothService(this, mHandler);
	}

	/**
	 * ������Ϣ
	 * 
	 * @param message
	 *            ���͵�����
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

	// ��Handler����BluetoothChatService��������Ϣ
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_STATE_CHANGE:
				Log.e(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
				switch (msg.arg1) {
				case BluetoothService.STATE_CONNECTED:
					mTitle.setText(R.string.devoice_connected_to);
					mTitle.append(mConnectedDeviceName);
					mConversationArrayAdapter.clear();
					break;
				case BluetoothService.STATE_CONNECTING:
					mTitle.setText(R.string.devoice_connecting);
					break;
				case BluetoothService.STATE_LISTEN:
				case BluetoothService.STATE_NONE:
					mTitle.setText(R.string.devoice_not_connected);
					break;
				}
				break;
			case MESSAGE_WRITE:
				byte[] writeBuf = (byte[]) msg.obj;
				String writeMessage = new String(writeBuf);
				mConversationArrayAdapter.add("Me:  " + writeMessage);
				break;
			case MESSAGE_READ:
				byte[] readBuf = (byte[]) msg.obj;
				// ��ȡ��������
				String readMessage = new String(readBuf, 0, msg.arg1);
				mConversationArrayAdapter.add(mConnectedDeviceName + ":  "
						+ readMessage);
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

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.e(TAG, "onActivityResult " + resultCode);
		switch (requestCode) {
		case REQUEST_CONNECT_DEVICE:
			// ��DeviceListActivity�������豸���ӵ���Ϣ
			if (resultCode == Activity.RESULT_OK) {
				// �����豸��MAC��ַ
				String address = data.getExtras().getString(
						DeviceListActivity.EXTRA_DEVICE_ADDRESS);
				// �õ���������
				BluetoothDevice device = mBluetoothAdapter
						.getRemoteDevice(address);
				// ��ʼ�����豸
				mChatService.connect(device);
			}
			break;
		case REQUEST_ENABLE_BT:
			// �ж������Ƿ�����
			if (resultCode == Activity.RESULT_OK) {
				// ��������
				setupChat();
			} else {
				Log.e(TAG, "����δ����");
				Toast.makeText(this, R.string.bt_not_enabled_leaving,
						Toast.LENGTH_SHORT).show();
				finish();
			}
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_connect:
			// �����豸
			Intent serverIntent = new Intent(this, DeviceListActivity.class);
			startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
			break;
		case R.id.btn_discover:
			// ���������豸
			ensureDiscoverable();
			break;

		default:
			break;
		}
	}

	/**
	 * �����豸������
	 */
	private void ensureDiscoverable() {
		Log.e(TAG, "----��������");
		if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
			Intent discoverableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			discoverableIntent.putExtra(
					BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
			startActivity(discoverableIntent);
		}
	}

	@Override
	public synchronized void onPause() {
		super.onPause();
		Log.e(TAG, "----onPause()");
	}

	@Override
	public void onStop() {
		super.onStop();
		Log.e(TAG, "----onStop()");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// ֹͣ����
//		if (mChatService != null)
//			mChatService.stop();
		Log.e(TAG, "----onDestroy()");
	}

}