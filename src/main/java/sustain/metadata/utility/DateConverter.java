package sustain.metadata.utility;

import sustain.metadata.utility.exceptions.DateConvertException;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.regex.Pattern;

/**
 * Created by laksheenmendis on 8/5/20 at 5:20 PM
 */
public class DateConverter {

    // Date formats; yyyy-mm-dd, yyyy/mm/dd, yyyy mm dd, yyyy.mm.dd
//    static Pattern pattern1 = Pattern.compile("^(19|20)\\d\\d[- /.](0[1-9]|1[012])[- /.](0[1-9]|[12][0-9]|3[01])$");
    static Pattern pattern1 = Pattern.compile("^(19|20)\\d\\d[-](0[1-9]|1[012])[-](0[1-9]|[12][0-9]|3[01])$");
    // Date formats; dd-mm-yyyy, dd/mm/yyyy, dd mm yyyy, dd.mm.yyyy
//    static Pattern pattern2 = Pattern.compile("^(0[1-9]|[12][0-9]|3[01])[- /.](0[1-9]|1[012])[- /.](19|20)\\d\\d$");
    static Pattern pattern2 = Pattern.compile("^(0[1-9]|[12][0-9]|3[01])[-](0[1-9]|1[012])[-](19|20)\\d\\d$");
    //UTC formatted date with timestamp (Ex: 2015-06-16T00:00:00.000Z)
    static Pattern pattern3 = Pattern.compile("^(-?(?:[1-9][0-9]*)?[0-9]{4})-(1[0-2]|0[1-9])-(3[01]|0[1-9]|[12][0-9])T(2[0-3]|[01][0-9]):([0-5][0-9]):([0-5][0-9])(\\.[0-9]+)?(Z)?$");
    //2019/02/01 00:00:00
    static Pattern pattern4 = Pattern.compile("^(19|20)\\d\\d[/](0[1-9]|1[012])[/](0[1-9]|[12][0-9]|3[01]) (2[0-3]|[01][0-9]):([0-5][0-9]):([0-5][0-9])$");

    public static Date convert(String dateStr) throws ParseException, DateConvertException {

        long time = 0;

        if(pattern1.matcher(dateStr).matches())
        {
            time =  new SimpleDateFormat("yyyy-MM-dd").parse(dateStr).getTime();
        }
        else if(pattern2.matcher(dateStr).matches())
        {
            time =  new SimpleDateFormat("dd-MM-yyyy").parse(dateStr).getTime();
        }
        else if(pattern3.matcher(dateStr).matches())
        {
            time = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.000'Z'").parse(dateStr).getTime();
        }
        else if(pattern4.matcher(dateStr).matches())
        {
            time = new SimpleDateFormat("yyyy/MM/dd' 'HH:mm:ss").parse(dateStr).getTime();
        }
        else
        {
            System.out.println("Pattern for " + dateStr + " not found.\nPlease include new Pattern in sustain.metadata.utility.DateConverter");
            throw new DateConvertException("Pattern not found for " +  dateStr);
        }

        return new Date(time);
    }
}
