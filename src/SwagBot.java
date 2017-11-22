import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3TouchSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorMode;
import lejos.robotics.*;
import lejos.robotics.filter.MeanFilter;
import lejos.utility.Delay;

public class SwagBot{

    private RegulatedMotor motor_left;
    private RegulatedMotor motor_right;
    private EV3UltrasonicSensor ultrasonic;
    private SampleProvider ultrasonic_provider;
    private EV3ColorSensor color_sensor;
    private EV3TouchSensor button_sensor;

    private float white;
    private float black;
    private float midpoint;
    private float max_speed;

    public SwagBot(Port motor_left, Port motor_right, Port port_button_sensor, Port port_ultrasonic, Port port_color_sensor) {
        this(motor_left, motor_right, port_ultrasonic, port_button_sensor);
        this.color_sensor = new EV3ColorSensor(port_color_sensor);
    }

    public SwagBot(Port motor_left, Port motor_right, Port port_button_sensor, Port port_ultrasonic) {
        this(motor_left, motor_right);
        this.ultrasonic = new EV3UltrasonicSensor(port_ultrasonic);
        this.ultrasonic_provider = this.ultrasonic.getDistanceMode();
        this.button_sensor = new EV3TouchSensor(port_button_sensor);
    }


    public SwagBot(Port motor_left, Port motor_right) {
        this.motor_left = new EV3LargeRegulatedMotor(motor_left);
        this.max_speed = this.motor_left.getMaxSpeed();
        this.motor_right = new EV3LargeRegulatedMotor(motor_right);
    }

    protected void finalize() throws Throwable {
        super.finalize();

        this.motor_left.close();
        this.motor_right.close();
        this.ultrasonic.close();
        this.color_sensor.close();
    }

    public boolean isPush(){
        SensorMode touch = button_sensor.getTouchMode();
        float[] sample = new float[touch.sampleSize()];
        touch.fetchSample(sample,0);
        int is_touch = (int)sample[0];
        return (is_touch == 1);
    }

    public void stop() {
        this.motor_left.stop(true);
        this.motor_right.stop();
    }

    public void rotate(float angle) {
        int deg_angle = (int)((180.0/Math.PI)*angle) * 2;
        this.motor_left.rotate(deg_angle, true);
        this.motor_right.rotate(-deg_angle);
    }

    public void forward() {
        this.motor_left.forward();
        this.motor_right.forward();
    }

    public void speed(int speed_left, int speed_right){
        this.motor_left.setSpeed(speed_left);
        this.motor_left.forward();
        this.motor_right.setSpeed(speed_right);
        this.motor_right.forward();
    }

    public float distance() {
        RangeFinder sonar = new RangeFinderAdapter(this.ultrasonic);
        return(sonar.getRange());
    }

    public float distance(int iterations) {
        MeanFilter mean_ultrasonic = new MeanFilter(this.ultrasonic_provider, iterations);
        float[] samples = new float[this.ultrasonic_provider.sampleSize()];
        mean_ultrasonic.fetchSample(samples, 0);
        return(samples[0]);
    }

    public int color() {
        SensorMode color_mode = this.color_sensor.getColorIDMode();
        float [] color_sample = new float[color_mode.sampleSize()];
        this.color_sensor.setFloodlight(true);
        color_mode.fetchSample(color_sample, 0);
        int colorId = (int)color_sample[0];
        return colorId;
    }

    public float getMaxSpeed(){
        return max_speed;
    }

    public float getWhite() {
        return white;
    }

    public float getBlack() {
        return black;
    }

    public float[] rgb_data() {
        SensorMode rgb_mode = this.color_sensor.getRGBMode();
        float [] rgb_sample = new float[rgb_mode.sampleSize()];
        this.color_sensor.setFloodlight(true);
        rgb_mode.fetchSample(rgb_sample, 0);
        return rgb_sample;
    }

    public float mean_rgb() {
        float [] rgb_sample = this.rgb_data();
        return (rgb_sample[0] + rgb_sample[1] + rgb_sample[2])/3;
    }

    public void calibration() {
        System.out.println("Noir");
        while (!this.isPush()){

        }
        this.black = this.mean_rgb();
        Delay.msDelay(1000);
        System.out.println("Blanc");
        while (!this.isPush()){

        }
        this.white = this.mean_rgb();

        this.midpoint = ( this.white - this.black ) / 2 + this.black;

        Delay.msDelay(1000);
    }

    public void line_follower_pid(int speed, float kp, float ki, float kd) {

        float last_error = 0;
        float integral = 0;

        while(!this.isPush()){
            float value = this.mean_rgb();
            float error = this.midpoint - value;
            integral = error + integral;
            float derivative = error - last_error;

            float correction = kp * error + ki * integral + kd * derivative;

            System.out.println(correction);

            float left_speed = speed - correction;
            float right_speed = speed + correction;

            this.speed((int)left_speed, (int)right_speed);

            last_error = error;
        }
    }
}
