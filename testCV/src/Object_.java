import org.opencv.core.Point;

public class Object_ {
	public boolean isFinded; 
	public Line_[] lines;
	public Point cent_intersec;
	
	public Object_(){
		lines = new Line_[4];
		for(int i = 0; i < 4; i++ )
			lines[i] = new Line_(0,0,0,0);
		cent_intersec = new Point(0, 0);
		isFinded = false;
	}
	public Object_(Line_ line_1, Line_ line_2, Line_ line_3, Line_ line_4){
		this();
        set( line_1,  line_2,  line_3,  line_4);	
	}
	public void set (Line_ line_1, Line_ line_2, Line_ line_3, Line_ line_4){
		
		lines[0] = line_1;
		lines[1] = line_2;
		lines[2] = line_3;
		lines[3] = line_4;
		Point intersec_1 = intersec(line_1, line_2);
		Point intersec_2 = intersec(line_1, line_4);
		Point intersec_3 = intersec(line_3, line_2);
		Point intersec_4 = intersec(line_3, line_4);
		if ((intersec_1.x >= 0) && (intersec_2.x >= 0) && (intersec_3.x >= 0) && (intersec_4.x >= 0)){
			cent_intersec.x = (intersec_1.x + intersec_2.x + intersec_3.x + intersec_4.x) / 4;
			cent_intersec.y = (intersec_1.y + intersec_2.y + intersec_3.y + intersec_4.y) / 4;
		}else
			cent_intersec = intersec(line_1, line_2);
		
	}
	public Line_[] get(){
		return lines;
	}
 	private Point intersec(Line_ line_1, Line_ line_2){
		Point _intersec = new Point(0, 0);
		
		if ((line_2.A - line_1.A) != 0)   _intersec.x = (line_1.B - line_2.B) / (line_2.A - line_1.A);
		else _intersec.x = -10000;
		
		_intersec.y = _intersec.x * line_1.A + line_1.B; 
		
		if (((line_1.point_1.x - _intersec.x) * (line_1.point_2.x - _intersec.x) <= 0) && 
			((line_2.point_1.x - _intersec.x) * (line_2.point_2.x - _intersec.x) <= 0)){
			return _intersec;
		}else
		return new Point(-1, -1);

	}

}
