package com.idata.bluetoothime;

import android.content.Context;
import android.content.res.Configuration;
import android.view.Display;
import android.view.WindowManager;

/**
 * Global environment configurations for showing soft keyboard and candidate
 * view. All original dimension values are defined in float, and the real size
 * is calculated from the float values of and screen size. In this way, this
 * input method can work even when screen size is changed.
 * ���ౣ�沼�ֵ�һЩ�ߴ硣���磺��Ļ�Ŀ�ȡ���Ļ�ĸ߶�
 * �������ĸ߶ȡ���ѡ������ĸ߶ȡ��������ݿ�ȱȰ�����ȴ�Ĳ�ֵ���������ݸ߶ȱȰ����߶ȴ�Ĳ�ֵ�������������ı��Ĵ�С
 * �����ܰ������ı��Ĵ�С�����������������ı��Ĵ�С�����ܰ����������ı��Ĵ�С��
 */
public class Environment {
	/**
	 * The key height for portrait mode. It is relative to the screen height.
	 * ���������߶ȣ�ֵ���������Ļ�߶ȡ�
	 */
	private static final float KEY_HEIGHT_RATIO_PORTRAIT = 0.105f;

	/**
	 * The key height for landscape mode. It is relative to the screen height.
	 * ���������߶ȣ�ֵ���������Ļ�߶ȡ�
	 */
	private static final float KEY_HEIGHT_RATIO_LANDSCAPE = 0.147f;

	/**
	 * The height of the candidates area for portrait mode. It is relative to
	 * screen height. ������ѡ������ĸ߶ȣ�ֵ���������Ļ�߶ȡ�
	 */
	private static final float CANDIDATES_AREA_HEIGHT_RATIO_PORTRAIT = 0.084f;

	/**
	 * The height of the candidates area for portrait mode. It is relative to
	 * screen height. ������ѡ������߶ȣ�ֵ���������Ļ�߶ȡ�
	 */
	private static final float CANDIDATES_AREA_HEIGHT_RATIO_LANDSCAPE = 0.125f;

	/**
	 * How much should the balloon width be larger than width of the real key.
	 * It is relative to the smaller one of screen width and height.
	 * �²⣺�������̰�ťʱ�����������ݴ��ڰ����Ŀ�ȵĲ�ֵ��ֵ���������Ļ�߶ȺͿ�Ƚ�С����һ����
	 */
	private static final float KEY_BALLOON_WIDTH_PLUS_RATIO = 0.08f;

	/**
	 * How much should the balloon height be larger than that of the real key.
	 * It is relative to the smaller one of screen width and height.
	 * �²⣺�������̰�ťʱ�����������ݴ��ڰ����ĸ߶ȵĲ�ֵ��ֵ���������Ļ�߶ȺͿ�Ƚ�С����һ����
	 */
	private static final float KEY_BALLOON_HEIGHT_PLUS_RATIO = 0.07f;

	/**
	 * The text size for normal keys. It is relative to the smaller one of
	 * screen width and height. �����������ı��Ĵ�С��ֵ���������Ļ�߶ȺͿ�Ƚ�С����һ����
	 */
	private static final float NORMAL_KEY_TEXT_SIZE_RATIO = 0.075f;

	/**
	 * The text size for function keys. It is relative to the smaller one of
	 * screen width and height. ���ܰ������ı��Ĵ�С��ֵ���������Ļ�߶ȺͿ�Ƚ�С����һ����
	 */
	private static final float FUNCTION_KEY_TEXT_SIZE_RATIO = 0.055f;

	/**
	 * The text size balloons of normal keys. It is relative to the smaller one
	 * of screen width and height. �����������������ݵ��ı��Ĵ�С��ֵ���������Ļ�߶ȺͿ�Ƚ�С����һ����
	 */
	private static final float NORMAL_BALLOON_TEXT_SIZE_RATIO = 0.14f;

	/**
	 * The text size balloons of function keys. It is relative to the smaller
	 * one of screen width and height. ���ܰ������������ݵ��ı��Ĵ�С��ֵ���������Ļ�߶ȺͿ�Ƚ�С����һ����
	 */
	private static final float FUNCTION_BALLOON_TEXT_SIZE_RATIO = 0.085f;

	/**
	 * The configurations are managed in a singleton. �����ʵ��������������ģʽ�ĵ���ģʽ��
	 */
	private static Environment mInstance;

	private int mScreenWidth; // ��Ļ�Ŀ��
	private int mScreenHeight; // ��Ļ�ĸ߶�
	private int mKeyHeight; // �����ĸ߶�
	private int mCandidatesAreaHeight; // ��ѡ������ĸ߶�
	private int mKeyBalloonWidthPlus; // �������ݿ�ȱȰ�����ȴ�Ĳ�ֵ
	private int mKeyBalloonHeightPlus; // �������ݸ߶ȱȰ����߶ȴ�Ĳ�ֵ
	private int mNormalKeyTextSize; // �����������ı��Ĵ�С
	private int mFunctionKeyTextSize; // ���ܰ������ı��Ĵ�С
	private int mNormalBalloonTextSize; // ���������������ı��Ĵ�С
	private int mFunctionBalloonTextSize; // ���ܰ����������ı��Ĵ�С
	private Configuration mConfig = new Configuration();
	private boolean mDebug = false;

	private Environment() {
	}

	public static Environment getInstance() {
		if (null == mInstance) {
			mInstance = new Environment();
		}
		return mInstance;
	}

	public void onConfigurationChanged(Configuration newConfig, Context context) {
		if (mConfig.orientation != newConfig.orientation) {
			WindowManager wm = (WindowManager) context
					.getSystemService(Context.WINDOW_SERVICE);
			Display d = wm.getDefaultDisplay();
			mScreenWidth = d.getWidth();
			mScreenHeight = d.getHeight();

			int scale;
			if (mScreenHeight > mScreenWidth) {
				mKeyHeight = (int) (mScreenHeight * KEY_HEIGHT_RATIO_PORTRAIT);
				mCandidatesAreaHeight = (int) (mScreenHeight * CANDIDATES_AREA_HEIGHT_RATIO_PORTRAIT);
				scale = mScreenWidth;
			} else {
				mKeyHeight = (int) (mScreenHeight * KEY_HEIGHT_RATIO_LANDSCAPE);
				mCandidatesAreaHeight = (int) (mScreenHeight * CANDIDATES_AREA_HEIGHT_RATIO_LANDSCAPE);
				scale = mScreenHeight;
			}
			mNormalKeyTextSize = (int) (scale * NORMAL_KEY_TEXT_SIZE_RATIO);
			mFunctionKeyTextSize = (int) (scale * FUNCTION_KEY_TEXT_SIZE_RATIO);
			mNormalBalloonTextSize = (int) (scale * NORMAL_BALLOON_TEXT_SIZE_RATIO);
			mFunctionBalloonTextSize = (int) (scale * FUNCTION_BALLOON_TEXT_SIZE_RATIO);
			mKeyBalloonWidthPlus = (int) (scale * KEY_BALLOON_WIDTH_PLUS_RATIO);
			mKeyBalloonHeightPlus = (int) (scale * KEY_BALLOON_HEIGHT_PLUS_RATIO);
		}

		mConfig.updateFrom(newConfig);
	}

	public Configuration getConfiguration() {
		return mConfig;
	}

	public int getScreenWidth() {
		return mScreenWidth;
	}

	public int getScreenHeight() {
		return mScreenHeight;
	}

	public int getHeightForCandidates() {
		return mCandidatesAreaHeight;
	}

	public float getKeyXMarginFactor() {
		return 1.0f;
	}

	public float getKeyYMarginFactor() {
		if (Configuration.ORIENTATION_LANDSCAPE == mConfig.orientation) {
			return 0.7f;
		}
		return 1.0f;
	}

	public int getKeyHeight() {
		return mKeyHeight;
	}

	public int getKeyBalloonWidthPlus() {
		return mKeyBalloonWidthPlus;
	}

	public int getKeyBalloonHeightPlus() {
		return mKeyBalloonHeightPlus;
	}

	public int getSkbHeight() {
		if (Configuration.ORIENTATION_PORTRAIT == mConfig.orientation) {
			return mKeyHeight * 4;
		} else if (Configuration.ORIENTATION_LANDSCAPE == mConfig.orientation) {
			return mKeyHeight * 4;
		}
		return 0;
	}

	/**
	 * ��ð������ı���С
	 * 
	 * @param isFunctionKey
	 *            �Ƿ��ǹ��ܼ�
	 * @return
	 */
	public int getKeyTextSize(boolean isFunctionKey) {
		if (isFunctionKey) {
			return mFunctionKeyTextSize;
		} else {
			return mNormalKeyTextSize;
		}
	}

	public int getBalloonTextSize(boolean isFunctionKey) {
		if (isFunctionKey) {
			return mFunctionBalloonTextSize;
		} else {
			return mNormalBalloonTextSize;
		}
	}

	public boolean hasHardKeyboard() {
		if (mConfig.keyboard == Configuration.KEYBOARD_NOKEYS
				|| mConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES) {
			return false;
		}
		return true;
	}

	public boolean needDebug() {
		return mDebug;
	}
}
