package com.idata.bluetoothime;

/**
 * Interface to notify the input method when the user clicks a candidate or
 * makes a direction-gesture on candidate view.
 */
/**
 * ��ѡ����ͼ�������ӿ�
 * 
 * @ClassName CandidateViewListener
 * @author LiChao
 */
public interface CandidateViewListener {

	/**
	 * ѡ���˺�ѡ�ʵĴ�����
	 * 
	 * @param choiceId
	 */
	public void onClickChoice(int choiceId);

	/**
	 * ���󻬶������ƴ�����
	 */
	public void onToLeftGesture();

	/**
	 * ���һ��������ƴ�����
	 */
	public void onToRightGesture();

	/**
	 * ���ϻ��������ƴ�����
	 */
	public void onToTopGesture();

	/**
	 * ���»��������ƴ�����
	 */
	public void onToBottomGesture();
}
