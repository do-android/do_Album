package doext.implement;

import java.util.ArrayList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import core.DoServiceContainer;
import core.helper.DoIOHelper;
import doext.choosephotos.AlbumHelper;

public class do_Album_SaveVideo_AsyncTask extends AsyncTask<Void, Void, ArrayList<String>> {

	private ProgressDialog mpDialog;
	private Activity activity;
	private String tagerDir;
	private AlbumHelper helper;

	public do_Album_SaveVideo_AsyncTask(Activity activity, Intent intent) {
		this.activity = activity;
		this.tagerDir = intent.getStringExtra("tagerDir");
		helper = AlbumHelper.getHelper();
		helper.init(DoServiceContainer.getPageViewFactory().getAppContext());
	}

	protected void onPreExecute() {
		mpDialog = new ProgressDialog(activity);
		mpDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);// 设置风格为圆形进度条
		mpDialog.setMessage("正在保存视频...");
		mpDialog.setIndeterminate(false);// 设置进度条是否为不明确
		mpDialog.setCancelable(false);// 设置进度条是否可以按退回键取消
		mpDialog.setCanceledOnTouchOutside(false);
		mpDialog.show();
	};

	@Override
	protected ArrayList<String> doInBackground(Void... params) {
		ArrayList<String> urlList = new ArrayList<String>();
		try {
			for (int i = 0; i < helper.video_list.size(); i++) {
				String videoPath = helper.video_list.get(i);
				String _fileName = videoPath.substring(videoPath.lastIndexOf("/") + 1, videoPath.length());
				String _fileFullName = tagerDir + "/temp/do_Album/";
				DoIOHelper.copyFileOrDirectory(videoPath, _fileFullName);
				urlList.add("data://temp/do_Album/" + _fileName);
			}
			//保存过之后清空之前保存的视频路径
			helper.video_list.clear();
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
}
