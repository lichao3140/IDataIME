package com.idata.bluetoothime;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import com.idata.bluetoothime.ToolsUtil.CallBack;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.inputmethodservice.InputMethodService;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

/**
 * Main class of the Pinyin input method. ���뷨����
 */
public class PinyinIME extends InputMethodService {
	
	private static final int IDATA_KEY_CONNECT_BLUETOOTH = -301;//��������
	private static final int IDATA_KEY_DISCONNECT_BLUETOOTH = -302;//�Ͽ�����
	private static final int IDATA_KEY_OPEN_SCAN = -303;//����ɨ��
	private static final int IDATA_KEY_USB = -304;//USB
	private static final int IDATA_KEY_FUNCTION = -305;//����
	private static final int IDATA_KEY_DEVICE = -306;//�豸
	/**
	 * TAG for debug.
	 */
	static final String TAG = "PinyinIME";
	static PinyinIME pinyinIME;
	/**
	 * If is is true, IME will simulate key events for delete key, and send the
	 * events back to the application.
	 */
	private static final boolean SIMULATE_KEY_DELETE = true;

	/**
	 * Necessary environment configurations like screen size for this IME.
	 * �ö��󱣴��˲��ֵ�һЩ�ߴ磬�������ǵ���ģʽ��
	 */
	private Environment mEnvironment;

	/**
	 * Used to switch input mode. ���뷨״̬�任��
	 */
	private InputModeSwitcher mInputModeSwitcher;

	/**
	 * Soft keyboard container view to host real soft keyboard view. ����̼�װ��
	 */
	private SkbContainer mSkbContainer;

	/**
	 * The floating container which contains the composing view. If necessary,
	 * some other view like candiates container can also be put here. ������ͼ��װ��
	 */
	private LinearLayout mFloatingContainer;

	/**
	 * View to show the composing string. ����ַ�����View��������ʾ�����ƴ����
	 */
	private ComposingView mComposingView;

	/**
	 * Window to show the composing string. ��������ƴ���ַ����Ĵ��ڡ�
	 */
	private PopupWindow mFloatingWindow;

	/**
	 * Used to show the floating window. ��ʾ�����ƴ���ַ���PopupWindow ��ʱ��
	 */
	private PopupTimer mFloatingWindowTimer = new PopupTimer();

	/**
	 * View to show candidates list. ��ѡ����ͼ��װ��
	 */
	private CandidatesContainer mCandidatesContainer;

	/**
	 * Balloon used when user presses a candidate. ��ѡ������
	 */
	private BalloonHint mCandidatesBalloon;

	/**
	 * Used to notify the input method when the user touch a candidate.
	 * ���û�ѡ���˺�ѡ�ʻ����ں�ѡ����ͼ����������ʱ��֪ͨ���뷨�� ʵ���˺�ѡ����ͼ�ļ�����CandidateViewListener��
	 */
	private ChoiceNotifier mChoiceNotifier;

	/**
	 * Used to notify gestures from soft keyboard. ����̵����Ƽ�����
	 */
	private OnGestureListener mGestureListenerSkb;

	/**
	 * Used to notify gestures from candidates view. ��ѡ�ʵ����Ƽ�����
	 */
	private OnGestureListener mGestureListenerCandidates;

	/**
	 * The on-screen movement gesture detector for soft keyboard. ����̵����Ƽ����
	 */
	private GestureDetector mGestureDetectorSkb;

	/**
	 * The on-screen movement gesture detector for candidates view. ��ѡ�ʵ����Ƽ����
	 */
	private GestureDetector mGestureDetectorCandidates;

	/**
	 * Option dialog to choose settings and other IMEs. ���ܶԻ���
	 */
	private AlertDialog mOptionsDialog;

	/**
	 * Connection used to bind the decoding service. ����
	 * �ʿ����Զ�̷���PinyinDecoderService �ļ�����
	 */
	private PinyinDecoderServiceConnection mPinyinDecoderServiceConnection;

	/**
	 * The current IME status. ��ǰ�����뷨״̬
	 * 
	 * @see com.android.inputmethod.pinyin.PinyinIME.ImeState
	 */
	private ImeState mImeState = ImeState.STATE_IDLE;

	/**
	 * The decoding information, include spelling(Pinyin) string, decoding
	 * result, etc. �ʿ�����������
	 */
	private DecodingInfo mDecInfo = new DecodingInfo();

	/**
	 * For English input. Ӣ�����뷨����������
	 * 
	 */
	private EnglishInputProcessor mImEn;

	// receive ringer mode changes
	/**
	 * ����ģʽ�ı�ʱ�Ĺ㲥������
	 */
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			SoundManager.getInstance(context).updateRingerMode();
		}
	};

	@Override
	public void onCreate() {
		mEnvironment = Environment.getInstance();
		if (mEnvironment.needDebug()) {
			Log.d(TAG, "onCreate.");
		}
		super.onCreate();
		pinyinIME = this;

		// �󶨴ʿ����Զ�̷���PinyinDecoderService
		startPinyinDecoderService();

		mImEn = new EnglishInputProcessor();
		Settings.getInstance(PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext()));

		mInputModeSwitcher = new InputModeSwitcher(this);
		mChoiceNotifier = new ChoiceNotifier(this);
		mGestureListenerSkb = new OnGestureListener(false);
		mGestureListenerCandidates = new OnGestureListener(true);
		mGestureDetectorSkb = new GestureDetector(this, mGestureListenerSkb);
		mGestureDetectorCandidates = new GestureDetector(this, mGestureListenerCandidates);

		mEnvironment.onConfigurationChanged(getResources().getConfiguration(), this);
	}

	@Override
	public void onDestroy() {
		if (mEnvironment.needDebug()) {
			Log.d(TAG, "onDestroy.");
		}

		// ��󶨴ʿ����Զ�̷���PinyinDecoderService
		unbindService(mPinyinDecoderServiceConnection);

		// �ͷ������������
		Settings.releaseInstance();

		super.onDestroy();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		Environment env = Environment.getInstance();
		if (mEnvironment.needDebug()) {
			Log.d(TAG, "onConfigurationChanged");
			Log.d(TAG, "--last config: " + env.getConfiguration().toString());
			Log.d(TAG, "---new config: " + newConfig.toString());
		}
		// We need to change the local environment first so that UI components
		// can get the environment instance to handle size issues. When
		// super.onConfigurationChanged() is called, onCreateCandidatesView()
		// and onCreateInputView() will be executed if necessary.
		env.onConfigurationChanged(newConfig, this);

		// Clear related UI of the previous configuration.
		if (null != mSkbContainer) {
			mSkbContainer.dismissPopups();
		}
		if (null != mCandidatesBalloon) {
			mCandidatesBalloon.dismiss();
		}
		super.onConfigurationChanged(newConfig);

		// ���õ�����״̬
		resetToIdleState(false);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (processKey(event, 0 != event.getRepeatCount()))
			return true;
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (processKey(event, true))
			return true;
		return super.onKeyUp(keyCode, event);
	}

	/**
	 * ��EditText�����ı����ڹ㲥������MyReceiver��onReceive�����е��á�
	 * 
	 * @param text
	 */
	public void SetText(CharSequence text) {
		InputConnection ic = getCurrentInputConnection();
		if (ic == null)
			return;
		ic.beginBatchEdit();

		ic.commitText(text, 0);
		ic.endBatchEdit();

	}

	/**
	 * ����������
	 * 
	 * @param event
	 * @param realAction
	 * @return
	 */
	private boolean processKey(KeyEvent event, boolean realAction) {
		if (ImeState.STATE_BYPASS == mImeState)
			return false;

		int keyCode = event.getKeyCode();
		
		Log.i("lichao", "PinyinIME->processKey->keyCode=" + keyCode);
		// SHIFT-SPACE is used to switch between Chinese and English
		// when HKB is on.
		// SHIFT + SPACE ������ϴ���
		if (KeyEvent.KEYCODE_SPACE == keyCode && event.isShiftPressed()) {
			if (!realAction)
				return true;

			updateIcon(mInputModeSwitcher.switchLanguageWithHkb());
			resetToIdleState(false);

			// ���alt shift sym ����ס��״̬
			int allMetaState = KeyEvent.META_ALT_ON | KeyEvent.META_ALT_LEFT_ON
					| KeyEvent.META_ALT_RIGHT_ON | KeyEvent.META_SHIFT_ON
					| KeyEvent.META_SHIFT_LEFT_ON
					| KeyEvent.META_SHIFT_RIGHT_ON | KeyEvent.META_SYM_ON;
			getCurrentInputConnection().clearMetaKeyStates(allMetaState);
			return true;
		}

		// If HKB is on to input English, by-pass the key event so that
		// default key listener will handle it.
		// �����Ӳ����Ӣ������״̬���ͺ��Ե��ð�������Ĭ�ϵİ���������ȥ��������
		if (mInputModeSwitcher.isEnglishWithHkb()) {
			return false;
		}

		// ���ܼ�����
		if (processFunctionKeys(keyCode, realAction)) {
			return true;
		}

		int keyChar = 0;
		if (keyCode >= KeyEvent.KEYCODE_A && keyCode <= KeyEvent.KEYCODE_Z) {
			keyChar = keyCode - KeyEvent.KEYCODE_A + 'a';
		} else if (keyCode >= KeyEvent.KEYCODE_0
				&& keyCode <= KeyEvent.KEYCODE_9) {
			keyChar = keyCode - KeyEvent.KEYCODE_0 + '0';
		} else if (keyCode == KeyEvent.KEYCODE_COMMA) { // ����
			keyChar = ',';
		} else if (keyCode == KeyEvent.KEYCODE_PERIOD) {// ���
			keyChar = '.';
		} else if (keyCode == KeyEvent.KEYCODE_SPACE) {// �ո��
			keyChar = ' ';
		} else if (keyCode == KeyEvent.KEYCODE_APOSTROPHE) {
			keyChar = '\'';
		}

		if (mInputModeSwitcher.isEnglishWithSkb()) {// Ӣ������̴���
			return mImEn.processKey(getCurrentInputConnection(), event,
					mInputModeSwitcher.isEnglishUpperCaseWithSkb(), realAction);
		} else if (mInputModeSwitcher.isChineseText()) {// �������뷨ģʽ
			if (mImeState == ImeState.STATE_IDLE
					|| mImeState == ImeState.STATE_APP_COMPLETION) {
				mImeState = ImeState.STATE_IDLE;
				return processStateIdle(keyChar, keyCode, event, realAction);
			} else if (mImeState == ImeState.STATE_INPUT) {
				return processStateInput(keyChar, keyCode, event, realAction);
			} else if (mImeState == ImeState.STATE_PREDICT) {
				return processStatePredict(keyChar, keyCode, event, realAction);
			} else if (mImeState == ImeState.STATE_COMPOSING) {
				return processStateEditComposing(keyChar, keyCode, event, realAction);
			}
		} else {// ���Ŵ���
			if (0 != keyChar && realAction) {
				// �����ı���EditText
				commitResultText(String.valueOf((char) keyChar));
			}
		}

		return false;
	}

	// keyCode can be from both hard key or soft key.
	/**
	 * ���ܼ�������
	 * 
	 * @param keyCode
	 * @param realAction
	 * @return
	 */
	private boolean processFunctionKeys(int keyCode, boolean realAction) {
		// Back key is used to dismiss all popup UI in a soft keyboard.
		// ���˼��Ĵ���������̵�������ʾ��ʱ�����realActionΪtrue����ô�͵���dismissPopupSkb�������ظ�����̵�������ʾ���������ͼ��
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (isInputViewShown()) {
				if (mSkbContainer.handleBack(realAction))
					return true;
			}
		}

		// Chinese related input is handle separately.
		// ������������ǵ�������ģ�������ߴ���
		if (mInputModeSwitcher.isChineseText()) {
			return false;
		}

		if (null != mCandidatesContainer && mCandidatesContainer.isShown()
				&& !mDecInfo.isCandidatesListEmpty()) {// ��ѡ����ͼ��ʾ��ʱ��
			if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
				if (!realAction)
					return true;

				// ѡ��ǰ�����ĺ�ѡ��
				chooseCandidate(-1);
				return true;
			}

			if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
				if (!realAction)
					return true;

				// ����λ������һ����ѡ���ƶ������ƶ�����һҳ�����һ����ѡ�ʵ�λ�á�
				mCandidatesContainer.activeCurseBackward();
				return true;
			}

			if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
				if (!realAction)
					return true;

				// ����λ������һ����ѡ���ƶ������ƶ�����һҳ�ĵ�һ����ѡ�ʵ�λ�á�
				mCandidatesContainer.activeCurseForward();
				return true;
			}

			if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
				if (!realAction)
					return true;

				// ����һҳ��ѡ��
				mCandidatesContainer.pageBackward(false, true);
				return true;
			}

			if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
				if (!realAction)
					return true;

				// ����һҳ��ѡ��
				mCandidatesContainer.pageForward(false, true);
				return true;
			}

			// ��Ԥ��״̬�µ�ɾ��������
			if (keyCode == KeyEvent.KEYCODE_DEL
					&& ImeState.STATE_PREDICT == mImeState) {
				if (!realAction)
					return true;
				resetToIdleState(false);
				return true;
			}
		} else {// û�к�ѡ����ʾ��ʱ��

			if (keyCode == KeyEvent.KEYCODE_DEL) {
				if (!realAction)
					return true;
				if (SIMULATE_KEY_DELETE) {
					// ��EditText����һ��ɾ�������İ��º͵����¼���
					simulateKeyEventDownUp(keyCode);
				} else {
					// ����ɾ��һ���ַ��Ĳ�����EditText
					getCurrentInputConnection().deleteSurroundingText(1, 0);
				}
				return true;
			}
			if (keyCode == KeyEvent.KEYCODE_ENTER) {
				if (!realAction)
					return true;

				// ����Enter����EditText
				sendKeyChar('\n');
				return true;
			}
			// �ո��
			if (keyCode == KeyEvent.KEYCODE_SPACE) {
				if (!realAction)
					return true;

				// ����' '�ַ���EditText
				sendKeyChar(' ');
				Log.i("lichao", "Ӣ��״̬�ո��");
				return true;
			}
		}

		return false;
	}

	/**
	 * �� mImeState == ImeState.STATE_IDLE ���� mImeState ==
	 * ImeState.STATE_APP_COMPLETION ʱ�İ���������
	 * 
	 * @param keyChar
	 * @param keyCode
	 * @param event
	 * @param realAction
	 * @return
	 */
	private boolean processStateIdle(int keyChar, int keyCode, KeyEvent event,
			boolean realAction) {
		// In this status, when user presses keys in [a..z], the status will
		// change to input state.
		if (keyChar >= 'a' && keyChar <= 'z' && !event.isAltPressed()) {
			if (!realAction)
				return true;
			mDecInfo.addSplChar((char) keyChar, true);

			// �������ƴ�����в�ѯ
			chooseAndUpdate(-1);
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_DEL) {
			if (!realAction)
				return true;
			if (SIMULATE_KEY_DELETE) {
				// ģ��ɾ�������͸� EditText
				simulateKeyEventDownUp(keyCode);
			} else {
				// ����ɾ��һ���ַ��Ĳ����� EditText
				getCurrentInputConnection().deleteSurroundingText(1, 0);
			}
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_ENTER) {
			if (!realAction)
				return true;

			// ���� ENTER ���� EditText
			sendKeyChar('\n');
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_ALT_LEFT
				|| keyCode == KeyEvent.KEYCODE_ALT_RIGHT
				|| keyCode == KeyEvent.KEYCODE_SHIFT_LEFT
				|| keyCode == KeyEvent.KEYCODE_SHIFT_RIGHT) {
			return true;
		} else if (event.isAltPressed()) {
			// ��ȡ����ȫ���ַ�
			char fullwidth_char = KeyMapDream.getChineseLabel(keyCode);
			if (0 != fullwidth_char) {
				if (realAction) {
					String result = String.valueOf(fullwidth_char);
					commitResultText(result);
				}
				return true;
			} else {
				if (keyCode >= KeyEvent.KEYCODE_A
						&& keyCode <= KeyEvent.KEYCODE_Z) {
					return true;
				}
			}
		} else if (keyChar != 0 && keyChar != '\t') {
			if (realAction) {
				if (keyChar == ',' || keyChar == '.') {
					// ���� '\uff0c' ���� '\u3002' ��EditText
					inputCommaPeriod("", keyChar, false, ImeState.STATE_IDLE);
				} else {
					if (0 != keyChar) {
						String result = String.valueOf((char) keyChar);
						commitResultText(result);
					}
				}
			}
			return true;
		}
		return false;
	}

	/**
	 * �� mImeState == ImeState.STATE_INPUT ʱ�İ���������
	 * 
	 * @param keyChar
	 * @param keyCode
	 * @param event
	 * @param realAction
	 * @return
	 */
	private boolean processStateInput(int keyChar, int keyCode, KeyEvent event,
			boolean realAction) {
		// If ALT key is pressed, input alternative key. But if the
		// alternative key is quote key, it will be used for input a splitter
		// in Pinyin string.
		// ��� ALT ����ס
		if (event.isAltPressed()) {
			if ('\'' != event.getUnicodeChar(event.getMetaState())) {
				if (realAction) {
					// ��ȡ����ȫ���ַ�
					char fullwidth_char = KeyMapDream.getChineseLabel(keyCode);
					if (0 != fullwidth_char) {
						// ���͸����ĺ�ѡ�� + ����ȫ���ַ� �� EditView
						commitResultText(mDecInfo
								.getCurrentFullSent(mCandidatesContainer
										.getActiveCandiatePos())
								+ String.valueOf(fullwidth_char));
						resetToIdleState(false);
					}
				}
				return true;
			} else {
				keyChar = '\'';
			}
		}

		if (keyChar >= 'a' && keyChar <= 'z' || keyChar == '\''
				&& !mDecInfo.charBeforeCursorIsSeparator()
				|| keyCode == KeyEvent.KEYCODE_DEL) {
			if (!realAction)
				return true;

			// ��������ƴ����Ȼ����дʿ��ѯ������ɾ�������ƴ��ָ�����ַ����ַ�����Ȼ����дʿ��ѯ��
			return processSurfaceChange(keyChar, keyCode);
		} else if (keyChar == ',' || keyChar == '.') {
			if (!realAction)
				return true;

			// ���� '\uff0c' ���� '\u3002' ��EditText
			inputCommaPeriod(mDecInfo.getCurrentFullSent(mCandidatesContainer
					.getActiveCandiatePos()), keyChar, true,
					ImeState.STATE_IDLE);
			return true;

		} else if (keyCode == KeyEvent.KEYCODE_DPAD_UP
				|| keyCode == KeyEvent.KEYCODE_DPAD_DOWN
				|| keyCode == KeyEvent.KEYCODE_DPAD_LEFT
				|| keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
			if (!realAction)
				return true;

			if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
				// ����λ������һ����ѡ���ƶ������ƶ�����һҳ�����һ����ѡ�ʵ�λ�á�
				mCandidatesContainer.activeCurseBackward();
			} else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
				// ����λ������һ����ѡ���ƶ������ƶ�����һҳ�ĵ�һ����ѡ�ʵ�λ�á�
				mCandidatesContainer.activeCurseForward();
			} else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
				// If it has been the first page, a up key will shift
				// the state to edit composing string.
				// ����һҳ��ѡ��
				if (!mCandidatesContainer.pageBackward(false, true)) {
					mCandidatesContainer.enableActiveHighlight(false);
					changeToStateComposing(true);
					updateComposingText(true);
				}
			} else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
				// ����һҳ��ѡ��
				mCandidatesContainer.pageForward(false, true);
			}
			return true;
		} else if (keyCode >= KeyEvent.KEYCODE_1
				&& keyCode <= KeyEvent.KEYCODE_9) {
			if (!realAction)
				return true;

			int activePos = keyCode - KeyEvent.KEYCODE_1;
			int currentPage = mCandidatesContainer.getCurrentPage();
			if (activePos < mDecInfo.getCurrentPageSize(currentPage)) {
				activePos = activePos
						+ mDecInfo.getCurrentPageStart(currentPage);
				if (activePos >= 0) {
					// ѡ���ѡ�ʣ������������Ƿ������һ����Ԥ����
					chooseAndUpdate(activePos);
				}
			}
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_ENTER) {
			if (!realAction)
				return true;
			if (mInputModeSwitcher.isEnterNoramlState()) {
				// �������ƴ���ַ������͸�EditText
				commitResultText(mDecInfo.getOrigianlSplStr().toString());
				resetToIdleState(false);
			} else {
				// �Ѹ����ĺ�ѡ�ʷ��͸�EditText
				commitResultText(mDecInfo
						.getCurrentFullSent(mCandidatesContainer
								.getActiveCandiatePos()));
				// ��ENTER���͸�EditText
				sendKeyChar('\n');
				resetToIdleState(false);
			}
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER
				|| keyCode == KeyEvent.KEYCODE_SPACE) {
			if (!realAction)
				return true;
			// ѡ������ĺ�ѡ��
			chooseCandidate(-1);
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (!realAction)
				return true;
			resetToIdleState(false);
			// �ر����뷨
			requestHideSelf(0);
			return true;
		}
		return false;
	}

	/**
	 * �� mImeState == ImeState.STATE_PREDICT ʱ�İ���������
	 * 
	 * @param keyChar
	 * @param keyCode
	 * @param event
	 * @param realAction
	 * @return
	 */
	private boolean processStatePredict(int keyChar, int keyCode,
			KeyEvent event, boolean realAction) {
		if (!realAction)
			return true;

		// If ALT key is pressed, input alternative key.
		// ��סAlt��
		if (event.isAltPressed()) {
			// ��ȡ����ȫ���ַ�
			char fullwidth_char = KeyMapDream.getChineseLabel(keyCode);
			if (0 != fullwidth_char) {
				// ���͸����ĺ�ѡ�� + ����ȫ���ַ� �� EditView
				commitResultText(mDecInfo.getCandidate(mCandidatesContainer
						.getActiveCandiatePos())
						+ String.valueOf(fullwidth_char));
				resetToIdleState(false);
			}
			return true;
		}

		// In this status, when user presses keys in [a..z], the status will
		// change to input state.
		if (keyChar >= 'a' && keyChar <= 'z') {
			changeToStateInput(true);
			// ��һ���ַ��������ƴ���ַ�����
			mDecInfo.addSplChar((char) keyChar, true);
			// �������ƴ�����в�ѯ��
			chooseAndUpdate(-1);
		} else if (keyChar == ',' || keyChar == '.') {
			// ���� '\uff0c' ���� '\u3002' ��EditText
			inputCommaPeriod("", keyChar, true, ImeState.STATE_IDLE);
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_UP
				|| keyCode == KeyEvent.KEYCODE_DPAD_DOWN
				|| keyCode == KeyEvent.KEYCODE_DPAD_LEFT
				|| keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
			if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
				// ����λ������һ����ѡ���ƶ������ƶ�����һҳ�����һ����ѡ�ʵ�λ�á�
				mCandidatesContainer.activeCurseBackward();
			}
			if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
				// ����λ������һ����ѡ���ƶ������ƶ�����һҳ�ĵ�һ����ѡ�ʵ�λ�á�
				mCandidatesContainer.activeCurseForward();
			}
			if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
				// ����һҳ��ѡ��
				mCandidatesContainer.pageBackward(false, true);
			}
			if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
				// ����һҳ��ѡ��
				mCandidatesContainer.pageForward(false, true);
			}
		} else if (keyCode == KeyEvent.KEYCODE_DEL) {
			resetToIdleState(false);
		} else if (keyCode == KeyEvent.KEYCODE_BACK) {
			resetToIdleState(false);
			// �ر����뷨
			requestHideSelf(0);
		} else if (keyCode >= KeyEvent.KEYCODE_1
				&& keyCode <= KeyEvent.KEYCODE_9) {
			int activePos = keyCode - KeyEvent.KEYCODE_1;
			int currentPage = mCandidatesContainer.getCurrentPage();
			if (activePos < mDecInfo.getCurrentPageSize(currentPage)) {
				activePos = activePos
						+ mDecInfo.getCurrentPageStart(currentPage);
				if (activePos >= 0) {
					// ѡ���ѡ��
					chooseAndUpdate(activePos);
				}
			}
		} else if (keyCode == KeyEvent.KEYCODE_ENTER) {
			// ����ENTER����EditText
			sendKeyChar('\n');
			resetToIdleState(false);
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER
				|| keyCode == KeyEvent.KEYCODE_SPACE) {
			// ѡ���ѡ��
			chooseCandidate(-1);
		}

		return true;
	}

	/**
	 * �� mImeState == ImeState.STATE_COMPOSING ʱ�İ���������
	 * 
	 * @param keyChar
	 * @param keyCode
	 * @param event
	 * @param realAction
	 * @return
	 */
	private boolean processStateEditComposing(int keyChar, int keyCode,
			KeyEvent event, boolean realAction) {
		if (!realAction)
			return true;

		// ��ȡ��������ַ�����״̬
		ComposingView.ComposingStatus cmpsvStatus = mComposingView
				.getComposingStatus();

		// If ALT key is pressed, input alternative key. But if the
		// alternative key is quote key, it will be used for input a splitter
		// in Pinyin string.
		// ��ס ALT ��
		if (event.isAltPressed()) {
			if ('\'' != event.getUnicodeChar(event.getMetaState())) {
				// ��ȡ����ȫ���ַ�
				char fullwidth_char = KeyMapDream.getChineseLabel(keyCode);
				if (0 != fullwidth_char) {
					String retStr;
					if (ComposingView.ComposingStatus.SHOW_STRING_LOWERCASE == cmpsvStatus) {
						// ��ȡԭʼ������ƴ�����ַ�
						retStr = mDecInfo.getOrigianlSplStr().toString();
					} else {
						// ��ȡ��ϵ�����ƴ�����ַ����п��ܴ���ѡ�еĺ�ѡ�ʣ�
						retStr = mDecInfo.getComposingStr();
					}
					// �����ı���EditText
					commitResultText(retStr + String.valueOf(fullwidth_char));
					resetToIdleState(false);
				}
				return true;
			} else {
				keyChar = '\'';
			}
		}

		if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
			if (!mDecInfo.selectionFinished()) {
				changeToStateInput(true);
			}
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT
				|| keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
			// �ƶ���ѡ�ʵĹ��
			mComposingView.moveCursor(keyCode);
		} else if ((keyCode == KeyEvent.KEYCODE_ENTER && mInputModeSwitcher
				.isEnterNoramlState())
				|| keyCode == KeyEvent.KEYCODE_DPAD_CENTER
				|| keyCode == KeyEvent.KEYCODE_SPACE) {
			if (ComposingView.ComposingStatus.SHOW_STRING_LOWERCASE == cmpsvStatus) {
				// ��ȡԭʼ������ƴ�����ַ�
				String str = mDecInfo.getOrigianlSplStr().toString();
				if (!tryInputRawUnicode(str)) {
					// �����ı���EditText
					commitResultText(str);
				}
			} else if (ComposingView.ComposingStatus.EDIT_PINYIN == cmpsvStatus) {
				// ��ȡ��ϵ�����ƴ�����ַ����п��ܴ���ѡ�еĺ�ѡ�ʣ�
				String str = mDecInfo.getComposingStr();
				// �Կ�ͷ���߽�βΪ"unicode"���ַ�������ת��
				if (!tryInputRawUnicode(str)) {
					// �����ı���EditText
					commitResultText(str);
				}
			} else {
				// ���� ��ϵ�����ƴ�����ַ����п��ܴ���ѡ�еĺ�ѡ�ʣ� �� EditText
				commitResultText(mDecInfo.getComposingStr());
			}
			resetToIdleState(false);
		} else if (keyCode == KeyEvent.KEYCODE_ENTER
				&& !mInputModeSwitcher.isEnterNoramlState()) {
			String retStr;
			if (!mDecInfo.isCandidatesListEmpty()) {
				// ��ȡ��ǰ�����ĺ�ѡ��
				retStr = mDecInfo.getCurrentFullSent(mCandidatesContainer
						.getActiveCandiatePos());
			} else {
				// ��ȡ��ϵ�����ƴ�����ַ����п��ܴ���ѡ�еĺ�ѡ�ʣ�
				retStr = mDecInfo.getComposingStr();
			}
			// �����ı���EditText
			commitResultText(retStr);
			// ����ENTER����EditText
			sendKeyChar('\n');
			resetToIdleState(false);
		} else if (keyCode == KeyEvent.KEYCODE_BACK) {
			resetToIdleState(false);
			// �ر����뷨
			requestHideSelf(0);
			return true;
		} else {
			// ��������ƴ����Ȼ����дʿ��ѯ������ɾ�������ƴ��ָ�����ַ����ַ�����Ȼ����дʿ��ѯ��
			return processSurfaceChange(keyChar, keyCode);
		}
		return true;
	}

	/**
	 * �Կ�ͷ���߽�βΪ"unicode"���ַ�������ת��
	 * 
	 * @param str
	 * @return
	 */
	private boolean tryInputRawUnicode(String str) {
		if (str.length() > 7) {
			if (str.substring(0, 7).compareTo("unicode") == 0) {// str��"unicode"��ͷ
				try {
					// ��ȡ"unicode"������ַ���
					String digitStr = str.substring(7);
					int startPos = 0;
					int radix = 10;
					if (digitStr.length() > 2 && digitStr.charAt(0) == '0'
							&& digitStr.charAt(1) == 'x') {
						startPos = 2;
						radix = 16;
					}
					digitStr = digitStr.substring(startPos);
					// ȡdigitStr��Ӧ������
					int unicode = Integer.parseInt(digitStr, radix);
					if (unicode > 0) {
						char low = (char) (unicode & 0x0000ffff);
						char high = (char) ((unicode & 0xffff0000) >> 16);
						commitResultText(String.valueOf(low));
						if (0 != high) {
							commitResultText(String.valueOf(high));
						}
					}
					return true;
				} catch (NumberFormatException e) {
					return false;
				}
			} else if (str.substring(str.length() - 7, str.length()).compareTo(
					"unicode") == 0) {// str��"unicode"��β
				String resultStr = "";
				for (int pos = 0; pos < str.length() - 7; pos++) {
					if (pos > 0) {
						resultStr += " ";
					}

					resultStr += "0x" + Integer.toHexString(str.charAt(pos));
				}
				commitResultText(String.valueOf(resultStr));
				return true;
			}
		}
		return false;
	}

	/**
	 * ��������ƴ����Ȼ����дʿ��ѯ������ɾ�������ƴ��ָ�����ַ����ַ�����Ȼ����дʿ��ѯ��
	 * 
	 * @param keyChar
	 * @param keyCode
	 * @return
	 */
	private boolean processSurfaceChange(int keyChar, int keyCode) {
		if (mDecInfo.isSplStrFull() && KeyEvent.KEYCODE_DEL != keyCode) {
			return true;
		}

		if ((keyChar >= 'a' && keyChar <= 'z')
				|| (keyChar == '\'' && !mDecInfo.charBeforeCursorIsSeparator())
				|| (((keyChar >= '0' && keyChar <= '9') || keyChar == ' ') && ImeState.STATE_COMPOSING == mImeState)) {
			mDecInfo.addSplChar((char) keyChar, false);
			chooseAndUpdate(-1);
		} else if (keyCode == KeyEvent.KEYCODE_DEL) {
			mDecInfo.prepareDeleteBeforeCursor();
			chooseAndUpdate(-1);
		}
		return true;
	}

	/**
	 * �������뷨״̬Ϊ mImeState = ImeState.STATE_COMPOSING;
	 * 
	 * @param updateUi
	 *            �Ƿ����UI
	 */
	private void changeToStateComposing(boolean updateUi) {
		mImeState = ImeState.STATE_COMPOSING;
		if (!updateUi)
			return;

		if (null != mSkbContainer && mSkbContainer.isShown()) {
			mSkbContainer.toggleCandidateMode(true);
		}
	}

	/**
	 * �������뷨״̬Ϊ mImeState = ImeState.STATE_INPUT;
	 * 
	 * @param updateUi
	 *            �Ƿ����UI
	 */
	private void changeToStateInput(boolean updateUi) {
		mImeState = ImeState.STATE_INPUT;
		if (!updateUi)
			return;

		if (null != mSkbContainer && mSkbContainer.isShown()) {
			mSkbContainer.toggleCandidateMode(true);
		}
		showCandidateWindow(true);
	}

	/**
	 * ģ�ⰴ��һ������
	 * 
	 * @param keyCode
	 */
	private void simulateKeyEventDownUp(int keyCode) {
		InputConnection ic = getCurrentInputConnection();
		if (null == ic)
			return;

		ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, keyCode));
		ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, keyCode));
	}

	/**
	 * �����ַ������༭��
	 * 
	 * @param resultText
	 */
	private void commitResultText(String resultText) {
		InputConnection ic = getCurrentInputConnection();
		Log.i("lichao", "PinyinIME->commitResultText->resultText=" + resultText);
		if (null != ic)
			ic.commitText(resultText, 1);
		if (null != mComposingView) {
			mComposingView.setVisibility(View.INVISIBLE);
			mComposingView.invalidate();
		}
	}

	/**
	 * �����Ƿ���ʾ����ƴ����view
	 * 
	 * @param visible
	 */
	private void updateComposingText(boolean visible) {
		if (!visible) {
			mComposingView.setVisibility(View.INVISIBLE);
		} else {
			mComposingView.setDecodingInfo(mDecInfo, mImeState);
			mComposingView.setVisibility(View.VISIBLE);
		}
		mComposingView.invalidate();
	}

	/**
	 * ���� '\uff0c' ���� '\u3002' ��EditText
	 * 
	 * @param preEdit
	 * @param keyChar
	 * @param dismissCandWindow
	 *            �Ƿ����ú�ѡ�ʴ���
	 * @param nextState
	 *            mImeState����һ��״̬
	 */
	private void inputCommaPeriod(String preEdit, int keyChar,
			boolean dismissCandWindow, ImeState nextState) {
		if (keyChar == ',')
			preEdit += '\uff0c';
		else if (keyChar == '.')
			preEdit += '\u3002';
		else
			return;
		Log.i("lichao", "PinyinIME->inputCommaPeriod->preEdit=" + preEdit);
		commitResultText(preEdit);
		if (dismissCandWindow)
			resetCandidateWindow();
		mImeState = nextState;
	}

	/**
	 * ���õ�����״̬
	 * 
	 * @param resetInlineText
	 */
	private void resetToIdleState(boolean resetInlineText) {
		if (ImeState.STATE_IDLE == mImeState)
			return;

		mImeState = ImeState.STATE_IDLE;
		mDecInfo.reset();

		// ������ʾ����ƴ���ַ����� View
		if (null != mComposingView)
			mComposingView.reset();
		if (resetInlineText)
			commitResultText("");

		resetCandidateWindow();
	}

	/**
	 * ѡ���ѡ�ʣ������������Ƿ������һ����Ԥ����
	 * 
	 * @param candId
	 *            ���candIdС��0 ���Ͷ������ƴ�����в�ѯ��
	 */
	private void chooseAndUpdate(int candId) {

		// �����������뷨״̬
		if (!mInputModeSwitcher.isChineseText()) {
			String choice = mDecInfo.getCandidate(candId);
			if (null != choice) {
				commitResultText(choice);
			}
			resetToIdleState(false);
			return;
		}

		if (ImeState.STATE_PREDICT != mImeState) {
			// Get result candidate list, if choice_id < 0, do a new decoding.
			// If choice_id >=0, select the candidate, and get the new candidate
			// list.
			mDecInfo.chooseDecodingCandidate(candId);
		} else {
			// Choose a prediction item.
			mDecInfo.choosePredictChoice(candId);
		}

		if (mDecInfo.getComposingStr().length() > 0) {
			String resultStr;
			// ��ȡѡ���˵ĺ�ѡ��
			resultStr = mDecInfo.getComposingStrActivePart();

			// choiceId >= 0 means user finishes a choice selection.
			if (candId >= 0 && mDecInfo.canDoPrediction()) {
				// ����ѡ���˵ĺ�ѡ�ʸ�EditText
				commitResultText(resultStr);
				// �������뷨״̬ΪԤ��
				mImeState = ImeState.STATE_PREDICT;
				// TODO ��һ������ʲô��
				if (null != mSkbContainer && mSkbContainer.isShown()) {
					mSkbContainer.toggleCandidateMode(false);
				}

				// Try to get the prediction list.
				// ��ȡԤ���ĺ�ѡ���б�
				if (Settings.getPrediction()) {
					InputConnection ic = getCurrentInputConnection();
					if (null != ic) {
						CharSequence cs = ic.getTextBeforeCursor(3, 0);
						if (null != cs) {
							mDecInfo.preparePredicts(cs);
						}
					}
				} else {
					mDecInfo.resetCandidates();
				}

				if (mDecInfo.mCandidatesList.size() > 0) {
					showCandidateWindow(false);
				} else {
					resetToIdleState(false);
				}
			} else {
				if (ImeState.STATE_IDLE == mImeState) {
					if (mDecInfo.getSplStrDecodedLen() == 0) {
						changeToStateComposing(true);
					} else {
						changeToStateInput(true);
					}
				} else {
					if (mDecInfo.selectionFinished()) {
						changeToStateComposing(true);
					}
				}
				showCandidateWindow(true);
			}
		} else {
			resetToIdleState(false);
		}
	}

	// If activeCandNo is less than 0, get the current active candidate number
	// from candidate view, otherwise use activeCandNo.
	/**
	 * ѡ���ѡ��
	 * 
	 * @param activeCandNo
	 *            ���С��0����ѡ��ǰ�����ĺ�ѡ�ʡ�
	 */
	private void chooseCandidate(int activeCandNo) {
		if (activeCandNo < 0) {
			activeCandNo = mCandidatesContainer.getActiveCandiatePos();
		}
		if (activeCandNo >= 0) {
			chooseAndUpdate(activeCandNo);
		}
	}

	/**
	 * �󶨴ʿ����Զ�̷���PinyinDecoderService
	 * 
	 * @return
	 */
	private boolean startPinyinDecoderService() {
		if (null == mDecInfo.mIPinyinDecoderService) {
			Intent serviceIntent = new Intent();
			serviceIntent.setClass(this, PinyinDecoderService.class);

			if (null == mPinyinDecoderServiceConnection) {
				mPinyinDecoderServiceConnection = new PinyinDecoderServiceConnection();
			}

			// Bind service
			if (bindService(serviceIntent, mPinyinDecoderServiceConnection,
					Context.BIND_AUTO_CREATE)) {
				return true;
			} else {
				return false;
			}
		}
		return true;
	}

	@Override
	public View onCreateCandidatesView() {
		if (mEnvironment.needDebug()) {
			Log.d(TAG, "onCreateCandidatesView.");
		}

		LayoutInflater inflater = getLayoutInflater();

		// ������ʾ����ƴ���ַ���View�ļ�װ��
		// Inflate the floating container view
		mFloatingContainer = (LinearLayout) inflater.inflate(
				R.layout.floating_container, null);

		// The first child is the composing view.
		mComposingView = (ComposingView) mFloatingContainer.getChildAt(0);

		// ���ú�ѡ�ʼ�װ��
		mCandidatesContainer = (CandidatesContainer) inflater.inflate(
				R.layout.activity_settings, null);

		// Create balloon hint for candidates view. ������ѡ������
		mCandidatesBalloon = new BalloonHint(this, mCandidatesContainer,
				MeasureSpec.UNSPECIFIED);
		mCandidatesBalloon.setBalloonBackground(getResources().getDrawable(
				R.drawable.candidate_balloon_bg));
		mCandidatesContainer.initialize(mChoiceNotifier, mCandidatesBalloon,
				mGestureDetectorCandidates);

		// The floating window
		if (null != mFloatingWindow && mFloatingWindow.isShowing()) {
			mFloatingWindowTimer.cancelShowing();
			mFloatingWindow.dismiss();
		}
		mFloatingWindow = new PopupWindow(this);
		mFloatingWindow.setClippingEnabled(false);
		mFloatingWindow.setBackgroundDrawable(null);
		mFloatingWindow.setInputMethodMode(PopupWindow.INPUT_METHOD_NOT_NEEDED);
		mFloatingWindow.setContentView(mFloatingContainer);

		setCandidatesViewShown(true);
		return mCandidatesContainer;
	}

	/**
	 * ��Ӧ����̰����Ĵ�������������̼�װ��SkbContainer��responseKeyEvent�����ĵ��á�
	 * ����̼�װ��SkbContainer��responseKeyEvent�������������е��á�
	 * 
	 * @param sKey
	 */
	public void responseSoftKeyEvent(SoftKey sKey) {
		if (null == sKey)
			return;

		InputConnection ic = getCurrentInputConnection();
		if (ic == null)
			return;

		int keyCode = sKey.getKeyCode();
		Log.e("lichao", "PinyinIME->responseSoftKeyEvent->keyCode=" + keyCode);
		iDataSoftKeyEvent(keyCode);
		// Process some general keys, including KEYCODE_DEL, KEYCODE_SPACE,
		// KEYCODE_ENTER and KEYCODE_DPAD_CENTER.
		if (sKey.isKeyCodeKey()) {// ��ϵͳ��keycode
			// ���ܼ�������
			if (processFunctionKeys(keyCode, true))
				return;
		}

		if (sKey.isUserDefKey()) {// ���û������keycode
			// ͨ�����Ƕ��������̵İ������л����뷨ģʽ��
			updateIcon(mInputModeSwitcher.switchModeForUserKey(keyCode));
			resetToIdleState(false);
			mSkbContainer.updateInputMode();
		} else {
			if (sKey.isKeyCodeKey()) {// ��ϵͳ��keycode
				KeyEvent eDown = new KeyEvent(0, 0, KeyEvent.ACTION_DOWN,
						keyCode, 0, 0, 0, 0, KeyEvent.FLAG_SOFT_KEYBOARD);
				KeyEvent eUp = new KeyEvent(0, 0, KeyEvent.ACTION_UP, keyCode,
						0, 0, 0, 0, KeyEvent.FLAG_SOFT_KEYBOARD);

				onKeyDown(keyCode, eDown);
				onKeyUp(keyCode, eUp);
			} else if (sKey.isUniStrKey()) {// ���ַ�����
				boolean kUsed = false;
				// ��ȡ�������ַ�
				String keyLabel = sKey.getKeyLabel();
				if (mInputModeSwitcher.isChineseTextWithSkb()
						&& (ImeState.STATE_INPUT == mImeState || ImeState.STATE_COMPOSING == mImeState)) {
					if (mDecInfo.length() > 0 && keyLabel.length() == 1
							&& keyLabel.charAt(0) == '\'') {
						// ����ƴ���ָ�����Ȼ����дʿ��ѯ
						processSurfaceChange('\'', 0);
						kUsed = true;
					}
				}
				if (!kUsed) {
					if (ImeState.STATE_INPUT == mImeState) {
						// ���͸�����ѡ�ʸ�EditText
						commitResultText(mDecInfo
								.getCurrentFullSent(mCandidatesContainer
										.getActiveCandiatePos()));
					} else if (ImeState.STATE_COMPOSING == mImeState) {
						// ���� ƴ���ַ������п��ܴ���ѡ�еĺ�ѡ�ʣ� ��EditText
						commitResultText(mDecInfo.getComposingStr());
					}

					// ���� �������ַ� ��EditText
					commitResultText(keyLabel);
					resetToIdleState(false);
				}
			}

			// If the current soft keyboard is not sticky, IME needs to go
			// back to the previous soft keyboard automatically.
			// �����ǰ������̲���ճ�Եģ���ô���뷨��Ҫ������һ�����뷨ģʽ��
			if (!mSkbContainer.isCurrentSkbSticky()) {
				updateIcon(mInputModeSwitcher.requestBackToPreviousSkb());
				resetToIdleState(false);
				mSkbContainer.updateInputMode();
			}
		}
	}
	
	/**
	 * iData���ܼ�����
	 * @param keyCode
	 */
	private void iDataSoftKeyEvent(int keyCode) {
		Intent intent = new Intent();
		switch (keyCode) {
		case IDATA_KEY_DEVICE:
			intent.setClass(PinyinIME.this, ConnectActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
			break;
		case IDATA_KEY_CONNECT_BLUETOOTH:
			Intent intentBlutooth = new Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
			intentBlutooth.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intentBlutooth);
			break;
		default:
			break;
		}
	}

	/**
	 * ��ʾ��ѡ����ͼ
	 * 
	 * @param showComposingView
	 *            �Ƿ���ʾ�����ƴ��View
	 */
	private void showCandidateWindow(boolean showComposingView) {
		if (mEnvironment.needDebug()) {
			Log.d(TAG, "Candidates window is shown. Parent = "
					+ mCandidatesContainer);
		}

		setCandidatesViewShown(true);

		if (null != mSkbContainer)
			mSkbContainer.requestLayout();

		if (null == mCandidatesContainer) {
			resetToIdleState(false);
			return;
		}

		updateComposingText(showComposingView);
		mCandidatesContainer.showCandidates(mDecInfo,
				ImeState.STATE_COMPOSING != mImeState);
		mFloatingWindowTimer.postShowFloatingWindow();
	}

	/**
	 * �رպ�ѡ�ʴ��ڣ����ҹر���������ƴ���ַ����Ĵ���
	 */
	private void dismissCandidateWindow() {
		if (mEnvironment.needDebug()) {
			Log.d(TAG, "Candidates window is to be dismissed");
		}
		if (null == mCandidatesContainer)
			return;
		try {
			mFloatingWindowTimer.cancelShowing();
			mFloatingWindow.dismiss();
		} catch (Exception e) {
			Log.e(TAG, "Fail to show the PopupWindow.");
		}
		setCandidatesViewShown(false);

		if (null != mSkbContainer && mSkbContainer.isShown()) {
			mSkbContainer.toggleCandidateMode(false);
		}
	}

	/**
	 * ���ú�ѡ������
	 */
	private void resetCandidateWindow() {
		if (mEnvironment.needDebug()) {
			Log.d(TAG, "Candidates window is to be reset");
		}
		if (null == mCandidatesContainer)
			return;
		try {
			mFloatingWindowTimer.cancelShowing();
			mFloatingWindow.dismiss();
		} catch (Exception e) {
			Log.e(TAG, "Fail to show the PopupWindow.");
		}

		if (null != mSkbContainer && mSkbContainer.isShown()) {
			mSkbContainer.toggleCandidateMode(false);
		}

		mDecInfo.resetCandidates();

		if (null != mCandidatesContainer && mCandidatesContainer.isShown()) {
			showCandidateWindow(false);
		}
	}

	/**
	 * �������뷨�����ͼ��
	 * 
	 * @param iconId
	 */
	private void updateIcon(int iconId) {
		if (iconId > 0) {
			showStatusIcon(iconId);
		} else {
			hideStatusIcon();
		}
	}

	@Override
	public View onCreateInputView() {
		if (mEnvironment.needDebug()) {
			Log.d(TAG, "onCreateInputView.");
		}
		LayoutInflater inflater = getLayoutInflater();
		mSkbContainer = (SkbContainer) inflater.inflate(R.layout.skb_container,
				null);
		mSkbContainer.setService(this);
		mSkbContainer.setInputModeSwitcher(mInputModeSwitcher);
		mSkbContainer.setGestureDetector(mGestureDetectorSkb);
		return mSkbContainer;
	}

	@Override
	public void onStartInput(EditorInfo editorInfo, boolean restarting) {
		if (mEnvironment.needDebug()) {
			Log.d(TAG,
					"onStartInput " + " ccontentType: "
							+ String.valueOf(editorInfo.inputType)
							+ " Restarting:" + String.valueOf(restarting));
		}
		updateIcon(mInputModeSwitcher.requestInputWithHkb(editorInfo));
		resetToIdleState(false);
	}

	@Override
	public void onStartInputView(EditorInfo editorInfo, boolean restarting) {
		if (mEnvironment.needDebug()) {
			Log.d(TAG,
					"onStartInputView " + " contentType: "
							+ String.valueOf(editorInfo.inputType)
							+ " Restarting:" + String.valueOf(restarting));
		}
		updateIcon(mInputModeSwitcher.requestInputWithSkb(editorInfo));
		resetToIdleState(false);
		mSkbContainer.updateInputMode();
		setCandidatesViewShown(false);
	}

	@Override
	public void onFinishInputView(boolean finishingInput) {
		if (mEnvironment.needDebug()) {
			Log.d(TAG, "onFinishInputView.");
		}
		resetToIdleState(false);
		super.onFinishInputView(finishingInput);
	}

	@Override
	public void onFinishInput() {
		if (mEnvironment.needDebug()) {
			Log.d(TAG, "onFinishInput.");
		}
		resetToIdleState(false);
		super.onFinishInput();
	}

	@Override
	public void onFinishCandidatesView(boolean finishingInput) {
		if (mEnvironment.needDebug()) {
			Log.d(TAG, "onFinishCandidateView.");
		}
		resetToIdleState(false);
		super.onFinishCandidatesView(finishingInput);
	}

	@Override
	public void onDisplayCompletions(CompletionInfo[] completions) {
		// TODO �ú���ʲô����±����ã�
		if (!isFullscreenMode())
			return;
		if (null == completions || completions.length <= 0)
			return;
		if (null == mSkbContainer || !mSkbContainer.isShown())
			return;

		if (!mInputModeSwitcher.isChineseText()
				|| ImeState.STATE_IDLE == mImeState
				|| ImeState.STATE_PREDICT == mImeState) {
			mImeState = ImeState.STATE_APP_COMPLETION;
			// ׼����app��ȡ��ѡ��
			mDecInfo.prepareAppCompletions(completions);
			showCandidateWindow(false);
		}
	}

	/**
	 * ѡ���ѡ�ʺ�Ĵ���������ChoiceNotifier��ʵ��CandidateViewListener��������onClickChoice�����е���
	 * ��
	 * 
	 * @param activeCandNo
	 */
	private void onChoiceTouched(int activeCandNo) {
		if (mImeState == ImeState.STATE_COMPOSING) {
			changeToStateInput(true);
		} else if (mImeState == ImeState.STATE_INPUT
				|| mImeState == ImeState.STATE_PREDICT) {
			// ѡ���ѡ��
			chooseCandidate(activeCandNo);
		} else if (mImeState == ImeState.STATE_APP_COMPLETION) {
			if (null != mDecInfo.mAppCompletions && activeCandNo >= 0
					&& activeCandNo < mDecInfo.mAppCompletions.length) {
				CompletionInfo ci = mDecInfo.mAppCompletions[activeCandNo];
				if (null != ci) {
					InputConnection ic = getCurrentInputConnection();
					// ���ʹ�APP�л�ȡ�ĺ�ѡ�ʸ�EditText
					ic.commitCompletion(ci);
				}
			}
			resetToIdleState(false);
		}
	}

	@Override
	public void requestHideSelf(int flags) {
		if (mEnvironment.needDebug()) {
			Log.d(TAG, "DimissSoftInput.");
		}
		dismissCandidateWindow();
		if (null != mSkbContainer && mSkbContainer.isShown()) {
			mSkbContainer.dismissPopups();
		}
		super.requestHideSelf(flags);
	}

	/**
	 * ѡ��˵��Ի���
	 */
	public void showOptionsMenu() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setCancelable(true);
		builder.setIcon(R.drawable.app_icon);
		builder.setNegativeButton(android.R.string.cancel, null);
		CharSequence itemSettings = getString(R.string.ime_settings_activity_name);
		CharSequence itemInputMethod = "";// =
											// getString(com.android.internal.R.string.inputMethod);
		builder.setItems(new CharSequence[] { itemSettings, itemInputMethod },
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface di, int position) {
						di.dismiss();
						switch (position) {
						case 0:
							launchSettings();

							break;
						case 1:
							// InputMethodManager.getInstance(PinyinIME.this)
							// .showInputMethodPicker();
							break;
						}
					}
				});
		builder.setTitle(getString(R.string.ime_name));
		mOptionsDialog = builder.create();
		Window window = mOptionsDialog.getWindow();
		WindowManager.LayoutParams lp = window.getAttributes();
		lp.token = mSkbContainer.getWindowToken();
		lp.type = WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG;
		window.setAttributes(lp);
		window.addFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
		mOptionsDialog.show();
	}

	/**
	 * ����ϵͳ������ҳ��
	 */
	private void launchSettings() {
		Intent intent = new Intent();
		intent.setClass(PinyinIME.this, SettingsActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}

	/**
	 * ��ʾ�����ƴ���ַ���PopupWindow ��ʱ��
	 * 
	 * @ClassName PopupTimer
	 * @author LiChao
	 */
	private class PopupTimer extends Handler implements Runnable {
		private int mParentLocation[] = new int[2];

		void postShowFloatingWindow() {
			mFloatingContainer.measure(LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT);
			mFloatingWindow.setWidth(mFloatingContainer.getMeasuredWidth());
			mFloatingWindow.setHeight(mFloatingContainer.getMeasuredHeight());
			post(this);
		}

		void cancelShowing() {
			if (mFloatingWindow.isShowing()) {
				mFloatingWindow.dismiss();
			}
			removeCallbacks(this);
		}

		public void run() {
			// ��ȡ��ѡ��װ���λ��
			mCandidatesContainer.getLocationInWindow(mParentLocation);

			if (!mFloatingWindow.isShowing()) {
				// ��ʾ��ѡ��PopupWindow
				mFloatingWindow.showAtLocation(mCandidatesContainer,
						Gravity.LEFT | Gravity.TOP, mParentLocation[0],
						mParentLocation[1] - mFloatingWindow.getHeight());
			} else {
				// ���º�ѡ��PopupWindow
				mFloatingWindow
						.update(mParentLocation[0], mParentLocation[1]
								- mFloatingWindow.getHeight(),
								mFloatingWindow.getWidth(),
								mFloatingWindow.getHeight());
			}
		}
	}

	/**
	 * Used to notify IME that the user selects a candidate or performs an
	 * gesture. ���û�ѡ���˺�ѡ�ʻ����ں�ѡ����ͼ����������ʱ��֪ͨ���뷨��ʵ���˺�ѡ����ͼ�ļ�����CandidateViewListener��
	 * ��ѡ���ѡ�ʵĴ��������������һ����Ĵ��������������󻬶��Ĵ����� ���������ϻ����Ĵ��������������»����Ĵ�������
	 */
	public class ChoiceNotifier extends Handler implements
			CandidateViewListener {
		PinyinIME mIme;

		ChoiceNotifier(PinyinIME ime) {
			mIme = ime;
		}

		public void onClickChoice(int choiceId) {
			if (choiceId >= 0) {
				mIme.onChoiceTouched(choiceId);
			}
		}

		public void onToLeftGesture() {
			if (ImeState.STATE_COMPOSING == mImeState) {
				changeToStateInput(true);
			}
			mCandidatesContainer.pageForward(true, false);
		}

		public void onToRightGesture() {
			if (ImeState.STATE_COMPOSING == mImeState) {
				changeToStateInput(true);
			}
			mCandidatesContainer.pageBackward(true, false);
		}

		public void onToTopGesture() {
		}

		public void onToBottomGesture() {
		}
	}

	/**
	 * ���Ƽ�����
	 * 
	 * @ClassName OnGestureListener
	 * @author LiChao
	 */
	public class OnGestureListener extends
			GestureDetector.SimpleOnGestureListener {
		/**
		 * When user presses and drags, the minimum x-distance to make a
		 * response to the drag event. ���û���ק��ʱ��x������С�Ĳ�ֵ�ſ��Բ�����ק�¼���
		 */
		private static final int MIN_X_FOR_DRAG = 60;

		/**
		 * When user presses and drags, the minimum y-distance to make a
		 * response to the drag event.���û���ק��ʱ��y������С�Ĳ�ֵ�ſ��Բ�����ק�¼���
		 */
		private static final int MIN_Y_FOR_DRAG = 40;

		/**
		 * Velocity threshold for a screen-move gesture. If the minimum
		 * x-velocity is less than it, no
		 * gesture.x���ϵ����Ƶ���С���ʷ�ֵ��С�������ֵ���Ͳ������ơ�ֻҪ�ڻ������ڼ�
		 * ��������һ�ε�����С�����ֵ�����ж���εĻ�����������mNotGesture = true���������ȥ���������ʱ��Ҳ��û�á�
		 */
		static private final float VELOCITY_THRESHOLD_X1 = 0.3f;

		/**
		 * Velocity threshold for a screen-move gesture. If the maximum
		 * x-velocity is less than it, no
		 * gesture.x���ϵ����Ƶ�������ʷ�ֵ�����������ֵ����һ�������ƣ�mGestureRecognized = true��
		 */
		static private final float VELOCITY_THRESHOLD_X2 = 0.7f;

		/**
		 * Velocity threshold for a screen-move gesture. If the minimum
		 * y-velocity is less than it, no
		 * gesture.y���ϵ����Ƶ���С���ʷ�ֵ��С�������ֵ���Ͳ������ơ�ֻҪ�ڻ������ڼ�
		 * ��������һ�ε�����С�����ֵ�����ж���εĻ�����������mNotGesture =
		 * true���������ȥ���������ʱ��Ҳ��û�ã�mGestureRecognized = true��
		 */
		static private final float VELOCITY_THRESHOLD_Y1 = 0.2f;

		/**
		 * Velocity threshold for a screen-move gesture. If the maximum
		 * y-velocity is less than it, no gesture.y���ϵ����Ƶ�������ʷ�ֵ�����������ֵ����һ�������ơ�
		 */
		static private final float VELOCITY_THRESHOLD_Y2 = 0.45f;

		/** If it false, we will not response detected gestures. �Ƿ���Ӧ��⵽������ */
		private boolean mReponseGestures;

		/** The minimum X velocity observed in the gesture. �ܼ�⵽��x��С���ʵ����� */
		private float mMinVelocityX = Float.MAX_VALUE;

		/** The minimum Y velocity observed in the gesture. �ܼ�⵽y��С���ʵ����� */
		private float mMinVelocityY = Float.MAX_VALUE;

		/**
		 * The first down time for the series of touch events for an
		 * action.��һ�δ����¼���ʱ��
		 */
		private long mTimeDown;

		/** The last time when onScroll() is called.���һ�� onScroll���������õ�ʱ�� */
		private long mTimeLastOnScroll;

		/**
		 * This flag used to indicate that this gesture is not a gesture.
		 * �Ƿ���һ�����ƣ�
		 */
		private boolean mNotGesture;

		/**
		 * This flag used to indicate that this gesture has been recognized.
		 * �Ƿ���һ�����ϵ����ƣ�
		 */
		private boolean mGestureRecognized;

		public OnGestureListener(boolean reponseGestures) {
			mReponseGestures = reponseGestures;
		}

		@Override
		public boolean onDown(MotionEvent e) {
			mMinVelocityX = Integer.MAX_VALUE;
			mMinVelocityY = Integer.MAX_VALUE;
			mTimeDown = e.getEventTime();
			mTimeLastOnScroll = mTimeDown;
			mNotGesture = false;
			mGestureRecognized = false;
			return false;
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			if (mNotGesture)
				return false;
			if (mGestureRecognized)
				return true;

			if (Math.abs(e1.getX() - e2.getX()) < MIN_X_FOR_DRAG
					&& Math.abs(e1.getY() - e2.getY()) < MIN_Y_FOR_DRAG)
				return false;

			long timeNow = e2.getEventTime();
			long spanTotal = timeNow - mTimeDown;
			long spanThis = timeNow - mTimeLastOnScroll;
			if (0 == spanTotal)
				spanTotal = 1;
			if (0 == spanThis)
				spanThis = 1;

			// ����������
			float vXTotal = (e2.getX() - e1.getX()) / spanTotal;
			float vYTotal = (e2.getY() - e1.getY()) / spanTotal;

			// The distances are from the current point to the previous one.
			// ������� onScroll ������
			float vXThis = -distanceX / spanThis;
			float vYThis = -distanceY / spanThis;

			float kX = vXTotal * vXThis;
			float kY = vYTotal * vYThis;
			float k1 = kX + kY;
			float k2 = Math.abs(kX) + Math.abs(kY);

			// TODO �����ʲô���㹫ʽ��
			if (k1 / k2 < 0.8) {
				mNotGesture = true;
				return false;
			}
			float absVXTotal = Math.abs(vXTotal);
			float absVYTotal = Math.abs(vYTotal);
			if (absVXTotal < mMinVelocityX) {
				mMinVelocityX = absVXTotal;
			}
			if (absVYTotal < mMinVelocityY) {
				mMinVelocityY = absVYTotal;
			}

			// �����С�����ʱȹ涨��С����ô�Ͳ������ơ�
			if (mMinVelocityX < VELOCITY_THRESHOLD_X1
					&& mMinVelocityY < VELOCITY_THRESHOLD_Y1) {
				mNotGesture = true;
				return false;
			}

			// �ж���ʲô���ƣ����������ƴ�������
			if (vXTotal > VELOCITY_THRESHOLD_X2
					&& absVYTotal < VELOCITY_THRESHOLD_Y2) {
				if (mReponseGestures)
					onDirectionGesture(Gravity.RIGHT);
				mGestureRecognized = true;
			} else if (vXTotal < -VELOCITY_THRESHOLD_X2
					&& absVYTotal < VELOCITY_THRESHOLD_Y2) {
				if (mReponseGestures)
					onDirectionGesture(Gravity.LEFT);
				mGestureRecognized = true;
			} else if (vYTotal > VELOCITY_THRESHOLD_Y2
					&& absVXTotal < VELOCITY_THRESHOLD_X2) {
				if (mReponseGestures)
					onDirectionGesture(Gravity.BOTTOM);
				mGestureRecognized = true;
			} else if (vYTotal < -VELOCITY_THRESHOLD_Y2
					&& absVXTotal < VELOCITY_THRESHOLD_X2) {
				if (mReponseGestures)
					onDirectionGesture(Gravity.TOP);
				mGestureRecognized = true;
			}

			mTimeLastOnScroll = timeNow;
			return mGestureRecognized;
		}

		@Override
		public boolean onFling(MotionEvent me1, MotionEvent me2,
				float velocityX, float velocityY) {
			return mGestureRecognized;
		}

		/**
		 * ���ƵĴ�����
		 * 
		 * @param gravity
		 *            ���Ƶ����
		 */
		public void onDirectionGesture(int gravity) {
			if (Gravity.NO_GRAVITY == gravity) {
				return;
			}

			if (Gravity.LEFT == gravity || Gravity.RIGHT == gravity) {
				if (mCandidatesContainer.isShown()) {
					if (Gravity.LEFT == gravity) {
						mCandidatesContainer.pageForward(true, true);
					} else {
						mCandidatesContainer.pageBackward(true, true);
					}
					return;
				}
			}
		}
	}

	/**
	 * Connection used for binding to the Pinyin decoding service.
	 * �ʿ����Զ�̷���PinyinDecoderService �ļ�����
	 */
	public class PinyinDecoderServiceConnection implements ServiceConnection {
		public void onServiceConnected(ComponentName name, IBinder service) {
			mDecInfo.mIPinyinDecoderService = IPinyinDecoderService.Stub
					.asInterface(service);
		}

		public void onServiceDisconnected(ComponentName name) {
		}
	}

	/**
	 * ���뷨״̬
	 */
	public enum ImeState {
		STATE_BYPASS, STATE_IDLE, STATE_INPUT, STATE_COMPOSING, STATE_PREDICT, STATE_APP_COMPLETION
	}

	/**
	 * �ʿ�����������
	 * 
	 * @ClassName DecodingInfo
	 * @author LiChao
	 */
	public class DecodingInfo {
		/**
		 * Maximum length of the Pinyin string
		 * �����ַ����ĳ��ȣ���ʵֻ��27����Ϊ���һλΪ0����mPyBuf[]�ĳ���
		 */
		private static final int PY_STRING_MAX = 28;

		/**
		 * Maximum number of candidates to display in one page. һҳ��ʾ��ѡ�ʵ�������
		 */
		private static final int MAX_PAGE_SIZE_DISPLAY = 10;

		/**
		 * Spelling (Pinyin) string. ƴ���ַ���
		 */
		private StringBuffer mSurface;

		/**
		 * Byte buffer used as the Pinyin string parameter for native function
		 * call. �ַ���������Ϊƴ���ַ������������غ������ã����ĳ���ΪPY_STRING_MAX�����һλΪ0
		 */
		private byte mPyBuf[];

		/**
		 * The length of surface string successfully decoded by engine.
		 * �ɹ�������ַ�������
		 */
		private int mSurfaceDecodedLen;

		/**
		 * Composing string. ƴ���ַ���
		 */
		private String mComposingStr;

		/**
		 * Length of the active composing string. ���ƴ���ַ�������
		 */
		private int mActiveCmpsLen;

		/**
		 * Composing string for display, it is copied from mComposingStr, and
		 * add spaces between spellings.
		 * ��ʾ��ƴ���ַ������Ǵ�mComposingStr���ƹ����ģ�������ƴд֮������˿ո�
		 **/
		private String mComposingStrDisplay;

		/**
		 * Length of the active composing string for display. ��ʾ��ƴ���ַ����ĳ���
		 */
		private int mActiveCmpsDisplayLen;

		/**
		 * The first full sentence choice. ��һ���������ӣ���һ����ѡ�ʡ�
		 */
		private String mFullSent;

		/**
		 * Number of characters which have been fixed. �̶����ַ�������
		 */
		private int mFixedLen;

		/**
		 * If this flag is true, selection is finished. �Ƿ�ѡ������ˣ�
		 */
		private boolean mFinishSelection;

		/**
		 * The starting position for each spelling. The first one is the number
		 * of the real starting position elements. ÿ��ƴд�Ŀ�ʼλ�ã��²⣺��һ��Ԫ����ƴд����������
		 */
		private int mSplStart[];

		/**
		 * Editing cursor in mSurface. ����λ��
		 */
		private int mCursorPos;

		/**
		 * Remote Pinyin-to-Hanzi decoding engine service. ��������Զ�̷���
		 */
		private IPinyinDecoderService mIPinyinDecoderService;

		/**
		 * The complication information suggested by application. Ӧ�õĲ���������Ϣ
		 */
		private CompletionInfo[] mAppCompletions;

		/**
		 * The total number of choices for display. The list may only contains
		 * the first part. If user tries to navigate to next page which is not
		 * in the result list, we need to get these items. ��ʾ�Ŀ�ѡ�������
		 **/
		public int mTotalChoicesNum;

		/**
		 * Candidate list. The first one is the full-sentence candidate. ��ѡ���б�
		 */
		public List<String> mCandidatesList = new Vector<String>();

		/**
		 * Element i stores the starting position of page i. ҳ�Ŀ�ʼλ��
		 */
		public Vector<Integer> mPageStart = new Vector<Integer>();

		/**
		 * Element i stores the number of characters to page i. ÿһҳ������
		 */
		public Vector<Integer> mCnToPage = new Vector<Integer>();

		/**
		 * The position to delete in Pinyin string. If it is less than 0, IME
		 * will do an incremental search, otherwise IME will do a deletion
		 * operation. if {@link #mIsPosInSpl} is true, IME will delete the whole
		 * string for mPosDelSpl-th spelling, otherwise it will only delete
		 * mPosDelSpl-th character in the Pinyin string. ��ƴ���ַ����е�ɾ��λ��
		 */
		public int mPosDelSpl = -1;

		/**
		 * If {@link #mPosDelSpl} is big than or equal to 0, this member is used
		 * to indicate that whether the postion is counted in spelling id or
		 * character. ��� mPosDelSpl ���ڵ��� 0����ô������������ڱ����Ƿ��� ƴд��id ���� �ַ���
		 */
		public boolean mIsPosInSpl;

		public DecodingInfo() {
			mSurface = new StringBuffer();
			mSurfaceDecodedLen = 0;
		}

		/**
		 * ����
		 */
		public void reset() {
			mSurface.delete(0, mSurface.length());
			mSurfaceDecodedLen = 0;
			mCursorPos = 0;
			mFullSent = "";
			mFixedLen = 0;
			mFinishSelection = false;
			mComposingStr = "";
			mComposingStrDisplay = "";
			mActiveCmpsLen = 0;
			mActiveCmpsDisplayLen = 0;

			resetCandidates();
		}

		/**
		 * ��ѡ���б��Ƿ�Ϊ��
		 * 
		 * @return
		 */
		public boolean isCandidatesListEmpty() {
			return mCandidatesList.size() == 0;
		}

		/**
		 * ƴд���ַ����Ƿ�����
		 * 
		 * @return
		 */
		public boolean isSplStrFull() {
			if (mSurface.length() >= PY_STRING_MAX - 1)
				return true;
			return false;
		}

		/**
		 * ����ƴд�ַ�
		 * 
		 * @param ch
		 * @param reset
		 *            ƴд�ַ��Ƿ�����
		 */
		public void addSplChar(char ch, boolean reset) {
			if (reset) {
				mSurface.delete(0, mSurface.length());
				mSurfaceDecodedLen = 0;
				mCursorPos = 0;
				try {
					mIPinyinDecoderService.imResetSearch();
				} catch (RemoteException e) {
				}
			}
			mSurface.insert(mCursorPos, ch);
			mCursorPos++;
		}

		// Prepare to delete before cursor. We may delete a spelling char if
		// the cursor is in the range of unfixed part, delete a whole spelling
		// if the cursor in inside the range of the fixed part.
		// This function only marks the position used to delete.
		/**
		 * ɾ��ǰ��׼�����ú���ֻ�Ǳ��Ҫɾ����λ�á�
		 */
		public void prepareDeleteBeforeCursor() {
			if (mCursorPos > 0) {
				int pos;

				for (pos = 0; pos < mFixedLen; pos++) {
					if (mSplStart[pos + 2] >= mCursorPos
							&& mSplStart[pos + 1] < mCursorPos) {
						// ɾ��һ��ƴд�ַ���
						mPosDelSpl = pos;
						mCursorPos = mSplStart[pos + 1];
						mIsPosInSpl = true;
						break;
					}
				}

				if (mPosDelSpl < 0) {
					// ɾ��һ���ַ�
					mPosDelSpl = mCursorPos - 1;
					mCursorPos--;
					mIsPosInSpl = false;
				}
			}
		}

		/**
		 * ��ȡƴ���ַ�������
		 * 
		 * @return
		 */
		public int length() {
			return mSurface.length();
		}

		/**
		 * ���ƴ���ַ�����ָ��λ�õ��ַ�
		 * 
		 * @param index
		 * @return
		 */
		public char charAt(int index) {
			return mSurface.charAt(index);
		}

		/**
		 * ���ƴ���ַ���
		 * 
		 * @return
		 */
		public StringBuffer getOrigianlSplStr() {
			return mSurface;
		}

		/**
		 * ��óɹ�������ַ�������
		 * 
		 * @return
		 */
		public int getSplStrDecodedLen() {
			return mSurfaceDecodedLen;
		}

		/**
		 * ���ÿ��ƴд�ַ����Ŀ�ʼλ��
		 * 
		 * @return
		 */
		public int[] getSplStart() {
			return mSplStart;
		}

		/**
		 * ��ȡƴ���ַ������п��ܴ���ѡ�еĺ�ѡ��
		 * 
		 * @return
		 */
		public String getComposingStr() {
			return mComposingStr;
		}

		/**
		 * ��ȡ���ƴ���ַ���������ѡ���˵ĺ�ѡ�ʡ�
		 * 
		 * @return
		 */
		public String getComposingStrActivePart() {
			assert (mActiveCmpsLen <= mComposingStr.length());
			return mComposingStr.substring(0, mActiveCmpsLen);
		}

		/**
		 * ��û��ƴ���ַ�������
		 * 
		 * @return
		 */
		public int getActiveCmpsLen() {
			return mActiveCmpsLen;
		}

		/**
		 * ��ȡ��ʾ��ƴ���ַ���
		 * 
		 * @return
		 */
		public String getComposingStrForDisplay() {
			return mComposingStrDisplay;
		}

		/**
		 * ��ʾ��ƴ���ַ����ĳ���
		 * 
		 * @return
		 */
		public int getActiveCmpsDisplayLen() {
			return mActiveCmpsDisplayLen;
		}

		/**
		 * ��һ����������
		 * 
		 * @return
		 */
		public String getFullSent() {
			return mFullSent;
		}

		/**
		 * ��ȡ��ǰ��������
		 * 
		 * @param activeCandPos
		 * @return
		 */
		public String getCurrentFullSent(int activeCandPos) {
			try {
				String retStr = mFullSent.substring(0, mFixedLen);
				retStr += mCandidatesList.get(activeCandPos);
				return retStr;
			} catch (Exception e) {
				return "";
			}
		}

		/**
		 * ���ú�ѡ���б�
		 */
		public void resetCandidates() {
			mCandidatesList.clear();
			mTotalChoicesNum = 0;

			mPageStart.clear();
			mPageStart.add(0);
			mCnToPage.clear();
			mCnToPage.add(0);
		}

		/**
		 * ��ѡ������app���ж����뷨״̬ mImeState == ImeState.STATE_APP_COMPLETION��
		 * 
		 * @return
		 */
		public boolean candidatesFromApp() {
			return ImeState.STATE_APP_COMPLETION == mImeState;
		}

		/**
		 * �ж� mComposingStr.length() == mFixedLen ��
		 * 
		 * @return
		 */
		public boolean canDoPrediction() {
			return mComposingStr.length() == mFixedLen;
		}

		/**
		 * ѡ���Ƿ����
		 * 
		 * @return
		 */
		public boolean selectionFinished() {
			return mFinishSelection;
		}

		// After the user chooses a candidate, input method will do a
		// re-decoding and give the new candidate list.
		// If candidate id is less than 0, means user is inputting Pinyin,
		// not selecting any choice.
		/**
		 * ���candId��0����ѡ��һ����ѡ�ʣ��������»�ȡһ����ѡ���б�ѡ��ĺ�ѡ�ʴ����mComposingStr�У�ͨ��mDecInfo.
		 * getComposingStrActivePart()ȡ���������candIdС��0 ���Ͷ������ƴ�����в�ѯ��
		 * 
		 * @param candId
		 */
		private void chooseDecodingCandidate(int candId) {
			if (mImeState != ImeState.STATE_PREDICT) {
				resetCandidates();
				int totalChoicesNum = 0;
				try {
					if (candId < 0) {
						if (length() == 0) {
							totalChoicesNum = 0;
						} else {
							if (mPyBuf == null)
								mPyBuf = new byte[PY_STRING_MAX];
							for (int i = 0; i < length(); i++)
								mPyBuf[i] = (byte) charAt(i);
							mPyBuf[length()] = 0;

							if (mPosDelSpl < 0) {
								totalChoicesNum = mIPinyinDecoderService
										.imSearch(mPyBuf, length());
							} else {
								boolean clear_fixed_this_step = true;
								if (ImeState.STATE_COMPOSING == mImeState) {
									clear_fixed_this_step = false;
								}
								totalChoicesNum = mIPinyinDecoderService
										.imDelSearch(mPosDelSpl, mIsPosInSpl,
												clear_fixed_this_step);
								mPosDelSpl = -1;
							}
						}
					} else {
						totalChoicesNum = mIPinyinDecoderService
								.imChoose(candId);
					}
				} catch (RemoteException e) {
				}
				updateDecInfoForSearch(totalChoicesNum);
			}
		}

		/**
		 * ���²�ѯ�ʿ�����Ϣ
		 * 
		 * @param totalChoicesNum
		 */
		private void updateDecInfoForSearch(int totalChoicesNum) {
			mTotalChoicesNum = totalChoicesNum;
			if (mTotalChoicesNum < 0) {
				mTotalChoicesNum = 0;
				return;
			}

			try {
				String pyStr;

				mSplStart = mIPinyinDecoderService.imGetSplStart();
				pyStr = mIPinyinDecoderService.imGetPyStr(false);
				mSurfaceDecodedLen = mIPinyinDecoderService.imGetPyStrLen(true);
				assert (mSurfaceDecodedLen <= pyStr.length());

				mFullSent = mIPinyinDecoderService.imGetChoice(0);
				mFixedLen = mIPinyinDecoderService.imGetFixedLen();

				// Update the surface string to the one kept by engine.
				mSurface.replace(0, mSurface.length(), pyStr);

				if (mCursorPos > mSurface.length())
					mCursorPos = mSurface.length();
				mComposingStr = mFullSent.substring(0, mFixedLen)
						+ mSurface.substring(mSplStart[mFixedLen + 1]);

				mActiveCmpsLen = mComposingStr.length();
				if (mSurfaceDecodedLen > 0) {
					mActiveCmpsLen = mActiveCmpsLen
							- (mSurface.length() - mSurfaceDecodedLen);
				}

				// Prepare the display string.
				if (0 == mSurfaceDecodedLen) {
					mComposingStrDisplay = mComposingStr;
					mActiveCmpsDisplayLen = mComposingStr.length();
				} else {
					mComposingStrDisplay = mFullSent.substring(0, mFixedLen);
					for (int pos = mFixedLen + 1; pos < mSplStart.length - 1; pos++) {
						mComposingStrDisplay += mSurface.substring(
								mSplStart[pos], mSplStart[pos + 1]);
						if (mSplStart[pos + 1] < mSurfaceDecodedLen) {
							mComposingStrDisplay += " ";
						}
					}
					mActiveCmpsDisplayLen = mComposingStrDisplay.length();
					if (mSurfaceDecodedLen < mSurface.length()) {
						mComposingStrDisplay += mSurface
								.substring(mSurfaceDecodedLen);
					}
				}

				if (mSplStart.length == mFixedLen + 2) {
					mFinishSelection = true;
				} else {
					mFinishSelection = false;
				}
			} catch (RemoteException e) {
				Log.w(TAG, "PinyinDecoderService died", e);
			} catch (Exception e) {
				mTotalChoicesNum = 0;
				mComposingStr = "";
			}
			// Prepare page 0.
			if (!mFinishSelection) {
				preparePage(0);
			}
		}

		/**
		 * ѡ��Ԥ����ѡ��
		 * 
		 * @param choiceId
		 */
		private void choosePredictChoice(int choiceId) {
			if (ImeState.STATE_PREDICT != mImeState || choiceId < 0
					|| choiceId >= mTotalChoicesNum) {
				return;
			}

			String tmp = mCandidatesList.get(choiceId);

			resetCandidates();

			mCandidatesList.add(tmp);
			mTotalChoicesNum = 1;

			mSurface.replace(0, mSurface.length(), "");
			mCursorPos = 0;
			mFullSent = tmp;
			mFixedLen = tmp.length();
			mComposingStr = mFullSent;
			mActiveCmpsLen = mFixedLen;

			mFinishSelection = true;
		}

		/**
		 * ���ָ���ĺ�ѡ��
		 * 
		 * @param candId
		 * @return
		 */
		public String getCandidate(int candId) {
			// Only loaded items can be gotten, so we use mCandidatesList.size()
			// instead mTotalChoiceNum.
			if (candId < 0 || candId > mCandidatesList.size()) {
				return null;
			}
			return mCandidatesList.get(candId);
		}

		/**
		 * �ӻ����л�ȡһҳ�ĺ�ѡ�ʣ�Ȼ��Ž�mCandidatesList�С����ֲ�ͬ�Ļ�ȡ��ʽ��1��mIPinyinDecoderService.
		 * imGetChoiceList
		 * ������2��mIPinyinDecoderService.imGetPredictList��3����mAppCompletions[]ȡ��
		 */
		private void getCandiagtesForCache() {
			int fetchStart = mCandidatesList.size();
			int fetchSize = mTotalChoicesNum - fetchStart;
			if (fetchSize > MAX_PAGE_SIZE_DISPLAY) {
				fetchSize = MAX_PAGE_SIZE_DISPLAY;
			}
			try {
				List<String> newList = null;
				if (ImeState.STATE_INPUT == mImeState
						|| ImeState.STATE_IDLE == mImeState
						|| ImeState.STATE_COMPOSING == mImeState) {
					newList = mIPinyinDecoderService.imGetChoiceList(
							fetchStart, fetchSize, mFixedLen);
				} else if (ImeState.STATE_PREDICT == mImeState) {
					newList = mIPinyinDecoderService.imGetPredictList(
							fetchStart, fetchSize);
				} else if (ImeState.STATE_APP_COMPLETION == mImeState) {
					newList = new ArrayList<String>();
					if (null != mAppCompletions) {
						for (int pos = fetchStart; pos < fetchSize; pos++) {
							CompletionInfo ci = mAppCompletions[pos];
							if (null != ci) {
								CharSequence s = ci.getText();
								if (null != s)
									newList.add(s.toString());
							}
						}
					}
				}
				mCandidatesList.addAll(newList);
			} catch (RemoteException e) {
				Log.w(TAG, "PinyinDecoderService died", e);
			}
		}

		/**
		 * �ж�ָ��ҳ�Ƿ�׼�����ˣ�
		 * 
		 * @param pageNo
		 * @return
		 */
		public boolean pageReady(int pageNo) {
			// If the page number is less than 0, return false
			if (pageNo < 0)
				return false;

			// Page pageNo's ending information is not ready.
			if (mPageStart.size() <= pageNo + 1) {
				return false;
			}

			return true;
		}

		/**
		 * ׼��ָ��ҳ���ӻ�����ȡ��ָ��ҳ�ĺ�ѡ�ʡ�
		 * 
		 * @param pageNo
		 * @return
		 */
		public boolean preparePage(int pageNo) {
			// If the page number is less than 0, return false
			if (pageNo < 0)
				return false;

			// Make sure the starting information for page pageNo is ready.
			if (mPageStart.size() <= pageNo) {
				return false;
			}

			// Page pageNo's ending information is also ready.
			if (mPageStart.size() > pageNo + 1) {
				return true;
			}

			// If cached items is enough for page pageNo.
			if (mCandidatesList.size() - mPageStart.elementAt(pageNo) >= MAX_PAGE_SIZE_DISPLAY) {
				return true;
			}

			// Try to get more items from engine
			getCandiagtesForCache();

			// Try to find if there are available new items to display.
			// If no new item, return false;
			if (mPageStart.elementAt(pageNo) >= mCandidatesList.size()) {
				return false;
			}

			// If there are new items, return true;
			return true;
		}

		/**
		 * ׼��Ԥ����ѡ��
		 * 
		 * @param history
		 */
		public void preparePredicts(CharSequence history) {
			if (null == history)
				return;

			resetCandidates();

			if (Settings.getPrediction()) {
				String preEdit = history.toString();
				int predictNum = 0;
				if (null != preEdit) {
					try {
						mTotalChoicesNum = mIPinyinDecoderService
								.imGetPredictsNum(preEdit);
					} catch (RemoteException e) {
						return;
					}
				}
			}

			preparePage(0);
			mFinishSelection = false;
		}

		/**
		 * ׼����app��ȡ��ѡ��
		 * 
		 * @param completions
		 */
		private void prepareAppCompletions(CompletionInfo completions[]) {
			resetCandidates();
			mAppCompletions = completions;
			mTotalChoicesNum = completions.length;
			preparePage(0);
			mFinishSelection = false;
			return;
		}

		/**
		 * ��ȡ��ǰҳ�ĳ���
		 * 
		 * @param currentPage
		 * @return
		 */
		public int getCurrentPageSize(int currentPage) {
			if (mPageStart.size() <= currentPage + 1)
				return 0;
			return mPageStart.elementAt(currentPage + 1)
					- mPageStart.elementAt(currentPage);
		}

		/**
		 * ��ȡ��ǰҳ�Ŀ�ʼλ��
		 * 
		 * @param currentPage
		 * @return
		 */
		public int getCurrentPageStart(int currentPage) {
			if (mPageStart.size() < currentPage + 1)
				return mTotalChoicesNum;
			return mPageStart.elementAt(currentPage);
		}

		/**
		 * �Ƿ�����һҳ��
		 * 
		 * @param currentPage
		 * @return
		 */
		public boolean pageForwardable(int currentPage) {
			if (mPageStart.size() <= currentPage + 1)
				return false;
			if (mPageStart.elementAt(currentPage + 1) >= mTotalChoicesNum) {
				return false;
			}
			return true;
		}

		/**
		 * �Ƿ�����һҳ
		 * 
		 * @param currentPage
		 * @return
		 */
		public boolean pageBackwardable(int currentPage) {
			if (currentPage > 0)
				return true;
			return false;
		}

		/**
		 * ���ǰ����ַ��Ƿ��Ƿָ�����'��
		 * 
		 * @return
		 */
		public boolean charBeforeCursorIsSeparator() {
			int len = mSurface.length();
			if (mCursorPos > len)
				return false;
			if (mCursorPos > 0 && mSurface.charAt(mCursorPos - 1) == '\'') {
				return true;
			}
			return false;
		}

		/**
		 * ��ȡ���λ��
		 * 
		 * @return
		 */
		public int getCursorPos() {
			return mCursorPos;
		}

		/**
		 * ��ȡ�����ƴ���ַ����е�λ��
		 * 
		 * @return
		 */
		public int getCursorPosInCmps() {
			int cursorPos = mCursorPos;
			int fixedLen = 0;

			for (int hzPos = 0; hzPos < mFixedLen; hzPos++) {
				if (mCursorPos >= mSplStart[hzPos + 2]) {
					cursorPos -= mSplStart[hzPos + 2] - mSplStart[hzPos + 1];
					cursorPos += 1;
				}
			}
			return cursorPos;
		}

		/**
		 * ��ȡ�������ʾ��ƴ���ַ����е�λ��
		 * 
		 * @return
		 */
		public int getCursorPosInCmpsDisplay() {
			int cursorPos = getCursorPosInCmps();
			// +2 is because: one for mSplStart[0], which is used for other
			// purpose(The length of the segmentation string), and another
			// for the first spelling which does not need a space before it.
			for (int pos = mFixedLen + 2; pos < mSplStart.length - 1; pos++) {
				if (mCursorPos <= mSplStart[pos]) {
					break;
				} else {
					cursorPos++;
				}
			}
			return cursorPos;
		}

		/**
		 * �ƶ���굽ĩβ
		 * 
		 * @param left
		 */
		public void moveCursorToEdge(boolean left) {
			if (left)
				mCursorPos = 0;
			else
				mCursorPos = mSurface.length();
		}

		// Move cursor. If offset is 0, this function can be used to adjust
		// the cursor into the bounds of the string.
		/**
		 * �ƶ����
		 * 
		 * @param offset
		 */
		public void moveCursor(int offset) {
			if (offset > 1 || offset < -1)
				return;

			if (offset != 0) {
				int hzPos = 0;
				for (hzPos = 0; hzPos <= mFixedLen; hzPos++) {
					if (mCursorPos == mSplStart[hzPos + 1]) {
						if (offset < 0) {
							if (hzPos > 0) {
								offset = mSplStart[hzPos]
										- mSplStart[hzPos + 1];
							}
						} else {
							if (hzPos < mFixedLen) {
								offset = mSplStart[hzPos + 2]
										- mSplStart[hzPos + 1];
							}
						}
						break;
					}
				}
			}
			mCursorPos += offset;
			if (mCursorPos < 0) {
				mCursorPos = 0;
			} else if (mCursorPos > mSurface.length()) {
				mCursorPos = mSurface.length();
			}
		}

		/**
		 * ��ȡƴд�ַ���������
		 * 
		 * @return
		 */
		public int getSplNum() {
			return mSplStart[0];
		}

		/**
		 * ��ȡ�̶����ַ�������
		 * 
		 * @return
		 */
		public int getFixedLen() {
			return mFixedLen;
		}
	}
}