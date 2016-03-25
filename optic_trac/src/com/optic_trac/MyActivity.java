package com.optic_trac;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class MyActivity extends Activity implements OnTouchListener{
    final int STATUS_OK = 0; // получен правильный пакет данных
    final int STATUS_1 = 1; // РїРѕРґРєР»СЋС‡Р°РµРјСЃСЏ
    final int STATUS_2 = 2; // Данные переданы
    final int STATUS_ERROR = 3; // Ошибка при приеме пакета данных

    final int ROLL = 0;
    final int PITCH = 1;
    final int THROTTLE = 2;
    final int YAW = 3;
    final int REG_CH = 5;
    final int YAW_KOEF = 30;
    final int PITCH_KOEF = 30;
    final int ROLL_KOEF = 30;
    final int THROTTLE_KOEF = 30;

    private float angle_x = 0.0f;
    private float angle_y = 0.0f;
    private float acc_x = 0.0f;
    private float acc_y = 0.0f;
    private float acc_z = 0.0f;
    private float gyr_x = 0.0f;
    private float gyr_y = 0.0f;
    private float gyr_z = 0.0f;
    private int sonar_left = 0;
    private int sonar_right = 0;
    private int sonar_forw_l = 0;
    private int sonar_forw_r = 0;
    private int rc_command = 0;

    private int error_count = 0;
    Handler h;
    private static final String TAG = "Optic_bluetooth";
    private static final int REQUEST_ENABLE_BT = 1;
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private ConnectedThread mConnectedThread;
    private TimerThread mTimerThread;
    private CommandThread mCommandThread;

    private int[]out_chanels = {0,0,0,0,0,0,0,0};

    TextView tv_angle_x, tv_angle_y;
    TextView tv_acc_x, tv_acc_y, tv_acc_z;
    TextView tv_gyro_x, tv_gyro_y, tv_gyro_z;
    TextView tv_sonar_l, tv_sonar_f_l, tv_sonar_f_r, tv_sonar_r;
    TextView tv_rc_command;
    TextView tv_out;
    Button btn_fly, btn_land;
    ImageButton btn_yaw_l, btn_yaw_r;
    ImageButton btn_roll_l, btn_roll_r, btn_pitch_f, btn_pitch_b;
    EditText koef;
    CheckBox chb_x, chb_y;

    // SPP UUID сервиса
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    // MAC-адрес Bluetooth модуля
    private static String address = "00:15:FF:F3:1F:DA";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        ImageView view = (ImageView) findViewById(R.id.imageView1);
        view.setOnTouchListener(this);

        tv_angle_x = (TextView)findViewById(R.id.angle_x);
        tv_angle_y = (TextView)findViewById(R.id.angle_y);
        tv_acc_x = (TextView)findViewById(R.id.acc_x);
        tv_acc_y = (TextView)findViewById(R.id.acc_y);
        tv_acc_z = (TextView)findViewById(R.id.acc_z);
        tv_gyro_x = (TextView)findViewById(R.id.gyro_x);
        tv_gyro_y = (TextView)findViewById(R.id.gyro_y);
        tv_gyro_z = (TextView)findViewById(R.id.gyro_z);
        tv_sonar_l = (TextView)findViewById(R.id.sonar_l);
        tv_sonar_f_l = (TextView)findViewById(R.id.sonar_f_l);
        tv_sonar_f_r = (TextView)findViewById(R.id.sonar_f_r);
        tv_sonar_r = (TextView)findViewById(R.id.sonar_r);
        tv_rc_command = (TextView)findViewById(R.id.rc_command);
        tv_out = (TextView)findViewById(R.id.out);

        btn_fly = (Button)findViewById(R.id.fly);
        btn_land = (Button)findViewById(R.id.land);
        btn_yaw_l = (ImageButton)findViewById(R.id.yaw_left);
        btn_yaw_r = (ImageButton) findViewById(R.id.yaw_right);

        koef = (EditText) findViewById(R.id.editText1);
        chb_x = (CheckBox) findViewById(R.id.checkBox_X);
        chb_y = (CheckBox) findViewById(R.id.checkBox_Y);

        OnTouchListener otl_Btn = new OnTouchListener() {
            
            public boolean onTouch(View view, MotionEvent event) {
            	int tmp_koef = Integer.valueOf(koef.getText().toString());
                switch (view.getId()) {
                    case R.id.yaw_left:
                        if (event.getAction() == MotionEvent.ACTION_DOWN) out_chanels[YAW] += YAW_KOEF + tmp_koef;
                        if (event.getAction() == MotionEvent.ACTION_UP) out_chanels[YAW] -= YAW_KOEF + tmp_koef;
                        break;
                    case R.id.yaw_right:
                        if (event.getAction() == MotionEvent.ACTION_DOWN) out_chanels[YAW] -= YAW_KOEF + tmp_koef;
                        if (event.getAction() == MotionEvent.ACTION_UP) out_chanels[YAW] += YAW_KOEF + tmp_koef;
                        break;
                    case R.id.fly:
                        if (event.getAction() == MotionEvent.ACTION_DOWN) out_chanels[THROTTLE] += THROTTLE_KOEF + tmp_koef;
                        if (event.getAction() == MotionEvent.ACTION_UP) out_chanels[THROTTLE] -= THROTTLE_KOEF + tmp_koef;
                        break;
                    case R.id.land:
                        if (event.getAction() == MotionEvent.ACTION_DOWN) out_chanels[THROTTLE] -= THROTTLE_KOEF + tmp_koef;
                        if (event.getAction() == MotionEvent.ACTION_UP) out_chanels[THROTTLE] += THROTTLE_KOEF + tmp_koef;
                        break;
                }
                return false;
            }
        };
        btn_yaw_l.setOnTouchListener(otl_Btn);
        btn_yaw_r.setOnTouchListener(otl_Btn);
        btn_fly.setOnTouchListener(otl_Btn);
        btn_land.setOnTouchListener(otl_Btn);


        h = new Handler() {
            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    case STATUS_OK:
                    	short[] readBuf = (short[]) msg.obj;// если приняли сообщение в Handler
                        decode_message(readBuf);
                        break;
                    case STATUS_ERROR:
                        Log.d(TAG, "...Пакетов с ошибками - " + (++error_count) + "...");
                        break;
                    case STATUS_1:
//                    	byte[] readBuf1 =  (byte[]) msg.obj;
//                    	tv_out.setText("out : " + readBuf1);
//                    	chb_y.setChecked(true);
                        break;
                    case STATUS_2:
                        tv_out.setText("out : " + out_chanels[0] + "; " + out_chanels[1] + "; " + out_chanels[2] + "; " + out_chanels[3] + "; " + out_chanels[4] + "; " + out_chanels[5] + "; " + out_chanels[6] + "; " + out_chanels[7]);
                        break;
                }
            };
        };

        btAdapter = BluetoothAdapter.getDefaultAdapter();		// получаем локальный Bluetooth адаптер
        checkBTState();

    }
    protected void decode_message(short[] readBuf) {
    	angle_x = (readBuf[0] + (readBuf[1] << 8)) / 100.0f;
//    	short tmp_short = (short) ((short) (readBuf[3] << 8) + readBuf[2]);
    	angle_y = (readBuf[2] + (short)(readBuf[3] << 8)) / 100.0f;
    	acc_x = (readBuf[4] + (short)(readBuf[5] << 8)) / 1000.0f;
    	acc_y = (readBuf[6] + (short)(readBuf[7] << 8)) / 1000.0f;
    	acc_z = (readBuf[8] + (short)(readBuf[9] << 8)) / 1000.0f;
    	gyr_x = (readBuf[10] + (short)(readBuf[11] << 8)) / 100.0f;
    	gyr_y = (readBuf[12] + (short)(readBuf[13] << 8)) / 100.0f;
    	gyr_z = (readBuf[14] + (short)(readBuf[15] << 8)) / 100.0f;
    	sonar_left = readBuf[16];
    	sonar_forw_l = readBuf[17];
    	sonar_forw_r = readBuf[18];
    	sonar_right = readBuf[19];
    	rc_command = readBuf[20];
    	
    	tv_angle_x.setText("X : " + String.valueOf(angle_x));
    	tv_angle_y.setText("Y : " + String.valueOf(angle_y));
    	tv_acc_x.setText("X : " + String.valueOf(acc_x));
    	tv_acc_y.setText("Y : " + String.valueOf(acc_y));
    	tv_acc_z.setText("Z : " + String.valueOf(acc_z));
    	tv_gyro_x.setText("X : " + String.valueOf(gyr_x));
    	tv_gyro_y.setText("Y : " + String.valueOf(gyr_y));
    	tv_gyro_z.setText("Z : " + String.valueOf(gyr_z));
    	tv_sonar_l.setText("Son_L : " + String.valueOf(sonar_left));
    	tv_sonar_f_l.setText("Son_F_L : " + String.valueOf(sonar_forw_l));
    	tv_sonar_f_r.setText("Son_F_R : " + String.valueOf(sonar_forw_r));
    	tv_sonar_r.setText("Son_R : " + String.valueOf(sonar_right));
    	tv_rc_command.setText("RC_command : " + String.valueOf(rc_command));
    	
    	Log.d(TAG,readBuf[0] + ";" + readBuf[1] + ";" + readBuf[2] + ";" + readBuf[3] + ";" + readBuf[4] + ";" + readBuf[5] + ";" + readBuf[6] + ";" + readBuf[7] + ";" + readBuf[16] + ";" + readBuf[17] + ";" + readBuf[18] + ";" + readBuf[19]);
	}
    
	@Override
    protected void onResume() {
        super.onResume();

        Log.d(TAG, "...onResume - попытка соединения...");

        // Set up a pointer to the remote node using it's address.
        BluetoothDevice device = btAdapter.getRemoteDevice (address);

        // Two things are needed to make a connection:
        //   A MAC address, which we got above.
        //   A Service ID or UUID.  In this case we are using the
        //     UUID for SPP.
        try {
            btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            errorExit("Fatal Error", "In onResume() and socket create failed: " + e.getMessage() + ".");
        }

        // Discovery is resource intensive.  Make sure it isn't going on
        // when you attempt to connect and pass your message.
        btAdapter.cancelDiscovery();

        // Establish the connection.  This will block until it connects.
        Log.d(TAG, "...РЎРѕРµРґРёРЅСЏРµРјСЃСЏ...");
        try {
            btSocket.connect();
            Log.d(TAG, "...РЎРѕРµРґРёРЅРµРЅРёРµ СѓСЃС‚Р°РЅРѕРІР»РµРЅРѕ Рё РіРѕС‚РѕРІРѕ Рє РїРµСЂРµРґР°С‡Рё РґР°РЅРЅС‹С…...");
        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) {
                errorExit("Fatal Error", "In onResume() and unable to close socket during connection failure" + e2.getMessage() + ".");
            }
        }

        // Create a data stream so we can talk to server.
        Log.d(TAG, "...Создание Socket...");

        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();
        mTimerThread = new TimerThread();
        mTimerThread.start();
        mCommandThread = new CommandThread();
        mCommandThread.start();

    }

    @Override
    protected void onStop() {
        super.onStop();
    }
    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "...In onPause()...");
    }
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	Log.d(TAG, "...Destroy Activity...");
/* 
    	mConnectedThread.stop();
    	mTimerThread.stop();
    	mCommandThread.stop();
*/   	
    	try     {
            btSocket.close();
        } catch (IOException e2) {
            errorExit("Fatal Error", "In onDestroy() and failed to close socket." + e2.getMessage() + ".");
        }
 
    }
    
    private void checkBTState() {
        // Check for Bluetooth support and then check to make sure it is turned on
        // Emulator doesn't support Bluetooth and will return null
        if(btAdapter==null) {
            errorExit("Fatal Error", "Bluetooth не поддерживается");
        } else {
            if (btAdapter.isEnabled()) {
                Log.d(TAG, "...Bluetooth включен...");
            } else {
                //Prompt user to turn on Bluetooth
                Intent enableBtIntent = new Intent(btAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
    }

    private void errorExit(String title, String message){
        Toast.makeText(getBaseContext(), title + " - " + message, Toast.LENGTH_LONG).show();
        finish();
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            short[] buffer = new short[21];  // buffer store for the stream
//            int bytes; // bytes returned from read()
            int in_byte;

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    do {
                        in_byte = mmInStream.read() ;
 //                       h.sendEmptyMessage(STATUS_1);
                    } while ((in_byte & 0x000000FF) != 0xFF);

                    for(int i=0; i < 21; i++) buffer[i] = (short) mmInStream.read();
                    
                    in_byte = mmInStream.read() ;
                    if ((in_byte & 0x000000FF) == 0xFE){
                        h.obtainMessage(STATUS_OK, 6, -1, buffer).sendToTarget();
                    }else{
                        in_byte = (byte) mmInStream.read() ;
                        h.sendEmptyMessage(STATUS_ERROR);
                    }
                } catch (IOException e) {
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(byte[] message) {
//            Log.d(TAG, "...Данные для отправки: " + message + "...");
            try {
                mmOutStream.write(0x99);
                mmOutStream.write(message);
                mmOutStream.write(0x88);
            } catch (IOException e) {
                Log.d(TAG, "...Ошибка отправки данных: " + e.getMessage() + "...");
            }
        }
    }
    
    public boolean onTouch(View v, MotionEvent event) {
    	switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if (chb_x.isChecked()){
				out_chanels[ROLL] = (int) event.getX() - 200;
			}else{
				out_chanels[ROLL] = -((int) event.getX() - 200);
			}
			if (chb_y.isChecked()){
				out_chanels[PITCH] = (int) event.getY() - 200;
			}else{
				out_chanels[PITCH] = -((int) event.getY() - 200);
			}
			break;
		case MotionEvent.ACTION_UP:
			out_chanels[ROLL] = 0;
			out_chanels[PITCH] = 0;
			break;
		case MotionEvent.ACTION_MOVE:
			if (chb_x.isChecked()){
				out_chanels[ROLL] = (int) event.getX() - 200;
			}else{
				out_chanels[ROLL] = -((int) event.getX() - 200);
			}
			if (chb_y.isChecked()){
				out_chanels[PITCH] = (int) event.getY() - 200;
			}else{
				out_chanels[PITCH] = -((int) event.getY() - 200);
			}
			break;
		default:
			break;
		}
		return true;
	}
    
    private class TimerThread extends Thread {
        private byte[] byte_message = new byte[16];
        public TimerThread (){   }
        public void run(){
            while(true){
                for(int tmp = 0; tmp < 8; tmp++) {
                    byte_message[tmp * 2] = (byte) (out_chanels[tmp] & 0xFF);
                    byte_message[tmp * 2 + 1] = (byte) ((out_chanels[tmp] & 0xFF00) >> 8);
                }
                mConnectedThread.write(byte_message);
                h.sendEmptyMessage(STATUS_2);
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    Log.d(TAG, "...Ошибка метода TimerThread.sleep: " + e.getMessage() + "...");
                }
            }
        }
    }
    
    private class CommandThread extends Thread {
    	private boolean isFly;
    	private float position_x;
    	private float position_y;
    	private int landing_count = 0;  // Счетчик для режима посадки 
    	private int takeoff_count = 0;	// Счетчик для режима взлета
    	private int rc_command_last;
    	private long time_ = 0;
    	private long time_last = 0;
    	private double dt;
    	
    	private PID pid_roll;
    	private PID pid_pitch;
    	private PID pid_yaw;
    	
        public CommandThread (){
        	isFly = false;
        	position_x = 0.0f;
        	position_y = 0.0f;
        	
        	pid_roll = new PID();
        	pid_pitch = new PID();
        	pid_yaw = new PID();
        	time_last = System.currentTimeMillis();
        }
        public void run(){
            while(true){
    			time_= System.currentTimeMillis();
    			dt = (time_ - time_last) / 1000.0f;
    			time_last = time_;
    			
    			if (rc_command == 01){   // Полуавтоматический режим
            		if (rc_command != rc_command_last){
            			pid_yaw.reset();
            			pid_roll.reset();
            			rc_command_last = rc_command;
            		}
            			
            		if ((sonar_forw_l !=0 ) && (sonar_forw_r !=0 )){ //выравнивание по передней стенке
            			int error = sonar_forw_l - sonar_forw_r;
            			int cmd = pid_yaw.updatePid(error, dt);
            			out_chanels[YAW] = cmd;
            		}
/*
            		if ((sonar_left !=0 ) && (sonar_right !=0 )){ //выравнивание по боковым стенкам
            			int error = sonar_left - sonar_right;
						int cmd = pid_roll.updatePid(error, dt);
						out_chanels[ROLL] = cmd;
            		}
*/            		
            		
            	}
            	if (rc_command == 02){	 // Автоматический режим
            		if (!isFly){
            			takeoff_count = 50;
            			isFly = true;
            		}
            		
            		if (isFly){
            			if (takeoff_count > 0){
            				out_chanels[2] = 1700;
            				takeoff_count--;
            			}else if (landing_count > 0){
            					out_chanels[2] = 1300;
            					landing_count--; 
            				  }else out_chanels[2] = 1500;
            		}
            		
            	}
            	
            	try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    Log.d(TAG, "...Ошибка метода CommandThread.sleep: " + e.getMessage() + "...");
                }
            }
        }
    }

}
