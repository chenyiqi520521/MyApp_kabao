package com.apicloud.controller;

import java.io.File;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import com.apicloud.common.Image;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class HttpPostFile {

	/**
	 * @param context
	 */
	public HttpPostFile(Context context) {
		super();

		// TODO Auto-generated constructor stub
	}

	/**
	 * 
	 * @param f上传的图片文件
	 * @return
	 */
	public boolean postFile(File file,String cradid,String rechono,String url,Handler hanlder) {
		if (file == null) {
			return false;
		}
		HttpClient client = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(url);
		HttpResponse response = null;
		try {
			Message msg1=hanlder.obtainMessage();
			msg1.obj="发起图片上传请求";
			hanlder.sendMessage(msg1);
			MultipartEntity mpEntity = new MultipartEntity();
			long l = file.length();
			Log.i("tag", l + "");
			ContentBody cbFile1 = new FileBody(file, "image/png");		
			ContentBody cbuserid = new StringBody(cradid);
			ContentBody cbrechono=new StringBody(rechono);
			mpEntity.addPart("image", cbFile1);// 上传文件
			mpEntity.addPart("lkey", cbuserid);// 上传用户名
			mpEntity.addPart("rechno", cbrechono);//上传流水号
			httpPost.setEntity(mpEntity);
			
			response = client.execute(httpPost);
			Message msg2=hanlder.obtainMessage();
			msg2.obj="服务器返回1"+response;
			
			hanlder.sendMessage(msg2);
			if (response != null&& response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				String json = EntityUtils.toString(response.getEntity(),"UTF-8");
				response = client.execute(httpPost);
				
				Message msg3=hanlder.obtainMessage();
				msg3.obj="服务器返回2"+json;
				hanlder.sendMessage(msg3);
				
				JSONObject jObject = new JSONObject(json);
				if (jObject != null) {
					if (jObject.has("error")) {
						int value = jObject.getInt("error");
						if (value == 0) {
							return true;
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return false;
	}
	/**
	 * 
	 * @param f上传的图片文件
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public boolean postFiles(List<Image> file,String cradid,String url) {
		if (file == null) {
			return false;
		};
		HttpClient client = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(url);
		HttpResponse response = null;
		try {
			File file1=new File(file.get(0).image1);
			File file2=new File(file.get(0).image2);
			File file3=new File(file.get(0).image3);
			File file4=new File(file.get(0).image4);
			MultipartEntity mpEntity = new MultipartEntity();
			ContentBody cbFile1 = new FileBody(file1, "image/png");
			ContentBody cbFile2 = new FileBody(file2, "image/png");
			ContentBody cbFile3 = new FileBody(file3, "image/png");
			ContentBody cbFile4 = new FileBody(file4, "image/png");
			ContentBody cbuserid = new StringBody(cradid);
			mpEntity.addPart("image1", cbFile1);// 上传文件
			mpEntity.addPart("image2",cbFile2);
			mpEntity.addPart("image3",cbFile3);
			mpEntity.addPart("image4",cbFile4);
			mpEntity.addPart("cardid", cbuserid);// 上传用户名
			httpPost.setEntity(mpEntity);
			response = client.execute(httpPost);
			if (response != null&& response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				String json = EntityUtils.toString(response.getEntity(),"UTF-8");
				JSONObject jObject = new JSONObject(json);
				if (jObject != null) {
					if (jObject.has("error")) {
						int value = jObject.getInt("error");
						if (value == 0) {
							return true;
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return false;
	}
}
