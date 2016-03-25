

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.opencv.imgproc.Imgproc;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Size;

public class ZnakLoc {
	private double  MIN_LINE_LENGTH = 50.0d;
	private final double DIF_DIST = 10.0d;
	private final double DIF_ANGLE = 5 * Math.PI / 180;
	
	private  Mat p_lines;
	private  double[] data;
	private  Line_[] lines;
	private  int lines_num;
	private  Object_ obj;
	private  Point top_left;
	
	private  int           mHistSizeNum = 10;
    private  Mat           mMat0;
    private  MatOfInt      mChannels;
    private  MatOfInt      mHistSize;
    private  MatOfFloat    mRanges;
    private  Mat           hist_znak;
        
    private  Mat			 mIntermediateMat;
    private  Mat			 grayMat;


	public ZnakLoc() {
		p_lines = new Mat();
		top_left = new Point();
	}

	public Object_ find_contour(Mat rgba){
	
		Mat mHierarchy = new Mat();
		Mat mDilatedMask = new Mat();
//		Scalar CONTOUR_COLOR = new Scalar(255,0,0,255); 
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
//		List<MatOfPoint> big_contours = new ArrayList<MatOfPoint>();
		Mat hist = new Mat();
		float[] mBuff = new float[mHistSizeNum];
//		int thikness = 5;
//		Point mP1 = new Point();
//		Point mP2 = new Point();
		
		int [][]find_rect = new int[100][5];
		int rect_index = 0;
		
		grayMat = new Mat();
		mIntermediateMat = new Mat();
		mMat0 = new Mat();
        mChannels = new MatOfInt(0);
        mHistSize = new MatOfInt(mHistSizeNum);
        mRanges = new MatOfFloat(0f, 256f);
        hist_znak = new Mat(10,1,CvType.CV_32FC1);
        hist_znak.put(0, 0, new float[] {12, 25, 8, 3, 0, 0, 12, 50, 110, 80} );
		obj = new Object_();
		
		Imgproc.cvtColor(rgba,grayMat,Imgproc.COLOR_RGB2GRAY);
		Core.normalize(grayMat, grayMat, 255, 0, Core.NORM_INF);
    	
		Imgproc.GaussianBlur(grayMat, grayMat, new Size(5,5), 3);
    	Imgproc.Canny(grayMat, mIntermediateMat, 80, 90);
		Imgproc.dilate(mIntermediateMat, mDilatedMask, new Mat());
		Imgproc.findContours(mDilatedMask, contours, mHierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE); 
        
        Iterator<MatOfPoint> each = contours.iterator();
        while (each.hasNext()) {
            MatOfPoint contour = each.next();
            double area = Imgproc.contourArea(contour);
            if (area > 1500){
            	// find rectangle of contour
            	Point[] pa = contour.toArray();
                Point tl = new Point(pa[0].x,pa[0].y);
                Point br = new Point(pa[0].x,pa[0].y);

                for (int i = 1; i < pa.length; i++){
                    if (pa[i].x > br.x) br.x = pa[i].x;
                    if (pa[i].x < tl.x) tl.x = pa[i].x;
                    if (pa[i].y > br.y) br.y = pa[i].y;
                    if (pa[i].y < tl.y) tl.y = pa[i].y;
                }
                int top = (int) (br.x - tl.x);
                int left = (int) (br.y - tl.y);
                if ((3*top < left) ||(3*left < top)) continue;

                Mat contMat = grayMat.submat((int)tl.y, (int)br.y, (int)tl.x, (int)br.x);
            	Imgproc.calcHist(Arrays.asList(contMat), mChannels, mMat0, hist, mHistSize, mRanges);

            	hist.get(0, 0, mBuff);
                float summ = 0;
                float max = 0;
                for(float i: mBuff){
                	summ += i;
                	if (i > max) max = i;
                }
            	Core.normalize(hist, hist, max * 300 / summ, 0, Core.NORM_INF);
                double comp = Imgproc.compareHist(hist_znak, hist, Imgproc.CV_COMP_CHISQR);
            	if (comp < 500){
            		find_rect[rect_index][0] = (int) tl.x;
            		find_rect[rect_index][1] = (int) tl.y;
            		find_rect[rect_index][2] = (int) br.x;
            		find_rect[rect_index][3] = (int) br.y;
            		find_rect[rect_index][4] = (int) comp;
            		
            		find_lines(mIntermediateMat.submat((int)tl.y, (int)br.y, (int)tl.x, (int)br.x), tl);
            		if (obj.isFinded){
            			System.out.println("Contour: " + tl + " " + br + " " + comp);
            			rect_index++;
            			break;
            		}
            	}
            }
        }
        return obj;
		
	}

	private void find_lines(Mat innerMat, Point tl) {
	
		Imgproc.HoughLinesP(innerMat, p_lines, 1, Math.PI/180, 50,30,20);
		MIN_LINE_LENGTH = Math.min(innerMat.cols(), innerMat.rows()) / 3.0;
		top_left = tl;
		lines_num = p_lines.cols();		
		lines = new Line_ [lines_num];
		obj = new Object_();
		
		for(int i = 0; i < lines_num; i++){
			data = p_lines.get(0, i);
			lines[i] = new Line_(data[0] + top_left.x , data[1] + top_left.y, data[2] + top_left.x, data[3] + top_left.y);
			if ( lines[i].length < MIN_LINE_LENGTH ) lines[i].enable = false; //Отсекаем короткие линии
		}
		
		for(int i = 0; i < lines_num; i++){ //Отсекаем близкорасположенные линии
			if (!lines[i].enable) continue;
			for(int j = i + 1; j < lines_num; j++){
				if (lines[j].enable){
					if ((Math.abs(lines[i].angle - lines[j].angle) < DIF_ANGLE)&&
					    (Distanse(lines[i].center, lines[j].center) < DIF_DIST)) 
						lines[j].enable = false;
				}
			}
		}
		
//		find_point:
		for(int line_idx_1 = 0; line_idx_1 < lines_num; line_idx_1++){
			
			if (!lines[line_idx_1].enable) continue;
			
			for(int line_idx_2 = line_idx_1 + 1; line_idx_2 < lines_num; line_idx_2++){
				
				if (lines[line_idx_2].enable){
					
					if ((Math.abs(lines[line_idx_1].angle - lines[line_idx_2].angle) < Math.PI / 2 + DIF_ANGLE) &&  
						(Math.abs(lines[line_idx_1].angle - lines[line_idx_2].angle) > Math.PI / 2 - DIF_ANGLE)) { //Пересекаются ли линии под прямым уголм?
						
						if ( isIntersec(line_idx_1, line_idx_2) ){
							
							int line_3_idx = find_parallel_line(line_idx_1);
							if ( line_3_idx >= 0 ){
								
								int line_4_idx = find_parallel_line(line_idx_2);
								if ( line_4_idx >= 0 ){
									
									obj.set(lines[line_idx_1],lines[line_idx_2], lines[line_3_idx], lines[line_4_idx]);
									obj.isFinded = true;
//									return obj;
									//break find_point;
								}
							}
						}	
						/*
						if (((lines[i][0] - intersec.x) * (lines[i][2] - intersec.x) <= 0) && ((lines[j][0] - intersec.x) * (lines[j][2] - intersec.x) <= 0))
							if (Math.abs((lines[i][0] + lines[i][2]) / 2 - intersec.x) < (Math.abs(lines[i][0] - lines[i][2]) / 4)){
							}
						*/
					}
				}
//			if (j < lines_num) break;
			}
		}
//		return obj;
	}

	private int find_parallel_line(int line_idx) {
		double dist;
		
		for(int i = 0; i < lines_num; i++){
			if ( (i == line_idx) || (!lines[i].enable) ) continue;
			
			if ( Math.abs(lines[i].angle - lines[line_idx].angle) <  DIF_ANGLE ){ //Проверка на параллельность
				dist = Distanse(lines[i].center, lines[line_idx].center);
				if ( (dist > DIF_DIST) && ( dist < Math.min( lines[i].length, lines[line_idx].length)) ) return i;
			}
		}
		return -1;
	}
	private boolean isIntersec(int line_1, int line_2) {
		Point _intersec = new Point();
		
		if ((lines[line_2].A - lines[line_1].A) != 0)
			 _intersec.x = (lines[line_1].B - lines[line_2].B) / (lines[line_2].A - lines[line_1].A);
		else _intersec.x = -10000;
		_intersec.y = _intersec.x * lines[line_1].A + lines[line_1].B; 
		
		if ((Distanse(_intersec, lines[line_1].center) < (lines[line_1].length / 4)) && 
			(Distanse(_intersec, lines[line_2].center) < (lines[line_2].length / 4))) {
			return true;
		}
		else return false;
	}

	private double Distanse (Point point_1, Point point_2){
		double dist = Math.sqrt(Math.pow(point_1.x - point_2.x, 2) + Math.pow(point_1.y - point_2.y, 2));
		return dist;
	}
}