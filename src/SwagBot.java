import lejos.hardware.BrickFinder;
import lejos.hardware.Button;
import lejos.hardware.LED;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.*;
import lejos.robotics.*;
import lejos.robotics.filter.MeanFilter;
import lejos.utility.Delay;

public class SwagBot{

    private RegulatedMotor motor_left;
    private RegulatedMotor motor_right;
    private EV3IRSensor ultrasonic;
    private SampleProvider ultrasonic_provider;
    private EV3ColorSensor color_sensor;
    private SensorMode rgb_mode;
    private LED led;

    private float white;
    private float black;
    private float[] orange;
    private float midpoint;
    private float max_speed;

    public SwagBot(Port motor_left, Port motor_right, Port port_ultrasonic, Port port_color_sensor) {
        this(motor_left, motor_right, port_ultrasonic);
        this.color_sensor = new EV3ColorSensor(port_color_sensor);
        rgb_mode = this.color_sensor.getRGBMode();
    }

    public SwagBot(Port motor_left, Port motor_right, Port port_ultrasonic) {
        this(motor_left, motor_right);
        this.ultrasonic = new EV3IRSensor(port_ultrasonic);
        this.ultrasonic_provider = this.ultrasonic.getDistanceMode();
    }


    public SwagBot(Port motor_left, Port motor_right) {
        this.motor_left = new EV3LargeRegulatedMotor(motor_left);
        this.max_speed = this.motor_left.getMaxSpeed();
        this.motor_right = new EV3LargeRegulatedMotor(motor_right);

        this.led = BrickFinder.getLocal().getLED();
        this.led.setPattern(0);
    }

    protected void finalize() throws Throwable {
        super.finalize();

        this.motor_left.close();
        this.motor_right.close();
        this.ultrasonic.close();
        this.color_sensor.close();
    }

    public void stop() {
        this.motor_left.setSpeed(0);
        this.motor_right.setSpeed(0);
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

    public float getMaxSpeed(){
        return max_speed;
    }

    public float getWhite() {
        return white;
    }

    public float getBlack() {
        return black;
    }

    public float[] getOrange() { return orange; }

    public float[] rgb_data() {
        float [] rgb_sample = new float[rgb_mode.sampleSize()];
        rgb_mode.fetchSample(rgb_sample, 0);
        return rgb_sample;
    }

    public float mean_rgb() {
        float [] rgb_sample = this.rgb_data();
        return (rgb_sample[0] + rgb_sample[1] + rgb_sample[2])/3;
    }

    public void calibration() {
        System.out.println("Noir");
        Button.waitForAnyPress();
        this.black = this.mean_rgb();
        Delay.msDelay(1000);
        System.out.println("Blanc");
        Button.waitForAnyPress();
        this.white = this.mean_rgb();
        Delay.msDelay(1000);
        System.out.println("Orange");
        Button.waitForAnyPress();
        this.orange = this.rgb_data();

        this.midpoint = ( this.white - this.black ) / 2 + this.black;

        Delay.msDelay(1000);
    }

    public int getTachoCount() {
        return this.motor_left.getTachoCount();
    }

    public void resetTachoCount(){
        this.motor_left.resetTachoCount();
    }

    public void setLEDColor(int pattern){
        led.setPattern(pattern);
    }
}
