package com.apicloud.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.util.Log;
import android.util.LogPrinter;
import android.widget.Toast;

/**
 * 网络流获取工具
 * 
 * @author Eilin.Yang
 * 
 */
public class HttpTools {
	private static final String TAG = "HttpTools";
	public static final int METHOD_GET = 1;
	public static final int METHOD_POST = 2;
	public static final String BASE_URL = "";

	/**
	 * 获取输入流
	 * 
	 * @param uri
	 *            网络地址
	 * @param params
	 *            请求参数
	 * @param method
	 *            请求方式
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static InputStream getStream(String uri,
			ArrayList<BasicNameValuePair> params, int method)
			throws ClientProtocolException, IOException {
		InputStream in = null;
		HttpEntity entity = getEntity(uri, params, method);
		if (entity != null)
			in = entity.getContent();
		Log.i(TAG,
				"getStream(String uri,ArrayList<BasicNameValuePair> params,int method)");
		return in;
	}

	/**
	 * 获取字节流
	 * 
	 * @param uri
	 *            请求地址
	 * @param params
	 *            请求参数
	 * @param method
	 *            请求方式
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static byte[] getBytes(String uri,
			ArrayList<BasicNameValuePair> params, int method)
			throws ClientProtocolException, IOException {
		byte[] bytes = null;
		HttpEntity entity = getEntity(uri, params, method);
		if (entity != null)
			bytes = EntityUtils.toByteArray(entity);
		Log.i(TAG,
				"getBytes(String uri,ArrayList<BasicNameValuePair> params,int method)");
		return bytes;
	}

	/**
	 * 获取返回的实体字符串
	 * 
	 * @param uri
	 *            请求地址
	 * @param params
	 *            请求参数
	 * @param method
	 *            请求方式
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static String toString(String uri,
			ArrayList<BasicNameValuePair> params, int method)
			throws ClientProtocolException, IOException {

		Log.i(TAG,
				"toString(String uri,ArrayList<BasicNameValuePair> params,int method)");
		HttpEntity entity = getEntity(uri, params, method);
		if (entity != null)
			return EntityUtils.toString(entity);
		return null;
	}

	/**
	 * 获取返回实体
	 * 
	 * @param uri
	 *            请求地址
	 * @param params
	 *            请求参数
	 * @param method
	 *            请求方式
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static HttpEntity getEntity(String uri,
			ArrayList<BasicNameValuePair> params, int method)
			throws ClientProtocolException, IOException {
		HttpEntity entity = null;
		DefaultHttpClient client = new DefaultHttpClient();
		client.getParams().setParameter(
				CoreConnectionPNames.CONNECTION_TIMEOUT, 3000);
		HttpUriRequest request = null;
		switch (method) {
		case METHOD_GET:
			StringBuilder sb = new StringBuilder(uri);
			if (params != null && !params.isEmpty()) {
				sb.append('?');
				for (BasicNameValuePair pair : params) {
					sb.append(pair.getName()).append('=')
							.append(pair.getValue()).append('&');
				}
				sb.deleteCharAt(sb.length() - 1);
			}
			request = new HttpGet(sb.toString());
			break;
		case METHOD_POST:
			request = new HttpPost(uri);
			if (params != null && !params.isEmpty()) {
				UrlEncodedFormEntity requestEntity = new UrlEncodedFormEntity(
						params);
				((HttpPost) request).setEntity(requestEntity);
			}
			break;
		}
		try {
			HttpResponse response = client.execute(request);
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				entity = response.getEntity();
			}
			Log.i(TAG,
					"getEntity(String uri,ArrayList<BasicNameValuePair> params,int method)");

		} catch (ClientProtocolException e) {
			Log.e("HttpConnectionUtil", e.getMessage(), e);
		} catch (InterruptedIOException e) {
			Log.e("http", "请求超时");
			// TODO: handle exception
		} catch (Exception e) {
			Log.e("HttpConnectionUtil", e.getMessage(), e);
		}

		return entity;
	}

	/**
	 * 获取返回实体长度
	 * 
	 * @param entity
	 *            需要获取长度的实体
	 * @return
	 */
	public static long getLength(HttpEntity entity) {
		Log.i(TAG, "getLength(HttpEntity entity)");
		if (entity != null) {
			return entity.getContentLength();
		}
		return -1;
	}

	/**
	 * 通过实体获取输入流
	 * 
	 * @param entity
	 * @return
	 * @throws IllegalStateException
	 * @throws IOException
	 */
	public static InputStream getStream(HttpEntity entity)
			throws IllegalStateException, IOException {
		Log.i(TAG, "getStream(HttpEntity entity)");
		if (entity != null) {
			return entity.getContent();
		}
		return null;
	}

	public static InputStream getStream(String url)
			throws IllegalStateException, IOException {
		Log.i(TAG, "getStream(HttpEntity entity)");
		if (url != null) {
			HttpEntity entity = getEntity(url, null, METHOD_GET);
			if (entity != null) {
				return entity.getContent();
			}
		}
		return null;
	}

	private HttpTools() {
	}

	
	public static String getHttpGetRequestString(String uri){
		String strResult = "";
		DefaultHttpClient client = new DefaultHttpClient();
		try {
			client.getParams().setParameter(
					CoreConnectionPNames.CONNECTION_TIMEOUT, 3000);
			// 相应超时
			client.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,
					15000);
			HttpGet get = new HttpGet(uri);
			// 获得HttpResponse对象
			HttpResponse httpResponse = client.execute(get);
			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				// 取得返回的数据
				strResult = EntityUtils.toString(httpResponse.getEntity());
				Log.i("strResult的值是"," "+strResult);
			}
		} catch (ConnectTimeoutException e) {
			// DialogUtils.showToast(this.activity, "连接超时", 2000);
		} catch (InterruptedIOException e) {
			// strResult = "连接超时";
			return strResult;
			// e.printStackTrace();
			// DialogUtils.showToast(this.activity, "响应超时", 2000);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return strResult;
	}
	 public static String convertStreamToString(InputStream is) {
		     Log.v("param", "isTo->"+ is.toString());
		   
		     if(new InputStreamReader(is)==null){
		    	 Log.v("param", "is is null");
		      }else{
		    	 Log.v("param", "is is not  null");
		     }
	        BufferedReader reader = new BufferedReader(new InputStreamReader(is));      
	        StringBuilder sb = new StringBuilder();      
	        //reader.
	        String line = null;      
	        try {      
	            while ((line = reader.readLine()) != null) {
	            	Log.v("param", "line->"+line.toString());
	                sb.append(line + "\n");      
	            }      
	        } catch (IOException e) {      
	            e.printStackTrace();      
	        } finally {      
	            try {      
	                is.close();      
	            } catch (IOException e) {      
	               e.printStackTrace();      
	            }      
	        }      
	        return sb.toString();      
	    }  
	  
	 
	/**
	 * get JSon data
	 * 
	 * @param params
	 *            the request property .NameValuePair
	 * 
	 * @param uri
	 *            the request API
	 * 
	 * @return JSon data or "".
	 */
	public static String getHttpRequestString(List<NameValuePair> params,
			String uri) {
		String strResult = "";
		DefaultHttpClient client = new DefaultHttpClient();
		try {
			client.getParams().setParameter(
					CoreConnectionPNames.CONNECTION_TIMEOUT, 2*60*1000);
			// 相应超时
			client.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,
					2*60*1000);
			HttpPost post = new HttpPost(uri);
			Log.v("param_充值", "url->"+uri+"");
			if(params!=null){
				 HttpEntity entity = new UrlEncodedFormEntity(params, "UTF-8");
			    post.setEntity(entity);
			}
		   
			
			
			// 获得HttpResponse对象
			HttpResponse httpResponse = client.execute(post);
			Log.v("param", "code->"+httpResponse.getStatusLine().getStatusCode()+"");
			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				// 取得返回的数据
				if(httpResponse.getEntity()==null){
					Log.v("param", "null");
				}else{
					Log.v("param", "not null");
					try {
						Log.v("param", httpResponse.getEntity().toString());
						strResult = EntityUtils.toString(httpResponse.getEntity());
						//strResult = convertStreamToString(httpResponse.getEntity().getContent());
						//return strResult;
						Log.v("param", strResult+"");
					} catch (Exception e) {
						Log.v("param", e.getMessage()+"");
					}
					
				}
				
			}
		} catch (IOException e) {
			Log.v("param", "error->"+e.getMessage()+"");
			e.printStackTrace();
		}
		return strResult;
	}

	/**
	 * get JSon data
	 * 
	 * @param params
	 *            the request property .NameValuePair
	 * 
	 * @param uri
	 *            the request API
	 * 
	 * @return JSon data or "".
	 */
	public static String getJsonString(List<NameValuePair> params, String uri) {
		String strResult = "";
		DefaultHttpClient client = new DefaultHttpClient();
		try {
			client.getParams().setParameter(
					CoreConnectionPNames.CONNECTION_TIMEOUT, 3000);
			// 相应超时
			client.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,
					5000);
			HttpPost post = new HttpPost(uri);
			post.addHeader("Accept-Encoding", "gzip");
			HttpEntity entity = new UrlEncodedFormEntity(params, "UTF-8");
			post.setEntity(entity);
			// 获得HttpResponse对象
			HttpResponse httpResponse = client.execute(post);
			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				// 取得返回的数据
				// strResult = EntityUtils.toString(httpResponse.getEntity());
				strResult = HttpTools.readHttpResponse(httpResponse);
			}
		} catch (ConnectTimeoutException e) {
			// DialogUtils.showToast(this.activity, "连接超时", 2000);
			return strResult;
		} catch (InterruptedIOException e) {
			return strResult;
			// DialogUtils.showToast(this.activity, "响应超时", 2000);
		} catch (IOException e) {
			e.printStackTrace();
			return strResult;
		}
		return strResult;
	}

	

	/**
	 * parse the HttpResponse compressed whit GZip
	 * 
	 * @param response
	 * @return
	 */
	public static String readHttpResponse(HttpResponse response) {
		String result = "";
		HttpEntity entity = response.getEntity();
		InputStream inputStream;
		try {
			inputStream = entity.getContent();
			ByteArrayOutputStream content = new ByteArrayOutputStream();

			Header header = response.getFirstHeader("Content-Encoding");
			if (header != null
					&& header.getValue().toLowerCase().indexOf("gzip") > -1) {
				inputStream = new GZIPInputStream(inputStream);
			}

			int readBytes = 0;
			byte[] sBuffer = new byte[512];
			while ((readBytes = inputStream.read(sBuffer)) != -1) {
				content.write(sBuffer, 0, readBytes);
			}
			result = new String(content.toByteArray());
			return result;
		} catch (IllegalStateException e) {
		} catch (IOException e) {
		}
		return result;
	}

}
