package com.wxt.myapplication;

import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SocketActivity extends AppCompatActivity implements MyHandler.InMyHandler {

    //主线程Handler,用于将从服务器获取的消息显示出来
    private MyHandler myHandler;
    //采用线程池进行线程管理
    private ExecutorService mThreadPool;
    //Socket变量
    private Socket socket;
    //输出流
    private OutputStream outputStream;
    //服务器返回的数据转化为StringBuffer
    private StringBuffer responseBuffer;

    //UI控件
    private EditText editText;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //控件
        editText = findViewById(R.id.socket_command);
        textView = findViewById(R.id.socket_content);
        textView.setMovementMethod(ScrollingMovementMethod.getInstance());

        //初始化线程池
        mThreadPool = Executors.newCachedThreadPool();
        //获取处理消息的Handler单例
        myHandler = MyHandler.getInstance();
        //设置接口监听
        myHandler.setInMyHandler(this);
        //开启socket线程
        socket_thread();

    }

    /**
     * 开启socket线程，连接，获取输入输出流
     */
    private void socket_thread() {
        //利用线程池开启一个线程并执行该线程
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    //创建Socket
                    socket = new Socket("192.168.0.xxx", 1000);
                    //判断客户端和服务器是否连接成功
                    if (socket.isConnected()) {
                        //连接成功,message.what=1
                        myHandler.sendEmptyMessage(1);

                        //获取socket的输出流
                        outputStream = socket.getOutputStream();

                        //输入流,读取服务器返回的数据
                        InputStream inputStream = socket.getInputStream();
                        //创建一个字节数组用来接收服务端发来的字节流，多大自己说了算
                        byte[] b = new byte[1024];
                        //写一个死循环，不停的接收服务端发来的数据
                        while (socket.isConnected()) {
                            //将数据读入字节数组，返回长度
                            int length = inputStream.read(b);
                            //如果服务端没发数据就return不在往下执行，继续读数据
                            if (length <= 0) return;
                            //如果服务端发的数据不为空，则把字节数组转成字符串
//                            String str = new String(b, 0, length, "gb2312");
                            String str = new String(b, 0, length);
                            responseBuffer = new StringBuffer(str);
                            myHandler.sendEmptyMessage(2);
                        }

                    } else {
                        //连接失败,message.what=0
                        myHandler.sendEmptyMessage(0);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * socket发送数据
     */
    private void socketSendMessage(final String str) {
        //利用线程池开启一个线程并执行该线程
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    //首先判断一下socket是不是已经创建连接了，如果没有就不管它
                    if (socket == null) return;
                    if (outputStream == null) return;
                    outputStream.write(str.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 连接结果
     */
    private void handlerConnectResult(int result) {
        if (result == 0) {
            Toast.makeText(this, "Connect Failed", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Connect Success", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * socket接收数据
     */
    private void handlerDataFeedBack() {
        StringBuffer stringBuffer = new StringBuffer(textView.getText().toString());
        stringBuffer.append(responseBuffer);
        textView.setText(stringBuffer);
        textView.setGravity(Gravity.BOTTOM);
    }

    /**
     * 更新UI
     */
    @Override
    public void InHandleMessage(Message message) {
        switch (message.what) {
            case 0:
                handlerConnectResult(0);
                break;
            case 1:
                handlerConnectResult(1);
                break;
            case 2:
                handlerDataFeedBack();
                break;
        }
    }

    /**
     * 发送按钮点击事件处理
     */
    public void socket_SendMessage(View view) {
        String str = editText.getText().toString();
        editText.setText("");
        InputMethodManager im = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        im.hideSoftInputFromWindow(this.getWindow().getDecorView().getWindowToken(), 0);
        //判断传入字符串为空则不作处理
        if (str.length() <= 0) return;
        //发送socket数据
        socketSendMessage(str);
    }

    public void socket_backClick(View view) {
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //视图销毁时断开socket连接
        try {
            outputStream = null;//关闭输出流
            socket.close();//关闭socket
            socket = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        mThreadPool = null;//销毁线程池
        myHandler.removeInMyHandler();//移除接口监听
        myHandler = null;
        responseBuffer = null;
    }
}