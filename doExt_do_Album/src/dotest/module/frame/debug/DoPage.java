package dotest.module.frame.debug;

import org.json.JSONObject;

import core.interfaces.DoIApp;
import core.interfaces.DoIPage;
import core.interfaces.DoIPageView;
import core.interfaces.DoIScriptEngine;
import core.object.DoMultitonModule;
import core.object.DoSourceFile;
import core.object.DoUIContainer;
import core.object.DoUIModule;

public class DoPage implements DoIPage {

	@Override
	public DoUIModule createUIModule(DoUIContainer arg0, JSONObject arg1)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean deleteMultitonModule(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public DoIApp getCurrentApp() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DoMultitonModule getMultitonModuleByAddress(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DoIPageView getPageView() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DoUIModule getRootView() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DoIScriptEngine getScriptEngine() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DoSourceFile getUIFile() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DoUIModule getUIModuleByAddress(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void loadRootUiContainer() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadScriptEngine(String arg0, String arg1) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeUIModule(DoUIModule arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setData(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public DoMultitonModule createMultitonModule(String _typeID, String _id) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setFullScreen(boolean isFullScreen) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isFullScreen() {
		// TODO Auto-generated method stub
		return false;
	}

}
