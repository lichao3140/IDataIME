package com.idata.bluetoothime;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.ViewFlipper;
import com.idata.bluetoothime.PinyinIME.DecodingInfo;

/**
 * ��װ���еļ�ͷ���¼�����
 * 
 * @ClassName ArrowUpdater
 * @author LiChao
 */
interface ArrowUpdater {
	void updateArrowStatus();
}

/**
 * Container used to host the two candidate views. When user drags on candidate
 * view, animation is used to dismiss the current candidate view and show a new
 * one. These two candidate views and their parent are hosted by this container.
 * <p>
 * Besides the candidate views, there are two arrow views to show the page
 * forward/backward arrows.
 * </p>
 */
/**
 * ��ѡ�ʼ�װ��
 * 
 * @ClassName CandidatesContainer
 * @author LiChao
 */
public class CandidatesContainer extends RelativeLayout implements
		OnTouchListener, AnimationListener, ArrowUpdater {
	/**
	 * Alpha value to show an enabled arrow. ��ͷͼƬ��ʾʱ��͸����
	 */
	private static int ARROW_ALPHA_ENABLED = 0xff;

	/**
	 * Alpha value to show an disabled arrow. ��ͷͼƬ����ʾʱ��͸����
	 */
	private static int ARROW_ALPHA_DISABLED = 0x40;

	/**
	 * Animation time to show a new candidate view and dismiss the old one.
	 * ��ʾ���߹ر�һ����ѡ��View�Ķ���ʱ��
	 */
	private static int ANIMATION_TIME = 200;

	/**
	 * Listener used to notify IME that user clicks a candidate, or navigate
	 * between them. ��ѡ����ͼ������
	 */
	private CandidateViewListener mCvListener;

	/**
	 * The left arrow button used to show previous page. ��߼�ͷ��ť
	 */
	private ImageButton mLeftArrowBtn;

	/**
	 * The right arrow button used to show next page. �ұ߼�ͷ��ť
	 */
	private ImageButton mRightArrowBtn;

	/**
	 * Decoding result to show. �ʿ�������
	 */
	private DecodingInfo mDecInfo;

	/**
	 * The animation view used to show candidates. It contains two views.
	 * Normally, the candidates are shown one of them. When user navigates to
	 * another page, animation effect will be performed.
	 * ViewFlipperҳ�����������������ͼ������ֻ��ʾ����һ�������л���ѡ��ҳ��ʱ�򣬾�������һ����ͼװ�ؽ���Ҫ��ʾ�ĺ�ѡ�����������
	 */
	private ViewFlipper mFlipper;

	/**
	 * The x offset of the flipper in this container. ViewFlipper �ڼ�װ���ƫ��λ�á�
	 */
	private int xOffsetForFlipper;

	/**
	 * Animation used by the incoming view when the user navigates to a left
	 * page. ����ҳ���ƶ�����ߵĶ���
	 */
	private Animation mInAnimPushLeft;

	/**
	 * Animation used by the incoming view when the user navigates to a right
	 * page. ����ҳ���ƶ����ұߵĶ���
	 */
	private Animation mInAnimPushRight;

	/**
	 * Animation used by the incoming view when the user navigates to a page
	 * above. If the page navigation is triggered by DOWN key, this animation is
	 * used. ����ҳ���ƶ����ϵĶ���
	 */
	private Animation mInAnimPushUp;

	/**
	 * Animation used by the incoming view when the user navigates to a page
	 * below. If the page navigation is triggered by UP key, this animation is
	 * used. ����ҳ���ƶ����µĶ���
	 */
	private Animation mInAnimPushDown;

	/**
	 * Animation used by the outgoing view when the user navigates to a left
	 * page. ����ҳ���ƶ�����ߵĶ���
	 */
	private Animation mOutAnimPushLeft;

	/**
	 * Animation used by the outgoing view when the user navigates to a right
	 * page.����ҳ���ƶ����ұߵĶ���
	 */
	private Animation mOutAnimPushRight;

	/**
	 * Animation used by the outgoing view when the user navigates to a page
	 * above. If the page navigation is triggered by DOWN key, this animation is
	 * used.����ҳ���ƶ����ϱߵĶ���
	 */
	private Animation mOutAnimPushUp;

	/**
	 * Animation used by the incoming view when the user navigates to a page
	 * below. If the page navigation is triggered by UP key, this animation is
	 * used.����ҳ���ƶ����±ߵĶ���
	 */
	private Animation mOutAnimPushDown;

	/**
	 * Animation object which is used for the incoming view currently.
	 * ����ҳ�浱ǰʹ�õĶ���
	 */
	private Animation mInAnimInUse;

	/**
	 * Animation object which is used for the outgoing view currently.
	 * ����ҳ�浱ǰʹ�õĶ���
	 */
	private Animation mOutAnimInUse;

	/**
	 * Current page number in display. ��ǰ��ʾ��ҳ��
	 */
	private int mCurrentPage = -1;

	public CandidatesContainer(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void initialize(CandidateViewListener cvListener,
			BalloonHint balloonHint, GestureDetector gestureDetector) {
		mCvListener = cvListener;

		mLeftArrowBtn = (ImageButton) findViewById(R.id.arrow_left_btn);
		mRightArrowBtn = (ImageButton) findViewById(R.id.arrow_right_btn);
		mLeftArrowBtn.setOnTouchListener(this);
		mRightArrowBtn.setOnTouchListener(this);

		mFlipper = (ViewFlipper) findViewById(R.id.candidate_flipper);
		mFlipper.setMeasureAllChildren(true);

		invalidate();
		requestLayout();

		for (int i = 0; i < mFlipper.getChildCount(); i++) {
			CandidateView cv = (CandidateView) mFlipper.getChildAt(i);
			cv.initialize(this, balloonHint, gestureDetector, mCvListener);
		}
	}

	/**
	 * ��ʾ��ѡ��
	 * 
	 * @param decInfo
	 * @param enableActiveHighlight
	 */
	public void showCandidates(PinyinIME.DecodingInfo decInfo,
			boolean enableActiveHighlight) {
		if (null == decInfo)
			return;
		mDecInfo = decInfo;
		mCurrentPage = 0;

		if (decInfo.isCandidatesListEmpty()) {
			showArrow(mLeftArrowBtn, false);
			showArrow(mRightArrowBtn, false);
		} else {
			showArrow(mLeftArrowBtn, true);
			showArrow(mRightArrowBtn, true);
		}

		for (int i = 0; i < mFlipper.getChildCount(); i++) {
			CandidateView cv = (CandidateView) mFlipper.getChildAt(i);
			cv.setDecodingInfo(mDecInfo);
		}
		stopAnimation();

		CandidateView cv = (CandidateView) mFlipper.getCurrentView();
		cv.showPage(mCurrentPage, 0, enableActiveHighlight);

		updateArrowStatus();
		invalidate();
	}

	/**
	 * ��ȡ��ǰ��ҳ��
	 * 
	 * @return
	 */
	public int getCurrentPage() {
		return mCurrentPage;
	}

	/**
	 * ���ú�ѡ���Ƿ����
	 * 
	 * @param enableActiveHighlight
	 */
	public void enableActiveHighlight(boolean enableActiveHighlight) {
		CandidateView cv = (CandidateView) mFlipper.getCurrentView();
		cv.enableActiveHighlight(enableActiveHighlight);
		invalidate();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		Environment env = Environment.getInstance();
		int measuredWidth = env.getScreenWidth();
		int measuredHeight = getPaddingTop();
		measuredHeight += env.getHeightForCandidates();
		widthMeasureSpec = MeasureSpec.makeMeasureSpec(measuredWidth,
				MeasureSpec.EXACTLY);
		heightMeasureSpec = MeasureSpec.makeMeasureSpec(measuredHeight,
				MeasureSpec.EXACTLY);
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		if (null != mLeftArrowBtn) {
			// ���ú�ѡ�����ڵ� ViewFlipper �ڼ�װ���е�ƫ��λ��
			xOffsetForFlipper = mLeftArrowBtn.getMeasuredWidth();
		}
	}

	/**
	 * ����λ������һ����ѡ���ƶ������ƶ�����һҳ�����һ����ѡ�ʵ�λ�á�
	 * 
	 * @return
	 */
	public boolean activeCurseBackward() {
		if (mFlipper.isFlipping() || null == mDecInfo) {
			return false;
		}

		CandidateView cv = (CandidateView) mFlipper.getCurrentView();

		if (cv.activeCurseBackward()) {
			cv.invalidate();
			return true;
		} else {
			return pageBackward(true, true);
		}
	}

	/**
	 * ����λ������һ����ѡ���ƶ������ƶ�����һҳ�ĵ�һ����ѡ�ʵ�λ�á�
	 * 
	 * @return
	 */
	public boolean activeCurseForward() {
		if (mFlipper.isFlipping() || null == mDecInfo) {
			return false;
		}

		CandidateView cv = (CandidateView) mFlipper.getCurrentView();

		if (cv.activeCursorForward()) {
			cv.invalidate();
			return true;
		} else {
			return pageForward(true, true);
		}
	}

	/**
	 * ����һҳ��ѡ��
	 * 
	 * @param animLeftRight
	 *            ����λ���Ƿ񵽱�ҳ���һ����ѡ��λ��
	 * @param enableActiveHighlight
	 * @return
	 */
	public boolean pageBackward(boolean animLeftRight,
			boolean enableActiveHighlight) {
		if (null == mDecInfo)
			return false;

		if (mFlipper.isFlipping() || 0 == mCurrentPage)
			return false;

		int child = mFlipper.getDisplayedChild();
		int childNext = (child + 1) % 2;
		CandidateView cv = (CandidateView) mFlipper.getChildAt(child);
		CandidateView cvNext = (CandidateView) mFlipper.getChildAt(childNext);

		mCurrentPage--;
		int activeCandInPage = cv.getActiveCandiatePosInPage();
		if (animLeftRight)
			activeCandInPage = mDecInfo.mPageStart.elementAt(mCurrentPage + 1)
					- mDecInfo.mPageStart.elementAt(mCurrentPage) - 1;

		cvNext.showPage(mCurrentPage, activeCandInPage, enableActiveHighlight);
		loadAnimation(animLeftRight, false);
		startAnimation();

		updateArrowStatus();
		return true;
	}

	/**
	 * ����һҳ��ѡ��
	 * 
	 * @param animLeftRight
	 *            ����λ���Ƿ񵽱�ҳ��һ����ѡ��λ��
	 * @param enableActiveHighlight
	 * @return
	 */
	public boolean pageForward(boolean animLeftRight,
			boolean enableActiveHighlight) {
		if (null == mDecInfo)
			return false;

		if (mFlipper.isFlipping() || !mDecInfo.preparePage(mCurrentPage + 1)) {
			return false;
		}

		int child = mFlipper.getDisplayedChild();
		int childNext = (child + 1) % 2;
		CandidateView cv = (CandidateView) mFlipper.getChildAt(child);
		int activeCandInPage = cv.getActiveCandiatePosInPage();
		cv.enableActiveHighlight(enableActiveHighlight);

		CandidateView cvNext = (CandidateView) mFlipper.getChildAt(childNext);
		mCurrentPage++;
		if (animLeftRight)
			activeCandInPage = 0;

		cvNext.showPage(mCurrentPage, activeCandInPage, enableActiveHighlight);
		loadAnimation(animLeftRight, true);
		startAnimation();

		updateArrowStatus();
		return true;
	}

	/**
	 * ��ȡ����������ĺ�ѡ�������к�ѡ���е�λ��
	 * 
	 * @return
	 */
	public int getActiveCandiatePos() {
		if (null == mDecInfo)
			return -1;
		CandidateView cv = (CandidateView) mFlipper.getCurrentView();
		return cv.getActiveCandiatePosGlobal();
	}

	/**
	 * ���¼�ͷ��ʾ
	 */
	public void updateArrowStatus() {
		if (mCurrentPage < 0)
			return;
		boolean forwardEnabled = mDecInfo.pageForwardable(mCurrentPage);
		boolean backwardEnabled = mDecInfo.pageBackwardable(mCurrentPage);

		if (backwardEnabled) {
			enableArrow(mLeftArrowBtn, true);
		} else {
			enableArrow(mLeftArrowBtn, false);
		}
		if (forwardEnabled) {
			enableArrow(mRightArrowBtn, true);
		} else {
			enableArrow(mRightArrowBtn, false);
		}
	}

	/**
	 * ���ü�ͷͼ���Ƿ���Ч����ͼ���͸���ȡ�
	 * 
	 * @param arrowBtn
	 * @param enabled
	 */
	private void enableArrow(ImageButton arrowBtn, boolean enabled) {
		arrowBtn.setEnabled(enabled);
		if (enabled)
			arrowBtn.setAlpha(ARROW_ALPHA_ENABLED);
		else
			arrowBtn.setAlpha(ARROW_ALPHA_DISABLED);
	}

	/**
	 * ���ü�ͷͼ���Ƿ���ʾ
	 * 
	 * @param arrowBtn
	 * @param show
	 */
	private void showArrow(ImageButton arrowBtn, boolean show) {
		if (show)
			arrowBtn.setVisibility(View.VISIBLE);
		else
			arrowBtn.setVisibility(View.INVISIBLE);
	}

	/**
	 * view�Ĵ����¼�������
	 * 
	 * @param v
	 * @param event
	 */
	public boolean onTouch(View v, MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			if (v == mLeftArrowBtn) {
				// ���ú�ѡ����ͼ�����������һ������ƴ�����
				mCvListener.onToRightGesture();
			} else if (v == mRightArrowBtn) {
				// ���ú�ѡ����ͼ�����������󻬶����ƴ�����
				mCvListener.onToLeftGesture();
			}
		} else if (event.getAction() == MotionEvent.ACTION_UP) {
			// ���ú�ѡ����ͼ������ĺ�ѡ�ʡ�
			CandidateView cv = (CandidateView) mFlipper.getCurrentView();
			cv.enableActiveHighlight(true);
		}

		return false;
	}

	// The reason why we handle candiate view's touch events here is because
	// that the view under the focused view may get touch events instead of the
	// focused one.
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO �����¼����������������Ϊԭ�㣿
		// ���������¼�����������
		event.offsetLocation(-xOffsetForFlipper, 0);
		// ���ú�ѡ����ͼ�����¼�������
		CandidateView cv = (CandidateView) mFlipper.getCurrentView();
		cv.onTouchEventReal(event);
		return true;
	}

	/**
	 * ���������������ø�ViewFlipper mFlipper��
	 * 
	 * @param animLeftRight
	 * @param forward
	 */
	public void loadAnimation(boolean animLeftRight, boolean forward) {
		if (animLeftRight) {
			if (forward) {
				if (null == mInAnimPushLeft) {
					mInAnimPushLeft = createAnimation(1.0f, 0, 0, 0, 0, 1.0f,
							ANIMATION_TIME);
					mOutAnimPushLeft = createAnimation(0, -1.0f, 0, 0, 1.0f, 0,
							ANIMATION_TIME);
				}
				mInAnimInUse = mInAnimPushLeft;
				mOutAnimInUse = mOutAnimPushLeft;
			} else {
				if (null == mInAnimPushRight) {
					mInAnimPushRight = createAnimation(-1.0f, 0, 0, 0, 0, 1.0f,
							ANIMATION_TIME);
					mOutAnimPushRight = createAnimation(0, 1.0f, 0, 0, 1.0f, 0,
							ANIMATION_TIME);
				}
				mInAnimInUse = mInAnimPushRight;
				mOutAnimInUse = mOutAnimPushRight;
			}
		} else {
			if (forward) {
				if (null == mInAnimPushUp) {
					mInAnimPushUp = createAnimation(0, 0, 1.0f, 0, 0, 1.0f,
							ANIMATION_TIME);
					mOutAnimPushUp = createAnimation(0, 0, 0, -1.0f, 1.0f, 0,
							ANIMATION_TIME);
				}
				mInAnimInUse = mInAnimPushUp;
				mOutAnimInUse = mOutAnimPushUp;
			} else {
				if (null == mInAnimPushDown) {
					mInAnimPushDown = createAnimation(0, 0, -1.0f, 0, 0, 1.0f,
							ANIMATION_TIME);
					mOutAnimPushDown = createAnimation(0, 0, 0, 1.0f, 1.0f, 0,
							ANIMATION_TIME);
				}
				mInAnimInUse = mInAnimPushDown;
				mOutAnimInUse = mOutAnimPushDown;
			}
		}

		// ���ö�����������������������ʱ�򣬵���onAnimationEnd������
		mInAnimInUse.setAnimationListener(this);

		mFlipper.setInAnimation(mInAnimInUse);
		mFlipper.setOutAnimation(mOutAnimInUse);
	}

	/**
	 * �����ƶ�����
	 * 
	 * @param xFrom
	 * @param xTo
	 * @param yFrom
	 * @param yTo
	 * @param alphaFrom
	 * @param alphaTo
	 * @param duration
	 * @return
	 */
	private Animation createAnimation(float xFrom, float xTo, float yFrom,
			float yTo, float alphaFrom, float alphaTo, long duration) {
		AnimationSet animSet = new AnimationSet(getContext(), null);
		Animation trans = new TranslateAnimation(Animation.RELATIVE_TO_SELF,
				xFrom, Animation.RELATIVE_TO_SELF, xTo,
				Animation.RELATIVE_TO_SELF, yFrom, Animation.RELATIVE_TO_SELF,
				yTo);
		Animation alpha = new AlphaAnimation(alphaFrom, alphaTo);
		animSet.addAnimation(trans);
		animSet.addAnimation(alpha);
		animSet.setDuration(duration);
		return animSet;
	}

	/**
	 * ��ʼ������mFlipper��ʾ��һ����
	 */
	private void startAnimation() {
		mFlipper.showNext();
	}

	/**
	 * ֹͣ������mFlipperֹͣ�л�Flipping��
	 */
	private void stopAnimation() {
		mFlipper.stopFlipping();
	}

	/**
	 * ����������������ֹͣ��ʱ��ļ������ص���
	 */
	public void onAnimationEnd(Animation animation) {
		if (!mLeftArrowBtn.isPressed() && !mRightArrowBtn.isPressed()) {
			CandidateView cv = (CandidateView) mFlipper.getCurrentView();
			cv.enableActiveHighlight(true);
		}
	}

	/**
	 * �����������������ظ���ʱ��ļ������ص���
	 */
	public void onAnimationRepeat(Animation animation) {
	}

	/**
	 * ������������������ʼ��ʱ��ļ������ص���
	 */
	public void onAnimationStart(Animation animation) {
	}
}
