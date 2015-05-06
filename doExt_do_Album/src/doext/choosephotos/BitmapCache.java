package doext.choosephotos;

import java.lang.ref.SoftReference;
import java.util.HashMap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;
import core.helper.DoResourcesHelper;
import core.interfaces.DoIModuleTypeID;
import doext.app.do_Album_App;

public class BitmapCache implements DoIModuleTypeID {

	private final String TAG = getClass().getSimpleName();
	private Handler h = new Handler();
	private HashMap<String, SoftReference<Bitmap>> imageCache = new HashMap<String, SoftReference<Bitmap>>();
	private static final int MAXWIDTH = 256;
	private static final int MAXHEIGHT = 256;

	private void put(String path, Bitmap bmp) {
		if (!TextUtils.isEmpty(path) && bmp != null) {
			imageCache.put(path, new SoftReference<Bitmap>(bmp));
		}
	}

	public void displayBmp(final ImageView iv, final String thumbPath, final String sourcePath, final ImageCallback callback) {
		if (TextUtils.isEmpty(thumbPath) && TextUtils.isEmpty(sourcePath)) {
			Log.e(TAG, "no paths pass in");
			return;
		}

		final String path;
		final boolean isThumbPath;
		if (!TextUtils.isEmpty(thumbPath)) {
			path = thumbPath;
			isThumbPath = true;
		} else if (!TextUtils.isEmpty(sourcePath)) {
			path = sourcePath;
			isThumbPath = false;
		} else {
			return;
		}

		if (imageCache.containsKey(path)) {
			SoftReference<Bitmap> reference = imageCache.get(path);
			Bitmap bmp = reference.get();
			if (bmp != null) {
				if (callback != null) {
					callback.imageLoad(iv, bmp, sourcePath);
				}
				iv.setImageBitmap(bmp);
				Log.d(TAG, "hit cache");
				return;
			}
		}
		iv.setImageBitmap(null);

		new Thread() {
			Bitmap thumb;

			public void run() {
				try {
					if (isThumbPath) {
						thumb = BitmapFactory.decodeFile(thumbPath);
						if (thumb == null) {
							thumb = BitmapUtils.revitionImageSize(sourcePath, MAXWIDTH, MAXHEIGHT);
						}
					} else {
						thumb = BitmapUtils.revitionImageSize(sourcePath, MAXWIDTH, MAXHEIGHT);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (thumb == null) {
					int album_unfocused_id = DoResourcesHelper.getIdentifier("album_unfocused", "drawable", BitmapCache.this);
					thumb = BitmapFactory.decodeResource(iv.getResources(), album_unfocused_id);
				}
				Log.e(TAG, "-------thumb------" + thumb);
				put(path, thumb);
				if (callback != null) {
					h.post(new Runnable() {
						@Override
						public void run() {
							callback.imageLoad(iv, thumb, sourcePath);
						}
					});
				}
			}
		}.start();

	}

	public interface ImageCallback {
		public void imageLoad(ImageView imageView, Bitmap bitmap, Object... params);
	}

	@Override
	public String getTypeID() {
		return do_Album_App.getInstance().getModuleTypeID();
	}
}
