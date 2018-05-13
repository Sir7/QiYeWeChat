package com.alphabet.wechat.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

import com.alphabet.authsystem.entity.UserEntity;
import com.alphabet.common.ErpCommon;
import com.alphabet.wechat.service.QiyeWechatOrgUserSynService;

/**
 * 下载在职员工为上传头像名称
 * @author yang.lvsen
 * @date 2018年1月18日 上午10:28:43
 */
public class DownloadNoImage {
	
	public static void downloadNoImageData(){
		List<UserEntity> userList = QiyeWechatOrgUserSynService.getToAddUsers();	//只下载在职员工的头像
		for(UserEntity ue : userList){
			if(ErpCommon.isNull(ue.getUrlPath()) || "default.jpg".equals(ue.getUrlPath())){
				BufferedWriter out = null;
				try {
					File file =new File("M:\\aaa.txt");	//下载保存位置
				    if(!file.exists()){
				       file.createNewFile();
				    }
					out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true)));	//追加写入
					out.write(ue.getOrgName()+"-"+ue.getUserName()+"\r\n");
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					try {
						out.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		System.out.println("-----写入结束-----");
    }

}
