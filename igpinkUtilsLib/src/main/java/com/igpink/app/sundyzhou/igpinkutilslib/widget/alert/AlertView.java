package com.igpink.app.sundyzhou.igpinkutilslib.widget.alert;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.igpink.app.sundyzhou.igpinkutilslib.R;
import com.igpink.app.sundyzhou.igpinkutilslib.core.App;
import com.igpink.app.sundyzhou.igpinkutilslib.core.BasicUtil;
import com.igpink.app.sundyzhou.igpinkutilslib.core.Port;
import com.sina.weibo.sdk.api.TextObject;
import com.sina.weibo.sdk.api.WeiboMessage;
import com.sina.weibo.sdk.api.share.IWeiboShareAPI;
import com.sina.weibo.sdk.api.share.SendMessageToWeiboRequest;
import com.sina.weibo.sdk.api.share.WeiboShareSDK;
import com.tencent.connect.share.QQShare;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Sai on 15/8/9.
 * 精仿iOSAlertViewController控件
 * 点击取消按钮返回 －1，其他按钮从0开始算
 */
public class AlertView implements IWXAPIEventHandler {
    public static final int TYPE_APP = 0x008;


    public static final int SHARE_WE_CHAT = 102;
    public static final int SHARE_CIRCLE = 103;
    public static final int SHARE_QQ = 104;
    public static final int SHARE_WEI_BO = 105;

    /*public static ShareFragment instance;*/

    int type;

    //分享Url
    String url = "";

    String userName = "";
    String shareImagePath = "";

    private RelativeLayout body = null;

    //WeChat
    private IWXAPI iwxapi;
    SendMessageToWX.Req req;


    //WeiBo
    /** 微博分享的接口实例 */
    private IWeiboShareAPI weiboShareAPI;

    //Tencent
    Tencent tencent;

    String wxImageUrl = "";


    String appShareLink = "http://yun.igpink.com";


    @Override
    public void onReq(BaseReq baseReq) {

    }

    @Override
    public void onResp(BaseResp baseResp) {
        switch (baseResp.errCode) {
            case BaseResp.ErrCode.ERR_OK:
                //分享成功
                Toast.makeText(context, R.string.share_success, Toast.LENGTH_SHORT).show();
                break;
            case BaseResp.ErrCode.ERR_USER_CANCEL:
                //分享取消
                Toast.makeText(context, R.string.share_cancel, Toast.LENGTH_SHORT).show();
                break;
            case BaseResp.ErrCode.ERR_AUTH_DENIED:
                //分享拒绝
                Toast.makeText(context, R.string.share_failure, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    public static enum Style{
        ActionShare,
        ActionSheet,
        Alert
    }
    private final FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM
    );
    public static final int HORIZONTAL_BUTTONS_MAXCOUNT = 2;
    public static final String OTHERS = "others";
    public static final String DESTRUCTIVE = "destructive";
    public static final String CANCEL = "cancel";
    public static final String TITLE = "title";
    public static final String MSG = "msg";
    public static final int CANCELPOSITION = -1;//点击取消按钮返回 －1，其他按钮从0开始算

    private String title;
    private String msg;
    private List<String> mDestructive;
    private List<String> mOthers;
    private String cancel;
    private ArrayList<String> mDatas = new ArrayList<String>();

    private Context context;
    private ViewGroup contentContainer;
    private ViewGroup decorView;//activity的根View
    private ViewGroup rootView;//AlertView 的 根View
    private ViewGroup loAlertHeader;//窗口headerView

    private Style style = Style.Alert;

    private OnDismissListener onDismissListener;
    private OnItemClickListener onItemClickListener;
    private boolean isDismissing;

    private Animation outAnim;
    private Animation inAnim;
    private int gravity = Gravity.CENTER;

    public AlertView(String title, String msg, String cancel, String[] destructive, String[] others, Context context, Style style,OnItemClickListener onItemClickListener, int type, Bundle arguments, Activity activity){
        this.context = context;
        if(style != null)this.style = style;
        this.onItemClickListener = onItemClickListener;
        setType(type);
        setArguments(arguments);
        setActivity(activity);

        initData(title, msg, cancel, destructive, others);
        initViews();
        init();
        initEvents();
    }

    public void setType(int type) {
        this.type = type;
    }

    /**
     * 获取数据
     */
    protected void initData(String title, String msg, String cancel, String[] destructive, String[] others) {

        this.title = title;
        this.msg = msg;
        if (destructive != null){
            this.mDestructive = Arrays.asList(destructive);
            this.mDatas.addAll(mDestructive);
        }
        if (others != null){
            this.mOthers = Arrays.asList(others);
            this.mDatas.addAll(mOthers);
        }
        if (cancel != null){
            this.cancel = cancel;
            if(style == Style.Alert && mDatas.size() < HORIZONTAL_BUTTONS_MAXCOUNT){
                this.mDatas.add(0,cancel);
            }
        }

    }
    protected void initViews(){
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        decorView = (ViewGroup) ((Activity)context).getWindow().getDecorView().findViewById(android.R.id.content);
        rootView = (ViewGroup) layoutInflater.inflate(R.layout.layout_alertview, decorView, false);
        rootView.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        ));
        contentContainer = (ViewGroup) rootView.findViewById(R.id.content_container);
        int margin_alert_left_right = 0;
        switch (style){
            case ActionShare:
                params.gravity = Gravity.BOTTOM;
                margin_alert_left_right = context.getResources().getDimensionPixelSize(R.dimen.margin_actionsheet_left_right);
                params.setMargins(margin_alert_left_right,0,margin_alert_left_right,margin_alert_left_right);
                contentContainer.setLayoutParams(params);
                gravity = Gravity.BOTTOM;
                initActionShareViews(layoutInflater);
                break;
            case ActionSheet:
                params.gravity = Gravity.BOTTOM;
                margin_alert_left_right = context.getResources().getDimensionPixelSize(R.dimen.margin_actionsheet_left_right);
                params.setMargins(margin_alert_left_right,0,margin_alert_left_right,margin_alert_left_right);
                contentContainer.setLayoutParams(params);
                gravity = Gravity.BOTTOM;
                initActionSheetViews(layoutInflater);
                break;
            case Alert:
                params.gravity = Gravity.CENTER;
                margin_alert_left_right = context.getResources().getDimensionPixelSize(R.dimen.margin_alert_left_right);
                params.setMargins(margin_alert_left_right,0,margin_alert_left_right,0);
                contentContainer.setLayoutParams(params);
                gravity = Gravity.CENTER;
                initAlertViews(layoutInflater);
                break;
        }
    }

    protected void initHeaderView(ViewGroup viewGroup){
        loAlertHeader = (ViewGroup) viewGroup.findViewById(R.id.loAlertHeader);
        //标题和消息
        TextView tvAlertTitle = (TextView) viewGroup.findViewById(R.id.tvAlertTitle);
        TextView tvAlertMsg = (TextView) viewGroup.findViewById(R.id.tvAlertMsg);
        if(title != null) {
            tvAlertTitle.setText(title);
        }else{
            tvAlertTitle.setVisibility(View.GONE);
        }
        if(msg != null) {
            tvAlertMsg.setText(msg);
        }else{
            tvAlertMsg.setVisibility(View.GONE);
        }
    }
    protected void initListView(){
        ListView alertButtonListView = (ListView) contentContainer.findViewById(R.id.alertButtonListView);
        //把cancel作为footerView
        if(cancel != null && style == Style.Alert){
            View itemView = LayoutInflater.from(context).inflate(R.layout.item_alertbutton, null);
            TextView tvAlert = (TextView) itemView.findViewById(R.id.tvAlert);
            tvAlert.setText(cancel);
            tvAlert.setClickable(true);
            tvAlert.setTypeface(Typeface.DEFAULT_BOLD);
            tvAlert.setTextColor(context.getResources().getColor(R.color.textColor_alert_button_cancel));
            tvAlert.setBackgroundResource(R.drawable.bg_alertbutton_bottom);
            tvAlert.setOnClickListener(new OnTextClickListener(CANCELPOSITION));
            alertButtonListView.addFooterView(itemView);
        }
        AlertViewAdapter adapter = new AlertViewAdapter(mDatas,mDestructive);
        alertButtonListView.setAdapter(adapter);
        alertButtonListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                if(onItemClickListener != null)onItemClickListener.onItemClick(AlertView.this,position);
                dismiss();
            }
        });
    }


    protected void initShareHeaderView(ViewGroup viewGroup){
        loAlertHeader = (ViewGroup) viewGroup.findViewById(R.id.loAlertHeader);
        //标题和消息
        TextView tvAlertTitle = (TextView) viewGroup.findViewById(R.id.tvAlertTitle);
        TextView tvAlertMsg = (TextView) viewGroup.findViewById(R.id.tvAlertMsg);
        tvAlertTitle.setTextColor(ContextCompat.getColor(context, R.color.textColor_alert_button_cancel));
        if(title != null) {
            tvAlertTitle.setText(title);
        }else{
            tvAlertTitle.setVisibility(View.GONE);
        }
        if(msg != null) {
            tvAlertMsg.setText(msg);
        }else{
            tvAlertMsg.setVisibility(View.GONE);
        }
    }
    protected void initShareListView(){

        if (App.APP_KEY == null){
            throw new NullPointerException("App key not be null");
        }
        if (App.QQID == null){
            throw new NullPointerException("QQ id not be null");
        }
        if (App.WX_APP_ID == null){
            throw new NullPointerException("WX app id not be null");
        }
        switch (type){
            case TYPE_APP:{
                url = appShareLink;
            }
            break;
            default:{
                url = Port._PORT;
            }
            break;
        }

        //TODO:实例化 WeChat
        iwxapi = WXAPIFactory.createWXAPI(context, App.WX_APP_ID);
        iwxapi.registerApp(App.WX_APP_ID);

        /**
         * 微信分享 （这里仅提供一个分享网页的示例，其它请参看官网示例代码）
         * @param flag(0:分享到微信好友，1：分享到微信朋友圈)
         */
        WXWebpageObject webpage = new WXWebpageObject();
        webpage.webpageUrl = url;
        WXMediaMessage msg = new WXMediaMessage(webpage);
        msg.title = "title";
        msg.description = userName+" 分享给您的";
        Bitmap thumb = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_com_sina_weibo_sdk_logo);
        msg.thumbData = BasicUtil.bmpToByteArray(thumb, true);
        req = new SendMessageToWX.Req();
        req.transaction = String.valueOf(System.currentTimeMillis());
        req.message = msg;

        //TODO:WeiBo
        // 创建微博 SDK 接口实例
        weiboShareAPI = WeiboShareSDK.createWeiboAPI(context, App.APP_KEY);


        //TODO: Tencent
        tencent = Tencent.createInstance(App.QQID, context);



        ImageView circle = (ImageView) contentContainer.findViewById(R.id.circle);
        ImageView weChat = (ImageView) contentContainer.findViewById(R.id.weChat);
        ImageView weiBo = (ImageView) contentContainer.findViewById(R.id.weiBo);
        ImageView qq = (ImageView) contentContainer.findViewById(R.id.qq);
        circle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onItemClickListener != null)onItemClickListener.onItemClick(AlertView.this,0);
                req.scene = SendMessageToWX.Req.WXSceneTimeline;
                iwxapi.sendReq(req);
                dismiss();
            }
        });
        weChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onItemClickListener != null)onItemClickListener.onItemClick(AlertView.this,1);
                if (type == TYPE_APP) {
                    SendMessageToWX.Req req;
                    WXWebpageObject webpage = new WXWebpageObject();
                    webpage.webpageUrl = appShareLink;
                    WXMediaMessage msg = new WXMediaMessage(webpage);
                    msg.title = "title";
                    msg.description = "description";
                    /*Bitmap thumb2 = BitmapFactory.decodeResource(context.getResources(), R.drawable.logo_ic_un);
                    msg.thumbData = Util.bmpToByteArray(thumb2, true);*/
                    req = new SendMessageToWX.Req();
                    req.transaction = String.valueOf(System.currentTimeMillis());
                    req.message = msg;
                    req.scene = SendMessageToWX.Req.WXSceneSession;
                    iwxapi.sendReq(req);
                    req.scene = SendMessageToWX.Req.WXSceneSession;
                    iwxapi.sendReq(req);
                }else {
                    iwxapi.sendReq(req);
                }
                dismiss();
            }
        });
        weiBo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onItemClickListener != null)onItemClickListener.onItemClick(AlertView.this,2);
                //检查版本支持情况
                checkSinaVersin(context);
                weiboShareAPI.registerApp();
                //初始化微博的分享消息
                WeiboMessage weiboMessage = new WeiboMessage();
                weiboMessage.mediaObject = getTextObj(url);
                //初始化从第三方到微博的消息请求
                SendMessageToWeiboRequest request = new SendMessageToWeiboRequest();
                request.transaction = buildTransaction("sinatext");
                request.message = weiboMessage;
                //发送请求信息到微博，唤起微博分享界面
                weiboShareAPI.sendRequest(getActivity(), request);
                dismiss();
            }
        });
        qq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onItemClickListener != null)onItemClickListener.onItemClick(AlertView.this,3);
                ShareListener myListener = new ShareListener();
                final Bundle params = new Bundle();
                params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_DEFAULT);
                params.putString(QQShare.SHARE_TO_QQ_TITLE, "title");
                params.putString(QQShare.SHARE_TO_QQ_SUMMARY, "summary");
                params.putString(QQShare.SHARE_TO_QQ_TARGET_URL, url);
                params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, shareImagePath);

                tencent.shareToQQ(getActivity(), params, myListener);
                dismiss();
            }
        });

    }

    private class ShareListener implements IUiListener {

        @Override
        public void onCancel() {
            // TODO Auto-generated method stub
            Toast.makeText(context, R.string.share_cancel, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onComplete(Object arg0) {
            // TODO Auto-generated method stub
            Toast.makeText(context, R.string.share_success, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onError(UiError arg0) {
            // TODO Auto-generated method stub
            Toast.makeText(context, R.string.share_failure, Toast.LENGTH_SHORT).show();
        }

    }

    Activity activity;

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    private TextObject getTextObj(String text) {
        TextObject textObject = new TextObject();
        textObject.text = text;
        return textObject;
    }

    private String buildTransaction(final String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
    }

    private void checkSinaVersin(final Context context) {
        // 获取微博客户端相关信息，如是否安装、支持 SDK 的版本
        boolean isInstalledWeibo = weiboShareAPI.isWeiboAppInstalled();
        //int supportApiLevel = sinaAPI.getWeiboAppSupportAPI();

        // 如果未安装微博客户端，设置下载微博对应的回调
        if (!isInstalledWeibo) {
            Toast.makeText(context, R.string.no_weibo_client, Toast.LENGTH_SHORT).show();
        }
    }

    Bundle arguments;

    public void setArguments(Bundle arguments) {
        this.arguments = arguments;
    }

    private Bundle getArguments() {
        return arguments;
    }

    protected void initActionShareViews(LayoutInflater layoutInflater) {
        ViewGroup viewGroup = (ViewGroup) layoutInflater.inflate(R.layout.layout_alertview_actionshare,contentContainer);
        initShareHeaderView(viewGroup);

        initShareListView();
        TextView tvAlertCancel = (TextView) contentContainer.findViewById(R.id.tvAlertCancel);
        if(cancel != null){
            tvAlertCancel.setVisibility(View.VISIBLE);
            tvAlertCancel.setText(cancel);
        }
        tvAlertCancel.setOnClickListener(new OnTextClickListener(CANCELPOSITION));
    }
    protected void initActionSheetViews(LayoutInflater layoutInflater) {
        ViewGroup viewGroup = (ViewGroup) layoutInflater.inflate(R.layout.layout_alertview_actionsheet,contentContainer);
        initHeaderView(viewGroup);

        initListView();
        TextView tvAlertCancel = (TextView) contentContainer.findViewById(R.id.tvAlertCancel);
        if(cancel != null){
            tvAlertCancel.setVisibility(View.VISIBLE);
            tvAlertCancel.setText(cancel);
        }
        tvAlertCancel.setOnClickListener(new OnTextClickListener(CANCELPOSITION));
    }
    protected void initAlertViews(LayoutInflater layoutInflater) {

        ViewGroup viewGroup = (ViewGroup) layoutInflater.inflate(R.layout.layout_alertview_alert, contentContainer);
        initHeaderView(viewGroup);

        int position = 0;
        //如果总数据小于等于HORIZONTAL_BUTTONS_MAXCOUNT，则是横向button
        if(mDatas.size()<=HORIZONTAL_BUTTONS_MAXCOUNT){
            ViewStub viewStub = (ViewStub) contentContainer.findViewById(R.id.viewStubHorizontal);
            viewStub.inflate();
            LinearLayout loAlertButtons = (LinearLayout) contentContainer.findViewById(R.id.loAlertButtons);
            for (int i = 0; i < mDatas.size(); i ++) {
                //如果不是第一个按钮
                if (i != 0){
                    //添加上按钮之间的分割线
                    View divier = new View(context);
                    divier.setBackgroundColor(context.getResources().getColor(R.color.bgColor_divier));
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams((int)context.getResources().getDimension(R.dimen.size_divier), LinearLayout.LayoutParams.MATCH_PARENT);
                    loAlertButtons.addView(divier,params);
                }
                View itemView = LayoutInflater.from(context).inflate(R.layout.item_alertbutton, null);
                TextView tvAlert = (TextView) itemView.findViewById(R.id.tvAlert);
                tvAlert.setClickable(true);

                //设置点击效果
                if(mDatas.size() == 1){
                    tvAlert.setBackgroundResource(R.drawable.bg_alertbutton_bottom);
                }
                else if(i == 0){//设置最左边的按钮效果
                    tvAlert.setBackgroundResource(R.drawable.bg_alertbutton_left);
                }
                else if(i == mDatas.size() - 1){//设置最右边的按钮效果
                    tvAlert.setBackgroundResource(R.drawable.bg_alertbutton_right);
                }
                String data = mDatas.get(i);
                tvAlert.setText(data);

                //取消按钮的样式
                if (data == cancel){
                    tvAlert.setTypeface(Typeface.DEFAULT_BOLD);
                    tvAlert.setTextColor(context.getResources().getColor(R.color.textColor_alert_button_cancel));
                    tvAlert.setOnClickListener(new OnTextClickListener(CANCELPOSITION));
                    position = position - 1;
                }
                //高亮按钮的样式
                else if (mDestructive!= null && mDestructive.contains(data)){
                    tvAlert.setTextColor(context.getResources().getColor(R.color.textColor_alert_button_destructive));
                }

                tvAlert.setOnClickListener(new OnTextClickListener(position));
                position++;
                loAlertButtons.addView(itemView,new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT, 1));
            }
        }
        else{
            ViewStub viewStub = (ViewStub) contentContainer.findViewById(R.id.viewStubVertical);
            viewStub.inflate();
            initListView();
        }
    }
    protected void init() {
        inAnim = getInAnimation();
        outAnim = getOutAnimation();
    }
    protected void initEvents() {
    }
    public AlertView addExtView(View extView){
        loAlertHeader.addView(extView);
        return this;
    }
    /**
     * show的时候调用
     *
     * @param view 这个View
     */
    private void onAttached(View view) {
        decorView.addView(view);
        contentContainer.startAnimation(inAnim);
    }
    /**
     * 添加这个View到Activity的根视图
     */
    public void show() {
        if (isShowing()) {
            return;
        }
        onAttached(rootView);
    }
    /**
     * 检测该View是不是已经添加到根视图
     *
     * @return 如果视图已经存在该View返回true
     */
    public boolean isShowing() {
        View view = decorView.findViewById(R.id.outmost_container);
        return view != null;
    }
    public void dismiss() {
        if (isDismissing) {
            return;
        }

        //消失动画
        outAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                decorView.post(new Runnable() {
                    @Override
                    public void run() {
                        //从activity根视图移除
                        decorView.removeView(rootView);
                        isDismissing = false;
                        if (onDismissListener != null) {
                            onDismissListener.onDismiss(AlertView.this);
                        }
                    }
                });
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        contentContainer.startAnimation(outAnim);
        isDismissing = true;
    }
    public Animation getInAnimation() {
        int res = AlertAnimateUtil.getAnimationResource(this.gravity, true);
        return AnimationUtils.loadAnimation(context, res);
    }

    public Animation getOutAnimation() {
        int res = AlertAnimateUtil.getAnimationResource(this.gravity, false);
        return AnimationUtils.loadAnimation(context, res);
    }

    public AlertView setOnDismissListener(OnDismissListener onDismissListener) {
        this.onDismissListener = onDismissListener;
        return this;
    }

    class OnTextClickListener implements View.OnClickListener{

        private int position;
        public OnTextClickListener(int position){
            this.position = position;
        }
        @Override
        public void onClick(View view) {
            if(onItemClickListener != null)onItemClickListener.onItemClick(AlertView.this,position);
            dismiss();
        }
    }

    /**
     * 主要用于拓展View的时候有输入框，键盘弹出则设置MarginBottom往上顶，避免输入法挡住界面
     */
    public void setMarginBottom(int marginBottom){
        int margin_alert_left_right = context.getResources().getDimensionPixelSize(R.dimen.margin_alert_left_right);
        params.setMargins(margin_alert_left_right,0,margin_alert_left_right,marginBottom);
        contentContainer.setLayoutParams(params);
    }
    public AlertView setCancelable(boolean isCancelable) {
        View view = rootView.findViewById(R.id.outmost_container);

        if (isCancelable) {
            view.setOnTouchListener(onCancelableTouchListener);
        }
        else{
            view.setOnTouchListener(null);
        }
        return this;
    }
    /**
     * Called when the user touch on black overlay in order to dismiss the dialog
     */
    private final View.OnTouchListener onCancelableTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                dismiss();
            }
            return false;
        }
    };
}
