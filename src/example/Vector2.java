package example;

public class Vector2 {
  
  private float x;
  private float y;
  
  public Vector2() {
    this.x = 0;
    this.y = 0;
  }
  
  public String toString() {
    return String.format("[%f, %f]", x, y);
  }
  
  public Vector2(float x, float y) {
    this.x = x;
    this.y = y;
  }

  public Vector2 setX(float x) {
    this.x = x;
    return this;
  }

  public Vector2 setY(float y) {
    this.y = y;
    return this;
  }

  public Vector2 add(float x, float y) {
    this.x += x;
    this.y += y;
    return this;
  }

  public Vector2 set(Vector2 other) {
    this.x = other.x;
    this.y = other.y;
    return this;
  }

  public Vector2 set(float x, float y) {
    this.x = x;
    this.y = y;
    return this;
  }

  public Vector2 add(Vector2 other) {
    this.x += other.x;
    this.y += other.y;
    return this;
  }
  
  public Vector2 sub(int x, int y) {
    this.x -= x;
    this.y -= y;
    return this;
  }

  public Vector2 add(Vector2 other, float multiplier) {
    this.x += other.x * multiplier;
    this.y += other.y * multiplier;
    return this;
  }

  public float getX() {
    return x;
  }

  public float getY() {
    return y;
  }

  public Vector2 limitToMaxAndMin(Vector2 limit) {
    limitToMax(limit, 1);
    limitToMin(limit, -1);
    return this;
  }
  
  public Vector2 limitToMax(Vector2 limit, float multiplier) {
    this.x = Math.min(x, multiplier * limit.getX());
    this.y = Math.min(y, multiplier * limit.getY());
    return this;
  }
  
  public Vector2 limitToMin(Vector2 limit, float multiplier) {
    this.x = Math.max(x, multiplier * limit.getX());
    this.y = Math.max(y, multiplier * limit.getY());
    return this;
  }

  public Vector2 mult(float multiplier) {
    this.x *= multiplier;
    this.y *= multiplier;
    return this;
  }

  public Vector2 sub(Vector2 other) {
    this.x -= other.x;
    this.y -= other.y;
    return this;
  }

  public Vector2 normalize() {
    return mult(1/length());
  }
  
  public float lengthSquared() {
    return (x * x) + (y * y);
  }
  
  public float length() {
    return (float) Math.sqrt((x * x) + (y * y));
  }
  
  public Vector2 addFromAngle(float angle, float multiplier) {
    this.x += multiplier * Math.cos(angle);
    this.y += multiplier * Math.sin(angle);
    return this;
  }

  public float toAngle() {
    return (float) Math.atan2(y, x);
  }

  public boolean is(float x, float y) {
    return this.x == x && this.y == y;
  }

}
