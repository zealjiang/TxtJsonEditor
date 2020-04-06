package com.example.zealjiang.bean;

/**
 * Created by zhanxiang on 2017/11/29.
 */

public class Point {
    public float x;
    public float y;

    public Point() {

    }

    public Point(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public static Point getRotatePoint(Point a, double angle) {
        double radian = Math.toRadians(angle);
        Point o = new Point(0, 0);
        Point b = new Point();
        b.x = (float) ((a.x - o.x) * Math.cos(radian) - (a.y - o.y) * Math.sin(radian) + o.x);
        b.y = (float) ((a.x - o.x) * Math.sin(radian) + (a.y - o.y) * Math.cos(radian) + o.y);
        return b;

    }

    public static Point getRotatePoint(Point a, Point o, double angle) {
        double radian = Math.toRadians(angle);
        Point b = new Point();
        b.x = (float) ((a.x - o.x) * Math.cos(radian) - (a.y - o.y) * Math.sin(radian) + o.x);
        b.y = (float) ((a.x - o.x) * Math.sin(radian) + (a.y - o.y) * Math.cos(radian) + o.y);
        return b;
    }

    public static Point getMinRectLTPoint(float left, float top, float width, float height, double angle) {
        Point a1 = new Point(left, top);
        Point a2 = new Point(left + width, top);
        Point a3 = new Point(left + width, top + height);
        Point a4 = new Point(left, top + height);
        return getMinRectLTPoint(a1, a2, a3, a4, angle);
    }
    public static Point getMinRectLTPoint(float left, float top, float width, float height, float scale,double angle){
        float newLeft=left-width*(scale-1)/2;
        float newTop=top-height*(scale-1)/2;
        float newHeight=height*scale;
        float newWidth=width*scale;
        return getMinRectLTPoint(newLeft,newTop,newWidth,newHeight,angle);
    }


    public static Point getMinRectLTPoint(Point a1, Point a2, Point a3, Point a4, double angle) {
        Point o=new Point((a2.x-a1.x)/2+a1.x,(a4.y-a1.y)/2+a1.y);

        Point b1 = getRotatePoint(a1,o, angle);
        Point b2 = getRotatePoint(a2,o,angle);
        Point b3 = getRotatePoint(a3,o,angle);
        Point b4 = getRotatePoint(a4,o,angle);

        Point result = new Point(getMin(b1.x, b2.x, b3.x, b4.x), getMin(b1.y, b2.y, b3.y, b4.y));
        return result;
    }

    private static float getMin(float a, float b, float c, float d) {
        return Math.min(Math.min(Math.min(a, b), c), d);
    }

    public static float cos(double angle){
        return (float) Math.cos(Math.toRadians(angle));
    }
    public static float sin(double angle){
        return (float) Math.sin(Math.toRadians(angle));
    }

    public static class Rect{
        public Point p1;
        public Point p2;

        public Rect(Point p1, Point p2) {
            this.p1 = p1;
            this.p2 = p2;
        }
    }

    public static Rect getIntersectionRect( Rect r1,Rect r2){
        //先找出每个矩形的最小x,然后再取两个最小x值中的较大值
        Point ap1=r1.p1;
        Point ap2=r1.p2;
        Point bp1=r2.p1;
        Point bp2=r2.p2;
        float minX = Math.max(Math.min(ap1.x, ap2.x),

                Math.min(bp1.x, bp2.x));

        //先找出每个矩形的最小y,然后再取两个最小y值中的较大值

        float minY = Math.max(Math.min(ap1.y, ap2.y),

                Math.min(bp1.y, bp2.y));

        //先找出每个矩形的最大x,然后再取两个最大x值中的较小值

        float maxX = Math.min(Math.max(ap1.x, ap2.x),

                Math.max(bp1.x, bp2.x));

        //先找出每个矩形的最大y,然后再取两个最大y值中的较小值

        float maxY = Math.min(Math.max(ap1.y, ap2.y),

                Math.max(bp1.y, bp2.y));
        return new Rect(new Point(minX,minY),new Point(maxX,maxY));

    }
}
