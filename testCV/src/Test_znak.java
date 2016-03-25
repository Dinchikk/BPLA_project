import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;

public class Test_znak extends JFrame 
{
	private static int			 W = 640;
	private static int			 H = 480;
	private static JPanel		 c1 = new JPanel();
	private static JPanel 		 c2 = new JPanel();
	private static BufferedImage i1;
	private static BufferedImage i2;
	
    private static  Mat			 rgba;

    private float				 stab_ang_a=0;
    private float				 stab_ang_b=0;
    private static float		 len_to_znak=0;
    private static Point		 coord_znak;
    final static float			 K_LPF = 0.1f;
    
    private static boolean		 toExit = false;

	
	public static void main( String[] args )
	{
		Test_znak idt = new Test_znak();
		i1 = new BufferedImage(640,480,BufferedImage.TYPE_INT_RGB);
		i2 = new BufferedImage(640,480,BufferedImage.TYPE_INT_RGB);
		idt.start();

		System.loadLibrary( Core.NATIVE_LIBRARY_NAME );

		VideoCapture cap = new VideoCapture();
		cap.open(1);
		boolean bool = cap.isOpened();
		System.out.println(bool);
		if (bool){
	        coord_znak = new Point(0, 0);
	        
	        try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	        
	        ZnakLoc zl = new ZnakLoc();
	        Object_ obj = new Object_();
	        
			while(!toExit){
				if (cap.grab()){
					cap.retrieve(rgba);

					obj = zl.find_contour(rgba);
					
            		if (obj.isFinded){
                		Line_[] lines = obj.get();
            			Core.line(rgba, lines[0].point_1, lines[0].point_2, new Scalar(0,0,255,255), 1);
            			Core.line(rgba, lines[1].point_1, lines[1].point_2, new Scalar(0,0,255,255), 1);
            			Core.line(rgba, lines[2].point_1, lines[2].point_2, new Scalar(0,255,0,255), 1);
            			Core.line(rgba, lines[3].point_1, lines[3].point_2, new Scalar(0,255,0,255), 1);
            			Core.circle(rgba, obj.cent_intersec, 5, new Scalar(0,255,255,255));
            		}

            		i1 = MattoBufferedImage(rgba);
					
					idt.repaint();
				}
			}
		}

		cap.release();
		idt.setVisible(false);
		;
	}
	
	private void start() { 
        setLayout(new GridLayout(1, 2)); 
        setSize(W * 2 + 10, H + 35);
        c1.setSize(W, H);
        c2.setSize(W, H);
        add(c1);
        add(c2);
        setTitle("“естирование системы поиска посадочного знака");
        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        addKeyListener(new KeyListener(){

        	@Override
			public void keyPressed(KeyEvent e) {
				toExit = true;				
			}
			
        	@Override
			public void keyReleased(KeyEvent e) {}
			@Override
			public void keyTyped(KeyEvent e) {}
        });
	}

	@Override
    public void paint(Graphics g) {//redraw frame
        c1.getGraphics().drawImage(i1, 0, 0, this);
        c2.getGraphics().drawImage(i2, 0, 0, this);
	}
	
	public static BufferedImage MattoBufferedImage(Mat m){
	      int type = BufferedImage.TYPE_BYTE_GRAY;
	      if ( m.channels() > 1 ) 
	          type = BufferedImage.TYPE_3BYTE_BGR;
	      int bufferSize = m.channels()*m.cols()*m.rows();
	      byte [] b = new byte[bufferSize];
	      m.get(0,0,b); // get all the pixels
	      BufferedImage image = new BufferedImage(m.cols(),m.rows(), type);
	      final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
	      System.arraycopy(b, 0, targetPixels, 0, b.length);  
	      return image;
	  }


}
