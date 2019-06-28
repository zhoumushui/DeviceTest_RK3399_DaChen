package com.rockchip.devicetest.testcase;

import java.io.File;

import android.util.Log;

import com.rockchip.devicetest.utils.FileUtils;
import com.rockchip.devicetest.utils.LogUtil;

public class LEDSettings {

	public enum LEDMode {
		ON("3"), OFF("0");

		public String value;

		private LEDMode(String value) {
			this.value = value;
		}

		public static LEDMode getMode(String mode) {
			for (LEDMode um : LEDMode.values()) {
				if (um.value.equals(mode)) {
					return um;
				}
			}
			return null;
		}
	}

	private static final String SYS_LED_FILE = "/sys/class/led_gpio/net_led";
	/*增加对firefly开发板独有的led测试支持！*/
	//private static final String FIREFLY_LED_FILE = "/proc/driver/firefly-leds";
	private static final String FIREFLY_LED_FILE = "/sys/class/leds/firefly:yellow:user/brightness";//"/sys/class/leds/firefly:yellow:user/trigger";
	

	
	/**
	 * 获得当前灯的状态
	 * 
	 * public static LEDMode getCurrentLedMode(){ File file = new
	 * File(SYS_LED_FILE); String mode = FileUtils.readFromFile(file);
	 * System.out.println("=============="+mode);
	 * 
	 * return LEDMode.getMode(mode); }
	 */

	/**
	 * 开灯
	 */
	public static boolean onLed() {
		setFireflyLedMode(FIREFLY_LED_ON);
		return setLedMode(LEDMode.ON);
	}

	/**
	 * 关灯
	 */
	public static boolean offLed() {
		setFireflyLedMode(FIREFLY_LED_OFF);
		return setLedMode(LEDMode.OFF);
	}

	/**
	 * 修改LED状态
	 */
	public static boolean setLedMode(LEDMode mode) {
		File file = new File(SYS_LED_FILE);
		return FileUtils.write2File(file, mode.value);
	}
	
	private static final String FIREFLY_LED_ON = "1";
	private static final String FIREFLY_LED_OFF = "0";
	/**
	 * 修改firefly LED状态
	 */
	public static boolean setFireflyLedMode(String mode) {
		File file = new File(FIREFLY_LED_FILE);
		return FileUtils.write2File(file, mode);
		
	}
}
