package sustain.metadata.utility;

import org.junit.Test;
import sustain.metadata.utility.exceptions.DateConvertException;

import java.text.ParseException;

import static org.junit.Assert.*;

/**
 * Created by laksheenmendis on 8/5/20 at 6:20 PM
 */
public class DateConverterTest {

    @Test
    public void convert() {


        String d1 = "2015-06-16T00:00:00.000Z";
        String d2 = "2016/09/09 00:00:00";
        String d3 = "2007-02-08";
        try {
            System.out.println(DateConverter.convert(d1));
        } catch (ParseException | DateConvertException e) {
            e.printStackTrace();
        }
    }
}