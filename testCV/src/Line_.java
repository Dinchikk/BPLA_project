import org.opencv.core.Point;

public class Line_ {
	public Point point_1;
	public Point point_2;
	public Point center;
	public double A;
	public double B;
	public double angle;
	public double length;
	public boolean enable;
	
	public Line_(double val_1, double val_2, double val_3, double val_4) {
		point_1 = new Point();
		point_2 = new Point();
		center = new Point();

		point_1.x = val_1;
		point_1.y = val_2;
		point_2.x = val_3;
		point_2.y = val_4;
		enable = true;
		calc_A_B_angle();
	}
	public Line_(int val_1, int val_2, int val_3, int val_4) {
		point_1 = new Point();
		point_2 = new Point();
		center = new Point();

		point_1.x = val_1;
    	point_1.y = val_2;
    	point_2.x = val_3;
    	point_2.y = val_4;
    	enable = true;
    	calc_A_B_angle();
	}
	public Line_() {
        this(0, 0, 0, 0);
    }
	public Line_(Point _point_1, Point _point_2) {
        this(_point_1.x, _point_1.y, _point_2.x, _point_2.y);
    }
	public Line_(double[] vals) {
		this();
        set(vals);	
	}
	public void set(int val_1, int val_2, int val_3, int val_4) {
		point_1.x = val_1;
    	point_1.y = val_2;
    	point_2.x = val_3;
    	point_2.y = val_4;
    	calc_A_B_angle();
	}
	public void set(double val_1, double val_2, double val_3, double val_4) {
		point_1.x = val_1;
    	point_1.y = val_2;
    	point_2.x = val_3;
    	point_2.y = val_4;
    	calc_A_B_angle();
	}
    public void set(double[] vals) {
        if ((vals != null) && (vals.length == 4)) {
        	point_1.x = vals[0];
        	point_1.y = vals[1];
        	point_2.x = vals[2];
        	point_2.y = vals[3];
        	calc_A_B_angle();
        } else {
        	point_1.x = 0;
        	point_1.y = 0;
        	point_2.x = 0;
        	point_2.y = 0;
        	calc_A_B_angle();
        }
    }
    public Line_ clone() {
        return new Line_(point_1, point_2);
    }
    public boolean _equals(Line_ line){
    	if (((int)this.point_1.x == (int)line.point_1.x) && ((int)this.point_1.y == (int)line.point_1.y) &&
    		((int)this.point_2.x == (int)line.point_2.x) && ((int)this.point_2.y == (int)line.point_2.y)) return true;
    	else return false;
    }
    private void calc_A_B_angle(){
		if (point_1.x == point_2.x) point_2.x++;
		A = (point_1.y - point_2.y) / (point_1.x - point_2.x);
		B = point_1.y - A * point_1.x;
		angle = Math.atan(A);
		center.x = (point_1.x + point_2.x) / 2.0;
		center.y = (point_1.y + point_2.y) / 2.0;
		length = Math.sqrt(Math.pow(point_1.x - point_2.x, 2) + Math.pow(point_1.y - point_2.y, 2));
    }

}
