package com.wxt.myapplication;
import android.os.Handler;
import android.os.Message;

/**
 * Handler单例的封装
 */
public class MyHandler extends Handler {
    private MyHandler(){}
    private static class MyHandlerHolder{
        private static final MyHandler INSTANCE = new MyHandler();
    }
    static MyHandler getInstance(){
        return MyHandlerHolder.INSTANCE;
    }//返回线程安全的单例

    @Override
    public void handleMessage(Message message){//重写handleMessage方法
        super.handleMessage(message);
        if (inMyHandler!=null){
            inMyHandler.InHandleMessage(message);//调用接口方法
        }
    }

    private static InMyHandler inMyHandler;//定义接口变量
    public interface InMyHandler{//定义接口
        void InHandleMessage(Message message);//定义接口方法
    }
    void setInMyHandler(InMyHandler inMyHandler){//注册接口的方法
        MyHandler.inMyHandler = inMyHandler;//接口的引用指向它的实例化对象,传入的参数inMyHandler为实现该接口的类的实例化对象
    }
    void removeInMyHandler(){//取消注册接口的方法
        MyHandler.inMyHandler = null;//inMyHandler置为null，inMyHandler将不再持有外部类引用
    }

}