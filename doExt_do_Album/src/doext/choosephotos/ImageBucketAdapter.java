package doext.choosephotos;

import java.util.List;

import android.app.Activity;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import core.helper.DoResourcesHelper;
import core.interfaces.DoIModuleTypeID;
import doext.app.do_Album_App;
import doext.choosephotos.BitmapCache.ImageCallback;

public class ImageBucketAdapter extends BaseAdapter implements DoIModuleTypeID {
	private final String TAG = getClass().getSimpleName();

	private Activity act;
	/**
	 * 图片集列表
	 */
	private List<ImageBucket> dataList;
	private BitmapCache cache;
	private ImageCallback callback = new ImageCallback() {
		@Override
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

	public ImageBucketAdapter(Activity act, List<ImageBucket> list) {
		this.act = act;
		dataList = list;

		cache = new BitmapCache();

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
	public Object getItem(int arg0) {
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	class Holder {
		private ImageView iv;
		private ImageView selected;
		private TextView name;
		private TextView count;
		private ImageView bucket_video_image;
	}

	@Override
	public View getView(int arg0, View arg1, ViewGroup arg2) {
		Holder holder;
		if (arg1 == null) {
			holder = new Holder();
			int album_bucket_itme_id = DoResourcesHelper.getIdentifier("album_bucket_itme", "layout", this);
			arg1 = View.inflate(act, album_bucket_itme_id, null);
			int image_id = DoResourcesHelper.getIdentifier("image", "id", this);
			holder.iv = (ImageView) arg1.findViewById(image_id);
			int isselected_id = DoResourcesHelper.getIdentifier("isselected", "id", this);
			holder.selected = (ImageView) arg1.findViewById(isselected_id);
			int name_id = DoResourcesHelper.getIdentifier("name", "id", this);
			holder.name = (TextView) arg1.findViewById(name_id);
			int count_id = DoResourcesHelper.getIdentifier("count", "id", this);
			holder.count = (TextView) arg1.findViewById(count_id);
			int bucket_video_image_id = DoResourcesHelper.getIdentifier("bucket_video_image", "id", this);
			holder.bucket_video_image = (ImageView) arg1.findViewById(bucket_video_image_id);
			arg1.setTag(holder);
		} else {
			holder = (Holder) arg1.getTag();
		}

		ImageBucket item = dataList.get(arg0);
		holder.count.setText("" + item.count);
		holder.name.setText(item.bucketName);
		holder.selected.setVisibility(View.GONE);
		if (item.imageList != null && item.imageList.size() > 0) {
			String thumbPath = item.imageList.get(0).thumbnailPath;
			String sourcePath = item.imageList.get(0).imagePath;
			holder.iv.setTag(sourcePath);
			cache.displayBmp(holder.iv, thumbPath, sourcePath, callback);
		} else {
			holder.iv.setImageBitmap(null);
			Log.e(TAG, "no images in bucket " + item.bucketName);
		}
		if ("Video".equals(item.bucketName))
			holder.bucket_video_image.setVisibility(View.VISIBLE);
		return arg1;
	}

	@Override
	public String getTypeID() {
		return do_Album_App.getInstance().getModuleTypeID();
	}

}
