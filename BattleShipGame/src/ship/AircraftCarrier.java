package ship;

/**
 * Aircraft carrier.
 *
 */
public class AircraftCarrier implements Ship{

    @Override
    public String name() {
        return "AircraftCarrier";
    }

    @Override
    public int len() {
        return 5;
    }
} // end of class AircraftCarrier
