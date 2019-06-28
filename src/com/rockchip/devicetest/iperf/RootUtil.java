package com.rockchip.devicetest.iperf;

import java.io.File;
import android.content.Context;
import android.util.Log;

public class RootUtil {

	private static final String TAG = "RootUtil";

	private static final String SU_PATH = "/system/xbin/su";
	private static final String ROOT_SU_NAME = "tchipsu";
	private static final String ROOT_SU_PATH = "/system/xbin/tchipsu";
	private static final String DEFAULT_SU_NAME = "defaultsu";
	private static final String DEFAULT_SU_PATH = "/system/xbin/defaultsu";

	public static boolean getRoot(Context ctx) {
		final StringBuilder res = new StringBuilder();
		String cmd = "mount -o remount,rw /system \n";
		// cmd = cmd+"cp "+ ROOT_SU_PATH +" "+"/system/xbin/su  \n";
		// cmd = cmd+"chmod 06755 "+ SU_PATH +"\n";
		// cmd = cmd+"mount -o remount,ro /system \n";

		Log.v(TAG, "getRoot cmd =" + cmd);

		try {
			if (runScriptAsRoot(ctx, cmd, res, "su") == 0)
				return true;
		} catch (Exception e) {
		}
		return false;
	}

	/*
	 * public static boolean getRoot() { Process process = null;
	 * DataOutputStream dos = null; try { process =
	 * Runtime.getRuntime().exec("tchipsu"); dos = new
	 * DataOutputStream(process.getOutputStream()); dos.flush();
	 * 
	 * 
	 * dos.writeBytes("cp /system/xbin/tchipsu /system/xbin/xxxsu \n");
	 * dos.writeBytes("exit " + "\n"); dos.flush(); try { process.waitFor(); }
	 * catch (InterruptedException e) { e.printStackTrace(); } int exitValue =
	 * process.exitValue(); try { if (exitValue == 0) {
	 * 
	 * return true; } else {
	 * 
	 * return false; } } catch (Exception e) { e.printStackTrace(); }
	 * 
	 * } catch (IOException e) { e.printStackTrace(); } finally {
	 * 
	 * if (dos != null) { try { dos.close(); } catch (IOException e) {
	 * e.printStackTrace(); } } if (process != null) { process.destroy(); } }
	 * return false;
	 * 
	 * }
	 */
	public static boolean restoreRoot(Context ctx) {
		final StringBuilder res = new StringBuilder();
		String cmd = "mount -o remount,rw /system \n";
		cmd = cmd + "cp " + DEFAULT_SU_PATH + " " + "/system/xbin/su  \n";
		cmd = cmd + "chmod 06755 " + SU_PATH + "\n";
		cmd = cmd + "mount -o remount,ro /system \n";
		Log.v(TAG, "getRoot cmd =" + cmd);
		try {
			if (runScriptAsRoot(ctx, cmd, res, ROOT_SU_NAME) == 0)
				return true;
		} catch (Exception e) {
		}
		return false;
	}

	public static boolean hasRootAccess(Context ctx) {
		final StringBuilder res = new StringBuilder();
		try {
			if (runScriptAsRoot(ctx, "exit 0", res) == 0)
				return true;
		} catch (Exception e) {
		}
		return false;
	}

	public static boolean iperfTest(Context ctx) {
		final StringBuilder res = new StringBuilder();
		String cmd = "system/bin/iperf -c 168.168.100.19 -i 1 -w 1M \n";
		try {
			if (runScriptAsRoot(ctx, cmd, res) == 0)
				return true;
		} catch (Exception e) {
		}
		return false;
	}

	public static int runScriptAsRoot(Context ctx, String script,
			StringBuilder res) {
		final File file = new File(ctx.getCacheDir(), "secopt.sh");
		final ScriptRunner runner = new ScriptRunner(file, script, res);
		runner.start();
		try {
			runner.join(40000);
			if (runner.isAlive()) {
				runner.interrupt();
				runner.join(150);
				runner.destroy();
				runner.join(50);
			}
		} catch (InterruptedException ex) {
		}
		return runner.exitcode;
	}

	public static int runScriptAsRoot(Context ctx, String script,
			StringBuilder res, String suname) {
		final File file = new File(ctx.getCacheDir(), "secopt.sh");
		Log.v(TAG, file.getAbsolutePath());
		final ScriptRunner runner = new ScriptRunner(file, script, res, suname);
		runner.start();
		try {
			runner.join(40000);
			if (runner.isAlive()) {
				runner.interrupt();
				runner.join(150);
				runner.destroy();
				runner.join(50);
			}
		} catch (InterruptedException ex) {
		}
		return runner.exitcode;
	}

}
