/*******************************************************************
 * Company:     Fuzhou Rockchip Electronics Co., Ltd
 * Description:   
 * @author:     fxw@rock-chips.com
 * Create at:   2014年5月7日 下午2:45:32  
 * 
 * Modification History:  
 * Date         Author      Version     Description  
 * ------------------------------------------------------------------  
 * 2014年5月7日      fxw         1.0         create
 *******************************************************************/

package com.rockchip.devicetest.constants;

public class ParamConstants {

	public static final String ENABLED = "1";
	public static final String ACTIVATED = "activated";
	public static final String TEST_KEY = "test_key";
	public static final String TRUE = "true";

	public static final String WIFI_AP = "wifi_ap";
	public static final String WIFI_PSW = "password";
	public static final String WIFI_CONNECT = "connect";
	public static final String WIFI_DB_START = "db_start";
	public static final String WIFI_DB_END = "db_end";
	public static final String WIFI_NEED_CHECK_5G = "check_5g";
	
	/**
	 * usb的端口数，必须全部可写才为pass 或 其中一个不通过，弹出对话框，选择是否插入鼠标
	 */
	public static final String USB_PORT_COUNT = "usb_port_count";
	public static final String USB_MOUNT_PORT = "usb_mount_port";
	public static final String USB_MOUNT_DISK_SUFFIX = "usb_mount_disk_suffix";
	public static final String USB_NEED_CHECK_FLASH = "usb_need_check_flash";

	// HDMI
	public static final String VIDEO_TEST = "video_name";

	// LAN
	public static final String CONNECT_MODE = "connect_mode";
	public static final String STATIC_IP = "static_ip";
	public static final String GATEWAY = "gateway";
	public static final String NETMASK = "netmask";
	public static final String DNS1 = "dns1";
	public static final String DNS2 = "dns2";
	public static final String IS_SPEED_1000 = "is_speed_1000";

	// iperf
	public static final String SERVER_IP = "server_ip";
	public static final String SERVER_PORT = "server_port";

	//memory
	public static final String RAM_SIZE ="ram_size";
	public static final String FLASH_SIZE ="flash_size";

	//record
	public static final String RECORD_TIME_MAX ="record_time_max";

	// smb
	public static final String SMB_PATH = "smb_path";

	//AudioChannel
	public static final String AUDIO_CHANNEL_TEST_VOLUME_PERCENT = "audio_channel_test_volume_percent";
	
	//Spdif
	public static final String SPDIF_TEST_VOLUME_PERCENT = "spdif_test_volume_percent";
	
	// Serial port
	public static final String SERIAL_PORT_PATH = "serial_port_path";
	public static final String SERIAL_PORT_CMD1 = "serial_port_cmd1";
	public static final String SERIAL_PORT_CMD2 = "serial_port_cmd2";
	public static final String SERIAL_PORT_BAUDRATE = "serial_port_baudrate";
	
	//pcie
	public static final String READ_SPEED ="read_speed";
	public static final String WRITE_SPEED ="write_speed";
	public static final String PCIE_UUID ="pcie_uuid";
	public static final String FILE_SIZE ="file_size";
	
	//simcard
	public static final String SIGNAL_LEVEL ="signal_level";
	
	//Fan
	public static final String FAN_RUN_AD ="fan_run_ad";
	public static final String FAN_STOP_AD ="fan_stop_ad";
	
	
	//networktest
	public static final String NETWORK_TYPE ="network_type";
	public static final String NETWORK_TEST_IPERF ="network_test_iperf";	
	public static final String NETWORK_TEST_SMB ="network_test_smb";	
	public static final String NETWORK_TEST_PING ="network_test_ping";
	public static final String PACKAGE_SIZE ="package_size";
}
