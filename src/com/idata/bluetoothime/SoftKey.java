package com.idata.bluetoothime;

import android.graphics.drawable.Drawable;

/**
 * Class for soft keys which defined in the keyboard xml file. A soft key can be
 * a basic key or a toggling key. ����
 * 
 * @see com.android.inputmethod.pinyin.SoftKeyToggle
 */
public class SoftKey {
	protected static final int KEYMASK_REPEAT = 0x10000000;
	protected static final int KEYMASK_BALLOON = 0x20000000;

	/**
	 * For a finger touch device, after user presses a key, there will be some
	 * consequent moving events because of the changing in touching pressure. If
	 * the moving distance in x is within this threshold, the moving events will
	 * be ignored. �����ƶ��¼���Ч��x�����ֵ���ƶ���x�����ֵС����Ч��x�����ֵ���ƶ��¼���������
	 */
	public static final int MAX_MOVE_TOLERANCE_X = 0;

	/**
	 * For a finger touch device, after user presses a key, there will be some
	 * consequent moving events because of the changing in touching pressure. If
	 * the moving distance in y is within this threshold, the moving events will
	 * be ignored. �����ƶ��¼���Ч��y�����ֵ���ƶ���x�����ֵС����Ч��y�����ֵ���ƶ��¼���������
	 */
	public static final int MAX_MOVE_TOLERANCE_Y = 0;

	/**
	 * Used to indicate the type and attributes of this key. the lowest 8 bits
	 * should be reserved for SoftkeyToggle. ���������Ժ����ͣ���͵�8λ��������̱任״̬��
	 */
	protected int mKeyMask;

	/** key������ */
	protected SoftKeyType mKeyType;

	/** key��ͼ�� */
	protected Drawable mKeyIcon;

	/** key�ĵ���ͼ�� */
	protected Drawable mKeyIconPopup;

	/** key���ı� */
	protected String mKeyLabel;

	/** key��code */
	protected int mKeyCode;

	/**
	 * If this value is not 0, this key can be used to popup a sub soft keyboard
	 * when user presses it for some time.
	 * ����̵����Ի����id��������ֵ��Ϊ�գ���ô������������ʱ�򣬵���һ��������̡�
	 */
	public int mPopupSkbId;

	/** ���̿�ȵİٷֱ� ��mLeft = (int) (mLeftF * skbWidth); */
	public float mLeftF;
	public float mRightF;
	/** ���̸߶ȵİٷֱ� */
	public float mTopF;
	public float mBottomF;
	// TODO ���µ� ���������������ʲô�ģ���ȫ�ֻ�������ڸ���ͼ�ģ�
	public int mLeft;
	public int mRight;
	public int mTop;
	public int mBottom;

	/**
	 * ���ð��������͡�ͼ�ꡢ����ͼ��
	 * 
	 * @param keyType
	 * @param keyIcon
	 * @param keyIconPopup
	 */
	public void setKeyType(SoftKeyType keyType, Drawable keyIcon,
			Drawable keyIconPopup) {
		mKeyType = keyType;
		mKeyIcon = keyIcon;
		mKeyIconPopup = keyIconPopup;
	}

	// The caller guarantees that all parameters are in [0, 1]
	public void setKeyDimensions(float left, float top, float right,
			float bottom) {
		mLeftF = left;
		mTopF = top;
		mRightF = right;
		mBottomF = bottom;
	}

	public void setKeyAttribute(int keyCode, String label, boolean repeat,
			boolean balloon) {
		mKeyCode = keyCode;
		mKeyLabel = label;

		if (repeat) {
			mKeyMask |= KEYMASK_REPEAT;
		} else {
			mKeyMask &= (~KEYMASK_REPEAT);
		}

		if (balloon) {
			mKeyMask |= KEYMASK_BALLOON;
		} else {
			mKeyMask &= (~KEYMASK_BALLOON);
		}
	}

	/**
	 * ���ø�����̵�����
	 * 
	 * @param popupSkbId
	 */
	public void setPopupSkbId(int popupSkbId) {
		mPopupSkbId = popupSkbId;
	}

	// Call after setKeyDimensions(). The caller guarantees that the
	// keyboard with and height are valid.
	/**
	 * ���ð���������
	 * 
	 * @param skbWidth
	 *            ���̵Ŀ��
	 * @param skbHeight
	 *            ���̵ĸ߶�
	 */
	public void setSkbCoreSize(int skbWidth, int skbHeight) {
		mLeft = (int) (mLeftF * skbWidth);
		mRight = (int) (mRightF * skbWidth);
		mTop = (int) (mTopF * skbHeight);
		mBottom = (int) (mBottomF * skbHeight);
	}

	public Drawable getKeyIcon() {
		return mKeyIcon;
	}

	public Drawable getKeyIconPopup() {
		if (null != mKeyIconPopup) {
			return mKeyIconPopup;
		}
		return mKeyIcon;
	}

	/**
	 * ��ȡ������key code
	 * 
	 * @return
	 */
	public int getKeyCode() {
		return mKeyCode;
	}

	/**
	 * ��ȡ�������ַ�
	 * 
	 * @return
	 */
	public String getKeyLabel() {
		return mKeyLabel;
	}

	/**
	 * ��Сдת��
	 * 
	 * @param upperCase
	 */
	public void changeCase(boolean upperCase) {
		if (null != mKeyLabel) {
			if (upperCase)
				mKeyLabel = mKeyLabel.toUpperCase();
			else
				mKeyLabel = mKeyLabel.toLowerCase();
		}
	}

	public Drawable getKeyBg() {
		return mKeyType.mKeyBg;
	}

	public Drawable getKeyHlBg() {
		return mKeyType.mKeyHlBg;
	}

	public int getColor() {
		return mKeyType.mColor;
	}

	public int getColorHl() {
		return mKeyType.mColorHl;
	}

	public int getColorBalloon() {
		return mKeyType.mColorBalloon;
	}

	/**
	 * �Ƿ���ϵͳ��keycode
	 * 
	 * @return
	 */
	public boolean isKeyCodeKey() {
		if (mKeyCode > 0)
			return true;
		return false;
	}

	/**
	 * �Ƿ����û������keycode
	 * 
	 * @return
	 */
	public boolean isUserDefKey() {
		if (mKeyCode < 0)
			return true;
		return false;
	}

	/**
	 * �Ƿ����ַ�����
	 * 
	 * @return
	 */
	public boolean isUniStrKey() {
		if (null != mKeyLabel && mKeyCode == 0)
			return true;
		return false;
	}

	/**
	 * �Ƿ���Ҫ��������
	 * 
	 * @return
	 */
	public boolean needBalloon() {
		return (mKeyMask & KEYMASK_BALLOON) != 0;
	}

	/**
	 * �Ƿ����ظ����¹��ܣ�����������������Ƿ�ִ�в�ͬ�Ĳ�����
	 * 
	 * @return
	 */
	public boolean repeatable() {
		return (mKeyMask & KEYMASK_REPEAT) != 0;
	}

	public int getPopupResId() {
		return mPopupSkbId;
	}

	public int width() {
		return mRight - mLeft;
	}

	public int height() {
		return mBottom - mTop;
	}

	/**
	 * �ж������Ƿ��ڸð�����������
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean moveWithinKey(int x, int y) {
		if (mLeft - MAX_MOVE_TOLERANCE_X <= x
				&& mTop - MAX_MOVE_TOLERANCE_Y <= y
				&& mRight + MAX_MOVE_TOLERANCE_X > x
				&& mBottom + MAX_MOVE_TOLERANCE_Y > y) {
			return true;
		}
		return false;
	}

	@Override
	public String toString() {
		String str = "\n";
		str += "  keyCode: " + String.valueOf(mKeyCode) + "\n";
		str += "  keyMask: " + String.valueOf(mKeyMask) + "\n";
		str += "  keyLabel: " + (mKeyLabel == null ? "null" : mKeyLabel) + "\n";
		str += "  popupResId: " + String.valueOf(mPopupSkbId) + "\n";
		str += "  Position: " + String.valueOf(mLeftF) + ", "
				+ String.valueOf(mTopF) + ", " + String.valueOf(mRightF) + ", "
				+ String.valueOf(mBottomF) + "\n";
		return str;
	}
}
