/*******************************************************************
* Company:     Fuzhou Rockchip Electronics Co., Ltd
* Description:   
* @author:     fxw@rock-chips.com
* Create at:   2014年5月8日 下午3:24:40  
* 
* Modification History:  
* Date         Author      Version     Description  
* ------------------------------------------------------------------  
* 2014年5月8日      fxw         1.0         create
*******************************************************************/   

package com.rockchip.devicetest.enumerate;

import com.rockchip.devicetest.R;
import com.rockchip.devicetest.constants.TypeConstants;


public enum Commands {

	CMD_WIFI(TypeConstants.CMD_WIFI, R.string.cmd_wifi),
	CMD_LAN(TypeConstants.CMD_LAN, R.string.cmd_lan),
	CMD_SD(TypeConstants.CMD_SD, R.string.cmd_sd),
	CMD_HDMI(TypeConstants.CMD_HDMI, R.string.cmd_hdmi),
	CMD_CVBS(TypeConstants.CMD_CVBS, R.string.cmd_cvbs),
	CMD_CHNL(TypeConstants.CMD_CHNL, R.string.cmd_chnl),
	CMD_LED(TypeConstants.CMD_LED, R.string.cmd_led),
	CMD_KEY(TypeConstants.CMD_KEY, R.string.cmd_key),
	CMD_REST(TypeConstants.CMD_REST, R.string.cmd_rest),
	CMD_USB(TypeConstants.CMD_USB, R.string.cmd_usb),
	CMD_MIC(TypeConstants.CMD_MIC, R.string.cmd_mic),
	CMD_SSD(TypeConstants.CMD_SSD, R.string.cmd_ssd),
	
	CMD_BLUETOOTH(TypeConstants.CMD_BLUETOOTH,R.string.cmd_bluetooth),
	CMD_HDMI_IN(TypeConstants.CMD_HDMI_IN,R.string.cmd_hdmi_in),
	CMD_PPPoE(TypeConstants.CMD_PPPoE,R.string.cmd_pppoe),
	//CMD_RDSN(TypeConstants.CMD_RDSN, 0),
	CMD_CKSN(TypeConstants.CMD_CKSN, 0/*R.string.cmd_cksn*/),
	CMD_TEST(TypeConstants.CMD_TEST, 0),
	CMD_BEAT(TypeConstants.CMD_BEAT, 0),
	CMD_VGA(TypeConstants.CMD_VGA,R.string.cmd_vga),
	CMD_IPERF(TypeConstants.CMD_IPERF,R.string.cmd_iperf),
	CMD_SPDIF(TypeConstants.CMD_SPDIF, R.string.cmd_spdif),
	CMD_MEMORY(TypeConstants.CMD_MEMORY, R.string.cmd_memory),
	CMD_RECORD(TypeConstants.CMD_RECORD, R.string.cmd_record),
	CMD_CAMERA(TypeConstants.CMD_CAMERA, R.string.cmd_camera),
	CMD_UVC(TypeConstants.CMD_UVC, R.string.cmd_uvc),
	CMD_SMB(TypeConstants.CMD_SMB, R.string.cmd_smb),
	CMD_LINE_IN(TypeConstants.CMD_LINE_IN, R.string.cmd_line_in),
	CMD_SERIAL_PORT(TypeConstants.CMD_SERIAL_PORT,R.string.cmd_serial_port),
	CMD_HDMI_EDID(TypeConstants.CMD_HDMI_EDID, R.string.cmd_hdmi_edid),
	CMD_TIME(TypeConstants.CMD_TIME,R.string.cmd_time),
	CMD_DEVICE_CHK(TypeConstants.CMD_DEVICE_CHK,R.string.cmd_device_chk),
	CMD_WIFISMB(TypeConstants.CMD_WIFISMB, R.string.cmd_wifi_smb),
	CMD_PCIE(TypeConstants.CMD_PCIE, R.string.cmd_pcie),
	CMD_SIMCARD(TypeConstants.CMD_SIMCARD, R.string.cmd_simcard),
	CMD_FAN(TypeConstants.CMD_FAN, R.string.cmd_fan),
	CMD_TYPEC(TypeConstants.CMD_TYPEC, R.string.cmd_typec),
	CMD_NETWORK_LAN(TypeConstants.CMD_NETWORK_LAN, R.string.cmd_lan),
	CMD_NETWORK_WIFI(TypeConstants.CMD_NETWORK_WIFI, R.string.cmd_wifi),
	CMD_NETWORK_WIFI_5G(TypeConstants.CMD_NETWORK_WIFI_5G, R.string.cmd_wifi_5g),
	CMD_SATA(TypeConstants.CMD_SATA, R.string.sata_title),
	CMD_DP(TypeConstants.CMD_DP, R.string.dp_title);
	private String command;
	private int resID;
	
	private Commands(String command, int resID){
		this.command = command;
		this.resID = resID;
	}
	
	public String getCommand() {
		return command;
	}

	public int getResID() {
		return resID;
	}
	//忽略大小写
	public static Commands getType(String cmdstr){
		if(cmdstr==null) return null;
		for(Commands cmd : Commands.values()){
			if(cmdstr.equalsIgnoreCase(cmd.getCommand().trim())){
				return cmd;
			}
		}
		return null;
	}
}
