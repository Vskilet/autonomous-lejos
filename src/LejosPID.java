/**
 * Author : Nick Stracke
 * Licence : MIT
 * Source : https://github.com/kliyer-ai/LejosPID
 */
public class LejosPID {

    private float P=0;
    private float I=0;
    private float D=0;

    private float target=0;
    private float minTarget=0;
    private float maxTarget=0;

    private float minOutput=0;
    private float maxOutput=0;
    private float scale=1;
    private float maxIOutput=0;

    private boolean firstRun=true;
    private float sumError=0;
    private float lastInput=0;

    private float kalmanGain=-1;

    /**
     * Creates the PID controller. Values get normalized.
     * @param p Proportional Term
     * @param i Integral Term
     * @param d Derivative Term
     */
    public LejosPID(float p, float i, float d) {
        P=p;
        I=i;
        D=d;
        normalize();
    }

    public void setP(float p) {
        P = p;
        normalize();
    }

    public void setI(float i) {
        I = i;
        normalize();
    }

    public void setD(float d) {
        D = d;
        normalize();
    }

    /**
     * Scales the output of your PID controller. Sensor inputs are between 0 and 1 but you probably need an output between 0 and 360 that you can pass to your motors. Is set to 1 by default.
     * @param scale The factor by which you want to scale the output.
     */
    public void setScale(float scale){
        if(scale==0) return;
        this.scale=scale;
    }

    /**
     * Restricts maximal output of the I part. As a result, the I term does not stack up infinitely anymore. Can prevent massive overshooting.
     * @param maxIOutput
     */
    public void setMaxIOutput(float maxIOutput) {
        this.maxIOutput = Math.abs(maxIOutput);
    }

    /**
     * Sets targeted value for the PID controller. If goal is to follow a line, value should be between the color values of black and white.
     * @param target
     */
    public void setTarget(float target) {
        this.target = target;
        setTargetRange(0);
    }

    /**
     * Can be used if there is no explicit target or target changes slightly (due to different light conditions for color sensor, ...).
     * @param range No error will be produced between target-range and target+range.
     */
    public void setTargetRange(float range){
        setTargetRange(target-range, target+range);
    }

    /**
     * Can be used if there is no explicit target or target changes slightly (due to different light conditions for color sensor, ...).
     * @param minTarget
     * @param maxTarget
     */
    public void setTargetRange(float minTarget, float maxTarget){
        if(maxTarget < minTarget) return;

        this.minTarget=minTarget;
        this.maxTarget=maxTarget;
    }

    /**
     * Sets output range for SCALED! output. Can be used if you want to make sure that your output is always between 0 - 160.
     * @param range
     */
    //might be confusing
    private void setOutputRange(float range){
        setOutputRange(-range, range);
    }

    /**
     * Sets output range for SCALED! output. Can be used if you want to make sure that your output is always between 50 - 160. So motors would never go slower than 50rpm and never faster than 160rpm.
     * @param minOutput
     * @param maxOutput
     */
    public void setOutputRange(float minOutput, float maxOutput){
        if(maxOutput < minOutput) return;

        this.minOutput=minOutput;
        this.maxOutput=maxOutput;
    }

    /**
     * Activates kalman filter for (sensor) input. Value has to be between 0 and 1. 0 means that you only rely on your previous inputs where as 1 means that you only use your current input.
     * @param kalmanGain
     */
    public void setKalmanFilter(float kalmanGain){
        if(within(kalmanGain, 0, 1))
            this.kalmanGain = kalmanGain;
    }

    /**
     * Calculates the output of the PID controller.
     * @param input The sensor input of your robot
     * @return scaled output of PID controller
     */
    public float getOutput(float input){
        //check for first run
        if(firstRun){
            lastInput = input;
            firstRun = false;
        }

        //check if kalman filter is set
        if(kalmanGain!=-1)
            input = kalmanGain*input + (1-kalmanGain)*lastInput;

        float error;
        if(within(input, minTarget, maxTarget))
            error = 0;
        else
            error = contains(input, minTarget, maxTarget) - input;

        //calculate P term
        float pOutput = scale * P * error;

        //calculate D term
        float dOutput = scale * D * (input - lastInput);
        lastInput = input;

        //calculate I term
        float iOutput = scale *I * sumError;
        if(maxIOutput!=0 && !within(iOutput, -maxIOutput, maxIOutput)){
            iOutput=contains(iOutput, -maxIOutput, maxIOutput);

            //calculate maximal possible sumError
            //maxIOuput = I * sumError <=> sumError = maxIOutput/I
            float maxSumError = maxIOutput/(I*scale); //scales sumError down again
            sumError = contains(sumError+error, -maxSumError, maxSumError);	//sets sumError to highest value possible
        }
        else
            sumError+=error;

        //calculate error and scales it
        float output = pOutput + iOutput + dOutput;

        //check if output exceeds output limits
        //adjust output if necessary and adjust sumError
        //prevents future exceedances
        if(minOutput!=maxOutput && !within(output, minOutput, maxOutput)){
            output = contains(output, minOutput, maxOutput);
            sumError = error;	//makes for smooth transition
        }

        return output;
    }

    public void reset(){
        firstRun = true;
        sumError = 0;
        lastInput = 0;
    }

    private float contains(float target, float min, float max){
        if(target < min)
            return min;
        if(target > max)
            return max;
        return target;
    }

    private boolean within(float target, float min, float max){
        return target >= min && target <= max;
    }

    private void normalize(){
        float p = Math.abs(P);
        float i = Math.abs(I);
        float d = Math.abs(D);
        float sum = p+i+d;

        if(sum==0) sum=1;

        P = p/sum;
        I = i/sum;
        D = d/sum;
    }

}