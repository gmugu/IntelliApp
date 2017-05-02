package com.gmugu.intelliapp.ui.view;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class MonitoView extends View {

	public static final String TAG = "MonitoView";
	private int width = 320;
	private int height = 240;
	private byte[] mPixel = new byte[width * height * 2];
	private ByteBuffer buffer_temp = ByteBuffer.wrap(mPixel);
	private Bitmap videoBitmap = Bitmap.createBitmap(width, height,
			Config.RGB_565);
	private Rect srcrect = new Rect(0, 0, width, height);
	private Rect desrect = new Rect(0, 0, 320, 240);
	private DatagramSocket socket;
	private DatagramPacket packet;
	private int port = 9406;
	private boolean isRunning;
	private int frame = 0;
	private Paint textPaint = new Paint();

	public MonitoView(Context context) {
		super(context);
	}

	public MonitoView(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		videoBitmap.copyPixelsFromBuffer(buffer_temp);
		buffer_temp.position(0);
		canvas.drawBitmap(videoBitmap, srcrect, desrect, null);
		textPaint.setColor(0xffff0000);
		textPaint.setTextSize(30);
		canvas.drawText("frame:"+frame, 20, 30, textPaint);

	}

	public boolean openMonito() {
		if (socket == null) {
			try {
				socket = new DatagramSocket(port);
			} catch (SocketException e) {
				e.printStackTrace();
				Toast.makeText(getContext(), "open", Toast.LENGTH_LONG).show();
				return false;
			}
			byte buf[] = new byte[1024];
			packet = new DatagramPacket(buf, buf.length);
			isRunning = true;
			DecoderThread dThread = new DecoderThread();
			dThread.start();
		}
		return true;
	}

	public void closeMonito() {
		isRunning = false;
		if (socket!=null) {
			socket.close();
		}
	}

	public void resetMonito() {
		closeMonito();
		// Delay
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		openMonito();
	}
	private long preTime;
	private long count = 0;
	class DecoderThread extends Thread {

		@Override
		public void run() {
			while (isRunning) {
				try {
					socket.receive(packet);
					byte cache[] = packet.getData();
//					int packetSize = packet.getLength();
					int frameIndex = (cache[640] >= 0) ? cache[640]
							: (256 + cache[640]);
//					Log.d(TAG, "packetSize=" + packetSize + "  frameIndex=" + frameIndex);
					System.arraycopy(cache, 0, mPixel, frameIndex * 640, 640);
					if (frameIndex == 239) {
						if (++count == 10) {
							frame = (int) (count*1000/(System.currentTimeMillis() - preTime));
							preTime = System.currentTimeMillis();
							count = 0;
						}
						postInvalidate();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		}
	}

}
