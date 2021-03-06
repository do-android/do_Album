package doext.implement;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.text.TextUtils;
import core.DoServiceContainer;
import core.helper.DoIOHelper;
import doext.choosephotos.AlbumHelper;
import doext.choosephotos.BitmapUtils;

public class do_Album_SaveImage_AsyncTask extends AsyncTask<Void, Void, ArrayList<String>> {

	private ProgressDialog mpDialog;
	private Activity activity;
	private int width;
	private int height;
	private int quality;
	private String tagerDir;
	private String originalImage;
	private AlbumHelper helper;

	public do_Album_SaveImage_AsyncTask(Activity activity, Intent intent) {
		this.activity = activity;
		this.width = intent.getIntExtra("width", 0);
		this.height = intent.getIntExtra("height", 0);
		this.quality = intent.getIntExtra("quality", 0);
		this.quality = this.quality > 100 ? 100 : this.quality;
		this.quality = this.quality < 1 ? 1 : this.quality;
		this.tagerDir = intent.getStringExtra("tagerDir");
		// 原始图片路径 如果是截图 的话 会把这个数据传过来 传的是相册里面原始图片的路径
		this.originalImage = intent.getStringExtra("originalImage");
		helper = AlbumHelper.getHelper();
		helper.init(DoServiceContainer.getPageViewFactory().getAppContext());
	}

	protected void onPreExecute() {
		mpDialog = new ProgressDialog(activity);
		mpDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);// 设置风格为圆形进度条
		mpDialog.setMessage("正在保存图片...");
		mpDialog.setIndeterminate(false);// 设置进度条是否为不明确
		mpDialog.setCancelable(false);// 设置进度条是否可以按退回键取消
		mpDialog.setCanceledOnTouchOutside(false);
		mpDialog.show();
	};

	@Override
	protected ArrayList<String> doInBackground(Void... params) {
		ArrayList<String> urlList = new ArrayList<String>();
		try {
			for (int i = 0; i < BitmapUtils.selectPaths.size(); i++) {
				String imagePath = BitmapUtils.selectPaths.get(i);

				// 根据时间措及序号产生文件名
				String _fileName = getTimestampStr() + "_" + i + ".png.do";
				String _fileFullName = tagerDir + "/temp/do_Album/" + _fileName;

				ByteArrayOutputStream photo_data = new ByteArrayOutputStream();
				Bitmap bitmap = BitmapUtils.resizeRealImage(imagePath, width, height);
				bitmap.compress(Bitmap.CompressFormat.JPEG, quality, photo_data);
				if (bitmap != null && !bitmap.isRecycled()) {
					bitmap.recycle();
					bitmap = null;
				}
				DoIOHelper.writeAllBytes(_fileFullName, photo_data.toByteArray());
				String uri = "data://temp/do_Album/" + _fileName;
				urlList.add(uri);
				if (ConstantValue.ISCUT && imagePath.contains("do_Album")) {
					// 删除临时文件
					new File(imagePath).delete();
				}
				//如果originalImage有值 则代表 是截图跳转过来的页面
				if (TextUtils.isEmpty(originalImage))
					helper.saveExif(imagePath, _fileFullName);
				else
					helper.saveExif(originalImage, _fileFullName);
			}

		} catch (Exception _err) {
			_err.printStackTrace();
		}
		return urlList;
	}

	protected void onPostExecute(ArrayList<String> result) {
		if (mpDialog != null && mpDialog.isShowing()) {
			mpDialog.dismiss();
		}
		Intent intent = new Intent();
		intent.putStringArrayListExtra("result", result);
		activity.setResult(Activity.RESULT_OK, intent);
		activity.finish();
	};

	/**
	 * @return 类似于字符串 ：20140324043154806
	 */
	@SuppressLint("SimpleDateFormat")
	private String getTimestampStr() {
		java.text.DateFormat format = new java.text.SimpleDateFormat("yyyyMMddHHmmssS");
		return format.format(new Date());
	}
}
