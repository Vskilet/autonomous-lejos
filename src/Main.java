import lejos.hardware.port.MotorPort;
import lejos.hardware.port.SensorPort;

public class Main {
    public static void main(String[] args){
        SwagBot bot = new SwagBot(MotorPort.B, MotorPort.C, SensorPort.S1, SensorPort.S2, SensorPort.S3);
        bot.speed((int) bot.getMaxSpeed(),(int) bot.getMaxSpeed());
        while (!bot.isPush()){
            bot.forward();
        }
    }
}
