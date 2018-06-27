package com.idata.bluetoothime;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * �������������������
 */
@SuppressLint("Instantiatable") 
public class BluetoothService {
	private final static String TAG = "BluetoothService";
	private static final boolean D = true;
	// ��������
	private static final String NAME = "BluetoothChat";
	// �Լ������뷨
	private static final String INPUT_MOTHOD = "com.idata.bluetoothime/.PinyinIME";

	// ������ģ��ɴ��ڵķ�������һ��Ψһ��UUID
	private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	
	private static BluetoothAdapter mAdapter;
	private final Handler mHandler;
	private AcceptThread mAcceptThread;
	private ConnectThread mConnectThread;
	private ConnectedThread mConnectedThread;
	private WatchServerSocketThread wsst;
	private int mState;
	PinyinIME ss = new PinyinIME();

	static BluetoothService bluetoothService;
	// ������ʾ��ǰ������״̬
	public static final int STATE_NONE = 0;
	public static final int STATE_LISTEN = 1;
	public static final int STATE_CONNECTING = 2;
	public static final int STATE_CONNECTED = 3;
	
    private int conn_status = 0;
    private boolean BleIsOKFlag = false;
    private boolean ServerSocketIsClose = false;
    private int Conn_Error_Num = 0;
    private int Error_Num = 0;
    private int Num = 30; //��Ϊÿ1���Ӽ��һ�Σ�2�ξ���2����
    private int Interval = 8000; //����ʱ���� 60S
	
	public BluetoothService(Context context, Handler handler) {
		mAdapter = BluetoothAdapter.getDefaultAdapter();
		mState = STATE_NONE;
		mHandler = handler;
	}

	/**
	 * ���õ�ǰ����������״̬
	 * @param state ����״̬
	 */
	private synchronized void setState(int state) {
		mState = state;
		// ֪ͨActivity����UI
		mHandler.obtainMessage(ConnectActivity.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
	}

	/**
	 * ���ص�ǰ����״̬
	 */
	public synchronized int getState() {
		return mState;
	}

	/**
	 * ��ʼ�������
	 */
	public void startChat() {
		if (D)
			Log.e(TAG, "start ����");

		if (mConnectThread != null) {
			mConnectThread.cancel();
			mConnectThread = null;
		}

		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}

		if (mAcceptThread == null) {
			mAcceptThread = new AcceptThread();
			mAcceptThread.start();
		}
		setState(STATE_LISTEN);
	}

	/**
	 * ����Զ���豸
	 * @param device ��������
	 */
	public synchronized void connect(BluetoothDevice device) {
		if (D) 
			Log.d(TAG, "connect to: " + device);
		if (mState == STATE_CONNECTING) {
			if (mConnectThread != null) {
				mConnectThread.cancel();
				mConnectThread = null;
			}
		}

		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}

		mConnectThread = new ConnectThread(device);
		mConnectThread.start();
		wsst = new WatchServerSocketThread();
		wsst.start();
		setState(STATE_CONNECTING);
	}

	/**
	 * ����ConnectedThread��ʼ����һ����������
	 */
	public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
		Log.e(TAG, "ȷ��������� ");

		// Cancel any thread attempting to make a connection
		if (mConnectThread != null) {
			mConnectThread.cancel();
			mConnectThread = null;
		}

		// Cancel any thread currently running a connection
		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}

		if (mAcceptThread != null) {
			mAcceptThread.cancel();
			mAcceptThread = null;
		}

		// Start the thread to connect with the given device
		mConnectedThread = new ConnectedThread(socket);
		mConnectedThread.start();
		BleIsOKFlag = true;
		// ���ӳɹ���֪ͨactivity
		Message msg = mHandler.obtainMessage(ConnectActivity.MESSAGE_DEVICE_NAME);
		Bundle bundle = new Bundle();
		bundle.putString(ConnectActivity.DEVICE_NAME, device.getName());
		msg.setData(bundle);
		mHandler.sendMessage(msg);
		setState(STATE_CONNECTED);
	}

	/**
	 * ֹͣ�����߳�
	 */
	public synchronized void stop() {
		if (D)
			Log.e(TAG, "---stop()");
		setState(STATE_NONE);
		if (mConnectThread != null) {
			mConnectThread.cancel();
			mConnectThread = null;
		}
		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}
		if (mAcceptThread != null) {
			mAcceptThread.cancel();
			mAcceptThread = null;
		}
	}
	
	public static void discoverDevice() {
		//�������ɨ�裬��ֹͣɨ�裬������ɨ��
		if (mAdapter.isDiscovering()) {
			mAdapter.cancelDiscovery();
		}
		mAdapter.startDiscovery();
	}
	
	//�õ���Ե��豸�б��������Ե��豸  
    public static void removePairDevice() {  
        if(mAdapter != null){  
            Set<BluetoothDevice> bondedDevices = mAdapter.getBondedDevices();  
            for(BluetoothDevice device : bondedDevices ){  
                    unpairDevice(device);  
            }  
        }  
    }  
  
    //����������BluetoothDevice.removeBondȡ���豸�����
    private static void unpairDevice(BluetoothDevice device) {
        try {
            Method m = device.getClass().getMethod("removeBond", (Class[]) null);
            m.invoke(device, (Object[]) null);
        } catch (Exception e) {
            Log.e(TAG, "unpairDevice" + e.getMessage());
        }
    }
    
	/**
	 * ������Ϣ
	 * @param message
	 */
	public void sendMessage(String message) {
		if (message.length() > 0) {
			byte[] send = message.getBytes();
			write(send);
		}
	}

	/**
	 * �Է�ͬ����ʽд��ConnectedThread
	 * 
	 * @param out
	 */
	public void write(byte[] out) {
		ConnectedThread r;
		synchronized (this) {
			if (mState != STATE_CONNECTED)
				return;
			r = mConnectedThread;
		}
		r.write(out);
	}

	/**
	 * �޷����ӣ�֪ͨActivity
	 */
	private void connectionFailed() {
		setState(STATE_LISTEN);
		Message msg = mHandler.obtainMessage(ConnectActivity.MESSAGE_TOAST);
		Bundle bundle = new Bundle();
		bundle.putString(ConnectActivity.TOAST, "�޷������豸");
		msg.setData(bundle);
		mHandler.sendMessage(msg);
	}

	/**
	 * �豸�Ͽ����ӣ�֪ͨActivity
	 */
	private void connectionLost() {
		ServerSocketIsClose = true;
		Message msg = mHandler.obtainMessage(ConnectActivity.MESSAGE_TOAST);
		Bundle bundle = new Bundle();
		bundle.putString(ConnectActivity.TOAST, "�豸�Ͽ�����");
		msg.setData(bundle);
		mHandler.sendMessage(msg);
	}

	/**
	 * �������������
	 */
	private class AcceptThread extends Thread {
		private final BluetoothServerSocket mmServerSocket;

		public AcceptThread() {
			BluetoothServerSocket tmp = null;
			try {
				tmp = mAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
			} catch (IOException e) {
				Log.e(TAG, "--��ȡsocketʧ��:" + e);
			}
			mmServerSocket = tmp;
		}

		public void run() {
			setName("AcceptThread");
			BluetoothSocket socket = null;
			while (mState != STATE_CONNECTED) {
				Log.e(TAG, "----accept-ѭ��ִ����-");
				try {
					socket = mmServerSocket.accept();
				} catch (IOException e) {
					Log.e(TAG, "accept() ʧ��" + e);
					break;
				}

				// ������ӱ�����
				if (socket != null) {
					synchronized (BluetoothService.this) {
						switch (mState) {
						case STATE_LISTEN:
						case STATE_CONNECTING:
							// ��ʼ�����߳�
							connected(socket, socket.getRemoteDevice());
							break;
						case STATE_NONE:
						case STATE_CONNECTED:
							// û��׼���û��Ѿ�����
							try {
								socket.close();
							} catch (IOException e) {
								Log.e(TAG, "���ܹر���Щ����" + e);
							}
							break;
						}
					}
				}
			}
			Log.e(TAG, "����mAcceptThread");
		}

		public void cancel() {
			Log.e(TAG, "ȡ�� " + this);
			try {
				mmServerSocket.close();
			} catch (IOException e) {
				Log.e(TAG, "�ر�ʧ��" + e);
			}
		}
	}

	/**
	 * @description:���������߳�
	 */
	private class ConnectThread extends Thread {
		private final BluetoothSocket mmSocket;
		private final BluetoothDevice mmDevice;

		public ConnectThread(BluetoothDevice device) {
			mmDevice = device;
			BluetoothSocket tmp = null;
			try {
				tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
			} catch (IOException e) {
				Log.e(TAG, "socket��ȡʧ�ܣ�" + e);
			}
			mmSocket = tmp;
		}

		public void run() {
			Log.e(TAG, "��ʼmConnectThread");
			setName("ConnectThread");
			// mAdapter.cancelDiscovery();
			try {
				mmSocket.connect();
			} catch (IOException e) {
				// ����ʧ�ܣ�����ui
				connectionFailed();
				try {
					mmSocket.close();
				} catch (IOException e2) {
					Log.e(TAG, "�ر�����ʧ��" + e2);
				}
				Log.e(TAG, "�ر�����==" + e);
				// ������������߳�
				startChat();
				return;
			}
			
			synchronized (BluetoothService.this) {
				mConnectThread = null;
			}
			connected(mmSocket, mmDevice);
		}

		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) {
				Log.e(TAG, "�ر�����ʧ��" + e);
			}
		}
	}

	/**
	 * �Ѿ����ӳɹ�����߳� �������д���ʹ����Ĵ���
	 */
	private class ConnectedThread extends Thread {
		private final BluetoothSocket mmSocket;
		private final InputStream mmInStream;
		private final OutputStream mmOutStream;

		public ConnectedThread(BluetoothSocket socket) {
			mmSocket = socket;
			InputStream tmpIn = null;
			OutputStream tmpOut = null;
			// �õ�BluetoothSocket����������
			try {
				tmpIn = socket.getInputStream();
				tmpOut = socket.getOutputStream();
			} catch (IOException e) {
				Log.e(TAG, "temp sockets not created" + e);
			}

			mmInStream = tmpIn;
			mmOutStream = tmpOut;
		}
		public void run() {
			int bytes;
			// ѭ��������Ϣ
			while (true) {
				try {
					byte[] buffer = new byte[256];
					bytes = mmInStream.read(buffer);
					String readStr = new String(buffer, 0, bytes);// �ֽ�����ֱ��ת�����ַ���
					String str = bytes2HexString(buffer).replaceAll("00", "").trim();
					String im = android.provider.Settings.Secure.getString(ApplicationContext.getInstance().getContentResolver(),
			                android.provider.Settings.Secure.DEFAULT_INPUT_METHOD);
					
					Log.e("lichao", "BluetoothChatService->readStr=" + readStr);
					Log.e("lichao", "BluetoothChatService->str=" + str);
					if (bytes > 0) {// ����ȡ������Ϣ�������߳�
						mHandler.obtainMessage(
								ConnectActivity.MESSAGE_READ, bytes, -1,
								buffer).sendToTarget();
						if (im.equals(INPUT_MOTHOD)) {
							ss.pinyinIME.SetText(readStr);
						}
					} else {
						Log.e(TAG, "disconnected");
						connectionLost();
						if (mState != STATE_NONE) {
							Log.e(TAG, "disconnected");
							startChat();
						}
						break;
					}
				} catch (IOException e) {
					Log.e(TAG, "disconnected" + e);
					connectionLost();
					if (mState != STATE_NONE) {
						// ��������������ģʽ�����÷���
						startChat();
					}
					break;
				}
			}
		}

		/**
		 * д��OutStream����
		 * @param buffer Ҫд���ֽ�
		 */
		public void write(byte[] buffer) {
			try {
				mmOutStream.write(buffer);
				
				// ����Ϣ����UI
				mHandler.obtainMessage(ConnectActivity.MESSAGE_WRITE, -1,
						-1, buffer).sendToTarget();
			} catch (IOException e) {
				Log.e(TAG, "Exception during write:" + e);
			}
		}

		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) {
				Log.e(TAG, "close() of connect socket failed:" + e);
			}
		}
	}

	/**
	 * ���ֽ����鵽ʮ�������ַ���ת��
	 */
	public static String bytes2HexString(byte[] b) {
		String ret = "";
		for (int i = 0; i < b.length; i++) {
			String hex = Integer.toHexString(b[i] & 0xFF);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			ret += hex.toUpperCase();
		}
		return ret;
	}
	
	/**
	 * ����������socket�̡߳������Է��豸�Ƿ������Ͽ���������
	 * @author DELL
	 *
	 */
	class WatchServerSocketThread extends Thread {
		private BluetoothSocket mmSocket;
		private BluetoothDevice mmDevice;
	    
		@Override
		public void run() {
			while(true) {
				switch (conn_status) {
				case 0: //���
                    if (BleIsOKFlag && ServerSocketIsClose) {
                        //�Ѿ�ȷ�������ӶϿ�
                        ServerSocketIsClose = false;
                        Log.e("lichao", "===δ����");
                        conn_status = 1;
                    }
                    break;
                    
				case 1: //����
					//�����ͻ��˵�socket
                    try {
                    	mmDevice = mAdapter.getRemoteDevice("AA:A8:AC:10:87:8F");
                        mmSocket = mmDevice.createRfcommSocketToServiceRecord(MY_UUID);
                        mmSocket.connect();
                    } catch (IOException e) {
                        Error_Num++;
                        if(Error_Num > Num) {
                            Error_Num = 0;
                            Conn_Error_Num++;
                        }
                        e.printStackTrace();
                        //ע��ע��[��Ȼû���ӳɹ���û��Ҫִ������Ĵ�����]
                        continue;
                    }
                    
                    Error_Num = 0;
                    startChat();
                    connected(mmSocket, mmDevice);
                    Log.e("lichao", "===������");
                    //�ٴμ��
                    conn_status = 0;
					break;
				
				default: //Ĭ��
                    System.out.print("nothing to do...");
                    break;
				}
			}
		}
	}
}
