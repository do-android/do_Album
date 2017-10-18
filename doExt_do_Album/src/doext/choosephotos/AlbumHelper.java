package doext.choosephotos;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.media.ExifInterface;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.provider.MediaStore.Images.Thumbnails;
import android.text.TextUtils;

/**
 * 专辑帮助类
 * 
 * @author Administrator
 * 
 */
public class AlbumHelper {
	private Context context;
	private ContentResolver cr;
	// 缩略图列表
	private HashMap<String, String> thumbnailList = new HashMap<String, String>();
	// 专辑列表
	public HashMap<String, ImageBucket> bucketList = new HashMap<String, ImageBucket>();
	// 是否创建了图片集
//	private boolean hasBuildImagesBucketList = false;
	// 用于保存选中的视频路径
	public List<String> video_list = new ArrayList<String>();
	private static AlbumHelper instance;
	// 用于判断当前是照片库还是视频库
	public String currentType;

	private AlbumHelper() {
	}

	public static AlbumHelper getHelper() {
		if (instance == null) {
			instance = new AlbumHelper();
		}
		return instance;
	}

	/**
	 * 初始化
	 * 
	 * @param context
	 */
	public void init(Context context) {
		if (this.context == null) {
			this.context = context;
			cr = context.getContentResolver();
		}
	}

	/**
	 * 得到缩略图
	 */
	private void getThumbnail() {
		String[] projection = { Thumbnails._ID, Thumbnails.IMAGE_ID, Thumbnails.DATA };
		Cursor cursor = cr.query(Thumbnails.EXTERNAL_CONTENT_URI, projection, null, null, null);
		getThumbnailColumnData(cursor);
	}

	/**
	 * 从数据库中得到缩略图
	 * 
	 * @param cur
	 */
	private void getThumbnailColumnData(Cursor cur) {
		if (cur.moveToFirst()) {
			int image_id;
			String image_path;
			int image_idColumn = cur.getColumnIndex(Thumbnails.IMAGE_ID);
			int dataColumn = cur.getColumnIndex(Thumbnails.DATA);

			do {
				image_id = cur.getInt(image_idColumn);
				image_path = cur.getString(dataColumn);
				thumbnailList.put("" + image_id, image_path);
			} while (cur.moveToNext());
		}
	}

	/**
	 * 得到图片集 0-图片与视频；1-仅图片；2-仅视频
	 */
	private void buildImagesBucketList(int type) {
		if (type == 1) {
			getImageBucket();
		} else if (type == 2) {
			getVideoBucket();
		} else {
			getImageBucket();
			getVideoBucket();
		}

	}

	private void getImageBucket() {
		// 构造缩略图索引
		getThumbnail();
		// 构造相册索引
		String columns[] = new String[] { Media._ID, Media.BUCKET_ID, Media.PICASA_ID, Media.DATA, Media.DISPLAY_NAME, Media.TITLE, Media.SIZE, Media.BUCKET_DISPLAY_NAME };
		// 得到一个游标
		Cursor cur = cr.query(Media.EXTERNAL_CONTENT_URI, columns, null, null, "_id DESC");
		if (cur.moveToFirst()) {
			// 获取指定列的索引
			int photoIDIndex = cur.getColumnIndexOrThrow(Media._ID);
			int photoPathIndex = cur.getColumnIndexOrThrow(Media.DATA);
			int bucketDisplayNameIndex = cur.getColumnIndexOrThrow(Media.BUCKET_DISPLAY_NAME);
			int bucketIdIndex = cur.getColumnIndexOrThrow(Media.BUCKET_ID);
			do {
				String _id = cur.getString(photoIDIndex);
				String path = cur.getString(photoPathIndex);
				String bucketName = cur.getString(bucketDisplayNameIndex);
				String bucketId = cur.getString(bucketIdIndex);
				ImageBucket bucket = bucketList.get(bucketId);
				if (bucket == null) {
					bucket = new ImageBucket();
					bucketList.put(bucketId, bucket);
					bucket.imageList = new ArrayList<ImageItem>();
					bucket.bucketName = bucketName;
				}
				bucket.count++;
				ImageItem imageItem = new ImageItem();
				imageItem.imageId = _id;
				imageItem.imagePath = path;
				imageItem.thumbnailPath = thumbnailList.get(_id);
				imageItem.duration = "00:00";
				bucket.imageList.add(imageItem);

			} while (cur.moveToNext());
		}
	}

	private void getVideoBucket() {
		// 构造视频缩略图
		getVideoThumbnail();
		// 添加视频bucket
		String[] proj = { MediaStore.Video.Thumbnails._ID, MediaStore.Video.Thumbnails.DATA, MediaStore.Video.Media.DURATION, MediaStore.Video.Media.SIZE, MediaStore.Video.Media.DISPLAY_NAME,
				MediaStore.Video.Media.DATE_MODIFIED };
		Cursor mCursor = cr.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, proj, MediaStore.Video.Media.MIME_TYPE + "=?", new String[] { "video/mp4" }, MediaStore.Video.Media.DATE_MODIFIED
				+ " desc");
		if (mCursor != null) {
			ImageBucket bucket = new ImageBucket();
			bucket.imageList = new ArrayList<ImageItem>();
			bucket.bucketName = "Video";
			bucketList.put("video", bucket);
			while (mCursor.moveToNext()) {
				// 获取视频的路径
				String videoId = mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID));
				String path = mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
				long duration = mCursor.getLong(mCursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION));
				// 提前生成缩略图，再获取：http://stackoverflow.com/questions/27903264/how-to-get-the-video-thumbnail-path-and-not-the-bitmap
				MediaStore.Video.Thumbnails.getThumbnail(cr, Long.valueOf(videoId), MediaStore.Video.Thumbnails.MICRO_KIND, null);
				bucket.count++;
				ImageItem imageItem = new ImageItem();
				imageItem.imageId = videoId;
				imageItem.imagePath = path;
				imageItem.thumbnailPath = thumbnailList.get(videoId);
				imageItem.duration = getFormat(duration);
				bucket.imageList.add(imageItem);
			}
			mCursor.close();
		}
	}

	private String getFormat(long data) {
		long second = data / 1000;
		SimpleDateFormat sdf = null;
		if (second >= 0 && second < 3600) {
			sdf = new SimpleDateFormat("mm:ss", Locale.getDefault());
			sdf.setTimeZone(TimeZone.getTimeZone("GMT+0"));
		} else {
			sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
			sdf.setTimeZone(TimeZone.getTimeZone("GMT+0"));
		}
		return sdf.format(data);
	}

	private void getVideoThumbnail() {
		String[] thumbColumns = { MediaStore.Video.Thumbnails.DATA, MediaStore.Video.Thumbnails.VIDEO_ID };
		// 视频其他信息的查询条件
		String[] mediaColumns = { MediaStore.Video.Media._ID, MediaStore.Video.Media.DATA, MediaStore.Video.Media.DURATION };
		Cursor cursor = context.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, mediaColumns, null, null, null);
		if (null != cursor) {
			if (cursor.moveToFirst()) {
				do {
					int id = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media._ID));
					Cursor thumbCursor = context.getContentResolver()
							.query(MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI, thumbColumns, MediaStore.Video.Thumbnails.VIDEO_ID + "=" + id, null, null);
					if (thumbCursor.moveToFirst()) {
						String thumbnails = thumbCursor.getString(thumbCursor.getColumnIndex(MediaStore.Video.Thumbnails.DATA));
						// ThumbnailUtils.createVideoThumbnail(thumbnails,
						// kind);
						thumbnailList.put("" + id, thumbnails);
					}
				} while (cursor.moveToNext());
			}
			cursor.close();
		}
	}

	/**
	 * 得到图片集
	 * 
	 * @param refresh
	 * @return
	 */
	public List<ImageBucket> getImagesBucketList(int type) {
		clear();
		buildImagesBucketList(type);
		List<ImageBucket> tmpList = new ArrayList<ImageBucket>();
		Iterator<Entry<String, ImageBucket>> itr = bucketList.entrySet().iterator();
		while (itr.hasNext()) {
			Map.Entry<String, ImageBucket> entry = (Map.Entry<String, ImageBucket>) itr.next();
			tmpList.add(entry.getValue());
		}
		return tmpList;
	}

	// 保存元数据
	public void saveExif(String oldFilePath, String newFilePath) throws Exception {
		ExifInterface oldExif = new ExifInterface(oldFilePath);
		ExifInterface newExif = new ExifInterface(newFilePath);
		Class<ExifInterface> cls = ExifInterface.class;
		Field[] fields = cls.getFields();
		for (int i = 0; i < fields.length; i++) {
			String fieldName = fields[i].getName();
			if (!TextUtils.isEmpty(fieldName) && fieldName.startsWith("TAG")) {
				String fieldValue = fields[i].get(cls).toString();
				String attribute = oldExif.getAttribute(fieldValue);
				if (attribute != null) {
					newExif.setAttribute(fieldValue, attribute);
				}
			}
		}
		newExif.saveAttributes();
	}

	public void clear() {
		bucketList.clear();
	}
}