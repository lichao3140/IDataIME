package com.idata.bluetoothime;

import java.util.Vector;
import android.content.Context;

/**
 * Class used to cache previously loaded soft keyboard layouts.
 */
/**
 * ������ڴ�أ�������õ���ģʽ���������������б������ģ���б�������б�
 * 
 * @ClassName SkbPool
 * @author LiChao
 */
public class SkbPool {
	private static SkbPool mInstance = null;

	private Vector<SkbTemplate> mSkbTemplates = new Vector<SkbTemplate>();
	private Vector<SoftKeyboard> mSoftKeyboards = new Vector<SoftKeyboard>();

	private SkbPool() {
	}

	public static SkbPool getInstance() {
		if (null == mInstance)
			mInstance = new SkbPool();
		return mInstance;
	}

	public void resetCachedSkb() {
		mSoftKeyboards.clear();
	}

	/**
	 * ��ȡ�����ģ�档�߼���飺�����ȴ�mSkbTemplates�б��л�ȡ�����û�л�ȡ����
	 * �͵���XmlKeyboardLoader������Դ�ļ�IDΪskbTemplateId�������ģ��xml�ļ�
	 * ������һ��ģ�棬������mSkbTemplates�б��С�
	 * 
	 * @param skbTemplateId
	 * @param context
	 * @return
	 */
	public SkbTemplate getSkbTemplate(int skbTemplateId, Context context) {
		for (int i = 0; i < mSkbTemplates.size(); i++) {
			SkbTemplate t = mSkbTemplates.elementAt(i);
			if (t.getSkbTemplateId() == skbTemplateId) {
				return t;
			}
		}

		if (null != context) {
			XmlKeyboardLoader xkbl = new XmlKeyboardLoader(context);
			SkbTemplate t = xkbl.loadSkbTemplate(skbTemplateId);
			if (null != t) {
				mSkbTemplates.add(t);
				return t;
			}
		}
		return null;
	}

	// Try to find the keyboard in the pool with the cache id. If there is no
	// keyboard found, try to load it with the given xml id.
	/**
	 * ��ȡ����̡��߼���飺�����ȴ�mSoftKeyboards�б��л�ȡ�����û�л�ȡ����
	 * �͵���XmlKeyboardLoader������Դ�ļ�IDΪskbXmlId�������xml�ļ�
	 * ������һ������̣�������mSoftKeyboards�б��С�
	 * 
	 * @param skbCacheId
	 * @param skbXmlId
	 * @param skbWidth
	 * @param skbHeight
	 * @param context
	 * @return
	 */
	public SoftKeyboard getSoftKeyboard(int skbCacheId, int skbXmlId,
			int skbWidth, int skbHeight, Context context) {
		for (int i = 0; i < mSoftKeyboards.size(); i++) {
			SoftKeyboard skb = mSoftKeyboards.elementAt(i);
			if (skb.getCacheId() == skbCacheId && skb.getSkbXmlId() == skbXmlId) {
				skb.setSkbCoreSize(skbWidth, skbHeight);
				skb.setNewlyLoadedFlag(false);
				return skb;
			}
		}
		if (null != context) {
			XmlKeyboardLoader xkbl = new XmlKeyboardLoader(context);
			SoftKeyboard skb = xkbl.loadKeyboard(skbXmlId, skbWidth, skbHeight);
			if (skb != null) {
				if (skb.getCacheFlag()) {
					skb.setCacheId(skbCacheId);
					mSoftKeyboards.add(skb);
				}
			}
			return skb;
		}
		return null;
	}
}
