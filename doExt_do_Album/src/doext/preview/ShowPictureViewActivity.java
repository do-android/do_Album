package doext.preview;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import core.helper.DoResourcesHelper;
import core.interfaces.DoIModuleTypeID;
import doext.app.do_Album_App;
import doext.choosephotos.AlbumHelper;
import doext.choosephotos.BitmapUtils;
import doext.preview.custom.HackyViewPager;
import doext.preview.custom.PhotoView;

public class ShowPictureViewActivity extends Activity implements DoIModuleTypeID {

	private HackyViewPager mViewPager;
	private TextView tv_count;
	private static final int MAXWIDTH = 1000;
	private static final int MAXHEIGHT = 1000;
	private AlbumHelper helper;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		int album_preview_activity_id = DoResourcesHelper.getIdentifier("album_preview_activity", "layout", this);
		setContentView(album_preview_activity_id);
		int viewpager_id = DoResourcesHelper.getIdentifier("viewpager", "id", this);
		mViewPager = (HackyViewPager) findViewById(viewpager_id);
		int count_id = DoResourcesHelper.getIdentifier("count", "id", this);
		tv_count = (TextView) findViewById(count_id);
		helper = AlbumHelper.getHelper();
		helper.init(getApplicationContext());
		helper.init(getApplicationContext());
		tv_count.setText("1/" + BitmapUtils.selectPaths.size());
		mViewPager.setAdapter(new SamplePagerAdapter());
		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
				tv_count.setText((position + 1) + "/" + BitmapUtils.selectPaths.size());
			}

			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

			}

			@Override
			public void onPageScrollStateChanged(int state) {

			}
		});

		int back_id = DoResourcesHelper.getIdentifier("back", "id", this);
		findViewById(back_id).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

	private class SamplePagerAdapter extends PagerAdapter {

		@Override
		public int getCount() {
			return BitmapUtils.selectPaths.size();
		}

		@Override
		public View instantiateItem(ViewGroup container, final int position) {
			RelativeLayout layout = new RelativeLayout(ShowPictureViewActivity.this);
			PhotoView photoView = new PhotoView(container.getContext());
			try {
				Bitmap bmp;
				String path = BitmapUtils.selectPaths.get(position);
				SoftReference<Bitmap> softBmp = imageCache.get(path);
				if (softBmp == null) {
					bmp = BitmapUtils.revitionImageSize(path, MAXWIDTH, MAXHEIGHT);
					imageCache.put(path, new SoftReference<Bitmap>(bmp));
				} else {
					bmp = softBmp.get();
					if (bmp == null) { // 图片被回收掉了
						bmp = BitmapUtils.revitionImageSize(path, MAXWIDTH, MAXHEIGHT);
						imageCache.put(path, new SoftReference<Bitmap>(bmp));
					}
				}
				photoView.setImageBitmap(bmp);
			} catch (Exception e) {
				e.printStackTrace();
			}
			RelativeLayout.LayoutParams lp1 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
			layout.addView(photoView, lp1);
			if (helper.currentType.contains("video")) {
				final ImageView imageView = new ImageView(ShowPictureViewActivity.this);
				imageView.setTag(position);
				int video_icon_id = DoResourcesHelper.getIdentifierByStr("album_video_big", "drawable", "do_Album");
				imageView.setBackgroundResource(video_icon_id);
				RelativeLayout.LayoutParams lp2 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
				lp2.addRule(RelativeLayout.CENTER_IN_PARENT);
				layout.addView(imageView, lp2);
				imageView.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						int index = Integer.valueOf(v.getTag().toString());
						String videoPath = helper.video_list.get(index);
						Intent intent = new Intent(Intent.ACTION_VIEW);
						String bpath = "file://" + videoPath;
						intent.setDataAndType(Uri.parse(bpath), "video/*");
						startActivity(intent);
					}
				});
			}
			container.addView(layout, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			return layout;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		int cacheSize = imageCache.size();
		if (null != imageCache && cacheSize > 0) {
			for (Entry<String, SoftReference<Bitmap>> entry : imageCache.entrySet()) {
				recycleImageBitmap(entry.getKey());
			}
			imageCache.clear();
		}
	}

	private void recycleImageBitmap(String url) {
		if (null == url)
			return;
		SoftReference<Bitmap> bitmap = imageCache.get(url);
		Bitmap result = bitmap.get();
		if (null != bitmap && null != result) {
			if (!result.isRecycled()) {
				result.recycle();
				result = null;
			}
		}
		System.gc();
	}

//	// 存放缓存图片
	private Map<String, SoftReference<Bitmap>> imageCache = new HashMap<String, SoftReference<Bitmap>>();

	@Override
	public String getTypeID() {
		return do_Album_App.getInstance().getModuleTypeID();
	}
}