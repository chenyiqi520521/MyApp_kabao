/**   
 * Copyright © 2015 公司名. All rights reserved.
 * 
 * @Title: BankListActivity.java 
 * @Prject: APICloudSDK
 * @Package: com.apicloud.activity 
 * @Description: TODO
 * @author: user20   
 * @date: 2015年8月4日 上午11:05:55 
 * @version: V1.0   
 */
package com.apicloud.activity;

import java.util.ArrayList;

import com.apicloud.controller.Controller;
import com.apicloud.module.BankBean;
import com.uzmap.pkg.uzcore.UZResourcesIDFinder;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.ImageButton;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/** 
 * @ClassName: BankListActivity 
 * @Description: TODO
 * @author: user20
 * @date: 2015年8月4日 上午11:05:55  
 */
public class BankListActivity extends Activity  implements OnScrollListener{
	
	private ListView lv_bank;
	private BankListAdapter adapter;
	private int visibleLastIndex = 0;   //最后的可视项索引   
    private int visibleItemCount;       // 当前窗口可见项总数
    private int pageNum=1;//请求第几页
    private int requestEveryPageNum=30;
    public static  Handler handler;
    Controller controller;// 控制器
    private String chose_what;//判断是选择了请求主行还是支行
    private String parentCode="";
    private String branch="";
    ArrayList<BankBean>  data=null;
    private boolean needLoadMore=true;
    ImageButton ib_return;
    TextView tv_title;
    ProgressDialog pd;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		controller = new Controller(getApplicationContext());
		setContentView(UZResourcesIDFinder.getResLayoutID("layout_bank_list"));
		lv_bank=(ListView) findViewById(UZResourcesIDFinder.getResIdID("lv_bank"));
		ib_return=(ImageButton) findViewById(UZResourcesIDFinder.getResIdID("ib_return"));
		ib_return.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
				
			}
		});
		pd=new ProgressDialog(BankListActivity.this);
		pd.setMessage("加载数据");
		
		tv_title=(TextView) findViewById(UZResourcesIDFinder.getResIdID("tv_title"));
		initParam();
		initHandler();
		initAdapter();
		
		if(chose_what.equals(AddCradActivity.CHOSE_CHILDREN)){
			lv_bank.setOnScrollListener(this);
		}
		
		
	}
	void initParam(){
		if(this.getIntent()!=null){
			chose_what=this.getIntent().getStringExtra(AddCradActivity.CHOSEED_SEARCH);
			if(chose_what.equals(AddCradActivity.CHOSE_CHILDREN)){
				parentCode=this.getIntent().getStringExtra(AddCradActivity.PARENT_BANK_CODE);
				branch=this.getIntent().getStringExtra(AddCradActivity.BRANCH);
				tv_title.setText("银行网点选择");
			}
		}
	}
	void initAdapter(){
		if(pd!=null){
			pd.show();
		}
		new Thread(){
			public void run() {
				if(chose_what.equals(AddCradActivity.CHOSE_CHILDREN)){
					data=controller.getChilrenBankList(BankListActivity.this, parentCode, branch, pageNum+"");
					pageNum++;
				}else{
					 data=controller.getParetBankList(BankListActivity.this);
				}
				if(data!=null&&data.size()>0){
					if(data.size()<20){
						needLoadMore=false;
					}
					handler.post(new Runnable(){

						@Override
						public void run() {
							adapter=new BankListAdapter(BankListActivity.this, data,chose_what);
							if(adapter!=null){
								lv_bank.setAdapter(adapter);
							}
							if(pd!=null){
								pd.dismiss();
							}
							
						}
						
					});
				}else{
					
					handler.post(new Runnable(){

						@Override
						public void run() {
							Toast.makeText(BankListActivity.this,"暂无数据和该搜索匹配，请更换搜索关键词",Toast.LENGTH_SHORT).show();
							if(pd!=null){
								pd.dismiss();
							}
							
						}
						
					});
				}
				
			};
			
		}.start();
	    
	}
    void initHandler(){
    	handler=new Handler(){
    		@Override
    		public void handleMessage(Message msg) {
    			// TODO Auto-generated method stub
    			super.handleMessage(msg);
    		}
    		
    	};
    }
	void loadMore(){
		if(pd!=null){
			pd.setMessage("加载更多");
			pd.show();
		}
		new Thread(){
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				super.run();
				final ArrayList<BankBean> moreData=getMoreData();
			   
				if(moreData!=null){
					handler.post(new Runnable(){
						
						@Override
						public void run() {
							
							if(adapter!=null){
								if(pd!=null){
									pd.dismiss();
								}
								adapter.AddData(moreData);
								
								lv_bank.setSelection(visibleLastIndex - visibleItemCount + 1); //设置选中项 
							}
							
							
						}
					});
				}
				
			}
		}.start();
	}
	ArrayList<BankBean> getMoreData(){
		final ArrayList<BankBean> moreData=controller.getChilrenBankList(BankListActivity.this, parentCode, branch, pageNum+"");
		pageNum++;
		return moreData;
	}
	/* (non Javadoc) 
	 * @Title: onScroll
	 * @Description: TODO
	 * @param view
	 * @param firstVisibleItem
	 * @param visibleItemCount
	 * @param totalItemCount 
	 * @see android.widget.AbsListView.OnScrollListener#onScroll(android.widget.AbsListView, int, int, int) 
	 */
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		  this.visibleItemCount = visibleItemCount;  
	      visibleLastIndex = firstVisibleItem + visibleItemCount - 1;
		
	}

	/* (non Javadoc) 
	 * @Title: onScrollStateChanged
	 * @Description: TODO
	 * @param view
	 * @param scrollState 
	 * @see android.widget.AbsListView.OnScrollListener#onScrollStateChanged(android.widget.AbsListView, int) 
	 */
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
        int lastIndex = adapter.getCount() - 1;    //数据集最后一项的索引 
        if (scrollState == OnScrollListener.SCROLL_STATE_IDLE && visibleLastIndex == lastIndex&&needLoadMore) { 
        	loadMore();
        	//Toast.makeText(BankListActivity.this, "加载更多",Toast.LENGTH_SHORT).show();
        }
	}

}
