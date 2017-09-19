package doext.choosephotos;

import java.util.List;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import core.DoServiceContainer;
import core.helper.DoResourcesHelper;
import core.interfaces.DoIModuleTypeID;
import doext.app.do_Album_App;
import doext.choosephotos.BitmapCache.ImageCallback;
import doext.implement.ConstantValue;

public class ImageGridAdapter extends BaseAdapter implements DoIModuleTypeID {

	private TextCallback textcallback = null;
	private final String TAG = getClass().getSimpleName();
	private Activity act;
	private List<ImageItem> dataList;
	private BitmapCache cache;
	private Handler mHandler;
	private int selectTotal = 0;
	private AlbumHelper helper;
	private ImageCallback callback = new ImageCallback() {
		public void imageLoad(ImageView imageView, Bitmap bitmap, Object... params) {
			if (imageView != null && bitmap != null) {
				String url = (String) params[0];
				if (url != null && url.equals((String) imageView.getTag())) {
					((ImageView) imageView).setImageBitmap(bitmap);
				} else {
					Log.e(TAG, "callback, bmp not match");
				}
			} else {
				Log.e(TAG, "callback, bmp null");
			}
		}
	};

	public static interface TextCallback {
		public void onListen(int count);
	}

	public void setTextCallback(TextCallback listener) {
		textcallback = listener;
	}

	public ImageGridAdapter(Activity act, List<ImageItem> list, Handler mHandler) {
		this.act = act;
		dataList = list;
		cache = new BitmapCache();
		this.mHandler = mHandler;
		helper = AlbumHelper.getHelper();
		helper.init(DoServiceContainer.getPageViewFactory().getAppContext());
	}

	@Override
	public int getCount() {
		int count = 0;
		if (dataList != null) {
			count = dataList.size();
		}
		return count;
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	private class Holder {
		private ImageView iv;
		private ImageView selected;
		private TextView text;
		private TextView duration;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final Holder holder;

		if (convertView == null) {
			holder = new Holder();
			int album_image_itme_id = DoResourcesHelper.getIdentifier("album_image_itme", "layout", this);
			convertView = View.inflate(act, album_image_itme_id, null);
			int image_id = DoResourcesHelper.getIdentifier("image", "id", this);
			holder.iv = (ImageView) convertView.findViewById(image_id);
			int isselected_id = DoResourcesHelper.getIdentifier("isselected", "id", this);
			holder.selected = (ImageView) convertView.findViewById(isselected_id);
			int item_image_grid_text_id = DoResourcesHelper.getIdentifier("item_image_grid_text", "id", this);
			holder.text = (TextView) convertView.findViewById(item_image_grid_text_id);
			int video_duration_id = DoResourcesHelper.getIdentifier("video_duration", "id", this);
			holder.duration = (TextView) convertView.findViewById(video_duration_id);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}
		final ImageItem item = dataList.get(position);
		holder.iv.setTag(item.imagePath);
		cache.displayBmp(holder.iv, item.thumbnailPath, item.imagePath, callback);
		if (item.isSelected) {
			int album_select_id = DoResourcesHelper.getIdentifier("album_select", "drawable", this);
			holder.selected.setImageResource(album_select_id);
			int album_select_border_id = DoResourcesHelper.getIdentifier("album_select_border", "drawable", this);
			holder.text.setBackgroundResource(album_select_border_id);
		} else {
			holder.selected.setImageBitmap(null);
			holder.text.setBackgroundColor(Color.TRANSPARENT);
		}
		if (item.duration.equals("00:00")) {
			holder.duration.setVisibility(View.GONE);
		} else {
			holder.duration.setText(item.duration);
			holder.duration.setVisibility(View.VISIBLE);
		}

		holder.iv.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (selectTotal < ConstantValue.MAX_COUNT) {
					item.isSelected = !item.isSelected;
					if (item.isSelected) {
						int album_select_id = DoResourcesHelper.getIdentifier("album_select", "drawable", ImageGridAdapter.this);
						holder.selected.setImageResource(album_select_id);
						int album_select_border_id = DoResourcesHelper.getIdentifier("album_select_border", "drawable", ImageGridAdapter.this);
						holder.text.setBackgroundResource(album_select_border_id);
						selectTotal++;
						if (textcallback != null)
							textcallback.onListen(selectTotal);
						if (helper.currentType.equals("image")) {
							BitmapUtils.selectPaths.add(item.imagePath);
						} else {
							BitmapUtils.selectPaths.add(item.thumbnailPath);
							helper.video_list.add(item.imagePath);
						}
					} else {
						holder.selected.setImageBitmap(null);
						holder.text.setBackgroundColor(Color.TRANSPARENT);
						selectTotal--;
						if (textcallback != null)
							textcallback.onListen(selectTotal);
						if (helper.currentType.equals("image")) {
							BitmapUtils.selectPaths.remove(item.imagePath);
						} else {
							BitmapUtils.selectPaths.remove(item.thumbnailPath);
							helper.video_list.remove(item.imagePath);
						}
					}
				} else if (selectTotal >= ConstantValue.MAX_COUNT) {
					if (item.isSelected) {
						item.isSelected = !item.isSelected;
						holder.selected.setImageBitmap(null);
						holder.text.setBackgroundColor(Color.TRANSPARENT);
						selectTotal--;
						if (textcallback != null)
							textcallback.onListen(selectTotal);
						if (helper.currentType.equals("image")) {
							BitmapUtils.selectPaths.remove(item.imagePath);
						} else {
							BitmapUtils.selectPaths.remove(item.thumbnailPath);
							helper.video_list.remove(item.imagePath);
						}
					} else {
						Message message = Message.obtain(mHandler, 0);
						message.sendToTarget();
					}
				}
			}
		});
		return convertView;
	}

	@Override
	public String getTypeID() {
		return do_Album_App.getInstance().getModuleTypeID();
	}
}
