package com.idata.bluetoothime;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * �㲥������������android.intent.idatachina.RFID.BARCODE.SCANINFO�Ĺ㲥��ȡ��Intent���ֶ�
 * "idatachina.SCAN_DATA"�洢�����ݣ�����ƴ������PinyinIME���͸�EditText
 * 
 * @ClassName MyReceiver
 * @author LiChao
 */
public class MyReceiver extends BroadcastReceiver {
	PinyinIME ss = new PinyinIME();

	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		// MainActivity.onrecvintend(intent);
		String tinfo = intent.getStringExtra("idatachina.SCAN_DATA");
		ss.pinyinIME.SetText(tinfo);

	}
}
