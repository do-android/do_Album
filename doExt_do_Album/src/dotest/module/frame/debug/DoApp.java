package dotest.module.frame.debug;

import core.interfaces.DoIApp;
import core.interfaces.DoIConfig;
import core.interfaces.DoIDataFS;
import core.interfaces.DoIScriptEngine;
import core.interfaces.DoISourceFS;
import core.object.DoMultitonModule;

public class DoApp implements DoIApp {

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
	public String getAppID() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DoIConfig getConfig() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DoIConfig getConfig(String arg0) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DoIDataFS getDataFS() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DoMultitonModule getMultitonModuleByAddress(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DoIScriptEngine getScriptEngine() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DoISourceFS getSourceFS() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DoMultitonModule createMultitonModule(String _typeID, String _id) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
