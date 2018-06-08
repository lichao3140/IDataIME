package com.idata.bluetoothime;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * Class used to maintain settings. �����࣬�������ļ����ж�ȡд���������������˵���ģʽ��
 * 
 * @ClassName Settings
 * @author LiChao
 */
public class Settings {
	private static final String ANDPY_CONFS_KEYSOUND_KEY = "Sound";
	private static final String ANDPY_CONFS_VIBRATE_KEY = "Vibrate";
	private static final String ANDPY_CONFS_PREDICTION_KEY = "Prediction";

	private static boolean mKeySound;
	private static boolean mVibrate;
	private static boolean mPrediction;

	private static Settings mInstance = null;

	/**
	 * ���ü���
	 */
	private static int mRefCount = 0;

	private static SharedPreferences mSharedPref = null;

	protected Settings(SharedPreferences pref) {
		mSharedPref = pref;
		initConfs();
	}

	/**
	 * ��ø�ʵ��
	 * 
	 * @param pref
	 * @return
	 */
	public static Settings getInstance(SharedPreferences pref) {
		if (mInstance == null) {
			mInstance = new Settings(pref);
		}
		assert (pref == mSharedPref);
		mRefCount++;
		return mInstance;
	}

	/**
	 * �����𶯡�������Ԥ�����ر�ǽ��������ļ�
	 */
	public static void writeBack() {
		Editor editor = mSharedPref.edit();
		editor.putBoolean(ANDPY_CONFS_VIBRATE_KEY, mVibrate);
		editor.putBoolean(ANDPY_CONFS_KEYSOUND_KEY, mKeySound);
		editor.putBoolean(ANDPY_CONFS_PREDICTION_KEY, mPrediction);
		editor.commit();
	}

	/**
	 * �ͷŶԸ�ʵ����ʹ�á�
	 */
	public static void releaseInstance() {
		mRefCount--;
		if (mRefCount == 0) {
			mInstance = null;
		}
	}

	/**
	 * ��ʼ�����������ļ���ȡ���𶯡�������Ԥ�����ر�ǡ�
	 */
	private void initConfs() {
		mKeySound = mSharedPref.getBoolean(ANDPY_CONFS_KEYSOUND_KEY, true);
		mVibrate = mSharedPref.getBoolean(ANDPY_CONFS_VIBRATE_KEY, false);
		mPrediction = mSharedPref.getBoolean(ANDPY_CONFS_PREDICTION_KEY, true);
	}

	/**
	 * ��ð��������Ŀ���
	 * 
	 * @return
	 */
	public static boolean getKeySound() {
		return mKeySound;
	}

	/**
	 * ���ð��������Ŀ���
	 * 
	 * @param v
	 */
	public static void setKeySound(boolean v) {
		if (mKeySound == v)
			return;
		mKeySound = v;
	}

	/**
	 * ����𶯿���
	 * 
	 * @return
	 */
	public static boolean getVibrate() {
		return mVibrate;
	}

	/**
	 * �����𶯿���
	 * 
	 * @param v
	 */
	public static void setVibrate(boolean v) {
		if (mVibrate == v)
			return;
		mVibrate = v;
	}

	/**
	 * ���Ԥ������
	 * 
	 * @return
	 */
	public static boolean getPrediction() {
		return mPrediction;
	}

	/**
	 * ����Ԥ������
	 * 
	 * @param v
	 */
	public static void setPrediction(boolean v) {
		if (mPrediction == v)
			return;
		mPrediction = v;
	}
}
