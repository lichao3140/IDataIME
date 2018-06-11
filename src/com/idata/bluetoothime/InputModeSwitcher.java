package com.idata.bluetoothime;

import android.content.res.Resources;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import com.idata.bluetoothime.SoftKeyboard.KeyRow;

/**
 * 
 * ���뷨ģʽת�������������뷨������̡�
 * 
 * @author LiChao
 * 
 */
public class InputModeSwitcher {
	/**
	 * User defined key code, used by soft keyboard.
	 * �û������key��code����������̡�shift����code��
	 */
	private static final int USERDEF_KEYCODE_SHIFT_1 = -1;

	/**
	 * User defined key code, used by soft keyboard. ���Լ���code
	 */
	private static final int USERDEF_KEYCODE_LANG_2 = -2;

	/**
	 * User defined key code, used by soft keyboard. ����123������code�������������ּ��̵��л���
	 */
	private static final int USERDEF_KEYCODE_SYM_3 = -3;

	/**
	 * User defined key code, used by soft keyboard.
	 */
	public static final int USERDEF_KEYCODE_PHONE_SYM_4 = -4;

	/**
	 * User defined key code, used by soft keyboard.
	 */
	private static final int USERDEF_KEYCODE_MORE_SYM_5 = -5;

	/**
	 * User defined key code, used by soft keyboard. ΢Ц������code����������ʱ�����Լ��������Ǹ�������
	 */
	private static final int USERDEF_KEYCODE_SMILEY_6 = -6;

	/**
	 * Bits used to indicate soft keyboard layout. If none bit is set, the
	 * current input mode does not require a soft keyboard.
	 * ��8λָ������̵Ĳ��֡������8λΪ0����ô�ͱ�����ǰ���뷨ģʽ����Ҫ����̡�
	 **/
	private static final int MASK_SKB_LAYOUT = 0xf0000000;

	/**
	 * A kind of soft keyboard layout. An input mode should be anded with
	 * {@link #MASK_SKB_LAYOUT} to get its soft keyboard layout. ָ����׼�Ĵ�ͳ����
	 */
	private static final int MASK_SKB_LAYOUT_QWERTY = 0x10000000;

	/**
	 * A kind of soft keyboard layout. An input mode should be anded with
	 * {@link #MASK_SKB_LAYOUT} to get its soft keyboard layout. ָ�����������һ
	 */
	private static final int MASK_SKB_LAYOUT_SYMBOL1 = 0x20000000;

	/**
	 * A kind of soft keyboard layout. An input mode should be anded with
	 * {@link #MASK_SKB_LAYOUT} to get its soft keyboard layout. ָ����������̶�
	 */
	private static final int MASK_SKB_LAYOUT_SYMBOL2 = 0x30000000;

	/**
	 * A kind of soft keyboard layout. An input mode should be anded with
	 * {@link #MASK_SKB_LAYOUT} to get its soft keyboard layout. ָ��΢Ц�����
	 */
	private static final int MASK_SKB_LAYOUT_SMILEY = 0x40000000;

	/**
	 * A kind of soft keyboard layout. An input mode should be anded with
	 * {@link #MASK_SKB_LAYOUT} to get its soft keyboard layout. ָ���绰�����
	 */
	private static final int MASK_SKB_LAYOUT_PHONE = 0x50000000;

	/**
	 * Used to indicate which language the current input mode is in. If the
	 * current input mode works with a none-QWERTY soft keyboard, these bits are
	 * also used to get language information. For example, a Chinese symbol soft
	 * keyboard and an English one are different in an icon which is used to
	 * tell user the language information. BTW, the smiley soft keyboard mode
	 * should be set with {@link #MASK_LANGUAGE_CN} because it can only be
	 * launched from Chinese QWERTY soft keyboard, and it has Chinese icon on
	 * soft keyboard. ��7λָ�����ԡ�
	 */
	private static final int MASK_LANGUAGE = 0x0f000000;

	/**
	 * Used to indicate the current language. An input mode should be anded with
	 * {@link #MASK_LANGUAGE} to get this information. ָ���������ԡ�
	 */
	private static final int MASK_LANGUAGE_CN = 0x01000000;

	/**
	 * Used to indicate the current language. An input mode should be anded with
	 * {@link #MASK_LANGUAGE} to get this information. ָ��Ӣ�����ԡ�
	 */
	private static final int MASK_LANGUAGE_EN = 0x02000000;

	/**
	 * Used to indicate which case the current input mode is in. For example,
	 * English QWERTY has lowercase and uppercase. For the Chinese QWERTY, these
	 * bits are ignored. For phone keyboard layout, these bits can be
	 * {@link #MASK_CASE_UPPER} to request symbol page for phone soft keyboard.
	 * ��6λָ������̵�ǰ��״̬������ߣ���д�����ͣ�Сд����
	 */
	private static final int MASK_CASE = 0x00f00000;

	/**
	 * Used to indicate the current case information. An input mode should be
	 * anded with {@link #MASK_CASE} to get this information. ָ�������״̬Ϊ�ͣ�Сд����
	 */
	private static final int MASK_CASE_LOWER = 0x00100000;

	/**
	 * Used to indicate the current case information. An input mode should be
	 * anded with {@link #MASK_CASE} to get this information. ָ�������״̬Ϊ�ߣ���д����
	 */
	private static final int MASK_CASE_UPPER = 0x00200000;

	/**
	 * Mode for inputing Chinese with soft keyboard. ���ı�׼�����ģʽ
	 */
	public static final int MODE_SKB_CHINESE = (MASK_SKB_LAYOUT_QWERTY | MASK_LANGUAGE_CN);

	/**
	 * Mode for inputing basic symbols for Chinese mode with soft keyboard.
	 * ���ķ��������һģʽ
	 */
	public static final int MODE_SKB_SYMBOL1_CN = (MASK_SKB_LAYOUT_SYMBOL1 | MASK_LANGUAGE_CN);

	/**
	 * Mode for inputing more symbols for Chinese mode with soft keyboard.
	 * ���ķ�������̶�ģʽ
	 */
	public static final int MODE_SKB_SYMBOL2_CN = (MASK_SKB_LAYOUT_SYMBOL2 | MASK_LANGUAGE_CN);

	/**
	 * Mode for inputing English lower characters with soft keyboard. Ӣ��Сд�����ģʽ
	 */
	public static final int MODE_SKB_ENGLISH_LOWER = (MASK_SKB_LAYOUT_QWERTY
			| MASK_LANGUAGE_EN | MASK_CASE_LOWER);

	/**
	 * Mode for inputing English upper characters with soft keyboard. Ӣ�Ĵ�д�����ģʽ
	 */
	public static final int MODE_SKB_ENGLISH_UPPER = (MASK_SKB_LAYOUT_QWERTY
			| MASK_LANGUAGE_EN | MASK_CASE_UPPER);

	/**
	 * Mode for inputing basic symbols for English mode with soft keyboard.
	 * Ӣ�ķ��������һģʽ
	 */
	public static final int MODE_SKB_SYMBOL1_EN = (MASK_SKB_LAYOUT_SYMBOL1 | MASK_LANGUAGE_EN);

	/**
	 * Mode for inputing more symbols for English mode with soft keyboard.
	 * Ӣ�ķ�������̶�ģʽ
	 */
	public static final int MODE_SKB_SYMBOL2_EN = (MASK_SKB_LAYOUT_SYMBOL2 | MASK_LANGUAGE_EN);

	/**
	 * Mode for inputing smileys with soft keyboard. ����Ц�������ģʽ
	 */
	public static final int MODE_SKB_SMILEY = (MASK_SKB_LAYOUT_SMILEY | MASK_LANGUAGE_CN);

	/**
	 * Mode for inputing phone numbers. �绰���������ģʽ
	 */
	public static final int MODE_SKB_PHONE_NUM = (MASK_SKB_LAYOUT_PHONE);

	/**
	 * Mode for inputing phone numbers. �绰�����д�����ģʽ
	 */
	public static final int MODE_SKB_PHONE_SYM = (MASK_SKB_LAYOUT_PHONE | MASK_CASE_UPPER);

	/**
	 * Mode for inputing Chinese with a hardware keyboard. ����Ӳ����ģʽ����������Ҫ����̣�
	 */
	public static final int MODE_HKB_CHINESE = (MASK_LANGUAGE_CN);

	/**
	 * Mode for inputing English with a hardware keyboard Ӣ��Ӳ����ģʽ����������Ҫ����̣�
	 */
	public static final int MODE_HKB_ENGLISH = (MASK_LANGUAGE_EN);

	/**
	 * Unset mode. δ�������뷨ģʽ��
	 */
	public static final int MODE_UNSET = 0;

	/**
	 * Maximum toggle states for a soft keyboard.
	 * һ������̵��л�״̬�����������������л�״̬��ָҪ��ʾ�İ�����״̬
	 * ��������Щ״̬������������ͬʱ������һ���������С����������ͬʱ������һ���������У��Ǿ� ��֪��Ҫ��ʾ��һ���ˡ�
	 */
	public static final int MAX_TOGGLE_STATES = 4;

	/**
	 * The input mode for the current edit box. ��ǰ���뷨��ģʽ
	 */
	private int mInputMode = MODE_UNSET;

	/**
	 * Used to remember previous input mode. When user enters an edit field, the
	 * previous input mode will be tried. If the previous mode can not be used
	 * for the current situation (For example, previous mode is a soft keyboard
	 * mode to input symbols, and we have a hardware keyboard for the current
	 * situation), {@link #mRecentLauageInputMode} will be tried. ǰһ�����뷨��ģʽ
	 **/
	private int mPreviousInputMode = MODE_SKB_CHINESE;

	/**
	 * Used to remember recent mode to input language. ������������뷨ģʽ
	 */
	private int mRecentLauageInputMode = MODE_SKB_CHINESE;

	/**
	 * Editor information of the current edit box. ��ǰ�༭��� EditorInfo ��
	 */
	private EditorInfo mEditorInfo;

	/**
	 * Used to indicate required toggling operations.
	 * ���Ƶ�ǰ���뷨ģʽ����̲���Ҫ��ʾ�İ����л�״̬��Ҫ��ʾ����ID�����統ǰ����̲�����
	 * ����һ��������Ĭ��״̬���������л�״̬��ToggleStates�е�mKeyStates[]����ľ��ǵ�ǰҪ��ʾ���л�״̬
	 * ����������������л�״̬��û����mKeyStates[]��
	 * ����ô��������ʾĬ��״̬����������л�״̬����һ����mKeyStates[]�У�����ʾ�Ǹ��л�״̬
	 * ��ע�⣺���Բ�������һ���������������������ϵ��л�״̬ͬʱ��mKeyStates
	 * []�С�ToggleStates�������ư�������ʾ״̬��������ֱ�ӿ���һ�а�������ʾ
	 * ��mRowIdToEnable����ľ��ǿ���ʾ���е�ID��ÿ�е�ID����Ψһ�� ��һ��IDͬʱ��ֵ�����У���ֻ��IDΪ
	 * mRowIdToEnable �� ALWAYS_SHOW_ROW_ID ���вŻᱻ��ʾ������
	 */
	private ToggleStates mToggleStates = new ToggleStates();

	/**
	 * The current field is a short message field? ��ǰ���ֶ��Ƿ���һ�������ֶΣ� ��
	 * editorInfo.inputType & EditorInfo.TYPE_MASK_VARIATION ==
	 * EditorInfo.TYPE_MASK_VARIATION ʱ��������Ϊtrue������Ϊfalse��
	 */
	private boolean mShortMessageField;

	/**
	 * Is return key in normal state? �Ƿ�������״̬�µ� Enter ����
	 */
	private boolean mEnterKeyNormal = true;

	/**
	 * Current icon. 0 for none icon. ��ǰ���뷨��ͼ�ꡣ��ʾ���ź�����ͼ�ꡣ
	 */
	int mInputIcon = R.drawable.ime_pinyin;

	/**
	 * IME service. ���뷨����
	 */
	private PinyinIME mImeService;

	/**
	 * Key toggling state for Chinese mode. ������еİ������л�״̬��ID������״̬��
	 */
	private int mToggleStateCn;

	/**
	 * Key toggling state for Chinese mode with candidates.
	 * ������еİ������л�״̬��ID�����ĺ�ѡ��״̬��
	 */
	private int mToggleStateCnCand;

	/**
	 * Key toggling state for English lowwercase mode. ������еİ������л�״̬��ID��Ӣ��Сд״̬��
	 */
	private int mToggleStateEnLower;

	/**
	 * Key toggling state for English upppercase mode. ������еİ������л�״̬��ID��Ӣ�Ĵ�д״̬��
	 */
	private int mToggleStateEnUpper;

	/**
	 * Key toggling state for English symbol mode for the first page.
	 * ������еİ������л�״̬��ID��Ӣ�ķ���һ״̬��
	 */
	private int mToggleStateEnSym1;

	/**
	 * Key toggling state for English symbol mode for the second page.
	 * ������еİ������л�״̬��ID��Ӣ�ķ��Ŷ�״̬��
	 */
	private int mToggleStateEnSym2;

	/**
	 * Key toggling state for smiley mode. ������еİ������л�״̬��ID��Ц��״̬��
	 */
	private int mToggleStateSmiley;

	/**
	 * Key toggling state for phone symbol mode. ������еİ������л�״̬��ID���绰����״̬��
	 */
	private int mToggleStatePhoneSym;

	/**
	 * Key toggling state for GO action of ENTER key.
	 * ������еİ������л�״̬��ID��Enter����Ϊgo����״̬��
	 */
	private int mToggleStateGo;

	/**
	 * Key toggling state for SEARCH action of ENTER key.
	 * ������еİ������л�״̬��ID��Enter����Ϊ��������״̬��
	 */
	private int mToggleStateSearch;

	/**
	 * Key toggling state for SEND action of ENTER key.
	 * ������еİ������л�״̬��ID��Enter����Ϊ���Ͳ���״̬��
	 */
	private int mToggleStateSend;

	/**
	 * Key toggling state for NEXT action of ENTER key.
	 * ������еİ������л�״̬��ID��Enter����Ϊ��һ������״̬��
	 */
	private int mToggleStateNext;

	/**
	 * Key toggling state for SEND action of ENTER key.
	 * ������еİ������л�״̬��ID��Enter����Ϊ��ɲ���״̬��
	 */
	private int mToggleStateDone;

	/**
	 * QWERTY row toggling state for Chinese input. ������е��е�ID������״̬��
	 */
	private int mToggleRowCn;

	/**
	 * QWERTY row toggling state for English input. ������е��е�ID��Ӣ��״̬��
	 */
	private int mToggleRowEn;

	/**
	 * QWERTY row toggling state for URI input. ������е��е�ID��Uri����״̬��
	 */
	private int mToggleRowUri;

	/**
	 * QWERTY row toggling state for email address input. ������е��е�ID���ʼ���ַ����״̬��
	 */
	private int mToggleRowEmailAddress;

	/**
	 * ���Ƶ�ǰ���뷨ģʽ����̲���Ҫ��ʾ�İ����л�״̬��Ҫ��ʾ����ID�Ĺ����ࡣ���統ǰ����̲�����
	 * ����һ��������Ĭ��״̬���������л�״̬��ToggleStates�е�mKeyStates[]����ľ��ǵ�ǰҪ��ʾ���л�״̬
	 * ����������������л�״̬��û����mKeyStates[]��
	 * ����ô��������ʾĬ��״̬����������л�״̬����һ����mKeyStates[]�У�����ʾ�Ǹ��л�״̬
	 * ��ע�⣺���Բ�������һ���������������������ϵ��л�״̬ͬʱ��mKeyStates
	 * []�С�ToggleStates�������ư�������ʾ״̬��������ֱ�ӿ���һ�а�������ʾ
	 * ��mRowIdToEnable����ľ��ǿ���ʾ���е�ID��ÿ�е�ID����Ψһ�� ��һ��IDͬʱ��ֵ�����У���ֻ��IDΪ
	 * mRowIdToEnable �� ALWAYS_SHOW_ROW_ID ���вŻᱻ��ʾ������
	 * 
	 * @ClassName ToggleStates
	 * @author LiChao
	 */
	class ToggleStates {
		/**
		 * If it is true, this soft keyboard is a QWERTY one. �Ƿ��Ǳ�׼����
		 */
		boolean mQwerty;

		/**
		 * If {@link #mQwerty} is true, this variable is used to decide the
		 * letter case of the QWERTY keyboard. �Ƿ��Ǳ�׼���̴�дģʽ
		 */
		boolean mQwertyUpperCase;

		/**
		 * The id of enabled row in the soft keyboard. Refer to
		 * {@link com.android.inputmethod.pinyin.SoftKeyboard.KeyRow} for
		 * details. �������Ҫ��ʾ���е�ID��ֻ��IDΪ mRowIdToEnable �� ALWAYS_SHOW_ROW_ID
		 * ���вŻᱻ��ʾ������
		 */
		public int mRowIdToEnable;

		/**
		 * Used to store all other toggle states for the current input mode.
		 * ���ﱣ�ִ˿�Ҫ��ʾ�İ�����״̬��������Щ״̬������������ͬʱ������һ���������С����������ͬʱ������һ���������У��Ǿ�
		 * ��֪��Ҫ��ʾ��һ���ˡ�
		 */
		public int mKeyStates[] = new int[MAX_TOGGLE_STATES];

		/**
		 * Number of states to toggle. �����л�״̬��������mKeyStates[]�����õĳ��ȡ�
		 */
		public int mKeyStatesNum;
	}

	public InputModeSwitcher(PinyinIME imeService) {
		mImeService = imeService;
		Resources r = mImeService.getResources();

		// ��ʼ�����������л�״̬��ID �� �е�ID
		mToggleStateCn = Integer.parseInt(r.getString(R.string.toggle_cn));
		mToggleStateCnCand = Integer.parseInt(r
				.getString(R.string.toggle_cn_cand));
		mToggleStateEnLower = Integer.parseInt(r
				.getString(R.string.toggle_en_lower));
		mToggleStateEnUpper = Integer.parseInt(r
				.getString(R.string.toggle_en_upper));
		mToggleStateEnSym1 = Integer.parseInt(r
				.getString(R.string.toggle_en_sym1));
		mToggleStateEnSym2 = Integer.parseInt(r
				.getString(R.string.toggle_en_sym2));
		mToggleStateSmiley = Integer.parseInt(r
				.getString(R.string.toggle_smiley));
		mToggleStatePhoneSym = Integer.parseInt(r
				.getString(R.string.toggle_phone_sym));

		mToggleStateGo = Integer
				.parseInt(r.getString(R.string.toggle_enter_go));
		mToggleStateSearch = Integer.parseInt(r
				.getString(R.string.toggle_enter_search));
		mToggleStateSend = Integer.parseInt(r
				.getString(R.string.toggle_enter_send));
		mToggleStateNext = Integer.parseInt(r
				.getString(R.string.toggle_enter_next));
		mToggleStateDone = Integer.parseInt(r
				.getString(R.string.toggle_enter_done));

		mToggleRowCn = Integer.parseInt(r.getString(R.string.toggle_row_cn));
		mToggleRowEn = Integer.parseInt(r.getString(R.string.toggle_row_en));
		mToggleRowUri = Integer.parseInt(r.getString(R.string.toggle_row_uri));
		mToggleRowEmailAddress = Integer.parseInt(r
				.getString(R.string.toggle_row_emailaddress));
	}

	/**
	 * ��ȡ��ǰ�����뷨ģʽ
	 * 
	 * @��Чֵ 
	 *      MODE_UNSET��δ�������뷨ģʽ����MODE_SKB_CHINESE�����ı�׼�����ģʽ����
	 *      MODE_SKB_SYMBOL1_CN�����ķ��������һģʽ����MODE_SKB_SYMBOL2_CN�����ķ�������̶�ģʽ����
	 *      MODE_SKB_ENGLISH_LOWER��Ӣ��Сд�����ģʽ����MODE_SKB_ENGLISH_UPPER��Ӣ�Ĵ�д�����ģʽ����
	 *      MODE_SKB_SYMBOL1_EN��Ӣ�ķ��������һģʽ����MODE_SKB_SYMBOL2_EN��Ӣ�ķ�������̶�ģʽ����
	 *      MODE_SKB_SMILEY������Ц�������ģʽ����MODE_SKB_PHONE_NUM���绰���������ģʽ����
	 *      MODE_SKB_PHONE_SYM���绰�����д�����ģʽ����MODE_HKB_CHINESE������Ӳ����ģʽ����
	 *      MODE_HKB_ENGLISH��Ӣ��Ӳ����ģʽ����
	 * @return
	 */
	public int getInputMode() {
		return mInputMode;
	}

	/**
	 * ��ȡ���Ƶ�ǰ���뷨ģʽ����̲��ֵİ����л�״̬�Ϳ���ʾ��ID�Ķ���
	 * 
	 * @return
	 */
	public ToggleStates getToggleStates() {
		return mToggleStates;
	}

	/**
	 * ��������� LAYOUT ��ȡ����̲����ļ���ԴID
	 * 
	 * @return
	 */
	public int getSkbLayout() {
		int layout = (mInputMode & MASK_SKB_LAYOUT);

		switch (layout) {
		case MASK_SKB_LAYOUT_QWERTY:
			return R.xml.skb_qwerty;
		case MASK_SKB_LAYOUT_SYMBOL1:
			return R.xml.skb_sym1;
		case MASK_SKB_LAYOUT_SYMBOL2:
			return R.xml.skb_sym2;
		case MASK_SKB_LAYOUT_SMILEY:
			return R.xml.skb_smiley;
		case MASK_SKB_LAYOUT_PHONE:
			return R.xml.skb_phone;
		}
		return 0;
	}

	/**
	 * �л�Ӳ���̵�����ģʽ�������л��������ģʽ��ͼ�ꡣ
	 * �������µ����뷨����������ģʽ�����жϵ�ǰ�����뷨����ģʽ�Ƿ�������ģʽ���ǵĻ����͸ĳ�Ӣ��ģʽ��
	 * 
	 * @return the icon to update
	 */
	public int switchLanguageWithHkb() {
		int newInputMode = MODE_HKB_CHINESE;
		mInputIcon = R.drawable.ime_pinyin;

		if (MODE_HKB_CHINESE == mInputMode) {
			newInputMode = MODE_HKB_ENGLISH;
			mInputIcon = R.drawable.ime_en;
		}

		saveInputMode(newInputMode);
		return mInputIcon;
	}

	/**
	 * ͨ�����Ƕ��������̵İ������л����뷨ģʽ��
	 * 
	 * @param userKey
	 * @return the icon to update.
	 */
	public int switchModeForUserKey(int userKey) {
		int newInputMode = MODE_UNSET;

		if (USERDEF_KEYCODE_LANG_2 == userKey) {
			// ���Լ�����ʾ���Ļ���Ӣ�ġ��з���Ӣ���ļ�
			if (MODE_SKB_CHINESE == mInputMode) {
				newInputMode = MODE_SKB_ENGLISH_LOWER;
			} else if (MODE_SKB_ENGLISH_LOWER == mInputMode
					|| MODE_SKB_ENGLISH_UPPER == mInputMode) {
				newInputMode = MODE_SKB_CHINESE;
			} else if (MODE_SKB_SYMBOL1_CN == mInputMode) {
				newInputMode = MODE_SKB_SYMBOL1_EN;
			} else if (MODE_SKB_SYMBOL1_EN == mInputMode) {
				newInputMode = MODE_SKB_SYMBOL1_CN;
			} else if (MODE_SKB_SYMBOL2_CN == mInputMode) {
				newInputMode = MODE_SKB_SYMBOL2_EN;
			} else if (MODE_SKB_SYMBOL2_EN == mInputMode) {
				newInputMode = MODE_SKB_SYMBOL2_CN;
			} else if (MODE_SKB_SMILEY == mInputMode) {
				newInputMode = MODE_SKB_CHINESE;
			}
		} else if (USERDEF_KEYCODE_SYM_3 == userKey) {
			// ϵͳ������ʾ��?123���ļ�
			if (MODE_SKB_CHINESE == mInputMode) {
				newInputMode = MODE_SKB_SYMBOL1_CN;
			} else if (MODE_SKB_ENGLISH_UPPER == mInputMode
					|| MODE_SKB_ENGLISH_LOWER == mInputMode) {
				newInputMode = MODE_SKB_SYMBOL1_EN;
			} else if (MODE_SKB_SYMBOL1_EN == mInputMode
					|| MODE_SKB_SYMBOL2_EN == mInputMode) {
				newInputMode = MODE_SKB_ENGLISH_LOWER;
			} else if (MODE_SKB_SYMBOL1_CN == mInputMode
					|| MODE_SKB_SYMBOL2_CN == mInputMode) {
				newInputMode = MODE_SKB_CHINESE;
			} else if (MODE_SKB_SMILEY == mInputMode) {
				newInputMode = MODE_SKB_SYMBOL1_CN;
			}
		} else if (USERDEF_KEYCODE_SHIFT_1 == userKey) {
			// shift������ʾ������ ���� ��Сдͼ��İ�����
			if (MODE_SKB_ENGLISH_LOWER == mInputMode) {
				newInputMode = MODE_SKB_ENGLISH_UPPER;
			} else if (MODE_SKB_ENGLISH_UPPER == mInputMode) {
				newInputMode = MODE_SKB_ENGLISH_LOWER;
			}
		} else if (USERDEF_KEYCODE_MORE_SYM_5 == userKey) {
			// ����ϵͳ������ʾ��ALT���İ���
			int sym = (MASK_SKB_LAYOUT & mInputMode);
			if (MASK_SKB_LAYOUT_SYMBOL1 == sym) {
				sym = MASK_SKB_LAYOUT_SYMBOL2;
			} else {
				sym = MASK_SKB_LAYOUT_SYMBOL1;
			}
			newInputMode = ((mInputMode & (~MASK_SKB_LAYOUT)) | sym);
		} else if (USERDEF_KEYCODE_SMILEY_6 == userKey) {
			// Ц��������ʾ������Ц��ͼ��İ���
//			if (MODE_SKB_CHINESE == mInputMode) {
//				newInputMode = MODE_SKB_SMILEY;
//			} else {
//				newInputMode = MODE_SKB_CHINESE;
//			}
			Log.i("lichao", "iData����");
		} else if (USERDEF_KEYCODE_PHONE_SYM_4 == userKey) {
			// �绰������ʾ��*#{�����ߡ�123���İ���
			if (MODE_SKB_PHONE_NUM == mInputMode) {
				newInputMode = MODE_SKB_PHONE_SYM;
			} else {
				newInputMode = MODE_SKB_PHONE_NUM;
			}
		}

		if (newInputMode == mInputMode || MODE_UNSET == newInputMode) {
			return mInputIcon;
		}

		// �����µ����뷨ģʽ
		saveInputMode(newInputMode);
		// ׼���л����뷨״̬
		prepareToggleStates(true);
		return mInputIcon;
	}

	/**
	 * ���ݱ༭��� EditorInfo ��Ϣ��ȡӲ���̵����뷨ģʽ��
	 * 
	 * @param editorInfo
	 * @return the icon to update.
	 */
	public int requestInputWithHkb(EditorInfo editorInfo) {
		mShortMessageField = false;
		boolean english = false;
		int newInputMode = MODE_HKB_CHINESE;

		switch (editorInfo.inputType & EditorInfo.TYPE_MASK_CLASS) {
		case EditorInfo.TYPE_CLASS_NUMBER:
		case EditorInfo.TYPE_CLASS_PHONE:
		case EditorInfo.TYPE_CLASS_DATETIME:
			english = true;
			break;
		case EditorInfo.TYPE_CLASS_TEXT:
			int v = editorInfo.inputType & EditorInfo.TYPE_MASK_VARIATION;
			if (v == EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
					|| v == EditorInfo.TYPE_TEXT_VARIATION_PASSWORD
					|| v == EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
					|| v == EditorInfo.TYPE_TEXT_VARIATION_URI) {
				english = true;
			} else if (v == EditorInfo.TYPE_TEXT_VARIATION_SHORT_MESSAGE) {
				mShortMessageField = true;
			}
			break;
		default:
		}

		if (english) {
			// If the application request English mode, we switch to it.
			newInputMode = MODE_HKB_ENGLISH;
		} else {
			// If the application do not request English mode, we will
			// try to keep the previous mode to input language text.
			// Because there is not soft keyboard, we need discard all
			// soft keyboard related information from the previous language
			// mode.
			// ������һ�ε����뷨ģʽ�����ģ���ô�͸�Ϊ���ġ�
			if ((mRecentLauageInputMode & MASK_LANGUAGE) == MASK_LANGUAGE_CN) {
				newInputMode = MODE_HKB_CHINESE;
			} else {
				newInputMode = MODE_HKB_ENGLISH;
			}
		}
		mEditorInfo = editorInfo;
		saveInputMode(newInputMode);
		prepareToggleStates(false);
		return mInputIcon;
	}

	/**
	 * ���ݱ༭��� EditorInfo ��Ϣ��ȡ����̵����뷨ģʽ��
	 * 
	 * @param editorInfo
	 * @return the icon to update.
	 */
	public int requestInputWithSkb(EditorInfo editorInfo) {
		mShortMessageField = false;

		int newInputMode = MODE_SKB_CHINESE;

		switch (editorInfo.inputType & EditorInfo.TYPE_MASK_CLASS) {
		case EditorInfo.TYPE_CLASS_NUMBER:
		case EditorInfo.TYPE_CLASS_DATETIME:
			newInputMode = MODE_SKB_SYMBOL1_EN;
			break;
		case EditorInfo.TYPE_CLASS_PHONE:
			newInputMode = MODE_SKB_PHONE_NUM;
			break;
		case EditorInfo.TYPE_CLASS_TEXT:
			int v = editorInfo.inputType & EditorInfo.TYPE_MASK_VARIATION;
			if (v == EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
					|| v == EditorInfo.TYPE_TEXT_VARIATION_PASSWORD
					|| v == EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
					|| v == EditorInfo.TYPE_TEXT_VARIATION_URI) {
				// If the application request English mode, we switch to it.
				newInputMode = MODE_SKB_ENGLISH_LOWER;
			} else {
				if (v == EditorInfo.TYPE_TEXT_VARIATION_SHORT_MESSAGE) {
					mShortMessageField = true;
				}
				// If the application do not request English mode, we will
				// try to keep the previous mode.
				int skbLayout = (mInputMode & MASK_SKB_LAYOUT);
				newInputMode = mInputMode;
				if (0 == skbLayout) {
					if ((mInputMode & MASK_LANGUAGE) == MASK_LANGUAGE_CN) {
						newInputMode = MODE_SKB_CHINESE;
					} else {
						newInputMode = MODE_SKB_ENGLISH_LOWER;
					}
				}
			}
			break;
		default:
			// Try to keep the previous mode.
			int skbLayout = (mInputMode & MASK_SKB_LAYOUT);
			newInputMode = mInputMode;
			if (0 == skbLayout) {
				if ((mInputMode & MASK_LANGUAGE) == MASK_LANGUAGE_CN) {
					newInputMode = MODE_SKB_CHINESE;
				} else {
					newInputMode = MODE_SKB_ENGLISH_LOWER;
				}
			}
			break;
		}

		mEditorInfo = editorInfo;
		saveInputMode(newInputMode);
		prepareToggleStates(true);
		return mInputIcon;
	}

	/**
	 * ���󷵻���һ�����뷨ģʽ
	 * 
	 * @return
	 */
	public int requestBackToPreviousSkb() {
		int layout = (mInputMode & MASK_SKB_LAYOUT);
		int lastLayout = (mPreviousInputMode & MASK_SKB_LAYOUT);
		if (0 != layout && 0 != lastLayout) {
			// TODO �������´���� mInputMode �� mPreviousInputMode ��һ���ˣ���������
			mInputMode = mPreviousInputMode;
			saveInputMode(mInputMode);
			prepareToggleStates(true);
			return mInputIcon;
		}
		return 0;
	}

	/**
	 * ������ĺ�ѡ��ģʽ״̬
	 * 
	 * @return
	 */
	public int getTooggleStateForCnCand() {
		return mToggleStateCnCand;
	}

	/**
	 * �Ƿ���Ӳ�������뷨ģʽ
	 * 
	 * @return
	 */
	public boolean isEnglishWithHkb() {
		return MODE_HKB_ENGLISH == mInputMode;
	}

	/**
	 * �Ƿ��������Ӣ��ģʽ
	 * 
	 * @return
	 */
	public boolean isEnglishWithSkb() {
		return MODE_SKB_ENGLISH_LOWER == mInputMode
				|| MODE_SKB_ENGLISH_UPPER == mInputMode;
	}

	/**
	 * �Ƿ�������̸ߣ���д��ģʽ
	 * 
	 * @return
	 */
	public boolean isEnglishUpperCaseWithSkb() {
		return MODE_SKB_ENGLISH_UPPER == mInputMode;
	}

	/**
	 * �Ƿ����������ԣ���ͳ��׼����̻���Ӳ���̣���
	 * 
	 * @return
	 */
	public boolean isChineseText() {
		int skbLayout = (mInputMode & MASK_SKB_LAYOUT);
		if (MASK_SKB_LAYOUT_QWERTY == skbLayout || 0 == skbLayout) {
			int language = (mInputMode & MASK_LANGUAGE);
			if (MASK_LANGUAGE_CN == language)
				return true;
		}
		return false;
	}

	/**
	 * �Ƿ���Ӳ���̵���������
	 * 
	 * @return
	 */
	public boolean isChineseTextWithHkb() {
		int skbLayout = (mInputMode & MASK_SKB_LAYOUT);
		if (0 == skbLayout) {
			int language = (mInputMode & MASK_LANGUAGE);
			if (MASK_LANGUAGE_CN == language)
				return true;
		}
		return false;
	}

	/**
	 * �Ƿ�������̵���������
	 * 
	 * @return
	 */
	public boolean isChineseTextWithSkb() {
		int skbLayout = (mInputMode & MASK_SKB_LAYOUT);
		if (MASK_SKB_LAYOUT_QWERTY == skbLayout) {
			int language = (mInputMode & MASK_LANGUAGE);
			if (MASK_LANGUAGE_CN == language)
				return true;
		}
		return false;
	}

	/**
	 * �Ƿ�������̵ķ���
	 * 
	 * @return
	 */
	public boolean isSymbolWithSkb() {
		int skbLayout = (mInputMode & MASK_SKB_LAYOUT);
		if (MASK_SKB_LAYOUT_SYMBOL1 == skbLayout
				|| MASK_SKB_LAYOUT_SYMBOL2 == skbLayout) {
			return true;
		}
		return false;
	}

	/**
	 * �Ƿ�������Enter��״̬
	 * 
	 * @return
	 */
	public boolean isEnterNoramlState() {
		return mEnterKeyNormal;
	}

	/**
	 * @param ��������
	 * @return
	 */
	public boolean tryHandleLongPressSwitch(int keyCode) {
		if (USERDEF_KEYCODE_LANG_2 == keyCode
				|| USERDEF_KEYCODE_PHONE_SYM_4 == keyCode) {
			mImeService.showOptionsMenu();
			return true;
		}
		return false;
	}

	/**
	 * �����µ����뷨ģʽ
	 * 
	 * @param newInputMode
	 */
	private void saveInputMode(int newInputMode) {
		// ���浱ǰ���뷨ģʽ
		mPreviousInputMode = mInputMode;
		// �����µ����뷨ģʽΪ��ǰ�����뷨ģʽ
		mInputMode = newInputMode;

		// ���뷨ģʽ�Ĳ������ԣ���8λ��
		int skbLayout = (mInputMode & MASK_SKB_LAYOUT);
		if (MASK_SKB_LAYOUT_QWERTY == skbLayout || 0 == skbLayout) {
			// ����������������뷨ģʽ
			mRecentLauageInputMode = mInputMode;
		}

		// �������뷨ͼ��
		mInputIcon = R.drawable.ime_pinyin;
		if (isEnglishWithHkb()) {
			mInputIcon = R.drawable.ime_en;
		} else if (isChineseTextWithHkb()) {
			mInputIcon = R.drawable.ime_pinyin;
		}

		// �����Ӳ���̣����������뷨ģʽΪδ����״̬��
		if (!Environment.getInstance().hasHardKeyboard()) {
			mInputIcon = 0;
		}
	}

	/**
	 * ׼�����ÿ�����ʾ�İ����л�״̬�Ϳ���ʾ��ID�Ķ�������ݣ���װmToggleStates�����ݡ�
	 * 
	 * @param needSkb
	 */
	private void prepareToggleStates(boolean needSkb) {
		mEnterKeyNormal = true;
		if (!needSkb)
			return;

		mToggleStates.mQwerty = false;
		mToggleStates.mKeyStatesNum = 0;

		int states[] = mToggleStates.mKeyStates;
		int statesNum = 0;
		// Toggle state for language.
		int language = (mInputMode & MASK_LANGUAGE);
		int layout = (mInputMode & MASK_SKB_LAYOUT);
		int charcase = (mInputMode & MASK_CASE);
		int variation = mEditorInfo.inputType & EditorInfo.TYPE_MASK_VARIATION;

		// �������뷨ģʽ���Ҫ��ʾ�İ������л�״̬
		if (MASK_SKB_LAYOUT_PHONE != layout) {
			if (MASK_LANGUAGE_CN == language) {
				// Chinese and Chinese symbol are always the default states,
				// do not add a toggling operation.
				if (MASK_SKB_LAYOUT_QWERTY == layout) {
					mToggleStates.mQwerty = true;
					mToggleStates.mQwertyUpperCase = true;
					if (mShortMessageField) {
						states[statesNum] = mToggleStateSmiley;
						statesNum++;
					}
				}
			} else if (MASK_LANGUAGE_EN == language) {
				if (MASK_SKB_LAYOUT_QWERTY == layout) {
					mToggleStates.mQwerty = true;
					mToggleStates.mQwertyUpperCase = false;
					states[statesNum] = mToggleStateEnLower;
					if (MASK_CASE_UPPER == charcase) {
						mToggleStates.mQwertyUpperCase = true;
						states[statesNum] = mToggleStateEnUpper;
					}
					statesNum++;
				} else if (MASK_SKB_LAYOUT_SYMBOL1 == layout) {
					states[statesNum] = mToggleStateEnSym1;
					statesNum++;
				} else if (MASK_SKB_LAYOUT_SYMBOL2 == layout) {
					states[statesNum] = mToggleStateEnSym2;
					statesNum++;
				}
			}

			// Toggle rows for QWERTY.
			mToggleStates.mRowIdToEnable = KeyRow.DEFAULT_ROW_ID;
			if (variation == EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS) {
				mToggleStates.mRowIdToEnable = mToggleRowEmailAddress;
			} else if (variation == EditorInfo.TYPE_TEXT_VARIATION_URI) {
				mToggleStates.mRowIdToEnable = mToggleRowUri;
			} else if (MASK_LANGUAGE_CN == language) {
				mToggleStates.mRowIdToEnable = mToggleRowCn;
			} else if (MASK_LANGUAGE_EN == language) {
				mToggleStates.mRowIdToEnable = mToggleRowEn;
			}
		} else {
			if (MASK_CASE_UPPER == charcase) {
				states[statesNum] = mToggleStatePhoneSym;
				statesNum++;
			}
		}

		// Toggle state for enter key.
		// ����EditorInfo.imeOptions��� Ҫ��ʾ�İ������л�״̬ ������ֻ��� Enter �����л�״̬��
		int action = mEditorInfo.imeOptions
				& (EditorInfo.IME_MASK_ACTION | EditorInfo.IME_FLAG_NO_ENTER_ACTION);

		if (action == EditorInfo.IME_ACTION_GO) {
			states[statesNum] = mToggleStateGo;
			statesNum++;
			mEnterKeyNormal = false;
		} else if (action == EditorInfo.IME_ACTION_SEARCH) {
			states[statesNum] = mToggleStateSearch;
			statesNum++;
			mEnterKeyNormal = false;
		} else if (action == EditorInfo.IME_ACTION_SEND) {
			states[statesNum] = mToggleStateSend;
			statesNum++;
			mEnterKeyNormal = false;
		} else if (action == EditorInfo.IME_ACTION_NEXT) {
			int f = mEditorInfo.inputType & EditorInfo.TYPE_MASK_FLAGS;
			if (f != EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE) {
				states[statesNum] = mToggleStateNext;
				statesNum++;
				mEnterKeyNormal = false;
			}
		} else if (action == EditorInfo.IME_ACTION_DONE) {
			states[statesNum] = mToggleStateDone;
			statesNum++;
			mEnterKeyNormal = false;
		}
		mToggleStates.mKeyStatesNum = statesNum;
	}
}
