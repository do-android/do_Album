package doext.choosephotos;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import core.DoServiceContainer;

public class BitmapUtils {
	public static List<String> selectPaths = new ArrayList<String>();

	public static Bitmap revitionImageSize(String path, int maxWidth, int maxHeight) throws IOException {
		BufferedInputStream in = new BufferedInputStream(new FileInputStream(new File(path)));
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeStream(in, null, options);
		in.close();
		int i = 0;
		Bitmap bitmap = null;
		while (true) {
			if ((options.outWidth >> i <= maxWidth) && (options.outHeight >> i <= maxHeight)) {
				in = new BufferedInputStream(new FileInputStream(new File(path)));
				options.inSampleSize = (int) Math.pow(2.0D, i);
				options.inJustDecodeBounds = false;
				bitmap = BitmapFactory.decodeStream(in, null, options);
				break;
			}
			i += 1;
		}
		return bitmap;
	}

	private static final int MAX_WIDTH = 768;
	private static final int MAX_HEIGHT = 1024;

	// 按给定长宽比缩放得到图像
	public static Bitmap resizeRealImage(String path, int width, int height) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPurgeable = true;
		options.inJustDecodeBounds = true;// 不加载bitmap到内存中

		BitmapFactory.decodeFile(path, options);

		if (width == -1 && height > 0) {
			options.inSampleSize = options.outHeight / height;
		} else if (height == -1 && width > 0) {
			options.inSampleSize = options.outWidth / width;
		} else if (width == -1 && height == -1) {
			if (options.outWidth > MAX_WIDTH || options.outHeight > MAX_HEIGHT) {
				options.inSampleSize = computeSampleSize(options, -1, MAX_WIDTH * MAX_HEIGHT);
			} else {
				options.inSampleSize = 1;
			}
		} else {
			options.inSampleSize = computeSampleSize(options, -1, width * height);
		}

		options.inJustDecodeBounds = false;

		Bitmap bm = BitmapFactory.decodeFile(path, options);
		// 处理旋转
		int degree = readPictureDegree(path);
		// 取得想要缩放的matrix参数
		Matrix matrix = new Matrix();
		// 旋转图片
		if (degree != 0) {
			matrix.postRotate(degree);
		}

		if (width > 0 && height > 0 && (width != bm.getWidth() || height != bm.getHeight())) {
			// 计算缩放比例
			float scaleWidth = ((float) width) / bm.getWidth();
			float scaleHeight = ((float) height) / bm.getHeight();
			matrix.postScale(scaleWidth, scaleHeight);
		}
		// 得到新的图片
		return Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
	}

	private static int computeSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {
		// 宽高为 -1,-1 和 宽高 -2,-2
		if (maxNumOfPixels == 1 || maxNumOfPixels == 4) {
			maxNumOfPixels = -1;
		}
		int initialSize = computeInitialSampleSize(options, minSideLength, maxNumOfPixels);
		if (initialSize == 1) {
			int x = DoServiceContainer.getGlobal().getScreenWidth();
			int y = DoServiceContainer.getGlobal().getScreenHeight();
			// 原图最大不能超过分辨率大小，如超出则进行缩小尺寸，避免OOM；
			initialSize = calculateInSampleSize(options, x, y);
		}
		if (initialSize > 5) {// 最大缩小倍数不超过5
			return 5;
		}
		return initialSize;
	}

	/**
	 * 根据分辨率大小返回inSampleSize
	 * 
	 * @param options
	 * @param reqWidth
	 * @param reqHeight
	 * @return
	 */
	public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;
		if (height >= reqHeight || width >= reqWidth) {
			int halfHeight = height / 2;
			int halfWidth = width / 2;
			inSampleSize = 2;
			// Calculate the largest inSampleSize value that is a power of 2 and
			// keeps both
			// height and width larger than the requested height and width.
			while (halfHeight >= reqHeight || halfWidth >= reqWidth) {
				inSampleSize += 1;
				halfHeight = halfHeight / 2;
				halfWidth = halfWidth / 2;
			}
		}
		return inSampleSize;
	}

	public static int computeInitialSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {
		double w = options.outWidth;
		double h = options.outHeight;
		int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math.sqrt(w * h / maxNumOfPixels));
		int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(Math.floor(w / minSideLength), Math.floor(h / minSideLength));
		if (upperBound < lowerBound) {
			// return the larger one when there is no overlapping zone.
			return lowerBound;
		}
		if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
			return 1;
		} else if (minSideLength == -1) {
			return lowerBound;
		} else {
			return upperBound;
		}
	}

	/**
	 * 读取图片属性：旋转的角度
	 * 
	 * @param path
	 *            图片绝对路径
	 * @return degree旋转的角度
	 */
	public static int readPictureDegree(String path) {
		int degree = 0;
		try {
			ExifInterface exifInterface = new ExifInterface(path);
			int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
			switch (orientation) {
			case ExifInterface.ORIENTATION_ROTATE_90:
				degree = 90;
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				degree = 180;
				break;
			case ExifInterface.ORIENTATION_ROTATE_270:
				degree = 270;
				break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return degree;
	}

//	/*
//	 * 旋转图片
//	 * 
//	 * @param angle
//	 * 
//	 * @param bitmap
//	 * 
//	 * @return Bitmap
//	 */
//	public static Bitmap rotaingImageView(int angle, Bitmap bitmap) {
//		// 旋转图片 动作
//		Matrix matrix = new Matrix();
//		;
//		matrix.postRotate(angle);
//		// 创建新的图片
//		Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
//		return resizedBitmap;
//	}
}
